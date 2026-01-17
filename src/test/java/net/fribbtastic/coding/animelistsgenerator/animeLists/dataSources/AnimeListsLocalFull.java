package net.fribbtastic.coding.animelistsgenerator.animeLists.dataSources;

import net.fribbtastic.coding.animelistsgenerator.animeLists.models.AnimeLists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tools.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Frederic Eßer
 */
@Service
@Profile("test-full")
public class AnimeListsLocalFull implements  AnimeListsDataSource{

    private static final Logger LOGGER = LoggerFactory.getLogger(AnimeListsLocalFull.class);

    private final XmlMapper mapper;

    public AnimeListsLocalFull(XmlMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public AnimeLists loadData() {
        LOGGER.info("Loading Data from local file");

        try {
            String fileContent = Files.readString(Paths.get("src/test/resources/files/anime-list-full.xml"));

            return mapper.readValue(fileContent, AnimeLists.class);

        } catch (IOException e) {
            throw new RuntimeException("could not load test file");
        }
    }
}
