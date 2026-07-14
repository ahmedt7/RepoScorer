package org.redCare.api.service.repoScorer.adapter.out.github;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redCare.api.service.repoScorer.application.model.repoScorer.SearchRepositoriesResponse;
import org.redCare.api.service.repoScorer.application.port.out.github.GithubSearchReposPort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class GithubSearchReposAdapter implements GithubSearchReposPort {

    private final GithubSearchReposClient githubSearchReposClient;

    @Override
    public SearchRepositoriesResponse searchRepositories(String query, String sort, String order, Integer perPage, Integer page) {
        return githubSearchReposClient.searchRepositories(query, sort, order, perPage, page).getBody();
    }
}
