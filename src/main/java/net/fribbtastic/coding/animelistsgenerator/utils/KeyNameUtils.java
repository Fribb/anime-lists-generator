package net.fribbtastic.coding.animelistsgenerator.utils;

import java.util.Map;

/**
 * @author Frederic Eßer
 */
public class KeyNameUtils {

    /**
     * the Map containing the individual Keys or hostnames that need to be mapped to a specific value
     */
    private static final Map<String, String> sourcesNames = Map.ofEntries(
            // anime-offline-database Key mapping
            Map.entry("anidb.net", "anidb"),
            Map.entry("anilist.co", "anilist"),
            Map.entry("anisearch.com", "anisearch"),
            Map.entry("kitsu.io", "kitsu"),
            Map.entry("livechart.me", "livechart"),
            Map.entry("myanimelist.net", "mal"),

            // anime-planet does not have an ID as integer value but rather uses the title
            Map.entry("anime-planet.com", "anime-planet"),

            // notify.moe does not use an ID as integer value but rather uses a hash
            Map.entry("notify.moe", "notify.moe"),

            // anime-lists Key mapping
            Map.entry("anidbid", "anidb"),
            Map.entry("tvdbid", "thetvdb"),
            Map.entry("tmdbid", "themoviedb"),
            Map.entry("imdbid", "imdb"),

            // TheMovieDB Key Mapping
            Map.entry("imdb_id", "imdb"),
            Map.entry("tvdb_id", "thetvdb")
    );

    /**
     * get the value for the key
     *
     * @param key the key in the map
     * @return just the value for the key in the map
     */
    public static String getValue(String key) {
        return sourcesNames.get(key);
    }

    /**
     * get the value with the '_id' suffix to map the keys of the anime-lists
     * and anime-offline-database to the resulting list
     *
     * @param key the key that should be looked up
     * @return the value of the key with '_id' as suffix
     */
    public static String getValueWithId(String key) {
        return sourcesNames.get(key) + "_id";
    }
}
