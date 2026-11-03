package net.fribbtastic.coding.animelistsgenerator.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.fribbtastic.coding.animelistsgenerator.animeOfflineDatabase.models.AODBItem;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * Reading "relatedAnime" in both formats and the relation types
 */
class RelationParserTest {

    private static final String ITEM = """
            {
              "sources": ["https://myanimelist.net/anime/1"],
              "title": "Test",
              "relatedAnime": [
                "https://myanimelist.net/anime/2",
                {"uri": "https://myanimelist.net/anime/3", "relation": "Sequel"},
                {"uri": "https://anilist.co/anime/4", "relation": "SHARED_UNIVERSE"},
                {"uri": "https://livechart.me/anime/5"}
              ]
            }
            """;

    @Test
    @DisplayName("Test: reads plain URLs and objects with a relation")
    void testBothFormats() throws Exception {
        AODBItem item = new ObjectMapper().readValue(ITEM, AODBItem.class);
        RelationParser parser = new RelationParser();

        Map<String, java.util.List<String>> relations = parser.parseRelations(item.getRelatedAnime());
        Assertions.assertThat(relations.get("mal")).containsExactly("2", "3");
        Assertions.assertThat(relations.get("anilist")).containsExactly("4");
        Assertions.assertThat(relations.get("livechart")).containsExactly("5");

        Map<String, Map<String, String>> types = parser.parseRelationTypes(item.getRelatedAnime());
        Assertions.assertThat(types.get("mal")).containsExactly(Map.entry("3", "sequel"));
        Assertions.assertThat(types.get("anilist")).containsExactly(Map.entry("4", "shared universe"));
        Assertions.assertThat(types).doesNotContainKey("livechart");
    }

    @Test
    @DisplayName("Test: normalises the labels of the different providers")
    void testNormalise() {
        Assertions.assertThat(RelationParser.normalise("SHARED_UNIVERSE")).isEqualTo("shared universe");
        Assertions.assertThat(RelationParser.normalise(" Shared  Universe: ")).isEqualTo("shared universe");
        Assertions.assertThat(RelationParser.normalise("spin-off")).isEqualTo("spin off");
        Assertions.assertThat(RelationParser.normalise(null)).isEmpty();
    }
}
