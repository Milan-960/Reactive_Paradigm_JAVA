
package com.reactivecapstone.orderaggregator.config;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import java.util.UUID;

// This filter intercepts every request to handle the requestId for logging
@Component
public class LoggingFilter implements WebFilter {

    private static final String REQUEST_ID_HEADER = "requestId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestId = exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        
        // Add the requestId to the reactive context
        return chain.filter(exchange)
                .contextWrite(Context.of(REQUEST_ID_HEADER, requestId));
    }
}
