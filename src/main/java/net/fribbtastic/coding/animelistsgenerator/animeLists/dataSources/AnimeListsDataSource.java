package net.fribbtastic.coding.animelistsgenerator.animeLists.dataSources;

import net.fribbtastic.coding.animelistsgenerator.animeLists.models.AnimeLists;

/**
 * @author Frederic Eßer
 */
public interface AnimeListsDataSource {

    AnimeLists loadData();
}
