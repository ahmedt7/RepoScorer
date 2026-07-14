package org.redCare.api.service.repoScorer.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.redCare.api.service.repoScorer.configuration.SecurityHeadersFilter.API_CLIENT_ROLES_HEADER;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.redCare.api.service.repoScorer.RepoScorerApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

/**
 * Full-stack repository search test using WireMock as the GitHub API.
 *
 * <p>WireMock mappings and response bodies are loaded from
 * {@code src/test/resources/wiremock}; the test verifies the public API
 * response and the outbound GitHub search request without embedding response
 * JSON in Java code.</p>
 */
@SpringBootTest(
        classes = {RepoScorerApplication.class, RepositoryScorerIntegrationTest.FixedClockConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(
        port = 0,
        stubs = "classpath:/wiremock/mappings",
        files = "classpath:/wiremock")
@TestPropertySource(properties = "github.api.baseUrl=http://localhost:${wiremock.server.port}")
class RepositoryScorerIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @LocalServerPort
    private int port;

    /**
     * Verifies the request path from HTTP API through Feign to WireMock and back
     * through the scoring layer.
     *
     * @throws Exception when the API response cannot be parsed
     */
    @Test
    void searchesRepositoriesThroughWireMockedGithubClient() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(API_CLIENT_ROLES_HEADER, "USER");

        var response = restTemplate.exchange(
                "http://localhost:%d/repositories?createdAfter=2026-01-01&language=Java&perPage=2&page=1"
                        .formatted(port),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("totalCount").asLong()).isEqualTo(2L);
        assertThat(body.get("repositories")).hasSize(2);
        assertThat(body.at("/repositories/0/name").asText()).isEqualTo("alpha");
        assertThat(body.at("/repositories/0/fullName").asText()).isEqualTo("redcare/alpha");
        assertThat(body.at("/repositories/0/stars").asInt()).isEqualTo(100);
        assertThat(body.at("/repositories/0/forks").asInt()).isEqualTo(50);
        assertThat(body.at("/repositories/0/watchers").asInt()).isEqualTo(20);
        assertThat(body.at("/repositories/0/openIssues").asInt()).isEqualTo(3);
        assertThat(body.at("/repositories/0/popularityScore").asDouble()).isEqualTo(100.0);
        assertThat(body.at("/repositories/1/name").asText()).isEqualTo("beta");
        assertThat(body.at("/repositories/1/popularityScore").asDouble()).isEqualTo(41.47);

        verify(getRequestedFor(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", equalTo("created:>=2026-01-01 language:Java"))
                .withQueryParam("sort", equalTo("updated"))
                .withQueryParam("order", equalTo("desc"))
                .withQueryParam("per_page", equalTo("2"))
                .withQueryParam("page", equalTo("1")));
    }

    /**
     * Provides a deterministic clock so recency-based scores remain stable.
     */
    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-07-14T12:00:00Z"), ZoneOffset.UTC);
        }
    }
}
