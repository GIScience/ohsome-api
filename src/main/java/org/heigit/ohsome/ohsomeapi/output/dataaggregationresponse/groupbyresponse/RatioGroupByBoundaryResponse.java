package org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;
import org.geojson.Feature;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Attribution;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Metadata;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * Represents the whole JSON response object for the data aggregation response using the
 * /ratio/groupBy/boundary resource. It contains an optional
 * {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Metadata Metadata} object, the
 * requested
 * {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.RatioGroupByResult
 * RatioGroupByResult} for a JSON response and an identifier of the object. If the output format is
 * GeoJSON, the response includes a {@link org.geojson.Feature Feature} array, which holds the
 * respective objects with their timestamp-value pairs.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
}
