package org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import org.geojson.GeoJsonObject;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.NotFoundException;
import org.heigit.bigspatialdata.oshdb.util.OSHDBBoundingBox;
import org.heigit.bigspatialdata.oshdb.util.geometry.OSHDBGeometryBuilder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
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
  private final ProcessingData processingData;

  public GeometryBuilder(ProcessingData processingData) {
    this.processingData = processingData;
  }

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
   * @throws BadRequestException if coordinates are invalid
   * @throws NotFoundException if the provided boundary parameter does not lie completely within the
   *         underlying data-extract polygon
   */
  public Geometry createBboxes(String[] bboxes) throws BadRequestException, NotFoundException {
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
      ArrayList<Geometry> geometryList = new ArrayList<Geometry>();
      geometryList.add(OSHDBGeometryBuilder.getGeometry(bbox));
      for (int i = 4; i < bboxes.length; i += 4) {
        minLon = Double.parseDouble(bboxes[i]);
        minLat = Double.parseDouble(bboxes[i + 1]);
        maxLon = Double.parseDouble(bboxes[i + 2]);
        maxLat = Double.parseDouble(bboxes[i + 3]);
        bbox = new OSHDBBoundingBox(minLon, minLat, maxLon, maxLat);
        geometryList.add(OSHDBGeometryBuilder.getGeometry(bbox));
        unifiedBbox = unifiedBbox.union(OSHDBGeometryBuilder.getGeometry(bbox));
      }
      Geometry result = unifyPolys(geometryList);
      processingData.setBoundaryList(geometryList);
      processingData.setRequestGeom(unifiedBbox);
      return result;
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
    ArrayList<Geometry> geometryList = new ArrayList<Geometry>();
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
          geometryList.add(geom);
          processingData.setBoundaryList(geometryList);
          processingData.setRequestGeom(geom);
          return geom;
        }
        geometryList.add(geom);
      }
      Geometry result = unifyPolys(geometryList);
      processingData.setBoundaryList(geometryList);
      processingData.setRequestGeom(result);
      return result;
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
    ArrayList<Coordinate> coords = new ArrayList<>();
    ArrayList<Geometry> geometryList = new ArrayList<>();
    if (bpolys[0].equals(bpolys[bpolys.length - 2])
        && bpolys[1].equals(bpolys[bpolys.length - 1])) {
      try {
        for (int i = 0; i < bpolys.length; i += 2) {
          coords.add(
              new Coordinate(Double.parseDouble(bpolys[i]), Double.parseDouble(bpolys[i + 1])));
        }
      } catch (NumberFormatException e) {
        throw new BadRequestException(ExceptionMessages.BPOLYS_FORMAT);
      }
      bpoly = geomFact.createPolygon((Coordinate[]) coords.toArray(new Coordinate[] {}));
      geometryList.add(bpoly);
      processingData.setBoundaryList(geometryList);
      processingData.setRequestGeom(bpoly);
      return bpoly;
    }
    Coordinate firstPoint = null;
    try {
      for (int i = 0; i < bpolys.length; i += 2) {
        if (firstPoint != null && firstPoint.x == Double.parseDouble(bpolys[i])
            && firstPoint.y == Double.parseDouble(bpolys[i + 1])) {
          Polygon poly;
          coords.add(
              new Coordinate(Double.parseDouble(bpolys[i]), Double.parseDouble(bpolys[i + 1])));
          poly = geomFact.createPolygon(coords.toArray(new Coordinate[] {}));
          geometryList.add(poly);
          coords.clear();
          firstPoint = null;
        } else {
          Coordinate coord =
              new Coordinate(Double.parseDouble(bpolys[i]), Double.parseDouble(bpolys[i + 1]));
          coords.add(coord);
          if (firstPoint == null) {
            firstPoint = coord;
          }
        }
      }
      Geometry result = unifyPolys(geometryList);
      processingData.setBoundaryList(geometryList);
      processingData.setRequestGeom(result);
      return result;
    } catch (NumberFormatException | MismatchedDimensionException e) {
      throw new BadRequestException(ExceptionMessages.BPOLYS_FORMAT);
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
      ProcessingData.setDataPolyGeom(reader.read(geoJson));
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
    ArrayList<Geometry> geometryList = new ArrayList<>();
    JsonObject root = null;
    try (JsonReader jsonReader = Json.createReader(new StringReader(geoJson))) {
      root = jsonReader.readObject();
    } catch (Exception e) {
      throw new BadRequestException("Error in reading of the given GeoJSON.");
    }
    if (!"FeatureCollection".equals(root.getString("type"))) {
      throw new BadRequestException("The given GeoJSON has to be of the type 'FeatureCollection'.");
    }
    JsonArray features = root.getJsonArray("features");
    Object[] boundaryIds = new Object[features.size()];
    GeoJsonObject[] geoJsonGeoms = new GeoJsonObject[features.size()];
    int count = 0;
    for (JsonValue featureVal : features) {
      JsonObject feature = featureVal.asJsonObject();
      JsonObject properties = feature.getJsonObject("properties");
      try {
        if (feature.containsKey("id")) {
          boundaryIds[count] = createBoundaryIdFromJsonObjectId(feature, inputProcessor);
        } else if (properties != null && properties.containsKey("id")) {
          boundaryIds[count] = createBoundaryIdFromJsonObjectId(properties, inputProcessor);
        } else {
          boundaryIds[count] = "feature" + (count + 1);
        }
        count++;
      } catch (BadRequestException e) {
        throw e;
      } catch (Exception e) {
        throw new BadRequestException("The provided custom id(s) could not be parsed.");
      }
      JsonObject geomObj = feature.getJsonObject("geometry");
      checkGeometryTypeOfFeature(geomObj);
      try {
        GeoJSONReader reader = new GeoJSONReader();
        Geometry currentResult = reader.read(geomObj.toString());
        geometryList.add(currentResult);
        geoJsonGeoms[count - 1] =
            new ObjectMapper().readValue(geomObj.toString(), GeoJsonObject.class);
      } catch (Exception e) {
        throw new BadRequestException("The provided GeoJSON cannot be converted.");
      }
    }
    Geometry result = unifyPolys(geometryList);
    processingData.setGeoJsonGeoms(geoJsonGeoms);
    processingData.setBoundaryList(geometryList);
    processingData.setRequestGeom(result);
    InputProcessingUtils util = inputProcessor.getUtils();
    util.setBoundaryIds(boundaryIds);
    return result;
  }

  /**
   * Computes the union of the given geometries and checks if it is completely within the underlying
   * data extract.
   * 
   * @param geometries <code>Collection</code> containing the geometries to unify
   * @return unified geometries
   * @throws NotFoundException if the unified Geometry does not lie completely within the underlying
   *         data extract
   */
  private Geometry unifyPolys(Collection<Geometry> geometries) throws NotFoundException {
    GeometryFactory geometryFactory = new GeometryFactory();
    Polygon[] polys = geometries.stream().flatMap(geo -> {
      if (geo instanceof MultiPolygon) {
        int num = geo.getNumGeometries();
        ArrayList<Polygon> parts = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
          parts.add((Polygon) geo.getGeometryN(i));
        }
        return parts.stream();
      } else {
        return Stream.of(geo);
      }
    }).toArray(Polygon[]::new);
    MultiPolygon mp = geometryFactory.createMultiPolygon(polys);
    // merge all input geometries to single (multi) polygon
    Geometry result = mp.union();
    InputProcessingUtils utils = new InputProcessingUtils();
    if (!utils.isWithin(result)) {
      throw new NotFoundException(ExceptionMessages.BOUNDARY_NOT_IN_DATA_EXTRACT);
    }
    return result;
  }

  /**
   * Creates a boundary ID value from the 'id' field in the given <code>JsonObject</code>.
   * 
   * @param jsonObject <code>JsonObject</code> where the 'id' value is extracted from
   * @param inputProcessor used for
   *        {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.InputProcessingUtils#checkCustomBoundaryId(String)
   *        checkCustomBoundaryId}
   * @return <code>Object</code> having the custom id of type <code>String</code> or
   *         <code>Integer</code>
   */
  private Object createBoundaryIdFromJsonObjectId(JsonObject jsonObject,
      InputProcessor inputProcessor) {
    if (jsonObject.get("id").getValueType().compareTo(JsonValue.ValueType.STRING) == 0) {
      String id = jsonObject.getString("id");
      if ("csv".equalsIgnoreCase(processingData.getFormat())) {
        inputProcessor.getUtils().checkCustomBoundaryId(id);
      }
      return id;
    } else {
      return jsonObject.getInt("id");
    }
  }

  /**
   * Checks the geometry of the given <code>JsonObject</code> on its type. If it's not of type
   * Polygon or Multipolygon, an exception is thrown.
   * 
   * @param geomObj <code>JsonObject</code> to check
   * @throws BadRequestException if the given <code>JsonObject</code> is not of type Polygon or
   *         Multipolygon
   */
  private void checkGeometryTypeOfFeature(JsonObject geomObj) throws BadRequestException {
    if (!geomObj.getString("type").equals("Polygon")
        && !geomObj.getString("type").equals("MultiPolygon")) {
      throw new BadRequestException(
          "The geometry of each feature in the GeoJSON has to be of type 'Polygon' "
              + "or 'MultiPolygon'.");
    }
  }

  public ProcessingData getprocessingData() {
    return processingData;
  }

}
