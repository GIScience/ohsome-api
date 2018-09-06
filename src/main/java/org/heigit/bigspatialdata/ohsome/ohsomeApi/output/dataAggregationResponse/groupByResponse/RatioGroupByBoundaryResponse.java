package org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse;

import org.geojson.Feature;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Attribution;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Metadata;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Response;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the whole JSON response object for the data aggregation response using the
 * /ratio/groupBy/boundary resource. It contains an optional
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Metadata
 * Metadata} object, the requested
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.RatioGroupByResult
 * RatioGroupByResult} for a JSON response and an identifier of the object. If the output format is
 * GeoJSON, the response includes a {@link org.geojson.Feature Feature} array, which holds the
 * respective objects with their timestamp-value pairs.
 */
@JsonInclude(Include.NON_NULL)
public class RatioGroupByBoundaryResponse implements Response {

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
  @ApiModelProperty(notes = "RatioGroupByResult array holding the respective objects "
      + "with their timestamp-value-value2-ratio values", required = true)
  private RatioGroupByResult[] groupByBoundaryResult;

  private RatioGroupByBoundaryResponse() {}

  public RatioGroupByBoundaryResponse(Attribution attribution, String apiVersion, Metadata metadata,
      RatioGroupByResult[] groupByBoundaryResult) {
    this.attribution = attribution;
    this.apiVersion = apiVersion;
    this.metadata = metadata;
    this.groupByBoundaryResult = groupByBoundaryResult;
  }

  /** Static factory method returning the whole GeoJSON response. */
  public static RatioGroupByBoundaryResponse of(Attribution attribution, String apiVersion,
      Metadata metadata, String type, Feature[] features) {
    RatioGroupByBoundaryResponse response = new RatioGroupByBoundaryResponse();
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

  public RatioGroupByResult[] getGroupByBoundaryResult() {
    return groupByBoundaryResult;
  }
}
