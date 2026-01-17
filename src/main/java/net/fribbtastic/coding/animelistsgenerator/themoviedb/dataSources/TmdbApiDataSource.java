package net.fribbtastic.coding.animelistsgenerator.themoviedb.dataSources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.fribbtastic.coding.animelistsgenerator.Constants;
import net.fribbtastic.coding.animelistsgenerator.themoviedb.model.TmdbFindResult;
import net.fribbtastic.coding.animelistsgenerator.themoviedb.model.TmdbItem;
import net.fribbtastic.coding.animelistsgenerator.utils.HTTPUtils;
import net.fribbtastic.coding.animelistsgenerator.utils.Properties;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Frederic Eßer
 */
@Service
public class TmdbApiDataSource implements TheMovieDBDataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TmdbApiDataSource.class);

    private final HTTPUtils httpUtils;
    private final ObjectMapper mapper;

    public TmdbApiDataSource(HTTPUtils httpUtils, ObjectMapper mapper) {
        this.httpUtils = httpUtils;
        this.mapper = mapper;
    }

    /**
     * load the Data from the TMDB API for the given mediaType and id
     *
     * @param mediaType either "tv" or "movie"
     * @param id the TMDB ID of the item
     * @return the {@link TmdbItem} returned by the TMDB API
     */
    @Override
    public TmdbItem loadData(String mediaType, Integer id) {

        Map<String, String> data = Map.of(
                "apiKey", Properties.theMovieDbApiKey,
                "id", id.toString(),
                "mediaType", mediaType
        );
        String url = StringSubstitutor.replace(Constants.TMDB_URL_EXTERNAL_IDS, data);

        try {
            String response = this.httpUtils.getResponse(url);

            return this.mapper.readValue(response, TmdbItem.class);
        }
        catch (JsonProcessingException e) {
            LOGGER.error("Error loading TheMovieDB item for mediaType {} and id {}", mediaType, id, e);
            return null;
        }
    }

    /**
     * use the external source endpoint to find the TMDB ID
     *
     * @param lookupId the ID to lookup
     * @param source the source of the ID (imdb_id, tvdb_id)
     * @return the first TMDB Result item in either movie_results or tv_results
     */
    @Override
    public TmdbFindResult findItem(String lookupId, String source) {
        Map<String, String> data = Map.of(
                "apiKey", Properties.theMovieDbApiKey,
                "id", lookupId,
                "source", source
        );

        String url = StringSubstitutor.replace(Constants.TMDB_URL_FIND_BY_EXTERNAL_ID, data);

        try {
            String response = this.httpUtils.getResponse(url);

            return this.mapper.readValue(response, TmdbFindResult.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error loading TheMovieDB item for external ID {} and source {}", lookupId, source, e);
            return null;
        }
    }
}
