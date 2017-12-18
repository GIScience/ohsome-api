package org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output;

/**
 * Represents the meta data, which will be sent back in the JSON response.
 *
 */
public class MetaData {

  private long executionTime;
  private String unit;
  private String description;

  public MetaData(long executionTime, String unit, String description) {
    this.executionTime = executionTime;
    this.unit = unit;
    this.description = description;
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

}
