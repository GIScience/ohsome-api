package org.heigit.ohsome.ohsomeapi.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents the metadata JSON object containing the execution time, a description of the result
 * values, as well as the request URL.
 */
@Getter
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Metadata {

  @ApiModelProperty(notes = "Time the server needed to execute the request", required = true)
  private Long executionTime;
  @ApiModelProperty(notes = "Text describing the result in a sentence", required = true)
  private String description;
  @ApiModelProperty(notes = "Request URL to which this whole output JSON was generated",
      required = true)
  private String requestUrl;
}
