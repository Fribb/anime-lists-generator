package net.fribbtastic.coding.animelistsgenerator.animeLists.dataSources;

import net.fribbtastic.coding.animelistsgenerator.Constants;
import net.fribbtastic.coding.animelistsgenerator.animeLists.models.AnimeLists;
import net.fribbtastic.coding.animelistsgenerator.exceptions.NotFoundException;
import net.fribbtastic.coding.animelistsgenerator.utils.HTTPUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tools.jackson.dataformat.xml.XmlMapper;

/**
 * @author Frederic Eßer
 */
@Service
@Profile("prod")
public class AnimeListsGitHubDataSource implements  AnimeListsDataSource{

    private static final Logger LOGGER = LoggerFactory.getLogger(AnimeListsGitHubDataSource.class);

    private final HTTPUtils httpUtils;
    private final XmlMapper mapper;

    public AnimeListsGitHubDataSource(HTTPUtils httpUtils, XmlMapper mapper) {
        this.httpUtils = httpUtils;
        this.mapper = mapper;
    }

    @Override
    public AnimeLists loadData() {
        LOGGER.info("Loading Data from Source: {}", Constants.ANIMELISTS_URL);

        try {
            String response = this.httpUtils.getResponse(Constants.ANIMELISTS_URL);

            return mapper.readValue(response, AnimeLists.class);
        } catch (NotFoundException e) {
            LOGGER.error("Request returned 404");
            return null;
        } catch (Exception e) {
            LOGGER.error("Error parsing the anime-lists XML file: {}", e.getMessage());
            return null;
        }
    }
}
