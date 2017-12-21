package org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents the meta data, which will be sent back in the JSON response.
 *
 */
@JsonInclude(Include.NON_NULL) // needed to exclude NULL objects from the result
public class MetaData {

  private long executionTime;
  private String unit;
  private String description;
  private String requestURL;

  public MetaData(long executionTime, String unit, String description, String requestURL) {
    this.executionTime = executionTime;
    this.unit = unit;
    this.description = description;
    this.requestURL = requestURL;
  }

  public long getExecutionTime() {
    return executionTime;
  }

  public String getUnit() {
    return unit;
  }

  public String getDescription() {
    return description;
  }

  public String getRequestURL() {
    return requestURL;
  }

}
