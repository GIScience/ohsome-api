package org.heigit.bigspatialdata.ohsome.oshdbRestApi.inputValidation;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.Application;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.eventHolder.EventHolderBean;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.exception.NotImplementedException;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDB_H2;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.OSMEntitySnapshotView;
import org.heigit.bigspatialdata.oshdb.api.object.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.api.utils.OSHDBTimestamps;
import org.heigit.bigspatialdata.oshdb.osm.OSMType;
import org.heigit.bigspatialdata.oshdb.util.BoundingBox;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Polygonal;

/**
 * Holds general input validation and computation methods and validates specific parameters given by
 * the request. Throws exceptions depending on their validity.
 *
 */
public class InputValidator {

  // HD: 8.6528, 49.3683, 8.7294, 49.4376
  // world: -179.9999, 180, -85.0511, 85.0511
  // default bbox defining the whole area (here: BW)
  private final double defMinLon = 7.3949;
  private final double defMaxLon = 10.6139;
  private final double defMinLat = 47.3937;
  private final double defMaxLat = 49.9079;
  private byte boundary;
  /**
   * Contains either custom ids or generated ones (int values starting at 1).
   */
  private String[] boundaryIds;
  private BoundingBox bbox;
  private Geometry bpointGeom;
  private Polygon bpoly;
  private Collection<Geometry> bboxColl;
  private Collection<Geometry> bpointColl;
  private Collection<Geometry> bpolyColl;
  private final String defEndTime =
      new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
  private final String defStartTime = "2007-11-01";
  private String[] timeData;
  private EnumSet<OSMType> osmTypes;
  private boolean showMetadata;
  /**
   * [0]:oshdb [1]:keytables
   */
  private OSHDB_H2[] dbConnObjects;

  /**
   * Method to process the input parameters of a POST or GET request.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param isPost <code>Boolean</code> value defining if it is a POST (true) or GET (false)
   *        request.
   * 
   * @return <code>MapReducer</code> object including the settings derived from the given
   *         parameters.
   */
  public MapReducer<OSMEntitySnapshot> processParameters(boolean isPost, String bboxes,
      String bpoints, String bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time, String showMetadata) throws BadRequestException {

    // check if this method is called from a POST request
    if (isPost) {
      // sets the string arrays to empty if they are null
      bboxes = checkBoundaryParamOnNull(bboxes);
      bpoints = checkBoundaryParamOnNull(bpoints);
      bpolys = checkBoundaryParamOnNull(bpolys);
      types = checkParamOnNull(types);
      keys = checkParamOnNull(keys);
      values = checkParamOnNull(values);
      userids = checkParamOnNull(userids);
      time = checkParamOnNull(time);
    }
    MapReducer<OSMEntitySnapshot> mapRed;

    // database
    EventHolderBean bean = Application.getEventHolderBean();
    dbConnObjects = bean.getDbConnObjects();
    mapRed = OSMEntitySnapshotView.on(dbConnObjects[0]).keytables(dbConnObjects[1]);

    // metadata
    if (showMetadata == null)
      this.showMetadata = false;
    else if (showMetadata.equals("true"))
      this.showMetadata = true;
    else if (showMetadata.equals("false") || showMetadata.equals(""))
      this.showMetadata = false;
    else
      throw new BadRequestException(
          "The showMetadata parameter can only contain the values 'true' or 'false' written as text(String).");

    // boundary (no parameter = 0, bboxes = 1, bpoints = 2, or bpolys = 3)
    checkBoundaryParams(bboxes, bpoints, bpolys);
    String[] boundaryValues;

    if (this.boundary == 0) {
      mapRed = mapRed.areaOfInterest(createBbox(new String[0]));
    } else if (this.boundary == 1) {
      boundaryValues = splitBoundaryParam(bboxes, (byte) 1);
      mapRed = mapRed.areaOfInterest((Geometry & Polygonal) createBboxes(boundaryValues));
    } else if (this.boundary == 2) {
      boundaryValues = splitBoundaryParam(bpoints, (byte) 2);
      mapRed = mapRed.areaOfInterest((Geometry & Polygonal) createCircularPolygons(boundaryValues));
    } else if (this.boundary == 3) {
      boundaryValues = splitBoundaryParam(bpolys, (byte) 3);
      mapRed = mapRed.areaOfInterest((Geometry & Polygonal) createBpolys(boundaryValues));
    } else
      throw new BadRequestException(
          "Your provided boundary parameter (bboxes, bpoints, or bpolys) does not fit its format. "
              + "or you defined more than one boundary parameter.");

    // osm-type (node, way, relation)
    osmTypes = checkTypes(types);
    mapRed = mapRed.osmTypes(osmTypes);

    // time parameter
    if (time.length == 1) {
      timeData = extractIsoTime(time[0]);
      if (timeData[2] != null) {
        // interval is given
        mapRed = mapRed.timestamps(new OSHDBTimestamps(timeData[0], timeData[1], timeData[2]));
      } else
        // no interval given
        mapRed = mapRed.timestamps(timeData[0], timeData[1]);
    } else if (time.length == 0) {
      // if no time parameter given --> return the default end time
      mapRed = mapRed.timestamps(defEndTime);
    } else {
      // list of timestamps
      String firstElem = time[0];
      time = ArrayUtils.remove(time, 0);
      mapRed = mapRed.timestamps(firstElem, time);
    }

    // key/value parameters
    mapRed = checkKeysValues(mapRed, keys, values);

    // checks if the userids parameter is not empty
    if (userids.length != 0) {
      checkUserids(userids);
      // more efficient way to include all userIDs
      Set<Integer> useridSet = new HashSet<>();
      for (String user : userids)
        useridSet.add(Integer.valueOf(user));

      mapRed = mapRed.where(entity -> {
        return useridSet.contains(entity.getUserId());
      });
    } else {
      // do nothing --> all users will be used
    }

    return mapRed;
  }

  /**
   * Gets the array of points (bounding box, polygon and point [+ radius]) and adds an id before
   * each element. Works atm for bboxes and bpoints.
   * 
   * @param boundary <code>String</code> array containing either bounding boxes, polygons, or points
   *        (+ radius).
   * @param boundaryType <code>String</code> defining which boundary parameter is given (bbox,
   *        bpoint, bpoly).
   * @return <code>String</code> array containing the given coordinates of each element + an added
   *         ID.
   */
  @SuppressWarnings("unused")
  private String[] addId(String[] boundary, String boundaryType) {

    int length;
    String[] boundaryId = null;
    int count = 0;
    switch (boundaryType) {
      case "bbox":
        length = boundary.length + (boundary.length / 4);
        boundaryId = new String[length];
        for (int i = 0; i < boundary.length; i += 4) {
          // sets the id
          boundaryId[i + count] = String.valueOf(count);
          // sets the coordinate values
          boundaryId[i + 1 + count] = boundary[i];
          boundaryId[i + 2 + count] = boundary[i + 1];
          boundaryId[i + 3 + count] = boundary[i + 2];
          boundaryId[i + 4 + count] = boundary[i + 3];
          count++;
        }
        break;
      case "bpoint":
        length = boundary.length + (boundary.length / 3);
        boundaryId = new String[length];
        for (int i = 0; i < boundary.length; i += 3) {
          // sets the id
          boundaryId[i + count] = String.valueOf(count);
          // sets the coordinate values + the radius
          boundaryId[i + 1 + count] = boundary[i];
          boundaryId[i + 2 + count] = boundary[i + 1];
          boundaryId[i + 3 + count] = boundary[i + 2];
          count++;
        }
        break;
      case "bpoly":
        throw new NotImplementedException(
            "Using polygons for groupBy/boundary is not implemented yet.");
    }

    return boundaryId;
  }

  /**
   * Checks the given boundary parameter(s) and sets a corresponding byte value (0 for no boundary,
   * 1 for bboxes, 2 for bpoints, 3 for bpolys). Only one (or none) of them is allowed to have
   * content in it.
   * 
   * @param bboxes <code>String</code> containing the bounding boxes separated via a pipe (|) and
   *        optional custom names at each first coordinate appended with a colon (:).
   * @param bpoints <code>String</code> containing the bounding points separated via a pipe (|) and
   *        optional custom names at each first coordinate appended with a colon (:).
   * @param bpolys <code>String</code> containing the bounding polygons separated via a pipe (|) and
   *        optional custom names at each first coordinate appended with a colon (:).
   */
  private void checkBoundaryParams(String bboxes, String bpoints, String bpolys) {
    if (bboxes.isEmpty() && bpoints.isEmpty() && bpolys.isEmpty()) {
      // no boundary param given --> 'global' request
      this.boundary = 0;
    } else if (!bboxes.isEmpty() && bpoints.isEmpty() && bpolys.isEmpty()) {
      // bboxes given
      this.boundary = 1;
    } else if (bboxes.isEmpty() && !bpoints.isEmpty() && bpolys.isEmpty()) {
      // bpoints given
      this.boundary = 2;
    } else if (bboxes.isEmpty() && bpoints.isEmpty() && !bpolys.isEmpty()) {
      // bpolys given
      this.boundary = 3;
    } else
      throw new BadRequestException(
          "Your provided boundary parameter (bboxes, bpoints, or bpolys) does not fit its format, "
              + "or you defined more than one boundary parameter.");
  }

  /**
   * Splits the given boundary parameter (bboxes, bpoints, or bpolys) two times. The first split is
   * on '|' and to seperate the bounding objects; The second is on ':' to seperate the custom ids
   * from each first coordinate; Returns the coordinates after the second split (and the radius in
   * case of bounding points).
   * 
   * @param boundaryParam <code>String</code> containing the given boundary parameter.
   * @param boundaryType <code>Byte</code> containing the value 1 (bboxes), 2 (bpoints) or 3
   *        (bpolys).
   * @return <code>String</code> array holding only coordinates (plus the radius in case of bounding
   *         points).
   */
  private String[] splitBoundaryParam(String boundaryParam, byte boundaryType) {

    String[] boundaryObjects;
    String[] boundaryParamValues = null;
    String[] boundaryIds = null;
    String[] coords;
    // to check if there is more than one boundary object given
    if (boundaryParam.contains("|"))
      boundaryObjects = boundaryParam.split("\\|");
    else
      boundaryObjects = new String[] {boundaryParam};

    boundaryIds = new String[boundaryObjects.length];
    int idCount = 0;
    int paramCount = 0;

    try {
      if (boundaryType == 1) {
        // bboxes given
        if (boundaryObjects[0].contains(":")) {
          // custom ids are given
          boundaryParamValues = new String[boundaryObjects.length * 4];
          for (String bObject : boundaryObjects) {
            coords = bObject.split("\\,");
            if (coords[0].contains(":")) {
              String[] idAndCoordinate = coords[0].split(":");
              // extract the id
              boundaryIds[idCount] = idAndCoordinate[0];
              // extract the coordinates
              boundaryParamValues[paramCount] = idAndCoordinate[1];
              boundaryParamValues[paramCount + 1] = coords[1];
              boundaryParamValues[paramCount + 2] = coords[2];
              boundaryParamValues[paramCount + 3] = coords[3];
              idCount++;
              paramCount += 4;
            } else {
              throw new BadRequestException(
                  "One or more boundary object(s) have a custom id (or at least a colon), whereas other(s) don't. "
                      + "You can either set custom ids for all your boundary objects, or for none.");
            }
          }
        } else {
          // no custom ids are given
          boundaryParamValues = new String[boundaryObjects.length * 4];
          idCount = 1;
          // walks through the bbox objects
          for (String bObject : boundaryObjects) {
            coords = bObject.split("\\,");
            // walks through the coordinates
            for (String coord : coords) {
              boundaryParamValues[paramCount] = coord;
              paramCount++;
            }
            // adding of ids
            boundaryIds[idCount - 1] = "bbox" + String.valueOf(idCount);
            idCount++;
          }
        }
      } else if (boundaryType == 2) {
        // bpoints given
        if (boundaryObjects[0].contains(":")) {
          // custom ids are given
          boundaryParamValues = new String[boundaryObjects.length * 3];
          for (String bObject : boundaryObjects) {
            coords = bObject.split("\\,");
            if (coords[0].contains(":")) {
              String[] idAndCoordinate = coords[0].split(":");
              // extract the id
              boundaryIds[idCount] = idAndCoordinate[0];
              // extract the coordinate
              boundaryParamValues[paramCount] = idAndCoordinate[1];
              boundaryParamValues[paramCount + 1] = coords[1];
              // extract the radius
              boundaryParamValues[paramCount + 2] = coords[2];
              idCount++;
              paramCount += 3;
            } else {
              throw new BadRequestException(
                  "One or more boundary object(s) have a custom id (or at least a colon), whereas other(s) don't. "
                      + "You can either set custom ids for all your boundary objects, or for none.");
            }
          }
        } else {
          // no custom ids are given
          boundaryParamValues = new String[boundaryObjects.length * 3];
          idCount = 1;
          // walks through the bbox objects
          for (String bObject : boundaryObjects) {
            coords = bObject.split("\\,");
            // walks through the coordinates + radius
            for (String coord : coords) {
              boundaryParamValues[paramCount] = coord;
              paramCount++;
            }
            // adding of ids
            boundaryIds[idCount - 1] = "bpoint" + String.valueOf(idCount);
            idCount++;
          }
        }
      } else {
        // bpolys given
        if (boundaryObjects[0].contains(":")) {
          // custom ids are given
          boundaryParamValues = new String[boundaryParam.length()];
          for (String bObject : boundaryObjects) {
            coords = bObject.split("\\,");
            if (coords[0].contains(":")) {
              String[] idAndCoordinate = coords[0].split(":");
              // extract the id and the first coordinate
              boundaryIds[idCount] = idAndCoordinate[0];
              boundaryParamValues[paramCount] = idAndCoordinate[1];
              paramCount++;
              // extract the other coordinates
              for (int i = 1; i < coords.length; i++) {
                boundaryParamValues[paramCount] = coords[i];
                paramCount++;
              }
              idCount++;

            } else {
              throw new BadRequestException(
                  "One or more boundary object(s) have a custom id (or at least a colon), whereas other(s) don't. "
                      + "You can either set custom ids for all your boundary objects, or for none.");
            }
          }
        } else {
          // no custom ids are given
          boundaryParamValues = new String[boundaryParam.length()];
          idCount = 1;
          // walks through the bbox objects
          for (String bObject : boundaryObjects) {
            coords = bObject.split("\\,");
            // walks through the coordinates
            for (String coord : coords) {
              boundaryParamValues[paramCount] = coord;
              paramCount++;
            }
            // adding of ids
            boundaryIds[idCount - 1] = "bpoly" + String.valueOf(idCount);
            idCount++;
          }
        }
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new BadRequestException(
          "The processing of the boundary parameter gave an error. Please use the predefined format "
              + "where you delimit different objects with the pipe-sign '|' "
              + "and optionally add custom ids with the colon ':' at the first coordinate of each object.");
    }
    this.boundaryIds = boundaryIds;
    // remove the possible null values in the array
    boundaryParamValues =
        Arrays.stream(boundaryParamValues).filter(Objects::nonNull).toArray(String[]::new);
    return boundaryParamValues;
  }

  /**
   * Creates a <code>BoundingBox</code> object out of the content of the given <code>String</code>
   * array. Only used if one or no bounding box is given.
   * 
   * @param bbox <code>String</code> array containing the lon/lat coordinates of the bounding box.
   *        It must consist of 2 lon/lat coordinate pairs (bottom-left and top-right).
   * 
   * @return <code>BoundingBox</code> object.
   * @throws BadRequestException Invalid coordinates.
   */
  private BoundingBox createBbox(String[] bbox) throws BadRequestException {
    if (bbox.length == 0) {
      // no bboxes given -> global request
      this.bbox = new BoundingBox(defMinLon, defMaxLon, defMinLat, defMaxLat);
      return this.bbox;
    } else if (bbox.length == 4) {
      try {
        // parsing of the bbox values
        double minLon = Double.parseDouble(bbox[0]);
        double minLat = Double.parseDouble(bbox[1]);
        double maxLon = Double.parseDouble(bbox[2]);
        double maxLat = Double.parseDouble(bbox[3]);
        // creation of the bbox
        this.bbox = new BoundingBox(minLon, maxLon, minLat, maxLat);
        bboxColl = new LinkedHashSet<Geometry>();;
        bboxColl.add(this.bbox.getGeometry());
        return this.bbox;
      } catch (NumberFormatException e) {
        throw new BadRequestException(
            "Apart from the custom id, the bounding box must contain double-parseable values in the following order: minLon, minLat, maxLon, maxLat.");
      }
    } else {
      throw new BadRequestException(
          "Apart from the custom id, the bounding box must contain double-parseable values in the following order: minLon, minLat, maxLon, maxLat.");
    }
  }

  /**
   * Creates a unified <code>Geometry</code> object out of the content of the given
   * <code>String</code> array. Only used if more than one bounding box is given in the input array.
   * 
   * @param bboxes <code>String</code> array containing the lon/lat coordinates of the bounding
   *        boxes. Each bounding box must consist of 2 lon/lat coordinate pairs (bottom-left and
   *        top-right).
   * 
   * @return <code>Geometry</code> object representing the unified bounding boxes.
   * @throws BadRequestException Invalid coordinates.
   */
  private Geometry createBboxes(String[] bboxes) throws BadRequestException {

    try {
      Geometry unifiedBbox;
      GeometryFactory gf = new GeometryFactory();
      // parsing of the first bboxes values
      double minLon = Double.parseDouble(bboxes[0]);
      double minLat = Double.parseDouble(bboxes[1]);
      double maxLon = Double.parseDouble(bboxes[2]);
      double maxLat = Double.parseDouble(bboxes[3]);
      // creation of the first bbox
      this.bbox = new BoundingBox(minLon, maxLon, minLat, maxLat);
      unifiedBbox = gf.createGeometry(this.bbox.getGeometry());
      // create the collection and add the bbox geometry
      bboxColl = new LinkedHashSet<Geometry>();;
      bboxColl.add(this.bbox.getGeometry());

      for (int i = 4; i < bboxes.length; i += 4) {
        // parsing of the other bboxes values
        minLon = Double.parseDouble(bboxes[i]);
        minLat = Double.parseDouble(bboxes[i + 1]);
        maxLon = Double.parseDouble(bboxes[i + 2]);
        maxLat = Double.parseDouble(bboxes[i + 3]);
        this.bbox = new BoundingBox(minLon, maxLon, minLat, maxLat);
        // add it to the geometry collection
        bboxColl.add(this.bbox.getGeometry());
        // union of the bboxes
        unifiedBbox = unifiedBbox.union(this.bbox.getGeometry());
      }
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
   * @param bpoints <code>String</code> array containing the lon/lat coordinates of the point at [0]
   *        and [1] and the size of the buffer at [2].
   * 
   * @return <code>Geometry</code> object representing a circular polygon around the bounding point.
   * 
   * @throws BadRequestException Invalid coordinates or radius.
   */
  private Geometry createCircularPolygons(String[] bpoints) throws BadRequestException {
    GeometryFactory geomFact = new GeometryFactory();
    Geometry buffer;
    Geometry geom;
    CoordinateReferenceSystem sourceCRS;
    CoordinateReferenceSystem targetCRS;
    MathTransform transform = null;
    Collection<Geometry> geometryCollection = new LinkedHashSet<Geometry>();
    try {
      // walks through all bounding points, creates polygons and adds them to the collection
      for (int i = 0; i < bpoints.length; i += 3) {
        // Set source and target CRS + transformation
        sourceCRS = CRS.decode("EPSG:4326", true);
        targetCRS = CRS.decode(
            findEPSG(Double.parseDouble(bpoints[i]), Double.parseDouble(bpoints[i + 1])), true);
        transform = CRS.findMathTransform(sourceCRS, targetCRS, false);
        // creates a point and a buffer from the coordinates
        Point p = geomFact.createPoint(
            new Coordinate(Double.parseDouble(bpoints[i]), Double.parseDouble(bpoints[i + 1])));
        buffer = JTS.transform(p, transform).buffer(Double.parseDouble(bpoints[i + 2]));
        // transform back again
        transform = CRS.findMathTransform(targetCRS, sourceCRS, false);
        geom = JTS.transform(buffer, transform);
        bpointGeom = geom;
        // returns this geometry if there was only one bpoint given
        if (bpoints.length == 3) {
          geometryCollection.add(geom);
          bpointColl = geometryCollection;
          return geom;
        }
        geometryCollection.add(geom);
      }
      // set the geometryCollection to be accessible for /groupBy/boundary
      bpointColl = geometryCollection;
      // unifies polygons that intersect with each other
      geometryCollection = unifyIntersectedPolys(geometryCollection);
      // creates a MultiPolygon out of the polygons in the collection
      MultiPolygon combined = createMultiPolygon(geometryCollection);
      bpointGeom = combined;

      return combined;
    } catch (FactoryException | MismatchedDimensionException | TransformException e) {
      throw new BadRequestException(
          "Each bpoint must consist of a lon/lat coordinate pair plus a buffer in meters.");
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
   * 
   * @throws BadRequestException Invalid coordinates.
   */
  private Geometry createBpolys(String[] bpolys) throws BadRequestException {
    GeometryFactory geomFact = new GeometryFactory();
    ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
    // checks if the first and last coordinate pairs are the same (= only 1 polygon)
    if (bpolys[0].equals(bpolys[bpolys.length - 2])
        && bpolys[1].equals(bpolys[bpolys.length - 1])) {
      try {
        // walks through the string array and parses the coordinates
        for (int i = 0; i < bpolys.length; i += 2) {
          coords.add(
              new Coordinate(Double.parseDouble(bpolys[i]), Double.parseDouble(bpolys[i + 1])));
        }
        // adds the first coordinate pair again so the polygon will be closed
        coords.add(new Coordinate(Double.parseDouble(bpolys[0]), Double.parseDouble(bpolys[1])));
      } catch (NumberFormatException e) {
        throw new BadRequestException(
            "The bpolys parameter must contain double-parseable values in form of lon/lat coordinate pairs.");
      }
      // creates a polygon from the coordinates
      this.bpoly = geomFact.createPolygon((Coordinate[]) coords.toArray(new Coordinate[] {}));
      return this.bpoly;
    } else {

      Collection<Geometry> geometryCollection = new LinkedHashSet<Geometry>();
      Coordinate firstPoint;

      try {
        // sets the first point and adds it to the arraylist
        firstPoint = new Coordinate(Double.parseDouble(bpolys[0]), Double.parseDouble(bpolys[1]));
        coords.add(firstPoint);
        // walks through all remaining coordinates, creates polygons and adds them to the collection
        for (int i = 2; i < bpolys.length; i += 2) {
          // compares the current point to the first point
          if (firstPoint.x == Double.parseDouble(bpolys[i])
              && firstPoint.y == Double.parseDouble(bpolys[i + 1])) {
            Polygon poly;
            coords.add(
                new Coordinate(Double.parseDouble(bpolys[i]), Double.parseDouble(bpolys[i + 1])));
            // create a polygon from the coordinates and add it to the collection
            poly = geomFact.createPolygon((Coordinate[]) coords.toArray(new Coordinate[] {}));
            geometryCollection.add(poly);
            // clear the coords array
            coords.removeAll(coords);
            // if the end is reached
            if (i + 2 >= bpolys.length)
              break;
            // set the new first point and add it to the array
            firstPoint = new Coordinate(Double.parseDouble(bpolys[i + 2]),
                Double.parseDouble(bpolys[i + 3]));
            coords.add(firstPoint);
            i += 2;
          } else
            coords.add(
                new Coordinate(Double.parseDouble(bpolys[i]), Double.parseDouble(bpolys[i + 1])));
        }
        bpolyColl = geometryCollection;
        // unifies polygons that intersect with each other
        geometryCollection = unifyIntersectedPolys(geometryCollection);
        // creates a MultiPolygon out of the polygons in the collection
        MultiPolygon combined = createMultiPolygon(geometryCollection);
        return combined;
      } catch (NumberFormatException e) {
        throw new BadRequestException(
            "The bpolys parameter must contain double-parseable values in form of lon/lat coordinate pairs.");
      }
    }
  }

  /**
   * Checks and extracts the content of the types parameter.
   * 
   * @param types <code>String</code> array containing 1, 2, or all 3 OSM types (node, way,
   *        relation). If the array is empty, all 3 types are used.
   * 
   * @return <code>EnumSet</code> containing the requested OSM type(s).
   * @throws BadRequestException If the content of the parameter does not represent one, two, or all
   *         three OSM types.
   */
  private EnumSet<OSMType> checkTypes(String[] types) throws BadRequestException {
    // checks if the types array is too big
    if (types.length > 3) {
      throw new BadRequestException(
          "Parameter containing the OSM Types cannot have more than 3 entries.");
    }
    // check if the types array only contains the default value (length == 0)
    if (types.length == 0) {
      return EnumSet.of(OSMType.NODE, OSMType.WAY, OSMType.RELATION);
    }
    // complex if-else structure, which adds the corresponding OSMType(s) depending
    // on the String(s) and throws exceptions if they aren't one or more of "node",
    // "way", "relation"
    if (types.length == 1) {
      if (types[0].equalsIgnoreCase("node"))
        osmTypes = EnumSet.of(OSMType.NODE);
      else if (types[0].equalsIgnoreCase("way"))
        osmTypes = EnumSet.of(OSMType.WAY);
      else if (types[0].equalsIgnoreCase("relation"))
        osmTypes = EnumSet.of(OSMType.RELATION);
      else
        throw new BadRequestException(
            "Array containing the OSM-Types can only have one, two, or all three of the following Strings: 'node', 'way', 'relation'.");
    } else if (types.length == 2) {
      if (types[0].equalsIgnoreCase("node")) {
        if (types[1].equalsIgnoreCase("way"))
          osmTypes = EnumSet.of(OSMType.NODE, OSMType.WAY);
        else if (types[1].equalsIgnoreCase("relation"))
          osmTypes = EnumSet.of(OSMType.NODE, OSMType.RELATION);
        else
          throw new BadRequestException(
              "Array containing the OSM-Types can only have one, two, or all three of the following Strings: 'node', 'way', 'relation'.");
      } else if (types[0].equalsIgnoreCase("way")) {
        if (types[1].equalsIgnoreCase("node"))
          osmTypes = EnumSet.of(OSMType.WAY, OSMType.NODE);
        else if (types[1].equalsIgnoreCase("relation"))
          osmTypes = EnumSet.of(OSMType.WAY, OSMType.RELATION);
        else
          throw new BadRequestException(
              "Array containing the OSM-Types can only have one, two, or all three of the following Strings: 'node', 'way', 'relation'.");
      } else if (types[0].equalsIgnoreCase("relation")) {
        if (types[1].equalsIgnoreCase("node"))
          osmTypes = EnumSet.of(OSMType.RELATION, OSMType.NODE);
        else if (types[1].equalsIgnoreCase("way"))
          osmTypes = EnumSet.of(OSMType.RELATION, OSMType.WAY);
        else
          throw new BadRequestException(
              "Array containing the OSM-Types can only have one, two, or all three of the following Strings: 'node', 'way', 'relation'.");
      }
      // happens when array.size == 3
    } else if ((types[0].equalsIgnoreCase("node") && types[1].equalsIgnoreCase("way")
        && types[2].equalsIgnoreCase("relation"))
        || (types[0].equalsIgnoreCase("node") && types[1].equalsIgnoreCase("relation")
            && types[2].equalsIgnoreCase("way"))
        || (types[0].equalsIgnoreCase("way") && types[1].equalsIgnoreCase("node")
            && types[2].equalsIgnoreCase("relation"))
        || (types[0].equalsIgnoreCase("way") && types[1].equalsIgnoreCase("relation")
            && types[2].equalsIgnoreCase("node"))
        || (types[0].equalsIgnoreCase("relation") && types[1].equalsIgnoreCase("node")
            && types[2].equalsIgnoreCase("way"))
        || (types[0].equalsIgnoreCase("relation") && types[1].equalsIgnoreCase("way")
            && types[2].equalsIgnoreCase("node")))
      osmTypes = EnumSet.of(OSMType.NODE, OSMType.WAY, OSMType.RELATION);
    else
      throw new BadRequestException(
          "Array containing the OSM-Types can only have one, two, or all three of the following Strings: 'node', 'way', 'relation'.");
    return osmTypes;
  }

  private MapReducer<OSMEntitySnapshot> checkKeysValues(MapReducer<OSMEntitySnapshot> mapRed,
      String[] keys, String[] values) {
    if (keys.length < values.length) {
      throw new BadRequestException(
          "There cannot be more values than keys. For each value in the values parameter, the respective key has to be provided at the same index in the keys parameter.");
    }
    if (keys.length != values.length) {
      String[] tempVal = new String[keys.length];
      // extracts the value entries from the old values array
      for (int a = 0; a < values.length; a++) {
        tempVal[a] = values[a];
      }
      // adds empty entries in the tempVal array
      for (int i = values.length; i < keys.length; i++) {
        tempVal[i] = "";
      }
      values = tempVal;
    }
    // prerequisites: both arrays (keys and values) must be of the same length
    // and key-value pairs need to be at the same index in both arrays
    for (int i = 0; i < keys.length; i++) {
      if (values[i].equals(""))
        mapRed = mapRed.where(keys[i]);
      else
        mapRed = mapRed.where(keys[i], values[i]);
    }
    return mapRed;
  }

  /**
   * Checks the content of the userids <code>String</code> array.
   * 
   * @param userids String array containing the OSM user IDs.
   * 
   * @throws BadRequestException If one of the userids is invalid.
   */
  private void checkUserids(String[] userids) {
    for (String user : userids) {
      try {
        // tries to parse the String to a long
        Long.valueOf(user);
      } catch (NumberFormatException e) {
        throw new BadRequestException(
            "The userids parameter can only contain valid OSM userids, which are always a positive whole number");
      }
    }
  }

  /**
   * Extracts the time information out of the time parameter and checks the content on its format,
   * as well as <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO-8601</a> conformity. This
   * method is used if one datetimestring is given. Following time formats are allowed:
   * <ul>
   * <li><strong>YYYY-MM-DD</strong> or <strong>YYYY-MM-DDThh:mm:ss</strong>: When a timestamp
   * includes 'T', hh:mm must also be given. This applies for all time formats, which use
   * timestamps. If -MM-DD or only -DD is missing, '01' is used as default for month and day.</li>
   * <li><strong>YYYY-MM-DD/YYYY-MM-DD</strong>: start/end timestamps</li>
   * <li><strong>YYYY-MM-DD/YYYY-MM-DD/PnYnMnD</strong>: start/end/period where n refers to the size
   * of the respective period</li>
   * <li><strong>/YYYY-MM-DD</strong>: #/end where # equals the earliest timestamp</li>
   * <li><strong>/YYYY-MM-DD/PnYnMnD</strong>: #/end/period</li>
   * <li><strong>YYYY-MM-DD/</strong>: start/# where # equals the latest timestamp</li>
   * <li><strong>YYYY-MM-DD//PnYnMnD</strong>: start/#/period</li>
   * <li><strong>/</strong>: #/# where # equals the earliest and latest timestamp</li>
   * <li><strong>//PnYnMnD</strong>: #/#/period</li>
   * <li><strong>invalid</strong>: throws BadRequestException</li>
   * </ul>
   * <p>
   * For clarification: the format YYYY-MM-DDThh:mm:ss can be applied to any format, where a
   * timestamp is used and # is a replacement holder for "no value". Note that the positioning and
   * using of the forward slash '/' is very important.
   * 
   * @param time <code>String</code> holding the unparsed time information.
   * @return <code>String</code> array containing the startTime at at [0], the endTime at [1] and
   *         the period at [2].
   * @throws BadRequestException The provided time parameter does not fit to any specified format.
   */
  private String[] extractIsoTime(String time) throws BadRequestException {
    String[] timeVals = new String[3];
    if (time.contains("/")) {
      if (time.length() == 1) {
        // only "/" is given
        timeVals[0] = defStartTime;
        timeVals[1] = defEndTime;
        return timeVals;
      }
      String[] timeSplit = time.split("/");
      if (timeSplit[0].length() > 0) {
        // start timestamp
        checkIsoConformity(timeSplit[0], "start");
        timeVals[0] = timeSplit[0];
        if (time.endsWith("/") && (timeSplit.length < 2 || timeSplit[1].length() == 0)) {
          // latest timestamp
          timeVals[1] = defEndTime;
          return timeVals;
        }
      } else {
        // earliest timestamp
        timeVals[0] = defStartTime;
      }
      if (timeSplit[1].length() > 0) {
        // end timestamp
        checkIsoConformity(timeSplit[1], "end");
        timeVals[1] = timeSplit[1];
      } else {
        // latest timestamp
        timeVals[1] = defEndTime;
      }
      if (timeSplit.length == 3 && timeSplit[2].length() > 0) {
        // interval
        try {
          Period.parse(timeSplit[2]);
          timeVals[2] = timeSplit[2];
        } catch (DateTimeParseException e) {
          throw new BadRequestException(
              "The interval (period) of the provided time parameter is not ISO-8601 conform.");
        }
      }
    } else {
      // just one timestamp
      try {
        if (time.length() == 10) {
          LocalDate.parse(time);
        } else {
          LocalDateTime.parse(time);
        }
        timeVals[0] = time;
        timeVals[1] = time;
        timeVals[2] = "P1Y";
      } catch (DateTimeParseException e) {
        throw new BadRequestException("The provided time parameter is not ISO-8601 conform.");
      }
    }

    return timeVals;
  }

  /**
   * Checks the given <code>String</code> on its content and if it is ISO-8601 conform.
   * 
   * @param time <code>String</code> containing the start or end time from the given time parameter.
   * @throws BadRequestException if the given time-String is not ISO-8601 conform;
   */
  private void checkIsoConformity(String time, String startEnd) {

    try {
      // YYYY
      if (time.length() == 4) {
        // add the month and day and try to parse it
        time = time + "-01-01";
        LocalDate.parse(time);
      }
      // YYYY-MM
      else if (time.length() == 7) {
        // add the day and try to parse it
        time = time + "-01";
        LocalDate.parse(time);
      }
      // YYYY-MM-DD
      else if (time.length() == 10) {
        LocalDate.parse(time);
      }
      // YYYY-MM-DDThh:mm or YYYY-MM-DDThh:mm:ss
      else if (time.length() == 16 || time.length() == 19) {
        LocalDateTime.parse(time);
      } else {
        throw new BadRequestException(
            "The " + startEnd + " time of the provided time parameter is not ISO-8601 conform.");
      }
    } catch (DateTimeParseException e) {
      throw new BadRequestException(
          "The " + startEnd + " time of the provided time parameter is not ISO-8601 conform.");
    }
  }

  /**
   * Checking if an input parameter of a POST request is null.
   * 
   * @param toCheck <code>String</code> array, which is checked.
   * @return <code>String</code> array, which is empty, but not null.
   */
  private String[] checkParamOnNull(String[] toCheck) {
    if (toCheck == null)
      toCheck = new String[0];
    return toCheck;
  }

  /**
   * Checking if a given boundary input parameter of a POST request is null.
   * 
   * @param toCheck <code>String</code>, which is checked.
   * @return <code>String</code>, which is empty, but not null.
   */
  private String checkBoundaryParamOnNull(String toCheck) {
    if (toCheck == null)
      toCheck = "";
    return toCheck;
  }

  /**
   * Unifies polygons, which intersect with each other and adds the unified polygons to the
   * collection.
   * 
   * @param collection <code>Collection</code> that includes all polygons.
   * @return Collection that includes unified polygons created from intersected polygons and other
   *         polygons, which do not intersect with any other polygon.
   */
  private Collection<Geometry> unifyIntersectedPolys(Collection<Geometry> collection) {
    // converts the collection to an array
    Geometry[] polys = collection.toArray(new Geometry[collection.size()]);
    // walks through all polys and checks if one or more intersect with each other
    for (int i = 0; i < polys.length - 1; i++) {
      for (int j = i + 1; j < polys.length; j++) {
        if (polys[i].intersects(polys[j])) {
          // union the polys that intersect
          Geometry unionedPoly = polys[i].union(polys[j]);
          // remove them from the array
          polys = ArrayUtils.remove(polys, i);
          polys = ArrayUtils.remove(polys, j - 1);
          // add the unioned poly to the array via creating a new one
          polys = ArrayUtils.add(polys, unionedPoly);
        }
      }
    }
    // convert the array back to a collection
    collection = new LinkedHashSet<Geometry>(Arrays.asList(polys));
    return collection;
  }

  /**
   * Creates a <code>MultiPolygon</code> out of the polygons in the given <code>Collection</code>.
   * 
   * @param collection <code>Collection</code> that holds the polygons.
   * @return <code>MultiPolygon</code> object consisting of the given polygons.
   */
  private MultiPolygon createMultiPolygon(Collection<Geometry> collection) {
    Polygon p = null;
    MultiPolygon combined = null;
    // creates a union out of the polygons in the collection
    for (Geometry g : collection) {
      if (p == null)
        p = (Polygon) g;
      else {
        if (combined == null)
          combined = (MultiPolygon) p.union((Polygon) g);
        else
          combined = (MultiPolygon) combined.union((Polygon) g);
      }
    }
    return combined;
  }

  /**
   * Gets the <code>Geometry</code> for each boundary object in the given <code>String</code> array.
   * 
   * @param type <code>String</code> defining the boundary type (bbox, bpoint, bpoly)
   * @return <code>ArrayList</code> containing the <code>Geometry</code> objects for each input
   *         boundary object sorted by the given order of the array.
   */
  public ArrayList<Geometry> getGeometry(String type) {

    ArrayList<Geometry> geoms = new ArrayList<>();
    switch (type) {
      case "bbox":
        // add the bbox geoms from the geometry collection to the arraylist
        geoms.addAll(bboxColl);
        break;
      case "bpoint":
        // add the bpoint geoms from the geometry collection to the arraylist
        geoms.addAll(bpointColl);
        break;
      case "bpoly":
        // add the bpoly geoms from the geometry collection to the arraylist
        geoms.addAll(bpolyColl);
        break;
    }
    return geoms;
  }

  /**
   * Finds and returns the EPSG code of the given point, which is needed for
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.inputValidation.InputValidator#createCircularPolygons
   * createCircularPolygon}. Adapted code from UTMCodeFromLonLat.java class in the osmatrix project
   * (© by Michael Auer)
   * 
   * @param lon Longitude coordinate of the point.
   * @param lat Latitude coordinate of the point.
   * @return <code>String</code> representing the corresponding EPSG code.
   */
  private String findEPSG(double lon, double lat) {

    if (lat >= 84)
      return "EPSG:32661"; // UPS North
    if (lat < -80)
      return "EPSG:32761"; // UPS South

    int zoneNumber = (int) (Math.floor((lon + 180) / 6) + 1);
    if (lat >= 56.0 && lat < 64.0 && lon >= 3.0 && lon < 12.0)
      zoneNumber = 32;
    // Special zones for Svalbard
    if (lat >= 72.0 && lat < 84.0) {
      if (lon >= 0.0 && lon < 9.0)
        zoneNumber = 31;
      else if (lon >= 9.0 && lon < 21.0)
        zoneNumber = 33;
      else if (lon >= 21.0 && lon < 33.0)
        zoneNumber = 35;
      else if (lon >= 33.0 && lon < 42.0)
        zoneNumber = 37;
    }
    String isNorth = (lat > 0) ? "6" : "7";
    String zone = (zoneNumber < 10) ? "0" + zoneNumber : "" + zoneNumber;
    return "EPSG:32" + isNorth + zone;
  }

  /*
   * Getters start here
   */
  public byte getBoundary() {
    return boundary;
  }

  public String[] getBoundaryIds() {
    return boundaryIds;
  }

  public BoundingBox getBbox() {
    return bbox;
  }

  public Geometry getBpointGeom() {
    return bpointGeom;
  }

  public Polygon getBpoly() {
    return bpoly;
  }

  public boolean getShowMetadata() {
    return this.showMetadata;
  }
}
