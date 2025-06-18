package com.reactivecapstone.orderaggregator.service;

import com.reactivecapstone.orderaggregator.model.*;
import com.reactivecapstone.orderaggregator.repository.UserInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class OrderAggregationService {

    private final UserInfoRepository userInfoRepository;
    private final WebClient orderSearchWebClient;
    private final WebClient productInfoWebClient;

    public OrderAggregationService(UserInfoRepository userInfoRepository,
                                   @Qualifier("orderSearchWebClient") WebClient orderSearchWebClient,
                                   @Qualifier("productInfoWebClient") WebClient productInfoWebClient) {
        this.userInfoRepository = userInfoRepository;
        this.orderSearchWebClient = orderSearchWebClient;
        this.productInfoWebClient = productInfoWebClient;
    }

    public Flux<AggregatedOrderInfo> getOrdersByUserId(String userId) {
        return userInfoRepository.findById(userId)
                .doOnNext(user -> log.info("Found user: {}", user.getName()))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("User not found for userId: {}", userId);
                    return Mono.empty();
                }))
                .flatMapMany(this::fetchOrdersForUser);
    }

    private Flux<AggregatedOrderInfo> fetchOrdersForUser(UserInfo user) {
        return orderSearchWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/orderSearchService/order/phone")
                        .queryParam("phoneNumber", user.getPhone())
                        .build())
                .retrieve()
                .bodyToFlux(OrderSearchResponse.class)
                .doOnNext(order -> log.info("Received order: {} for product code: {}", order.getOrderNumber(), order.getProductCode()))
                .flatMap(order -> enrichOrderWithProductInfo(order, user))
                .onErrorResume(e -> {
                    log.error("Failed to fetch orders for phone {}: {}", user.getPhone(), e.getMessage());
                    return Flux.empty();
                });
    }

    private Mono<AggregatedOrderInfo> enrichOrderWithProductInfo(OrderSearchResponse order, UserInfo user) {
        return fetchProductInfo(order.getProductCode())
                .map(bestProduct -> new AggregatedOrderInfo(
                        order.getOrderNumber(),
                        user.getName(),
                        user.getPhone(),
                        order.getProductCode(),
                        bestProduct.getProductName(),
                        bestProduct.getProductId()
                ))
                .defaultIfEmpty(new AggregatedOrderInfo(
                        order.getOrderNumber(),
                        user.getName(),
                        user.getPhone(),
                        order.getProductCode(),
                        null,
                        null
                ));
    }

    private Mono<ProductInfoResponse> fetchProductInfo(String productCode) {
        return productInfoWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/productInfoService/product/names")
                        .queryParam("productCode", productCode)
                        .build())
                .retrieve()
                .bodyToFlux(ProductInfoResponse.class)
                .collectList()
                .timeout(Duration.ofSeconds(5))
                .doOnNext(products -> log.info("Received {} product candidates for code: {}", products.size(), productCode))
                .flatMap(this::findBestProduct)
                .doOnError(e -> log.error("Error fetching product info for code: {}. Reason: {}", productCode, e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    private Mono<ProductInfoResponse> findBestProduct(List<ProductInfoResponse> products) {
        return Mono.justOrEmpty(products.stream().max(Comparator.comparing(ProductInfoResponse::getScore)));
    }
}
