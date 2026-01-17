package net.fribbtastic.coding.animelistsgenerator.models;

/**
 * @author Frederic Eßer
 */
public class SeasonFilter {

    @Override
    public boolean equals(Object obj) {
        // If the Object is null, return true to omit it from the JSON
        if (obj == null) {
            return true;
        }
        // don't do anything if the Object is not a Season
        if (!(obj instanceof Season season)) {
            return false;
        }
        Integer tmdb = season.getTheMovieDb();
        Integer tvdb = season.getThetvdb();

        // return true when both tvdb and tmdb are null
        return tvdb == null && tmdb == null;
    }
}
