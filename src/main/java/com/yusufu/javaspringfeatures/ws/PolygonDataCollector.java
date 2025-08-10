package com.yusufu.javaspringfeatures.ws;

import com.yusufu.javaspringfeatures.PolygonDataException;
import com.yusufu.javaspringfeatures.model.PolygonMetadata;
import com.yusufu.javaspringfeatures.model.PolygonResponse;
import com.yusufu.javaspringfeatures.model.TickData;
import com.yusufu.javaspringfeatures.repo.PolygonMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class PolygonDataCollector {

    private final InfluxDBService influxDBService;
    private final PolygonMetadataRepository metadataRepository;
    private final RestTemplate restTemplate;

    @Value("${external.polygon.api_key}")
    private String apiKey;
    private static final String TICKER = "AAPL";

    @Scheduled(fixedRate = 12*1000)
    public void backfillWithRetry() {
        LocalDate lastProcessedDate = getLastProcessedDate(TICKER);
        LocalDate dateToFetch = lastProcessedDate.plusDays(1);
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        if (dateToFetch.isAfter(today)) {
            System.out.println("After today's data can not be fetched " + dateToFetch);
            return;
        }

        String url = null;
        try {
            url = buildPolygonUrl(TICKER, dateToFetch);
            PolygonResponse response = fetchPolygonData(TICKER, dateToFetch);

            System.out.println("Successfully fetched tick data for " + dateToFetch);

            if (response.getResults() == null || response.getResults().isEmpty()) {
                saveMetadata(response, dateToFetch, url, null);
                return;
            }

            //It is using async writing
            //registered  event listener already to catch the async exception which try/catch can't do it here
            for (TickData tick : response.getResults()) {
                influxDBService.writeTickData(TICKER, tick);
            }
            influxDBService.checkAndThrowIfError();

            saveMetadata(response, dateToFetch, url, null);
            System.out.println("Successfully saved metadata data for " + dateToFetch);

        } catch (HttpClientErrorException.Forbidden e) {
            saveMetadata(null, dateToFetch, url, e.getMessage());
            throw new PolygonDataException(e.getMessage());
        } catch (HttpClientErrorException.Unauthorized e) {
            saveMetadata(null, dateToFetch, url, e.getMessage());
            throw new PolygonDataException(e.getMessage());
        } catch (HttpClientErrorException.TooManyRequests e) {
            saveMetadata(null, dateToFetch, url, e.getMessage());
            throw new PolygonDataException(e.getMessage());
        } catch (Exception e) {
            saveMetadata(null, dateToFetch, url, e.getMessage().substring(0, 1000));
            //TODO: Global exception handler is not able to catch this, fix it
            throw new PolygonDataException(e.getMessage());
        }
    }

    private void saveMetadata( PolygonResponse response, LocalDate date, String url, String errorMessage) {
        if(response!=null){
            PolygonMetadata metadata = PolygonMetadata.builder()
                    .ticker(response.getTicker())
                    .queryCount(response.getQueryCount())
                    .resultsCount(response.getResultsCount())
                    .adjusted(response.isAdjusted())
                    .status(response.getStatus())
                    .requestId(response.getRequestId())
                    .count(response.getCount())
                    .nextUrl(response.getNextUrl())
                    .forDate(date)
                    .retrievedAt(Instant.now())
                    .urlTried(url)
                    .errorMessage(errorMessage)
                    .build();
            metadataRepository.save(metadata);
        } else {
            PolygonMetadata metadata = PolygonMetadata.builder()
                    .ticker(TICKER)
                    .queryCount(0)
                    .resultsCount(0)
                    .adjusted(false)
                    .status("FAILED")
                    .requestId("N/A")
                    .count(0)
                    .nextUrl(null)
                    .forDate(date)
                    .retrievedAt(Instant.now())
                    .urlTried(url)
                    .errorMessage(errorMessage)
                    .build();
            metadataRepository.save(metadata);
        }


    }

    private PolygonResponse fetchPolygonData(String ticker, LocalDate date) {
        String url = buildPolygonUrl(ticker, date);
        return restTemplate.getForObject(url, PolygonResponse.class);
    }

    private String buildPolygonUrl(String ticker, LocalDate date) {
        return String.format(
                "https://api.polygon.io/v2/aggs/ticker/%s/range/1/minute/%s/%s?adjusted=true&sort=asc&limit=50000&apiKey=_eMvjl5ZlzXgQw7DxV2nq9v2YiMyh5Ia",
                ticker,
                date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                apiKey
        );
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private LocalDate getLastProcessedDate(String ticker) {
        return metadataRepository.findTopByTickerOrderByForDateDesc(ticker)
                .map(PolygonMetadata::getForDate)
                .orElse(LocalDate.now().minusMonths(1));
    }
}
