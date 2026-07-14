package org.redCare.api.service.repoScorer.adapter.out.github;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.redCare.api.service.repoScorer.adapter.in.web.api.SearchApi;
import org.redCare.api.service.repoScorer.application.model.repoScorer.SearchRepositoriesResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Validated
@FeignClient(
        name = "GithubSearchReposClient",
        url = "${github.api.baseUrl}/"
)
@CircuitBreaker(name = "GambitPaymentOptionClientCB")
public interface GithubSearchReposClient extends SearchApi{

    @GetMapping("/search/repositories")
    ResponseEntity<SearchRepositoriesResponse> searchRepositories(@RequestParam String q,@RequestParam String sort,@RequestParam String order,@RequestParam Integer per_page,@RequestParam Integer page);
}
