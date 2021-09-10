/**
 * 
 */
package net.fribbtastic.coding.anime_lists_generator.utils;

import java.io.File;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Fribb
 *
 */
public class CommonUtils {
	
	private String animeListsUrl = "https://raw.githubusercontent.com/ScudLee/anime-lists/master/anime-list-full.xml";
	private String animeOfflineDbUrl = "https://raw.githubusercontent.com/manami-project/anime-offline-database/master/anime-offline-database.json";
	private String condensedAnimOfflineDBFileName =  "anime-offline-database-reduced.json";
	private String condensedAnimeListsFileName = "anime-lists-reduced.json";
	private HashMap<String, String> animeOfflineDbSources = new HashMap<String, String>();
	private HashMap<String, String> animeListSources = new HashMap<String, String>();

	
	/**
	 * 
	 */
	public CommonUtils() {
		this.initAnimeOfflineDbSources();
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
	public String getAnimeOfflineDbUrl() {
		return this.animeOfflineDbUrl;
	}
	
	/**
	 * initialize the Anime-Offline-Database sources
	 */
	private void initAnimeOfflineDbSources() {
		this.animeOfflineDbSources.put("anidb.net", "anidb");
		this.animeOfflineDbSources.put("anilist.co", "anilist");
		this.animeOfflineDbSources.put("anime-planet.com", "anime-planet"); // anime-planet does not have an ID as integer value but rather uses the title
		this.animeOfflineDbSources.put("anisearch.com", "anisearch");
		this.animeOfflineDbSources.put("kitsu.io", "kitsu");
		this.animeOfflineDbSources.put("livechart.me", "livechart");
		this.animeOfflineDbSources.put("myanimelist.net", "mal");
		this.animeOfflineDbSources.put("notify.moe", "notify.moe"); // notify.moe does not use an ID as integer value but rather uses a hash
		
		this.animeListSources.put("anidbid", "anidb");
		this.animeListSources.put("tvdbid", "thetvdb");
		this.animeListSources.put("tmdbid", "themoviedb");
		this.animeListSources.put("imdbid", "imdb");
	}
	
	/**
	 * get the short version of the Anime-Offline-Database Source URL
	 * 
	 * @param key
	 * @return value
	 */
	public String getAnimeOfflineDbShortSource(String key) {
		return this.animeOfflineDbSources.get(key);
	}
	
	/**
	 * get the short version for the anime-lists key
	 * 
	 * @param key
	 * @return value
	 */
	public String getAnimeListsShortSource(String key) {
		return this.animeListSources.get(key);
	}
	
	/**
	 * get the complete file path for the condensed Anime-Offline-Database
	 *  
	 * @return
	 */
	public String getAnimeOfflineDbFilePath() {
		String path = PropertyUtils.getPropertyValue(PropertyUtils.PATH);
		
		return path + File.separator + this.condensedAnimOfflineDBFileName;
	}

	/**
	 * get the complete file path for the condensed anime-lists
	 * 
	 * @return
	 */
	public String getAnimeListsFilePath() {
		String path = PropertyUtils.getPropertyValue(PropertyUtils.PATH);
		
		return path + File.separator + this.condensedAnimeListsFileName;
	}
}