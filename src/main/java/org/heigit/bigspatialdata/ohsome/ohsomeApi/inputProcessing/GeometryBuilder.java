package org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.exception.NotFoundException;
import org.heigit.bigspatialdata.oshdb.util.OSHDBBoundingBox;
import org.heigit.bigspatialdata.oshdb.util.geometry.OSHDBGeometryBuilder;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.wololo.jts2geojson.GeoJSONReader;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Includes methods to create and manipulate geometries derived from the boundary input parameters.
 */
public class GeometryBuilder {

  // default bbox coordinates defining the whole area (here: BW)
  private final double defMinLon = 7.3949;
  private final double defMaxLon = 10.6139;
  private final double defMinLat = 47.3937;
  private final double defMaxLat = 49.9079;
  private OSHDBBoundingBox bbox;
  private Geometry bcircleGeom;
  private Polygon bpoly;
  private Collection<Geometry> bboxColl;
  private Collection<Geometry> bcircleColl;
  private Collection<Geometry> bpolyColl;

  /**
   * Creates a <code>BoundingBox</code> object out of the default coordinates.
   * 
   * @return <code>BoundingBox</code> object.
   */
  public OSHDBBoundingBox createDefaultBbox() {
    this.bbox = new OSHDBBoundingBox(defMinLon, defMinLat, defMaxLon, defMaxLat);
    return this.bbox;
  }

  /**
   * Creates a unified <code>Geometry</code> object out of the content of the given
   * <code>String</code> array. Only used if more than one bounding box is given in the input array.
   * 
   * @param bboxes <code>String</code> array containing the lon/lat coordinates of the bounding
   *        boxes. Each bounding box must consist of 2 lon/lat coordinate pairs (bottom-left and
   *        top-right).
   * @return <code>Geometry</code> object representing the unified bounding boxes.
   * @throws BadRequestException if coordinates are invalid, or boundary does not intersect with
   *         underlying data polygon
   */
  public Geometry createBboxes(String[] bboxes) throws BadRequestException, NumberFormatException {

    Utils utils = new Utils();
    try {
      Geometry unifiedBbox;
      GeometryFactory gf = new GeometryFactory();
      double minLon = Double.parseDouble(bboxes[0]);
      double minLat = Double.parseDouble(bboxes[1]);
      double maxLon = Double.parseDouble(bboxes[2]);
      double maxLat = Double.parseDouble(bboxes[3]);
      this.bbox = new OSHDBBoundingBox(minLon, minLat, maxLon, maxLat);
      unifiedBbox = gf.createGeometry(OSHDBGeometryBuilder.getGeometry(this.bbox));
      bboxColl = new LinkedHashSet<Geometry>();;
      bboxColl.add(OSHDBGeometryBuilder.getGeometry(this.bbox));

      for (int i = 4; i < bboxes.length; i += 4) {
        minLon = Double.parseDouble(bboxes[i]);
        minLat = Double.parseDouble(bboxes[i + 1]);
        maxLon = Double.parseDouble(bboxes[i + 2]);
        maxLat = Double.parseDouble(bboxes[i + 3]);
        this.bbox = new OSHDBBoundingBox(minLon, minLat, maxLon, maxLat);
        bboxColl.add(OSHDBGeometryBuilder.getGeometry(this.bbox));
        unifiedBbox = unifiedBbox.union(OSHDBGeometryBuilder.getGeometry(this.bbox));
      }
      if (utils.isWithin(unifiedBbox) == false)
        throw new NotFoundException(
            "The provided boundary parameter does not lie completely within the underlying data-extract polygon.");
      return unifiedBbox;
    } catch (NumberFormatException e) {
      throw new BadRequestException(
          "Apart from the custom ids, the bboxeses array must contain double-parseable values in the following order: minLon, minLat, maxLon, maxLat.");
    }
  }

  /**
   * Creates a <code>Geometry</code> object around the coordinates of the given <code>String</code>
   * array.
   * 
   * @param bcircles <code>String</code> array containing the lon/lat coordinates of the point at
   *        [0] and [1] and the size of the buffer at [2].
   * @return <code>Geometry</code> object representing a circular polygon around the bounding point.
   * @throws BadRequestException if coordinates or radius are invalid
   */
  public Geometry createCircularPolygons(String[] bcircles)
      throws BadRequestException, NumberFormatException {
    GeometryFactory geomFact = new GeometryFactory();
    Geometry buffer;
    Geometry geom;
    CoordinateReferenceSystem sourceCRS;
    CoordinateReferenceSystem targetCRS;
    MathTransform transform = null;
    Collection<Geometry> geometryCollection = new LinkedHashSet<Geometry>();
    Utils utils = new Utils();
    try {
      for (int i = 0; i < bcircles.length; i += 3) {
        sourceCRS = CRS.decode("EPSG:4326", true);
        targetCRS = CRS.decode(
            utils.findEPSG(Double.parseDouble(bcircles[i]), Double.parseDouble(bcircles[i + 1])),
            true);
        transform = CRS.findMathTransform(sourceCRS, targetCRS, false);
        Point p = geomFact.createPoint(
            new Coordinate(Double.parseDouble(bcircles[i]), Double.parseDouble(bcircles[i + 1])));
        buffer = JTS.transform(p, transform).buffer(Double.parseDouble(bcircles[i + 2]));
        // transform back again
        transform = CRS.findMathTransform(targetCRS, sourceCRS, false);
        geom = JTS.transform(buffer, transform);
        bcircleGeom = geom;
        // returns this geometry if there was only one bcircle given
        if (bcircles.length == 3) {
          if (utils.isWithin(geom) == false)
            throw new NotFoundException(
                "The provided boundary parameter does not lie completely within the underlying data-extract polygon.");
          geometryCollection.add(geom);
          bcircleColl = geometryCollection;
          return geom;
        }
        geometryCollection.add(geom);
      }
      // set the geometryCollection to be accessible for /groupBy/boundary
      bcircleColl = geometryCollection;
      Geometry unifiedBCircles =
          geomFact.createGeometryCollection(geometryCollection.toArray(new Geometry[] {})).union();
      if (utils.isWithin(unifiedBCircles) == false)
        throw new NotFoundException(
            "The provided boundary parameter does not lie completely within the underlying data-extract polygon.");
      return unifiedBCircles;
    } catch (NumberFormatException | FactoryException | MismatchedDimensionException
        | TransformException e) {
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
   */
  public Geometry createBpolys(String[] bpolys) throws BadRequestException, NumberFormatException {
    GeometryFactory geomFact = new GeometryFactory();
    ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
    Utils utils = new Utils();
    // checks if the first and last coordinate pairs are the same (= only 1 polygon)
    if (bpolys[0].equals(bpolys[bpolys.length - 2])
        && bpolys[1].equals(bpolys[bpolys.length - 1])) {
      try {
        for (int i = 0; i < bpolys.length; i += 2) {
          coords.add(
              new Coordinate(Double.parseDouble(bpolys[i]), Double.parseDouble(bpolys[i + 1])));
        }
      } catch (NumberFormatException e) {
        throw new BadRequestException(
            "The bpolys parameter must contain double-parseable values in form of lon/lat coordinate pairs.");
      }
      // creates a polygon from the coordinates
      this.bpoly = geomFact.createPolygon((Coordinate[]) coords.toArray(new Coordinate[] {}));
      if (utils.isWithin(this.bpoly) == false)
        throw new NotFoundException(
            "The provided boundary parameter does not lie completely within the underlying data-extract polygon.");
      return this.bpoly;
    } else {
      Collection<Geometry> geometryCollection = new LinkedHashSet<Geometry>();
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
            poly = geomFact.createPolygon((Coordinate[]) coords.toArray(new Coordinate[] {}));
            geometryCollection.add(poly);
            coords.removeAll(coords);
            // if the end is reached
            if (i + 2 >= bpolys.length)
              break;
            firstPoint = new Coordinate(Double.parseDouble(bpolys[i + 2]),
                Double.parseDouble(bpolys[i + 3]));
            coords.add(firstPoint);
            i += 2;
          } else
            coords.add(
                new Coordinate(Double.parseDouble(bpolys[i]), Double.parseDouble(bpolys[i + 1])));
        }
        bpolyColl = geometryCollection;
        Geometry unifiedBPolys = geomFact
            .createGeometryCollection(geometryCollection.toArray(new Geometry[] {})).union();
        if (utils.isWithin(unifiedBPolys) == false)
          throw new NotFoundException(
              "The provided boundary parameter does not lie completely within the underlying data-extract polygon.");
        return unifiedBPolys;
      } catch (NumberFormatException | MismatchedDimensionException e) {
        throw new BadRequestException(
            "The bpolys parameter must contain double-parseable values in form of lon/lat coordinate pairs.");
      }
    }
  }

  /**
   * Creates a Geometry object from the given GeoJSON String, which is derived from the metadata.
   * 
   * @param geoJson
   * @return <code>Geometry</code>
   * @throws BadRequestException if the given GeoJSON cannot be converted to a Geometry
   */
  public Geometry createPolygonFromMetadataGeoJson(String geoJson) throws BadRequestException {

    GeoJSONReader reader = new GeoJSONReader();
    try {
      return reader.read(geoJson);
    } catch (Exception e) {
      throw new BadRequestException("The provided GeoJSON cannot be converted.");
    }
  }

  /**
   * Creates a Geometry object from the given GeoJSON String. It must be of type 'FeatureCollection'
   * and its features must be of type 'Polygon' or 'Multipolygon'.
   * 
   * @param geoJson
   * @return <code>Geometry</code>
   * @throws BadRequestException if the given GeoJSON cannot be converted to a Geometry
   */
  public Geometry createGeometryFromGeoJson(String geoJson, InputProcessor iP) {

    Collection<Geometry> geometryCollection = new LinkedHashSet<Geometry>();
    Utils util = iP.getUtils();
    Geometry result = null;
    GeoJSONReader geoJsonReader = new GeoJSONReader();
    JsonReader jsonReader = Json.createReader(new StringReader(geoJson));
    JsonObject root = jsonReader.readObject();
    if (!root.getString("type").equals("FeatureCollection"))
      throw new BadRequestException("The given GeoJSON has to be of the type 'FeatureCollection'.");
    JsonArray features = root.getJsonArray("features");
    String[] boundaryIds = new String[features.size()];
    int count = 0;
    for (JsonValue featureVal : features) {
      JsonObject feature = featureVal.asJsonObject();
      JsonObject properties = feature.getJsonObject("properties");
      // custom ids extracted from properties
      if (properties.containsKey("id")) {
        boundaryIds[count] = properties.getString("id");
        count++;
      } else {
        boundaryIds[count] = "feature" + String.valueOf(count+1);
        count++;
      }
      JsonObject geomObj = feature.getJsonObject("geometry");
      if (!geomObj.getString("type").equals("Polygon")
          && !geomObj.getString("type").equals("MultiPolygon"))
        throw new BadRequestException(
            "The geometry of each feature in the GeoJSON has to be of type 'Polygon' or 'MultiPolygon'.");
      try {
        if (result == null) {
          result = geoJsonReader.read(geomObj.toString());
          geometryCollection.add(result);
        }
        else {
          Geometry currentResult = geoJsonReader.read(geomObj.toString());
          geometryCollection.add(currentResult);
          result = currentResult.union(result);
        }
      } catch (Exception e) {
        throw new BadRequestException("The provided GeoJSON cannot be converted.");
      }
    }
    bpolyColl = geometryCollection;
    util.setBoundaryIds(boundaryIds);
    return result;
  }

  /**
   * Gets the <code>Geometry</code> for each boundary object in the given <code>String</code> array.
   * 
   * @param type <code>String</code> defining the boundary type (bbox, bcircle, bpoly)
   * @return <code>ArrayList</code> containing the <code>Geometry</code> objects for each input
   *         boundary object sorted by the given order of the array.
   */
  public ArrayList<Geometry> getGeometry(BoundaryType type) {

    ArrayList<Geometry> geoms = new ArrayList<>();
    switch (type) {
      case BBOXES:
        geoms.addAll(bboxColl);
        break;
      case BCIRCLES:
        geoms.addAll(bcircleColl);
        break;
      case BPOLYS:
        geoms.addAll(bpolyColl);
        break;
      default:
        geoms = null;
        break;
    }
    return geoms;
  }

  public OSHDBBoundingBox getBbox() {
    return bbox;
  }

  public Geometry getbcircleGeom() {
    return bcircleGeom;
  }

  public Polygon getBpoly() {
    return bpoly;
  }

}
