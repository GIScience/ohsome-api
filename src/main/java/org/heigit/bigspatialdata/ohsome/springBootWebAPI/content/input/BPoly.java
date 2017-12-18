package org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.input;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents the bounding-polygon parameter within POST requests.
 *
 */
@JsonInclude(Include.NON_NULL) // needed to exclude NULL objects from the result
public class BPoly {

  private String id;
  private String[] bpoly;

  public BPoly(String id, String[] bpoly) {
    this.id = id;
    this.bpoly = bpoly;
  }

  /**
   * Empty dummy constructor (needed for Jackson).
   */
  public BPoly() {

  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String[] getBpoly() {
    return bpoly;
  }
}
