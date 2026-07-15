package org.redCare.api.service.repoScorer.configuration.error;

import static org.springframework.http.HttpHeaders.RETRY_AFTER;
import static org.springframework.util.StringUtils.hasText;


import feign.RetryableException;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.redCare.api.service.repoScorer.configuration.error.model.BasicError;
import org.redCare.api.service.repoScorer.configuration.error.server.BadGatewayException;
import org.redCare.api.service.repoScorer.configuration.error.server.GatewayTimeoutException;
import org.redCare.api.service.repoScorer.configuration.error.server.InternalServerErrorException;
import org.redCare.api.service.repoScorer.configuration.error.server.NotImplementedException;
import org.redCare.api.service.repoScorer.configuration.error.server.ResponseValidationException;
import org.redCare.api.service.repoScorer.configuration.error.server.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Order(3)
@Slf4j
public class ServerErrorControllerAdvice {
  private final String activeProfiles;

  public ServerErrorControllerAdvice(
      @Value("${spring.profiles.active:UNKNOWN}") String activeProfiles) {
    this.activeProfiles = activeProfiles;
  }

  // 500
  @ExceptionHandler(Exception.class)
  public ResponseEntity<BasicError> handleUnhandledException(Exception ex) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    String message =
        "Unhandled %s was thrown and results in an %s response"
            .formatted(ex.getClass().getSimpleName(), status.name());
    log.error(message, ex);
    BasicError basicError = basicError(status, ex);
    return new ResponseEntity<>(basicError, status);
  }

  // 500
  @ExceptionHandler({InternalServerErrorException.class, ResponseValidationException.class})
  public ResponseEntity<BasicError> handleInternalServerErrorException(Exception ex) {
    log.error(ex.getMessage(), ex);
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    BasicError basicError = basicError(status, ex);
    return new ResponseEntity<>(basicError, status);
  }

  // 501
  @ExceptionHandler(NotImplementedException.class)
  public ResponseEntity<BasicError> handleNotImplementedException(NotImplementedException ex) {
    log.warn("Unimplemented code was called", ex);
    HttpStatus status = HttpStatus.NOT_IMPLEMENTED;
    BasicError basicError = basicError(status, ex);
    return new ResponseEntity<>(basicError, status);
  }

  // 502
  @ExceptionHandler(BadGatewayException.class)
  public ResponseEntity<BasicError> handleBadGatewayException(BadGatewayException ex) {
    log.warn(ex.getMessage(), ex);
    HttpStatus status = HttpStatus.BAD_GATEWAY;
    BasicError basicError = basicError(status, ex);
    return new ResponseEntity<>(basicError, status);
  }

  // 503
  @ExceptionHandler(ServiceUnavailableException.class)
  public ResponseEntity<BasicError> handleServiceUnavailableException(
      ServiceUnavailableException ex) {
    log.warn(ex.getMessage(), ex);
    HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
    BasicError basicError = basicError(status, ex);
    if (ex.getRetryAfter() != null) {
      return ResponseEntity.status(status)
          .headers(headers -> headers.add(RETRY_AFTER, String.valueOf(ex.getRetryAfter())))
          .body(basicError);
    }
    return new ResponseEntity<>(basicError, status);
  }

  // 504
  @ExceptionHandler(GatewayTimeoutException.class)
  public ResponseEntity<BasicError> handleGatewayTimeoutException(GatewayTimeoutException ex) {
    log.warn(ex.getMessage(), ex);
    HttpStatus status = HttpStatus.GATEWAY_TIMEOUT;
    BasicError basicError = basicError(status, ex);
    return new ResponseEntity<>(basicError, status);
  }

  private BasicError basicError(HttpStatus status, Exception ex) {
    String message = hideErrorMessage(ex) ? status.getReasonPhrase() : ex.getMessage();
    return new BasicError(message);
  }

  private boolean hideErrorMessage(Exception ex) {
    return activeProfiles.toLowerCase().contains("prod") || !hasText(ex.getMessage());
  }
}
