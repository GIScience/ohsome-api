package org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.elements;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Result;


/**
 * Represents the result JSON object for most of the /elements resources containing the timestamp
 * together with the corresponding value.
 */
@Getter
@AllArgsConstructor
public class ElementsResult implements Result {

  @ApiModelProperty(notes = "Timestamp in the format YYYY-MM-DDThh:mm:ssZ", required = true)
  private String timestamp;
  @ApiModelProperty(notes = "Value corresponding to the filter parameters", required = true)
  private double value;
}
