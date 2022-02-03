/**
 * 
 */
package net.fribbtastic.coding.anime_lists_generator.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StrSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Fribb
 *
 */
public class TheMovieDBUtils {
	private static Logger logger = LogManager.getLogger(TheMovieDBUtils.class);
	
	/**
	 * the API URLs for TheMovieDB.org
	 */
	private static String API_SCHEME = "https";
	private static String API_HOST = "api.tmdb.org";
	private static String API_FIND = "/3/find/${tvdbid}";
	private static String API_FIND_QUERY = "api_key=${api_key}&external_source=${source}";

	/**
	 * lookup the TMDB ID with a TVDB ID
	 * 
	 * @param id
	 * @param source
	 * @return
	 */
	public static Integer lookupTmdbId(Object id, String source, String resultsName) {
		logger.info("looking up TheMovieDB ID for " + source + " (" + id + ") with " + resultsName);
		
		Map<String, String> data = new HashMap<String, String>();
		data.put("tvdbid", id.toString());
		data.put("api_key", PropertyUtils.getPropertyValue(PropertyUtils.THEMOVIEDBAPIKEY));
		data.put("source", source);
		
		String find = StrSubstitutor.replace(API_FIND, data);
		String query = StrSubstitutor.replace(API_FIND_QUERY, data);
		
		try {
			URI uri = new URI(API_SCHEME, null, API_HOST, -1, find, query, null);
			
			JSONObject response = new JSONObject(HTTPUtils.getResponse(uri.toASCIIString()));
			
			JSONArray results = response.getJSONArray(resultsName);
			
			if (results.length() > 0) {
				// get the first item from the results
				JSONObject item = (JSONObject) results.get(0);
				
				return item.getInt("id");
			} else {
				return -1;
			}
			
		} catch (URISyntaxException e) {
			logger.error("URI Syntax is wrong", e);
		}
		
		return null;
	}
}