package org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents the meta data JSON object containing the execution time, the unit and a description of
 * the values, which are in the
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result.Result
 * Result} objects, as well as the request URL.
 */
@JsonInclude(Include.NON_NULL)
public class Metadata {

  private long executionTime;
  private String unit;
  private String description;
  private String requestURL;

  public Metadata(long executionTime, String unit, String description, String requestURL) {
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
