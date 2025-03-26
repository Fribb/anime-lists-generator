package net.fribbtastic.coding.animelistsgenerator;

import net.fribbtastic.coding.animelistsgenerator.utils.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

/**
 * @author Frederic Eßer
 */
public class Generator {

    private static final Logger logger = LoggerFactory.getLogger(Generator.class);

    @SuppressWarnings("FieldCanBeLocal")
    private final String animeListFull = "anime-list-full.json";
    @SuppressWarnings("FieldCanBeLocal")
    private final AnimeListsUtils animeListsUtils;
    @SuppressWarnings("FieldCanBeLocal")
    private final TheMovieDBUtils theMovieDBUtils;
    /**
     * start the anime-lists generation process
     */
    public Generator() {
        logger.info("starting generating anime-lists");

        logger.info(Properties.projectPath);

        // init utils
        AnimeOfflineDatabaseUtils animeOfflineDatabaseUtils = new AnimeOfflineDatabaseUtils();
        this.animeListsUtils = new AnimeListsUtils();
        this.theMovieDBUtils = new TheMovieDBUtils();

        // get the Anime-Offline-Database as condensed JSONArray
        JSONArray animeOfflineDB = animeOfflineDatabaseUtils.getAnimeOfflineDB();

        // get the Anime-lists as condensed JSONArray
        JSONArray animeLists = animeListsUtils.getAnimeLists();

        // check that the lists actually contain something
        if (animeOfflineDB == null || animeOfflineDB.isEmpty() || animeLists == null || animeLists.isEmpty()) {
            logger.error("Lists are empty or there was an error creating them");
            return;
        }

        // save the individual condensed lists as files
        FileUtils.writeFile(animeOfflineDB.toString(), animeOfflineDatabaseUtils.getFilePath());
        FileUtils.writeFile(animeLists.toString(), animeListsUtils.getFilePath());

        // merge both lists and save it as file
        JSONArray fullList = this.mergeLists(animeOfflineDB, animeLists);
        FileUtils.writeFile(fullList.toString(), this.getAnimeListsFullFilePath());
    }

    /**
     * get the complete file path for the merged full anime-lists
     *
     * @return the file path
     */
    private String getAnimeListsFullFilePath() {
        String path = Properties.projectPath + File.separator + this.animeListFull;
        logger.info("saving full list to file: {}", path);

        return path;
    }

    /**
     * creates and returns a new list of the merged items containing the IDs
     * it will also look up the IDs on the TheMovieDB API to complete the IDs
     *
     * @param animeOfflineDB the Anime-Offline-Database List
     * @param animeLists the anime-lists List
     * @return returns a {@link JSONArray} containing the IDs of both lists
     */
    private JSONArray mergeLists(JSONArray animeOfflineDB, JSONArray animeLists) {
        logger.info("merging lists");
        JSONArray results = new JSONArray();

        for (Object item : animeOfflineDB) {
            JSONObject animeItem = (JSONObject) item;

            if (animeItem.has("anidb_id")) {
                // we can only merge the lists based on the anidb_id
                Integer anidbID = animeItem.getInt("anidb_id");

                // get the anime-lists item for the anidb ID
                JSONObject animeListItem = this.animeListsUtils.getAnimeListsItem(anidbID, animeLists);

                // add the IDs to the animeItem
                for (String key : animeListItem.keySet()) {
                    animeItem.put(key, animeListItem.get(key));
                }

                // get the TheMovieDB Entry
                JSONObject tmdbEntry = null;

                if (animeItem.has("imdb_id") && !animeItem.has("themoviedb_id")) {
                    // TheMovieDB ID is not available, but we have an IMDB ID
                    tmdbEntry = this.theMovieDBUtils.findTheMovieDbIdByIMDB(animeItem.getString("imdb_id"));

                } else if (animeItem.has("thetvdb_id") && !animeItem.has("themoviedb_id")) {
                    // TheMovieDB ID is not available, but we have a TheTVDB ID
                    tmdbEntry = this.theMovieDBUtils.findTheMovieDbIdByTVDB(animeItem.get("thetvdb_id"));
                }

                if (tmdbEntry != null) {
                    // add the TheMovieDB ID to the animeItem
                    Integer tmdbId = tmdbEntry.getInt("id");
                    animeItem.put("themoviedb_id", tmdbId);

                    // get the External IDs for the TheMovieDB ID and add all available IDs to the animeItem
                    Map<String, Object> externalIds = this.theMovieDBUtils.getTheMovieDbExternalIds(tmdbId, tmdbEntry.getString("media_type"));

                    for (String key : externalIds.keySet()) {
                        Object value = externalIds.get(key);

                        animeItem.put(key, value);
                    }
                }
            }
            results.put(animeItem);
        }

        return results;
    }

}
