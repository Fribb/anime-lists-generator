package net.fribbtastic.coding.animelistsgenerator;

import net.fribbtastic.coding.animelistsgenerator.animeLists.service.AnimeListsService;
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

    public Generator(AnimeOfflineDatabaseService animeOfflineDatabaseService, AnimeListsService animeListsService, TheMovieDBService theMovieDBService, FileUtils fileUtils) {
        this.animeOfflineDatabaseService = animeOfflineDatabaseService;
        this.animeListsService = animeListsService;
        this.theMovieDBService = theMovieDBService;
        this.fileUtils = fileUtils;
    }

    /**
     * Generate the final Anime-lists by using the source lists, condense the information on them and merge them together.
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
        theMovieDBService.appendMissingIds(mergedList);

        // save the merged list with pretty print and minified
        this.fileUtils.writeToFile(mergedList, Path.of(Properties.projectPath + File.separator + Constants.ANIME_LISTS_FULL));
        this.fileUtils.writeToFile(mergedList, Path.of(Properties.projectPath + File.separator + Constants.ANIME_LISTS_FULL_MINIFIED), false);

    }

    /**
     * merge the two lists together by using the AniDB ID as a key.
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
