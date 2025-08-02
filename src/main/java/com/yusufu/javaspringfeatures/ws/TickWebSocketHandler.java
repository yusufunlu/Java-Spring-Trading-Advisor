package com.yusufu.javaspringfeatures.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.influxdb.query.FluxRecord;
import com.yusufu.javaspringfeatures.ws.InfluxDBService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TickWebSocketHandler extends TextWebSocketHandler {

    private final InfluxDBService influxDBService;

    // sessionId -> Pair(session, lastSeen)
    private final Map<String, Pair<WebSocketSession, Instant>> sessionMap = new ConcurrentHashMap<>();

    // Maintain state for the scheduled data feed
    private LocalDate backfillDate = LocalDate.now().minusDays(1);
    @Autowired
    private ObjectMapper mapper;

    private static final long TIMEOUT_MILLIS = 30 * 1000;
    private static final List<String> TICKERS_TO_BROADCAST = List.of("AAPL", "BTC-USD");

    @Autowired
    public TickWebSocketHandler(InfluxDBService influxDBService) {
        this.influxDBService = influxDBService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionMap.put(session.getId(), Pair.of(session, Instant.now()));
        System.out.println("Connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();

        if ("ping".equalsIgnoreCase(payload)) {
            sessionMap.computeIfPresent(session.getId(), (id, pair) ->
                    Pair.of(pair.getLeft(), Instant.now()));
            session.sendMessage(new TextMessage("pong"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionMap.remove(session.getId());
        System.out.println("Disconnected: " + session.getId());
    }

    @Scheduled(fixedRate = 10 * 1000) // Check every 10 sec if any session died
    public void cleanUpStaleSessions() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, Pair<WebSocketSession, Instant>>> iterator = sessionMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Pair<WebSocketSession, Instant>> entry = iterator.next();
            WebSocketSession session = entry.getValue().getLeft();
            Instant lastSeen = entry.getValue().getRight();
            // Close session if last message from client's session stale more than 30 sec
            if (now.toEpochMilli() - lastSeen.toEpochMilli() > TIMEOUT_MILLIS) {
                try {
                    if (session.isOpen()) session.close(CloseStatus.SESSION_NOT_RELIABLE);
                } catch (IOException ignored) {}
                iterator.remove();
            }
        }
    }

    @Scheduled(fixedRate = 60000) // Run every minute to fetch new data
    public void broadcastHistoricalData() {
        System.out.println("Broadcasting data for: " + backfillDate);
        for (String ticker : TICKERS_TO_BROADCAST) {
            List<FluxRecord> records = influxDBService.getHistoricalDataForDate(ticker, backfillDate);

            // Broadcast all records for the day
            for (FluxRecord record : records) {
                try {
                    String tickJson = mapper.writeValueAsString(record.getValues());
                    broadcastMessage(tickJson);
                    Thread.sleep(100);
                } catch (Exception e) {
                    System.err.println("Failed to broadcast record: " + e.getMessage());
                }
            }
        }
        // After broadcasting a day data move to the next day backward
        backfillDate = backfillDate.minusDays(1);
    }

    private void broadcastMessage(String message) {
        sessionMap.values().stream()
                .map(Pair::getLeft)
                .filter(WebSocketSession::isOpen)
                .forEach(session -> {
                    try {
                        session.sendMessage(new TextMessage(message));
                    } catch (IOException e) {
                        System.err.println("Failed to send message to session " + session.getId() + ": " + e.getMessage());
                    }
                });
    }
}
