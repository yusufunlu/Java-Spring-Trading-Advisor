package com.yusufu.javaspringfeatures.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.influxdb.query.FluxRecord;
import com.yusufu.javaspringfeatures.model.TickData;
import com.yusufu.javaspringfeatures.ws.InfluxDBService;
import com.yusufu.javaspringfeatures.ws.TickWebSocketHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

@Service
public class PolygonDataCollector {

    @Value("${external.polygon.api_key}")
    private String apiKey;

    private final TickWebSocketHandler tickWebSocketHandler;
    private final InfluxDBService influxDBService;

    @Autowired
    private ObjectMapper mapper;

    public PolygonDataCollector(TickWebSocketHandler tickWebSocketHandler, InfluxDBService influxDBService) {
        this.tickWebSocketHandler = tickWebSocketHandler;
        this.influxDBService = influxDBService;
    }

    private static final String BASE_URL_TEMPLATE =
            "https://api.polygon.io/v2/aggs/ticker/%s/range/1/minute/%s/%s?adjusted=true&sort=asc&limit=50000&apiKey=%s";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @PostConstruct
    public void init() {
        downloadTickerData(List.of("AAPL", "BTC-USD"));
    }

    @Scheduled(fixedRate = 60000)
    public void refreshData() {
        downloadTickerData(List.of("AAPL", "BTC-USD"));
    }

    public void downloadTickerData(List<String> tickers) {
        for (String ticker : tickers) {
            String dateStr = null;
            try {
                List<FluxRecord> records = influxDBService.getLatestRecordForTicker(ticker);

                Instant latestTimestamp = null;
                if (!records.isEmpty()) {
                    latestTimestamp = records.get(0).getTime();
                }

                LocalDate dateToFetch;
                if (latestTimestamp == null) {
                    dateToFetch = LocalDate.now().minusDays(1);
                } else {
                    dateToFetch = latestTimestamp.atZone(java.time.ZoneId.of("UTC")).toLocalDate().minusDays(1);
                }

                dateStr = dateToFetch.format(FORMATTER);
                System.out.println("Fetching data for date: " + dateStr + " for ticker: " + ticker);

                String url = String.format(BASE_URL_TEMPLATE, ticker, dateStr, dateStr, apiKey);
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");

                Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8);
                StringBuilder response = new StringBuilder();

                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }

                scanner.close();


                JsonNode root = mapper.readTree(response.toString());
                JsonNode results = root.path("results");

                if (results.isArray()) {
                    Path file = Paths.get("data", ticker, dateStr + ".csv");
                    Files.createDirectories(file.getParent());
                    try (FileWriter writer = new FileWriter(file.toFile())) {
                        writer.write("timestamp,open,high,low,close,volume,vwap,transactions\n");
                        for (JsonNode candle : results) {
                            TickData tickData = TickData.builder()
                                    .timestamp(candle.path("t").asLong())
                                    .open(candle.path("o").asDouble())
                                    .high(candle.path("h").asDouble())
                                    .low(candle.path("l").asDouble())
                                    .close(candle.path("c").asDouble())
                                    .volume(candle.path("v").asLong())
                                    .vwap(candle.path("vw").asDouble())
                                    .transactions(candle.path("n").asInt())
                                    .build();

                            influxDBService.writeTickData(ticker, tickData);
                            writer.write(tickData.toCsvString());
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to download data for " + ticker + " on " + dateStr + ": " + e.getMessage());
            }
        }
    }
}
