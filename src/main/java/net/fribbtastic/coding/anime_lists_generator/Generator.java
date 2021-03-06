/**
 * 
 */
package net.fribbtastic.coding.anime_lists_generator;

import net.fribbtastic.coding.anime_lists_generator.utils.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.net.MalformedURLException;
import java.net.URL;

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
		JSONArray animeOfflineDatabase;
		JSONArray animeLists;

		// Request the Anime-Offline-Database to parse it into a more condensed format
		String animeOfflineDatabaseResponse = HTTPUtils.getResponse(this.commonUtils.getAnimeOfflineDbUrl());

		if (animeOfflineDatabaseResponse != null) {
			JSONObject animeOfflineDatabaseObject = new JSONObject(animeOfflineDatabaseResponse);

			animeOfflineDatabase = this.parseAnimeOfflineDatabase(animeOfflineDatabaseObject.getJSONArray("data"));

			FileUtils.writeFile(animeOfflineDatabase.toString(), this.commonUtils.getAnimeOfflineDbFilePath());
		} else {
			logger.error("There was an error requesting the Anime-Offline-Database");
			return;
		}

		// Request the Anime-lists to parse it into a more condensed format
		String animelistResponse = HTTPUtils.getResponse(this.commonUtils.getAnimeListsUrl());

		if (animelistResponse != null) {
			
			JSONObject animeListObject = XML.toJSONObject(animelistResponse);
			JSONObject animelistElem = animeListObject.getJSONObject("anime-list");
			
			animeLists = this.parseAnimeLists(animelistElem.getJSONArray("anime"));
			
//			this.cleanUpAnimeList(animeListParsed);
			
			FileUtils.writeFile(animeLists.toString(), this.commonUtils.getAnimeListsFilePath());
		} else {
			logger.error("There was an error requesting the anime-lists");
			return;
		}
		
		// merge the elements
		JSONArray fullList = this.mergeLists(animeOfflineDatabase, animeLists);
		FileUtils.writeFile(fullList.toString(), this.commonUtils.getAnimeListFullFilePath());
	}
	
	/**
	 * merge both lists
	 * 
	 * @param animeOfflineDatabase
	 * @param animeLists
	 */
	private JSONArray mergeLists(JSONArray animeOfflineDatabase, JSONArray animeLists) {
		logger.info("Merging lists");
		String anidbName = this.commonUtils.getAnimeListsShortSource("anidbid") + "_id";
		String imdbName = this.commonUtils.getAnimeListsShortSource("imdbid") + "_id";
		String tmdbName = this.commonUtils.getAnimeListsShortSource("tmdbid") + "_id";
		JSONArray result = new JSONArray();
		
		for (Object item : animeOfflineDatabase) {
			JSONObject animeItem = (JSONObject) item;
			
			if (animeItem.has(anidbName)) {
				Integer anidbId = animeItem.getInt(anidbName);
				
				// get the IDs from the anime-lists
				JSONObject additionalIds = this.getIds(anidbId, animeLists);
				
				for(String addIdsKey : additionalIds.keySet()) {
					animeItem.put(addIdsKey, additionalIds.get(addIdsKey));
				}
				
				// lookup tmdb ID if not already available
				String type = animeItem.getString("type");
				if (type.equals("MOVIE")) {
					
					// only request the TMDB ID if it has a IMDB ID and not a TMDB ID 
					if (animeItem.has(imdbName) && !animeItem.has(tmdbName)) {
						String imdbId = animeItem.getString(imdbName);
						Integer tmdbId = TheMovieDBUtils.lookupTmdbId(imdbId, "imdb_id", "movie_results");
						if(tmdbId != -1) {
							animeItem.put(tmdbName, tmdbId);
						}
					}
				}
			}
			
			result.put(animeItem);
		}
		
		return result;
	}
	
	private JSONObject getIds(Integer id, JSONArray animeLists) {
		String anidbName = this.commonUtils.getAnimeListsShortSource("anidbid") + "_id";
		String tvdbName = this.commonUtils.getAnimeListsShortSource("tvdbid") + "_id";
		String tmdbName = this.commonUtils.getAnimeListsShortSource("tmdbid") + "_id";
		String imdbName = this.commonUtils.getAnimeListsShortSource("imdbid") + "_id";
		JSONObject result = new JSONObject();
		
		for (Object item : animeLists) {
			JSONObject animeItem = (JSONObject) item;
			
			if (animeItem.get(anidbName).equals(id)) {
				
				// get the tvdbID if it is available
				// only accept Integer values
				if (animeItem.has(tvdbName)) {
					
					Object tvdbId = animeItem.get(tvdbName);
					if(tvdbId instanceof Integer) {
						result.put(tvdbName, animeItem.get(tvdbName));
					}
				}

				// get the tmdbID if it is available
				if (animeItem.has(tmdbName)) {
					result.put(tmdbName, animeItem.get(tmdbName));
				}

				// get the imdbID if it is available
				// IMDB Ids start with tt to be valid
				// pick only the first entry if it is a comma separated list of ids 
				if (animeItem.has(imdbName)) {
					Object imdbId = animeItem.get(imdbName);
					if(imdbId instanceof String && imdbId.toString().startsWith("tt")) {
						String[] split = imdbId.toString().split(",");
						result.put(imdbName, split[0]);
					}
				}
			}
			
			logger.debug("got IDs from anime-lists-full: " + animeItem.toString());
		}
		
		return result;
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
					
					// in rare cases, the anime-list can contain both an TVDB ID and IMDB ID
					// if it has a IMDB ID then we can consider it a movie and need to look it up as a movie
					if(animeIds.has(imdbName)) {
						if(animeIds.get(imdbName).toString().startsWith("tt")) {
							String imdbId = animeIds.getString(imdbName);
							Integer tmdbId = TheMovieDBUtils.lookupTmdbId(imdbId, "imdb_id", "movie_results");
							logger.info("adding tmbdid (" + tmdbId + ")");
							animeIds.put(tmdbName, tmdbId);
						} else {
							logger.warn("IMDB ID was available but not a string (should start with tt)");
						}
					} else {
						Integer tmdbId = TheMovieDBUtils.lookupTmdbId(tvdbId, "tvdb_id", "tv_results");
						logger.info("adding tmbdid (" + tmdbId + ")");
						animeIds.put(tmdbName, tmdbId);
					}
					
					
				}
			} else if (animeIds.get(tvdbName) instanceof String) {
				logger.debug("TVDB ID is a String (" + animeIds.getString(tvdbName) + ")");
				
				// remove invalid ID
				animeIds.remove(tvdbName);
				
				if (animeIds.has(imdbName) && !animeIds.has(tmdbName)) {
					logger.debug("IMDB ID is available - looking up TMDB ID");
					
					String imdbId = animeIds.getString(imdbName);
					
					if (imdbId.startsWith("tt")) {
						Integer tmdbId = TheMovieDBUtils.lookupTmdbId(imdbId, "imdb_id", "movie_results");
						logger.info("adding tmdbid (" + tmdbId + ")");
						animeIds.put(tmdbName, tmdbId);
					} else {
						logger.warn("IMDB ID was not a correct ID (" + imdbId + ")");
					}
					
				} else {
					logger.warn("IMDB ID is not available - can't do anything here");
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
			
			if (animeItem.has("type")) {
				newItem.put("type", animeItem.getString("type"));
			}

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
					logger.trace(keyString + " not available in map");
				}
			}
			
			result.put(newItem);
		}
		
		return result;
	}

}
