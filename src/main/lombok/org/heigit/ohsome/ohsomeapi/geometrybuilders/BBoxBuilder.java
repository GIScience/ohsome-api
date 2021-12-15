package org.heigit.ohsome.ohsomeapi.geometrybuilders;

import java.util.ArrayList;
import java.util.List;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.oshdb.OSHDBBoundingBox;
import org.heigit.ohsome.oshdb.util.geometry.OSHDBGeometryBuilder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.stereotype.Component;

@Component
public class BBoxBuilder extends GeometryBuilder implements GeometryFromCoordinates {
  List<Geometry> geometryList;
  Geometry unifiedBbox;

  /**
   * Creates a unified <code>Geometry</code> object out of the content of the given
   * <code>String</code> array.
   *
   * @param bboxes <code>String</code> array containing the lon/lat coordinates of the bounding
   *     boxes. Each bounding box must consist of 2 lon/lat coordinate pairs (bottom-left and
   *     top-right).
   * @return <code>Geometry</code> object representing the unified bounding boxes.
   * @throws BadRequestException if bboxes coordinates are invalid
   */
  public Geometry create(String[] bboxes) {
    GeometryFactory gf;
    try {

      OSHDBBoundingBox bbox;
      double minLon = Double.parseDouble(bboxes[0]);
      double minLat = Double.parseDouble(bboxes[1]);
      double maxLon = Double.parseDouble(bboxes[2]);
      double maxLat = Double.parseDouble(bboxes[3]);
      bbox = OSHDBBoundingBox.bboxWgs84Coordinates(minLon, minLat, maxLon, maxLat);
      gf = new GeometryFactory();
      unifiedBbox = gf.createGeometry(OSHDBGeometryBuilder.getGeometry(bbox));
      geometryList = new ArrayList<>();
      geometryList.add(OSHDBGeometryBuilder.getGeometry(bbox));
      for (int i = 4; i < bboxes.length; i += 4) {
        minLon = Double.parseDouble(bboxes[i]);
        minLat = Double.parseDouble(bboxes[i + 1]);
        maxLon = Double.parseDouble(bboxes[i + 2]);
        maxLat = Double.parseDouble(bboxes[i + 3]);
        bbox = OSHDBBoundingBox.bboxWgs84Coordinates(minLon, minLat, maxLon, maxLat);
        geometryList.add(OSHDBGeometryBuilder.getGeometry(bbox));
        unifiedBbox = unifiedBbox.union(OSHDBGeometryBuilder.getGeometry(bbox));
      }
      Geometry result = this.unifyPolys(geometryList);
//      geometryBuilder = new org.heigit.ohsome.ohsomeapi.inputprocessing.GeometryBuilder(
//          inputProcessor.getProcessingData());
//      inputProcessor.getProcessingData().setBoundaryList(geometryList);
//      inputProcessor.getProcessingData().setRequestGeom(unifiedBbox);
      return result;
    } catch (NumberFormatException e) {
      throw new BadRequestException(
          "Apart from the custom ids, the bboxeses array must contain double-parseable values "
              + "in the following order: minLon, minLat, maxLon, maxLat.");
    }
  }

  public List<Geometry> getGeometryList() {
    return geometryList;
  }

  public Geometry getUnifiedBbox() {
    return unifiedBbox;
  }

  public void setGeometryList(ArrayList<Geometry> geometryList) {
    this.geometryList = geometryList;
  }

  public void setUnifiedBbox(Geometry unifiedBbox) {
    this.unifiedBbox = unifiedBbox;
  }
}