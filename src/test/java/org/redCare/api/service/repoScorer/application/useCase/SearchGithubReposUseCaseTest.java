package org.redCare.api.service.repoScorer.application.useCase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redCare.api.service.repoScorer.application.model.repoScorer.SearchRepositoriesResponse;
import org.redCare.api.service.repoScorer.application.port.out.github.GithubSearchReposPort;

@ExtendWith(MockitoExtension.class)
class SearchGithubReposUseCaseTest {

    @Mock
    private GithubSearchReposPort githubSearchReposPort;

    @InjectMocks
    private SearchGithubReposUseCase useCase;

    @Test
    void delegatesQueryToGithubSearchPort() {
        SearchGithubReposQuery query = new SearchGithubReposQuery("language:Java", "updated", "desc", 20, 3);
        SearchRepositoriesResponse response = new SearchRepositoriesResponse();
        when(githubSearchReposPort.searchRepositories("language:Java", "updated", "desc", 20, 3))
                .thenReturn(response);

        SearchRepositoriesResponse result = useCase.handle(query);

        assertThat(result).isSameAs(response);
        verify(githubSearchReposPort).searchRepositories("language:Java", "updated", "desc", 20, 3);
    }
}
