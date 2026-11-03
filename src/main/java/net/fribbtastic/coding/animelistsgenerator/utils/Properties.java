package net.fribbtastic.coding.animelistsgenerator.utils;

import net.fribbtastic.coding.animelistsgenerator.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * @author Frederic Eßer
 */
@Component
public class Properties {

    public static String projectPath;
    public static String theMovieDbApiKey;

    @Value("${project.path}")
    public void setProjectPath(String path) {
        projectPath = path;
    }

    @Value("${project.apikey.themoviedb}")
    public void setTheMovieDbApiKey(String apiKey) {
        theMovieDbApiKey = apiKey;
    }

    /** Relations (comma separated, see RelationParser.normalise) that don't join two anime into one collection */
    public static String collectionExcludedRelations = "crossover,other,shared universe,character";

    @Value("${collection.excluded-relations:crossover,other,shared universe,character}")
    public void setCollectionExcludedRelations(String value) {
        collectionExcludedRelations = value;
    }

    /** Put groups cut off only by excluded relations back into their collection, if it is the only one they touch */
    public static boolean collectionRejoinSingleNeighbour = true;

    @Value("${collection.rejoin-single-neighbour:true}")
    public void setCollectionRejoinSingleNeighbour(boolean value) {
        collectionRejoinSingleNeighbour = value;
    }

    /** Download anime-offline-database and anime-list-full.xml (true) or read the local files below (false) */
    public static boolean download = true;
    /** Local anime-offline-database-minified.json, read when download is false; empty = the saved download */
    public static String animeOfflineDatabaseFile = "";
    /** Local anime-list-full.xml (Anime-Lists), read when download is false; empty = the saved download */
    public static String animeListsFile = "";

    @Value("${data.download:true}")
    public void setDownload(boolean value) {
        download = value;
    }

    @Value("${anime-offline-database.file:}")
    public void setAnimeOfflineDatabaseFile(String value) {
        animeOfflineDatabaseFile = value;
    }

    @Value("${anime-lists.file:}")
    public void setAnimeListsFile(String value) {
        animeListsFile = value;
    }

    /**
     * The local file to read instead of downloading, or null when downloading.
     *
     * @param file the configured file
     * @param fileName name of the downloaded file, used when no file is configured
     * @return the configured file, or the saved download in project.path/source/; null if data.download is true
     */
    public static String localFile(String file, String fileName) {
        if (download) {
            return null;
        }
        if (file == null || file.isBlank()) {
            return sourceFile(fileName).toString();
        }
        return file;
    }

    /**
     * Where a downloaded source file is saved: project.path/source/fileName
     *
     * @param fileName the file name
     * @return the path
     */
    public static Path sourceFile(String fileName) {
        String base = projectPath == null ? "" : projectPath;
        return Path.of(base, Constants.SOURCE_DIRECTORY, fileName);
    }
}
