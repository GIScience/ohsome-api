package org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse;

import io.swagger.annotations.ApiModelProperty;

/**
 * Abstract class used by the groupByResult classes:
 * <ul>
 * <li>{@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.GroupByResult
 * GroupByResult}</li>
 * <li>{@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.RatioGroupByResult
 * RatioGroupByResult}</li>
 * </ul>
 */
public abstract class GroupByObject {

  @ApiModelProperty(notes = "Object on which the results are grouped on", required = true)
  protected Object groupByObjectValue;

  public GroupByObject(Object groupByObjectValue) {
    this.groupByObjectValue = groupByObjectValue;
  }

  public Object getGroupByObject() {
    return groupByObjectValue;
  }
}
