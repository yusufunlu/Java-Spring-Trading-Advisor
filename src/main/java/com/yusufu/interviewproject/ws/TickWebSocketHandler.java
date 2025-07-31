package com.yusufu.interviewproject.ws;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TickWebSocketHandler extends TextWebSocketHandler {

    // sessionId -> Pair(session, lastSeen)
    private final Map<String, Pair<WebSocketSession, Instant>> sessionMap = new ConcurrentHashMap<>();

    private static final long TIMEOUT_MILLIS = 30*1000;

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

    public void broadcastTick(String tickJson) {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, Pair<WebSocketSession, Instant>>> iterator = sessionMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Pair<WebSocketSession, Instant>> entry = iterator.next();
            String sessionId = entry.getKey();
            WebSocketSession session = entry.getValue().getLeft();
            Instant lastSeen = entry.getValue().getRight();

            boolean timedOut = now.toEpochMilli() - lastSeen.toEpochMilli() > TIMEOUT_MILLIS;
            boolean closed = !session.isOpen();

            if (timedOut || closed) {
                try {
                    session.close(CloseStatus.SESSION_NOT_RELIABLE);
                } catch (IOException ignored) {}
                iterator.remove();
                continue;
            }

            try {
                session.sendMessage(new TextMessage(tickJson));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Scheduled(fixedRate = 10*1000)
    public void cleanUpStaleSessions() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, Pair<WebSocketSession, Instant>>> iterator = sessionMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Pair<WebSocketSession, Instant>> entry = iterator.next();
            WebSocketSession session = entry.getValue().getLeft();
            Instant lastSeen = entry.getValue().getRight();

            if (now.toEpochMilli() - lastSeen.toEpochMilli() > TIMEOUT_MILLIS) {
                try {
                    if (session.isOpen()) session.close(CloseStatus.SESSION_NOT_RELIABLE);
                } catch (IOException ignored) {}
                iterator.remove();
            }
        }
    }
}
