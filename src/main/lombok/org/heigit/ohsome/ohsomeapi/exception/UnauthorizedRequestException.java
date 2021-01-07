package org.heigit.ohsome.ohsomeapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Exception class corresponding to the HTTP status code 401. */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnauthorizedRequestException extends RuntimeException {

  private static final long serialVersionUID = -3021015133062599721L;

  public UnauthorizedRequestException(String message) {
    super(message);
  }
}
