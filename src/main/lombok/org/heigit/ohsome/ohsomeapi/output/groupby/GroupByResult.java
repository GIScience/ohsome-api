package org.heigit.ohsome.ohsomeapi.output.groupby;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.heigit.ohsome.ohsomeapi.output.Result;
import org.heigit.ohsome.ohsomeapi.output.elements.ElementsResult;

/**
 * Represents the groupBy result JSON object containing the groupBy value and the respective
 * {@link org.heigit.ohsome.ohsomeapi.output.Result Result} objects. The GroupByResult is only used
 * in responses for groupBy requests.
 */
@Getter
@Setter
public class GroupByResult extends GroupByObject implements Result {

  @ApiModelProperty(notes = "Result array holding timestamp-value pairs", required = true)
  private final List<Result> result;

  public GroupByResult(Object groupByName, List<ElementsResult> result) {
    super(groupByName);
    this.result = result;
  }

  @Override
  public double getValue() {
    return 0;
  }
}
