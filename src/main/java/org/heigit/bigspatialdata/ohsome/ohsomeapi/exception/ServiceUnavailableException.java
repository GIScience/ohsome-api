package org.heigit.bigspatialdata.ohsome.ohsomeapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Exception class corresponding to the HTTP status code 503. */
@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class ServiceUnavailableException extends RuntimeException {

  private static final long serialVersionUID = 1250019723628732922L;

  public ServiceUnavailableException(String message) {
    super(message);
  }
}
