package org.redCare.api.service.repoScorer.adapter.in.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redCare.api.service.repoScorer.application.model.repoScorer.ScoredRepositoriesResponse;
import org.redCare.api.service.repoScorer.application.service.RepoScorerApplicationService;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ReposCorerControllerTest {

    @Mock
    private RepoScorerApplicationService repoScorerApplicationService;

    @InjectMocks
    private ReposCorerController controller;

    @Test
    void returnsOkResponseFromApplicationService() {
        ScoredRepositoriesResponse serviceResponse = new ScoredRepositoriesResponse().totalCount(4L);
        LocalDate createdAfter = LocalDate.of(2026, 1, 1);
        when(repoScorerApplicationService.searchRepositories(createdAfter, "Java", 10, 1))
                .thenReturn(serviceResponse);

        var response = controller.searchRepositories(createdAfter, "Java", 10, 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(serviceResponse);
        verify(repoScorerApplicationService).searchRepositories(createdAfter, "Java", 10, 1);
    }
}
