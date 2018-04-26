package org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.elements;

import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Attribution;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Metadata;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the whole JSON response object for the data aggregation response using the
 * count|length|area|perimeter/groupBy/ resource. It contains an optional
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Metadata
 * Metadata}, the requested
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.elements.GroupByResult
 * GroupByResult} and an identifier of the object plus the corresponding
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.elements.Result
 * Result} objects.
 */
@JsonInclude(Include.NON_NULL)
public class GroupByResponse {

  @ApiModelProperty(notes = "License and copyright info", required = true, position = 0)
  private Attribution attribution;
  @ApiModelProperty(notes = "Version of this api", required = true, position = 1)
  private String apiVersion;
  @ApiModelProperty(notes = "Metadata describing the output", position = 2)
  private Metadata metadata;
  @ApiModelProperty(notes = "GroupByResult array holding the respective objects "
      + "with their timestamp-value pairs", required = true)
  private GroupByResult[] groupByResult;

  public GroupByResponse(Attribution attribution, String apiVersion, Metadata metadata,
      GroupByResult[] groupByUserResult) {
    this.attribution = attribution;
    this.apiVersion = apiVersion;
    this.metadata = metadata;
    this.groupByResult = groupByUserResult;
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

  public GroupByResult[] getGroupByResult() {
    return groupByResult;
  }

}
