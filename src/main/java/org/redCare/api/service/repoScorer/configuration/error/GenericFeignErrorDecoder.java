package org.redCare.api.service.repoScorer.configuration.error;

import static org.springframework.http.HttpHeaders.RETRY_AFTER;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.redCare.api.service.repoScorer.configuration.error.client.BadRequestException;
import org.redCare.api.service.repoScorer.configuration.error.client.ForbiddenException;
import org.redCare.api.service.repoScorer.configuration.error.client.GenericClientErrorException;
import org.redCare.api.service.repoScorer.configuration.error.client.NotFoundException;
import org.redCare.api.service.repoScorer.configuration.error.model.BasicError;
import org.redCare.api.service.repoScorer.configuration.error.server.BadGatewayException;
import org.redCare.api.service.repoScorer.configuration.error.server.GatewayTimeoutException;
import org.redCare.api.service.repoScorer.configuration.error.server.InternalServerErrorException;
import org.redCare.api.service.repoScorer.configuration.error.server.NotImplementedException;
import org.redCare.api.service.repoScorer.configuration.error.server.ServiceUnavailableException;

@Slf4j
public class GenericFeignErrorDecoder implements ErrorDecoder, FeignResponseFunctions {
  private final ObjectMapper mapper =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  @Override
  public Exception decode(String methodKey, Response response) {
    switch (response.status()) {
      case 400 -> throw new BadRequestException(getErrorMessage(response));
      case 403 -> throw new ForbiddenException(getErrorMessage(response));
      case 404 -> throw new NotFoundException();
      case 406 -> throw new InternalServerErrorException(getErrorMessage(response));
      case 501 -> throw new NotImplementedException(getErrorMessage(response));
      case 500, 502 -> throw new BadGatewayException();
      case 503 ->
          throw headerValue(response, RETRY_AFTER)
              .map(Integer::valueOf)
              .map(ServiceUnavailableException::new)
              .orElseGet(ServiceUnavailableException::new);
      case 504 -> throw new GatewayTimeoutException();
      default -> {
        if ((response.status() >= 400) && (response.status() <= 499)) {
          throw new GenericClientErrorException(getErrorMessage(response), response.status());
        }
        log.error(
            "An error was encountered while trying to reach another service. Response Code: {}",
            response.status());
        throw new InternalServerErrorException(getErrorMessage(response));
      }
    }
  }

  private String getErrorMessage(Response response) {
    return initBasicErrors(response).get(0).getMessage();
  }

  private List<BasicError> initBasicErrors(Response response) {
    List<BasicError> basicErrors = null;
    if (response.body() != null) {
      String bodyString = null;
      try (InputStream bodyInputStream = response.body().asInputStream()) {
        bodyString = new String(bodyInputStream.readAllBytes(), StandardCharsets.UTF_8);
        basicErrors = mapBasicErrors(bodyString);
      } catch (IOException e) {
        log.warn("Unable to deserialize: {}", bodyString, e);
      }
    }
    return (basicErrors != null)
        ? basicErrors
        : List.of(new BasicError(defaultErrorMessage(response)));
  }

  private List<BasicError> mapBasicErrors(String bodyString) throws IOException {
    try {
      return List.of(mapper.readValue(bodyString, BasicError[].class));
    } catch (IOException e) {
      return List.of(mapper.readValue(bodyString, BasicError.class));
    }
  }
}
