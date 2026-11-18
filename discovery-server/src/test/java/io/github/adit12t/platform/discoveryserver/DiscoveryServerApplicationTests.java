package io.github.adit12t.platform.discoveryserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DiscoveryServerApplicationTests {

    @Value("${local.server.port}")
    private int port;

    private RestClient rest() {
        return RestClient.create("http://localhost:" + port);
    }

    @Test
    void registryIsUp() {
        assertThat(rest().get().uri("/actuator/health").retrieve().body(String.class)).contains("UP");
        assertThat(rest().get().uri("/eureka/apps").retrieve().toBodilessEntity().getStatusCode().is2xxSuccessful()).isTrue();
    }
}
