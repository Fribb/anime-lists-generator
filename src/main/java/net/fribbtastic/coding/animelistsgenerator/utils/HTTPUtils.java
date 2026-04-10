package net.fribbtastic.coding.animelistsgenerator.utils;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import net.fribbtastic.coding.animelistsgenerator.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Frederic Eßer
 */
@Component
public class HTTPUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPUtils.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36";

    private final HttpClient client;
    private final RateLimiter rateLimiter;
    private final Retry retry;

    @SuppressWarnings("FieldCanBeLocal")
    private final Integer requestsInWindow = 2;
    @SuppressWarnings("FieldCanBeLocal")
    private final Duration requestWindow = Duration.ofSeconds(1);

    public HTTPUtils(HttpClient client) {

        // configure the HTTP Client
        this.client = client;

        // configure the RateLimiter
        RateLimiterConfig rateLimiterConfig = RateLimiterConfig.custom()
                .limitForPeriod(this.requestsInWindow)
                .limitRefreshPeriod(requestWindow)
                .timeoutDuration(TIMEOUT)
                .build();

        this.rateLimiter = RateLimiter.of("anime-list-generator", rateLimiterConfig);

        // configure the retry with exponential backoff
        IntervalFunction intervalFunction = IntervalFunction.ofExponentialBackoff(500, 2.0);

        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(4)
                .intervalFunction(intervalFunction)
                .ignoreExceptions(NotFoundException.class)
                .retryExceptions(IOException.class, RuntimeException.class)
                .build();

        this.retry = Retry.of("anime-list-generator", retryConfig);

        // subscribe to the retry event and log the attempt
        this.retry.getEventPublisher().onRetry(event -> LOGGER.info("retrying request #{}", event.getNumberOfRetryAttempts()));
    }

    /**
     * get the response of a request for the given URL
     *
     * @param urlString the URL as String
     * @return the response as String
     */
    public String getResponse(String urlString) {
        LOGGER.debug("Sending request to {}", urlString);

        Supplier<String> supplier = () -> this.executeRequest(urlString);

        Supplier<String> rateLimited = RateLimiter.decorateSupplier(rateLimiter, supplier);
        Supplier<String> retriable = Retry.decorateSupplier(retry, rateLimited);

        return retriable.get();
    }

    /**
     * execute the request and return the response
     *
     * @param urlString the URL as String
     * @return the response as String
     */
    private String executeRequest(String urlString) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .timeout(TIMEOUT)
                    .build();

            HttpResponse<InputStream> response = this.client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            int statusCode = response.statusCode();
            LOGGER.debug("Response Code: {}", statusCode);

            if (statusCode == 404) {
                throw new NotFoundException("Resource not found" + urlString);
            } else if (statusCode >= 500) {
                throw new RuntimeException("Server error");
            } else if (statusCode == 429) {
                throw new RuntimeException("Rate limit exceeded");
            } else if (statusCode >= 400) {
                throw new RuntimeException("Client error; " + statusCode);
            }

            try (InputStream inputStream = response.body();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                return reader.lines().collect(Collectors.joining("\n"));
            }

        } catch (IOException e) {
            // this triggers a retry
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
