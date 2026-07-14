package org.redCare.api.service.repoScorer.configuration.error.client;

public class ForbiddenException extends ClientError {
  public ForbiddenException() {}

  public ForbiddenException(String message) {
    super(message);
  }

  public ForbiddenException(String message, Throwable cause) {
    super(message, cause);
  }

  public ForbiddenException(Throwable cause) {
    super(cause);
  }
}
