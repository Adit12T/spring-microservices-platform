package io.github.adit12t.platform.apigateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

/**
 * The only public entry point. lb:// URIs resolve service names through the registry and
 * spread calls across instances. Exchange rates are read-only from outside: PUT is not routed.
 */
@Configuration
public class GatewayRoutes {

    @Bean
    RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("exchange-rates", r -> r.path("/api/v1/exchange-rates/**").and().method(HttpMethod.GET)
                        .uri("lb://currency-exchange-service"))
                .route("conversions", r -> r.path("/api/v1/conversions/**")
                        .filters(f -> f
                                .circuitBreaker(cb -> cb.setName("conversion").setFallbackUri("forward:/fallback/conversion"))
                                .retry(retry -> retry.setRetries(2).setMethods(HttpMethod.GET)))
                        .uri("lb://currency-conversion-service"))
                .route("limits", r -> r.path("/api/v1/limits").and().method(HttpMethod.GET)
                        .uri("lb://limits-service"))
                .route("users", r -> r.path("/api/v1/users", "/api/v1/users/**")
                        .uri("lb://user-service"))
                .build();
    }
}
