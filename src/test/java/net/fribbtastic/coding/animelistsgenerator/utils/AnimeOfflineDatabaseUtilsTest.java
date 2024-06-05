package net.fribbtastic.coding.animelistsgenerator.utils;

import org.assertj.core.api.Assertions;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Frederic Eßer
 */
@SpringBootTest
class AnimeOfflineDatabaseUtilsTest {

    @SuppressWarnings("FieldCanBeLocal")
    private final String dataJSONString = "[{\"sources\":[\"https://anilist.co/anime/142051\",\"https://anime-planet.com/anime/raise-a-suilen-nvade-show\",\"https://kitsu.io/anime/47450\",\"https://myanimelist.net/anime/51478\"],\"title\":\"!NVADE SHOW!\",\"type\":\"SPECIAL\",\"episodes\":1,\"status\":\"FINISHED\",\"animeSeason\":{\"season\":\"FALL\",\"year\":2020},\"picture\":\"https://cdn.myanimelist.net/images/anime/1930/122178.jpg\",\"thumbnail\":\"https://cdn.myanimelist.net/images/anime/1930/122178t.jpg\",\"synonyms\":[\"!nvade Show!\",\"Invade Show!\",\"RAISE A SUILEN\",\"RAISE A SUILEN: !NVADE SHOW!\"],\"relatedAnime\":[\"https://anilist.co/anime/101633\",\"https://kitsu.io/anime/12330\",\"https://myanimelist.net/anime/37869\"],\"tags\":[\"band\",\"full cgi\",\"music\",\"primarily female cast\",\"primarily teen cast\"]},{\"sources\":[\"https://anidb.net/anime/10143\",\"https://anilist.co/anime/102416\",\"https://anime-planet.com/anime/chiaki-kuriyama-0\",\"https://anisearch.com/anime/9010\",\"https://kitsu.io/anime/8925\",\"https://myanimelist.net/anime/20707\",\"https://notify.moe/anime/Ff1bpKmmR\"],\"title\":\"\\\"0\\\"\",\"type\":\"SPECIAL\",\"episodes\":1,\"status\":\"FINISHED\",\"animeSeason\":{\"season\":\"SUMMER\",\"year\":2013},\"picture\":\"https://cdn.myanimelist.net/images/anime/12/81160.jpg\",\"thumbnail\":\"https://cdn.myanimelist.net/images/anime/12/81160t.jpg\",\"synonyms\":[\"\\\"Zero\\\"\",\"0 (Zero)\",\"Chiaki Kuriyama - 0\",\"Chiaki Kuriyama - Zero\",\"Chiaki Kuriyama: \\\"0\\\"\",\"Chiaki Kuriyama: 「0」\",\"Kuriyama Chiaki - 0\",\"「0」\",\"栗山 千明「0」\",\"栗山千明 - 0\"],\"relatedAnime\":[],\"tags\":[\"drama\",\"female protagonist\",\"indefinite\",\"music\",\"present\"]}]";

    @Test
    void parseAnimeOfflineDatabase() {
        AnimeOfflineDatabaseUtils utils = new AnimeOfflineDatabaseUtils();

        JSONArray array = new JSONArray(dataJSONString);

        Assertions.assertThat(array.length()).isEqualTo(2);

        JSONArray parsedArray = utils.parseAnimeOfflineDatabase(array);

        Assertions.assertThat(parsedArray).isNotNull();
        Assertions.assertThat(parsedArray.length()).isEqualTo(2);
    }
}