package org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents the result JSON object for the share request containing the timestamp together with
 * the whole and the part(ial) value.
 */
@JsonInclude(Include.NON_NULL)
public class ShareResult {
  private String timestamp;
  private double whole;
  private double part;

  public ShareResult(String timestamp, double whole, double part) {
    this.timestamp = timestamp;
    this.whole = whole;
    this.part = part;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public double getWhole() {
    return whole;
  }

  public double getPart() {
    return part;
  }
}
