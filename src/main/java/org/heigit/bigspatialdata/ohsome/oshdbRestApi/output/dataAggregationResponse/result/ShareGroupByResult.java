package org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the share-groupBy result JSON object containing the groupBy value and the respective
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result.ShareResult
 * ShareResult} objects. The ShareGroupByResult is only used in responses for /share/groupBy requests.
 */
@JsonInclude(Include.NON_NULL)
public class ShareGroupByResult {
  
  @ApiModelProperty(notes = "Object on which the share-results are grouped on", required = true)
  private String groupByObject;
  @ApiModelProperty(notes = "ShareResult array holding timestamp, whole and part values", required = true)
  private ShareResult[] shareResult;

  public ShareGroupByResult(String groupByObject, ShareResult[] shareResult) {
    this.groupByObject = groupByObject;
    this.shareResult = shareResult;
  }

  public String getGroupByObject() {
    return groupByObject;
  }

  public ShareResult[] getShareResult() {
    return shareResult;
  }

}
