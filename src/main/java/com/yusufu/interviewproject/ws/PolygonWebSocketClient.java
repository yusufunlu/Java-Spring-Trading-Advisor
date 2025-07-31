package com.yusufu.interviewproject.ws;

import jakarta.annotation.PostConstruct;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class PolygonWebSocketClient {

    private static final String API_KEY = "API_KEY";
    private static final String WS_URL = "wss://socket.polygon.io/stocks";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build();

    private WebSocket webSocket;

    @PostConstruct
    public void start() {
        Request request = new Request.Builder()
                .url(WS_URL)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                System.out.println("✅ Connected to Polygon WebSocket");

                // Authenticate
                webSocket.send("{\"action\":\"auth\",\"params\":\"" + API_KEY + "\"}");

                // Subscribe to AAPL tick data
                webSocket.send("{\"action\":\"subscribe\",\"params\":\"T.AAPL\"}");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                System.out.println("📈 Tick: " + text);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                System.out.println("🔁 Closing WebSocket: " + code + " / " + reason);
                webSocket.close(1000, null);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                System.out.println("🔒 WebSocket closed: " + code + " / " + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                System.err.println("❌ WebSocket failure: " + t.getMessage());
            }
        });

        // NOTE: Don't shut down dispatcher now — keep receiving messages
    }
}
