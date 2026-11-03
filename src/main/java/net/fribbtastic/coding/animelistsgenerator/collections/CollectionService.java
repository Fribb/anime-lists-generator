package net.fribbtastic.coding.animelistsgenerator.collections;

import net.fribbtastic.coding.animelistsgenerator.models.AnimeCollection;
import net.fribbtastic.coding.animelistsgenerator.models.AnimeItem;
import net.fribbtastic.coding.animelistsgenerator.utils.Properties;
import net.fribbtastic.coding.animelistsgenerator.utils.RelationParser;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Frederic Eßer
 */
@Service
public class CollectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionService.class);

    // Configurable (application.properties):
    //   collection.excluded-relations  (default "crossover,other,shared universe,character", empty = all relations join)
    //   collection.rejoin-single-neighbour (default true)

    private static final Comparator<String> MIXED_ID_COMPARATOR = (a, b) -> {
        boolean aNumeric = a.matches("\\d+");
        boolean bNumeric = b.matches("\\d+");

        if (aNumeric && bNumeric) {
            return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
        }

        // compare IMDB Ids
        if (a.startsWith("tt") && b.startsWith("tt")) {
            try {
                int aNum = Integer.parseInt(a.substring(2));
                int bNum = Integer.parseInt(b.substring(2));

                return Integer.compare(aNum, bNum);
            } catch (NumberFormatException e) {
                LOGGER.warn("could not parse IMDB ID '{}' and '{}'", a, b);
            }
        }

        return a.compareTo(b);
    };

    /**
     * generate the collections separated by source
     * The graph will only include mutual relations between the anime items
     *
     * @param animeItems the list of AnimeItems
     * @return the map of collections separated by source
     */
    public Map<String, List<AnimeCollection>> generateCollections(Collection<AnimeItem> animeItems) {

        Map<String, Graph<String, DefaultEdge>> graphs = new HashMap<>();
        Map<String, Map<String, AnimeItem>> lookup = new HashMap<>();

        // initialize the grap and lookup
        for (AnimeItem anime : animeItems) {
            Map<String, List<String>> relations = anime.getRelations();
            // don't need to do anything if there are no relations
            if(relations == null) {
                continue;
            }

            for (String source : relations.keySet()) {
                // skip anime-planet because the IDs are the titles that makes it unusable for the collections
                if(source.equals("anime-planet")) {
                    continue;
                }

                graphs.computeIfAbsent(source, s -> new SimpleGraph<>(DefaultEdge.class));
                for (String id : anime.getIdForSource(source)) {
                    lookup.computeIfAbsent(source, s -> new HashMap<>()).put(id, anime);
                }
            }
        }

        Set<String> excludedRelations = parseExcludedRelations(Properties.collectionExcludedRelations);
        // mutual relations which were left out of the graph because of their type
        Map<String, List<String[]>> excludedEdges = new HashMap<>();

        // build the graph
        for (AnimeItem anime : animeItems) {
            Map<String, List<String>> relations = anime.getRelations();
            // again, don't need to do anything if there are no relations
            if(relations == null) {
                continue;
            }

            for (Map.Entry<String, List<String>> entry : relations.entrySet()) {
                String source = entry.getKey();
                // again, skip anime-planet
                if(source.equals("anime-planet")) {
                    continue;
                }

                Graph<String, DefaultEdge> graph = graphs.get(source);
                List<String> selfIds = anime.getIdForSource(source);
                if (graph == null || selfIds == null) {
                    continue;
                }

                selfIds.forEach(graph::addVertex);

                for (String relationId : entry.getValue()) {
                    graph.addVertex(relationId);

                    for (String selfId : selfIds) {
                        if (isMutualRelation(lookup.get(source), selfId, relationId) && !selfId.equals(relationId)) {
                            if (isExcludedRelation(anime, lookup.get(source), source, selfId, relationId, excludedRelations)) {
                                excludedEdges.computeIfAbsent(source, s -> new ArrayList<>()).add(new String[]{selfId, relationId});
                            } else {
                                graph.addEdge(selfId, relationId);
                            }
                        }
                    }
                }
            }
        }

        boolean rejoin = Properties.collectionRejoinSingleNeighbour;
        LOGGER.info("generating collections: excluded-relations={}, rejoin-single-neighbour={}",
                excludedRelations.isEmpty() ? "none" : excludedRelations, rejoin);

        // extract collections
        Map<String, List<AnimeCollection>> result = new HashMap<>();
        for (Map.Entry<String, Graph<String, DefaultEdge>> entry : graphs.entrySet()) {
            String source = entry.getKey();
            Graph<String, DefaultEdge> graph = entry.getValue();
            Map<String, AnimeItem> sourceLookup = lookup.get(source);

            ConnectivityInspector<String, DefaultEdge> inspector = new ConnectivityInspector<>(graph);
            List<Set<String>> connectedSets = inspector.connectedSets();
            if (rejoin) {
                connectedSets = rejoinSingleNeighbourGroups(connectedSets, excludedEdges.getOrDefault(source, List.of()));
            }

            List<AnimeCollection> collections = new ArrayList<>();
            for (Set<String> connectedSet : connectedSets) {
                List<String> sorted = new ArrayList<>(connectedSet);
                sorted.sort(MIXED_ID_COMPARATOR);

                // determine the name of the collection
                String name = this.determineCollectionName(sorted, sourceLookup);

                // convert the ID to the correct format
                List<Object> convertedIds = sorted.stream().map(this::convertId).toList();

                collections.add(new AnimeCollection(name, convertedIds));
            }

            result.put(source, collections);
        }

        return result;
    }

    /**
     * Groups which were only cut off by excluded relations go back into the collection they were cut off from, as
     * long as there is just one such collection. A group touching several collections through excluded relations
     * (a crossover, an advert for several franchises) stays on its own.
     * <p>
     * A group rejoins its neighbour if it is the only neighbouring group, it is larger and has at least two entries.
     * The groups are compared as they were before rejoining, so nothing is moved twice: a neighbour which takes in a
     * group has that group as neighbour too and can't be the smaller one.
     *
     * @param groups the connected sets of the graph without the excluded relations
     * @param excludedEdges the mutual relations left out of the graph because of their type
     * @return the groups after rejoining
     */
    static List<Set<String>> rejoinSingleNeighbourGroups(List<Set<String>> groups, List<String[]> excludedEdges) {
        Map<String, Integer> groupOf = new HashMap<>();
        for (int i = 0; i < groups.size(); i++) {
            for (String id : groups.get(i)) {
                groupOf.put(id, i);
            }
        }

        List<Set<Integer>> neighbours = new ArrayList<>();
        for (int i = 0; i < groups.size(); i++) {
            neighbours.add(new HashSet<>());
        }

        for (String[] edge : excludedEdges) {
            // both ends are in the graph, the check only guards against a relation to an unknown ID
            Integer a = groupOf.get(edge[0]);
            Integer b = groupOf.get(edge[1]);
            if (a == null || b == null || a.equals(b)) {
                continue;
            }
            neighbours.get(a).add(b);
            neighbours.get(b).add(a);
        }

        Map<Integer, Set<String>> merged = new LinkedHashMap<>();
        int rejoined = 0;
        for (int i = 0; i < groups.size(); i++) {
            int target = i;
            if (neighbours.get(i).size() == 1) {
                int neighbour = neighbours.get(i).iterator().next();
                int size = groups.get(i).size();
                int neighbourSize = groups.get(neighbour).size();
                if (size < neighbourSize && neighbourSize >= 2) {
                    target = neighbour;
                    rejoined++;
                }
            }
            merged.computeIfAbsent(target, k -> new HashSet<>()).addAll(groups.get(i));
        }

        if (rejoined > 0) {
            LOGGER.debug("rejoined {} groups with their only neighbouring collection", rejoined);
        }

        return new ArrayList<>(merged.values());
    }

    /**
     * parse the comma separated list of excluded relations (normalised like the relation types)
     */
    static Set<String> parseExcludedRelations(String value) {
        Set<String> relations = new HashSet<>();
        if (value == null) {
            return relations;
        }
        for (String relation : value.split(",")) {
            String normalised = RelationParser.normalise(relation);
            if (!normalised.isEmpty()) {
                relations.add(normalised);
            }
        }
        return relations;
    }

    /**
     * Is the relation between A and B one of the excluded kinds (e.g. "character" or "other")? Checked in both
     * directions, because the providers don't always label a relation the same way from both sides.
     *
     * @param anime the anime with ID A
     * @param sourceLookup the lookup map for the source
     * @param source the source
     * @param a the ID of the first anime
     * @param b the ID of the related anime
     * @param excludedRelations the normalised excluded relations
     * @return true if A labels B or B labels A with an excluded relation
     */
    static boolean isExcludedRelation(AnimeItem anime, Map<String, AnimeItem> sourceLookup, String source,
                                      String a, String b, Set<String> excludedRelations) {
        if (excludedRelations.isEmpty()) {
            return false;
        }

        if (excludedRelations.contains(relationType(anime, source, b))) {
            return true;
        }

        AnimeItem other = sourceLookup == null ? null : sourceLookup.get(b);
        return excludedRelations.contains(relationType(other, source, a));
    }

    private static String relationType(AnimeItem anime, String source, String relatedId) {
        if (anime == null || anime.getRelationTypes() == null) {
            return "";
        }
        Map<String, String> types = anime.getRelationTypes().get(source);
        return types == null ? "" : types.getOrDefault(relatedId, "");
    }

    private Object convertId(String id) {
        if (id != null && id.matches("\\d+")) {
            try {
                return Integer.parseInt(id);
            } catch (NumberFormatException e) {
                LOGGER.warn("could not parse ID '{}' as Integer", id);
            }
        }
        return id;
    }

    /**
     * Determine the name of the collection based on the sorted list of IDs.
     *
     * @param sortedList the sorted list of IDs
     * @param lookup the lookup map for the source
     * @return the name of the collection
     */
    private String determineCollectionName(List<String> sortedList, Map<String, AnimeItem> lookup) {

        if(sortedList == null || sortedList.isEmpty()) {
            return "Unknown";
        }

        // if there is no lookup map, just return the first ID as fallback
        if(lookup == null) {
            return sortedList.getFirst();
        }

        for (String id : sortedList) {
            AnimeItem anime = lookup.get(id);
            if(anime != null && anime.getTitle() != null) {
                return anime.getTitle(); // return the earliest title that is available
            }
        }

        return sortedList.getFirst(); // fallback to the first ID if no title is available
    }

    /**
     * Only connect when A and B are mutually related
     *
     * @param sourceLookup the lookup map for the source
     * @param a the ID of the first anime
     * @param b the ID of the second anime
     * @return true if A and B are mutually related, false otherwise
     */
    private boolean isMutualRelation(Map<String, AnimeItem> sourceLookup, String a, String b) {
        if (sourceLookup == null) {
            return true;
        }

        AnimeItem other = sourceLookup.get(b);
        if (other == null || other.getRelations() == null) {
            return false;
        }

        for (List<String> relations : other.getRelations().values()) {
            if (relations.contains(a)) {
                return true;
            }
        }

        return false;
    }
}
