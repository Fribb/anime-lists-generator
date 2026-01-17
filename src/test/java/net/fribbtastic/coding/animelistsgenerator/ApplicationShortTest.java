package net.fribbtastic.coding.animelistsgenerator;

import net.fribbtastic.coding.animelistsgenerator.animeLists.service.AnimeListsService;
import net.fribbtastic.coding.animelistsgenerator.animeOfflineDatabase.service.AnimeOfflineDatabaseService;
import net.fribbtastic.coding.animelistsgenerator.models.AnimeItem;
import net.fribbtastic.coding.animelistsgenerator.themoviedb.service.TheMovieDBService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;

/**
 * @author Frederic Eßer
 */
@SpringBootTest
@ActiveProfiles("test-short")
public class ApplicationShortTest {

    @Autowired
    private AnimeOfflineDatabaseService animeOfflineDatabaseService;
    @Autowired
    private AnimeListsService animeListsService;
    @Autowired
    private TheMovieDBService theMovieDBService;
    @Autowired
    private Generator generator;

    /**
     * Test the Generator to run against a smaller set of Anime.
     * The First anime is a TV Show
     * The Second anime is a Movie
     *
     */
    @Test
    @DisplayName("Test: Test a smaller set of Anime")
    void testShortAnime() {

        ArrayList<AnimeItem> aodbList = this.animeOfflineDatabaseService.generateList();

        Assertions.assertThat(aodbList).isNotNull();
        Assertions.assertThat(aodbList.size()).isEqualTo(2);
        Assertions.assertThat(aodbList.getFirst()).isNotNull();
        Assertions.assertThat(aodbList.getFirst().getAnidb()).isEqualTo(1);
        Assertions.assertThat(aodbList.getFirst().getAnilist()).isEqualTo(290);
        Assertions.assertThat(aodbList.getFirst().getAnimePlanet()).isEqualTo("crest-of-the-stars");
        Assertions.assertThat(aodbList.getFirst().getAnimeCountdown()).isEqualTo(36462);
        Assertions.assertThat(aodbList.getFirst().getAnimeNewsNetwork()).isEqualTo(14);
        Assertions.assertThat(aodbList.getFirst().getAnisearch()).isEqualTo(3039);
        Assertions.assertThat(aodbList.getFirst().getKitsu()).isEqualTo(265);
        Assertions.assertThat(aodbList.getFirst().getLivechart()).isEqualTo(4157);
        Assertions.assertThat(aodbList.getFirst().getMyanimelist()).isEqualTo(290);
        Assertions.assertThat(aodbList.getFirst().getSimkl()).isEqualTo(36462);

        ArrayList<AnimeItem> animeListsList = this.animeListsService.generateList();

        Assertions.assertThat(animeListsList).isNotNull();
        Assertions.assertThat(animeListsList.size()).isEqualTo(2);
        Assertions.assertThat(animeListsList.getFirst()).isNotNull();
        Assertions.assertThat(animeListsList.getFirst().getAnidb()).isEqualTo(1);
        Assertions.assertThat(animeListsList.getFirst().getTvdb()).isEqualTo(72025);
        Assertions.assertThat(animeListsList.getFirst().getTheMovieDb()).isEqualTo(26209);
        Assertions.assertThat(animeListsList.getFirst().getSeason()).isNotNull();
        Assertions.assertThat(animeListsList.getFirst().getSeason().getThetvdb()).isEqualTo(1);
        Assertions.assertThat(animeListsList.getFirst().getSeason().getTheMovieDb()).isEqualTo(1);

        ArrayList<AnimeItem> mergedList = this.generator.mergeLists(animeListsList, aodbList);

        Assertions.assertThat(mergedList).isNotNull();
        Assertions.assertThat(mergedList.getFirst()).isNotNull();
        Assertions.assertThat(mergedList.getFirst().getAnidb()).isEqualTo(1);
        Assertions.assertThat(mergedList.getFirst().getAnilist()).isEqualTo(290);
        Assertions.assertThat(mergedList.getFirst().getAnimePlanet()).isEqualTo("crest-of-the-stars");
        Assertions.assertThat(mergedList.getFirst().getAnimeCountdown()).isEqualTo(36462);
        Assertions.assertThat(mergedList.getFirst().getAnimeNewsNetwork()).isEqualTo(14);
        Assertions.assertThat(mergedList.getFirst().getAnisearch()).isEqualTo(3039);
        Assertions.assertThat(mergedList.getFirst().getKitsu()).isEqualTo(265);
        Assertions.assertThat(mergedList.getFirst().getLivechart()).isEqualTo(4157);
        Assertions.assertThat(mergedList.getFirst().getMyanimelist()).isEqualTo(290);
        Assertions.assertThat(mergedList.getFirst().getSimkl()).isEqualTo(36462);
        Assertions.assertThat(mergedList.getFirst().getAnidb()).isEqualTo(1);
        Assertions.assertThat(mergedList.getFirst().getTvdb()).isEqualTo(72025);
        Assertions.assertThat(mergedList.getFirst().getTheMovieDb()).isEqualTo(26209);
        Assertions.assertThat(mergedList.getFirst().getSeason()).isNotNull();
        Assertions.assertThat(mergedList.getFirst().getSeason().getThetvdb()).isEqualTo(1);
        Assertions.assertThat(mergedList.getFirst().getSeason().getTheMovieDb()).isEqualTo(1);

        theMovieDBService.appendMissingIds(mergedList);

        Assertions.assertThat(mergedList.getFirst().getImdb()).isEqualTo("tt0286390");
        Assertions.assertThat(mergedList.getFirst().getTvdb()).isEqualTo(72025);

        Assertions.assertThat(mergedList.get(1).getImdb()).isEqualTo("tt0119698");
        Assertions.assertThat(mergedList.get(1).getTheMovieDb()).isEqualTo(128);
        /*
         TMDB does not allow a TVDB ID to be set for Movies even though TVDB also supports Movies now
         */
        // Assertions.assertThat(mergedList.get(1).getTvdb()).isEqualTo(791);
    }
}
