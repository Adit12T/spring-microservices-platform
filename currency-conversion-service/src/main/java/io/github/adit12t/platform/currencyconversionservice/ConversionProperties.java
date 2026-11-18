package io.github.adit12t.platform.currencyconversionservice;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties("conversion")
public record ConversionProperties(BigDecimal maxAmount) {

    public ConversionProperties {
        maxAmount = maxAmount == null ? new BigDecimal("1000000") : maxAmount;
    }
}
