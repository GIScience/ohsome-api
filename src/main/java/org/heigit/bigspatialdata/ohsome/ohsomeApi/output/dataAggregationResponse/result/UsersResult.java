package org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.result;

import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the result JSON object for the /users resource containing the from timestamp together
 * with the corresponding value.
 */
public class UsersResult {

  @ApiModelProperty(notes = "Timestamp in the format YYYY-MM-DDThh:mm:ssZ", required = true)
  private String fromTimestamp;
  @ApiModelProperty(notes = "Value corresponding to the filter parameters", required = true)
  private double value;

  public UsersResult(String fromTimestamp, double value) {
    this.fromTimestamp = fromTimestamp;
    this.value = value;
  }

  public String getFromTimestamp() {
    return fromTimestamp;
  }


  public double getValue() {
    return value;
  }
}
