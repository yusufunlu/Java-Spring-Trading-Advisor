package com.yusufu.interviewproject.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
public class PolygonDataService {

    private static final String API_KEY = "API_KEY";
    private static final String BASE_URL = "https://api.polygon.io/v2/aggs/ticker/%s/range/1/minute/%s/%s?adjusted=true&apiKey=" + API_KEY;
    private static final String TICKER = "AAPL";
    private static final String DATA_DIR = "data/" + TICKER;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private LocalDate lastDate;

    public PolygonDataService() {
        new File(DATA_DIR).mkdirs();
    }

    @PostConstruct
    public void init() {
        this.lastDate = findLatestSavedDate().orElse(LocalDate.now());
        log.info("Initial lastDate set to {}", lastDate);
    }

    @Scheduled(fixedRate = 6000) // 10 times per minute
    public void downloadNextRange() {
        try {
            lastDate = lastDate.minusDays(1);
            String url = String.format(BASE_URL, TICKER, lastDate.format(FORMATTER), lastDate.format(FORMATTER));

            log.info("Fetching data for date: {}", lastDate);

            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            HttpStatusCode statusCode = responseEntity.getStatusCode();

            if (statusCode != HttpStatus.OK) {
                log.warn("HTTP status not OK: {}", statusCode);
                return;
            }

            String responseBody = responseEntity.getBody();
            JsonNode root = objectMapper.readTree(responseBody);

            JsonNode results = root.path("results");
            if (!results.isArray() || results.size() == 0) {
                log.warn("No results for date {}", lastDate);
                return;
            }

            String ticker = root.path("ticker").asText();

            StringBuilder builder = new StringBuilder();
            builder.append("ticker,volume,open,close,high,low,time,transactions\n");
            for (JsonNode candle : results) {
                builder.append(String.format("%s,%.0f,%.2f,%.2f,%.2f,%.2f,%d,%d\n",
                        ticker,
                        candle.path("v").asDouble(),
                        candle.path("o").asDouble(),
                        candle.path("c").asDouble(),
                        candle.path("h").asDouble(),
                        candle.path("l").asDouble(),
                        candle.path("t").asLong(),
                        candle.path("n").asInt()));
            }

            String fileName = String.format("%s/%s_%s.csv", DATA_DIR, TICKER, lastDate.format(FORMATTER));
            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write(builder.toString());
            }
            log.info("Saved data for {} to {}", lastDate, fileName);

        } catch (Exception e) {
            log.error("Error while downloading data: ", e);
        }
    }

    private Optional<LocalDate> findLatestSavedDate() {
        try (Stream<Path> files = Files.list(Paths.get(DATA_DIR))) {
            return files
                    .filter(p -> p.getFileName().toString().endsWith(".csv"))
                    .map(p -> p.getFileName().toString().replace(TICKER + "_", "").replace(".csv", ""))
                    .map(dateStr -> {
                        try {
                            return LocalDate.parse(dateStr, FORMATTER);
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(d -> d != null)
                    .max(Comparator.naturalOrder());
        } catch (Exception e) {
            log.warn("Failed to list files in {}", DATA_DIR);
            return Optional.empty();
        }
    }
}
