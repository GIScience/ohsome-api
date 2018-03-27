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
  private JsonNode spatialExtend;
  @ApiModelProperty(notes = "Temporal extend of this extract-region", position = 2)
  private TemporalExtend temporalExtend;

  public ExtractRegion(Attribution attribution, JsonNode spatialExtend,
      TemporalExtend temporalExtend) {
    this.attribution = attribution;
    this.spatialExtend = spatialExtend;
    this.temporalExtend = temporalExtend;
  }

  public Attribution getAttribution() {
    return attribution;
  }

  public JsonNode getSpatialExtend() {
    return spatialExtend;
  }

  public TemporalExtend getTemporalExtend() {
    return temporalExtend;
  }

}
