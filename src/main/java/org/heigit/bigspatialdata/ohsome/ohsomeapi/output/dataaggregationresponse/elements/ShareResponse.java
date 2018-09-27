package org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.elements;

import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Attribution;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Metadata;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;


/**
 * Represents the whole JSON response object for the /elements/count|length|perimeter|area/share
 * resources. It contains the attribution, optional
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Metadata
 * Metadata}, as well as the results section showing
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.elements.ShareResult
 * ShareResult} objects.
 */
@JsonInclude(Include.NON_NULL)
public class ShareResponse implements Response {

  @ApiModelProperty(notes = "License and copyright info", required = true)
  private Attribution attribution;
  @ApiModelProperty(notes = "Version of this api", required = true)
  private String apiVersion;
  @ApiModelProperty(notes = "Metadata describing the output")
  private Metadata metadata;
  @ApiModelProperty(notes = "ShareResult for /share requests", required = true)
  private ShareResult[] shareResult;

  public ShareResponse(Attribution attribution, String apiVersion, Metadata metadata,
      ShareResult[] shareResult) {
    this.attribution = attribution;
    this.apiVersion = apiVersion;
    this.metadata = metadata;
    this.shareResult = shareResult;
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

  public ShareResult[] getShareResult() {
    return shareResult;
  }
}
