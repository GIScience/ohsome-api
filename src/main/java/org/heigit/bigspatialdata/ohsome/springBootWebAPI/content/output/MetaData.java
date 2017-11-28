package org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output;

/**
 * Represents the meta data, which will be sent back in each JSON response.
 * @author kowatsch
 *
 */
public class MetaData {
	
	private long executionTime;
	private String unit;
	
	public MetaData(long executionTime, String unit) {
		this.executionTime = executionTime;
		this.unit = unit;
	}

	public long getExecutionTime() {
		return executionTime;
	}

	public String getUnit() {
		return unit;
	}

}
