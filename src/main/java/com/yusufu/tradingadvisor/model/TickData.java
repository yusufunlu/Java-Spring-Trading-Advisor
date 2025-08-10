package com.yusufu.tradingadvisor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TickData {

    @JsonProperty("t")
    private long timestamp;

    @JsonProperty("o")
    private double open;

    @JsonProperty("h")
    private double high;

    @JsonProperty("l")
    private double low;

    @JsonProperty("c")
    private double close;

    @JsonProperty("v")
    private long volume;

    @JsonProperty("vw")
    private double vwap;

    @JsonProperty("n")
    private int transactions;

    public String toCsvString() {
        return String.format("%d,%.2f,%.2f,%.2f,%.2f,%d,%.2f,%d\n",
                timestamp, open, high, low, close, volume, vwap, transactions
        );
    }
}