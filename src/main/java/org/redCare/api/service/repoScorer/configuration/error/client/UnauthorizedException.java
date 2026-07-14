package org.redCare.api.service.repoScorer.configuration.error.client;

public class UnauthorizedException extends ClientError {
  public UnauthorizedException() {}

  public UnauthorizedException(String message) {
    super(message);
  }

  public UnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }

  public UnauthorizedException(Throwable cause) {
    super(cause);
  }
}
