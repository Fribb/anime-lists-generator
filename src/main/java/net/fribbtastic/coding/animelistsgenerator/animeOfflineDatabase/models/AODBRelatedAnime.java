package net.fribbtastic.coding.animelistsgenerator.animeOfflineDatabase.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

/**
 * An entry of "relatedAnime" in the anime-offline-database.
 * Reads both formats: the plain URL ({@code "https://myanimelist.net/anime/2994"}) and the object
 * with the kind of relation as the meta data provider labels it
 * ({@code {"uri": "https://myanimelist.net/anime/2994", "relation": "Summary"}}).
 */
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class AODBRelatedAnime {

    String uri;

    /** the provider's own label, e.g. "Sequel", "SEQUEL", "sequel"; null if the provider doesn't name it */
    String relation;

    @JsonCreator
    public AODBRelatedAnime(@JsonProperty("uri") String uri, @JsonProperty("relation") String relation) {
        this.uri = uri;
        this.relation = relation;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AODBRelatedAnime fromUri(String uri) {
        return new AODBRelatedAnime(uri, null);
    }
}
