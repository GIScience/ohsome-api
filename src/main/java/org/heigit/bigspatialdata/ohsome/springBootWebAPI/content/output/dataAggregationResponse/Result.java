package org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.dataAggregationResponse;

/**
 * Second (or third in case of groupBy requests) level response content for the data aggregation requests. It holds the
 * timestamp together with the corresponding value.
 *
 */
public class Result {

	private String timestamp;
	private String value;

	public Result(String timestamp, String value) {
		this.timestamp = timestamp;
		this.value = value;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String getValue() {
		return value;
	}
}
