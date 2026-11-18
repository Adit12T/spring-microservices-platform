package io.github.adit12t.platform.currencyconversionservice;

import java.math.BigDecimal;

/** How the conversion service gets a rate. The real implementation calls currency-exchange-service. */
public interface ExchangeRateClient {

    Rate rate(String from, String to);

    record Rate(String from, String to, BigDecimal rate, String instance) {
    }

    /** The exchange service answered, but has no rate for this pair. */
    class RateNotFoundException extends RuntimeException {
        public RateNotFoundException(String message) {
            super(message);
        }
    }
}
