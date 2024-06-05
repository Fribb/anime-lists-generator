package net.fribbtastic.coding.animelistsgenerator.utils;


import org.assertj.core.api.Assertions;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

/**
 * @author Frederic Eßer
 */
@SpringBootTest
class TheMovieDBUtilsTest {

    private final TheMovieDBUtils utils = new TheMovieDBUtils();

    @Test
    public void testFindTheMovieDbIdByIMDBId() {

        JSONObject tmdbEntry = this.utils.findTheMovieDbIdByIMDB("tt1164545");

        Assertions.assertThat(tmdbEntry.getInt("id")).isEqualTo(26595);
    }

    @Test
    public void getTheMovieDbExternalIdsForTV() {
        Integer id = 46414;
        String mediaType = "tv";

        Map<String, Object> result = this.utils.getTheMovieDbExternalIds(id, mediaType);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isNotEmpty();
        Assertions.assertThat(result.size()).isEqualTo(2);
        Assertions.assertThat(result.get("imdb_id")).isEqualTo("tt1216124");
        Assertions.assertThat(result.get("thetvdb_id")).isEqualTo(81830);
    }

    @Test
    public void getTheMovieDbExternalIdsForMovie() {
        Integer id = 26595;
        String mediaType = "movie";

        Map<String, Object> result = this.utils.getTheMovieDbExternalIds(id, mediaType);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isNotEmpty();
        Assertions.assertThat(result.size()).isEqualTo(1);
        Assertions.assertThat(result.get("imdb_id")).isEqualTo("tt1164545");
    }
}