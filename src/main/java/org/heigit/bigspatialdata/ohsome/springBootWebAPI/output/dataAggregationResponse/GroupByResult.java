package org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse;

/**
 * Represents the groupBy result JSON object containing the groupBy value and the respective
 * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.Result
 * Result} objects. The GroupByResult is only used in responses for groupBy requests and null in
 * case of other requests.
 *
 */
public class GroupByResult {

  private String groupByObj;
  private Result[] result;

  public GroupByResult(String groupByObj, Result[] result) {
    this.groupByObj = groupByObj;
    this.result = result;
  }

  public String getGroupByObj() {
    return groupByObj;
  }

  public Result[] getResult() {
    return result;
  }
}
