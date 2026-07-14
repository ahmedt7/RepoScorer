package org.redCare.api.service.repoScorer.application.port.out.github;

import org.redCare.api.service.repoScorer.application.model.repoScorer.SearchRepositoriesResponse;

public interface GithubSearchReposPort {

    SearchRepositoriesResponse searchRepositories(String query, String sort, String order, Integer perPage, Integer page);
}
