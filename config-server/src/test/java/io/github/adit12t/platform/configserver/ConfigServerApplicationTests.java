package io.github.adit12t.platform.configserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "eureka.client.enabled=false")
class ConfigServerApplicationTests {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void servesProfileSpecificValuesOverDefaults() {
        String dev = rest.getForObject("/limits-service/dev", String.class);
        String prod = rest.getForObject("/limits-service/prod", String.class);

        assertThat(dev).contains("\"limits.maximum\":100");
        assertThat(prod).contains("\"limits.maximum\":50000");
        assertThat(dev).contains("\"limits.currency\":\"USD\"");
    }
}
