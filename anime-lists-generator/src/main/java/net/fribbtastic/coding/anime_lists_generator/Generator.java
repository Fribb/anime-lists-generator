/**
 * 
 */
package net.fribbtastic.coding.anime_lists_generator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import net.fribbtastic.coding.anime_lists_generator.utils.CommonUtils;
import net.fribbtastic.coding.anime_lists_generator.utils.FileUtils;
import net.fribbtastic.coding.anime_lists_generator.utils.PropertyUtils;

/**
 * @author Fribb
 *
 */
public class Generator {
	private static Logger logger = Logger.getLogger(Generator.class);

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
			
			FileUtils.writeFile(animeListParsed.toString(), this.commonUtils.getAnimeListsFilePath());
		} else {
			logger.error("There was an error requesting the anime-lists");
			return;
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

					newItem.put(shortSource + "_id", id);

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
