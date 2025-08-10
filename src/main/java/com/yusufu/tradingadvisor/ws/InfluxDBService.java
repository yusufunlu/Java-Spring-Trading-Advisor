package com.yusufu.tradingadvisor.ws;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.WriteOptions;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.client.write.events.WriteErrorEvent;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.yusufu.tradingadvisor.PolygonDataException;
import com.yusufu.tradingadvisor.model.TickData;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class InfluxDBService {

    private static final Logger log = LoggerFactory.getLogger(InfluxDBService.class);
    private final String url;
    private final String token;
    private final String org;
    private final String bucket;
    private final InfluxDBClient influxDBClient;
    private final WriteApi writeApi;

    private final AtomicReference<Throwable> lastWriteError = new AtomicReference<>();
    public InfluxDBService(@Value("${influxdb.url}") String url,
                           @Value("${influxdb.token}") String token,
                           @Value("${influxdb.org}") String org,
                           @Value("${influxdb.bucket}") String bucket) {
        this.url = url;
        this.token = token;
        this.org = org;
        this.bucket = bucket;
        this.influxDBClient = InfluxDBClientFactory.create(this.url, this.token.toCharArray(), this.org);
        // Initialize the WriteApi once as a singleton
        this.writeApi = influxDBClient.getWriteApi(
                WriteOptions.builder()
                        .batchSize(500)
                        .flushInterval(1000)
                        .build()
        );
        writeApi.listenEvents(WriteErrorEvent.class, event -> {
            //log.warn("InfluxDB write error: {}", event.getThrowable().getMessage());
            lastWriteError.set(event.getThrowable());
        });
    }

    public void checkAndThrowIfError() {
        Throwable t = lastWriteError.getAndSet(null);
        if (t != null) {
            //TODO: Global exception handler is not able to catch this, fix it
            throw new PolygonDataException("Influx write failed: " + t.getMessage());
        }
    }

    @PreDestroy
    public void closeClient() {
        if (writeApi != null) {
            writeApi.close();
        }
        if (influxDBClient != null) {
            influxDBClient.close();
        }
    }

    public void writeTickData(String ticker, TickData tickData) {
        try {
            Point point = Point.measurement("stock_ticks")
                    .addTag("ticker", ticker)
                    .addTag("source", "polygon.io")
                    .time(Instant.ofEpochMilli(tickData.getTimestamp()), WritePrecision.MS)
                    .addField("open", tickData.getOpen())
                    .addField("high", tickData.getHigh())
                    .addField("low", tickData.getLow())
                    .addField("close", tickData.getClose())
                    .addField("volume", tickData.getVolume())
                    .addField("vwap", tickData.getVwap())
                    .addField("transactions", tickData.getTransactions());

            // Class-level writeApi kullan
            writeApi.writePoint(bucket, org, point);
            //System.out.println("Successfully wrote tick data for " + ticker);
        } catch (Exception e) {
            System.err.println("Failed to write to InfluxDB: " + e.getMessage());
        }
    }

    public void flushWriteApi() {
        writeApi.flush();
    }

    /**
     * Queries InfluxDB to get the latest timestamp for a specific ticker.
     * @param ticker The ticker symbol to query.
     * @return The latest Instant for the ticker, or an empty list if no data is found.
     */
    public List<FluxRecord> getLatestRecordForTicker(String ticker) {
        String fluxQuery = String.format(
                "from(bucket:\"%s\")\n" +
                        "  |> range(start: 0)\n" +
                        "  |> filter(fn: (r) => r[\"_measurement\"] == \"stock_ticks\")\n" +
                        "  |> filter(fn: (r) => r[\"ticker\"] == \"%s\")\n" +
                        "  |> last()", bucket, ticker);

        List<FluxTable> tables = influxDBClient.getQueryApi().query(fluxQuery);
        if (tables != null && !tables.isEmpty()) {
            return tables.get(0).getRecords();
        }
        return List.of();
    }

    public List<FluxRecord> getHistoricalDataForDate(String ticker, LocalDate date) {
        Instant startOfDay = date.atStartOfDay(ZoneId.of("UTC")).toInstant();
        Instant endOfDay = startOfDay.plus(1, ChronoUnit.DAYS);

        String fluxQuery = String.format(
                "from(bucket:\"%s\")\n" +
                        "  |> range(start: %s, stop: %s)\n" +
                        "  |> filter(fn: (r) => r[\"_measurement\"] == \"stock_ticks\")\n" +
                        "  |> filter(fn: (r) => r[\"ticker\"] == \"%s\")", bucket, startOfDay.toString(), endOfDay.toString(), ticker);

        List<FluxTable> tables = influxDBClient.getQueryApi().query(fluxQuery);
        if (tables != null && !tables.isEmpty()) {
            return tables.get(0).getRecords();
        }
        return List.of();
    }
}
