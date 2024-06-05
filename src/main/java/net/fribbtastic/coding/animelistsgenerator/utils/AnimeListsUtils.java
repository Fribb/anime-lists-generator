package net.fribbtastic.coding.animelistsgenerator.utils;

import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author Frederic Eßer
 */
public class AnimeListsUtils {

    private static final Logger logger = LoggerFactory.getLogger(AnimeListsUtils.class);

    @SuppressWarnings("FieldCanBeLocal")
    private final String condensedAnimeListsFileName = "anime-lists-reduced.json";
    @SuppressWarnings("FieldCanBeLocal")
    private final String animeListsURLString = "https://raw.githubusercontent.com/Anime-Lists/anime-lists/master/anime-list-full.xml";

    /**
     * get the JSON List of the anime-lists containing the individual IDs of the Anime
     *
     * @return the {@link JSONArray} with the results from the anime-lists
     */
    public JSONArray getAnimeLists() {
        logger.info("getting anime-lists from source");

        JSONArray results;

        // get the anime-lists from the GitHub Repository
        String animeListsResponse = HTTPUtils.getResponse(this.animeListsURLString);

        if (animeListsResponse != null) {
            JSONObject animeListsObject = XML.toJSONObject(animeListsResponse);
            JSONObject animeListElem = animeListsObject.getJSONObject("anime-list");

            results = this.parseAnimeLists(animeListElem.getJSONArray("anime"));
        } else {
            throw new RuntimeException("anime-lists could not be loaded");
        }

        return results;
    }

    /**
     * Parse the anime-lists to return the Array of only IDs
     *
     * @param animeLists the element of the response that we want to parse
     * @return the {@link JSONArray} with the parsed IDs
     */
    private JSONArray parseAnimeLists(JSONArray animeLists) {
        logger.info("parsing anime-lists response");

        JSONArray results = new JSONArray();

        // iterate over each item in the animeLists array
        for (Object item : animeLists) {
            JSONObject animeItem = (JSONObject) item;
            JSONObject newItem = new JSONObject();

            for (String key : animeItem.keySet()) {
                String shortSource = KeyNameUtils.getValue(key);

                if (shortSource != null) {
                    newItem.put(KeyNameUtils.getValueWithId(key), animeItem.get(key));
                } else {
                    logger.debug("{} not available in the map", key);
                }
            }
            results.put(newItem);
        }
        return results;
    }

    /**
     * get the complete file path for the condensed anime-lists
     *
     * @return the file path
     */
    public String getFilePath() {
        String path = Properties.projectPath + File.separator + this.condensedAnimeListsFileName;
        logger.info("saving condensed anime-lists to path: {}", path);

        return path;
    }

    /**
     * get the specific item from the anime-lists List
     * instead of just searching for the AniDB ID and returning the found element
     * the Anime-Lists uses strings for the TheTVDB ID in cases it is a Movie, OVA or possibly other types.
     * This cannot be blindly added to the list as TVDB ID
     *
     * @param anidbID the AniDB ID that is being used to search
     * @param animeLists the condensed anime-lists list
     * @return the Object that contains the aniDB ID
     */
    public JSONObject getAnimeListsItem(Integer anidbID, JSONArray animeLists) {
        JSONObject result = new JSONObject();
        String aniDbIdName = "anidb_id";
        String tvDbIdName = "thetvdb_id";
        String tmdbIdName = "themoviedb_id";
        String imdbIdName = "imdb_id";

        // iterate over each item of the anime-lists Array
        for (Object item : animeLists) {
            JSONObject animeListsItem = (JSONObject) item;

            // check if the ID matches the ID we are looking for
            if (animeListsItem.get(aniDbIdName).equals(anidbID)) {

                // add the TheTVDB ID if it is available
                if (animeListsItem.has(tvDbIdName)) {
                    // we only want to add Integer IDs
                    Object tvDbId = animeListsItem.get(tvDbIdName);
                    if (tvDbId instanceof Integer) {
                        result.put(tvDbIdName, animeListsItem.getInt(tvDbIdName));
                    } else {
                        // it isn't an integer, try to parse it into one
                        try {
                            Integer id = NumberUtils.createInteger(tvDbId.toString());
                            result.put(tvDbIdName, id);
                        } catch (NumberFormatException e) {
                            logger.warn("TheTVDB ID wasn't an integer for anidbID '{}' and couldn't be parsed into one! Got ID '{}'", anidbID, tvDbId);
                        }
                    }
                }

                // add TheMovieDB ID if it is available
                if (animeListsItem.has(tmdbIdName)) {
                    Object tmdbId = animeListsItem.get(tmdbIdName);

                    if (tmdbId instanceof Integer) {
                        // The ID element is an integer, it is the actual ID and can be added
                        result.put(tmdbIdName, animeListsItem.getInt(tmdbIdName));
                    } else if (tmdbId instanceof String) {
                        // The ID element is a String, the ID contains multiple IDs
                        // we just add the first ID to the item
                        String[] split = tmdbId.toString().split(",");
                        result.put(tmdbIdName, split[0]);
                    }
                }

                // add the IMDB ID if it is available
                if (animeListsItem.has(imdbIdName)) {
                    String imdbId = animeListsItem.getString(imdbIdName);

                    // the IMDB ID is already a string but there could be multiple IDs separated by a ','
                    if (imdbId.contains(",")) {
                        String[] split = imdbId.split(",");
                        result.put(imdbIdName, split[0]);
                    } else {
                        result.put(imdbIdName, imdbId);
                    }
                }
            }
        }

        return result;
    }
}
