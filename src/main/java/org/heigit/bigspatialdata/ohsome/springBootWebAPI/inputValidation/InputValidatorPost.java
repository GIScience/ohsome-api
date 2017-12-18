package org.heigit.bigspatialdata.ohsome.springBootWebAPI.inputValidation;

import org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.input.AggregationContent;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.input.BBox;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.input.BPoint;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.input.BPoly;
import org.heigit.bigspatialdata.oshdb.util.BoundingBox;

/**
 * Validates the parameters given by a POST request. Throws exceptions depending on their validity.
 * This class is not up-to-date and not used in the current implementation.
 *
 */
public class InputValidatorPost {

  // default bBox defines the whole world
  private final double defMinLon = -179.9999;
  private final double defMaxLon = 180;
  private final double defMinLat = -85.0511;
  private final double defMaxLat = 85.0511;

  public InputValidatorPost() {}

  /**
   * Checks the given boundary parameters (bboxes, bpoints, bpolys).
   * 
   * @param content AggregationContent object containing the provided parameters.
   */
  public void checkBoundaryPost(AggregationContent content) {
    // checks which bounding type (box, point, polygon) is given
    if ((content.getBboxes() != null && content.getBboxes().length != 0)
        && (content.getBpoints() == null || content.getBpoints().length == 0)
        && (content.getBpolys() == null || content.getBpolys().length == 0)) {
      // bBox(es) is/are given
      checkBBoxes(content.getBboxes());

    } else if ((content.getBpoints() != null && content.getBpoints().length != 0)
        && (content.getBboxes() == null || content.getBboxes().length == 0)
        && (content.getBpolys() == null || content.getBpolys().length == 0)) {
      // bPoint(s) is/are given
      checkBPoints(content.getBpoints());

    } else if ((content.getBpolys() != null && content.getBpolys().length != 0)
        && (content.getBboxes() == null || content.getBboxes().length == 0)
        && (content.getBpoints() == null || content.getBpoints().length == 0)) {
      // bPoly(s) is/are given
      checkBPolys(content.getBpolys());

    } else {
      throw new RuntimeException(
          "You must set (only) one of the following parameters: bboxes, bpoints, or bpolys!");
    }
  }

  /**
   * Checks and extracts the content of the given <code>BBox</code> array. This method should only
   * be used for a POST request. This REST API follows the following format for defining a bounding
   * box: minLon, minLat, maxLon, maxLat. This means that the first coordinate pair defines the
   * lower left and the second pair the upper right point.
   * 
   * @param bBoxes <code>BBox</code> array containing 1...n BBox objects.
   */
  public BoundingBox checkBBoxes(BBox[] bBoxes) {
    // coordinates
    double minLon;
    double minLat;
    double maxLon;
    double maxLat;

    // if no id obj or if empty --> create and set ids
    if (bBoxes[0].getId() == null || bBoxes[0].getId().isEmpty()) {
      int id = 1;
      for (BBox bBox : bBoxes) {
        bBox.setId(String.valueOf(id));
        id = id + 1;
      }
    }
    // default bBox --> global request
    if (bBoxes[0].getId().equals("abc")) {
      return new BoundingBox(defMinLon, defMaxLon, defMinLat, defMaxLat);
    }
    // if there is only 1 bBox (not the default one)
    else if (bBoxes.length == 1) {
      try {
        // parsing of bBox values
        minLon = Double.parseDouble(bBoxes[0].getBboxCoords()[0]);
        minLat = Double.parseDouble(bBoxes[0].getBboxCoords()[1]);
        maxLon = Double.parseDouble(bBoxes[0].getBboxCoords()[2]);
        maxLat = Double.parseDouble(bBoxes[0].getBboxCoords()[3]);
        // creation of the bBox object
        return new BoundingBox(minLon, maxLon, minLat, maxLat);
      } catch (NumberFormatException e) {
        System.out.println(
            "Each bBox object must contain double-parseable String values in the bbox array in the following order: minLon, minLat, maxLon, maxLat.");
      }
    } else {
      try {
        int counter = 0;
        BoundingBox resultingBBox = null;
        // walking through all BBox objects
        for (BBox bBox : bBoxes) {
          // parsing of bBox values
          minLon = Double.parseDouble(bBox.getBboxCoords()[0]);
          minLat = Double.parseDouble(bBox.getBboxCoords()[1]);
          maxLon = Double.parseDouble(bBox.getBboxCoords()[2]);
          maxLat = Double.parseDouble(bBox.getBboxCoords()[3]);

          // only in the first iteration
          if (counter == 0) {
            // creation of the bBox object
            resultingBBox = new BoundingBox(minLon, maxLon, minLat, maxLat);
            continue;
          }
          // intermediate bBox
          BoundingBox intermBBox = new BoundingBox(minLon, maxLon, minLat, maxLat);
          // creates a union of the old bBox together with the newest bBox
          // resultingBBox = BoundingBox.union(resultingBBox, intermBBox);
          counter = counter + 1;
        }
        return resultingBBox;
      } catch (NumberFormatException e) {
        System.out.println(
            "Each bBox object must contain double-parseable String values in the bbox array in the following order: minLon, minLat, maxLon, maxLat.");
      }
    }
    return null;

  }

  /**
   * Checks and extracts the content of the given bPoints array. Format: lon, lat, radius
   * 
   * @param bPoints
   */
  private void checkBPoints(BPoint[] bPoints) {

    // TODO: to be implemented
  }

  /**
   * Checks and extracts the content of the given bPolys array.
   * 
   * @param bPolys String array containing lon/lat coordinate pairs.
   */
  private void checkBPolys(BPoly[] bPolys) {
    // checks if the length of the array is divisible by 2
    if (bPolys.length % 2 != 0) {
      throw new RuntimeException(
          "The polygon must consist of lon/lat coordinate pairs only (total coordinate number must be even).");
    }

    // TODO: to be implemented

    // Polygon bPoly = new Polygon(null, null, null);

    for (int i = 0; i <= bPolys.length; i += 2) {

    }

  }
}
