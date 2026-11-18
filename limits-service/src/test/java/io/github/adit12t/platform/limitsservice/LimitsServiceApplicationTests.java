package io.github.adit12t.platform.limitsservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(properties = {"eureka.client.enabled=false", "spring.cloud.config.enabled=false", "management.tracing.enabled=false", "limits.maximum=250"})
@AutoConfigureMockMvc
class LimitsServiceApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void returnsConfiguredLimits() throws Exception {
        mvc.perform(get("/api/v1/limits"))
                .andExpect(jsonPath("$.minimum").value(1))
                .andExpect(jsonPath("$.maximum").value(250))
                .andExpect(jsonPath("$.currency").value("USD"));
    }
}
