package org.redCare.api.service.repoScorer.configuration.error.server;



public class NotImplementedException extends ServerError {
  public NotImplementedException() {}

  public NotImplementedException(String message) {
    super(message);
  }

  public NotImplementedException(String message, Throwable cause) {
    super(message, cause);
  }

  public NotImplementedException(Throwable cause) {
    super(cause);
  }
}
