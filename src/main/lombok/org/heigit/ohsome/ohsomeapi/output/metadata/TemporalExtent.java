package org.heigit.ohsome.ohsomeapi.output.metadata;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Represents the result JSON object containing the from- and toTimestamps of the respective
 * data-extract.
 */
@Getter
//@AllArgsConstructor
@Component
public class TemporalExtent {

  @ApiModelProperty(notes = "Timestamp in the format YYYY-MM-DDThh:mm:ssZ", required = true)
  private String fromTimestamp;
  @ApiModelProperty(notes = "Timestamp in the format YYYY-MM-DDThh:mm:ssZ", required = true)
  private String toTimestamp;
}
