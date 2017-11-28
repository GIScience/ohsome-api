package org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.input;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents the bounding-point parameter within POST requests.
 *
 */
@JsonInclude(Include.NON_NULL) // needed to exclude NULL objects from the result
public class BPoint {
	
	private String id;
	private String[] bpoint;
	
	public BPoint(String id, String[] bpoint) {
		this.id = id;
		this.bpoint = bpoint;
	}
	
	/**
	 * Empty dummy constructor (needed for Jackson).
	 */
	public BPoint() {
		
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String[] getBpoint() {
		return bpoint;
	}
}
