package org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result;

import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the result JSON object containing the timestamp together with the corresponding value.
 */
public class Result {

  @ApiModelProperty(notes = "Timestamp in the format YYYY-MM-DDThh:mm:ssZ", required = true)
  private String timestamp;
  @ApiModelProperty(notes = "Value corresponding to the filter parameters", required = true)
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
