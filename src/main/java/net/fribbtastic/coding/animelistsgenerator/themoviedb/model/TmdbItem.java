package net.fribbtastic.coding.animelistsgenerator.themoviedb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Frederic Eßer
 * Example:
 * {
 *     "id": 26209,
 *     "imdb_id": "tt0286390",
 *     "freebase_mid": "/m/07cgbwg",
 *     "freebase_id": "/en/banner_of_the_stars_jp",
 *     "tvdb_id": 72025,
 *     "tvrage_id": null,
 *     "wikidata_id": "Q137782309",
 *     "facebook_id": null,
 *     "instagram_id": null,
 *     "twitter_id": null
 * }
 *
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbItem {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("imdb_id")
    private String imdb;

    @JsonProperty("tvdb_id")
    private Integer tvdb;
}
