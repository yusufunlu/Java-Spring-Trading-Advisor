package com.yusufu.javaspringfeatures.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.influxdb.query.FluxRecord;
import com.yusufu.javaspringfeatures.model.TickData;
import com.yusufu.javaspringfeatures.model.PolygonResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

@Service
public class PolygonDataCollector {

    @Value("${external.polygon.api_key}")
    private String apiKey;

    private final InfluxDBService influxDBService;

    @Autowired
    private ObjectMapper mapper;

    // Base URL for Polygon's aggregates API, with placeholders for ticker, from_date, to_date, and API key.
    // This fetches a full day's data, which covers all trading hours including
    // pre-market (4:00 AM ET / 8:00 AM UTC) and after-hours (up to 8:00 PM ET / 00:00 AM UTC the next day).
    private static final String BASE_URL_TEMPLATE =
            "https://api.polygon.io/v2/aggs/ticker/%s/range/1/minute/%s/%s?adjusted=true&sort=asc&limit=50000&apiKey=%s";

    // Date formatter for the yyyy-MM-dd format used in the API request.
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // The start date for backfilling if no data exists in InfluxDB.
    private static final LocalDate BACKFILL_START_DATE = LocalDate.of(2025, 1, 2);

    public PolygonDataCollector(InfluxDBService influxDBService) {
        this.influxDBService = influxDBService;
    }

    /**
     * This method is automatically called after the bean is constructed.
     * It initiates the sequential backfilling of ticker data starting from
     * the earliest available date or the first day of 2025.
     */
    @PostConstruct
    public void init() {
        // Run the backfill in a separate thread to not block application startup.
        Thread backfillThread = new Thread(() -> backfillTickerData());
        backfillThread.start();
    }

    /**
     * This method is a scheduled task that runs every 10 seconds to check for and download
     * the previous day's data if it is not already present.
     */
    @Scheduled(fixedRate = 10000)
    public void scheduledDataCheck() {
        backfillTickerData();
    }

    /**
     * Backfills historical data for a given ticker day by day until it reaches the current date.
     */
    private void backfillTickerData() {
        String ticker = "AAPL";
        LocalDate dateToFetch;
        // Get the latest record from InfluxDB to determine the starting point for the backfill.
        List<FluxRecord> records = influxDBService.getLatestRecordForTicker(ticker);

        if (records.isEmpty()) {
            // If no data exists, start from the defined BACKFILL_START_DATE.
            dateToFetch = BACKFILL_START_DATE;
            System.out.println("No existing data found for " + ticker + ". Starting backfill from " + dateToFetch.format(FORMATTER));
        } else {
            // Get the timestamp from the latest record.
            Instant latestTimestamp = records.get(0).getTime();
            // Convert the UTC timestamp to a LocalDate and add one day to get the next date.
            // This handles the transition from one trading day's after-hours to the next day's pre-market.
            dateToFetch = latestTimestamp.atZone(ZoneOffset.UTC).toLocalDate().plusDays(1);
            System.out.println("Latest data found for " + ticker + " is from " + latestTimestamp);
            System.out.println("Checking for new data starting from: " + dateToFetch.format(FORMATTER));
        }

        LocalDate today = LocalDate.now(ZoneId.of("America/New_York"));

        // Only fetch data if the date to fetch is before today.
        // This ensures we only download a full day's worth of data.
        if (dateToFetch.isBefore(today)) {
            System.out.println("Fetching data for " + dateToFetch.format(FORMATTER));
            downloadDataForDay(ticker, dateToFetch);
        } else {
            System.out.println("Data for " + ticker + " is up-to-date.");
        }
    }


    /**
     * Downloads and processes one day's worth of data for a specific ticker.
     *
     * @param ticker The stock ticker symbol.
     * @param dateToFetch The LocalDate for the data to be fetched.
     */
    @Retry(name = "polygon", fallbackMethod = "downloadDataForDayFallback")
    @CircuitBreaker(name = "polygon", fallbackMethod = "downloadDataForDayFallback")
    private void downloadDataForDay(String ticker, LocalDate dateToFetch) {
        String dateStr = dateToFetch.format(FORMATTER);
        System.out.println("Fetching data for date: " + dateStr + " for ticker: " + ticker);

        try {
            String url = String.format(BASE_URL_TEMPLATE, ticker, dateStr, dateStr, apiKey);
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();

            if (responseCode != 200) {
                // Throw an exception for non-200 responses to trigger the retry/circuit breaker.
                throw new RuntimeException("API call failed with status code: " + responseCode);
            }

            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }

                // Deserialize the entire response into the new PolygonResponse class
                PolygonResponse polygonResponse = mapper.readValue(response.toString(), PolygonResponse.class);
                List<TickData> results = polygonResponse.getResults();

                if (results != null && !results.isEmpty()) {
                    for (TickData tickData : results) {
                        influxDBService.writeTickData(ticker,  tickData);
                    }
                    influxDBService.flushWriteApi();
                } else {
                    System.out.println("No data returned for " + ticker + " on " + dateStr);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to download data for " + ticker + " on " + dateStr + ": " + e.getMessage());
            throw new RuntimeException("Failed to download data", e);
        }
    }

    /**
     * Fallback method for downloadDataForDay.
     * This is called if the circuit breaker is open or all retries are exhausted.
     */
    private void downloadDataForDayFallback(String ticker, LocalDate dateToFetch, Throwable t) {
        System.err.println("Circuit breaker is OPEN or all retries failed for " + ticker + " on " + dateToFetch.format(FORMATTER));
        System.err.println("Reason: " + t.getMessage());
    }
}
