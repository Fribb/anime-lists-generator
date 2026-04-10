package net.fribbtastic.coding.animelistsgenerator.animeOfflineDatabase.dataSources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.fribbtastic.coding.animelistsgenerator.Constants;
import net.fribbtastic.coding.animelistsgenerator.animeOfflineDatabase.models.AODB;
import net.fribbtastic.coding.animelistsgenerator.exceptions.NotFoundException;
import net.fribbtastic.coding.animelistsgenerator.utils.HTTPUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * @author Frederic Eßer
 */
@Service
@Profile("prod")
public class AODBGitHubDataSource implements AnimeOfflineDbDataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AODBGitHubDataSource.class);

    private final HTTPUtils httpUtils;
    private final ObjectMapper mapper;

    public AODBGitHubDataSource(HTTPUtils httpUtils, ObjectMapper mapper) {
        this.httpUtils = httpUtils;
        this.mapper = mapper;
    }

    @Override
    public AODB loadData() {
        LOGGER.info("Loading Data from Source: {}", Constants.ANIMEOFFLINEDB_URL);

        try {
            String response = this.httpUtils.getResponse(Constants.ANIMEOFFLINEDB_URL);

            return this.mapper.readValue(response, AODB.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (NotFoundException e) {
            LOGGER.error("Request returned 404");
            return null;
        }

    }
}
