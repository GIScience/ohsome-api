package org.heigit.ohsome.ohsomeapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Exception class corresponding to the HTTP status code 500. */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class DatabaseAccessException extends RuntimeException {

  private static final long serialVersionUID = 8495028398832091172L;

  public DatabaseAccessException(String message) {
    super(message);
  }
}
