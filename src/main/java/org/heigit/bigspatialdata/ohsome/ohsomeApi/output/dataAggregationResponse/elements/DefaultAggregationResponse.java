package org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.elements;

import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Attribution;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Metadata;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the outer JSON response object for the data aggregation requests that do not use the
 * /groupBy resource. It contains attribution info, the version of the api, optional
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Metadata
 * Metadata} and the
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.elements.Result
 * Result} objects.
 */
@JsonInclude(Include.NON_NULL)
public class DefaultAggregationResponse {

  @ApiModelProperty(notes = "License and copyright info", required = true, position = 0)
  private Attribution attribution;
  @ApiModelProperty(notes = "Version of this api", required = true, position = 1)
  private String apiVersion;
  @ApiModelProperty(notes = "Metadata describing the output", position = 2)
  private Metadata metadata;
  @ApiModelProperty(notes = "Result holding timestamp-value pairs", required = true)
  private Result[] result;

  public DefaultAggregationResponse(Attribution attribution, String apiVersion, Metadata metadata,
      Result[] result) {
    this.attribution = attribution;
    this.apiVersion = apiVersion;
    this.metadata = metadata;
    this.result = result;
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

  public Result[] getResult() {
    return result;
  }

}
