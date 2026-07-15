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
import org.redCare.api.service.repoScorer.configuration.RepoScorerApplicationProperties;

class RepositoryScoringDecoratorTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-14T12:00:00Z"), ZoneOffset.UTC);
    private final RepositoryScoringDecorator decorator = new RepositoryScoringDecorator(defaultProperties(), clock);

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

    @Test
    void appliesTieBreakersAfterPopularityScore() {
        RepositoryScoringDecorator recencyOnlyDecorator = new RepositoryScoringDecorator(recencyOnlyProperties(), clock);
        Repository lowerStars = repository(3L, "lower-stars", 10, 2, 1, 0, 0);
        Repository lowerForks = repository(1L, "lower-forks", 20, 1, 1, 0, 0);
        Repository lowerId = repository(2L, "lower-id", 20, 5, 1, 0, 0);
        Repository higherId = repository(4L, "higher-id", 20, 5, 1, 0, 0);
        SearchRepositoriesResponse response = new SearchRepositoriesResponse()
                .items(List.of(lowerStars, lowerForks, higherId, lowerId));

        List<ScoredRepository> result = recencyOnlyDecorator.decorate(response);

        assertThat(result).extracting(ScoredRepository::getName)
                .containsExactly("lower-id", "higher-id", "lower-forks", "lower-stars");
        assertThat(result).extracting(ScoredRepository::getPopularityScore)
                .containsOnly(100.0);
    }

    private RepoScorerApplicationProperties defaultProperties() {
        return new RepoScorerApplicationProperties();
    }

    private RepoScorerApplicationProperties recencyOnlyProperties() {
        RepoScorerApplicationProperties properties = new RepoScorerApplicationProperties();
        properties.getScoring().setStarWeight(0.0);
        properties.getScoring().setForkWeight(0.0);
        properties.getScoring().setRecencyWeight(1.0);
        return properties;
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
