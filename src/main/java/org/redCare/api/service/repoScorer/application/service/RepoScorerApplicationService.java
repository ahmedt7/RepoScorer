package org.redCare.api.service.repoScorer.application.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redCare.api.service.repoScorer.application.model.repoScorer.ScoredRepositoriesResponse;
import org.redCare.api.service.repoScorer.application.model.repoScorer.ScoredRepository;
import org.redCare.api.service.repoScorer.application.model.repoScorer.SearchRepositoriesResponse;
import org.redCare.api.service.repoScorer.application.useCase.SearchGithubReposQuery;
import org.redCare.api.service.repoScorer.application.useCase.SearchGithubReposUseCase;
import org.redCare.api.service.repoScorer.configuration.error.client.BadRequestException;
import org.springframework.stereotype.Service;

/**
 * Coordinates repository search and scoring for inbound API requests.
 *
 * <p>This service owns GitHub query construction, delegates the actual search
 * to the use case, and maps GitHub's response into the API's scored response
 * model.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RepoScorerApplicationService {
    private final SearchGithubReposUseCase searchGithubReposUseCase;
    private final RepositoryScoringDecorator repositoryScoringDecorator;

    /**
     * Searches repositories created after a given date and enriches them with a
     * popularity score.
     *
     * @param createdAfter inclusive lower bound for repository creation date
     * @param language single repository language qualifier; complex values are quoted
     * @param perPage number of results requested per page
     * @param page requested result page
     * @return scored repositories and the GitHub total count
     */
    public ScoredRepositoriesResponse searchRepositories(LocalDate createdAfter, String language, Integer perPage, Integer page) {
        SearchRepositoriesResponse searchRepositoriesResponse = searchGithubReposUseCase.handle(
                new SearchGithubReposQuery(buildSearchQuery(createdAfter, language), "updated", "desc", perPage, page)
        );
        List<ScoredRepository> scoredRepositories = repositoryScoringDecorator.decorate(searchRepositoriesResponse);

        return new ScoredRepositoriesResponse()
                .totalCount(toLong(searchRepositoriesResponse))
                .repositories(scoredRepositories);
    }

    private String buildSearchQuery(LocalDate createdAfter, String language) {
        return "created:>=%s language:%s".formatted(createdAfter, formatLanguageQualifier(language));
    }

    private String formatLanguageQualifier(String language) {

        String trimmedLanguage = language.trim();
        if ("null".equalsIgnoreCase(trimmedLanguage)) {
            throw new BadRequestException("language parameter is required");
        }

        if (trimmedLanguage.contains(",")) {
            throw new BadRequestException("language parameter must contain exactly one language");
        }

        if (trimmedLanguage.matches("[A-Za-z0-9#+.-]+")) {
            return trimmedLanguage;
        }
        return "\"%s\"".formatted(trimmedLanguage.replace("\\", "\\\\").replace("\"", "\\\""));
    }

    private Long toLong(SearchRepositoriesResponse searchRepositoriesResponse) {
        if (searchRepositoriesResponse == null || searchRepositoriesResponse.getTotalCount() == null) {
            return 0L;
        }
        return searchRepositoriesResponse.getTotalCount().longValue();
    }
}
