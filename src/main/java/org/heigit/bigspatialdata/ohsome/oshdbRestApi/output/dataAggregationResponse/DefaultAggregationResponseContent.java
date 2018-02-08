package org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse;

import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.metadata.Metadata;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result.Result;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the outer JSON response object for the data aggregation requests that do not use the
 * /groupBy resource. It contains the license and copyright, optional
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.metadata.Metadata
 * Metadata} and the
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result.Result
 * Result} objects.
 */
@JsonInclude(Include.NON_NULL)
public class DefaultAggregationResponseContent {

  @ApiModelProperty(notes = "The license of the used data.", required = true, position = 0)
  private String license;
  @ApiModelProperty(notes = "The copyright of the used data.", required = true, position = 1)
  private String copyright;
  @ApiModelProperty(notes = "The metadata describing the output.", position = 2)
  private Metadata metadata;
  @ApiModelProperty(notes = "The result for /count|length|area|perimeter or /density requests.")
  private Result[] result;

  public DefaultAggregationResponseContent(String license, String copyright, Metadata metadata,
      Result[] result) {
    this.license = license;
    this.copyright = copyright;
    this.metadata = metadata;
    this.result = result;
  }

  public String getLicense() {
    return license;
  }

  public String getCopyright() {
    return copyright;
  }

  public Metadata getMetadata() {
    return metadata;
  }

  public Result[] getResult() {
    return result;
  }

}
