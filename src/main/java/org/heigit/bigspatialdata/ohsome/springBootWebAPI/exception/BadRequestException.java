package org.heigit.bigspatialdata.ohsome.springBootWebAPI.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception class corresponding to the HTTP status code 400. It gives an informative exception
 * message depending on the error.
 *
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

  private static final long serialVersionUID = 5447236881796827663L;

  public BadRequestException(String message) {
    super(message);
  }

}
