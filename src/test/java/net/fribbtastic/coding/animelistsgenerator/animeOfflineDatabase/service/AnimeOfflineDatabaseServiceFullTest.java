package net.fribbtastic.coding.animelistsgenerator.animeOfflineDatabase.service;

import net.fribbtastic.coding.animelistsgenerator.models.AnimeItem;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;

/**
 * @author Frederic Eßer
 */
@SpringBootTest
@ActiveProfiles("test-full")
@ExtendWith(MockitoExtension.class)
class AnimeOfflineDatabaseServiceFullTest {

    @Autowired
    private AnimeOfflineDatabaseService animeOfflineDatabaseService;

    @Test
    @DisplayName("Test: Full AnimeOfflineDatabase")
    public void testGetAnimeOfflineDatabase() {

        ArrayList<AnimeItem> list = this.animeOfflineDatabaseService.generateList();

        Assertions.assertThat(list.size()).isEqualTo(40235);

        Assertions.assertThat(list.getFirst()).isNotNull();
        Assertions.assertThat(list.getFirst().getAnilist()).isEqualTo(142051);
        Assertions.assertThat(list.getFirst().getAnimePlanet()).isEqualTo("raise-a-suilen-nvade-show");
        Assertions.assertThat(list.getFirst().getKitsu()).isEqualTo(47450);
        Assertions.assertThat(list.getFirst().getMyanimelist()).isEqualTo(51478);

        Assertions.assertThat(list.get(1)).isNotNull();
        Assertions.assertThat(list.get(1).getAnidb()).isEqualTo(10143);
        Assertions.assertThat(list.get(1).getAnilist()).isEqualTo(102416);
        Assertions.assertThat(list.get(1).getAnimePlanet()).isEqualTo("chiaki-kuriyama-0");
        Assertions.assertThat(list.get(1).getAnimeCountdown()).isEqualTo(48858);
        Assertions.assertThat(list.get(1).getAnisearch()).isEqualTo(9010);
        Assertions.assertThat(list.get(1).getKitsu()).isEqualTo(8925);
        Assertions.assertThat(list.get(1).getMyanimelist()).isEqualTo(20707);
        Assertions.assertThat(list.get(1).getSimkl()).isEqualTo(48858);
    }

}