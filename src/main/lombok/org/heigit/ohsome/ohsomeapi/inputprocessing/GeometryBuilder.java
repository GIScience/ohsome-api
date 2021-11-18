package org.heigit.ohsome.ohsomeapi.inputprocessing;

import lombok.RequiredArgsConstructor;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;

/**
 * Includes methods to create and manipulate geometries derived from the boundary input parameters.
 */
@RequiredArgsConstructor
public class GeometryBuilder {
  private final ProcessingData processingData;

  public GeometryBuilder() {
    this.processingData = null;
  }

  /**
   * Creates a unified <code>Geometry</code> object out of the content of the given
   * <code>String</code> array.
   *
   * @param bboxes <code>String</code> array containing the lon/lat coordinates of the bounding
   *        boxes. Each bounding box must consist of 2 lon/lat coordinate pairs (bottom-left and
   *        top-right).
   * @return <code>Geometry</code> object representing the unified bounding boxes.
   * @throws BadRequestException if bboxes coordinates are invalid
   */
//  public Geometry createBboxes(String[] bboxes) {
//    BBoxBuilder bboxBuilder = new BBoxBuilder();
//    return bboxBuilder.create(bboxes);
//  }

  /**
   * Creates a <code>Geometry</code> object around the coordinates of the given <code>String</code>
   * array.
   *
   * @param bpoints <code>String</code> array containing the lon/lat coordinates of the point at [0]
   *        and [1] and the size of the buffer at [2].
   * @return <code>Geometry</code> object representing (a) circular polygon(s) around the given
   *         bounding point(s).
   * @throws BadRequestException if bcircle coordinates or radius are invalid
   */
//  public Geometry createCircularPolygons(String[] bpoints) {
//    BCircleBuilder bcircleBuilder = new BCircleBuilder();
//    return bcircleBuilder.create(bpoints);
//  }

  /**
   * Creates a <code>Polygon</code> out of the coordinates in the given array. If more polygons are
   * given, a union of the polygons is applied and a <code>MultiPolygon</code> is created.
   *
   * @param bpolys <code>String</code> array containing the lon/lat coordinates of the bounding
   *        polygon(s).
   * @return <code>Geometry</code> object representing a <code>Polygon</code> object, if only one
   *         polygon was given or a <code>MultiPolygon</code> object, if more than one were given.
   * @throws BadRequestException if bpolys coordinates are invalid
   */
//  public Geometry createBpolys(String[] bpolys) {
//  BPolygonBuilder bpolygonBuilder = new BPolygonBuilder();
//    return bpolygonBuilder.create(bpolys);
//  }

  /**
   * Creates a Geometry object from the given GeoJSON String, which is derived from the metadata.
   *
   * @throws RuntimeException if the derived GeoJSON cannot be converted to a Geometry
   */
//  public void createGeometryFromMetadataGeoJson(String geoJson) {
//    GeometryOfOSHDBExtent geometryFromMetadataGeoJSON = new GeometryOfOSHDBExtent();
//    geometryFromMetadataGeoJSON.create(geoJson);
//  }

  /**
   * Creates a Geometry object from the given GeoJSON String. It must be of type 'FeatureCollection'
   * and its features must be of type 'Polygon' or 'Multipolygon'.
   *
   * @throws BadRequestException if the given GeoJSON String cannot be converted to a Geometry, it
   *         is not of the type 'FeatureCollection', or if the provided custom id(s) cannot be
   *         parsed
   */
//  public Geometry createGeometryFromGeoJson(String geoJson, InputProcessor inputProcessor) {
//    BPolygonFromGeoJSON geometryFromGeoJSON = new BPolygonFromGeoJSON();
//    return geometryFromGeoJSON.create(geoJson);
//  }

  public ProcessingData getProcessingData() {
    return processingData;
  }
}
