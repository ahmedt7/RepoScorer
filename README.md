# RepoScorer

RepoScorer is a Spring Boot service that searches GitHub repositories and returns
the results with a calculated popularity score. The service exposes a local HTTP
API, calls GitHub through a Spring Cloud OpenFeign client, and decorates each
repository with a score based on stars, forks, and update recency.

## Stack

- Java 17
- Spring Boot 3.5
- Spring Cloud OpenFeign
- Resilience4j
- OpenAPI Generator
- JUnit 5, Mockito, AssertJ
- Spring Cloud Contract WireMock
- JaCoCo

## Architecture

The project follows a small ports-and-adapters layout:

- `adapter/in/web/controller`: inbound HTTP controller implementing the generated OpenAPI interface.
- `application/service`: application orchestration and repository scoring logic.
- `application/useCase`: use case boundary for GitHub repository search.
- `application/port/out/github`: outbound GitHub search port.
- `adapter/out/github`: Feign client, Feign configuration, and GitHub adapter.
- `configuration`: security, clock, and error-handling configuration.
- `src/main/openapi`: OpenAPI specifications used to generate API and model classes.

Generated OpenAPI API/model classes are written under `target/generated-sources/openapi`
during the Maven build.

## API

Search repositories:

```http
GET /repositories?createdAfter=2026-01-01&language=Java&perPage=30&page=1
X-ApiClient-Roles: USER
```

Query parameters:

- `createdAfter`: required ISO date. Used as the GitHub `created:>=...` qualifier.
- `language`: required GitHub language qualifier. Values containing spaces or quotes are escaped and quoted.
- `perPage`: optional page size sent to GitHub as `per_page`.
- `page`: optional GitHub result page.

The service searches GitHub using:

```text
created:>=<createdAfter> language:<language>
sort=updated
order=desc
```

## Scoring

Repository scores are calculated by `RepositoryScoringDecorator`.

Weights:

- Stars: `50%`
- Forks: `30%`
- Recency: `20%`

Stars and forks are normalized with `log1p` against the maximum value in the
current response. Recency is calculated as:

```text
1 / (1 + daysSinceLastUpdate)
```

The final score is scaled to `0..100`, rounded to two decimals, and results are
returned in descending score order.

## Configuration

GitHub base URL:

```yaml
github:
  api:
    baseUrl: https://api.github.com
```

Security roles are read from:

```text
X-ApiClient-Roles
```

The `USER` role is required for the GitHub search use case.

## Build And Test

Run the full build, tests, generated source compilation, and coverage check:

```bash
mvn verify
```

Run tests only:

```bash
mvn test
```

Generate OpenAPI classes without running tests:

```bash
mvn generate-sources
```

## Test Coverage

JaCoCo runs during `mvn verify` and enforces the configured line coverage gate.
Generated OpenAPI classes, the Spring Boot launcher, and most error support
classes are excluded from the report. The Feign error decoder remains included.

Coverage report:

```text
target/site/jacoco/index.html
```

## WireMock Integration Test

`RepositoryScorerIntegrationTest` starts the Spring Boot application on a random
port and starts WireMock on a random port. The test overrides:

```text
github.api.baseUrl=http://localhost:${wiremock.server.port}
```

WireMock stubs are file-backed:

- Mapping: `src/test/resources/wiremock/mappings/github-search-repositories-java.json`
- Response body: `src/test/resources/wiremock/__files/github/search-repositories-java.json`

The integration test verifies both the public `/repositories` response and the
outbound GitHub `/search/repositories` request.

## Development Notes

- Keep GitHub response fixtures in `src/test/resources/wiremock/__files`.
- Keep WireMock request mappings in `src/test/resources/wiremock/mappings`.
- Prefer focused unit tests for scoring/query behavior and file-backed WireMock
  tests for HTTP and Feign integration behavior.
- OpenAPI-generated code should not be edited directly; update the YAML specs
  and regenerate instead.
