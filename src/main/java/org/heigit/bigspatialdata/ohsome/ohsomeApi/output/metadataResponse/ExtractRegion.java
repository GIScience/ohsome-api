package org.heigit.bigspatialdata.ohsome.ohsomeApi.output.metadataResponse;

import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Attribution;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the result JSON object containing the attribution, the spatial and the temporal extend
 * of the data-extract.
 */
public class ExtractRegion {

  @ApiModelProperty(notes = "License and copyright info", required = true, position = 0)
  private Attribution attribution;
  @ApiModelProperty(notes = "Spatial extend of this extract-region", required = true, position = 1)
  private JsonNode spatialExtent;
  @ApiModelProperty(notes = "Temporal extend of this extract-region", position = 2)
  private TemporalExtent temporalExtent;

  public ExtractRegion(Attribution attribution, JsonNode spatialExtent,
      TemporalExtent temporalExtent) {
    this.attribution = attribution;
    this.spatialExtent = spatialExtent;
    this.temporalExtent = temporalExtent;
  }

  public Attribution getAttribution() {
    return attribution;
  }

  public JsonNode getSpatialExtent() {
    return spatialExtent;
  }

  public TemporalExtent getTemporalExtent() {
    return temporalExtent;
  }

}
