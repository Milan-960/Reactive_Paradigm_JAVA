package com.reactivecapstone.orderaggregator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductInfoResponse {
    private String productId;
    private String productCode;
    private String productName;
    private double score;
}