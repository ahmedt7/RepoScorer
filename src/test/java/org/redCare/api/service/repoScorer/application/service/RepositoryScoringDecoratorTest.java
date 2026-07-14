package org.redCare.api.service.repoScorer.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.redCare.api.service.repoScorer.application.model.repoScorer.Owner;
import org.redCare.api.service.repoScorer.application.model.repoScorer.Repository;
import org.redCare.api.service.repoScorer.application.model.repoScorer.ScoredRepository;
import org.redCare.api.service.repoScorer.application.model.repoScorer.SearchRepositoriesResponse;

class RepositoryScoringDecoratorTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-14T12:00:00Z"), ZoneOffset.UTC);
    private final RepositoryScoringDecorator decorator = new RepositoryScoringDecorator(clock);

    @Test
    void decoratesRepositoriesWithScoresAndSortsByPopularityDescending() {
        Repository popular = repository(1L, "popular", 100, 50, 4, 2, 0);
        Repository stale = repository(2L, "stale", 10, 5, 3, 1, 9);
        SearchRepositoriesResponse response = new SearchRepositoriesResponse()
                .items(List.of(stale, popular));

        List<ScoredRepository> result = decorator.decorate(response);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ScoredRepository::getName).containsExactly("popular", "stale");
        assertThat(result.get(0))
                .returns(1L, ScoredRepository::getId)
                .returns("owner/popular", ScoredRepository::getFullName)
                .returns("Repository popular", ScoredRepository::getDescription)
                .returns(URI.create("https://github.com/owner/popular"), ScoredRepository::getHtmlUrl)
                .returns("Java", ScoredRepository::getLanguage)
                .returns(100, ScoredRepository::getStars)
                .returns(50, ScoredRepository::getForks)
                .returns(4, ScoredRepository::getWatchers)
                .returns(2, ScoredRepository::getOpenIssues)
                .returns(100.0, ScoredRepository::getPopularityScore)
                .returns(popular.getOwner(), ScoredRepository::getOwner);
        assertThat(result.get(1).getPopularityScore()).isLessThan(result.get(0).getPopularityScore());
    }

    @Test
    void treatsNullMetricsAndFutureUpdatesAsZeroMetricsAndCurrentRecency() {
        OffsetDateTime tomorrow = OffsetDateTime.now(clock).plusDays(1);
        Repository repository = repository(3L, "empty", null, null, null, null, 0)
                .updatedAt(tomorrow);

        List<ScoredRepository> result = decorator.decorate(new SearchRepositoriesResponse().items(List.of(repository)));

        assertThat(result).singleElement()
                .returns(0, ScoredRepository::getStars)
                .returns(0, ScoredRepository::getForks)
                .returns(0, ScoredRepository::getWatchers)
                .returns(0, ScoredRepository::getOpenIssues)
                .returns(20.0, ScoredRepository::getPopularityScore);
    }

    @Test
    void returnsEmptyListForNullResponseOrItems() {
        assertThat(decorator.decorate(null)).isEmpty();
        assertThat(decorator.decorate(new SearchRepositoriesResponse())).isEmpty();
    }

    private Repository repository(
            Long id,
            String name,
            Integer stars,
            Integer forks,
            Integer watchers,
            Integer openIssues,
            long daysSinceUpdate) {
        return new Repository()
                .id(id)
                .name(name)
                .fullName("owner/" + name)
                .description("Repository " + name)
                .htmlUrl(URI.create("https://github.com/owner/" + name))
                .createdAt(OffsetDateTime.now(clock).minusYears(1))
                .updatedAt(OffsetDateTime.now(clock).minusDays(daysSinceUpdate))
                .language("Java")
                .stargazersCount(stars)
                .forksCount(forks)
                .watchersCount(watchers)
                .openIssuesCount(openIssues)
                .owner(new Owner().login("owner").htmlUrl(URI.create("https://github.com/owner")));
    }
}
