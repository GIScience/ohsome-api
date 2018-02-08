package org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.metadata;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents the meta data JSON object for the /groupBy/boundary response containing the execution
 * time, the unit, the boundary array and a description of the values, which are in the
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result.Result
 * Result} objects, as well as the request URL
 */
@JsonInclude(Include.NON_NULL)
public class GroupByBoundaryMetadata {

  private long executionTime;
  private String unit;
  private Map<String, double[]> boundary;
  private String description;
  private String requestURL;

  public GroupByBoundaryMetadata(long executionTime, String unit, Map<String, double[]> boundary,
      String description, String requestURL) {
    this.executionTime = executionTime;
    this.unit = unit;
    this.boundary = boundary;
    this.description = description;
    this.requestURL = requestURL;
  }

  public long getExecutionTime() {
    return executionTime;
  }

  public String getUnit() {
    return unit;
  }

  public Map<String, double[]> getBoundary() {
    return boundary;
  }

  public String getDescription() {
    return description;
  }

  public String getRequestURL() {
    return requestURL;
  }
}
