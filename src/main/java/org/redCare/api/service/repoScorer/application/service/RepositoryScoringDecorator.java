package org.redCare.api.service.repoScorer.application.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.redCare.api.service.repoScorer.application.model.repoScorer.Repository;
import org.redCare.api.service.repoScorer.application.model.repoScorer.ScoredRepository;
import org.redCare.api.service.repoScorer.application.model.repoScorer.SearchRepositoriesResponse;
import org.springframework.stereotype.Component;

/**
 * Converts GitHub repository search results into scored repository responses.
 *
 * <p>The score combines normalized logarithmic stars, normalized logarithmic
 * forks, and update recency. Scores are rounded to two decimal places and
 * returned in descending popularity order.</p>
 */
@Component
@RequiredArgsConstructor
public class RepositoryScoringDecorator {
    private static final double STAR_WEIGHT = 0.50;
    private static final double FORK_WEIGHT = 0.30;
    private static final double RECENCY_WEIGHT = 0.20;
    private static final int SCORE_SCALE = 100;

    private final Clock clock;

    /**
     * Decorates a GitHub search response with popularity scores.
     *
     * @param searchRepositoriesResponse GitHub search response; {@code null}
     *                                   responses and item lists are treated as empty
     * @return scored repositories ordered by descending popularity score
     */
    public List<ScoredRepository> decorate(SearchRepositoriesResponse searchRepositoriesResponse) {
        List<Repository> repositories = Optional.ofNullable(searchRepositoriesResponse)
                .map(SearchRepositoriesResponse::getItems)
                .orElse(List.of());

        double maxStars = repositories.stream()
                .mapToDouble(repository -> Math.log1p(valueOrZero(repository.getStargazersCount())))
                .max()
                .orElse(0);
        double maxForks = repositories.stream()
                .mapToDouble(repository -> Math.log1p(valueOrZero(repository.getForksCount())))
                .max()
                .orElse(0);

        return repositories.stream()
                .map(repository -> toScoredRepository(repository, maxStars, maxForks))
                .sorted(Comparator.comparing(ScoredRepository::getPopularityScore).reversed())
                .toList();
    }

    private ScoredRepository toScoredRepository(Repository repository, double maxStars, double maxForks) {
        Integer stars = valueOrZero(repository.getStargazersCount());
        Integer forks = valueOrZero(repository.getForksCount());
        Integer watchers = valueOrZero(repository.getWatchersCount());
        Integer openIssues = valueOrZero(repository.getOpenIssuesCount());
        Double popularityScore = calculatePopularityScore(repository, maxStars, maxForks);

        return new ScoredRepository()
                .id(repository.getId())
                .name(repository.getName())
                .fullName(repository.getFullName())
                .description(repository.getDescription())
                .htmlUrl(repository.getHtmlUrl())
                .createdAt(repository.getCreatedAt())
                .updatedAt(repository.getUpdatedAt())
                .language(repository.getLanguage())
                .stars(stars)
                .forks(forks)
                .watchers(watchers)
                .openIssues(openIssues)
                .popularityScore(popularityScore)
                .owner(repository.getOwner());
    }

    private Double calculatePopularityScore(Repository repository, double maxStars, double maxForks) {
        double starScore = normalizeLog(repository.getStargazersCount(), maxStars);
        double forkScore = normalizeLog(repository.getForksCount(), maxForks);
        double recencyScore = calculateRecencyScore(repository.getUpdatedAt());

        double score = SCORE_SCALE * ((STAR_WEIGHT * starScore)
                + (FORK_WEIGHT * forkScore)
                + (RECENCY_WEIGHT * recencyScore));

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

    private Integer valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
