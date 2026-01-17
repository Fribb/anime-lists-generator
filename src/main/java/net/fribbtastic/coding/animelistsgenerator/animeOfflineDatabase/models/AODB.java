package net.fribbtastic.coding.animelistsgenerator.animeOfflineDatabase.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;

/**
 * @author Frederic Eßer
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AODB {

    @JsonProperty("lastUpdate")
    private String lastUpdate;

    @JsonProperty("data")
    private ArrayList<AODBItem> data;

}
