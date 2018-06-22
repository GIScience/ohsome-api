package org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse;

import org.geojson.Feature;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Attribution;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Metadata;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the whole JSON response object for the data aggregation response using the
 * count|length|area|perimeter/groupBy resource. It contains an optional
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Metadata
 * Metadata}, the requested
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResult
 * GroupByResult} and an identifier of the object plus the corresponding
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.elements.ElementsResult
 * ElementsResult} objects.
 */
@JsonInclude(Include.NON_NULL)
public class GroupByResponse {

  @ApiModelProperty(notes = "License and copyright info", required = true)
  private Attribution attribution;
  @ApiModelProperty(notes = "Version of this api", required = true)
  private String apiVersion;
  @ApiModelProperty(notes = "Metadata describing the output")
  private Metadata metadata;
  @ApiModelProperty(notes = "Type of the GeoJSON", required = true)
  private String type;
  @ApiModelProperty(notes = "GeoJSON Features", required = true)
  private Feature[] features;
  @ApiModelProperty(notes = "GroupByResult array holding the respective objects "
      + "with their timestamp-value pairs", required = true)
  private GroupByResult[] groupByResult;

  private GroupByResponse() {

  }

  public GroupByResponse(Attribution attribution, String apiVersion, Metadata metadata,
      GroupByResult[] groupByUserResult) {
    this.attribution = attribution;
    this.apiVersion = apiVersion;
    this.metadata = metadata;
    this.groupByResult = groupByUserResult;
  }

  /** Static factory method returning the whole GeoJSON response. */
  public static GroupByResponse of(Attribution attribution, String apiVersion, Metadata metadata,
      String type, Feature[] features) {
    GroupByResponse response = new GroupByResponse();
    response.attribution = attribution;
    response.apiVersion = apiVersion;
    response.metadata = metadata;
    response.type = type;
    response.features = features;
    return response;
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

  public String getType() {
    return type;
  }

  public Feature[] getFeatures() {
    return features;
  }

  public GroupByResult[] getGroupByResult() {
    return groupByResult;
  }

}
