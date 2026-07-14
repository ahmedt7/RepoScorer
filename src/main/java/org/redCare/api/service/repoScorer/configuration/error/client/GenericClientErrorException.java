package org.redCare.api.service.repoScorer.configuration.error.client;

import java.util.Objects;
import org.springframework.http.HttpStatus;

public class GenericClientErrorException extends ClientError {
  private final int statusCode;

  public GenericClientErrorException(String message, int statusCode) {
    super(message);
    if ((statusCode < 400) || (statusCode > 499)) {
      throw new IllegalArgumentException("Status code is out of range (400-499)");
    }
    this.statusCode = statusCode;
  }

  public int statusCode() {
    return statusCode;
  }

  public HttpStatus status() {
    return Objects.requireNonNull(HttpStatus.resolve(statusCode));
  }
}
