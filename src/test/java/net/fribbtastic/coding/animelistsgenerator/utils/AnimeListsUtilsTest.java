package net.fribbtastic.coding.animelistsgenerator.utils;

import org.assertj.core.api.Assertions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Frederic Eßer
 */
@SpringBootTest
class AnimeListsUtilsTest {

    @SuppressWarnings("FieldCanBeLocal")
    private final String testList = "[{\"thetvdb_id\":72025,\"anidb_id\":1,\"imdb_id\":\"tt0119698\"}]";

    @Test
    void getAnimeListsItem() {
        AnimeListsUtils utils = new AnimeListsUtils();

        JSONObject testObject = utils.getAnimeListsItem(1, new JSONArray(this.testList));

        Assertions.assertThat(testObject).isNotNull();
        Assertions.assertThat(testObject.get("thetvdb_id")).isEqualTo(72025);
        Assertions.assertThat(testObject.get("imdb_id")).isEqualTo("tt0119698");
    }
}