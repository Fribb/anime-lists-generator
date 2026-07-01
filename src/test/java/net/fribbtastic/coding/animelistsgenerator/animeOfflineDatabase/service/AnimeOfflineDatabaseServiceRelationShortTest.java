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
public class AnimeOfflineDatabaseServiceRelationShortTest {

    @Autowired
    private AnimeOfflineDatabaseService animeOfflineDatabaseService;

    @Test
    @DisplayName("Test: AODB Relations")
    public void testAnimeRelation() {


        ArrayList<AnimeItem> aodbList = this.animeOfflineDatabaseService.generateList();

        Assertions.assertThat(aodbList).isNotNull();
        Assertions.assertThat(aodbList.size()).isEqualTo(6);
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
        // Assert relations
        Assertions.assertThat(aodbList.getFirst().getRelations()).isNotNull();
        Assertions.assertThat(aodbList.getFirst().getRelations().get("anidb").size()).isEqualTo(6);
        Assertions.assertThat(aodbList.getFirst().getRelations().get("anidb").getFirst()).isEqualTo("1623");
        Assertions.assertThat(aodbList.getFirst().getRelations().get("anidb").get(1)).isEqualTo("2673");
        Assertions.assertThat(aodbList.getFirst().getRelations().get("anidb").get(2)).isEqualTo("4");
        Assertions.assertThat(aodbList.getFirst().getRelations().get("anidb").get(3)).isEqualTo("5");
        Assertions.assertThat(aodbList.getFirst().getRelations().get("anidb").get(4)).isEqualTo("6");
        Assertions.assertThat(aodbList.getFirst().getRelations().get("anidb").get(5)).isEqualTo("884");

        Assertions.assertThat(aodbList.getFirst().getRelations().get("anilist").size()).isEqualTo(3);
        Assertions.assertThat(aodbList.getFirst().getRelations().get("anime-planet").size()).isEqualTo(6);
        Assertions.assertThat(aodbList.getFirst().getRelations().get("animecountdown").size()).isEqualTo(6);
        Assertions.assertThat(aodbList.getFirst().getRelations().get("animenewsnetwork").size()).isEqualTo(6);
        Assertions.assertThat(aodbList.getFirst().getRelations().get("anisearch").size()).isEqualTo(3);
        Assertions.assertThat(aodbList.getFirst().getRelations().get("kitsu").size()).isEqualTo(3);
        Assertions.assertThat(aodbList.getFirst().getRelations().get("livechart").size()).isEqualTo(4);
        Assertions.assertThat(aodbList.getFirst().getRelations().get("mal").size()).isEqualTo(3);
        Assertions.assertThat(aodbList.getFirst().getRelations().get("simkl").size()).isEqualTo(6);
    }
}
