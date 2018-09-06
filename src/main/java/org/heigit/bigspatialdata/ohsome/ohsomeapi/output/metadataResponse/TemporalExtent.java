package org.heigit.bigspatialdata.ohsome.ohsomeapi.output.metadataResponse;

import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the result JSON object containing the from- and toTimestamps of the respective
 * data-extract.
 */
public class TemporalExtent {

  @ApiModelProperty(notes = "Timestamp in the format YYYY-MM-DDThh:mm:ssZ", required = true)
  private String fromTimestamp;
  @ApiModelProperty(notes = "Timestamp in the format YYYY-MM-DDThh:mm:ssZ", required = true)
  private String toTimestamp;

  public TemporalExtent(String fromTimestamp, String toTimestamp) {
    this.fromTimestamp = fromTimestamp;
    this.toTimestamp = toTimestamp;
  }

  public String getToTimestamp() {
    return toTimestamp;
  }

  public String getFromTimestamp() {
    return fromTimestamp;
  }
}
