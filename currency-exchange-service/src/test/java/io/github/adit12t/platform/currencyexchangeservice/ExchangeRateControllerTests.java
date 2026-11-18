package io.github.adit12t.platform.currencyexchangeservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Runs the Flyway migrations on an in-memory database in PostgreSQL mode. */
@SpringBootTest(properties = {"eureka.client.enabled=false", "spring.cloud.config.enabled=false", "management.tracing.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:exchange;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa", "spring.datasource.password="})
@AutoConfigureMockMvc
class ExchangeRateControllerTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void returnsASeededRateCaseInsensitively() throws Exception {
        mvc.perform(get("/api/v1/exchange-rates/usd/inr"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("USD"))
                .andExpect(jsonPath("$.rate").value(83.25))
                .andExpect(jsonPath("$.instance").exists());
    }

    @Test
    void unknownPairsAndBadCodesAreRejected() throws Exception {
        mvc.perform(get("/api/v1/exchange-rates/USD/XYZ")).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/exchange-rates/US/INR")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/exchange-rates/USD1/INR")).andExpect(status().isBadRequest());
    }

    @Test
    void createsAndUpdatesRates() throws Exception {
        mvc.perform(put("/api/v1/exchange-rates/CAD/INR").contentType(MediaType.APPLICATION_JSON).content("{\"rate\":61.5}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rate").value(61.5));
        mvc.perform(put("/api/v1/exchange-rates/CAD/INR").contentType(MediaType.APPLICATION_JSON).content("{\"rate\":62}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/exchange-rates/CAD/INR")).andExpect(jsonPath("$.rate").value(62.0));
        mvc.perform(put("/api/v1/exchange-rates/CAD/INR").contentType(MediaType.APPLICATION_JSON).content("{\"rate\":-1}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/exchange-rates/INR/INR").contentType(MediaType.APPLICATION_JSON).content("{\"rate\":1}"))
                .andExpect(status().isBadRequest());
    }
}
