package org.redCare.api.service.repoScorer.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.redCare.api.service.repoScorer.application.model.repoScorer.Repository;

class RepositoryPopularityScorerTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-14T12:00:00Z"), ZoneOffset.UTC);
    private final RepositoryScoringWeights defaultWeights = new RepositoryScoringWeights(0.50, 0.30, 0.20);

    @Test
    void calculatesMaximumScoreForRepositoryWithMaxMetricsAndCurrentUpdate() {
        Repository repository = repository(100, 50, 0);
        RepositoryPopularityScorer scorer = RepositoryPopularityScorer.from(List.of(repository), clock, defaultWeights);

        assertThat(scorer.calculate(repository)).isEqualTo(100.0);
    }

    @Test
    void treatsNullMetricsAndFutureUpdatesAsZeroMetricsAndCurrentRecency() {
        Repository repository = new Repository()
                .stargazersCount(null)
                .forksCount(null)
                .updatedAt(OffsetDateTime.now(clock).plusDays(1));
        RepositoryPopularityScorer scorer = RepositoryPopularityScorer.from(List.of(repository), clock, defaultWeights);

        assertThat(scorer.calculate(repository)).isEqualTo(20.0);
    }

    @Test
    void appliesConfiguredWeights() {
        Repository repository = repository(0, 0, 0);
        RepositoryPopularityScorer scorer = RepositoryPopularityScorer.from(
                List.of(repository),
                clock,
                new RepositoryScoringWeights(0.0, 0.0, 1.0));

        assertThat(scorer.calculate(repository)).isEqualTo(100.0);
    }

    @Test
    void normalizesStarsAndForksAgainstRepositorySet() {
        Repository max = repository(100, 50, 0);
        Repository empty = repository(0, 0, 0);
        RepositoryPopularityScorer scorer = RepositoryPopularityScorer.from(List.of(max, empty), clock, defaultWeights);

        assertThat(scorer.calculate(max)).isEqualTo(100.0);
        assertThat(scorer.calculate(empty)).isEqualTo(20.0);
    }

    private Repository repository(Integer stars, Integer forks, long daysSinceUpdate) {
        return new Repository()
                .stargazersCount(stars)
                .forksCount(forks)
                .updatedAt(OffsetDateTime.now(clock).minusDays(daysSinceUpdate));
    }
}
