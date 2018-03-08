package org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse;

import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.metadata.GroupByBoundaryMetadata;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.result.ShareGroupByResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the whole JSON response object for the data aggregation response using the
 * share/groupBy/boundary resource. It contains an optional
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.metadata.GroupByBoundaryMetadata
 * GroupByBoundaryMetadata} object, the requested
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.result.ShareGroupByResult
 * ShareGroupByResult}, which is named after the used /groupBy resource (e.g. groupByBoundaryResult
 * for using /groupBy/boundary) and an identifier of the object plus the corresponding
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.result.ShareResult
 * ShareResult} objects.
 */
@JsonInclude(Include.NON_NULL)
public class ShareGroupByBoundaryResponse {

  @ApiModelProperty(notes = "License and copyright info", required = true, position = 0)
  private Attribution attribution;
  @ApiModelProperty(notes = "Version of this api", required = true, position = 1)
  private String apiVersion;
  @ApiModelProperty(notes = "Metadata describing the /groupBy/boundary output", position = 2)
  private GroupByBoundaryMetadata groupByBoundaryMetadata;
  @ApiModelProperty(notes = "GroupByResult array holding the respective objects "
      + "with their timestamp-whole-part values", required = true)
  private ShareGroupByResult[] shareGroupByBoundaryResult;

  public ShareGroupByBoundaryResponse(Attribution attribution, String apiVersion,
      GroupByBoundaryMetadata groupByBoundaryMetadata,
      ShareGroupByResult[] shareGroupByBoundaryResult) {
    this.attribution = attribution;
    this.apiVersion = apiVersion;
    this.groupByBoundaryMetadata = groupByBoundaryMetadata;
    this.shareGroupByBoundaryResult = shareGroupByBoundaryResult;
  }

  public Attribution getAttribution() {
    return attribution;
  }

  public String getApiVersion() {
    return apiVersion;
  }

  public GroupByBoundaryMetadata getGroupByBoundaryMetadata() {
    return groupByBoundaryMetadata;
  }

  public ShareGroupByResult[] getShareGroupByBoundaryResult() {
    return shareGroupByBoundaryResult;
  }

}
