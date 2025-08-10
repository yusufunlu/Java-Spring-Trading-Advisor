package com.yusufu.tradingadvisor.model.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA needs this
@AllArgsConstructor
@Builder
public class ProductDto {
    private String id;
    private String name;
    private Double price;
    private String categoryId;
    private String categoryName;
}