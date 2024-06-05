package net.fribbtastic.coding.animelistsgenerator.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Frederic Eßer
 */
public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    /**
     * write the content to the file
     *
     * @param content - the content that should be written to the file
     * @param path - the path to the file
     */
    public static void writeFile(String content, String path) {

        File file = new File(path);

        // check if the file structure already exists, if not then create it
        if (!file.getParentFile().exists()) {
            boolean directoryCreated = file.getParentFile().mkdirs();
            if (directoryCreated) {
                logger.error("directory path was either created partially or not at all");
            }

        }

        try {
            FileWriter writer = new FileWriter(file);

            writer.write(content);
            logger.info("writing file to {}", path);

            writer.close();
        } catch (IOException e) {
            logger.error("An error occurred while writing the file", e);
        }
    }
}
