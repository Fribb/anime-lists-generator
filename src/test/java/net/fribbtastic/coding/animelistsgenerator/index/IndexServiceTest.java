package net.fribbtastic.coding.animelistsgenerator.index;

import net.fribbtastic.coding.animelistsgenerator.Generator;
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
import java.util.List;
import java.util.Map;

/**
 * @author Frederic Eßer
 */
@SpringBootTest
@ActiveProfiles("test-short")
class IndexServiceTest {

    @Autowired
    private AnimeOfflineDatabaseService animeOfflineDatabaseService;
    @Autowired
    private AnimeListsService animeListsService;
    @Autowired
    private TheMovieDBService theMovieDBService;
    @Autowired
    private Generator generator;
    @Autowired
    private IndexService indexService;

    @Test
    @DisplayName("Test: Creating the Index List")
    public void testCreateIndexList() {

        ArrayList<AnimeItem> aodbList = this.animeOfflineDatabaseService.generateList();
        ArrayList<AnimeItem> animeListsList = this.animeListsService.generateList();
        ArrayList<AnimeItem> mergedList = this.generator.mergeLists(animeListsList, aodbList);
        this.theMovieDBService.appendMissingIds(mergedList);

        Map<String, List<Integer>> indexMap = this.indexService.generateIndex(mergedList);

        Assertions.assertThat(indexMap).isNotNull();
        Assertions.assertThat(indexMap.size()).isEqualTo(37);
        Assertions.assertThat(indexMap.get("animecountdown:36462")).isNotNull();
        Assertions.assertThat(indexMap.get("animecountdown:36462")).hasSize(1);
        Assertions.assertThat(indexMap.get("animecountdown:36462").getFirst()).isEqualTo(0);
    }

}