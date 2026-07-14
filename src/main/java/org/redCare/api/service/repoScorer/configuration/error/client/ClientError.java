package org.redCare.api.service.repoScorer.configuration.error.client;

public abstract class ClientError extends RuntimeException {
  protected ClientError() {}

  protected ClientError(String message) {
    super(message);
  }

  protected ClientError(String message, Throwable cause) {
    super(message, cause);
  }

  protected ClientError(Throwable cause) {
    super(cause);
  }
}
