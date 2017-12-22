package org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse;

/**
 * Represents the result JSON object containing the timestamp together with the corresponding value.
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
