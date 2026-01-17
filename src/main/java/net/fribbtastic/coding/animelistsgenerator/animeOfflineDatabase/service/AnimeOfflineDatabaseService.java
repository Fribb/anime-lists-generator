package net.fribbtastic.coding.animelistsgenerator.animeOfflineDatabase.service;

import net.fribbtastic.coding.animelistsgenerator.animeOfflineDatabase.dataSources.AnimeOfflineDbDataSource;
import net.fribbtastic.coding.animelistsgenerator.animeOfflineDatabase.models.AODB;
import net.fribbtastic.coding.animelistsgenerator.animeOfflineDatabase.models.AODBItem;
import net.fribbtastic.coding.animelistsgenerator.models.AnimeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * @author Frederic Eßer
 */
@Service
public class AnimeOfflineDatabaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnimeOfflineDatabaseService.class);

    private final AnimeOfflineDbDataSource dataSource;

    public AnimeOfflineDatabaseService(AnimeOfflineDbDataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Generate the condensed List of AnimeOfflineDatabase sources
     *
     * @return an {@link ArrayList} of {@link AnimeItem} containing the source IDs
     */
    public ArrayList<AnimeItem> generateList() {
        LOGGER.info("Starting AnimeOfflineDatabase operation");

        // get AnimeOfflineDatabase file from the repository and store the response in a POJO
        AODB aodb = this.getAnimeOfflineDatabase();
        LOGGER.info("AnimeOfflineDatabase last updated: {}", aodb.getLastUpdate());

        // get the AnimeOfflineDatabase item list
        ArrayList<AODBItem> aodbItems = aodb.getData();
        LOGGER.info("Number of Items: {}", aodbItems.size());

        // parse the List of AODB Items to a List of AnimeListsItems
        ArrayList<AnimeItem> animeItems = this.parseSources(aodbItems);
        LOGGER.info("Number of parsed Items: {}", animeItems.size());

        return animeItems;
    }

    /**
     * get the AnimeOfflineDatabase file from the GitHub repository and parse the result to a POJO<br>
     * the POJO will only contain the necessary information (the sources for each entry)
     *
     * @return the {@link AODB} Object with all AODB entries
     */
    public AODB getAnimeOfflineDatabase() {
        LOGGER.info("getting AnimeOfflineDatabase from source");

        // load the animeOfflineDatabase from the GitHub Repository
        return this.dataSource.loadData();
    }

    /**
     * parse the sources in the List of AnimeOfflineDatabase items to a more condensed version
     *
     * @param aodbItems - the List of AnimeOfflineDatabase items
     * @return the {@link ArrayList} of {@link AnimeItem} with the IDs set
     */
    public ArrayList<AnimeItem> parseSources(ArrayList<AODBItem> aodbItems) {
        LOGGER.info("parsing AnimeOfflineDatabase items to standardized structure");
        ArrayList<AnimeItem> result = new ArrayList<>();

        for (AODBItem item : aodbItems) {
            // add the sources
            AnimeItem newItem = AnimeItem.fromAODBSourceUrls(item.getSources());

            // set the type
            String type = item.getType();
            newItem.setType(type);

            // add the new Item to the List
            result.add(newItem);
        }

        return result;
    }
}
