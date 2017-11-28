package org.heigit.bigspatialdata.ohsome.springBootWebAPI.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception class corresponding to the HTTP status code 501.
 * @author kowatsch
 *
 */
@ResponseStatus(value = HttpStatus.NOT_IMPLEMENTED)
public class NotImplementedException extends RuntimeException {
	
	private static final long serialVersionUID = -7303209315659028808L;

	public NotImplementedException(String message) {
		super(message);
	}

}
