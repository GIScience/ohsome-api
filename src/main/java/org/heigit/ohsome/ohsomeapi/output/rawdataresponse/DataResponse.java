package org.heigit.ohsome.ohsomeapi.output.rawdataresponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Attribution;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Metadata;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response;
import org.wololo.geojson.Feature;

/**
 * Represents the whole GeoJSON response object for the /elements resource.
 */
@JsonInclude(Include.NON_NULL)
public class DataResponse implements Response {

  @ApiModelProperty(notes = "License and copyright info", required = true)
  private Attribution attribution;
  @ApiModelProperty(notes = "Version of this api", required = true)
  private String apiVersion;
  @ApiModelProperty(notes = "Metadata describing the output")
  private Metadata metadata;
  @ApiModelProperty(notes = "Type of the GeoJSON", required = true)
  private String type;
  @ApiModelProperty(notes = "List of GeoJSON features containing the OSM data")
  private List<Feature> features;

  public DataResponse(Attribution attribution, String apiVersion, Metadata metadata, String type,
      List<Feature> features) {
    this.attribution = attribution;
    this.apiVersion = apiVersion;
    this.metadata = metadata;
    this.type = type;
    this.features = features;
  }

  @Override
  public Attribution getAttribution() {
    return attribution;
  }

  @Override
  public String getApiVersion() {
    return apiVersion;
  }

  @Override
  public Metadata getMetadata() {
    return metadata;
  }

  public String getType() {
    return type;
  }

  public List<Feature> getFeatures() {
    return features;
  }
}
