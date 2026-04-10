package net.fribbtastic.coding.animelistsgenerator.index;

import net.fribbtastic.coding.animelistsgenerator.models.AnimeItem;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Frederic Eßer
 */
@Service
public class IndexService {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(IndexService.class);

    /**
     * Generate the list of indexes of the merged list for each anime item
     *
     * @param mergedList the list containing the merged anime items
     * @return the map of indexes
     */
//    public Map<String, List<Integer>> generateIndex(ArrayList<AnimeItem> mergedList) {
//        LOGGER.info("Generating index of merged anime lists");
//
//        Map<String, List<Integer>> indexMap = new HashMap<>();
//
//        // iterate over every item in the merged list
//        for (int i = 0; i < mergedList.size(); i++) {
//            AnimeItem item = mergedList.get(i);
//
//            // iterate over every available ID in the item
//            for (Map.Entry<String, String> entry : item.getIdMap().entrySet()) {
//                String indexKey = entry.getKey() + ":" + entry.getValue();
//
//                indexMap.computeIfAbsent(indexKey, k -> new ArrayList<>()).add(i);
//            }
//        }
//
//        return indexMap;
//    }

    public Map<String, Map<String, List<Integer>>> generateIndex(ArrayList<AnimeItem> mergedList) {
        LOGGER.info("Generating index of merged anime lists");

        Map<String, Map<String, List<Integer>>> shardIndexMap = new HashMap<>();

        // iterate over every item in the merged list
        for (int i = 0; i < mergedList.size(); i++) {
            AnimeItem item = mergedList.get(i);

            // iterate over every available ID in the item
            for (Map.Entry<String, String> entry : item.getIdMap().entrySet()) {
                String source = entry.getKey(); // e.g. "anidb"
                String value = entry.getValue(); // e.g. "1"

                shardIndexMap.computeIfAbsent(source, s -> new HashMap<>())
                        .computeIfAbsent(value, v -> new ArrayList<>())
                        .add(i);
            }
        }

        return shardIndexMap;
    }
}
