package org.heigit.ohsome.ohsomeapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Exception class corresponding to the HTTP status code 413. */
@ResponseStatus(value = HttpStatus.PAYLOAD_TOO_LARGE)
public class PayloadTooLargeException extends RuntimeException {

  private static final long serialVersionUID = 3209093385286077205L;

  public PayloadTooLargeException(String message) {
    super(message);
  }
}
