package org.heigit.ohsome.ohsomeapi.output.contributions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.heigit.ohsome.ohsomeapi.output.Result;

/**
 * Represents the result JSON object for the resources working with contributions (currently: 
 * /users and /contributions/count) containing the from timestamp together with the corresponding 
 * value.
 */
@Getter
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ContributionsResult implements Result {

  @ApiModelProperty(notes = "Timestamp in the format YYYY-MM-DDThh:mm:ssZ", required = true)
  private String fromTimestamp;
  @ApiModelProperty(notes = "Timestamp in the format YYYY-MM-DDThh:mm:ssZ", required = true)
  private String toTimestamp;
  @ApiModelProperty(notes = "Value corresponding to the filter parameters", required = true)
  private double value;
}
