package org.heigit.bigspatialdata.ohsome.springBootWebAPI.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents the meta data JSON object containing the execution time, the unit and a description of
 * the values in the
 * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.Result
 * Result} objects, as well as the request URL.
 *
 */
@JsonInclude(Include.NON_NULL) // needed to exclude NULL objects from the result
public class Metadata {

  private long executionTime;
  private String unit;
  private String description;
  private String[] idBoundaryCombination;
  private String requestURL;

  public Metadata(long executionTime, String unit, String description, String[] idBoundaryCombination, String requestURL) {
    this.executionTime = executionTime;
    this.unit = unit;
    this.description = description;
    this.idBoundaryCombination = idBoundaryCombination;
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
  
  public String[] getIdBoundaryCombination() {
    return idBoundaryCombination;
  }

  public String getRequestURL() {
    return requestURL;
  }

}
