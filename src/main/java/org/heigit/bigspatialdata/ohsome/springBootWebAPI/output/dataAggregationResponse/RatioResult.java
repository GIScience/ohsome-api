package org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse;

/**
 * Represents the result JSON object for the ratio request containing the timestamp together with
 * both values and the resulting ratio.
 */
public class RatioResult {

  private String timestamp;
  private String value;
  private String value2;
  private String ratio;

  public RatioResult(String timestamp, String value, String value2, String ratio) {
    this.timestamp = timestamp;
    this.value = value;
    this.value2 = value2;
    this.ratio = ratio;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public String getValue() {
    return value;
  }
  
  public String getValue2() {
    return value2;
  }
  
  public String getRatio() {
    return ratio;
  }
}
