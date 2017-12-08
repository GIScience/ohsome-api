package org.heigit.bigspatialdata.ohsome.springBootWebAPI.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception class corresponding to the HTTP status code 405.
 *
 */
@ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED)
public class NotAllowedException extends RuntimeException {

	private static final long serialVersionUID = -4222709148776556662L;

	public NotAllowedException(String message) {
		super(message);
	}
}
