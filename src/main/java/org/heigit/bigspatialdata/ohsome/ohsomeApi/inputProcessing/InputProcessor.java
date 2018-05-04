package org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.executor.RequestParameters;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.oshdb.DbConnData;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.oshdb.ExtractMetadata;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.OSMContributionView;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.OSMEntitySnapshotView;
import org.heigit.bigspatialdata.oshdb.api.object.OSHDBMapReducible;
import org.heigit.bigspatialdata.oshdb.osm.OSMType;
import org.heigit.bigspatialdata.oshdb.util.time.OSHDBTimestamps;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygonal;

/**
 * Holds general input processing and validation methods and validates specific parameters given by
 * the request. Uses geometry methods from
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.GeometryBuilder
 * GeometryBuilder} and utils from
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.Utils Utils}. Throws exceptions
 * depending on their validity.
 */
public class InputProcessor {

  private BoundaryType boundary;
  private String[] boundaryValues;
  private String boundaryValuesGeoJson;
  private EnumSet<OSMType> osmTypes;
  private String[] timeData;
  private boolean showMetadata;
  private GeometryBuilder geomBuilder;
  private Utils utils;

  /**
   * Processes the input parameters from the given request.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer MapReducer} object
   *         including the settings derived from the given parameters.
   */
  @SuppressWarnings("unchecked") // intentionally unchecked
  public <T extends OSHDBMapReducible> MapReducer<T> processParameters(
      MapReducer<? extends OSHDBMapReducible> mapRed, RequestParameters rPs) throws Exception {

    boolean isPost = rPs.isPost();
    boolean isSnapshot = rPs.isSnapshot();
    String bboxes = rPs.getBboxes();
    String bcircles = rPs.getBcircles();
    String bpolys = rPs.getBpolys();
    String[] types = rPs.getTypes();
    String[] keys = rPs.getKeys();
    String[] values = rPs.getValues();
    String[] time = rPs.getTime();
    String[] userids = rPs.getUserids();
    String showMetadata = rPs.getShowMetadata();
    geomBuilder = new GeometryBuilder();
    utils = new Utils();
    if (isPost) {
      bboxes = createEmptyStringIfNull(bboxes);
      bcircles = createEmptyStringIfNull(bcircles);
      bpolys = createEmptyStringIfNull(bpolys);
      types = createEmptyArrayIfNull(types);
      keys = createEmptyArrayIfNull(keys);
      values = createEmptyArrayIfNull(values);
      userids = createEmptyArrayIfNull(userids);
      time = createEmptyArrayIfNull(time);
    }
    // database
    if (isSnapshot) {
      if (DbConnData.keytables == null)
        mapRed = OSMEntitySnapshotView.on(DbConnData.h2Db);
      else if (DbConnData.igniteDb == null)
        mapRed = OSMEntitySnapshotView.on(DbConnData.h2Db).keytables(DbConnData.keytables);
      else
        mapRed = OSMEntitySnapshotView.on(DbConnData.igniteDb).keytables(DbConnData.keytables);
    } else {
      if (DbConnData.keytables == null)
        mapRed = OSMContributionView.on(DbConnData.h2Db);
      else if (DbConnData.igniteDb == null)
        mapRed = OSMContributionView.on(DbConnData.h2Db).keytables(DbConnData.keytables);
      else
        mapRed = OSMContributionView.on(DbConnData.igniteDb).keytables(DbConnData.keytables);
    }
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
    checkBoundaryParams(bboxes, bcircles, bpolys);
    try {
      switch (boundary) {
        case NOBOUNDARY:
          if (ExtractMetadata.dataPoly == null)
            throw new BadRequestException(
                "You need to define one boundary parameter (bboxes, bcircles, or bpolys).");
          mapRed = mapRed.areaOfInterest((Geometry & Polygonal) ExtractMetadata.dataPoly);
          break;
        case BBOXES:
          mapRed = mapRed
              .areaOfInterest((Geometry & Polygonal) geomBuilder.createBboxes(boundaryValues));
          break;
        case BCIRCLES:
          mapRed = mapRed.areaOfInterest(
              (Geometry & Polygonal) geomBuilder.createCircularPolygons(boundaryValues));
          break;
        case BPOLYS:
          if (boundaryValues == null)
            mapRed = mapRed.areaOfInterest(
                (Geometry & Polygonal) geomBuilder.createGeometryFromGeoJson(bpolys, this));
          else
            mapRed = mapRed
                .areaOfInterest((Geometry & Polygonal) geomBuilder.createBpolys(boundaryValues));
          break;
        default:
          throw new BadRequestException(
              "Your provided boundary parameter (bboxes, bcircles, or bpolys) does not fit its format, "
                  + "or you defined more than one boundary parameter.");
      }
    } catch (ClassCastException e) {
      throw new BadRequestException(
          "The content of the provided boundary parameter (bboxes, bcircles, or bpolys) cannot be processed.");
    }
    mapRed = mapRed.osmTypes(checkTypes(types));
    mapRed = extractTime(mapRed, time, isSnapshot);
    mapRed = checkKeysValues(mapRed, keys, values);
    if (userids.length != 0) {
      checkUserids(userids);
      Set<Integer> useridSet = new HashSet<>();
      for (String user : userids) {
        useridSet.add(Integer.valueOf(user));
      }
      mapRed = mapRed.where(entity -> {
        return useridSet.contains(entity.getUserId());
      });
    } else {
      // do nothing --> all users will be used
    }
    return (MapReducer<T>) mapRed;
  }

  /**
   * Checks the given boundary parameter(s), sets a corresponding enum (NOBOUNDARY for no boundary,
   * BBOXES for bboxes, BCIRCLES for bcircles, BPOLYS for bpolys) and saves the splitted coordinates
   * into an array (in case of non-GeoJSON). Only one (or none) of the boundary parameters is
   * allowed to have content in it.
   * 
   * @param bboxes <code>String</code> containing the bounding boxes separated via a pipe (|) and
   *        optional custom names at each first coordinate appended with a colon (:).
   * @param bcircles <code>String</code> containing the bounding points separated via a pipe (|) and
   *        optional custom names at each first coordinate appended with a colon (:).
   * @param bpolys <code>String</code> containing the bounding polygons separated via a pipe (|) and
   *        optional custom names at each first coordinate appended with a colon (:).
   */
  private void checkBoundaryParams(String bboxes, String bcircles, String bpolys) {
    if (bboxes.isEmpty() && bcircles.isEmpty() && bpolys.isEmpty()) {
      boundary = BoundaryType.NOBOUNDARY;
    } else if (!bboxes.isEmpty() && bcircles.isEmpty() && bpolys.isEmpty()) {
      boundary = BoundaryType.BBOXES;
      boundaryValues = utils.splitBoundaryParam(bboxes, boundary);
    } else if (bboxes.isEmpty() && !bcircles.isEmpty() && bpolys.isEmpty()) {
      boundary = BoundaryType.BCIRCLES;
      boundaryValues = utils.splitBoundaryParam(bcircles, boundary);
    } else if (bboxes.isEmpty() && bcircles.isEmpty() && !bpolys.isEmpty()) {
      boundary = BoundaryType.BPOLYS;
      if (bpolys.startsWith("{")) {
        // geoJson expected
        boundaryValues = null;
      } else {
        boundaryValues = utils.splitBoundaryParam(bpolys, boundary);
      }
    } else
      throw new BadRequestException(
          "Your provided boundary parameter (bboxes, bcircles, or bpolys) does not fit its format, "
              + "or you defined more than one boundary parameter.");
  }

  /**
   * Checks and extracts the content of the types parameter.
   * 
   * @param types <code>String</code> array containing one, two, or all 3 OSM types (node, way,
   *        relation). If the array is empty, all three types are used.
   * @return <code>EnumSet</code> containing the requested OSM type(s).
   * @throws BadRequestException if the content of the parameter does not represent one, two, or all
   *         three OSM types
   */
  private EnumSet<OSMType> checkTypes(String[] types) throws BadRequestException {
    if (types.length > 3) {
      throw new BadRequestException(
          "Parameter 'types' containing the OSM Types cannot have more than 3 entries.");
    } else if (types.length == 0) {
      this.osmTypes = EnumSet.of(OSMType.NODE, OSMType.WAY, OSMType.RELATION);
      return this.osmTypes;
    } else {
      this.osmTypes = EnumSet.noneOf(OSMType.class);
      for (String type : types) {
        if (type.equals("node"))
          this.osmTypes.add(OSMType.NODE);
        else if (type.equals("way"))
          this.osmTypes.add(OSMType.WAY);
        else if (type.equals("relation"))
          this.osmTypes.add(OSMType.RELATION);
        else
          throw new BadRequestException(
              "Parameter 'types' can only have 'node' and/or 'way' and/or 'relation' as its content.");
      }
      return this.osmTypes;
    }
  }

  /**
   * Checks the given keys and values parameters on their length and includes them in the
   * {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer#where(String) where(key)}, or
   * {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer#where(String, String)
   * where(key, value)} method.
   * <p>
   * The keys and values parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param mapRed current {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer
   *        MapReducer} object
   * @return {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer MapReducer} object
   *         including the filters derived from the given parameters.
   * @throws BadRequestException if there are more values than keys given
   */
  private MapReducer<? extends OSHDBMapReducible> checkKeysValues(
      MapReducer<? extends OSHDBMapReducible> mapRed, String[] keys, String[] values)
      throws BadRequestException {
    if (keys.length < values.length) {
      throw new BadRequestException(
          "There cannot be more values than keys. For each value in the values parameter, the respective key has to be provided at the same index in the keys parameter.");
    }
    if (keys.length != values.length) {
      String[] tempVal = new String[keys.length];
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
   * Extracts the information from the given time array and fills the toTimestamps[] with content
   * (in case of isSnapshot=false).
   * 
   * @param mapRed
   * @param time
   * @param isSnapshot
   * @return
   * @throws Exception
   */
  private MapReducer<? extends OSHDBMapReducible> extractTime(
      MapReducer<? extends OSHDBMapReducible> mapRed, String[] time, boolean isSnapshot)
      throws Exception {

    String[] toTimestamps = null;
    if (time.length == 1) {
      timeData = utils.extractIsoTime(time[0]);
      if (timeData[2] != null) {
        // interval is given
        toTimestamps = utils.defineToTimestamps(timeData);
        mapRed = mapRed.timestamps(new OSHDBTimestamps(timeData[0], timeData[1], timeData[2]));
      } else if (timeData[1] != null) {
        mapRed = mapRed.timestamps(timeData[0], timeData[1]);
      } else {
        if (!isSnapshot)
          throw new BadRequestException(
              "You need to give at least two timestamps or a time interval for this resource.");
        mapRed = mapRed.timestamps(timeData[0]);
      }
    } else if (time.length == 0) {
      if (!isSnapshot) {
        toTimestamps = new String[] {ExtractMetadata.fromTstamp, ExtractMetadata.toTstamp};
        mapRed = mapRed.timestamps(ExtractMetadata.fromTstamp, ExtractMetadata.toTstamp);
      } else {
        mapRed = mapRed.timestamps(ExtractMetadata.toTstamp);
      }
    } else {
      // list of timestamps
      int tCount = 1;
      for (String timestamp : time) {
        utils.checkIsoConformity(timestamp, "timestamp number " + tCount);
        tCount++;
      }
      if (!isSnapshot)
        toTimestamps = utils.defineToTimestamps(time);
      String firstElem = time[0];
      time = ArrayUtils.remove(time, 0);
      mapRed = mapRed.timestamps(firstElem, firstElem, time);
    }
    utils.setToTimestamps(toTimestamps);
    return mapRed;
  }

  /**
   * Checks the content of the userids <code>String</code> array.
   * 
   * @param userids String array containing the OSM user IDs.
   * @throws BadRequestException if one of the userids is invalid
   */
  private void checkUserids(String[] userids) {
    for (String user : userids) {
      try {
        Long.valueOf(user);
      } catch (NumberFormatException e) {
        throw new BadRequestException(
            "The userids parameter can only contain valid OSM userids, which are always a positive whole number");
      }
    }
  }

  /**
   * Creates an empty array if an input parameter of a POST request is null.
   * 
   * @param toCheck <code>String</code> array, which is checked.
   * @return <code>String</code> array, which is empty.
   */
  private String[] createEmptyArrayIfNull(String[] toCheck) {
    if (toCheck == null)
      toCheck = new String[0];
    return toCheck;
  }

  /**
   * Creates an empty <code>String</code>, if a given boundary input parameter of a POST request is
   * null.
   * 
   * @param toCheck <code>String</code>, which is checked.
   * @return <code>String</code>, which is empty, but not null.
   */
  private String createEmptyStringIfNull(String toCheck) {
    if (toCheck == null)
      toCheck = "";
    return toCheck;
  }

  /*
   * Getters start here
   */
  public BoundaryType getBoundaryType() {
    return boundary;
  }

  public String[] getBoundaryValues() {
    return boundaryValues;
  }

  public String getBoundaryValuesGeoJson() {
    return boundaryValuesGeoJson;
  }

  public boolean getShowMetadata() {
    return this.showMetadata;
  }

  public EnumSet<OSMType> getOsmTypes() {
    return osmTypes;
  }

  public GeometryBuilder getGeomBuilder() {
    return geomBuilder;
  }

  public Utils getUtils() {
    return utils;
  }

  public void setUtils(Utils utils) {
    this.utils = utils;
  }
}
