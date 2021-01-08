package org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Abstract class used by the groupByResult classes:
 * <ul>
 * <li>{@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.GroupByResult
 * GroupByResult}</li>
 * <li>{@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.RatioGroupByResult
 * RatioGroupByResult}</li>
 * </ul>
 */
@Getter
@AllArgsConstructor
public abstract class GroupByObject {

  @ApiModelProperty(notes = "Object on which the results are grouped on", required = true)
  protected Object groupByObject;
}
