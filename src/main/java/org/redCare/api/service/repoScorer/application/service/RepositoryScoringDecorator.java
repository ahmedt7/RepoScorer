package org.redCare.api.service.repoScorer.application.service;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.redCare.api.service.repoScorer.application.model.repoScorer.Repository;
import org.redCare.api.service.repoScorer.application.model.repoScorer.ScoredRepository;
import org.redCare.api.service.repoScorer.application.model.repoScorer.SearchRepositoriesResponse;
import org.redCare.api.service.repoScorer.configuration.RepoScorerApplicationProperties;
import org.redCare.api.service.repoScorer.domain.model.RepositoryPopularityScorer;
import org.redCare.api.service.repoScorer.domain.model.RepositoryScoringWeights;
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
    private final RepoScorerApplicationProperties properties;
    private static final Comparator<ScoredRepository> POPULARITY_ORDER =
            Comparator.comparing(ScoredRepository::getPopularityScore, Comparator.reverseOrder())
                    .thenComparing(ScoredRepository::getStars, Comparator.reverseOrder())
                    .thenComparing(ScoredRepository::getForks, Comparator.reverseOrder())
                    .thenComparing(ScoredRepository::getId, Comparator.nullsLast(Comparator.naturalOrder()));

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

        RepositoryPopularityScorer popularityScorer = RepositoryPopularityScorer.from(
                repositories,
                clock,
                RepositoryScoringWeights.of(
                        properties.getScoring().getStarWeight(),
                        properties.getScoring().getForkWeight(),
                        properties.getScoring().getRecencyWeight()));

        return repositories.stream()
                .map(repository -> toScoredRepository(repository, popularityScorer))
                .sorted(POPULARITY_ORDER)
                .toList();
    }

    private ScoredRepository toScoredRepository(Repository repository, RepositoryPopularityScorer popularityScorer) {
        Integer stars = valueOrZero(repository.getStargazersCount());
        Integer forks = valueOrZero(repository.getForksCount());
        Integer watchers = valueOrZero(repository.getWatchersCount());
        Integer openIssues = valueOrZero(repository.getOpenIssuesCount());
        Double popularityScore = popularityScorer.calculate(repository);

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

    private Integer valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
