package io.github.adit12t.platform.currencyexchangeservice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@RestController
@Validated
@RequestMapping("/api/v1/exchange-rates")
public class ExchangeRateController {

    private static final String CURRENCY = "^[A-Za-z]{3}$";

    private final ExchangeRateRepository repository;
    private final String instanceId;

    public ExchangeRateController(ExchangeRateRepository repository,
                                  @Value("${eureka.instance.instance-id:${spring.application.name}:${random.value}}")
                                  String instanceId) {
        this.repository = repository;
        this.instanceId = instanceId;
    }

    @GetMapping
    public List<RateResponse> all() {
        return repository.findAllByOrderByFromAscToAsc().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{from}/{to}")
    public RateResponse rate(@PathVariable @Pattern(regexp = CURRENCY) String from,
                             @PathVariable @Pattern(regexp = CURRENCY) String to) {
        return repository.findByFromAndTo(upper(from), upper(to))
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No exchange rate from " + upper(from) + " to " + upper(to)));
    }

    /** Internal: the gateway does not route PUT, so only services inside the network can change rates. */
    @PutMapping("/{from}/{to}")
    @Transactional
    public RateResponse upsert(@PathVariable @Pattern(regexp = CURRENCY) String from,
                               @PathVariable @Pattern(regexp = CURRENCY) String to,
                               @Valid @RequestBody RateUpdate update) {
        if (upper(from).equals(upper(to))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from and to must differ");
        }
        ExchangeRate rate = repository.findByFromAndTo(upper(from), upper(to))
                .orElseGet(() -> new ExchangeRate(upper(from), upper(to), update.rate()));
        rate.changeRate(update.rate());
        return toResponse(repository.save(rate));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ProblemDetail invalid() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Currencies must be three-letter ISO 4217 codes.");
    }

    private RateResponse toResponse(ExchangeRate rate) {
        return new RateResponse(rate.getFrom(), rate.getTo(), rate.getRate(), rate.getUpdatedAt(), instanceId);
    }

    private static String upper(String code) {
        return code.toUpperCase(Locale.ROOT);
    }

    public record RateUpdate(@NotNull @DecimalMin(value = "0.000001") @Digits(integer = 13, fraction = 6) BigDecimal rate) {
    }

    /** {@code instance} shows which copy of the service answered, to make load balancing visible. */
    public record RateResponse(String from, String to, BigDecimal rate, Instant updatedAt, String instance) {
    }
}
