package com.reactivecapstone.orderaggregator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

// Create integrations (part 1)
@Configuration
public class WebClientConfig {

    @Value("${services.order-search.url}")
    private String orderSearchUrl;

    @Value("${services.product-info.url}")
    private String productInfoUrl;

    @Bean
    public WebClient orderSearchWebClient() {
        return WebClient.builder().baseUrl(orderSearchUrl).build();
    }

    @Bean
    public WebClient productInfoWebClient() {
        return WebClient.builder().baseUrl(productInfoUrl).build();
    }
}
