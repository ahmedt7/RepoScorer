package org.redCare.api.service.repoScorer.adapter.out.github;

import static org.springframework.http.HttpHeaders.RETRY_AFTER;

import com.condor.api.service.core.error.client.BadRequestException;
import com.condor.api.service.core.error.client.ConflictException;
import com.condor.api.service.core.error.client.ForbiddenException;
import com.condor.api.service.core.error.client.GenericClientErrorException;
import com.condor.api.service.core.error.client.NotFoundException;
import com.condor.api.service.core.error.server.BadGatewayException;
import com.condor.api.service.core.error.server.GatewayTimeoutException;
import com.condor.api.service.core.error.server.InternalServerErrorException;
import com.condor.api.service.core.error.server.NotImplementedException;
import com.condor.api.service.core.error.server.ServiceUnavailableException;
import com.condor.api.service.core.model.BasicError;
import com.condor.api.service.core.model.DetailedError;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenericFeignErrorDecoder implements ErrorDecoder {
  private final ObjectMapper mapper =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  @Override
  public Exception decode(String methodKey, Response response) {
    switch (response.status()) {
      case 400 -> throw new BadRequestException(getErrorMessage(response));
      case 403 -> throw new ForbiddenException();
      case 404 -> throw new NotFoundException();
      case 406 -> throw new InternalServerErrorException(getErrorMessage(response));
      case 409 -> {
        DetailedError detailedError = initDetailedError(response);
        throw new ConflictException(
            detailedError.getMessage(), detailedError.getPath(), detailedError.getErrors());
      }
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

  private DetailedError initDetailedError(Response response) {
    DetailedError detailedError = null;
    if (response.body() != null) {
      String bodyString = null;
      try (InputStream bodyInputStream = response.body().asInputStream()) {
        bodyString = new String(bodyInputStream.readAllBytes(), StandardCharsets.UTF_8);
        detailedError = mapper.readValue(bodyString, DetailedError.class);
      } catch (IOException e) {
        log.warn("Unable to deserialize: {}", bodyString, e);
      }
    }
    return (detailedError != null)
        ? detailedError
        : new DetailedError(defaultErrorMessage(response));
  }

  private List<BasicError> mapBasicErrors(String bodyString) throws IOException {
    try {
      return List.of(mapper.readValue(bodyString, BasicError[].class));
    } catch (IOException e) {
      return List.of(mapper.readValue(bodyString, BasicError.class));
    }
  }
}
