package net.fribbtastic.coding.animelistsgenerator.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

/**
 * @author Frederic Eßer
 */
public class HTTPUtils {

    private static final Logger logger = LoggerFactory.getLogger(HTTPUtils.class);

    /**
     * Return the content of a response
     *
     * @param urlString the string of the URL
     * @return the content of the response
     */
    public static String getResponse(String urlString) {
        logger.debug("Sending request to {}", urlString);

        try {
            Thread.sleep(500);

            URL url = new URI(urlString).toURL();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            int code = connection.getResponseCode();
            BufferedReader reader;

            logger.info("Response Code: {}", code);
            if (code == HttpURLConnection.HTTP_OK) {

                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            String line;
            StringBuilder stringBuffer = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                stringBuffer.append(line);
            }

            reader.close();
            connection.disconnect();

            return stringBuffer.toString();

        } catch (InterruptedException e) {
            logger.error("Thread interrupted", e);
        } catch (MalformedURLException e) {
            logger.error("The URL is malformed", e);
        } catch (IOException e) {
            logger.error("An error occurred while requesting a response", e);
        } catch (URISyntaxException e) {
            logger.error("The URI Syntax is not correct", e);
        }

        return null;
    }
}
