package net.fribbtastic.coding.animelistsgenerator.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Downloaded source files: saved after every download, read instead of downloading when data.download=false
 */
public final class SourceFiles {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceFiles.class);

    private SourceFiles() {
    }

    /**
     * Saves a downloaded file to project.path/source/. A failure only logs a warning, the run goes on.
     *
     * @param fileName the file name
     * @param content the downloaded content
     */
    public static void save(String fileName, String content) {
        Path file = Properties.sourceFile(fileName);
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            Files.writeString(file, content);
            LOGGER.info("Saved the download to {}", file);
        } catch (IOException e) {
            LOGGER.warn("Could not save the download to {}: {}", file, e.getMessage());
        }
    }

    /**
     * Reads a local source file.
     *
     * @param file the file
     * @param setting the setting that points to it, for the error message
     * @return the content
     */
    public static String read(String file, String setting) {
        Path path = Path.of(file);
        if (!Files.isRegularFile(path)) {
            throw new IllegalStateException("data.download=false, but " + path + " doesn't exist. Set " + setting
                    + " or run once with data.download=true to save the download there.");
        }
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new UncheckedIOException("could not read " + path, e);
        }
    }
}
