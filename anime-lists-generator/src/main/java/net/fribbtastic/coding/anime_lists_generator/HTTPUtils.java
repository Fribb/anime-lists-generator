/**
 * 
 */
package net.fribbtastic.coding.anime_lists_generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * @author Fribb
 *
 */
public class HTTPUtils {
	private static Logger logger = Logger.getLogger(HTTPUtils.class);

	/**
	 * Return the content of a response
	 * 
	 * @param urlString
	 * @return the content of the response
	 */
	public static String getResponse(String urlString) {
		logger.info("Sending request to " + urlString.toString());

		try {
			URL url = new URL(urlString);

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			Integer code = connection.getResponseCode();
			BufferedReader reader = null;

			logger.info("Response Code: " + code);
			if (code == HttpURLConnection.HTTP_OK) {

				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			} else {
				reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
			}

			String line;
			StringBuffer stringBuffer = new StringBuffer();

			while ((line = reader.readLine()) != null) {
				stringBuffer.append(line);
			}

			reader.close();
			connection.disconnect();

			return stringBuffer.toString();

		} catch (MalformedURLException e) {
			logger.error("The URL is malformed", e);
		} catch (IOException e) {
			logger.error("An error occured while requesting a response", e);
		}

		return null;
	}

}
