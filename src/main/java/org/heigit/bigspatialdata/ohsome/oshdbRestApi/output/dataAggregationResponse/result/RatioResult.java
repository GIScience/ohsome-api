package org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result;

import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the result JSON object for the ratio request containing the timestamp together with
 * two values and their resulting ratio.
 */
public class RatioResult {

  @ApiModelProperty(notes = "Timestamp in the format YYYY-MM-DDThh:mm:ssZ", required = true)
  private String timestamp;
  @ApiModelProperty(notes = "Value corresponding to the initial filter parameters",
      required = true)
  private double value;
  @ApiModelProperty(notes = "Value corresponding to the second (2) filter parameters",
      required = true)
  private double value2;
  @ApiModelProperty(notes = "Ratio of value2/value", required = true)
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
