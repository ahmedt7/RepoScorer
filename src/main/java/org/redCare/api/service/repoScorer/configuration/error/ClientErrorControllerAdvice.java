package org.redCare.api.service.repoScorer.configuration.error;

import static java.util.stream.Collectors.joining;
import static org.springframework.util.StringUtils.hasText;


import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redCare.api.service.repoScorer.configuration.error.client.BadRequestException;
import org.redCare.api.service.repoScorer.configuration.error.client.ClientError;
import org.redCare.api.service.repoScorer.configuration.error.client.ForbiddenException;
import org.redCare.api.service.repoScorer.configuration.error.client.GenericClientErrorException;
import org.redCare.api.service.repoScorer.configuration.error.client.NotFoundException;
import org.redCare.api.service.repoScorer.configuration.error.client.UnauthorizedException;
import org.redCare.api.service.repoScorer.configuration.error.model.BasicError;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
@Order(2)
@Slf4j
public class ClientErrorControllerAdvice {
  // 400
  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<List<BasicError>> handleBadRequestException(BadRequestException ex) {
    log.info(ex.getMessage(), ex);
    HttpStatus status = HttpStatus.BAD_REQUEST;
    BasicError basicError = basicError(status, ex);
    return new ResponseEntity<>(List.of(basicError), status);
  }

  // 400
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<List<BasicError>> handleConstraintViolationException(
      ConstraintViolationException ex) {
    log.info(ex.getMessage(), ex);
    HttpStatus status = HttpStatus.BAD_REQUEST;
    BasicError basicError = new BasicError(ex.getMessage());
    return new ResponseEntity<>(List.of(basicError), status);
  }

  // 400
  @ExceptionHandler(BindException.class)
  public ResponseEntity<List<BasicError>> handleRequestBodyException(BindException ex) {
    log.info(ex.getMessage(), ex);
    List<BasicError> errors = new ArrayList<>();
    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
      BasicError error = null;
      if (fieldError.contains(TypeMismatchException.class)) {
        TypeMismatchException tme = fieldError.unwrap(TypeMismatchException.class);
        Class<?> requiredType = tme.getRequiredType();
        if (tme.getCause() instanceof ConversionFailedException conversionFailedException) {
          requiredType = (conversionFailedException.getTargetType()).getType();
        }
        if (requiredType != null) {
          error = new BasicError();
          setMessageForTypeMismatch(tme.getPropertyName(), error, requiredType);
        }
      }
      errors.add(
          Objects.requireNonNullElseGet(
              error,
              () ->
                  new BasicError(
                      "Validation failed for field '%s': %s"
                          .formatted(fieldError.getField(), fieldError.getDefaultMessage()))));
    }
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  // 400
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<List<BasicError>> handleMissingParams(
      MissingServletRequestParameterException ex) {
    log.info(ex.getMessage(), ex);
    BasicError basicError =
        new BasicError(String.format("%s parameter is missing", ex.getParameterName()));
    return new ResponseEntity<>(List.of(basicError), HttpStatus.BAD_REQUEST);
  }

  // 400
  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<List<BasicError>> handleMissingHeaders(MissingRequestHeaderException ex) {
    log.info(ex.getMessage(), ex);
    String message = String.format("%s header is missing", ex.getHeaderName());
    BasicError basicError = new BasicError(message);
    return new ResponseEntity<>(List.of(basicError), HttpStatus.BAD_REQUEST);
  }

  // 400
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<List<BasicError>> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex) {
    log.info(ex.getMessage(), ex);
    String message = ex.getMessage();
    if (ex.getCause() instanceof InvalidFormatException invalidFormatException
        && invalidFormatException.getTargetType() != null
        && invalidFormatException.getTargetType().isEnum()) {
      message =
          String.format(
              "Unexpected value '%s' for the field: '%s'. The value must be one of: %s",
              invalidFormatException.getValue(),
              invalidFormatException
                  .getPath()
                  .get(invalidFormatException.getPath().size() - 1)
                  .getFieldName(),
              Arrays.toString(invalidFormatException.getTargetType().getEnumConstants()));
    } else if (ex.getCause() instanceof ValueInstantiationException valueInstantiationException) {
      message = valueInstantiationException.getCause().getMessage();
    }
    BasicError basicError = new BasicError(message);
    return new ResponseEntity<>(List.of(basicError), HttpStatus.BAD_REQUEST);
  }

  // 400
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<List<BasicError>> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException ex) {
    log.info(ex.getMessage(), ex);
    BasicError basicError = new BasicError();
    Class<?> requiredType = ex.getRequiredType();
    if (ex.getCause() instanceof ConversionFailedException conversionFailedException) {
      requiredType = conversionFailedException.getTargetType().getType();
    }
    setMessageForTypeMismatch(ex.getName(), basicError, requiredType);
    return new ResponseEntity<>(List.of(basicError), HttpStatus.BAD_REQUEST);
  }

  // 401
  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<BasicError> handle(UnauthorizedException ex) {
    log.warn("Unauthorized request", ex);
    HttpStatus status = HttpStatus.UNAUTHORIZED;
    BasicError basicError = basicError(status, ex);
    return new ResponseEntity<>(basicError, status);
  }

  // 403
  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<BasicError> handleForbiddenException(ForbiddenException ex) {
    return createForbiddenResponseEntity(ex);
  }

  // 403
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<BasicError> handleForbiddenException(AccessDeniedException ex) {
    return createForbiddenResponseEntity(ex);
  }

  private @NotNull ResponseEntity<BasicError> createForbiddenResponseEntity(RuntimeException ex) {
    log.warn("Access forbidden", ex);
    HttpStatus status = HttpStatus.FORBIDDEN;
    BasicError basicError = basicError(status, ex);
    return new ResponseEntity<>(basicError, status);
  }

  // 404
  @ExceptionHandler(NoHandlerFoundException.class)
  protected ResponseEntity<BasicError> handleNoHandlerFoundException(NoHandlerFoundException ex) {
    log.error(
        String.format("No handler found for %s %s", ex.getHttpMethod(), ex.getRequestURL()), ex);
    HttpStatus status = HttpStatus.NOT_FOUND;
    BasicError basicError = basicError(status, ex);
    return new ResponseEntity<>(basicError, status);
  }

  // 404
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<BasicError> handleNotFoundException(NotFoundException ex) {
    log.info(ex.getMessage(), ex);
    HttpStatus status = HttpStatus.NOT_FOUND;
    BasicError basicError = basicError(status, ex);
    return new ResponseEntity<>(basicError, status);
  }

  // 405
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
  public void handleMethodNotSupportedException() {
    // return standard 405 error code message though annotation
  }

  // 408
  @ExceptionHandler(TimeoutException.class)
  @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
  public void handleTimeoutException() {
    // return standard 408 error code message though annotation
  }

  // 415
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<List<BasicError>> handleUnSupportedMediaTypeException(
      HttpMediaTypeNotSupportedException ex) {
    String message =
        "%s media type is not supported. Supported media types are %s"
            .formatted(
                ex.getContentType(),
                ex.getSupportedMediaTypes().stream()
                    .map(MediaType::toString)
                    .collect(joining(", ")));
    BasicError basicError = new BasicError(message);
    return new ResponseEntity<>(List.of(basicError), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
  }

  // 429
  @ExceptionHandler(RequestNotPermitted.class)
  @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
  public void handleRequestNotPermitted() {
    // return standard 429 error code message though annotation
  }

  // 4xx
  @ExceptionHandler(GenericClientErrorException.class)
  public ResponseEntity<BasicError> handleGenericClientErrorException(
      GenericClientErrorException ex) {
    log.info(ex.getMessage(), ex);
    HttpStatus status = ex.status();
    BasicError basicError = basicError(status, ex);
    return new ResponseEntity<>(basicError, status);
  }

  void setMessageForTypeMismatch(String paramName, BasicError basicError, Class<?> type) {
    if (type != null) {
      if (type.isEnum()) {
        basicError.setMessage(
            "The parameter %s must have a value among : %s"
                .formatted(paramName, StringUtils.join(type.getEnumConstants(), ", ")));
      } else {
        basicError.setMessage(
            "The parameter %s must be of type %s".formatted(paramName, type.getTypeName()));
        if (type.getTypeName().equals("java.time.LocalDate")) {
          basicError.setMessage(
              "%s. Date should be a valid ISO 8601 10 letter date."
                  .formatted(basicError.getMessage()));
        }
      }
    }
  }

  private BasicError basicError(HttpStatus status, Exception ex) {
    String message = hideErrorMessage(ex) ? status.getReasonPhrase() : ex.getMessage();
    return new BasicError(message);
  }

  private boolean hideErrorMessage(Exception ex) {
    return !(ex instanceof ClientError) || !hasText(ex.getMessage());
  }
}
