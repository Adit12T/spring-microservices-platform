package io.github.adit12t.platform.currencyconversionservice;

import io.github.adit12t.platform.currencyconversionservice.ExchangeRateClient.Rate;
import io.github.adit12t.platform.currencyconversionservice.ExchangeRateClient.RateNotFoundException;
import io.github.adit12t.platform.currencyconversionservice.HttpExchangeRateClient.ExchangeServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"eureka.client.enabled=false", "spring.cloud.config.enabled=false", "management.tracing.enabled=false", "conversion.max-amount=5000"})
@AutoConfigureMockMvc
class ConversionControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ExchangeRateClient rates;

    @Test
    void convertsWithBankersRounding() throws Exception {
        when(rates.rate("USD", "INR")).thenReturn(new Rate("USD", "INR", new BigDecimal("83.255"), "exchange:1"));

        mvc.perform(get("/api/v1/conversions").param("from", "usd").param("to", "inr").param("amount", "10.50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.converted").value(874.18))
                .andExpect(jsonPath("$.rateServedBy").value("exchange:1"));
    }

    @Test
    void mapsMissingRatesAndOutagesToClearStatuses() throws Exception {
        when(rates.rate("USD", "XYZ")).thenThrow(new RateNotFoundException("No exchange rate from USD to XYZ"));
        when(rates.rate("USD", "EUR")).thenThrow(new ExchangeServiceUnavailableException(new RuntimeException("down")));

        mvc.perform(get("/api/v1/conversions").param("from", "USD").param("to", "XYZ").param("amount", "1"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/conversions").param("from", "USD").param("to", "EUR").param("amount", "1"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void validatesInput() throws Exception {
        mvc.perform(get("/api/v1/conversions").param("from", "US").param("to", "INR").param("amount", "1"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/conversions").param("from", "USD").param("to", "INR").param("amount", "-5"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/conversions").param("from", "USD").param("to", "INR").param("amount", "6000"))
                .andExpect(status().isBadRequest());
    }
}
