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

import java.util.ArrayList;

/**
 * @author Frederic Eßer
 */
@Data
public class AnimeItem {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnimeItem.class);

    public enum TmdbIdOrigin {
        TMDB_ID,
        TMDB_TV
    }

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
    private String imdb;

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
    private Integer theMovieDb;

    @JsonIgnore
    private TmdbIdOrigin tmdbIdOrigin;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("tvdb_id")
    private Integer tvdb;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = SeasonFilter.class)
    @JsonProperty("season")
    private Season season;

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
        animeItem.setImdb(item.getImdbid());

        // set the TMDB ID
        /*
        The anime-lists element uses two ways to set the TMDB ID
        1. with the tmdbid attribute
        2. with the tmdbtv attribute
         */
        String tmdbId = item.getTmdbid();
        Integer tmdbTvId = item.getTmdbtv();

        if (tmdbId != null && tmdbTvId != null) {
            // both attributes are set, shouldn't happen but just to be sure
            LOGGER.warn("Both TMDB ID ({}) and TMDB TV ID ({}) were set, don't know what to do", tmdbId, tmdbTvId);
        } else if (tmdbId != null) {
            // this would implicitly mean that tmdbTvId is null
            animeItem.setTheMovieDb(parseStringToInteger(item.getAnidbid(),"tmdb id", tmdbId));
            animeItem.setTmdbIdOrigin(TmdbIdOrigin.TMDB_ID);
        } else if (tmdbTvId != null) {
            // this would implicitly mean that tmdbId is null
            animeItem.setTheMovieDb(tmdbTvId);
            animeItem.setTmdbIdOrigin(TmdbIdOrigin.TMDB_TV);
        }

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

        return animeItem;
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
        if (this.tmdbIdOrigin == null) this.tmdbIdOrigin = other.getTmdbIdOrigin();
        if (this.tvdb == null) this.tvdb = other.getTvdb();

        if (this.season == null) {
            this.season = other.getSeason();
        }
    }
}
