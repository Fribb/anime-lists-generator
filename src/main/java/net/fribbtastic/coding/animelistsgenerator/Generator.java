package net.fribbtastic.coding.animelistsgenerator;

import net.fribbtastic.coding.animelistsgenerator.animeLists.service.AnimeListsService;
import net.fribbtastic.coding.animelistsgenerator.index.IndexService;
import net.fribbtastic.coding.animelistsgenerator.models.AnimeItem;
import net.fribbtastic.coding.animelistsgenerator.animeOfflineDatabase.service.AnimeOfflineDatabaseService;
import net.fribbtastic.coding.animelistsgenerator.themoviedb.service.TheMovieDBService;
import net.fribbtastic.coding.animelistsgenerator.utils.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fribbtastic.coding.animelistsgenerator.utils.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Frederic Eßer
 */
@Component
public class Generator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Generator.class);

    private final AnimeOfflineDatabaseService animeOfflineDatabaseService;
    private final AnimeListsService animeListsService;
    private final TheMovieDBService theMovieDBService;
    private final FileUtils fileUtils;
    private final IndexService indexService;

    public Generator(AnimeOfflineDatabaseService animeOfflineDatabaseService, AnimeListsService animeListsService, TheMovieDBService theMovieDBService, FileUtils fileUtils, IndexService indexService) {
        this.animeOfflineDatabaseService = animeOfflineDatabaseService;
        this.animeListsService = animeListsService;
        this.theMovieDBService = theMovieDBService;
        this.fileUtils = fileUtils;
        this.indexService = indexService;
    }

    /**
     * Generate the final Anime-lists by using the source lists, condense the information on them, and merge them.
     */
    public void generateLists() {
        LOGGER.info("starting generating anime-lists");
        LOGGER.info("Path: {}", Properties.projectPath);

        // create the condensed AnimeOfflineDatabase List
        ArrayList<AnimeItem> parsedAODBItems = this.animeOfflineDatabaseService.generateList();

        // save the condensed list of AnimeOfflineDatabase items
        this.fileUtils.writeToFile(parsedAODBItems, Path.of(Properties.projectPath + File.separator + Constants.ANIMEOFFLINEDB_REDUCED));

        // create the condensed anime-lists list
        ArrayList<AnimeItem> parsedAnimeListsItems = this.animeListsService.generateList();

        // save the condensed list of Anime-Lists items
        this.fileUtils.writeToFile(parsedAnimeListsItems, Path.of(Properties.projectPath + File.separator + Constants.ANIMELISTS_REDUCED));

        // merge the two lists
        ArrayList<AnimeItem> mergedList = this.mergeLists(parsedAnimeListsItems, parsedAODBItems);
        LOGGER.info("Number of merged Items: {}", mergedList.size());

        // request TheMovieDB for IDs if not already available
        this.theMovieDBService.appendMissingIds(mergedList);

        /*
         shardIndexMap contains the following structure:
         outer key (first map): the source (mal, tmdb, imdb, tvdb, anidb, anime-planet, etc)
         inner key (second map): the ID of the item (e.g., mal_id in the merged list)
         value: the array of indices where that ID can be found in the merged list
         */
        Map<String, Map<String, List<Integer>>> shardIndexMap = this.indexService.generateIndex(mergedList);
        Map<String, Map<String, List<Integer>>> sortedShardIndexMap = this.sortIndices(shardIndexMap);

        // save the merged list with pretty print and minified
        this.fileUtils.writeToFile(mergedList, Path.of(Properties.projectPath + File.separator + Constants.ANIME_LISTS_FULL));
        this.fileUtils.writeToFile(mergedList, Path.of(Properties.projectPath + File.separator + Constants.ANIME_LISTS_FULL_MINIFIED), false);
        // save one index file for each source
        for (Map.Entry<String, Map<String, List<Integer>>> shard : sortedShardIndexMap.entrySet()) {
            String source = shard.getKey();

            this.fileUtils.writeToFile(shard.getValue(), Path.of(Properties.projectPath + File.separator + Constants.INDEX_DIRECTORY + File.separator + source + Constants.INDEX_FILENAME_SUFFIX), true);
        }
    }

    /**
     * sort the indexes in the index map of the individual shards
     *
     * @param shardIndexMap the whole index maps
     * @return the sorted index maps
     */
    private Map<String, Map<String, List<Integer>>> sortIndices(Map<String, Map<String, List<Integer>>> shardIndexMap) {

        Map<String, Map<String, List<Integer>>> result = new HashMap<>();

        for (Map.Entry<String, Map<String, List<Integer>>> shard : shardIndexMap.entrySet()) {
            String source = shard.getKey();
            Map<String, List<Integer>> innerMap = shard.getValue();

            List<String> sortedKeys = new ArrayList<>(innerMap.keySet());
            sortedKeys.sort(this.idComparator());

            Map<String, List<Integer>> sortedInnerMap = new LinkedHashMap<>();

            for (String key : sortedKeys) {
                sortedInnerMap.put(key, innerMap.get(key));
            }

            result.put(source, sortedInnerMap);
        }

        return result;
    }

    /**
     * comparator for sorting the IDs to compare them numerically.
     *
     * @return the comparator
     */
    private Comparator<String> idComparator() {
        return (a,b) -> {
            boolean aNum = a.matches("\\d+");
            boolean bNum = b.matches("\\d+");

            if (aNum && bNum) {
                return Long.compare(Long.parseLong(a), Long.parseLong(b));
            }

            return a.compareTo(b);
        };
    }

    /**
     * merge the two lists by using the AniDB ID as a key.
     *
     * @param parsedAnimeListsItems the list of parsed Anime-Lists items
     * @param parsedAODBItems the list of parsed AnimeOfflineDatabase items
     * @return the sorted and merged list of AnimeItems
     */
    public ArrayList<AnimeItem> mergeLists(ArrayList<AnimeItem> parsedAnimeListsItems, ArrayList<AnimeItem> parsedAODBItems) {
        // create a LinkedHashMap as master to track the Items by AniDB ID and maintain some order
        Map<Integer, AnimeItem> masterMap = new LinkedHashMap<>();
        // create an ArrayList for those items that don't have an AniDB ID and that couldn't be merged
        ArrayList<AnimeItem> noAniDBItems = new ArrayList<>();

        // process the AODB items
        for (AnimeItem aodbItem : parsedAODBItems) {
            if (aodbItem.getAnidb() != null) {
                // add the item to the master map
                masterMap.put(aodbItem.getAnidb(), aodbItem);
            } else {
                // or to the list when there is no AniDB ID
                noAniDBItems.add(aodbItem);
            }
        }

        // process the anime-lists items
        for (AnimeItem animeListsItem : parsedAnimeListsItems) {
            Integer anidbId = animeListsItem.getAnidb();
            if (anidbId != null) {
                // there was an AniDB ID, so we can merge the items
                if (masterMap.containsKey(anidbId)) {
                    // we found a matching AniDB ID
                    masterMap.get(anidbId).merge(animeListsItem);
                } else {
                    // there was no matching AniDB ID, so we add the item to the master map as a new item
                    masterMap.put(anidbId, animeListsItem);
                }
            } else {
                // if there is no AniDB ID, we add the item to the "noID" list
                noAniDBItems.add(animeListsItem);
            }
        }

        // combine the lists
        ArrayList<AnimeItem> mergedList = new ArrayList<>(masterMap.values());
        // sort the items by anidb ID
        mergedList.sort(Comparator.comparing(AnimeItem::getAnidb));
        // add all remaining items that don't have an AniDB ID
        mergedList.addAll(noAniDBItems);

        return mergedList;
    }
}
