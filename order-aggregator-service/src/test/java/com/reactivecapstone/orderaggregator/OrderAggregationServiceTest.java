package com.reactivecapstone.orderaggregator;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.reactivecapstone.orderaggregator.model.AggregatedOrderInfo;
import com.reactivecapstone.orderaggregator.model.UserInfo;
import com.reactivecapstone.orderaggregator.repository.UserInfoRepository;
import com.reactivecapstone.orderaggregator.service.OrderAggregationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@WireMockTest(httpPort = 8080) // Starts WireMock on the same port as our external services
public class OrderAggregationServiceTest {

    @Autowired
    private OrderAggregationService service;

    @MockBean
    private UserInfoRepository userInfoRepository;

    @BeforeEach
    void setup() {
        // Reset WireMock before each test
        WireMock.reset();
    }

    @Test
    void getOrdersByUserId_success() {
        // Mock the database response
        UserInfo mockUser = new UserInfo();
        mockUser.setId("user1");
        mockUser.setName("Test User");
        mockUser.setPhone("111222333");
        when(userInfoRepository.findById("user1")).thenReturn(Mono.just(mockUser));

        // Stub the Order Search Service response
        stubFor(get(urlEqualTo("/orderSearchService/order/phone?phoneNumber=111222333"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_NDJSON_VALUE)
                        .withBody("{\"orderNumber\":\"Order_1\",\"productCode\":\"P1\"}\n" +
                                  "{\"orderNumber\":\"Order_2\",\"productCode\":\"P2\"}")));

        // Stub the Product Info Service response for product P1
        stubFor(get(urlEqualTo("/productInfoService/product/names?productCode=P1"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("[{\"productId\":\"Prod_A\",\"productName\":\"Product A\",\"score\":0.9}]")));

        // Stub the Product Info Service to be slow for product P2 (will cause a timeout)
        stubFor(get(urlEqualTo("/productInfoService/product/names?productCode=P2"))
                .willReturn(aResponse()
                        .withFixedDelay(6000) // 6 seconds, longer than our 5s timeout
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("[]")));


        // --- 2. ACT ---
        Flux<AggregatedOrderInfo> resultFlux = service.getOrdersByUserId("user1");


        // --- 3. ASSERT ---
        StepVerifier.create(resultFlux)
                // Expect the first order to be fully enriched
                .expectNextMatches(order ->
                        order.getOrderNumber().equals("Order_1") &&
                        order.getProductName().equals("Product A")
                )
                // Expect the second order to have null product info due to timeout
                .expectNextMatches(order ->
                        order.getOrderNumber().equals("Order_2") &&
                        order.getProductName() == null
                )
                .verifyComplete();
    }

    @Test
    void getOrdersByUserId_userNotFound() {
        // Mock the database to return nothing
        when(userInfoRepository.findById(anyString())).thenReturn(Mono.empty());

        Flux<AggregatedOrderInfo> resultFlux = service.getOrdersByUserId("unknownUser");

        StepVerifier.create(resultFlux)
                .expectNextCount(0)
                .verifyComplete();
    }
}
