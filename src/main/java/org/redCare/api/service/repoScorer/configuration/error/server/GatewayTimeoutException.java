package org.redCare.api.service.repoScorer.configuration.error.server;


public class GatewayTimeoutException extends ServerError {
  public GatewayTimeoutException() {}

  public GatewayTimeoutException(String message) {
    super(message);
  }

  public GatewayTimeoutException(String message, Throwable cause) {
    super(message, cause);
  }

  public GatewayTimeoutException(Throwable cause) {
    super(cause);
  }
}
