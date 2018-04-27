package org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.users;

import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Result;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the result JSON object for the /users resource containing the from timestamp together
 * with the corresponding value.
 */
@JsonInclude(Include.NON_NULL)
public class UsersResult implements Result {

  @ApiModelProperty(notes = "Timestamp in the format YYYY-MM-DDThh:mm:ssZ", required = true)
  private String fromTimestamp;
  @ApiModelProperty(notes = "Timestamp in the format YYYY-MM-DDThh:mm:ssZ", required = true)
  private String toTimestamp;
  @ApiModelProperty(notes = "Value corresponding to the filter parameters", required = true)
  private double value;

  public UsersResult(String fromTimestamp, String toTimestamp, double value) {
    this.fromTimestamp = fromTimestamp;
    this.toTimestamp = toTimestamp;
    this.value = value;
  }

  public String getToTimestamp() {
    return toTimestamp;
  }

  public String getFromTimestamp() {
    return fromTimestamp;
  }

  public double getValue() {
    return value;
  }
}
