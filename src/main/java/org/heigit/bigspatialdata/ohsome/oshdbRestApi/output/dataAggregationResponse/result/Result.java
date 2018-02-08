package org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result;

/**
 * Represents the result JSON object containing the timestamp together with the corresponding value.
 */
public class Result {

  private String timestamp;
  private double value;

  public Result(String timestamp, double value) {
    this.timestamp = timestamp;
    this.value = value;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public double getValue() {
    return value;
  }
}
