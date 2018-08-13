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
 * /share/groupBy/boundary resource. It contains an optional
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Metadata
 * Metadata} object, the requested
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.ShareGroupByResult
 * ShareGroupByResult} for a JSON response and an identifier of the object. If the output format is
 * GeoJSON, the response includes a {@link org.geojson.Feature Feature} array, which holds the
 * respective objects with their timestamp-value pairs.
 */
@JsonInclude(Include.NON_NULL)
public class ShareGroupByBoundaryResponse implements Response {

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
      + "with their timestamp-whole-part values", required = true)
  private ShareGroupByResult[] shareGroupByBoundaryResult;

  private ShareGroupByBoundaryResponse() {

  }

  public ShareGroupByBoundaryResponse(Attribution attribution, String apiVersion, Metadata metadata,
      ShareGroupByResult[] shareGroupByBoundaryResult) {
    this.attribution = attribution;
    this.apiVersion = apiVersion;
    this.metadata = metadata;
    this.shareGroupByBoundaryResult = shareGroupByBoundaryResult;
  }

  /** Static factory method returning the whole GeoJSON response. */
  public static ShareGroupByBoundaryResponse of(Attribution attribution, String apiVersion,
      Metadata metadata, String type, Feature[] features) {
    ShareGroupByBoundaryResponse response = new ShareGroupByBoundaryResponse();
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

  public ShareGroupByResult[] getShareGroupByBoundaryResult() {
    return shareGroupByBoundaryResult;
  }

}
