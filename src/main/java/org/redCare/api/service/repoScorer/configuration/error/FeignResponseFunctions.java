package org.redCare.api.service.repoScorer.configuration.error;

import feign.Response;
import java.util.Collection;
import java.util.Optional;
import org.springframework.http.HttpStatus;

public interface FeignResponseFunctions {
  default Optional<String> headerValue(Response response, String headerName) {
    return Optional.ofNullable(response.headers().get(headerName)).stream()
        .flatMap(Collection::stream)
        .findFirst();
  }

  default String defaultErrorMessage(Response response) {
    return HttpStatus.valueOf(response.status()).getReasonPhrase();
  }
}
