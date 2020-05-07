package org.heigit.ohsome.ohsomeapi.output.metadataresponse;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the result JSON object containing the spatial extent as GeoJSON, the 
 * {@link org.heigit.ohsome.ohsomeapi.output.metadataresponse.TemporalExtent
 * TemporalExtent} and the replication sequence number of the data-extract.
 */
public class ExtractRegion {

  @ApiModelProperty(notes = "Spatial extent of this extract-region", required = true, position = 1)
  private JsonNode spatialExtent;
  @ApiModelProperty(notes = "Temporal extent of this extract-region", position = 2)
  private TemporalExtent temporalExtent;
  @ApiModelProperty(notes = "Replication sequence number", position = 3)
  private int replicationSequenceNumber;

  public ExtractRegion(JsonNode spatialExtent, TemporalExtent temporalExtent,
      int replicationSequenceNumber) {
    this.spatialExtent = spatialExtent;
    this.temporalExtent = temporalExtent;
    this.replicationSequenceNumber = replicationSequenceNumber;
  }

  public JsonNode getSpatialExtent() {
    return spatialExtent;
  }

  public TemporalExtent getTemporalExtent() {
    return temporalExtent;
  }
  
  public int getReplicationSequenceNumber() {
    return replicationSequenceNumber;
  }
}
