package org.redCare.api.service.repoScorer.adapter.out.github;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redCare.api.service.repoScorer.application.model.repoScorer.SearchRepositoriesResponse;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class GithubSearchReposAdapterTest {

    @Mock
    private GithubSearchReposClient githubSearchReposClient;

    @InjectMocks
    private GithubSearchReposAdapter adapter;

    @Test
    void returnsBodyFromGithubClientResponse() {
        SearchRepositoriesResponse response = new SearchRepositoriesResponse().totalCount(5);
        when(githubSearchReposClient.searchRepositories("created:>=2026-01-01", "updated", "desc", 30, 1))
                .thenReturn(ResponseEntity.ok(response));

        SearchRepositoriesResponse result = adapter.searchRepositories("created:>=2026-01-01", "updated", "desc", 30, 1);

        assertThat(result).isSameAs(response);
        verify(githubSearchReposClient).searchRepositories("created:>=2026-01-01", "updated", "desc", 30, 1);
    }
}
