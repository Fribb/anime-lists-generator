package net.fribbtastic.coding.animelistsgenerator.themoviedb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Frederic Eßer
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbResultBase {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("media_type")
    private String mediaType;
}
