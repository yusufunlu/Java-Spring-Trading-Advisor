package com.yusufu.javaspringfeatures.ws;


import com.yusufu.javaspringfeatures.model.PolygonMetadata;
import com.yusufu.javaspringfeatures.model.PolygonResponse;
import com.yusufu.javaspringfeatures.model.TickData;
import com.yusufu.javaspringfeatures.repo.PolygonMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PolygonDataCollector {

    private final InfluxDBService influxDBService;
    private final PolygonMetadataRepository metadataRepository;
    private final RestTemplate restTemplate;

    @Value("${external.polygon.api_key}")
    private String apiKey;
    private static final String TICKER = "AAPL";

    @Scheduled(fixedRate = 30000)
    public void backfillWithRetry() {
        LocalDate current = getLastProcessedDate(TICKER).plusDays(1);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        while (!current.isAfter(today)) {
            if (isWeekend(current)) {
                current = current.plusDays(1);
                continue;
            }

            try {
                PolygonResponse response = fetchPolygonData(TICKER, current);

                if (response.getResults() == null || response.getResults().isEmpty()) {
                    if (current.equals(today)) {
                        return;
                    } else {
                        current = current.plusDays(1);
                        continue;
                    }
                }

                for (TickData tick : response.getResults()) {
                    influxDBService.writeTickData(TICKER, tick);
                }
                influxDBService.flushWriteApi();

                PolygonMetadata metadata = PolygonMetadata.builder()
                        .ticker(response.getTicker())
                        .queryCount(response.getQueryCount())
                        .resultsCount(response.getResultsCount())
                        .adjusted(response.isAdjusted())
                        .status(response.getStatus())
                        .requestId(response.getRequestId())
                        .count(response.getCount())
                        .nextUrl(response.getNextUrl())
                        .forDate(current)
                        .retrievedAt(Instant.now())
                        .build();

                metadataRepository.save(metadata);

            } catch (HttpClientErrorException.TooManyRequests e) {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException ignored) {}
                continue;
            } catch (Exception e) {
            }

            current = current.plusDays(1);
        }
    }

    private PolygonResponse fetchPolygonData(String ticker, LocalDate date) {
        String url = String.format("https://api.polygon.io/v2/aggs/ticker/%s/range/1/minute/%s/%s?adjusted=true&sort=asc&limit=50000&apiKey=%s",
                ticker,
                date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                apiKey);

        return restTemplate.getForObject(url, PolygonResponse.class);
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private LocalDate getLastProcessedDate(String ticker) {
        return metadataRepository.findTopByTickerOrderByForDateDesc(ticker)
                .map(PolygonMetadata::getForDate)
                .orElse(LocalDate.of(2025, 1, 1).minusDays(1));
    }
}
