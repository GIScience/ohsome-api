package org.heigit.ohsome.ohsomeapi.output.metadataresponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Attribution;

/**
 * Represents the JSON response object for the /metadata request. It contains information about the
 * attribution, the version of the API and the
 * {@link org.heigit.ohsome.ohsomeapi.output.metadataresponse.ExtractRegion ExtractRegion}.
 */
@Getter
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class MetadataResponse {

  @ApiModelProperty(notes = "License and copyright info", required = true, position = 0)
  private Attribution attribution;
  @ApiModelProperty(notes = "Version of this api", required = true, position = 1)
  private String apiVersion;
  @ApiModelProperty(notes = "Maximal response timeout in milliseconds", required = true,
      position = 2)
  private int timeOutInMs;
  @ApiModelProperty(
      notes = "Extract region object holding the spatial|temporal extend + attribution",
      required = true, position = 3)
  private ExtractRegion extractRegion;
}
