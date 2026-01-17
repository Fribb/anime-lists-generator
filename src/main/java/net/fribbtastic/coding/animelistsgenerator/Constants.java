package net.fribbtastic.coding.animelistsgenerator;

/**
 * @author Frederic Eßer
 */
public class Constants {

    public static final String ANIMEOFFLINEDB_URL = "https://github.com/manami-project/anime-offline-database/releases/download/latest/anime-offline-database-minified.json";
    public static final String ANIMEOFFLINEDB_REDUCED = "anime-offline-database-reduced.json";

    public static final String ANIMELISTS_URL = "https://raw.githubusercontent.com/Anime-Lists/anime-lists/refs/heads/master/anime-list-full.xml";
    public static final String ANIMELISTS_REDUCED = "anime-lists-reduced.json";

    public static final String ANIME_LISTS_FULL = "anime-list-full.json";
    public static final String ANIME_LISTS_FULL_MINIFIED = "anime-list-mini.json";

    public static final String TMDB_URL_EXTERNAL_IDS = "https://api.themoviedb.org/3/${mediaType}/${id}/external_ids?api_key=${apiKey}";
    public static final String TMDB_URL_FIND_BY_EXTERNAL_ID = "https://api.tmdb.org/3/find/${id}?external_source=${source}&api_key=${apiKey}";
}
