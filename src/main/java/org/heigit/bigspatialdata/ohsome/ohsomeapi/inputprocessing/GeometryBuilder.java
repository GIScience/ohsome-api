package org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import org.geojson.GeoJsonObject;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.NotFoundException;
import org.heigit.bigspatialdata.oshdb.util.OSHDBBoundingBox;
import org.heigit.bigspatialdata.oshdb.util.geometry.OSHDBGeometryBuilder;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.wololo.jts2geojson.GeoJSONReader;


/**
 * Includes methods to create and manipulate geometries derived from the boundary input parameters.
 */
public class GeometryBuilder {

  GeometryFactory gf;

  /**
   * Creates a unified <code>Geometry</code> object out of the content of the given
   * <code>String</code> array. Only used if more than one bounding box is given in the input array.
   * 
   * @param bboxes <code>String</code> array containing the lon/lat coordinates of the bounding
   *        boxes. Each bounding box must consist of 2 lon/lat coordinate pairs (bottom-left and
   *        top-right).
   * @return <code>Geometry</code> object representing the unified bounding boxes.
   * @throws BadRequestException if coordinates are invalid
   * @throws NotFoundException if the provided boundary parameter does not lie completely within the
   *         underlying data-extract polygon
   */
  public Geometry createBboxes(String[] bboxes) throws BadRequestException, NotFoundException {
    InputProcessingUtils utils = new InputProcessingUtils();
    try {
      Geometry unifiedBbox;
      OSHDBBoundingBox bbox;
      gf = new GeometryFactory();
      double minLon = Double.parseDouble(bboxes[0]);
      double minLat = Double.parseDouble(bboxes[1]);
      double maxLon = Double.parseDouble(bboxes[2]);
      double maxLat = Double.parseDouble(bboxes[3]);
      bbox = new OSHDBBoundingBox(minLon, minLat, maxLon, maxLat);
      unifiedBbox = gf.createGeometry(OSHDBGeometryBuilder.getGeometry(bbox));
      Collection<Geometry> geometryColl = new LinkedHashSet<Geometry>();
      geometryColl.add(OSHDBGeometryBuilder.getGeometry(bbox));
      for (int i = 4; i < bboxes.length; i += 4) {
        minLon = Double.parseDouble(bboxes[i]);
        minLat = Double.parseDouble(bboxes[i + 1]);
        maxLon = Double.parseDouble(bboxes[i + 2]);
        maxLat = Double.parseDouble(bboxes[i + 3]);
        bbox = new OSHDBBoundingBox(minLon, minLat, maxLon, maxLat);
        geometryColl.add(OSHDBGeometryBuilder.getGeometry(bbox));
        unifiedBbox = unifiedBbox.union(OSHDBGeometryBuilder.getGeometry(bbox));
      }
      if (utils.isWithin(unifiedBbox) == false) {
        throw new NotFoundException("The provided boundary parameter does not lie completely "
            + "within the underlying data-extract polygon.");
      }
      ProcessingData.boundaryColl = geometryColl;
      ProcessingData.bboxesGeom = unifiedBbox;
      return unifiedBbox;
    } catch (NumberFormatException e) {
      throw new BadRequestException(
          "Apart from the custom ids, the bboxeses array must contain double-parseable values "
              + "in the following order: minLon, minLat, maxLon, maxLat.");
    }
  }

  /**
   * Creates a <code>Geometry</code> object around the coordinates of the given <code>String</code>
   * array.
   * 
   * @param bpoints <code>String</code> array containing the lon/lat coordinates of the point at [0]
   *        and [1] and the size of the buffer at [2].
   * @return <code>Geometry</code> object representing (a) circular polygon(s) around the given
   *         bounding point(s).
   * @throws BadRequestException if coordinates or radius are invalid
   * @throws NotFoundException if the provided boundary parameter does not lie completely within the
   *         underlying data-extract polygon
   */
  public Geometry createCircularPolygons(String[] bpoints)
      throws BadRequestException, NotFoundException {
    GeometryFactory geomFact = new GeometryFactory();
    Geometry buffer;
    Geometry geom;
    CoordinateReferenceSystem sourceCrs;
    CoordinateReferenceSystem targetCrs;
    MathTransform transform = null;
    Collection<Geometry> geometryCollection = new LinkedHashSet<Geometry>();
    InputProcessingUtils utils = new InputProcessingUtils();
    try {
      for (int i = 0; i < bpoints.length; i += 3) {
        sourceCrs = CRS.decode("EPSG:4326", true);
        targetCrs = CRS.decode(
            utils.findEpsg(Double.parseDouble(bpoints[i]), Double.parseDouble(bpoints[i + 1])),
            true);
        transform = CRS.findMathTransform(sourceCrs, targetCrs, false);
        Point p = geomFact.createPoint(
            new Coordinate(Double.parseDouble(bpoints[i]), Double.parseDouble(bpoints[i + 1])));
        buffer = JTS.transform(p, transform).buffer(Double.parseDouble(bpoints[i + 2]));
        transform = CRS.findMathTransform(targetCrs, sourceCrs, false);
        geom = JTS.transform(buffer, transform);
        if (bpoints.length == 3) {
          if (utils.isWithin(geom) == false) {
            throw new NotFoundException("The provided boundary parameter does not lie completely "
                + "within the underlying data-extract polygon.");
          }
          geometryCollection.add(geom);
          ProcessingData.boundaryColl = geometryCollection;
          ProcessingData.bcirclesGeom = geom;
          return geom;
        }
        geometryCollection.add(geom);
      }
      Geometry unifiedBCircles =
          geomFact.createGeometryCollection(geometryCollection.toArray(new Geometry[] {})).union();
      if (utils.isWithin(unifiedBCircles) == false) {
        throw new NotFoundException("The provided boundary parameter does not lie completely "
            + "within the underlying data-extract polygon.");
      }
      ProcessingData.boundaryColl = geometryCollection;
      ProcessingData.bcirclesGeom = unifiedBCircles;
      return unifiedBCircles;
    } catch (NumberFormatException | FactoryException | MismatchedDimensionException
        | TransformException | ArrayIndexOutOfBoundsException e) {
      throw new BadRequestException(
          "Each bcircle must consist of a lon/lat coordinate pair plus a buffer in meters.");
    }
  }

  /**
   * Creates a <code>Polygon</code> out of the coordinates in the given array. If more polygons are
   * given, a union of the polygons is applied and a <code>MultiPolygon</code> is created.
   * 
   * @param bpolys <code>String</code> array containing the lon/lat coordinates of the bounding
   *        polygon(s).
   * @return <code>Geometry</code> object representing a <code>Polygon</code> object, if only one
   *         polygon was given or a <code>MultiPolygon</code> object, if more than one were given.
   * @throws BadRequestException if coordinates are invalid
   * @throws NotFoundException if the provided boundary parameter does not lie completely within the
   *         underlying data-extract polygon
   */
  public Geometry createBpolys(String[] bpolys) throws BadRequestException, NotFoundException {
    GeometryFactory geomFact = new GeometryFactory();
    Geometry bpoly;
    ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
    InputProcessingUtils utils = new InputProcessingUtils();
    Collection<Geometry> geometries = new LinkedHashSet<Geometry>();
    if (bpolys[0].equals(bpolys[bpolys.length - 2])
        && bpolys[1].equals(bpolys[bpolys.length - 1])) {
      try {
        for (int i = 0; i < bpolys.length; i += 2) {
          coords.add(
              new Coordinate(Double.parseDouble(bpolys[i]), Double.parseDouble(bpolys[i + 1])));
        }
      } catch (NumberFormatException e) {
        throw new BadRequestException("The bpolys parameter must contain double-parseable values "
            + "in form of lon/lat coordinate pairs.");
      }
      bpoly = geomFact.createPolygon((Coordinate[]) coords.toArray(new Coordinate[] {}));
      if (utils.isWithin(bpoly) == false) {
        throw new NotFoundException("The provided boundary parameter does not lie completely "
            + "within the underlying data-extract polygon.");
      }
      geometries.add(bpoly);
      ProcessingData.boundaryColl = geometries;
      ProcessingData.bpolysGeom = bpoly;
      return bpoly;
    } else {
      Coordinate firstPoint;
      try {
        firstPoint = new Coordinate(Double.parseDouble(bpolys[0]), Double.parseDouble(bpolys[1]));
        coords.add(firstPoint);
        for (int i = 2; i < bpolys.length; i += 2) {
          if (firstPoint.x == Double.parseDouble(bpolys[i])
              && firstPoint.y == Double.parseDouble(bpolys[i + 1])) {
            Polygon poly;
            coords.add(
                new Coordinate(Double.parseDouble(bpolys[i]), Double.parseDouble(bpolys[i + 1])));
            poly = geomFact.createPolygon(coords.toArray(new Coordinate[] {}));
            geometries.add(poly);
            coords.clear();
            if (i + 2 >= bpolys.length) {
              break;
            }
            firstPoint = new Coordinate(Double.parseDouble(bpolys[i + 2]),
                Double.parseDouble(bpolys[i + 3]));
            coords.add(firstPoint);
            i += 2;
          } else {
            coords.add(
                new Coordinate(Double.parseDouble(bpolys[i]), Double.parseDouble(bpolys[i + 1])));
          }
        }
        if (geometries.stream().anyMatch(geometry -> !utils.isWithin(geometry))) {
          throw new NotFoundException("The provided boundary parameter does not lie completely "
              + "within the underlying data-extract polygon.");
        }
        Geometry unifiedBPolys = geomFact
            .createGeometryCollection(geometries.toArray(new Geometry[] {})).union();
        ProcessingData.boundaryColl = geometries;
        ProcessingData.bpolysGeom = unifiedBPolys;
        return unifiedBPolys;
      } catch (NumberFormatException | MismatchedDimensionException e) {
        throw new BadRequestException("The bpolys parameter must contain double-parseable values "
            + "in form of lon/lat coordinate pairs.");
      }
    }
  }

  /**
   * Creates a Geometry object from the given GeoJSON String, which is derived from the metadata.
   * 
   * @throws RuntimeException if the derived GeoJSON cannot be converted to a Geometry
   */
  public void createGeometryFromMetadataGeoJson(String geoJson) throws RuntimeException {
    GeoJSONReader reader = new GeoJSONReader();
    try {
      ProcessingData.dataPolyGeom = reader.read(geoJson);
    } catch (Exception e) {
      throw new RuntimeException("The GeoJSON that is derived out of the metadata, cannot be "
          + "converted. Please use a different data file and contact an admin about this issue.");
    }
  }

  /**
   * Creates a Geometry object from the given GeoJSON String. It must be of type 'FeatureCollection'
   * and its features must be of type 'Polygon' or 'Multipolygon'.
   * 
   * @throws BadRequestException if the given GeoJSON cannot be converted to a Geometry
   */
  public Geometry createGeometryFromGeoJson(String geoJson, InputProcessor inputProcessor) {
    Collection<Geometry> geometryCollection = new LinkedHashSet<Geometry>();
    Geometry result = null;
    GeoJSONReader reader = new GeoJSONReader();
    JsonReader jsonReader = Json.createReader(new StringReader(geoJson));
    JsonObject root = jsonReader.readObject();
    if (!root.getString("type").equals("FeatureCollection")) {
      throw new BadRequestException("The given GeoJSON has to be of the type 'FeatureCollection'.");
    }
    JsonArray features = root.getJsonArray("features");
    String[] boundaryIds = new String[features.size()];
    GeoJsonObject[] geoJsonGeoms = new GeoJsonObject[features.size()];
    int count = 0;
    for (JsonValue featureVal : features) {
      JsonObject feature = featureVal.asJsonObject();
      JsonObject properties = feature.getJsonObject("properties");
      try {
        if (feature.containsKey("id")) {
          boundaryIds[count] = feature.getString("id");
          count++;
        } else if (properties.containsKey("id")) {
          boundaryIds[count] = properties.getString("id");
          count++;
        } else {
          boundaryIds[count] = "feature" + String.valueOf(count + 1);
          count++;
        }
      } catch (Exception e) {
        throw new BadRequestException(
            "The provided custom id(s) must be of the data type String and unique.");
      }
      JsonObject geomObj = feature.getJsonObject("geometry");
      if (!geomObj.getString("type").equals("Polygon")
          && !geomObj.getString("type").equals("MultiPolygon")) {
        throw new BadRequestException(
            "The geometry of each feature in the GeoJSON has to be of type 'Polygon' "
                + "or 'MultiPolygon'.");
      }
      try {
        if (result == null) {
          result = reader.read(geomObj.toString());
          geometryCollection.add(result);
          geoJsonGeoms[count - 1] =
              new ObjectMapper().readValue(geomObj.toString(), GeoJsonObject.class);
        } else {
          Geometry currentResult = reader.read(geomObj.toString());
          geometryCollection.add(currentResult);
          geoJsonGeoms[count - 1] =
              new ObjectMapper().readValue(geomObj.toString(), GeoJsonObject.class);
          result = currentResult.union(result);
        }
      } catch (Exception e) {
        throw new BadRequestException("The provided GeoJSON cannot be converted.");
      }
    }
    ProcessingData.geoJsonGeoms = geoJsonGeoms;
    ProcessingData.boundaryColl = geometryCollection;
    InputProcessingUtils util = inputProcessor.getUtils();
    util.setBoundaryIds(boundaryIds);
    return result;
  }

  /** Gets all the <code>Geometry</code> representations of each boundary object. */
  public ArrayList<Geometry> getGeometry() {
    return new ArrayList<>(ProcessingData.boundaryColl);
  }
}
