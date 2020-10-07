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
 * count|length|area|perimeter/groupBy resource. It contains an optional
 * {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Metadata Metadata}, the
 * requested
 * {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.GroupByResult
 * GroupByResult} for a JSON response and an identifier of the object. If the output format is
 * GeoJSON, the response includes a {@link org.geojson.Feature Feature} array, which holds the
 * respective objects with their timestamp-value pairs.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(Include.NON_NULL)
public class GroupByResponse implements Response {

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
}
