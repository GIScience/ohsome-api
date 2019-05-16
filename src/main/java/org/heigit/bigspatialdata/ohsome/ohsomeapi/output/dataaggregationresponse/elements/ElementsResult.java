package org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.elements;

import io.swagger.annotations.ApiModelProperty;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Result;


/**
 * Represents the result JSON object for most of the /elements resources containing the timestamp
 * together with the corresponding value.
 */
public class ElementsResult implements Result {

  @ApiModelProperty(notes = "Timestamp in the format YYYY-MM-DDThh:mm:ssZ", required = true)
  private String timestamp;
  @ApiModelProperty(notes = "Value corresponding to the filter parameters", required = true)
  private double value;

  public ElementsResult(String timestamp, double value) {
    this.timestamp = timestamp;
    this.value = value;
  }

  public String getTimestamp() {
    return timestamp;
  }

  @Override
  public double getValue() {
    return value;
  }
}
