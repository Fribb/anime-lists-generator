package net.fribbtastic.coding.animelistsgenerator.animeOfflineDatabase.dataSources;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.fribbtastic.coding.animelistsgenerator.animeOfflineDatabase.models.AODB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Frederic Eßer
 */
@Service
@Profile("test-short")
public class AODBLocalShort implements AnimeOfflineDbDataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AODBLocalShort.class);

    private final ObjectMapper mapper;

    public AODBLocalShort(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public AODB loadData() {
        LOGGER.info("Loading Data from local file");

        try {
            String fileContent = Files.readString(Paths.get("src/test/resources/files/anime-offline-database-short.json"));

            return this.mapper.readValue(fileContent, AODB.class);

        } catch (IOException e) {
            throw new RuntimeException("could not load test file");
        }
    }
}
