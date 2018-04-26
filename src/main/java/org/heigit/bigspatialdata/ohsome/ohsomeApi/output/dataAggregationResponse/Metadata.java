package org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the metadata JSON object containing the execution time, the unit, a description of the
 * result values, as well as the request URL.
 */
@JsonInclude(Include.NON_NULL)
public class Metadata {

  @ApiModelProperty(notes = "Time the server needed to execute the request", required = true,
      position = 0)
  private long executionTime;
  @ApiModelProperty(notes = "Text describing the result in a sentence", required = true,
      position = 1)
  private String description;
  @ApiModelProperty(notes = "Request URL to which this whole output JSON was generated",
      required = true, position = 2)
  private String requestUrl;

  public Metadata(long executionTime, String description, String requestUrl) {
    this.executionTime = executionTime;
    this.description = description;
    this.requestUrl = requestUrl;
  }

  public long getExecutionTime() {
    return executionTime;
  }

  public String getDescription() {
    return description;
  }

  public String getRequestUrl() {
    return requestUrl;
  }

}
