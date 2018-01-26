package org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.groupByResponse;

import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.Result;

/**
 * Represents the groupBy result JSON object containing the groupBy value and the respective
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.Result
 * Result} objects. The GroupByResult is only used in responses for groupBy requests.
 */
public class GroupByResult {

  private String groupByObject;
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
