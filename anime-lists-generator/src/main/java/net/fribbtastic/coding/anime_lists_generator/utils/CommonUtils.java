/**
 * 
 */
package net.fribbtastic.coding.anime_lists_generator.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author Fribb
 *
 */
public class CommonUtils {
	private static Logger logger = Logger.getLogger(CommonUtils.class);
	
	private String animeListsUrl 			= "https://raw.githubusercontent.com/Anime-Lists/anime-lists/master/anime-list-full.xml";
	private String animeOfflineDatabaseUrl 	= "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/anime-offline-database.json";
	private HashMap<String, String> animeOfflineDatabaseSources = new HashMap<String, String>();
	
	/**
	 * 
	 */
	public CommonUtils() {
		this.initAODSources();
	}

	/**
	 * @return the animeLists
	 */
	public String getAnimeListsUrl() {
		return this.animeListsUrl;
	}
	/**
	 * @return the animeOfflineDatabase
	 */
	public String getAnimeOfflineDatabaseUrl() {
		return this.animeOfflineDatabaseUrl;
	}
	
	/**
	 * initialize the Anime-Offline-Database sources
	 */
	private void initAODSources() {
		this.animeOfflineDatabaseSources.put("anidb.net", "anidb");
		this.animeOfflineDatabaseSources.put("anilist.co", "anilist");
		this.animeOfflineDatabaseSources.put("anime-planet.com", "anime-planet"); // anime-planet does not have an ID as integer value but rather uses the title
		this.animeOfflineDatabaseSources.put("anisearch.com", "anisearch");
		this.animeOfflineDatabaseSources.put("kitsu.io", "kitsu");
		this.animeOfflineDatabaseSources.put("livechart.me", "livechart");
		this.animeOfflineDatabaseSources.put("myanimelist.net", "mal");
		this.animeOfflineDatabaseSources.put("notify.moe", "notify.moe"); // notify.moe does not use an ID as integer value but rather uses a hash
	}
	
	/**
	 * get the short version of the Anime-Offline-Database Source URL
	 * 
	 * @param key
	 * @return value
	 */
	public String getAODShortSource(String key) {
		return this.animeOfflineDatabaseSources.get(key);
	}
}