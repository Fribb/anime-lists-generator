package net.fribbtastic.coding.animelistsgenerator.themoviedb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;

/**
 * @author Frederic Eßer
 * Example for a Movie result
 * {
 *     "movie_results": [
 *         {
 *             "adult": false,
 *             "backdrop_path": "/gl0jzn4BupSbL2qMVeqrjKkF9Js.jpg",
 *             "id": 128,
 *             "title": "Princess Mononoke",
 *             "original_title": "もののけ姫",
 *             "overview": "Ashitaka, a prince of the disappearing Emishi people, is cursed by a demonized boar god and must journey to the west to find a cure. Along the way, he encounters San, a young human woman fighting to protect the forest, and Lady Eboshi, who is trying to destroy it. Ashitaka must find a way to bring balance to this conflict.",
 *             "poster_path": "/cMYCDADoLKLbB83g4WnJegaZimC.jpg",
 *             "media_type": "movie",
 *             "original_language": "ja",
 *             "genre_ids": [
 *                 12,
 *                 14,
 *                 16
 *             ],
 *             "popularity": 10.0822,
 *             "release_date": "1997-07-12",
 *             "video": false,
 *             "vote_average": 8.322,
 *             "vote_count": 8699
 *         }
 *     ],
 *     "person_results": [],
 *     "tv_results": [],
 *     "tv_episode_results": [],
 *     "tv_season_results": []
 * }
 * ---------------------------------------------------------------------------
 * Example for a TV result
 * {
 *     "movie_results": [],
 *     "person_results": [],
 *     "tv_results": [
 *         {
 *             "adult": false,
 *             "backdrop_path": "/9Or0JCa6D6H9gbCwonGja1fBPgh.jpg",
 *             "id": 26209,
 *             "name": "Crest of the Stars",
 *             "original_name": "星界の紋章",
 *             "overview": "Jinto Lin's life changes forever when the Humankind Empire Abh takes over his home planet of Martine without firing a single shot. He is soon sent off to study the Abh language and culture and to prepare himself for his future as a nobleman - a future he never dreamed of. Or wanted. Now, Jinto is entering the next phase of his training, and he is about to meet his first Abh, the lovely Lafiel. But Jinto is about to learn that she is more than she appears to be. And together they will have to fight for their very lives.",
 *             "poster_path": "/bimOHbtqCjwpB2Dy6EXbcueg2td.jpg",
 *             "media_type": "tv",
 *             "original_language": "ja",
 *             "genre_ids": [
 *                 16,
 *                 18
 *             ],
 *             "popularity": 5.8626,
 *             "first_air_date": "1999-01-02",
 *             "vote_average": 7.1,
 *             "vote_count": 13,
 *             "origin_country": [
 *                 "JP"
 *             ]
 *         }
 *     ],
 *     "tv_episode_results": [
 *         {
 *             "id": 1005783,
 *             "name": "Little Green Men",
 *             "overview": "Stardate: Unknown. Quark and Rom take Nog to Earth and Starfleet Academy, but a malfunction with the ship takes the crew back in time, to Roswell New Mexico in 1947.",
 *             "media_type": "tv_episode",
 *             "vote_average": 7.8,
 *             "vote_count": 27,
 *             "air_date": "1995-11-13",
 *             "episode_number": 7,
 *             "episode_type": "standard",
 *             "production_code": "40510-480",
 *             "runtime": 45,
 *             "season_number": 4,
 *             "show_id": 580,
 *             "still_path": "/eejMLH9zy8KyMPMMkOTn7WZUzaW.jpg"
 *         }
 *     ],
 *     "tv_season_results": []
 * }
 * 
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbFindResult {

    @JsonProperty("movie_results")
    private ArrayList<TmdbMovieResult> movieResults;

    @JsonProperty("tv_results")
    private ArrayList<TmdbTvResult> tvResults;
}
