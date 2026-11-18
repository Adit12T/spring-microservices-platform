package io.github.adit12t.platform.limitsservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LimitsController {

    private final LimitsProperties limits;

    public LimitsController(LimitsProperties limits) {
        this.limits = limits;
    }

    @GetMapping("/api/v1/limits")
    public Limits limits() {
        return new Limits(limits.getMinimum(), limits.getMaximum(), limits.getCurrency());
    }

    public record Limits(int minimum, int maximum, String currency) {
    }
}
