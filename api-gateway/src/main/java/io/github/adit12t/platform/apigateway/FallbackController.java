package io.github.adit12t.platform.apigateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** What callers get when the conversion route's circuit breaker is open or the call fails. */
@RestController
public class FallbackController {

    @RequestMapping("/fallback/conversion")
    ResponseEntity<ProblemDetail> conversionUnavailable() {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Currency conversion is temporarily unavailable. Please try again shortly.");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).header("Retry-After", "10").body(problem);
    }
}
