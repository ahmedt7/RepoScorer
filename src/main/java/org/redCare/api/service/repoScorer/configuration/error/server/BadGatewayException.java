package org.redCare.api.service.repoScorer.configuration.error.server;


public class BadGatewayException extends ServerError {
  public BadGatewayException() {}

  public BadGatewayException(String message) {
    super(message);
  }

  public BadGatewayException(String message, Throwable cause) {
    super(message, cause);
  }

  public BadGatewayException(Throwable cause) {
    super(cause);
  }
}
