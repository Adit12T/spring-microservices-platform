package io.github.adit12t.platform.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"eureka.client.enabled=false", "management.tracing.enabled=false"})
class ApiGatewayApplicationTests {

    private WebTestClient client;

    @Autowired
    void bindClient(@org.springframework.beans.factory.annotation.Value("${local.server.port}") int port) {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port)
                .responseTimeout(java.time.Duration.ofSeconds(15)).build();
    }

    @Autowired
    private RouteLocator routes;

    @Test
    void definesAllFourRoutes() {
        assertThat(routes.getRoutes().map(r -> r.getId()).collectList().block())
                .containsExactlyInAnyOrder("exchange-rates", "conversions", "limits", "users");
    }

    @Test
    void conversionFallbackIsAProblemDetailWithRetryAfter() {
        client.get().uri("/fallback/conversion").exchange()
                .expectStatus().isEqualTo(503)
                .expectHeader().valueEquals("Retry-After", "10")
                .expectBody().jsonPath("$.detail").value(d -> assertThat((String) d).contains("temporarily unavailable"));
    }

    @Test
    void conversionRouteFallsBackWhenNoInstanceIsRegistered() {
        client.get().uri("/api/v1/conversions?from=USD&to=INR&amount=1").exchange()
                .expectStatus().isEqualTo(503)
                .expectHeader().exists(CorrelationIdFilter.HEADER);
    }

    @Test
    void keepsAWellFormedCorrelationIdAndReplacesAMalformedOne() {
        // The filter applies to routed requests, so use a real route (it falls back with no instances).
        client.get().uri("/api/v1/conversions?from=USD&to=INR&amount=1")
                .header(CorrelationIdFilter.HEADER, "abc12345-trace").exchange()
                .expectHeader().valueEquals(CorrelationIdFilter.HEADER, "abc12345-trace");
        client.get().uri("/api/v1/conversions?from=USD&to=INR&amount=1")
                .header(CorrelationIdFilter.HEADER, "<script>").exchange()
                .expectHeader().value(CorrelationIdFilter.HEADER, v -> assertThat(v).doesNotContain("<"));
    }

    @Test
    void doesNotRoutePutToExchangeRates() {
        client.put().uri("/api/v1/exchange-rates/USD/INR").bodyValue("{\"rate\":1}").exchange()
                .expectStatus().isNotFound();
    }
}
