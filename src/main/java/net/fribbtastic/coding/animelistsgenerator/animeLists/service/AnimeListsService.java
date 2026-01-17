package net.fribbtastic.coding.animelistsgenerator.animeLists.service;

import net.fribbtastic.coding.animelistsgenerator.animeLists.dataSources.AnimeListsDataSource;
import net.fribbtastic.coding.animelistsgenerator.animeLists.models.AnimeLists;
import net.fribbtastic.coding.animelistsgenerator.animeLists.models.AnimeListsItem;
import net.fribbtastic.coding.animelistsgenerator.models.AnimeItem;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * @author Frederic Eßer
 */
@Service
public class AnimeListsService {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AnimeListsService.class);

    private final AnimeListsDataSource dataSource;

    public AnimeListsService(AnimeListsDataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * generate the standardized List of AnimeItems from the anime-lists source
     *
     * @return the List of AnimeItems
     */
    public ArrayList<AnimeItem> generateList() {
        LOGGER.info("Starting AnimeLists operation");

        // get the anime-lists file from the repository and parse it to a POJO
        AnimeLists animeLists = this.getAnimeLists();
        LOGGER.info("Number of Items: {}", animeLists.getItems().size());

        ArrayList<AnimeItem> animeItems = this.parseAnimeLists(animeLists);
        LOGGER.info("Number of parsed Items: {}", animeItems.size());

        return animeItems;
    }

    /**
     * get the anime-lists file from the repository
     *
     * @return the anime-lists file as POJO
     */
    private AnimeLists getAnimeLists() {
        LOGGER.info("getting anime-lists from source");

        //load the anime-lists from the GitHub Repository
        return this.dataSource.loadData();
    }

    /**
     * parse the anime-lists file to a standardized List of AnimeItems
     *
     * @param animeLists the anime-lists file as POJO
     * @return the standardized List of AnimeItems
     */
    private ArrayList<AnimeItem> parseAnimeLists(AnimeLists animeLists) {
        LOGGER.info("parsing anime-lists item to standardized structure");
        ArrayList<AnimeItem> result = new ArrayList<>();

        for (AnimeListsItem item : animeLists.getItems()) {
            AnimeItem newItem = AnimeItem.fromAnimeListsSource(item);

            result.add(newItem);
        }

        return result;
    }
}
