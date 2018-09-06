package org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.groupByResponse;

import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.Result;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the groupBy result JSON object containing the groupBy value and the respective
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.Result Result}
 * objects. The GroupByResult is only used in responses for groupBy requests.
 */
public class GroupByResult {

  @ApiModelProperty(notes = "Object on which the results are grouped on", required = true)
  private String groupByObject;
  @ApiModelProperty(notes = "ElementsResult array holding timestamp-value pairs", required = true)
  private Result[] result;

  public GroupByResult(String groupByObject, Result[] result) {
    this.groupByObject = groupByObject;
    this.result = result;
  }

  public String getGroupByObject() {
    return groupByObject;
  }

  public Result[] getResult() {
    return result;
  }
}
