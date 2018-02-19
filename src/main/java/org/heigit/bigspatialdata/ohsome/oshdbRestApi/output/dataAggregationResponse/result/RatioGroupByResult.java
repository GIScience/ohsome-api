package org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result;

import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the ratio-groupBy result JSON object containing the groupBy value and the respective
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result.RatioResult
 * RatioResult} objects. The RatioGroupByResult is only used in responses for /ratio/groupBy requests.
 */
public class RatioGroupByResult {
  
  @ApiModelProperty(notes = "Object on which the ratio-results are grouped on", required = true)
  private String groupByObject;
  @ApiModelProperty(notes = "RatioResult array holding timestamp, whole and part values", required = true)
  private RatioResult[] ratioResult;

  public RatioGroupByResult(String groupByObject, RatioResult[] ratioResult) {
    this.groupByObject = groupByObject;
    this.ratioResult = ratioResult;
  }

  public String getGroupByObject() {
    return groupByObject;
  }

  public RatioResult[] getRatioResult() {
    return ratioResult;
  }

}
