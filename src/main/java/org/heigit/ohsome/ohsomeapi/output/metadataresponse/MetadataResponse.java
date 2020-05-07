package org.heigit.ohsome.ohsomeapi.output.metadataresponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Attribution;

/**
 * Represents the JSON response object for the /metadata request. It contains information about the
 * attribution, the version of the API and the
 * {@link org.heigit.ohsome.ohsomeapi.output.metadataresponse.ExtractRegion
 * ExtractRegion}.
 */
@JsonInclude(Include.NON_NULL)
public class MetadataResponse {

  @ApiModelProperty(notes = "License and copyright info", required = true, position = 0)
  private Attribution attribution;
  @ApiModelProperty(notes = "Version of this api", required = true, position = 0)
  private String apiVersion;
  @ApiModelProperty(
      notes = "Extract region object holding the spatial|temporal extend + attribution",
      required = true)
  private ExtractRegion extractRegion;

  public MetadataResponse(Attribution attribution, String apiVersion, ExtractRegion extractRegion) {
    this.attribution = attribution;
    this.apiVersion = apiVersion;
    this.extractRegion = extractRegion;
  }

  public Attribution getAttribution() {
    return attribution;
  }

  public String getApiVersion() {
    return apiVersion;
  }

  public ExtractRegion getExtractRegion() {
    return extractRegion;
  }
}
