package org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.input;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents the bounding-box parameter within POST requests.
 * 
 * @author kowatsch
 *
 */
@JsonInclude(Include.NON_NULL) // needed to exclude NULL objects from the result
public class BBox {

  private String id;
  private String[] bboxCoords;

  public BBox(String id, String[] bboxCoords) {
    this.id = id;
    this.bboxCoords = bboxCoords;
  }

  /**
   * Empty dummy constructor (needed for Jackson).
   */
  public BBox() {

  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String[] getBboxCoords() {
    return bboxCoords;
  }
}
