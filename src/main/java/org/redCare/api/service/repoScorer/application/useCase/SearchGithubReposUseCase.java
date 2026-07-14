package org.redCare.api.service.repoScorer.application.useCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redCare.api.service.repoScorer.application.model.repoScorer.SearchRepositoriesResponse;
import org.redCare.api.service.repoScorer.application.port.out.github.GithubSearchReposPort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Use case for executing a GitHub repository search through the outbound port.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchGithubReposUseCase {
    private final GithubSearchReposPort githubSearchReposPort;

    /**
     * Executes the search when the caller has the {@code USER} role.
     *
     * @param searchGithubReposQuery query, sorting, and pagination arguments
     * @return GitHub search response
     */
    @PreAuthorize( "hasRole('USER')")
    public SearchRepositoriesResponse handle(SearchGithubReposQuery searchGithubReposQuery) {
        return githubSearchReposPort.searchRepositories(searchGithubReposQuery.query(), searchGithubReposQuery.sort(), searchGithubReposQuery.order(), searchGithubReposQuery.perPage(), searchGithubReposQuery.page());
    }
}
