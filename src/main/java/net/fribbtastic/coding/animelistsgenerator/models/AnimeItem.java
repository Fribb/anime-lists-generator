package net.fribbtastic.coding.animelistsgenerator.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.fribbtastic.coding.animelistsgenerator.animeLists.models.AnimeListsItem;
import net.fribbtastic.coding.animelistsgenerator.animeOfflineDatabase.models.AnimeSource;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Frederic Eßer
 */
@Data
public class AnimeItem {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnimeItem.class);

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("type")
    private String type;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("anidb_id")
    private Integer anidb;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("anilist_id")
    private Integer anilist;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("animecountdown_id")
    private Integer animeCountdown;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("animenewsnetwork_id")
    private Integer animeNewsNetwork;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("anime-planet_id")
    private String animePlanet;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("anisearch_id")
    private Integer anisearch;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("imdb_id")
    private List<String> imdb;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("kitsu_id")
    private Integer kitsu;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("livechart_id")
    private Integer livechart;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("mal_id")
    private Integer myanimelist;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("simkl_id")
    private Integer simkl;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("themoviedb_id")
    private TheMovieDBItem theMovieDb;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("tvdb_id")
    private Integer tvdb;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = SeasonFilter.class)
    @JsonProperty("season")
    private Season season;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = EpisodeOffsetFilter.class)
    @JsonProperty("episode_offset")
    private EpisodeOffset episodeOffset;

    @JsonIgnore
    private transient Map<String, List<String>> relations;

    @JsonIgnore
    private transient String title;

    /** kind of relation per source and related ID, normalised (see RelationParser), only where the provider names it */
    @JsonIgnore
    private transient Map<String, Map<String, String>> relationTypes;

    /**
     * Set the IDs by parsing the individual Source URLs
     *
     * @param sources the {@link ArrayList} containing all the Source URLs
     * @return the {@link AnimeItem} with the Set members
     */
    public static AnimeItem fromAODBSourceUrls(ArrayList<String> sources) {
        AnimeItem animeItem = new AnimeItem();

        for(String sourceUrl : sources) {
            AnimeSource.fromUrl(sourceUrl).ifPresent(source -> {
                String id = source.extractId(sourceUrl);
                if (id != null) {
                    source.setId(animeItem, id);
                }
            });
        }

        return animeItem;
    }

    /**
     * parse the Item from the anime-lists source to the standardized AnimeItem structure
     * @param item the anime-lists source item
     * @return the standardized AnimeItem
     */
    public static AnimeItem fromAnimeListsSource(AnimeListsItem item) {
        AnimeItem animeItem = new AnimeItem();

        // set the AniDB ID
        animeItem.setAnidb(item.getAnidbid());

        // set the IMDB ID
        animeItem.setImdb(parseImdbId(item.getImdbid()));

        // set the TMDB ID
        /*
        The anime-lists element uses two ways to set the TMDB ID
        1. with the tmdbid attribute
        2. with the tmdbtv attribute
         */
        TheMovieDBItem tmdbItem = null;
        if (item.getTmdbid() != null || item.getTmdbtv() != null) {
            tmdbItem = new TheMovieDBItem();

            if (item.getTmdbid() != null) {
                tmdbItem.setMovie(parseTheMovieDb(item.getAnidbid(), item.getTmdbid()));
            }
            if (item.getTmdbtv() != null) {
                tmdbItem.setTv(item.getTmdbtv());
            }
        }

        animeItem.setTheMovieDb(tmdbItem);

        // set TVDB ID
        animeItem.setTvdb(parseStringToInteger(item.getAnidbid(),"tvdb id", item.getTvdbid()));

        // add season information
        Season season = new Season();

        // set TMDB season
        if (item.getTmdbseason() != null                    // the tmdbseason needs to be available
                && !item.getTmdbseason().equals("a")        // and can't be set to "a"
                /*&& !item.getTmdbseason().equals("0")*/) {     // and can't be set to "0"

            season.setTheMovieDb(parseStringToInteger(item.getAnidbid(), "tmdb season", item.getTmdbseason()));
        }

        // set TVDB season
        if (item.getDefaulttvdbseason() != null                     // the defaulttvdbseason needs to be available
                && !item.getTvdbid().equals("movie")                // the tvdb id can't be set to "movie"
                && !item.getDefaulttvdbseason().equals("a")         // and can't be set to "a"
                /*&& !item.getDefaulttvdbseason().equals("0")*/) {      // and can't be set to "0"

            season.setThetvdb(parseStringToInteger(item.getAnidbid(), "tvdb season", item.getDefaulttvdbseason()));
        }

        // add season to anime item
        animeItem.setSeason(season);

        // add the episode Offset
        if (item.getEpisodeoffset() != null || item.getTmdboffset() != null) {
            EpisodeOffset episodeOffset = new EpisodeOffset();

            if (item.getEpisodeoffset() != null) {
                episodeOffset.setThetvdb(parseStringToInteger(item.getAnidbid(), "tvdb episode offset", item.getEpisodeoffset()));
            }

            if (item.getTmdboffset() != null) {
                episodeOffset.setTheMovieDb(parseStringToInteger(item.getAnidbid(), "tmdb episode offset", item.getTmdboffset()));
            }

            animeItem.setEpisodeOffset(episodeOffset);
        }

        return animeItem;
    }

    /**
     * parse the TMDB ID from the anime-lists source to a List of Integers
     *
     * @param itemId        the ID of the item, for logging purposes
     * @param stringToParse the ID as a String that should be parsed
     * @return the parsed TMDB ID as a List of Integers
     */
    private static List<Integer> parseTheMovieDb(Integer itemId, String stringToParse) {
        if (stringToParse == null || stringToParse.isBlank()) {
            return null;
        }

        List<Integer> result = Arrays.stream(stringToParse.split(","))
                .map(String::trim)
                .filter(id -> !id.isBlank())
                .map(id -> parseStringToInteger(itemId, "tmdb id", id))
                .filter(Objects::nonNull)
                .toList();

        return result.isEmpty() ? null : result;
    }

    /**
     * parse the IMDB ID from the anime-lists source to a List of Strings
     *
     * @param imdbid the IMDB ID from the anime-lists source
     * @return the parsed IMDB ID as a List of Strings
     */
    private static List<String> parseImdbId(String imdbid) {
        if (imdbid == null || imdbid.isBlank()) {
            return null;
        }

        return Arrays.stream(imdbid.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    /**
     * parse the ID as a string to an Integer
     *
     * @param itemId the ID of the item, for logging purposes
     * @param stringToParse the ID as a String that should be parsed
     * @return the parsed ID or null if it couldn't be parsed
     */
    public static Integer parseStringToInteger(Integer itemId, String type,String stringToParse) {
        if (NumberUtils.isCreatable(stringToParse)) {
            return NumberUtils.createInteger(stringToParse);
        } else {
            LOGGER.warn("[AniDB ID={}] could not parse {} '{}' because it isn't an Integer", itemId, type, stringToParse);
            return null;
        }
    }

    /**
     * merge the 'other' AnimeItem into this one.
     *
     * @param other the other AnimeItem that will be merged into this one
     */
    public void merge(AnimeItem other) {
        if (this.type == null) this.type = other.getType();
        if (this.anilist == null) this.anilist = other.getAnilist();
        if (this.animeCountdown == null) this.animeCountdown = other.getAnimeCountdown();
        if (this.animeNewsNetwork == null) this.animeNewsNetwork = other.getAnimeNewsNetwork();
        if (this.animePlanet == null) this.animePlanet = other.getAnimePlanet();
        if (this.anisearch == null) this.anisearch = other.getAnisearch();
        if (this.imdb == null) this.imdb = other.getImdb();
        if (this.kitsu == null) this.kitsu = other.getKitsu();
        if (this.livechart == null) this.livechart = other.getLivechart();
        if (this.myanimelist == null) this.myanimelist = other.getMyanimelist();
        if (this.simkl == null) this.simkl = other.getSimkl();
        if (this.theMovieDb == null) this.theMovieDb = other.getTheMovieDb();
        if (this.tvdb == null) this.tvdb = other.getTvdb();
        if (this.season == null) this.season = other.getSeason();
        if (this.episodeOffset == null) this.episodeOffset = other.getEpisodeOffset();
    }

    @JsonIgnore
    public Map<String, List<String>> getIndexIdMap() {
        Map<String, List<String>> result = new HashMap<>();

        this.putIndexIdIfNotNull(result, "anidb", this.anidb);
        this.putIndexIdIfNotNull(result, "anilist", this.anilist);
        this.putIndexIdIfNotNull(result, "animecountdown", this.animeCountdown);
        this.putIndexIdIfNotNull(result, "animenewsnetwork", this.animeNewsNetwork);
        this.putIndexIdIfNotNull(result, "anime-planet", this.animePlanet);
        this.putIndexIdIfNotNull(result, "anisearch", this.anisearch);
        this.putIndexIdIfNotNull(result, "imdb", this.imdb);
        this.putIndexIdIfNotNull(result, "kitsu", this.kitsu);
        this.putIndexIdIfNotNull(result, "livechart", this.livechart);
        this.putIndexIdIfNotNull(result, "mal", this.myanimelist);
        this.putIndexIdIfNotNull(result, "simkl", this.simkl);
        this.putTheMovieDbIndexIdsIfNotNull(result);
        this.putIndexIdIfNotNull(result, "tvdb", this.tvdb);

        return result;
    }

    private void putIndexIdIfNotNull(Map<String, List<String>> map, String key, Object value) {
        if (value != null) {
            map.computeIfAbsent(key, ignored -> new ArrayList<>()).add(String.valueOf(value));
        }
    }

    private void putTheMovieDbIndexIdsIfNotNull(Map<String, List<String>> map) {
        if (this.theMovieDb == null) {
            return;
        }

        if (this.theMovieDb.getMovie() != null) {
            this.theMovieDb.getMovie().forEach(movieId -> map.computeIfAbsent("themoviedb", ignored -> new ArrayList<>())
                    .add("movie:" + movieId));
        }

        if (this.theMovieDb.getTv() != null) {
            map.computeIfAbsent("themoviedb", ignored -> new ArrayList<>())
                    .add("tv:" + this.theMovieDb.getTv());
        }
    }

    public List<String> getIdForSource(String source) {
        return switch (source) {
            case "anidb" -> this.anidb != null ? List.of(this.anidb.toString()) : List.of();
            case "anilist" -> this.anilist != null ? List.of(this.anilist.toString()) : List.of();
            case "animecountdown" -> this.animeCountdown != null ? List.of(this.animeCountdown.toString()) : List.of();
            case "animenewsnetwork" -> this.animeNewsNetwork != null ? List.of(this.animeNewsNetwork.toString()) : List.of();
            case "anime-planet" -> this.animePlanet != null ? List.of(this.animePlanet) : List.of();
            case "anisearch" -> this.anisearch != null ? List.of(this.anisearch.toString()) : List.of();
            case "imdb" -> this.imdb != null ? this.imdb : List.of();
            case "kitsu" -> this.kitsu != null ? List.of(this.kitsu.toString()) : List.of();
            case "livechart" -> this.livechart != null ? List.of(this.livechart.toString()) : List.of();
            case "mal" -> this.myanimelist != null ? List.of(this.myanimelist.toString()) : List.of();
            case "simkl" -> this.simkl != null ? List.of(this.simkl.toString()) : List.of();
            case "themoviedb" -> this.getTheMovieDbIndexId();
            case "tvdb" -> this.tvdb != null ? List.of(this.tvdb.toString()) : List.of();
            default -> throw new IllegalStateException("Unexpected value: " + source);
        };
    }

    @JsonIgnore
    private List<String> getTheMovieDbIndexId() {
        if (this.theMovieDb == null) {
            return List.of();
        }

        List<String> result = new ArrayList<>();

        if (this.theMovieDb.getMovie() != null) {
            this.theMovieDb.getMovie().forEach(movieId -> result.add("movie:" + movieId));
        }

        if (this.theMovieDb.getTv() != null) {
            result.add("tv:" + this.theMovieDb.getTv());
        }

        return result;
    }
}
