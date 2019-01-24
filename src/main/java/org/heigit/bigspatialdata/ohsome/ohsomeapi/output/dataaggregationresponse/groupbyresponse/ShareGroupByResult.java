package org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse;

import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.elements.ShareResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;


/**
 * Represents the share-groupBy result JSON object containing the groupBy value and the respective
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.elements.ShareResult
 * ShareResult} objects. The ShareGroupByResult is only used in responses for /share/groupBy
 * requests.
 */
@JsonInclude(Include.NON_NULL)
public class ShareGroupByResult extends GroupByObject{

  @ApiModelProperty(notes = "ShareResult array holding timestamp, whole and part values",
      required = true)
  private ShareResult[] shareResult;

  public ShareGroupByResult(Object groupByObject, ShareResult[] shareResult) {
    super(groupByObject);
    this.shareResult = shareResult;
  }

  public ShareResult[] getShareResult() {
    return shareResult;
  }
}
