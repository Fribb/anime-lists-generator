package net.fribbtastic.coding.animelistsgenerator.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Frederic Eßer
 */
@Component
public class FileUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    private final ObjectMapper mapper;

    public FileUtils(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Write an object as JSON to the specified Path, create the parent directories if they don't exist already
     * This will always pretty print the JSON
     *
     * @param object the Object to serialize
     * @param filePath the target path
     */
    public void writeToFile(Object object, Path filePath) {
        this.writeToFile(object, filePath, true);
    }

    /**
     * Write an object as JSON to the specified Path, create the parent directories if they don't exist already
     *
     * @param object the Object to serialize
     * @param filePath the target path
     */
    public void writeToFile(Object object, Path filePath, boolean prettyPrint) {
        try {
            // check if the path has a parent and create the directories if not
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }

            LOGGER.info("writing file to {}", filePath);

            // convert the Object to JSON and save it
            if (prettyPrint) {
                this.mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), object);
            } else {
                this.mapper.writeValue(filePath.toFile(), object);
            }

        } catch (IOException e) {
            LOGGER.error("Could not save the file", e);
            throw new RuntimeException(e);
        }

    }
}
