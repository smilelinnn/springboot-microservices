package com.example.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatsDTO {

    private Long totalProducts;
    private List<String> categories;
    private Map<String, Long> productsByCategory;
    private Double averagePrice;
    private Double minPrice;
    private Double maxPrice;
}
