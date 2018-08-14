package org.heigit.bigspatialdata.ohsome.ohsomeApi.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.interceptor.RequestInterceptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Custom handler to modify the different exception responses. */
@ControllerAdvice
@RestController
public class CustomizedExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(BadRequestException.class)
  public final ResponseEntity<ErrorDetails> handleBadRequestException(BadRequestException ex,
      WebRequest request) {

    return createExceptionResponse(ex, request, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NotAllowedException.class)
  public final ResponseEntity<ErrorDetails> handleNotAllowedException(NotAllowedException ex,
      WebRequest request) {

    return createExceptionResponse(ex, request, HttpStatus.METHOD_NOT_ALLOWED);
  }

  @ExceptionHandler(NotFoundException.class)
  public final ResponseEntity<ErrorDetails> handleNotFoundException(NotFoundException ex,
      WebRequest request) {

    return createExceptionResponse(ex, request, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(NotImplementedException.class)
  public final ResponseEntity<ErrorDetails> handleNotImplementedException(
      NotImplementedException ex, WebRequest request) {

    return createExceptionResponse(ex, request, HttpStatus.NOT_IMPLEMENTED);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public final ResponseEntity<ErrorDetails> handleUnauthorizedException(UnauthorizedException ex,
      WebRequest request) {

    return createExceptionResponse(ex, request, HttpStatus.UNAUTHORIZED);
  }

  /** Creates the error details based on the thrown exception. */
  private ResponseEntity<ErrorDetails> createExceptionResponse(Exception ex, WebRequest request,
      HttpStatus status) {

    ErrorDetails errorDetails;
    String requestUrl = RequestInterceptor.requestUrl;
    if (ex.getMessage().equals("No message available")) {
      if (RequestInterceptor.requestUrl.split("\\?")[1].equals("null"))
        requestUrl = requestUrl.split("\\?")[0];
      errorDetails = new ErrorDetails(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
          status.value(), "Internal server error", requestUrl);
    } else {
      if (RequestInterceptor.requestUrl.split("\\?")[1].equals("null"))
        requestUrl = requestUrl.split("\\?")[0];
      errorDetails = new ErrorDetails(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
          status.value(), ex.getMessage(), requestUrl);
    }
    return new ResponseEntity<>(errorDetails, status);
  }
}
