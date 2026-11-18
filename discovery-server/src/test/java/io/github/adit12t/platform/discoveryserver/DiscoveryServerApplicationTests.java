package io.github.adit12t.platform.discoveryserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DiscoveryServerApplicationTests {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void registryIsUp() {
        assertThat(rest.getForEntity("/actuator/health", String.class).getBody()).contains("UP");
        assertThat(rest.getForEntity("/eureka/apps", String.class).getStatusCode().is2xxSuccessful()).isTrue();
    }
}
