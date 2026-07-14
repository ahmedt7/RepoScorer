package org.redCare.api.service.repoScorer.configuration.error.server;

import lombok.Getter;

@Getter
public class ServiceUnavailableException extends ServerError {
  private final Integer retryAfter;

  public ServiceUnavailableException() {
    retryAfter = null;
  }

  public ServiceUnavailableException(String message) {
    super(message);
    retryAfter = null;
  }

  public ServiceUnavailableException(String message, Throwable cause) {
    super(message, cause);
    retryAfter = null;
  }

  public ServiceUnavailableException(Throwable cause) {
    super(cause);
    retryAfter = null;
  }

  public ServiceUnavailableException(int retryAfter) {
    this.retryAfter = retryAfter;
  }

  public ServiceUnavailableException(String message, int retryAfter) {
    super(message);
    this.retryAfter = retryAfter;
  }

  public ServiceUnavailableException(String message, Throwable cause, int retryAfter) {
    super(message, cause);
    this.retryAfter = retryAfter;
  }

  public ServiceUnavailableException(Throwable cause, int retryAfter) {
    super(cause);
    this.retryAfter = retryAfter;
  }
}
