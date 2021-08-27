/**
 * 
 */
package net.fribbtastic.coding.anime_lists_generator.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Fribb
 *
 */
public class PropertyUtils {

	private static Logger logger = LogManager.getLogger(PropertyUtils.class);

	// the App wide Properties
	private static Properties PROPERTIES = new Properties();

	// location of the properties file
	private static String FILE = "config.properties";

	// property keys
	public static String PATH = "project.path";

	/**
	 * Get the value of a given key
	 * 
	 * @param key - the key used in the properties
	 * @return the value of the property
	 */
	public static String getPropertyValue(String key) {
		return PROPERTIES.getProperty(key);
	}

	/**
	 * load the properties in the PROPERTIES variable to make them globally
	 * available
	 */
	public static void loadProperties() {
		String currentDir = ClassLoader.getSystemClassLoader().getResource(".").getPath();
		String sep = currentDir.endsWith(File.separator) ? "" : File.separator;

		File propertiesFile = new File(currentDir + sep + FILE);

		InputStream inputStream = null;

		try {
			inputStream = new FileInputStream(propertiesFile);

			PROPERTIES.load(inputStream);
		} catch (FileNotFoundException e) {
			logger.error("File was not found: ", e);
		} catch (IOException e) {
			logger.error("An error occurred loading the properties file", e);
		}
	}
}