package com.reactivecapstone.orderaggregator.controller;

import com.reactivecapstone.orderaggregator.model.AggregatedOrderInfo;
import com.reactivecapstone.orderaggregator.service.OrderAggregationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

// Implement REST API
@RestController
@RequestMapping("/api/orders")
public class OrderAggregationController {

    private final OrderAggregationService service;

    public OrderAggregationController(OrderAggregationService service) {
        this.service = service;
    }

    @GetMapping(value = "/{userId}", produces = MediaType.APPLICATION_NDJSON_VALUE) // Use NDJSON for streaming
    public Flux<AggregatedOrderInfo> getOrdersByUserId(@PathVariable String userId) {
        return service.getOrdersByUserId(userId);
    }
}
