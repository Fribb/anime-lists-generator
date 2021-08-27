/**
 * 
 */
package net.fribbtastic.coding.anime_lists_generator;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

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
		String path = PropertyUtils.getPropertyValue(PropertyUtils.PATH);
		
		//String animelistResponse = HTTPUtils.getResponse(this.commonUtils.getAnimeListsUrl());
		
		// Request the Anime-Offline-Database to parse it into a slimmer format
		String animeOfflineDatabaseResponse = HTTPUtils.getResponse(this.commonUtils.getAnimeOfflineDatabaseUrl());
		
		if (animeOfflineDatabaseResponse != null) {
			JSONObject animeOfflineDatabase = new JSONObject(animeOfflineDatabaseResponse);
			
			JSONArray aodParsed = this.parseAnimeOfflineDatabase(animeOfflineDatabase.getJSONArray("data"));
			
			FileUtils.writeFile(aodParsed.toString(), path + "/animeOfflineDatabase.json");
		} else {
			logger.error("There was an error requesting the Anime-Offline-Database");
			return;
		}
	}

	/**
	 * parse the Information from the Anime-Offline-Database into a reduced format
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
					
					String shortSource = this.commonUtils.getAODShortSource(host);
					
					newItem.put(shortSource + "_id", id);
					
				} catch (MalformedURLException e) {
					logger.warn("Url (" + source + ") was Malformed, skipping", e);
				}
			}
			result.put(newItem);
		}
		
		return result;
	}

}
