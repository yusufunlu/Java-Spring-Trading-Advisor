package com.yusufu.javaspringfeatures.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TickData {
    private long timestamp;
    private double open;
    private double high;
    private double low;
    private double close;
    private long volume;
    private double vwap;
    private int transactions;

    public String toCsvString() {
        return String.format("%d,%.2f,%.2f,%.2f,%.2f,%d,%.2f,%d\n",
                timestamp, open, high, low, close, volume, vwap, transactions
        );
    }
}