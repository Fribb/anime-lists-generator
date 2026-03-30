package net.fribbtastic.coding.animelistsgenerator.themoviedb.service;

import net.fribbtastic.coding.animelistsgenerator.models.AnimeItem;
import net.fribbtastic.coding.animelistsgenerator.themoviedb.dataSources.TheMovieDBDataSource;
import net.fribbtastic.coding.animelistsgenerator.themoviedb.model.TmdbFindResult;
import net.fribbtastic.coding.animelistsgenerator.themoviedb.model.TmdbItem;
import net.fribbtastic.coding.animelistsgenerator.themoviedb.model.TmdbMovieResult;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class TheMovieDBServiceTest {

    @Test
    void ignoresTmdbTvDerivedIdsForMovieRowsAndReResolvesThem() {
        FakeTheMovieDBDataSource dataSource = new FakeTheMovieDBDataSource();
        dataSource.findResult = buildMovieFindResult(609197);
        dataSource.loadDataResult = buildTmdbItem("tt0251174", null);

        AnimeItem item = new AnimeItem();
        item.setAnidb(8387);
        item.setType("MOVIE");
        item.setImdb("tt0251174");
        item.setTheMovieDb(31910);
        item.setTmdbIdOrigin(AnimeItem.TmdbIdOrigin.TMDB_TV);

        TheMovieDBService service = new TheMovieDBService(dataSource);
        service.appendMissingIds(new ArrayList<>(List.of(item)));

        Assertions.assertThat(item.getTheMovieDb()).isEqualTo(609197);
        Assertions.assertThat(item.getTmdbIdOrigin()).isNull();
        Assertions.assertThat(dataSource.findLookupId).isEqualTo("tt0251174");
        Assertions.assertThat(dataSource.findSource).isEqualTo("imdb_id");
        Assertions.assertThat(dataSource.loadedIds).containsExactly(609197);
        Assertions.assertThat(dataSource.loadedMediaTypes).containsExactly("movie");
    }

    private static TmdbFindResult buildMovieFindResult(int tmdbId) {
        TmdbMovieResult movieResult = new TmdbMovieResult();
        movieResult.setId(tmdbId);
        movieResult.setMediaType("movie");

        TmdbFindResult findResult = new TmdbFindResult();
        findResult.setMovieResults(new ArrayList<>(List.of(movieResult)));
        return findResult;
    }

    private static TmdbItem buildTmdbItem(String imdbId, Integer tvdbId) {
        TmdbItem item = new TmdbItem();
        item.setImdb(imdbId);
        item.setTvdb(tvdbId);
        return item;
    }

    private static class FakeTheMovieDBDataSource implements TheMovieDBDataSource {
        private TmdbFindResult findResult;
        private TmdbItem loadDataResult;
        private String findLookupId;
        private String findSource;
        private final ArrayList<Integer> loadedIds = new ArrayList<>();
        private final ArrayList<String> loadedMediaTypes = new ArrayList<>();

        @Override
        public TmdbItem loadData(String mediaType, Integer id) {
            loadedMediaTypes.add(mediaType);
            loadedIds.add(id);
            return loadDataResult;
        }

        @Override
        public TmdbFindResult findItem(String lookupId, String source) {
            findLookupId = lookupId;
            findSource = source;
            return findResult;
        }
    }
}
