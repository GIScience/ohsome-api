package org.heigit.ohsome.ohsomeapi.output.ratio;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import org.heigit.ohsome.ohsomeapi.output.Result;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByObject;

/**
 * Represents the ratio-groupBy result JSON object containing the groupBy value and the respective
 * {@link org.heigit.ohsome.ohsomeapi.output.ratio.RatioResult RatioResult} objects. The
 * RatioGroupByResult is only used in responses for /ratio/groupBy requests.
 */
@Getter
public class RatioGroupByResult extends GroupByObject implements Result {

  @ApiModelProperty(notes = "RatioResult array holding timestamp, whole and part values",
      required = true)
  private final RatioResult[] ratioResult;

  public RatioGroupByResult(Object groupByObject, RatioResult[] ratioResult) {
    super(groupByObject);
    this.ratioResult = ratioResult;
  }

  @Override
  public double getValue() {
    return 0;
  }
}
