package com.yusufu.interviewproject.ws;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class TickSimulatorService {

    private final TickWebSocketHandler webSocketHandler;
    private final Random random = new Random();

    public TickSimulatorService(TickWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Scheduled(fixedRate = 1000)
    public void generateAndBroadcastTick() {
        // Example tick data as JSON string
        String tickJson = String.format("{\"symbol\":\"AAPL\", \"price\":%.2f, \"volume\":%d}",
                100 + random.nextDouble() * 20,
                random.nextInt(1000));

        webSocketHandler.broadcastTick(tickJson);
    }
}
