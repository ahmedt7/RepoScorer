package org.redCare.api.service.repoScorer.configuration.error.server;


public class InternalServerErrorException extends ServerError {
  public InternalServerErrorException() {}

  public InternalServerErrorException(String message) {
    super(message);
  }

  public InternalServerErrorException(String message, Throwable cause) {
    super(message, cause);
  }

  public InternalServerErrorException(Throwable cause) {
    super(cause);
  }
}
