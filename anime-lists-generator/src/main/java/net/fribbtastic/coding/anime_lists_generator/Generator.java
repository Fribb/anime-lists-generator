/**
 * 
 */
package net.fribbtastic.coding.anime_lists_generator;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import net.fribbtastic.coding.anime_lists_generator.utils.CommonUtils;
import net.fribbtastic.coding.anime_lists_generator.utils.FileUtils;
import net.fribbtastic.coding.anime_lists_generator.utils.HTTPUtils;
import net.fribbtastic.coding.anime_lists_generator.utils.PropertyUtils;
import net.fribbtastic.coding.anime_lists_generator.utils.TheMovieDBUtils;

/**
 * @author Fribb
 *
 */
public class Generator {
	private static Logger logger = LogManager.getLogger(Generator.class);

	private CommonUtils commonUtils;

	public Generator() {
		this.commonUtils = new CommonUtils();
		PropertyUtils.loadProperties();
	}

	/**
	 * generrate the anime-list
	 */
	public void generate() {
		logger.info("starting generating anime-lists");

		// Request the Anime-Offline-Database to parse it into a more condensed format
		String animeOfflineDatabaseResponse = HTTPUtils.getResponse(this.commonUtils.getAnimeOfflineDbUrl());

		if (animeOfflineDatabaseResponse != null) {
			JSONObject animeOfflineDatabase = new JSONObject(animeOfflineDatabaseResponse);

			JSONArray aodParsed = this.parseAnimeOfflineDatabase(animeOfflineDatabase.getJSONArray("data"));

			FileUtils.writeFile(aodParsed.toString(), this.commonUtils.getAnimeOfflineDbFilePath());
		} else {
			logger.error("There was an error requesting the Anime-Offline-Database");
			return;
		}

		// Request the Anime-lists to parse it into a more condensed format
		String animelistResponse = HTTPUtils.getResponse(this.commonUtils.getAnimeListsUrl());

		if (animelistResponse != null) {
			
			JSONObject animeListJson = XML.toJSONObject(animelistResponse);
			JSONObject animelistElem = animeListJson.getJSONObject("anime-list");
			
			JSONArray animeListParsed = this.parseAnimeLists(animelistElem.getJSONArray("anime"));
			
			this.cleanUpAnimeList(animeListParsed);
			
			FileUtils.writeFile(animeListParsed.toString(), this.commonUtils.getAnimeListsFilePath());
		} else {
			logger.error("There was an error requesting the anime-lists");
			return;
		}
	}
	
	/**
	 * cleaning up the anime-lists JSON Array.
	 * this is necessary because the TheTVDB Id is not a valid ID in the ScudLee anime-lists project but can contain
	 * movie, OVA, hentai, tv special, unknown and possibly other things as well.
	 * 
	 * But since we only want to have IDs (or some value to say that this is "invalid" or "unkown" like '-1')
	 * we need to clean this up.
	 * 
	 * TheMovieDB provides an API endpoint for External IDs that can either be requested for a specific tmdb ID or searched for.
	 * 
	 * The conditions are:
	 * 
	 * 1. tvdb ID is an Integer and there is no tmdb ID -> search for tvdb Id on tmdb API and add tmdb ID to dataset
	 * 2. tvdb ID is not an Integer and there is a tmdb ID -> lookup external IDs for tmdb ID and add tvdb ID to dataset
	 * 3. tvdb ID is not an Integer and there is no tmdb ID -> set tvdb and tmdb IDs to -1
	 * 
	 * @param animeListParsed
	 */
	private void cleanUpAnimeList(JSONArray animeListParsed) {
		logger.info("cleaning up generated list from anime-lists");
		String tvdbName = this.commonUtils.getAnimeListsShortSource("tvdbid") + "_id";
		String tmdbName = this.commonUtils.getAnimeListsShortSource("tmdbid") + "_id";
		String imdbName = this.commonUtils.getAnimeListsShortSource("imdbid") + "_id";
		
		for (Object item : animeListParsed) {
			JSONObject animeIds = (JSONObject) item;
			
			// the tvdb ID is an Integer that we can use it to lookup the tmdb ID if not already available
			if (animeIds.get(tvdbName) instanceof Integer) {
				logger.debug("TVDB ID is an Integer");
				Integer tvdbId = animeIds.getInt(tvdbName);
				
				if (animeIds.has(tmdbName)) {
					logger.debug("TMDB ID is available - nothing to do here");
				} else {
					logger.debug("TMDB ID is not available - looking up");
					
					Integer tmdbId = TheMovieDBUtils.lookupTmdbId(tvdbId, "tvdb_id", "tv_results");
					logger.info("adding tmbdid (" + tmdbId + ")");
					animeIds.put(tmdbName, tmdbId);
				}
			} else if (animeIds.get(tvdbName) instanceof String) {
				logger.debug("TVDB ID is a String");
				
				if (animeIds.has(imdbName) && !animeIds.has(tmdbName)) {
					logger.debug("IMDB ID is available - looking up TMDB ID");
					
					String imdbId = animeIds.getString(imdbName);
					
					if (imdbId.startsWith("tt")) {
						Integer tmdbId = TheMovieDBUtils.lookupTmdbId(imdbId, "imdb_id", "movie_results");
						logger.info("adding tmdbid (" + tmdbId + ")");
						animeIds.put(tmdbName, imdbId);
					} else {
						logger.debug("IMDB ID was not a correct ID (" + imdbId + ")");
					}
				} else {
					logger.debug("IMDB ID is not available - can't do anything here");
				}
			} else {
				logger.warn("TVDB ID is neither an Integer nor a String");
			}
		}
	}

	/**
	 * parse the Information from the Anime-Offline-Database response into a reduced format
	 * 
	 * @param data
	 * @return
	 */
	private JSONArray parseAnimeOfflineDatabase(JSONArray data) {
		logger.info("parsing Anime-Offline-Database");

		JSONArray result = new JSONArray();

		for (Object item : data) {
			JSONObject animeItem = (JSONObject) item;
			JSONObject newItem = new JSONObject();

			JSONArray sources = animeItem.getJSONArray("sources");

			for (Object s : sources) {
				String source = (String) s;

				try {
					URL sourceUrl = new URL(source);

					String host = sourceUrl.getHost();
					String path = sourceUrl.getPath();
					String id = path.substring(path.lastIndexOf("/") + 1);

					String shortSource = this.commonUtils.getAnimeOfflineDbShortSource(host);
					
					// if the ID is an integer then add an integer to the newItem
					if (NumberUtils.isCreatable(id)) {
						Integer intId = NumberUtils.createInteger(id);
						
						newItem.put(shortSource + "_id", intId);
					} else {
						newItem.put(shortSource + "_id", id);
					}

				} catch (MalformedURLException e) {
					logger.warn("Url (" + source + ") was Malformed, skipping", e);
				}
			}
			result.put(newItem);
		}

		return result;
	}
	
	/**
	 * parse the information from the anime-lists response into a reduced format
	 * 
	 * @param animeLists
	 * @return
	 */
	private JSONArray parseAnimeLists(JSONArray animeLists) {
		logger.info("parsing anime-lists");
		
		JSONArray result = new JSONArray();
		
		for (Object item : animeLists) {
			JSONObject animeItem = (JSONObject) item;
			JSONObject newItem = new JSONObject();
			
			for (String keyString : animeItem.keySet()) {
				String shortSource = this.commonUtils.getAnimeListsShortSource(keyString);
				
				if (shortSource != null) {
					newItem.put(shortSource + "_id", animeItem.get(keyString));
				} else {
					logger.debug(keyString + " not available in map");
				}
			}
			
			result.put(newItem);
		}
		
		return result;
	}

}
