package net.fribbtastic.coding.animelistsgenerator.animeOfflineDatabase.service;

import net.fribbtastic.coding.animelistsgenerator.models.AnimeItem;
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
class AnimeOfflineDatabaseServiceShortTest {

    @Autowired
    private AnimeOfflineDatabaseService animeOfflineDatabaseService;

    @Test
    @DisplayName("Test: Single AnimeOfflineDatabase")
    public void testGetAnimeOfflineDatabase() {

        ArrayList<AnimeItem> list = this.animeOfflineDatabaseService.generateList();

        Assertions.assertThat(list.size()).isEqualTo(3);
        Assertions.assertThat(list.getFirst()).isNotNull();
        Assertions.assertThat(list.getFirst().getAnidb()).isEqualTo(1);
        Assertions.assertThat(list.getFirst().getAnilist()).isEqualTo(290);
        Assertions.assertThat(list.getFirst().getAnimePlanet()).isEqualTo("crest-of-the-stars");
        Assertions.assertThat(list.getFirst().getAnimeCountdown()).isEqualTo(36462);
        Assertions.assertThat(list.getFirst().getAnimeNewsNetwork()).isEqualTo(14);
        Assertions.assertThat(list.getFirst().getAnisearch()).isEqualTo(3039);
        Assertions.assertThat(list.getFirst().getKitsu()).isEqualTo(265);
        Assertions.assertThat(list.getFirst().getLivechart()).isEqualTo(4157);
        Assertions.assertThat(list.getFirst().getMyanimelist()).isEqualTo(290);
        Assertions.assertThat(list.getFirst().getSimkl()).isEqualTo(36462);
    }

}