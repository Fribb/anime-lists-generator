package net.fribbtastic.coding.animelistsgenerator.animeLists.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * @author Frederic Eßer
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnimeListsItem {

    private String name;

    @JacksonXmlProperty(isAttribute = true)
    private Integer anidbid;

    /**
     * The TVDB ID from the Anime-lists can sometimes be a String
     * because it contains "movie" or "ova" instead of the Integer ID
     */
    @JacksonXmlProperty(isAttribute = true)
    private String tvdbid;

    /**
     * The defaulttvdbseason can sometimes be a String
     * because it contains "a" (absolute episode numbering) instead of an Integer Season number
     */
    @JacksonXmlProperty(isAttribute = true)
    private String defaulttvdbseason;

    @JacksonXmlProperty(isAttribute = true)
    private Integer tmdbtv;

    /**
     * The tmdbseason can sometimes be a String
     * because it contains "a" (absolute episode numbering) instead of an Integer Season number
     */
    @JacksonXmlProperty(isAttribute = true)
    private String tmdbseason;

    /**
     * The tmdbid can sometimes be a String
     * because it contains a coma separated list ("145675,210227") instead of a single Integer ID
     */
    @JacksonXmlProperty(isAttribute = true)
    private String tmdbid;

    @JacksonXmlProperty(isAttribute = true)
    private String imdbid;
}
