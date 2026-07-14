package org.redCare.api.service.repoScorer.configuration.error.server;

public abstract class ServerError extends RuntimeException {
  protected ServerError() {}

  protected ServerError(String message) {
    super(message);
  }

  protected ServerError(String message, Throwable cause) {
    super(message, cause);
  }

  protected ServerError(Throwable cause) {
    super(cause);
  }
}
