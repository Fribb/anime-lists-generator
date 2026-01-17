package net.fribbtastic.coding.animelistsgenerator.animeOfflineDatabase.dataSources;

import net.fribbtastic.coding.animelistsgenerator.animeOfflineDatabase.models.AODB;

/**
 * @author Frederic Eßer
 */
public interface AnimeOfflineDbDataSource {

    AODB loadData();
}
