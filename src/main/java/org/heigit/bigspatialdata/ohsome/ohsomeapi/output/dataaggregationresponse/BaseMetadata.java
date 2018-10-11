package org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the metadata JSON object containing various metadata fields about the request:
 * for example, execution time, unit, a description of the result values, or the request URL.
 */
@JsonInclude(Include.NON_NULL)
public abstract class BaseMetadata {
  public BaseMetadata() {
  }
}
