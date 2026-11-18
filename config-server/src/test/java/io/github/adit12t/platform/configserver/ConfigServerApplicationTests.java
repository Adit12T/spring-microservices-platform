package io.github.adit12t.platform.configserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "eureka.client.enabled=false")
class ConfigServerApplicationTests {

    @Value("${local.server.port}")
    private int port;

    private RestClient rest() {
        return RestClient.create("http://localhost:" + port);
    }

    @Test
    void servesProfileSpecificValuesOverDefaults() {
        String dev = rest().get().uri("/limits-service/dev").retrieve().body(String.class);
        String prod = rest().get().uri("/limits-service/prod").retrieve().body(String.class);

        assertThat(dev).contains("\"limits.maximum\":100");
        assertThat(prod).contains("\"limits.maximum\":50000");
        assertThat(dev).contains("\"limits.currency\":\"USD\"");
    }
}
