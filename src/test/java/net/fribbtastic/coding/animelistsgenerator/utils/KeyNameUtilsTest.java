package net.fribbtastic.coding.animelistsgenerator.utils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Frederic Eßer
 */
@SpringBootTest
class KeyNameUtilsTest {

    @Test
    void getValue() {
        Assertions.assertThat(KeyNameUtils.getValue("anidb.net")).isEqualTo("anidb");
    }

    @Test
    void getValueWithId() {
        Assertions.assertThat(KeyNameUtils.getValueWithId("anidb.net")).isEqualTo("anidb_id");
    }
}