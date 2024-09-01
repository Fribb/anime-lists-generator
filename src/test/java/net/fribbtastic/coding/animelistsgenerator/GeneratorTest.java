package net.fribbtastic.coding.animelistsgenerator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Frederic Eßer
 */
@SpringBootTest
class GeneratorTest {

    @Test
    public void testGenerator() {
        new Generator();
    }

}