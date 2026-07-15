package org.redCare.api.service.repoScorer.domain.model;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import org.redCare.api.service.repoScorer.application.model.repoScorer.Repository;

/**
 * Domain model responsible for calculating repository popularity scores.
 *
 * <p>The score combines normalized logarithmic stars, normalized logarithmic
 * forks, and update recency. Scores are scaled to {@code 0..100} and rounded
 * to two decimal places.</p>
 */
public class RepositoryPopularityScorer {
    private static final int SCORE_SCALE = 100;

    private final double maxStars;
    private final double maxForks;
    private final Clock clock;
    private final RepositoryScoringWeights weights;

    private RepositoryPopularityScorer(
            double maxStars,
            double maxForks,
            Clock clock,
            RepositoryScoringWeights weights) {
        this.maxStars = maxStars;
        this.maxForks = maxForks;
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.weights = Objects.requireNonNull(weights, "weights must not be null");
    }

    /**
     * Creates a scorer normalized against the repositories being scored.
     *
     * @param repositories repositories in the current result page
     * @param clock clock used for deterministic recency calculation
     * @param weights scoring component weights
     * @return scorer for the provided repository set
     */
    public static RepositoryPopularityScorer from(
            List<Repository> repositories,
            Clock clock,
            RepositoryScoringWeights weights) {
        List<Repository> safeRepositories = repositories == null ? List.of() : repositories;
        double maxStars = safeRepositories.stream()
                .mapToDouble(repository -> Math.log1p(valueOrZero(repository.getStargazersCount())))
                .max()
                .orElse(0);
        double maxForks = safeRepositories.stream()
                .mapToDouble(repository -> Math.log1p(valueOrZero(repository.getForksCount())))
                .max()
                .orElse(0);

        return new RepositoryPopularityScorer(maxStars, maxForks, clock, weights);
    }

    /**
     * Calculates the popularity score for one repository.
     *
     * @param repository repository to score
     * @return score scaled to {@code 0..100} and rounded to two decimals
     */
    public Double calculate(Repository repository) {
        double starScore = normalizeLog(repository.getStargazersCount(), maxStars);
        double forkScore = normalizeLog(repository.getForksCount(), maxForks);
        double recencyScore = calculateRecencyScore(repository.getUpdatedAt());

        double score = SCORE_SCALE * ((weights.starWeight() * starScore)
                + (weights.forkWeight() * forkScore)
                + (weights.recencyWeight() * recencyScore));

        return Math.round(score * 100.0) / 100.0;
    }

    private double normalizeLog(Integer value, double maxValue) {
        if (maxValue == 0) {
            return 0;
        }
        return Math.log1p(valueOrZero(value)) / maxValue;
    }

    private double calculateRecencyScore(OffsetDateTime updatedAt) {
        if (updatedAt == null) {
            return 0;
        }

        long daysSinceUpdate = Math.max(0, ChronoUnit.DAYS.between(updatedAt, OffsetDateTime.now(clock)));
        return 1.0 / (1.0 + daysSinceUpdate);
    }

    private static Integer valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
