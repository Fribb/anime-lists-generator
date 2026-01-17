package net.fribbtastic.coding.animelistsgenerator.animeLists.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.ArrayList;

/**
 * @author Frederic Eßer
 */
@Data
@JsonRootName("anime-list")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnimeLists {

    @JacksonXmlProperty(localName = "anime")
    @JacksonXmlElementWrapper(useWrapping = false)
    private ArrayList<AnimeListsItem> items;

}
