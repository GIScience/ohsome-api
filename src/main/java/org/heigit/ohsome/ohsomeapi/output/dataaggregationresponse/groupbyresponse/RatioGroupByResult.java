package org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse;

import io.swagger.annotations.ApiModelProperty;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.RatioResult;
import lombok.Getter;

/**
 * Represents the ratio-groupBy result JSON object containing the groupBy value and the respective
 * {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.RatioResult RatioResult}
 * objects. The RatioGroupByResult is only used in responses for /ratio/groupBy requests.
 */
@Getter
public class RatioGroupByResult extends GroupByObject {

  @ApiModelProperty(notes = "RatioResult array holding timestamp, whole and part values",
      required = true)
  private RatioResult[] ratioResult;

  public RatioGroupByResult(Object groupByObjectId, RatioResult[] ratioResult) {
    super(groupByObjectId);
    this.ratioResult = ratioResult;
  }
}
