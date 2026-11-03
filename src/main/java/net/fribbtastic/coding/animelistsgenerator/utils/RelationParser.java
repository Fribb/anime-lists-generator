package net.fribbtastic.coding.animelistsgenerator.utils;

import net.fribbtastic.coding.animelistsgenerator.animeOfflineDatabase.models.AODBRelatedAnime;
import net.fribbtastic.coding.animelistsgenerator.animeOfflineDatabase.models.AnimeSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Frederic Eßer
 */
@Component
public class RelationParser {

    /**
     * parse the Array of related anime into a Map of sources that contains a List of Ids
     *
     * @param relatedAnime the Array of related anime
     * @return the Map of sources that contains a List of Ids
     */
    public Map<String, List<String>> parseRelations(List<AODBRelatedAnime> relatedAnime) {
        Map<String, List<String>> result = new HashMap<>();

        for (AODBRelatedAnime related : relatedAnime) {
            String url = related.getUri();
            AnimeSource.fromUrl(url).ifPresent(source -> {
                String id = source.extractId(url);
                if (id == null) {
                    return;
                }

                result.computeIfAbsent(source.getKey(), k -> new ArrayList<>()).add(id);
            });
        }

        return result;
    }

    /**
     * parse the kind of relation of the related anime into a Map of sources that contains a Map of Id to the
     * normalised relation (see {@link #normalise(String)}). Related anime without a relation are left out.
     *
     * @param relatedAnime the Array of related anime
     * @return the Map of sources that contains a Map of Id to relation
     */
    public Map<String, Map<String, String>> parseRelationTypes(List<AODBRelatedAnime> relatedAnime) {
        Map<String, Map<String, String>> result = new HashMap<>();

        for (AODBRelatedAnime related : relatedAnime) {
            String relation = normalise(related.getRelation());
            if (relation.isEmpty()) {
                continue;
            }

            String url = related.getUri();
            AnimeSource.fromUrl(url).ifPresent(source -> {
                String id = source.extractId(url);
                if (id == null) {
                    return;
                }

                result.computeIfAbsent(source.getKey(), k -> new HashMap<>()).put(id, relation);
            });
        }

        return result;
    }

    /**
     * Normalise a relation label so that the labels of the different providers can be compared:
     * lower case, "_" and "-" as spaces, no trailing ":" and single spaces.
     * "SHARED_UNIVERSE", "Shared Universe:" and "shared-universe" all become "shared universe".
     *
     * @param relation the provider's label, may be null
     * @return the normalised label, empty if there is none
     */
    public static String normalise(String relation) {
        if (relation == null) {
            return "";
        }

        String result = relation.toLowerCase(Locale.ROOT).replace('_', ' ').replace('-', ' ').trim();
        while (result.endsWith(":")) {
            result = result.substring(0, result.length() - 1).trim();
        }

        return result.replaceAll("\\s+", " ");
    }
}
