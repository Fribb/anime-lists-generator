package net.fribbtastic.coding.animelistsgenerator.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Frederic Eßer
 */
@Component
public class Properties {

    public static String projectPath;
    public static String theMovieDbApiKey;

    @Value("${project.path}")
    public void setProjectPath(String path) {
        projectPath = path;
    }

    @Value("${project.apikey.themoviedb}")
    public void setTheMovieDbApiKey(String theMovieDbApiKey) {
        Properties.theMovieDbApiKey = theMovieDbApiKey;
    }


}
