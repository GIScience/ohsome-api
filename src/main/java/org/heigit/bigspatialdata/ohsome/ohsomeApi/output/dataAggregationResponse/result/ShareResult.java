package org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the result JSON object for the share request containing the timestamp together with
 * the whole and the part(ial) value.
 */
@JsonInclude(Include.NON_NULL)
public class ShareResult {

  @ApiModelProperty(notes = "Timestamp in the format YYYY-MM-DDThh:mm:ssZ", required = true)
  private String timestamp;
  @ApiModelProperty(notes = "Whole value corresponding to the initial filter parameters",
      required = true)
  private double whole;
  @ApiModelProperty(notes = "Part(ial) value corresponding to the keys2 & values2 parameters",
      required = true)
  private double part;

  public ShareResult(String timestamp, double whole, double part) {
    this.timestamp = timestamp;
    this.whole = whole;
    this.part = part;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public double getWhole() {
    return whole;
  }

  public double getPart() {
    return part;
  }
}
