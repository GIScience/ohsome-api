package org.heigit.ohsome.ohsomeapi.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.heigit.ohsome.ohsomeapi.Application;
import org.wololo.geojson.Feature;

/**
 * Represents the whole GeoJSON response object for the data-extraction and contributions endpoints
 * that always extract the data as GeoJSON, e.g. /elements/geometry or /contributions/geometry.
 */
@JsonInclude(Include.NON_NULL)
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class ExtractionResponse implements Response {

  @ApiModelProperty(notes = "License and copyright info", required = true)
  private Attribution attribution;
  @ApiModelProperty(notes = "Version of this api", required = true)
  private final String apiVersion = Application.API_VERSION;
  @ApiModelProperty(notes = "Metadata describing the output")
  private Metadata metadata;
  @ApiModelProperty(notes = "Type of the GeoJSON", required = true)
  private final String type = "FeatureCollection";
  @ApiModelProperty(notes = "List of GeoJSON features containing the OSM data")
  private List<Feature> features;
}
