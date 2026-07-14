package org.redCare.api.service.repoScorer.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redCare.api.service.repoScorer.application.model.repoScorer.ScoredRepository;
import org.redCare.api.service.repoScorer.application.model.repoScorer.SearchRepositoriesResponse;
import org.redCare.api.service.repoScorer.application.useCase.SearchGithubReposQuery;
import org.redCare.api.service.repoScorer.application.useCase.SearchGithubReposUseCase;

@ExtendWith(MockitoExtension.class)
class RepoScorerApplicationServiceTest {

    @Mock
    private SearchGithubReposUseCase searchGithubReposUseCase;

    @Mock
    private RepositoryScoringDecorator repositoryScoringDecorator;

    @InjectMocks
    private RepoScorerApplicationService service;

    @Test
    void searchesGithubAndReturnsDecoratedRepositoriesWithTotalCount() {
        SearchRepositoriesResponse githubResponse = new SearchRepositoriesResponse().totalCount(12);
        List<ScoredRepository> scoredRepositories = List.of(new ScoredRepository().id(1L));
        when(searchGithubReposUseCase.handle(new SearchGithubReposQuery(
                "created:>=2026-01-01 language:Java", "updated", "desc", 25, 2)))
                .thenReturn(githubResponse);
        when(repositoryScoringDecorator.decorate(githubResponse)).thenReturn(scoredRepositories);

        var result = service.searchRepositories(LocalDate.of(2026, 1, 1), " Java ", 25, 2);

        assertThat(result.getTotalCount()).isEqualTo(12L);
        assertThat(result.getRepositories()).isSameAs(scoredRepositories);
    }

    @Test
    void quotesLanguageQualifierWhenLanguageContainsSpacesOrQuotes() {
        SearchRepositoriesResponse githubResponse = new SearchRepositoriesResponse();
        when(searchGithubReposUseCase.handle(org.mockito.ArgumentMatchers.any())).thenReturn(githubResponse);

        service.searchRepositories(LocalDate.of(2026, 2, 3), "C# \"Preview\"", 10, 1);

        ArgumentCaptor<SearchGithubReposQuery> queryCaptor = ArgumentCaptor.forClass(SearchGithubReposQuery.class);
        verify(searchGithubReposUseCase).handle(queryCaptor.capture());
        assertThat(queryCaptor.getValue())
                .isEqualTo(new SearchGithubReposQuery(
                        "created:>=2026-02-03 language:\"C# \\\"Preview\\\"\"", "updated", "desc", 10, 1));
    }

    @Test
    void returnsZeroTotalCountWhenGithubTotalCountIsMissing() {
        SearchRepositoriesResponse githubResponse = new SearchRepositoriesResponse();
        when(searchGithubReposUseCase.handle(org.mockito.ArgumentMatchers.any())).thenReturn(githubResponse);

        var result = service.searchRepositories(LocalDate.of(2026, 1, 1), "Kotlin", 5, 1);

        assertThat(result.getTotalCount()).isZero();
    }
}
