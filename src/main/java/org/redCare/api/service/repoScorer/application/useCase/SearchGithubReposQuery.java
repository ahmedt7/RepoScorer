package org.redCare.api.service.repoScorer.application.useCase;

public record SearchGithubReposQuery(String query, String sort, String order, Integer perPage, Integer page) {}
