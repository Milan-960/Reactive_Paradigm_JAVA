package com.reactivecapstone.orderaggregator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) 
public class OrderSearchResponse {
    private String orderNumber;
    private String productCode;
}
