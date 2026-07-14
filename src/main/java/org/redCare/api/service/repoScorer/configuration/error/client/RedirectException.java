package org.redCare.api.service.repoScorer.configuration.error.client;

import java.io.Serial;
import lombok.Getter;

@Getter
public class RedirectException extends ClientError {
  @Serial private static final long serialVersionUID = 6154293931886437582L;
  private final String location;
  private final int httpStatusCode;

  public RedirectException(String message, int httpStatusCode, String location) {
    super(message);
    this.location = location;
    this.httpStatusCode = httpStatusCode;
  }
}
