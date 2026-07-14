package org.redCare.api.service.repoScorer.adapter.in.web.controller;


import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.redCare.api.service.repoScorer.adapter.in.web.api.RepositoryScoringApi;
import org.redCare.api.service.repoScorer.application.model.repoScorer.ScoredRepositoriesResponse;
import org.redCare.api.service.repoScorer.application.service.RepoScorerApplicationService;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Validated
@RequiredArgsConstructor
public class ReposCorerController implements RepositoryScoringApi {
    private final RepoScorerApplicationService repoScorerApplicationService;
    @Override
    public ResponseEntity<ScoredRepositoriesResponse> searchRepositories(LocalDate createdAfter, String language, Integer perPage, Integer page) {
        ScoredRepositoriesResponse scoredRepositoriesResponse = repoScorerApplicationService.searchRepositories( createdAfter,  language,  perPage,  page);
        return ResponseEntity.ok(scoredRepositoriesResponse);
    }
}
