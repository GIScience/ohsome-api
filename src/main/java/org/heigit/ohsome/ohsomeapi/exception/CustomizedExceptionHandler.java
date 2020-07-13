package org.heigit.ohsome.ohsomeapi.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.servlet.http.HttpServletRequest;
import org.heigit.bigspatialdata.oshdb.util.exceptions.OSHDBTimeoutException;
import org.heigit.ohsome.ohsomeapi.utils.RequestUtils;
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
  public final ResponseEntity<ErrorDetails> handleBadRequestException(BadRequestException ex,
      HttpServletRequest servletRequest) {
    return createExceptionResponse(ex, HttpStatus.BAD_REQUEST, servletRequest);
  }

  @ExceptionHandler(NotAllowedException.class)
  public final ResponseEntity<ErrorDetails> handleNotAllowedException(NotAllowedException ex,
      HttpServletRequest servletRequest) {
    return createExceptionResponse(ex, HttpStatus.METHOD_NOT_ALLOWED, servletRequest);
  }

  @ExceptionHandler(NotFoundException.class)
  public final ResponseEntity<ErrorDetails> handleNotFoundException(NotFoundException ex,
      HttpServletRequest servletRequest) {
    return createExceptionResponse(ex, HttpStatus.NOT_FOUND, servletRequest);
  }

  @ExceptionHandler(PayloadTooLargeException.class)
  public final ResponseEntity<ErrorDetails> handlePayloadTooLargeException(
      PayloadTooLargeException ex, HttpServletRequest servletRequest) {
    return createExceptionResponse(ex, HttpStatus.PAYLOAD_TOO_LARGE, servletRequest);
  }

  @ExceptionHandler(NotImplementedException.class)
  public final ResponseEntity<ErrorDetails> handleNotImplementedException(
      NotImplementedException ex, HttpServletRequest servletRequest) {
    return createExceptionResponse(ex, HttpStatus.NOT_IMPLEMENTED, servletRequest);
  }

  @ExceptionHandler(UnauthorizedRequestException.class)
  public final ResponseEntity<ErrorDetails> handleUnauthorizedException(
      UnauthorizedRequestException ex, HttpServletRequest servletRequest) {
    return createExceptionResponse(ex, HttpStatus.UNAUTHORIZED, servletRequest);
  }

  @ExceptionHandler(OSHDBTimeoutException.class)
  public final ResponseEntity<ErrorDetails> handleTimeoutException(
      HttpServletRequest servletRequest) {
    return createExceptionResponse(
        new PayloadTooLargeException(ExceptionMessages.PAYLOAD_TOO_LARGE),
        HttpStatus.PAYLOAD_TOO_LARGE, servletRequest);
  }

  /**
   * Creates the error details based on the thrown exception. It adds "null" to the request URL
   * (e.g. /users/count?null), in case of a request without parameters but with the query question
   * mark right after the endpoint (e.g. /users/count?).
   */
  private ResponseEntity<ErrorDetails> createExceptionResponse(Exception ex, HttpStatus status,
      HttpServletRequest servletRequest) {
    ErrorDetails errorDetails;
    String servletRequestUrl = RequestUtils.extractRequestUrl(servletRequest);
    if (servletRequestUrl.endsWith("?")) {
      servletRequestUrl += "null";
    }
    if ("No message available".equals(ex.getMessage())) {
      if ("null".equals(servletRequestUrl.split("\\?")[1])) {
        servletRequestUrl = servletRequestUrl.split("\\?")[0];
      }
      errorDetails = new ErrorDetails(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
          status.value(), "Internal server error", servletRequestUrl);
    } else {
      if ("null".equals(servletRequestUrl.split("\\?")[1])) {
        servletRequestUrl = servletRequestUrl.split("\\?")[0];
      }
      errorDetails = new ErrorDetails(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
          status.value(), ex.getMessage(), servletRequestUrl);
    }
    return new ResponseEntity<>(errorDetails, status);
  }
}
