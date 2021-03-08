package org.heigit.ohsome.ohsomeapi.output.groupby;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import org.heigit.ohsome.ohsomeapi.output.Result;

/**
 * Represents the groupBy result JSON object containing the groupBy value and the respective
 * {@link org.heigit.ohsome.ohsomeapi.output.Result Result} objects. The GroupByResult is only used 
 * in responses for groupBy requests.
 */
@Getter
public class GroupByResult extends GroupByObject {

  @ApiModelProperty(notes = "Result array holding timestamp-value pairs", required = true)
  private Result[] result;

  public GroupByResult(Object groupByName, Result[] result) {
    super(groupByName);
    this.result = result;
  }
}
