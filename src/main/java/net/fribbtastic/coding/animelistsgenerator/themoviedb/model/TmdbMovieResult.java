package net.fribbtastic.coding.animelistsgenerator.themoviedb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Frederic Eßer
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbMovieResult extends TmdbResultBase {

    @JsonProperty("title")
    private String title;
}
