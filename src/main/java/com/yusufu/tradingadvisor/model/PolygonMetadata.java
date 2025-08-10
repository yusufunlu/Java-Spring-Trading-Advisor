package com.yusufu.tradingadvisor.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "polygon_metadata")
@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PolygonMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ticker;
    private int queryCount;
    private int resultsCount;
    private boolean adjusted;
    private String status;
    private String requestId;
    private int count;

    @Column(length = 1024)
    private String nextUrl;
    private String urlTried;
    private LocalDate forDate;
    private Instant retrievedAt;
    @Column(length = 1024)
    private String errorMessage;
}