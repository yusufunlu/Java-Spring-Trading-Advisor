package com.yusufu.tradingadvisor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PolygonResponse {

    @JsonProperty("ticker")
    private String ticker;

    @JsonProperty("queryCount")
    private int queryCount;

    @JsonProperty("resultsCount")
    private int resultsCount;

    @JsonProperty("adjusted")
    private boolean adjusted;

    @JsonProperty("results")
    private List<TickData> results;

    @JsonProperty("status")
    private String status;

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("count")
    private int count;

    @JsonProperty("next_url")
    private String nextUrl;
}