package org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.elements;

import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Attribution;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Metadata;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the whole JSON response object for the data aggregation response using the
 * ratio/groupBy/boundary resource. It contains an optional
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Metadata
 * Metadata} object, the requested
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.elements.RatioGroupByResult
 * RatioGroupByResult}, which is named after the used /groupBy resource (e.g. groupByBoundaryResult
 * for using /groupBy/boundary) and an identifier of the object plus the corresponding
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.elements.RatioResult
 * RatioResult} objects.
 */
public class RatioGroupByBoundaryResponse {

  @ApiModelProperty(notes = "License and copyright info", required = true, position = 0)
  private Attribution attribution;
  @ApiModelProperty(notes = "Version of this api", required = true, position = 1)
  private String apiVersion;
  @ApiModelProperty(notes = "Metadata describing the output", position = 2)
  private Metadata metadata;
  @ApiModelProperty(notes = "RatioGroupByResult array holding the respective objects "
      + "with their timestamp-value-value2-ratio values", required = true)
  private RatioGroupByResult[] groupByBoundaryResult;

  public RatioGroupByBoundaryResponse(Attribution attribution, String apiVersion, Metadata metadata,
      RatioGroupByResult[] groupByBoundaryResult) {
    this.attribution = attribution;
    this.apiVersion = apiVersion;
    this.metadata = metadata;
    this.groupByBoundaryResult = groupByBoundaryResult;
  }

  public Attribution getAttribution() {
    return attribution;
  }

  public String getApiVersion() {
    return apiVersion;
  }

  public Metadata getMetadata() {
    return metadata;
  }

  public RatioGroupByResult[] getGroupByBoundaryResult() {
    return groupByBoundaryResult;
  }
}
