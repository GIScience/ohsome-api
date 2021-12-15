package org.heigit.ohsome.ohsomeapi.output.elements;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ohsome.ohsomeapi.output.Result;

/**
 * Represents the result JSON object for most of the /elements resources containing the timestamp
 * together with the corresponding value.
 */
@Getter
@Setter
@AllArgsConstructor
public class ElementsResult implements Result {

  @ApiModelProperty(notes = "Timestamp in the format YYYY-MM-DDThh:mm:ssZ", required = true)
  private final String timestamp;
  @ApiModelProperty(notes = "Value corresponding to the filter parameters", required = true)
  private final double value;
}
