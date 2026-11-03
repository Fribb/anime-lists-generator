package net.fribbtastic.coding.animelistsgenerator.collections;

import net.fribbtastic.coding.animelistsgenerator.models.AnimeCollection;
import net.fribbtastic.coding.animelistsgenerator.models.AnimeItem;
import net.fribbtastic.coding.animelistsgenerator.utils.Properties;
import net.fribbtastic.coding.animelistsgenerator.utils.RelationParser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Collections and the excluded relations, on hand-made MAL entries
 */
class CollectionServiceTest {

    private String excludedRelations;
    private boolean rejoin;

    @BeforeEach
    void saveProperties() {
        this.excludedRelations = Properties.collectionExcludedRelations;
        this.rejoin = Properties.collectionRejoinSingleNeighbour;
        Properties.collectionRejoinSingleNeighbour = true;
        Properties.collectionExcludedRelations = "crossover,other,shared universe,character";
    }

    @AfterEach
    void restoreProperties() {
        Properties.collectionExcludedRelations = this.excludedRelations;
        Properties.collectionRejoinSingleNeighbour = this.rejoin;
    }

    @Test
    @DisplayName("Test: sequels form one collection")
    void testSequel() {
        List<AnimeItem> items = List.of(
                mal(1, "Series", related(2, "Sequel")),
                mal(2, "Series 2", related(1, "Prequel")));

        Assertions.assertThat(ids(new CollectionService().generateCollections(items)))
                .containsExactly(List.of(1, 2));
    }

    @Test
    @DisplayName("Test: excluded relations don't join two collections")
    void testExcludedRelations() {
        // 1+2 and 3+4 are two franchises, 2 and 3 are linked with an excluded relation in the spelling of
        // different providers
        for (String relation : List.of("Other", "CHARACTER", "shared_universe", "Shared Universe", "Crossover:")) {
            List<AnimeItem> items = List.of(
                    mal(1, "A", related(2, "Sequel")),
                    mal(2, "A 2", related(1, "Prequel", 3, relation)),
                    mal(3, "B", related(4, "Sequel", 2, relation)),
                    mal(4, "B 2", related(3, "Prequel")));

            Assertions.assertThat(ids(new CollectionService().generateCollections(items)))
                    .as(relation)
                    .containsExactlyInAnyOrder(List.of(1, 2), List.of(3, 4));
        }
    }

    @Test
    @DisplayName("Test: one side labelling the relation as excluded is enough")
    void testExcludedRelationOnOneSide() {
        List<AnimeItem> items = List.of(
                mal(1, "A", related(2, "Side Story")),
                mal(2, "B", related(1, "Character")));

        Assertions.assertThat(ids(new CollectionService().generateCollections(items)))
                .containsExactlyInAnyOrder(List.of(1), List.of(2));
    }

    @Test
    @DisplayName("Test: relations without a type still join, as before")
    void testWithoutRelationTypes() {
        List<AnimeItem> items = List.of(
                mal(1, "A", related(2, null)),
                mal(2, "B", related(1, null)));

        Assertions.assertThat(ids(new CollectionService().generateCollections(items)))
                .containsExactly(List.of(1, 2));
    }

    @Test
    @DisplayName("Test: without excluded relations every relation joins")
    void testNothingExcluded() {
        Properties.collectionExcludedRelations = "";
        List<AnimeItem> items = List.of(
                mal(1, "A", related(2, "Other")),
                mal(2, "B", related(1, "Other")));

        Assertions.assertThat(ids(new CollectionService().generateCollections(items)))
                .containsExactly(List.of(1, 2));
    }

    @Test
    @DisplayName("Test: a group cut off by an excluded relation rejoins its only neighbouring collection")
    void testRejoinSingleNeighbour() {
        // 3+4 (a short and its sequel) are only linked to the franchise 1+2 as "Other"
        List<AnimeItem> items = List.of(
                mal(1, "A", related(2, "Sequel", 3, "Other")),
                mal(2, "A 2", related(1, "Prequel", 5, "Sequel")),
                mal(3, "A Short", related(1, "Other", 4, "Sequel")),
                mal(4, "A Short 2", related(3, "Prequel")),
                mal(5, "A 3", related(2, "Prequel")));

        Assertions.assertThat(ids(new CollectionService().generateCollections(items)))
                .containsExactly(List.of(1, 2, 3, 4, 5));
    }

    @Test
    @DisplayName("Test: a group touching several collections through excluded relations stays on its own")
    void testNoRejoinWithSeveralNeighbours() {
        // 5 is a crossover of the franchises 1+2 and 3+4, labelled "Other" on one side and "Crossover" on the other
        List<AnimeItem> items = List.of(
                mal(1, "A", related(2, "Sequel")),
                mal(2, "A 2", related(1, "Prequel", 5, "Other")),
                mal(3, "B", related(4, "Sequel", 5, "Crossover")),
                mal(4, "B 2", related(3, "Prequel")),
                mal(5, "A x B", related(2, "Other", 3, "Other")));

        Assertions.assertThat(ids(new CollectionService().generateCollections(items)))
                .containsExactlyInAnyOrder(List.of(1, 2), List.of(3, 4), List.of(5));
    }

    @Test
    @DisplayName("Test: single entries don't rejoin each other")
    void testNoRejoinIntoSingleEntry() {
        List<AnimeItem> items = List.of(
                mal(1, "A", related(2, "Other")),
                mal(2, "B", related(1, "Other")));

        Assertions.assertThat(ids(new CollectionService().generateCollections(items)))
                .containsExactlyInAnyOrder(List.of(1), List.of(2));
    }

    @Test
    @DisplayName("Test: two collections of the same size linked by an excluded relation stay apart")
    void testNoRejoinOfEqualSize() {
        List<AnimeItem> items = List.of(
                mal(1, "A", related(2, "Sequel")),
                mal(2, "A 2", related(1, "Prequel", 3, "Other")),
                mal(3, "B", related(4, "Sequel", 2, "Other")),
                mal(4, "B 2", related(3, "Prequel")));

        Assertions.assertThat(ids(new CollectionService().generateCollections(items)))
                .containsExactlyInAnyOrder(List.of(1, 2), List.of(3, 4));
    }

    @Test
    @DisplayName("Test: without rejoining, a group cut off by an excluded relation stays on its own")
    void testRejoinDisabled() {
        Properties.collectionRejoinSingleNeighbour = false;
        List<AnimeItem> items = List.of(
                mal(1, "A", related(2, "Sequel", 3, "Other")),
                mal(2, "A 2", related(1, "Prequel")),
                mal(3, "A Short", related(1, "Other")));

        Assertions.assertThat(ids(new CollectionService().generateCollections(items)))
                .containsExactlyInAnyOrder(List.of(1, 2), List.of(3));
    }

    // --- helpers ---

    private static Map<Integer, String> related(Object... idsAndRelations) {
        Map<Integer, String> result = new LinkedHashMap<>();
        for (int i = 0; i < idsAndRelations.length; i += 2) {
            result.put((Integer) idsAndRelations[i], (String) idsAndRelations[i + 1]);
        }
        return result;
    }

    private static AnimeItem mal(int id, String title, Map<Integer, String> related) {
        AnimeItem item = new AnimeItem();
        item.setMyanimelist(id);
        item.setTitle(title);

        List<String> relatedIds = new ArrayList<>();
        Map<String, String> types = new HashMap<>();
        related.forEach((relatedId, relation) -> {
            relatedIds.add(relatedId.toString());
            if (relation != null) {
                types.put(relatedId.toString(), RelationParser.normalise(relation));
            }
        });

        item.setRelations(Map.of("mal", relatedIds));
        item.setRelationTypes(Map.of("mal", types));
        return item;
    }

    private static List<List<Object>> ids(Map<String, List<AnimeCollection>> collections) {
        return collections.get("mal").stream().map(AnimeCollection::getIdList).toList();
    }
}
