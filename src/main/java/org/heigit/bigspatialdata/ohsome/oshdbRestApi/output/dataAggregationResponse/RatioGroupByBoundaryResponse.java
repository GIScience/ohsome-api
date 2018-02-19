package org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse;

import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.metadata.GroupByBoundaryMetadata;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result.RatioGroupByResult;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the whole JSON response object for the data aggregation response using the
 * ratio/groupBy/boundary resource. It contains an optional
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.metadata.GroupByBoundaryMetadata
 * GroupByBoundaryMetadata} object, the requested
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result.RatioGroupByResult
 * RatioGroupByResult}, which is named after the used /groupBy resource (e.g. groupByBoundaryResult for
 * using /groupBy/boundary) and an identifier of the object plus the corresponding
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result.RatioResult
 * RatioResult} objects.
 */
public class RatioGroupByBoundaryResponse {

  @ApiModelProperty(notes = "License of the included data", required = true, position = 0)
  private String license;
  @ApiModelProperty(notes = "Copyright of the used data", required = true, position = 1)
  private String copyright;
  @ApiModelProperty(notes = "Metadata describing the /groupBy/boundary output", position = 2)
  private GroupByBoundaryMetadata groupByBoundaryMetadata;
  @ApiModelProperty(notes = "RatioGroupByResult array holding the respective objects "
      + "with their timestamp-value-value2-ratio values", required = true)
  private RatioGroupByResult[] groupByBoundaryResult;

  public RatioGroupByBoundaryResponse(String license, String copyright,
      GroupByBoundaryMetadata groupByBoundaryMetadata, RatioGroupByResult[] groupByBoundaryResult) {
    this.license = license;
    this.copyright = copyright;
    this.groupByBoundaryMetadata = groupByBoundaryMetadata;
    this.groupByBoundaryResult = groupByBoundaryResult;
  }

  public String getLicense() {
    return license;
  }

  public String getCopyright() {
    return copyright;
  }

  public GroupByBoundaryMetadata getGroupByBoundaryMetadata() {
    return groupByBoundaryMetadata;
  }

  public RatioGroupByResult[] getGroupByBoundaryResult() {
    return groupByBoundaryResult;
  }
}
