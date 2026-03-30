package net.fribbtastic.coding.animelistsgenerator.themoviedb.service;

import net.fribbtastic.coding.animelistsgenerator.models.AnimeItem;
import net.fribbtastic.coding.animelistsgenerator.themoviedb.dataSources.TheMovieDBDataSource;
import net.fribbtastic.coding.animelistsgenerator.themoviedb.model.TmdbFindResult;
import net.fribbtastic.coding.animelistsgenerator.themoviedb.model.TmdbItem;
import net.fribbtastic.coding.animelistsgenerator.themoviedb.model.TmdbMovieResult;
import net.fribbtastic.coding.animelistsgenerator.themoviedb.model.TmdbTvResult;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.StringJoiner;

/**
 * @author Frederic Eßer
 */
@Service
public class TheMovieDBService {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(TheMovieDBService.class);

    private final TheMovieDBDataSource dataSource;

    public TheMovieDBService(TheMovieDBDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void appendMissingIds(ArrayList<AnimeItem> itemList) {
        LOGGER.info("Appending missing IDs to anime list items");

        for (AnimeItem item : itemList) {
            if (this.shouldIgnoreTmdbId(item)) {
                LOGGER.warn(
                        "Item with AniDB ID [{}] is typed as MOVIE but its TMDB ID [{}] came from tmdbtv; ignoring it and re-resolving from external IDs",
                        item.getAnidb(),
                        item.getTheMovieDb()
                );
                item.setTheMovieDb(null);
                item.setTmdbIdOrigin(null);
            }

            LOGGER.info("Processing item with TMDB ID: [{}], TVDB ID: [{}], IMDB ID: [{}], Type: [{}]", item.getTheMovieDb(), item.getTvdb(), item.getImdb(), item.getType());
            boolean tmdbId = item.getTheMovieDb() != null;
            boolean tvdbId = item.getTvdb() != null;
            boolean imdbId = item.getImdb() != null;
            String type = item.getType();

            if (tmdbId) {
                // the item has a TheMovieDB ID set

                if (!tvdbId || !imdbId) {
                    // either tvdb id or imdb id is not set
                    LOGGER.debug("TMDB ID [{}] available, TVDB ID [{}] or IMDB ID [{}] missing", item.getTheMovieDb(), item.getTvdb(), item.getImdb());

                    // TheMovieDB API requests can only be done when we have a mediaType
                    if (type != null) {
                        /*
                        TheMovieDB distinguishes the items between TV and Movie API endpoints
                        The TMDB ID is shared between those two, the same ID can therefore be available for both

                        The Type of the Anime can be more than just TV or Movie it can be: Special, ONA, OVA, etc.
                        Since we cannot specifically tell what a Special, ONA, OVA is, we will only process items that either have TV or MOVIE as Type
                         */
                        String mediaType = null;
                        if (type.equals("TV")) {
                            // set the media type to "tv", request the external IDs for the TMDB
                            mediaType = "tv";
                        } else if (type.equals("MOVIE")) {
                            // set the media type to "movie", request the external IDs for the TMDB
                            mediaType = "movie";
                        } else {
                            LOGGER.info("Item with TMDB ID [{}] has unsupported type [{}], skipping", item.getTheMovieDb(), type);
                        }

                        if (mediaType != null) {
                            // use the TMDB ID to retrieve the external IDs from the TMDB API
                            this.updateInfoFromTmdb(item, mediaType);
                        }
                    }
                } else {
                    // both tvdb id and imdb id are set, so we don't need to do anything
                    LOGGER.info("TMDB ID [{}], TVDB ID [{}], IMDB ID [{}] available -> nothing to do here", item.getTheMovieDb(), item.getTvdb(), item.getImdb());
                }
            } else {
                // the item has no TheMovieDB ID set
                LOGGER.debug("TMDB ID missing, need to look it up");

                String source = null;
                String lookupId = null;
                if (item.getImdb() != null) {
                    LOGGER.info("IMDB ID [{}] available, looking up TMDB ID", item.getImdb());

                    source = "imdb_id";
                    lookupId = item.getImdb();
                } else if (item.getTvdb() != null) {
                    LOGGER.info("TVDB ID [{}] available, looking up TMDB ID", item.getTvdb());

                    source = "tvdb_id";
                    lookupId = item.getTvdb().toString();
                } else {
                    LOGGER.info("No IMDB or TVDB ID available, cannot look up TMDB ID");
                }

                if (lookupId != null || source != null) {
                    // find the ID on TMDB to get the TMDB ID
                    TmdbFindResult findResult = this.dataSource.findItem(lookupId, source);

                    if (findResult != null) {

                        Integer foundTmdbID = null;
                        String mediaType = null;

                        /*
                         TMDB will respond with different result objects, movie_results or tv_results
                         either of them can be set, depending on the media_type (tv would be in tv_results, movie in movie_results)
                         We need to extract the ID of the available result and add it to the item.
                         */
                        if (findResult.getMovieResults() != null && !findResult.getMovieResults().isEmpty()) {
                            LOGGER.debug("Found Movie results for source [{}] with ID [{}]",source, lookupId);
                            TmdbMovieResult movieResult = findResult.getMovieResults().getFirst();

                            foundTmdbID = movieResult.getId();
                            mediaType = movieResult.getMediaType();

                        } else if (findResult.getTvResults() != null && !findResult.getTvResults().isEmpty()) {
                            LOGGER.debug("Found TV results for source [{}] with ID [{}]",source, lookupId);
                            TmdbTvResult tvResult = findResult.getTvResults().getFirst();

                            foundTmdbID = tvResult.getId();
                            mediaType = tvResult.getMediaType();
                        }

                        // Add the TMDB ID to the item, when available
                        if (foundTmdbID != null) {
                            item.setTheMovieDb(foundTmdbID);
                            item.setTmdbIdOrigin(null);

                            // use the TMDB ID to retrieve the external IDs from the TMDB API
                            this.updateInfoFromTmdb(item, mediaType);

                        } else {
                            LOGGER.info("TMDB Lookup returned nothing for source [{}] with ID [{}]", source, lookupId);
                        }
                    } else {
                        LOGGER.info("TMDB Lookup returned nothing.");
                    }
                }

            }
            LOGGER.info("Finished processing item with TMDB ID: [{}], TVDB ID: [{}], IMDB ID: [{}], Type: [{}]", item.getTheMovieDb(), item.getTvdb(), item.getImdb(), item.getType());
        }
    }

    private boolean shouldIgnoreTmdbId(AnimeItem item) {
        return item.getTheMovieDb() != null
                && "MOVIE".equalsIgnoreCase(item.getType())
                && item.getTmdbIdOrigin() == AnimeItem.TmdbIdOrigin.TMDB_TV;
    }

    /**
     * Update the IMDB and TVDB ID from the TMDB API
     *
     * @param item the Anime item that will be updated
     * @param mediaType the media Type that should be used for the request
     */
    private void updateInfoFromTmdb(AnimeItem item, String mediaType) {
        TmdbItem theMovieDbItem = this.dataSource.loadData(mediaType, item.getTheMovieDb());
        if (theMovieDbItem != null) {

            StringJoiner updates = new StringJoiner(", ");

            if (theMovieDbItem.getImdb() != null) {
                item.setImdb(theMovieDbItem.getImdb());
                updates.add(String.format("IMDB ID: [%s->%s]", item.getImdb(), theMovieDbItem.getImdb()));
            }
            if (theMovieDbItem.getTvdb() != null) {
                item.setTvdb(theMovieDbItem.getTvdb());
                updates.add(String.format("TVDB ID: [%s->%s]", item.getTvdb(), theMovieDbItem.getTvdb()));
            }

            if (updates.length() > 0) {
                LOGGER.info("Updating item with TMDB ID [{}] from TMDB API [old->new]: {}", item.getTheMovieDb(), updates);
            } else {
                LOGGER.info("No updates needed/possible for item with TMDB ID [{}]", item.getTheMovieDb());
            }
        }
    }
}
