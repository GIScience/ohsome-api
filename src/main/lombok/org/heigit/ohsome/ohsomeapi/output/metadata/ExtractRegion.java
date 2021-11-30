package org.heigit.ohsome.ohsomeapi.output.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * Represents the result JSON object containing the spatial extent as GeoJSON, the {@link
 * org.heigit.ohsome.ohsomeapi.output.metadata.TemporalExtent TemporalExtent} and the replication
 * sequence number of the data-extract.
 */
@Getter
@Setter
//@AllArgsConstructor
@Component
public class ExtractRegion {

  @ApiModelProperty(notes = "Spatial extent of this extract-region", required = true, position = 1)
  private JsonNode spatialExtent;
  @ApiModelProperty(notes = "Temporal extent of this extract-region", position = 2)
  private TemporalExtent temporalExtent;
  @ApiModelProperty(notes = "Replication sequence number", position = 3)
  private int replicationSequenceNumber;
}
