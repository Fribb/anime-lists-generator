package net.fribbtastic.coding.animelistsgenerator;

import net.fribbtastic.coding.animelistsgenerator.animeLists.service.AnimeListsService;
import net.fribbtastic.coding.animelistsgenerator.animeOfflineDatabase.service.AnimeOfflineDatabaseService;
import net.fribbtastic.coding.animelistsgenerator.index.IndexService;
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

@SpringBootTest
@ActiveProfiles("test-full")
class ApplicationFullTest {

	@Autowired
	private AnimeOfflineDatabaseService animeOfflineDatabaseService;
	@Autowired
	private AnimeListsService animeListsService;
	@Autowired
	private TheMovieDBService theMovieDBService;
	@Autowired
	private IndexService indexService;
	@Autowired
	private Generator generator;

	@Test
	@DisplayName("Test: get first element of Merged list")
	void testLocalAndMerge() {

		ArrayList<AnimeItem> aodbList = this.animeOfflineDatabaseService.generateList();

		Assertions.assertThat(aodbList).isNotNull();
		Assertions.assertThat(aodbList.size()).isEqualTo(40235);

		ArrayList<AnimeItem> animeListsList = this.animeListsService.generateList();

		Assertions.assertThat(animeListsList).isNotNull();
		Assertions.assertThat(animeListsList.size()).isEqualTo(10403);

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
		Assertions.assertThat(mergedList.getFirst().getTvdb()).isEqualTo(72025);
		Assertions.assertThat(mergedList.getFirst().getTheMovieDb()).isEqualTo(26209);

		theMovieDBService.appendMissingIds(mergedList);

		//Assertions.assertThat(mergedList.getFirst().getImdb()).isEqualTo("tt0286390");

		Map<String, List<Integer>> indexMap = this.indexService.generateIndex(mergedList);

		Assertions.assertThat(indexMap).isNotNull();
	}

}
