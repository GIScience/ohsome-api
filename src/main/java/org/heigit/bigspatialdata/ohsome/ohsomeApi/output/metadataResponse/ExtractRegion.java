package org.heigit.bigspatialdata.ohsome.ohsomeApi.output.metadataResponse;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the result JSON object containing the spatial as GeoJSON and the
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.metadataResponse.TemporalExtent
 * TemporalExtent} of the data-extract.
 */
public class ExtractRegion {

  @ApiModelProperty(notes = "Spatial extend of this extract-region", required = true, position = 1)
  private JsonNode spatialExtent;
  @ApiModelProperty(notes = "Temporal extend of this extract-region", position = 2)
  private TemporalExtent temporalExtent;

  public ExtractRegion(JsonNode spatialExtent, TemporalExtent temporalExtent) {

    this.spatialExtent = spatialExtent;
    this.temporalExtent = temporalExtent;
  }

  public JsonNode getSpatialExtent() {
    return spatialExtent;
  }

  public TemporalExtent getTemporalExtent() {
    return temporalExtent;
  }

}
