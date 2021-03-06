package net.fribbtastic.coding.anime_lists_generator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Hello world!
 *
 */
public class App {
	
	private static Logger logger = LogManager.getLogger(App.class);
	
	public static void main(String[] args) {
		logger.info("anime-lists-generator starting");
		
		Generator generator = new Generator();
		generator.generate();
		
		logger.info("anime-lists-generator finished");
	}
}
