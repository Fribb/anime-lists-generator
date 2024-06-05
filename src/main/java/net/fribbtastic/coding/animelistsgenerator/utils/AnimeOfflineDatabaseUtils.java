package net.fribbtastic.coding.animelistsgenerator.utils;

import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Frederic Eßer
 */
public class AnimeOfflineDatabaseUtils {

    private static final Logger logger = LoggerFactory.getLogger(AnimeOfflineDatabaseUtils.class);

    @SuppressWarnings("FieldCanBeLocal")
    private final String condensedAnimOfflineDBFileName =  "anime-offline-database-reduced.json";
    @SuppressWarnings("FieldCanBeLocal")
    private final String animeOfflineDBURLString = "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/anime-offline-database.json";

    /**
     * get the JSON List of the Anime-Offline-Database containing the individual IDs of the Anime
     *
     * @return the {@link JSONArray} with the Results from the Anime-Offline-Database
     */
    public JSONArray getAnimeOfflineDB() {
        logger.info("getting Anime-Offline-Database from source");
        JSONArray results;

        // get the anime-offline-database from the GitHub Repository
        String animeOfflineDBResponse = HTTPUtils.getResponse(this.animeOfflineDBURLString);

        if (animeOfflineDBResponse != null) {
            JSONObject animeOfflineDBObject = new JSONObject(animeOfflineDBResponse);

            results = this.parseAnimeOfflineDatabase(animeOfflineDBObject.getJSONArray("data"));
        } else {
            throw new RuntimeException("Anime-Offline-Database could not be loaded");
        }

        return results;
    }

    /**
     * Parse the Data Array from the Anime-Offline-Database to return the Array of only IDs
     *
     * @param data the "data" element of the response containing the {@link JSONArray}
     * @return the {@link JSONArray} with the parsed IDs
     */
    public JSONArray parseAnimeOfflineDatabase(JSONArray data) {
        logger.info("parsing Anime-Offline-Database response");

        JSONArray results = new JSONArray();

        for (Object item : data) {
            JSONObject animeItem = (JSONObject) item;
            JSONObject newItem = new JSONObject();

            // add the type of the anime item
            if (animeItem.has("type")) {
                newItem.put("type", animeItem.getString("type"));
            }

            // get and add the individual sources of the anime item
            JSONArray sources = animeItem.getJSONArray("sources");
            for (Object s : sources) {
                String source = (String) s;

                try {
                    URI uri = new URI(source);

                    // get the shortSource from the map
                    String host = uri.getHost();

                    // get the ID
                    String path = uri.getPath();
                    String id = path.substring(path.lastIndexOf("/") + 1);

                    // if the ID is an integer, then add the ID as an integer to the newItem, otherwise as String
                    if (NumberUtils.isCreatable(id)) {
                        Integer intId = NumberUtils.createInteger(id);

                        newItem.put(KeyNameUtils.getValueWithId(host), intId);
                    } else {
                        newItem.put(KeyNameUtils.getValueWithId(host), id);
                    }

                }catch (URISyntaxException e) {
                    logger.error("The Syntax of the Anime-Offline-Database URI is not correct", e);
                }
            }

            // add the new anime item to the list
            results.put(newItem);

        }

        return results;
    }

    /**
     * get the complete file path for the condensed Anime-Offline Database
     *
     * @return the file path
     */
    public String getFilePath() {
        String path = Properties.projectPath + File.separator + this.condensedAnimOfflineDBFileName;
        logger.info("saving condensed Anime-Offline-Database to path: {}", path);

        return path;
    }
}
