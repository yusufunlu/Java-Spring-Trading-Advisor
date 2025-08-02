package com.yusufu.javaspringfeatures.ws;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.yusufu.javaspringfeatures.model.TickData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class InfluxDBService {

    private final String url;
    private final String token;
    private final String org;
    private final String bucket;
    private final InfluxDBClient influxDBClient;

    public InfluxDBService(@Value("${influxdb.url}") String url,
                           @Value("${influxdb.token}") String token,
                           @Value("${influxdb.org}") String org,
                           @Value("${influxdb.bucket}") String bucket) {
        this.url = url;
        this.token = token;
        this.org = org;
        this.bucket = bucket;
        this.influxDBClient = InfluxDBClientFactory.create(this.url, this.token.toCharArray(), this.org);
    }

    public void writeTickData(String ticker, TickData tickData) {
        try (WriteApi writeApi = influxDBClient.getWriteApi()) {
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

            writeApi.writePoint(bucket, org, point);
            System.out.println("Successfully wrote tick data for " + ticker);
        } catch (Exception e) {
            System.err.println("Failed to write to InfluxDB: " + e.getMessage());
        }
    }

    /**
     * Queries InfluxDB to get the latest timestamp for a specific ticker.
     * @param ticker The ticker symbol to query.
     * @return The latest Instant for the ticker, or null if no data is found.
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
