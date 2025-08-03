package com.yusufu.javaspringfeatures.repo;

import com.yusufu.javaspringfeatures.model.PolygonMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PolygonMetadataRepository extends JpaRepository<PolygonMetadata, Long> {
    Optional<PolygonMetadata> findTopByTickerOrderByForDateDesc(String ticker);
}