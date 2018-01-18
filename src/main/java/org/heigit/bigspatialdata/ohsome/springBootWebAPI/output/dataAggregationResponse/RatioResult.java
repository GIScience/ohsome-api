package org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse;

/**
 * Represents the result JSON object for the ratio request containing the timestamp together with
 * two values and their resulting ratio.
 */
public class RatioResult {

  private String timestamp;
  private double value;
  private double value2;
  private double ratio;

  public RatioResult(String timestamp, double value, double value2, double ratio) {
    this.timestamp = timestamp;
    this.value = value;
    this.value2 = value2;
    this.ratio = ratio;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public double getValue() {
    return value;
  }
  
  public double getValue2() {
    return value2;
  }
  
  public double getRatio() {
    return ratio;
  }
}
