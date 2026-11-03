package net.fribbtastic.coding.animelistsgenerator.themoviedb.service;

import net.fribbtastic.coding.animelistsgenerator.utils.Properties;

import net.fribbtastic.coding.animelistsgenerator.models.AnimeItem;
import net.fribbtastic.coding.animelistsgenerator.models.TheMovieDBItem;
import net.fribbtastic.coding.animelistsgenerator.themoviedb.dataSources.TheMovieDBDataSource;
import net.fribbtastic.coding.animelistsgenerator.themoviedb.model.TmdbFindResult;
import net.fribbtastic.coding.animelistsgenerator.themoviedb.model.TmdbItem;
import net.fribbtastic.coding.animelistsgenerator.themoviedb.model.TmdbMovieResult;
import net.fribbtastic.coding.animelistsgenerator.themoviedb.model.TmdbTvResult;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
        if (Properties.theMovieDbApiKey == null || Properties.theMovieDbApiKey.isBlank()) {
            LOGGER.warn("No TMDB API key configured (project.apikey.themoviedb) - skipping the TMDB ID lookup");
            return;
        }
        LOGGER.info("Appending missing IDs to anime list items");

        for (AnimeItem item : itemList) {
            LOGGER.info("Processing item with TMDB ID: [{}], TVDB ID: [{}], IMDB ID: [{}], Type: [{}]", item.getTheMovieDb(), item.getTvdb(), item.getImdb(), item.getType());
            boolean tmdb = item.getTheMovieDb() != null;

            if (tmdb) {

                // we have a TMDB object that should have IDs
                this.getTmdbInformationForId(item);

            } else {

                // the item has no TheMovieDB ID set
                LOGGER.debug("TMDB ID missing, need to look it up");

                String source = null;
                String lookupId = null;

                if (item.getImdb() != null) {
                    LOGGER.info("IMDB ID [{}] available, looking up TMDB ID", item.getImdb());

                    source = "imdb_id";
                    // TODO: (at some point) It might make sense to look up every single IMDB ID in the list
                    // Since the change to a list of IDs for the IMDB IDs, technically, I would need go through that list
                    // and look up the TMDB ID for each one. But this would then require that this would then also be checked
                    // against each other so that the resulting ID is the same, because if not, what would the process then be?
                    // this should be fine for now
                    lookupId = item.getImdb().getFirst();
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

                        TheMovieDBItem theMovieDbItem = null;

                        /*
                         TMDB will respond with different result objects, movie_results or tv_results
                         either of them can be set, depending on the media_type (tv would be in tv_results, movie in movie_results)
                         We need to extract the ID of the available result and add it to the item.
                         */
                        if (findResult.getMovieResults() != null && !findResult.getMovieResults().isEmpty()) {
                            LOGGER.debug("Found Movie results for source [{}] with ID [{}]",source, lookupId);
                            TmdbMovieResult movieResult = findResult.getMovieResults().getFirst();

                            Integer foundTmdbID = movieResult.getId();
                            if (foundTmdbID != null) {
                                theMovieDbItem = new TheMovieDBItem();
                                theMovieDbItem.setMovie(new ArrayList<>(List.of(foundTmdbID)));
                            }
                        } else if (findResult.getTvResults() != null && !findResult.getTvResults().isEmpty()) {
                            LOGGER.debug("Found TV results for source [{}] with ID [{}]",source, lookupId);
                            TmdbTvResult tvResult = findResult.getTvResults().getFirst();

                            Integer foundTmdbID = tvResult.getId();
                            if (foundTmdbID != null) {
                                theMovieDbItem = new TheMovieDBItem();
                                theMovieDbItem.setTv(foundTmdbID);
                            }
                        } else {
                            LOGGER.debug("Found no results for source [{}] with ID [{}]",source, lookupId);
                        }

                        item.setTheMovieDb(theMovieDbItem);

                        // use the TMDB ID to retrieve the external IDs from the TMDB API

                        this.getTmdbInformationForId(item);
                    }
                }
            }
            LOGGER.info("Finished processing item with TMDB ID: [{}], TVDB ID: [{}], IMDB ID: [{}], Type: [{}]", item.getTheMovieDb(), item.getTvdb(), item.getImdb(), item.getType());
        }
    }

    /**
     * Get the TMDB information for the given ID
     *
     * @param item the Anime item with all the IDs
     */
    private void getTmdbInformationForId(AnimeItem item) {
        boolean tmdbTvId = item.getTheMovieDb() != null && item.getTheMovieDb().getTv() != null;
        boolean tmdbId = item.getTheMovieDb() != null
                && item.getTheMovieDb().getMovie() != null
                && !item.getTheMovieDb().getMovie().isEmpty();
        boolean tvdbId = item.getTvdb() != null;
        boolean imdbId = item.getImdb() != null;

        if (!tvdbId || !imdbId) {
            // either tvdb id or imdb id is not set
            LOGGER.debug("TMDB ID [{}] available, TVDB ID [{}] or IMDB ID [{}] missing", item.getTheMovieDb(), item.getTvdb(), item.getImdb());

            /*
            TheMovieDB uses separate API endpoints for TV and Movie
            The TMDB ID is shared between those two, the same ID can therefore be available in both for different results

            Depending on which ID is set, TV or Movie, this needs to be used for the endpoint request
             */
            TmdbItem theMovieDbItem = null;
            if (tmdbId) {
                // we have an ID that is a Movie
                theMovieDbItem = this.getTmdbMovieInfo(item.getTheMovieDb().getMovie().getFirst());
                // TODO: (at some point) I would need to gather the TMDB Info for each of those IDs
                // Technically since the item.getTheMovieDb.getMovie() returns a list, I would need to get the info
                // for each of those IDs, but since they are not the same ID, they will not be "the same thing"
                // this should be fine for now
            } else if (tmdbTvId) {
                // we have an ID that is a TV Show
                theMovieDbItem = this.getTmdbTvInfo(item.getTheMovieDb().getTv());
            } else {
                LOGGER.warn("TMDB Object was set but there was no ID, skipping");
            }

            if (theMovieDbItem != null) {
                this.updateInfoFromTmdb(item, theMovieDbItem);
            } else {
                LOGGER.debug("TMDB response for IDs [{}] was null, skipping", item.getTheMovieDb());
            }

        } else {
            // both tvdb id and imdb id are set, so we don't need to do anything
            LOGGER.info("TMDB ID [{}], TVDB ID [{}], IMDB ID [{}] available -> nothing to do here", item.getTheMovieDb(), item.getTvdb(), item.getImdb());
        }
    }

    /**
     * Get the TV Show from the TMDB API with the given ID
     *
     * @param id the TMDB ID of the TV Show
     * @return the {@link TmdbItem} returned by the TMDB API
     */
    private TmdbItem getTmdbTvInfo(Integer id) {
        return this.dataSource.loadData("tv", id);
    }

    /**
     * Get the Movie from the TMDB API with the given ID
     *
     * @param id the TMDB ID of the Movie
     * @return the {@link TmdbItem} returned by the TMDB API
     */
    private TmdbItem getTmdbMovieInfo(Integer id) {
        return this.dataSource.loadData("movie", id);
    }

    /**
     * Update the IMDB and TVDB ID from the TMDB API
     *
     * @param item the Anime item that will be updated
     * @param theMovieDbItem the {@link TmdbItem} returned by the TMDB API
     */
    private void updateInfoFromTmdb(AnimeItem item, TmdbItem theMovieDbItem) {
        if (theMovieDbItem != null) {

            StringJoiner updates = new StringJoiner(", ");

            if (theMovieDbItem.getImdb() != null) {

                List<String> imdbIds = item.getImdb() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(item.getImdb());

                if (!imdbIds.contains(theMovieDbItem.getImdb())) {
                    List<String> oldImdbIds = item.getImdb() == null
                            ? List.of()
                            : item.getImdb();

                    imdbIds.add(theMovieDbItem.getImdb());
                    item.setImdb(imdbIds);

                    updates.add(String.format("IMDB ID: [%s->%s]", oldImdbIds, imdbIds));
                } else if (item.getImdb() != imdbIds) {
                    item.setImdb(imdbIds);
                }
            }
            if (theMovieDbItem.getTvdb() != null) {
                // save the old TVDB ID, so we can log it, and we know that it changed
                Integer oldTvdbId = item.getTvdb();
                item.setTvdb(theMovieDbItem.getTvdb());
                updates.add(String.format("TVDB ID: [%s->%s]", oldTvdbId, theMovieDbItem.getTvdb()));
            }

            if (updates.length() > 0) {
                LOGGER.info("Updating item with TMDB ID [{}] from TMDB API [old->new]: {}", item.getTheMovieDb(), updates);
            } else {
                LOGGER.info("No updates needed/possible for item with TMDB ID [{}]", item.getTheMovieDb());
            }
        }
    }
}