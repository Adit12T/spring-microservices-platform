package io.github.adit12t.platform.currencyconversionservice;

import io.github.adit12t.platform.currencyconversionservice.ExchangeRateClient.RateNotFoundException;
import io.github.adit12t.platform.currencyconversionservice.HttpExchangeRateClient.ExchangeServiceUnavailableException;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

@RestController
@Validated
@RequestMapping("/api/v1/conversions")
public class ConversionController {

    private static final String CURRENCY = "^[A-Za-z]{3}$";

    private final ExchangeRateClient rates;
    private final ConversionProperties properties;

    public ConversionController(ExchangeRateClient rates, ConversionProperties properties) {
        this.rates = rates;
        this.properties = properties;
    }

    /** Money is BigDecimal throughout; the result is rounded to 2 places with banker's rounding. */
    @GetMapping
    public Conversion convert(@RequestParam @Pattern(regexp = CURRENCY) String from,
                              @RequestParam @Pattern(regexp = CURRENCY) String to,
                              @RequestParam @DecimalMin(value = "0.01") @Digits(integer = 15, fraction = 2) BigDecimal amount) {
        if (amount.compareTo(properties.maxAmount()) > 0) {
            throw new IllegalArgumentException("amount exceeds the maximum of " + properties.maxAmount());
        }
        var rate = rates.rate(from.toUpperCase(Locale.ROOT), to.toUpperCase(Locale.ROOT));
        BigDecimal converted = amount.multiply(rate.rate()).setScale(2, RoundingMode.HALF_EVEN);
        return new Conversion(rate.from(), rate.to(), amount, rate.rate(), converted, rate.instance());
    }

    @ExceptionHandler(RateNotFoundException.class)
    ProblemDetail notFound(RateNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ExchangeServiceUnavailableException.class)
    ProblemDetail unavailable(ExchangeServiceUnavailableException e) {
        return ProblemDetail.forStatusAndDetail(e.status(), e.getMessage());
    }

    @ExceptionHandler({HandlerMethodValidationException.class, IllegalArgumentException.class})
    ProblemDetail invalid(Exception e) {
        String detail = e instanceof IllegalArgumentException ? e.getMessage()
                : "Use three-letter currency codes and a positive amount with at most 2 decimals.";
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }

    public record Conversion(String from, String to, BigDecimal amount, BigDecimal rate, BigDecimal converted,
                             String rateServedBy) {
    }
}
