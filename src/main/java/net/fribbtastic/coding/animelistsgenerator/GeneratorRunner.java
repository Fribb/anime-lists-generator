package net.fribbtastic.coding.animelistsgenerator;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author Frederic Eßer
 */
@Component
@Profile("prod")
public class GeneratorRunner implements CommandLineRunner {

    private final Generator generator;

    public GeneratorRunner(Generator generator) {
        this.generator = generator;
    }

    @Override
    public void run(String @NonNull ... args) throws Exception {
        this.generator.generateLists();
    }
}
