package net.fribbtastic.coding.animelistsgenerator.themoviedb.dataSources;

import net.fribbtastic.coding.animelistsgenerator.themoviedb.model.TmdbFindResult;
import net.fribbtastic.coding.animelistsgenerator.themoviedb.model.TmdbItem;

/**
 * @author Frederic Eßer
 */
public interface TheMovieDBDataSource {

    TmdbItem loadData(String mediaType, Integer id);

    TmdbFindResult findItem(String lookupId, String source);
}
