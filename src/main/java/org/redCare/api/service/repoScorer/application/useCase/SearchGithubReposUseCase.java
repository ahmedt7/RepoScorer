package org.redCare.api.service.repoScorer.application.useCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redCare.api.service.repoScorer.application.model.repoScorer.SearchRepositoriesResponse;
import org.redCare.api.service.repoScorer.application.port.out.github.GithubSearchReposPort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchGithubReposUseCase {
    private final GithubSearchReposPort githubSearchReposPort;

    @PreAuthorize( "hasRole('USER')")
    public SearchRepositoriesResponse handle(SearchGithubReposQuery searchGithubReposQuery) {
        return githubSearchReposPort.searchRepositories(searchGithubReposQuery.query(), searchGithubReposQuery.sort(), searchGithubReposQuery.order(), searchGithubReposQuery.perPage(), searchGithubReposQuery.page());
    }
}
