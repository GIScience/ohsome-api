package org.heigit.bigspatialdata.ohsome.ohsomeapi.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.interceptor.RequestInterceptor;
import org.heigit.bigspatialdata.oshdb.util.exceptions.OSHDBTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Custom handler to modify the different exception responses. */
@ControllerAdvice
@RestController
public class CustomizedExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(BadRequestException.class)
  public final ResponseEntity<ErrorDetails> handleBadRequestException(BadRequestException ex) {
    return createExceptionResponse(ex, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NotAllowedException.class)
  public final ResponseEntity<ErrorDetails> handleNotAllowedException(NotAllowedException ex) {
    return createExceptionResponse(ex, HttpStatus.METHOD_NOT_ALLOWED);
  }

  @ExceptionHandler(NotFoundException.class)
  public final ResponseEntity<ErrorDetails> handleNotFoundException(NotFoundException ex) {
    return createExceptionResponse(ex, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(PayloadTooLargeException.class)
  public final ResponseEntity<ErrorDetails> handlePayloadTooLargeException(
      PayloadTooLargeException ex) {
    return createExceptionResponse(ex, HttpStatus.PAYLOAD_TOO_LARGE);
  }

  @ExceptionHandler(NotImplementedException.class)
  public final ResponseEntity<ErrorDetails> handleNotImplementedException(
      NotImplementedException ex) {
    return createExceptionResponse(ex, HttpStatus.NOT_IMPLEMENTED);
  }

  @ExceptionHandler(UnauthorizedRequestException.class)
  public final ResponseEntity<ErrorDetails> handleUnauthorizedException(
      UnauthorizedRequestException ex) {
    return createExceptionResponse(ex, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(OSHDBTimeoutException.class)
  public final ResponseEntity<ErrorDetails> handleTimeoutException() {
    return createExceptionResponse(
        new PayloadTooLargeException(ExceptionMessages.PAYLOAD_TOO_LARGE),
        HttpStatus.PAYLOAD_TOO_LARGE);
  }

  /** Creates the error details based on the thrown exception. */
  private ResponseEntity<ErrorDetails> createExceptionResponse(Exception ex, HttpStatus status) {
    ErrorDetails errorDetails;
    String requestUrl = RequestInterceptor.requestUrl;
    if (ex.getMessage().equals("No message available")) {
      if (RequestInterceptor.requestUrl.split("\\?")[1].equals("null")) {
        requestUrl = requestUrl.split("\\?")[0];
      }
      errorDetails = new ErrorDetails(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
          status.value(), "Internal server error", requestUrl);
    } else {
      if (RequestInterceptor.requestUrl.split("\\?")[1].equals("null")) {
        requestUrl = requestUrl.split("\\?")[0];
      }
      errorDetails = new ErrorDetails(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
          status.value(), ex.getMessage(), requestUrl);
    }
    return new ResponseEntity<>(errorDetails, status);
  }
}
