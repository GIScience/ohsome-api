package org.heigit.ohsome.ohsomeapi.output.ratio;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByObject;

/**
 * Represents the ratio-groupBy result JSON object containing the groupBy value and the respective
 * {@link org.heigit.ohsome.ohsomeapi.output.ratio.RatioResult RatioResult}
 * objects. The RatioGroupByResult is only used in responses for /ratio/groupBy requests.
 */
@Getter
public class RatioGroupByResult extends GroupByObject {

  @ApiModelProperty(notes = "RatioResult array holding timestamp, whole and part values",
      required = true)
  private RatioResult[] result;

  public RatioGroupByResult(Object groupByObject, RatioResult[] result) {
    super(groupByObject);
    this.result = result;
  }
}
