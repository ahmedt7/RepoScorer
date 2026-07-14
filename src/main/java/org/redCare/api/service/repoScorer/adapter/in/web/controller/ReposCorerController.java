package org.redCare.api.service.repoScorer.adapter.in.web.controller;


import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.redCare.api.service.repoScorer.adapter.in.web.api.RepositoryScoringApi;
import org.redCare.api.service.repoScorer.application.model.repoScorer.ScoredRepositoriesResponse;
import org.redCare.api.service.repoScorer.application.service.RepoScorerApplicationService;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;


/**
 * HTTP controller that adapts the generated repository scoring API to the
 * application service.
 */
@RestController
@Validated
@RequiredArgsConstructor
public class ReposCorerController implements RepositoryScoringApi {
    private final RepoScorerApplicationService repoScorerApplicationService;

    /**
     * Searches GitHub repositories and returns scored results.
     *
     * @param createdAfter inclusive lower bound for repository creation date
     * @param language repository language qualifier
     * @param perPage number of repositories requested from GitHub
     * @param page GitHub search result page
     * @return scored repository response
     */
    @Override
    public ResponseEntity<ScoredRepositoriesResponse> searchRepositories(LocalDate createdAfter, String language, Integer perPage, Integer page) {
        ScoredRepositoriesResponse scoredRepositoriesResponse = repoScorerApplicationService.searchRepositories( createdAfter,  language,  perPage,  page);
        return ResponseEntity.ok(scoredRepositoriesResponse);
    }
}
