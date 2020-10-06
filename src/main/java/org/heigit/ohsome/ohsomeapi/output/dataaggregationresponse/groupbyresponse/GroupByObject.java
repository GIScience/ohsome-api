package org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse;

import io.swagger.annotations.ApiModelProperty;
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
public abstract class GroupByObject {

  @ApiModelProperty(notes = "Object on which the results are grouped on", required = true)
  protected Object groupByObject;

  public GroupByObject(Object groupByObject) {
    this.groupByObject = groupByObject;
  }
}
