package net.fribbtastic.coding.animelistsgenerator.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Frederic Eßer
 */
@Data
public class Season {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("tvdb")
    private Integer thetvdb;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("tmdb")
    private Integer theMovieDb;
}
