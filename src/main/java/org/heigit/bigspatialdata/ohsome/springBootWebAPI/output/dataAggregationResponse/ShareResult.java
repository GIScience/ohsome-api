package org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents the result JSON object for the share request containing the timestamp together with
 * the whole and the part(ial) value.
 */
@JsonInclude(Include.NON_NULL) // needed to exclude NULL objects from the result
public class ShareResult {
  private String timestamp;
  private String whole;
  private String part;

  public ShareResult(String timestamp, String whole, String part) {
    this.timestamp = timestamp;
    this.whole = whole;
    this.part = part;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public String getWhole() {
    return whole;
  }

  public String getPart() {
    return part;
  }
}
