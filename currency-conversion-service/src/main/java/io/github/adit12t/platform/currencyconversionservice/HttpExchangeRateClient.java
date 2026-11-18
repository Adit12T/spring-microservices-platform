package io.github.adit12t.platform.currencyconversionservice;

import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/**
 * Calls currency-exchange-service by its registry name. The load balancer spreads calls across
 * instances; the circuit breaker stops calling a failing service and fails fast instead.
 */
@Component
public class HttpExchangeRateClient implements ExchangeRateClient {

    private final RestClient rest;
    private final CircuitBreakerFactory<?, ?> circuitBreakers;

    public HttpExchangeRateClient(RestClient.Builder loadBalancedBuilder, CircuitBreakerFactory<?, ?> circuitBreakers) {
        this.rest = loadBalancedBuilder.baseUrl("http://currency-exchange-service").build();
        this.circuitBreakers = circuitBreakers;
    }

    @Override
    public Rate rate(String from, String to) {
        return circuitBreakers.create("exchange-service").run(
                () -> {
                    try {
                        return rest.get().uri("/api/v1/exchange-rates/{from}/{to}", from, to).retrieve().body(Rate.class);
                    } catch (HttpClientErrorException.NotFound e) {
                        throw new RateNotFoundException("No exchange rate from " + from + " to " + to);
                    }
                },
                failure -> {
                    if (failure instanceof RateNotFoundException notFound) {
                        throw notFound;
                    }
                    throw new ExchangeServiceUnavailableException(failure);
                });
    }

    public static class ExchangeServiceUnavailableException extends RuntimeException {
        public ExchangeServiceUnavailableException(Throwable cause) {
            super("The exchange-rate service is unavailable. Try again shortly.", cause);
        }

        public HttpStatus status() {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
    }

    @Configuration
    static class RestClientConfig {

        @Bean
        @LoadBalanced
        RestClient.Builder loadBalancedRestClientBuilder() {
            return RestClient.builder();
        }
    }
}
