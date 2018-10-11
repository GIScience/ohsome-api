package org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the metadata JSON object containing the execution time, the unit, a description of the
 * result values, as well as the request URL.
 */
@JsonInclude(Include.NON_NULL)
public class StreamMetadata extends BaseMetadata {

  @ApiModelProperty(notes = "Text describing the result in a sentence", required = true)
  private String description;
  @ApiModelProperty(notes = "Request URL to which this whole output JSON was generated",
      required = true)
  private String requestUrl;

  public StreamMetadata(String description, String requestUrl) {
    this.description = description;
    this.requestUrl = requestUrl;
  }

  public String getDescription() {
    return description;
  }

  public String getRequestUrl() {
    return requestUrl;
  }
}
