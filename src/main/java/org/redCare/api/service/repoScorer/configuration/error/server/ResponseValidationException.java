package org.redCare.api.service.repoScorer.configuration.error.server;



public class ResponseValidationException extends ServerError {
  public ResponseValidationException() {}

  public ResponseValidationException(String message) {
    super(message);
  }

  public ResponseValidationException(String message, Throwable cause) {
    super(message, cause);
  }

  public ResponseValidationException(Throwable cause) {
    super(cause);
  }
}
