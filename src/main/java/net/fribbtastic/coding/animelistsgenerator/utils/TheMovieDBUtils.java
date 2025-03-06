package net.fribbtastic.coding.animelistsgenerator.utils;

import org.apache.commons.text.StringSubstitutor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @author Frederic Eßer
 */
public class TheMovieDBUtils {

    private static final Logger logger = LoggerFactory.getLogger(TheMovieDBUtils.class);

    @SuppressWarnings("FieldCanBeLocal")
    private final String TMDB_FIND_API = "https://api.tmdb.org/3/find/${id}?api_key=${apiKey}&external_source=${source}";
    @SuppressWarnings("FieldCanBeLocal")
    private final String TMDB_EXTERNAL_IDS = "https://api.themoviedb.org/3/${mediaType}/${id}/external_ids?api_key=${apiKey}";
    @SuppressWarnings("FieldCanBeLocal")
    private final List<String> allowedKeys = Arrays.asList("imdb_id", "tvdb_id");

    /**
     * use the IMDB ID to find the TheMovieDB ID and return it
     *
     * @param imdbId the IMDB ID that should be used to search for the TheMovieDB Entry
     * @return the TheMovieDB Entry
     */
    public JSONObject findTheMovieDbIdByIMDB(String imdbId) {
        logger.info("finding the TheMovieDb ID for 'imdb_id'={}", imdbId);

        if (imdbId.equalsIgnoreCase("unknown")) {
            // we cannot search for an unknown IMDB ID
            logger.info("Unknown IMDB ID found - ignoring");
            return null;
        }

        return this.findTheMovieDbId("imdb_id", imdbId);
    }

    /**
     * use the TheTVDB ID to find the TheMovieDB ID and return it
     *
     * @param tvdbId the TheTVDB ID that should be used to search for the TheMovieDB Entry
     * @return the TheMovieDB Entry
     */
    public JSONObject findTheMovieDbIdByTVDB(Object tvdbId) {
        logger.info("finding the TheMovieDb ID for 'tvdb_id'={}", tvdbId);

        if (tvdbId instanceof String) {
            // TheTVDB ID cannot be a String
            logger.info("TMDB ID was a String - ignoring");
            return null;
        }

        return this.findTheMovieDbId("tvdb_id", tvdbId);
    }

    /**
     * get either the movie_results or tv_results {@link JSONArray} from the find response
     * return the ID of the first Object in that Array
     *
     * @param sourceName the name of the source, tvdb_id or imdb_id
     * @param id         the ID that we try to find the TheMovieDB Entry with
     * @return the TheMovieDB Entry
     */
    private JSONObject findTheMovieDbId(String sourceName, Object id) {

        // Construct the Map based on the data we have
        Map<String, String> data = new HashMap<>();
        data.put("id", id.toString());
        data.put("apiKey", Properties.theMovieDbApiKey);
        data.put("source", sourceName);

        // substitute the placeholders with the data
        String url = StringSubstitutor.replace(this.TMDB_FIND_API, data);

        try {
            URI uri = new URI(url);

            // get the response from the TheMovieDB API
            JSONObject response = new JSONObject(Objects.requireNonNull(HTTPUtils.getResponse(uri.toASCIIString())));

            // get the individual JSON Arrays from the response
            JSONArray movieResults = new JSONArray();
            if (response.has("movie_results")) {
                movieResults = response.getJSONArray("movie_results");
            }
            JSONArray tvResults = new JSONArray();
            if (response.has("tv_results")) {
                tvResults = response.getJSONArray("tv_results");
            }

            JSONObject resultItem;

            if (!movieResults.isEmpty() && tvResults.isEmpty()) {
                // there are only Movie Results
                resultItem = movieResults.getJSONObject(0);

            } else if (!tvResults.isEmpty() && movieResults.isEmpty()) {
                // there are only TV Results
                resultItem = tvResults.getJSONObject(0);

            } else {
                logger.warn("There were neither Movie nor TV Results - ignoring");
                return null;
            }

            return resultItem;

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * use the TheMovieDB ID to get all external IDs for it and return it in a map
     *
     * @param tmdbId    the TheMovieDB ID
     * @param mediaType the Media Type for the different API endpoints 'tv' or 'movie'
     * @return a {@link Map} containing the individual IDs found
     */
    public Map<String, Object> getTheMovieDbExternalIds(Integer tmdbId, String mediaType) {
        Map<String, Object> result = new HashMap<>();

        // Construct the Map based on the data we have
        Map<String, String> data = new HashMap<>();
        data.put("id", tmdbId.toString());
        data.put("apiKey", Properties.theMovieDbApiKey);
        data.put("mediaType", mediaType);

        // substitute the placeholders with the data
        String url = StringSubstitutor.replace(this.TMDB_EXTERNAL_IDS, data);

        try {
            URI uri = new URI(url);

            // get the response from the TheMovieDB API
            JSONObject response = new JSONObject(Objects.requireNonNull(HTTPUtils.getResponse(uri.toASCIIString())));

            // only add the allowed keys
            for (String key : this.allowedKeys) {
                if (response.has(key)) {
                    Object value = response.get(key);

                    // only add when the value is not null
                    if (value != null) {
                        result.put(KeyNameUtils.getValueWithId(key), value);
                    }
                }
            }

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return result;
    }
}
