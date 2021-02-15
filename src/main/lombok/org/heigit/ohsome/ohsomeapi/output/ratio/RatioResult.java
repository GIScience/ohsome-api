package org.heigit.ohsome.ohsomeapi.output.ratio;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.heigit.ohsome.ohsomeapi.output.Result;

/**
 * Represents the result JSON object for the /ratio resource containing the timestamp together with
 * two values and their resulting ratio.
 */
@Getter
@AllArgsConstructor
public class RatioResult implements Result {

  @ApiModelProperty(notes = "Timestamp in the format YYYY-MM-DDThh:mm:ssZ", required = true)
  private String timestamp;
  @ApiModelProperty(notes = "Value corresponding to the initial filter parameters", required = true)
  private double value;
  @ApiModelProperty(notes = "Value corresponding to the second (2) filter parameters",
      required = true)
  private double value2;
  @ApiModelProperty(notes = "Ratio of value2/value", required = true)
  private double ratio;
}
