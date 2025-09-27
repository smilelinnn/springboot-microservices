package com.example.product.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rating {

    @JsonProperty("rate")
    private Double rate;

    @JsonProperty("count")
    private Integer count;
}
