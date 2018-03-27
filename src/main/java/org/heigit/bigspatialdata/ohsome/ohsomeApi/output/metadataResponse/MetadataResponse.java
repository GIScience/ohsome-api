package org.heigit.bigspatialdata.ohsome.ohsomeApi.output.metadataResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the JSON response object for the /metadata request. It contains information about the
 * extract region and version of the API.
 */
@JsonInclude(Include.NON_NULL)
public class MetadataResponse {

  @ApiModelProperty(notes = "Version of this api", required = true, position = 0)
  private String apiVersion;
  @ApiModelProperty(notes = "Extract region object holding the spatial|temporal extend + attribution",
      required = true)
  private ExtractRegion extractRegion;

  public MetadataResponse(String apiVersion, ExtractRegion extractRegion) {
    this.apiVersion = apiVersion;
    this.extractRegion = extractRegion;
  }

  public String getApiVersion() {
    return apiVersion;
  }

  public ExtractRegion getExtractRegion() {
    return extractRegion;
  }

}
