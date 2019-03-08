package org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.geojson.GeoJsonObject;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.RequestParameters;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.utils.RequestUtils;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBIgnite;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBIgnite.ComputeMode;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.OSMContributionView;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.OSMEntitySnapshotView;
import org.heigit.bigspatialdata.oshdb.api.object.OSHDBMapReducible;
import org.heigit.bigspatialdata.oshdb.osm.OSMType;
import org.heigit.bigspatialdata.oshdb.util.geometry.OSHDBGeometryBuilder;
import org.heigit.bigspatialdata.oshdb.util.time.ISODateTimeParser;
import org.heigit.bigspatialdata.oshdb.util.time.OSHDBTimestamps;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygonal;
import org.wololo.jts2geojson.GeoJSONWriter;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Holds general input processing and validation methods and validates specific parameters given by
 * the request. Uses geometry methods from
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.GeometryBuilder
 * GeometryBuilder} and inputProcessingUtils from
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.InputProcessingUtils
 * InputProcessingUtils}. Throws exceptions depending on their validity.
 */
public class InputProcessor {

  /*
   * Represents about 1/500 of 180° * 360°.
   */
  public static final int COMPUTE_MODE_THRESHOLD = 130;
  private GeometryBuilder geomBuilder;
  private InputProcessingUtils utils;
  private ProcessingData processingData;
  private HttpServletRequest servletRequest;
  private boolean isSnapshot;
  private boolean isDensity;
  private String requestUrl;
  private boolean includeTags;
  private boolean includeOSMMetadata;

  public InputProcessor(HttpServletRequest servletRequest, boolean isSnapshot, boolean isDensity) {
    this.servletRequest = servletRequest;
    this.isSnapshot = isSnapshot;
    this.isDensity = isDensity;
    processingData =
        new ProcessingData(new RequestParameters(servletRequest.getMethod(), isSnapshot, isDensity,
            servletRequest.getParameter("bboxes"), servletRequest.getParameter("bcircles"),
            servletRequest.getParameter("bpolys"), servletRequest.getParameterValues("types"),
            servletRequest.getParameterValues("keys"), servletRequest.getParameterValues("values"),
            servletRequest.getParameterValues("time"), servletRequest.getParameter("format"),
            servletRequest.getParameter("showMetadata"), ProcessingData.getTimeout()), servletRequest);
    this.requestUrl = RequestUtils.extractRequestUrl(servletRequest);
  }

  public InputProcessor(ProcessingData processingData) {
    this.processingData = processingData;
  }

  public <T extends OSHDBMapReducible> MapReducer<T> processParameters() throws Exception {
    return this.processParameters(null);
  }

  /**
   * Processes the input parameters from the given request.
   * 
   * @return {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer MapReducer} object
   *         including the settings derived from the given parameters.
   */
  @SuppressWarnings("unchecked") // unchecked to allow cast of (MapReducer<T>) to mapRed
  public <T extends OSHDBMapReducible> MapReducer<T> processParameters(ComputeMode forceComputeMode)
      throws Exception {
    String bboxes = createEmptyStringIfNull(processingData.getRequestParameters().getBboxes());
    String bcircles = createEmptyStringIfNull(processingData.getRequestParameters().getBcircles());
    String bpolys = createEmptyStringIfNull(processingData.getRequestParameters().getBpolys());
    String[] types =
        splitParamOnComma(createEmptyArrayIfNull(processingData.getRequestParameters().getTypes()));
    String[] keys =
        splitParamOnComma(createEmptyArrayIfNull(processingData.getRequestParameters().getKeys()));
    String[] values = splitParamOnComma(
        createEmptyArrayIfNull(processingData.getRequestParameters().getValues()));
    String[] time =
        splitParamOnComma(createEmptyArrayIfNull(processingData.getRequestParameters().getTime()));
    String format = createEmptyStringIfNull(processingData.getRequestParameters().getFormat());
    String showMetadata =
        createEmptyStringIfNull(processingData.getRequestParameters().getShowMetadata());
    double timeout = defineRequestTimeout();
    // overwriting RequestParameters object with splitted/non-null parameters
    processingData.setRequestParameters(
        new RequestParameters(servletRequest.getMethod(), isSnapshot, isDensity, bboxes, bcircles,
            bpolys, types, keys, values, time, format, showMetadata, timeout));
    processingData.setFormat(format);
    MapReducer<? extends OSHDBMapReducible> mapRed = null;
    processingData.setBoundaryType(setBoundaryType(bboxes, bcircles, bpolys));
    geomBuilder = new GeometryBuilder(processingData);
    utils = new InputProcessingUtils();
    Geometry boundary;
    try {
      switch (processingData.getBoundaryType()) {
        case NOBOUNDARY:
          if (ExtractMetadata.dataPoly == null) {
            throw new BadRequestException(ExceptionMessages.NO_BOUNDARY);
          }
          boundary = ExtractMetadata.dataPoly;
          break;
        case BBOXES:
          processingData.setBoundaryValues(utils.splitBboxes(bboxes).toArray(new String[] {}));
          boundary = geomBuilder.createBboxes(processingData.getBoundaryValues());
          break;
        case BCIRCLES:
          processingData.setBoundaryValues(utils.splitBcircles(bcircles).toArray(new String[] {}));
          boundary = geomBuilder.createCircularPolygons(processingData.getBoundaryValues());
          break;
        case BPOLYS:
          if (bpolys.matches("^\\s*\\{[\\s\\S]*")) {
            boundary = geomBuilder.createGeometryFromGeoJson(bpolys, this);
          } else {
            processingData.setBoundaryValues(utils.splitBpolys(bpolys).toArray(new String[] {}));
            boundary = geomBuilder.createBpolys(processingData.getBoundaryValues());
          }
          break;
        default:
          throw new BadRequestException(ExceptionMessages.BOUNDARY_PARAM_FORMAT_OR_COUNT);
      }
    } catch (ClassCastException e) {
      throw new BadRequestException(ExceptionMessages.BOUNDARY_PARAM_FORMAT);
    }

    if (DbConnData.db instanceof OSHDBIgnite) {
      final OSHDBIgnite dbIgnite = (OSHDBIgnite) DbConnData.db;
      if (forceComputeMode != null) {
        dbIgnite.computeMode(forceComputeMode);
      } else {
        ComputeMode computeMode;
        double boundarySize = boundary.getEnvelope().getArea();
        if (boundarySize <= COMPUTE_MODE_THRESHOLD) {
          computeMode = ComputeMode.LocalPeek;
        } else {
          computeMode = ComputeMode.ScanQuery;
        }
        dbIgnite.computeMode(computeMode);
      }
    }

    DbConnData.db.timeout(timeout);

    if (isSnapshot) {
      if (DbConnData.keytables == null) {
        mapRed = OSMEntitySnapshotView.on(DbConnData.db);
      } else {
        mapRed = OSMEntitySnapshotView.on(DbConnData.db).keytables(DbConnData.keytables);
      }
    } else {
      if (DbConnData.keytables == null) {
        mapRed = OSMContributionView.on(DbConnData.db);
      } else {
        mapRed = OSMContributionView.on(DbConnData.db).keytables(DbConnData.keytables);
      }
    }
    if (boundary.isRectangle()) {
      mapRed =
          mapRed.areaOfInterest(OSHDBGeometryBuilder.boundingBoxOf(boundary.getEnvelopeInternal()));
    } else {
      mapRed = mapRed.areaOfInterest((Geometry & Polygonal) boundary);
    }

    if (showMetadata == null) {
      processingData.setShowMetadata(false);
    } else if ("true".equalsIgnoreCase(showMetadata.replaceAll("\\s", ""))
        || "yes".equalsIgnoreCase(showMetadata.replaceAll("\\s", ""))) {
      processingData.setShowMetadata(true);
    } else if ("false".equalsIgnoreCase(showMetadata.replaceAll("\\s", ""))
        || "".equals(showMetadata.replaceAll("\\s", ""))
        || "no".equalsIgnoreCase(showMetadata.replaceAll("\\s", ""))) {
      processingData.setShowMetadata(false);
    } else {
      throw new BadRequestException(ExceptionMessages.SHOWMETADATA_PARAM);
    }
    checkFormat(processingData.getFormat());
    if ("geojson".equalsIgnoreCase(processingData.getFormat())) {
      GeoJSONWriter writer = new GeoJSONWriter();
      Collection<Geometry> boundaryColl = processingData.getBoundaryColl();
      GeoJsonObject[] geoJsonGeoms = new GeoJsonObject[boundaryColl.size()];
      for (int i = 0; i < geoJsonGeoms.length; i++) {
        try {
          geoJsonGeoms[i] = new ObjectMapper().readValue(
              writer.write((Geometry) boundaryColl.toArray()[i]).toString(), GeoJsonObject.class);
        } catch (IOException e) {
          throw new BadRequestException(ExceptionMessages.BOUNDARY_PARAM_GEOJSON_FORMAT);
        }
      }
      processingData.setGeoJsonGeoms(geoJsonGeoms);
    }
    defineOSMTypes(types);
    mapRed = mapRed.osmType((EnumSet<OSMType>) processingData.getOsmTypes());
    mapRed = extractTime(mapRed, time, isSnapshot);
    mapRed = extractKeysValues(mapRed, keys, values);
    return (MapReducer<T>) mapRed;
  }

  /**
   * Defines the OSMType(s) out of the given String[].
   * 
   * @param types <code>String</code> array containing one, two, or all 3 OSM types (node, way,
   *        relation). If the array is empty, all three types are used.
   * @throws BadRequestException if the content of the parameter does not represent one, two, or all
   *         three OSM types
   */
  public void defineOSMTypes(String[] types) throws BadRequestException {
    types = createEmptyArrayIfNull(types);
    checkOSMTypes(types);
    if (types.length == 0) {
      processingData.setOsmTypes(EnumSet.of(OSMType.NODE, OSMType.WAY, OSMType.RELATION));
    } else {
      processingData.setOsmTypes(EnumSet.noneOf(OSMType.class));
      for (String type : types) {
        if ("node".equalsIgnoreCase(type)) {
          processingData.getOsmTypes().add(OSMType.NODE);
        } else if ("way".equalsIgnoreCase(type)) {
          processingData.getOsmTypes().add(OSMType.WAY);
        } else {
          processingData.getOsmTypes().add(OSMType.RELATION);
        }
      }
    }
  }

  /**
   * Splits the given input parameter on ',' if it has a length of 1 and contains ',' at [0].
   * Returns a String array containing the splits.
   * 
   * @param param <code>String</code> array containing the content to split
   * @return <code>String</code> array containing the splitted parameter content
   */
  public String[] splitParamOnComma(String[] param) {
    if (param.length == 1 && param[0].contains(",")) {
      return param[0].replaceAll("\\s", "").split(",");
    }
    return param;
  }

  /**
   * Creates an empty array if an input parameter of a POST request is null.
   * 
   * @param toCheck <code>String</code> array, which is checked.
   * @return <code>String</code> array, which is empty.
   */
  public String[] createEmptyArrayIfNull(String[] toCheck) {
    if (toCheck == null) {
      toCheck = new String[0];
    }
    return toCheck;
  }

  /**
   * Creates an empty <code>String</code>, if a given input parameter is null.
   * 
   * @param toCheck <code>String</code>, which is checked.
   * @return <code>String</code>, which may be empty but not null.
   */
  public String createEmptyStringIfNull(String toCheck) {
    if (toCheck == null) {
      toCheck = "";
    }
    return toCheck;
  }

  /** Checks the given keys and values String[] on their length. */
  public void checkKeysValues(String[] keys, String[] values) throws BadRequestException {
    if (values != null && keys.length < values.length) {
      throw new BadRequestException(ExceptionMessages.KEYS_VALUES_RATIO_INVALID);
    }
  }

  /**
   * Compares the keys and values arrays with each other. Returns true only if keys=keys2 and
   * values=values2.
   */
  public boolean compareKeysValues(String[] keys, String[] keys2, String[] values,
      String[] values2) {
    return (Arrays.equals(keys, keys2) && Arrays.equals(values, values2));
  }

  /**
   * Adds the filter parameters from keys and values to a list as tags. Only used in the processing
   * of /share requests.
   */
  public List<Pair<String, String>> addFilterKeysVals(String[] keys, String[] values,
      String[] keys2, String[] values2) {
    ArrayList<Pair<String, String>> tags = new ArrayList<>();
    for (int i = 0; i < keys.length; i++) {
      String key = keys[i];
      Pair<String, String> tag;
      if (i >= values.length) {
        tag = new ImmutablePair<>(key, "");
      } else {
        tag = new ImmutablePair<>(key, values[i]);
      }
      tags.add(tag);
    }
    for (int i = 0; i < keys2.length; i++) {
      String key = keys2[i];
      Pair<String, String> tag;
      if (i >= values2.length) {
        tag = new ImmutablePair<>(key, "");
      } else {
        tag = new ImmutablePair<>(key, values2[i]);
      }
      tags.add(tag);
    }
    // sorting to have all Pair<key,""> at the end of the list
    Collections.sort(tags, new Comparator<Pair<String, String>>() {
      @Override
      public int compare(Pair<String, String> p1, Pair<String, String> p2) {
        if ("".equals(p1.getValue()) && "".equals(p2.getValue())) {
          return 0;
        } else if ("".equals(p1.getValue()) && !"".equals(p2.getValue())) {
          return 1;
        } else if (!"".equals(p1.getValue()) && "".equals(p2.getValue())) {
          return -1;
        } else {
          return 0;
        }
      }
    });
    return tags;
  }

  /**
   * Compares the given keys arrays and adds those of the first to the second, if it has some, which
   * the second one has not. Only used in the processing of /share requests.
   */
  public String[] addFilterKeys(String[] keys, String[] keys2) {
    if (keys.length == 0) {
      return keys2;
    }
    if (Arrays.equals(keys, keys2)) {
      return keys2;
    }
    List<String> keysList = new ArrayList<>(Arrays.asList(keys2));
    for (String s : keys) {
      if (!keysList.contains(s)) {
        keysList.add(s);
      }
    }
    return keysList.toArray(new String[keysList.size()]);
  }

  /**
   * Used in /share and /ratio requests. If isShare: includes the keys and values parameters within
   * keys2 and values2.
   */
  public Pair<String[], String[]> processKeys2Vals2(String[] keys2, String[] values2,
      boolean isShare, RequestParameters requestParams) {
    keys2 = createEmptyArrayIfNull(keys2);
    values2 = createEmptyArrayIfNull(values2);
    if (isShare) {
      List<Pair<String, String>> keys2Vals2;
      if (requestParams.getValues().length == 0) {
        keys2 = addFilterKeys(requestParams.getKeys(), keys2);
      } else if (keys2.length == 0) {
        keys2 = requestParams.getKeys();
        values2 = requestParams.getValues();
      } else {
        keys2Vals2 =
            addFilterKeysVals(requestParams.getKeys(), requestParams.getValues(), keys2, values2);
        String[] newKeys2 = new String[keys2Vals2.size()];
        String[] newValues2 = new String[keys2Vals2.size()];
        for (int i = 0; i < keys2Vals2.size(); i++) {
          Pair<String, String> tag = keys2Vals2.get(i);
          newKeys2[i] = tag.getKey();
          newValues2[i] = tag.getValue();
        }
        keys2 = newKeys2;
        values2 =
            Arrays.stream(newValues2).filter(value -> !"".equals(value)).toArray(String[]::new);
      }
    }
    return new ImmutablePair<>(keys2, values2);
  }

  /**
   * Processes the properties parameter used in data-extraction ressources and sets the respective
   * boolean values includeTags and includeOSMMetadata.
   * 
   * @throws BadRequestException if the properties parameter contains invalid content
   */
  public void processPropertiesParam() throws BadRequestException {
    String[] properties =
        splitParamOnComma(createEmptyArrayIfNull(servletRequest.getParameterValues("properties")));
    if (properties.length > 2) {
      throw new BadRequestException(ExceptionMessages.PROPERTIES_PARAM);
    }
    for (String property : properties) {
      if ("tags".equalsIgnoreCase(property)) {
        this.includeTags = true;
      } else if ("metadata".equalsIgnoreCase(property)) {
        this.includeOSMMetadata = true;
      } else {
        throw new BadRequestException(ExceptionMessages.PROPERTIES_PARAM);
      }
    }
  }

  public String getRequestUrlIfGetRequest() {
    if (!"post".equalsIgnoreCase(servletRequest.getMethod())) {
      return this.getRequestUrl();
    }
    return null;
  }
  
  /**
   * Checks the given keys and values parameters on their length and includes them in the
   * {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer#where(String) where(key)}, or
   * {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer#where(String, String)
   * where(key, value)} method.
   * 
   * <p>
   * The keys and values parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @param mapRed current {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer
   *        MapReducer} object
   * @return {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer MapReducer} object
   *         including the filters derived from the given parameters.
   * @throws BadRequestException if there are more values than keys given
   */
  private MapReducer<? extends OSHDBMapReducible> extractKeysValues(
      MapReducer<? extends OSHDBMapReducible> mapRed, String[] keys, String[] values)
      throws BadRequestException {
    checkKeysValues(keys, values);
    if (keys.length != values.length) {
      String[] tempVal = new String[keys.length];
      for (int a = 0; a < values.length; a++) {
        tempVal[a] = values[a];
      }
      for (int i = values.length; i < keys.length; i++) {
        tempVal[i] = "";
      }
      values = tempVal;
    }
    // prerequisites: both arrays (keys and values) must be of the same length
    // and key-value pairs need to be at the same index in both arrays
    for (int i = 0; i < keys.length; i++) {
      if ("".equals(values[i])) {
        mapRed = mapRed.osmTag(keys[i]);
      } else {
        mapRed = mapRed.osmTag(keys[i], values[i]);
      }
    }
    return mapRed;
  }

  /**
   * Extracts the information from the given time array and fills the toTimestamps[] with content
   * (in case of isSnapshot=false).
   */
  private MapReducer<? extends OSHDBMapReducible> extractTime(
      MapReducer<? extends OSHDBMapReducible> mapRed, String[] time, boolean isSnapshot)
      throws Exception {
    String[] toTimestamps = null;
    String[] timeData;
    if (time.length == 0 || time[0].replaceAll("\\s", "").length() == 0) {
      if (!isSnapshot) {
        toTimestamps = new String[] {ExtractMetadata.fromTstamp, ExtractMetadata.toTstamp};
        mapRed = mapRed.timestamps(ExtractMetadata.fromTstamp, ExtractMetadata.toTstamp);
      } else {
        mapRed = mapRed.timestamps(ExtractMetadata.toTstamp);
      }
    } else if (time.length == 1) {
      timeData = utils.extractIsoTime(time[0]);
      if (!isSnapshot) {
        toTimestamps = utils.defineToTimestamps(timeData);
      }
      if (timeData[2] != null) {
        // interval is given
        mapRed = mapRed.timestamps(new OSHDBTimestamps(timeData[0], timeData[1], timeData[2]));
      } else if (timeData[1] != null) {
        mapRed = mapRed.timestamps(timeData[0], timeData[1]);
      } else {
        if (!isSnapshot) {
          throw new BadRequestException(ExceptionMessages.TIME_FORMAT_CONTRIBUTION);
        }
        mapRed = mapRed.timestamps(timeData[0]);
      }
    } else {
      utils.checkTimestampsOnIsoConformity(time);
      for (String timestamp : time) {
        ZonedDateTime zdt = ISODateTimeParser.parseISODateTime(timestamp);
        utils.checkTemporalExtend(zdt.format(DateTimeFormatter.ISO_DATE_TIME));
      }
      timeData = utils.sortTimestamps(time);
      if (!isSnapshot) {
        toTimestamps = utils.defineToTimestamps(timeData);
      }
      String firstElem = timeData[0];
      timeData = ArrayUtils.remove(timeData, 0);
      mapRed = mapRed.timestamps(firstElem, firstElem, timeData);
    }
    utils.setToTimestamps(toTimestamps);
    return mapRed;
  }

  /** Checks the given OSMType(s) String[] on its length and content. */
  private void checkOSMTypes(String[] types) throws BadRequestException {
    if (types.length > 3) {
      throw new BadRequestException("Parameter 'types' (and 'types2') containing the OSM Types "
          + "cannot have more than 3 entries.");
    } else if (types.length == 0) {
      // do nothing
    } else {
      for (String type : types) {
        if (!"node".equalsIgnoreCase(type) && !"way".equalsIgnoreCase(type)
            && !"relation".equalsIgnoreCase(type)) {
          throw new BadRequestException(
              "Parameter 'types' can only have 'node' and/or 'way' and/or 'relation' "
                  + "as its content.");
        }
      }
    }
  }

  /**
   * Checks the content of the given format parameter.
   */
  private void checkFormat(String format) throws BadRequestException {
    if (format != null && !format.isEmpty() && !"geojson".equalsIgnoreCase(format)
        && !"json".equalsIgnoreCase(format) && !"csv".equalsIgnoreCase(format)) {
      throw new BadRequestException(
          "The given 'format' parameter is invalid. Please choose between 'geojson'(only available"
              + " for /groupBy/boundary and data extraction requests), 'json', or 'csv'.");
    }
  }

  /**
   * Defines the timeout for this request depending on the given timeout parameter. If it is smaller
   * than the predefined value, it is used for this request.
   * 
   * @return <code>double</code> value defining the timeout for this request
   * @throws BadRequestException if the given timeout is larger than the predefined one
   */
  private double defineRequestTimeout() throws BadRequestException {
    double timeout = ProcessingData.getTimeout();
    String requestTimeoutString = createEmptyStringIfNull(servletRequest.getParameter("timeout"));
    if (!requestTimeoutString.isEmpty()) {
      double requestTimeoutDouble;
      try {
        requestTimeoutDouble = Double.parseDouble(requestTimeoutString);
      } catch (Exception e) {
        throw new BadRequestException(ExceptionMessages.TIMEOUT_FORMAT);
      }
      if (requestTimeoutDouble <= timeout) {
        timeout = requestTimeoutDouble;
      } else {
        throw new BadRequestException(
            ExceptionMessages.TIMEOUT + ProcessingData.getTimeout() + " seconds");
      }
    }
    return timeout;
  }

  /**
   * Sets a corresponding enum (NOBOUNDARY for no boundary, BBOXES for bboxes, BCIRCLES for
   * bcircles, BPOLYS for bpolys) based on the given boundary parameter(s). Only one of them is
   * allowed to have content in it.
   * 
   * @param bboxes <code>String</code> containing the bounding boxes separated via a pipe (|) and
   *        optional custom names at each first coordinate appended with a colon (:).
   * @param bcircles <code>String</code> containing the bounding circles separated via a pipe (|)
   *        and optional custom names at each first coordinate appended with a colon (:).
   * @param bpolys <code>String</code> containing the bounding polygons separated via a pipe (|) and
   *        optional custom names at each first coordinate appended with a colon (:).
   * @throws BadRequestException if there is not exactly one boundary parameter defined
   */
  private BoundaryType setBoundaryType(String bboxes, String bcircles, String bpolys)
      throws BadRequestException {
    if (bboxes.isEmpty() && bcircles.isEmpty() && bpolys.isEmpty()) {
      throw new BadRequestException(ExceptionMessages.NO_BOUNDARY);
    } else if (!bboxes.isEmpty() && bcircles.isEmpty() && bpolys.isEmpty()) {
      return BoundaryType.BBOXES;
    } else if (bboxes.isEmpty() && !bcircles.isEmpty() && bpolys.isEmpty()) {
      return BoundaryType.BCIRCLES;
    } else if (bboxes.isEmpty() && bcircles.isEmpty() && !bpolys.isEmpty()) {
      return BoundaryType.BPOLYS;
    } else {
      throw new BadRequestException(ExceptionMessages.BOUNDARY_PARAM_FORMAT_OR_COUNT);
    }
  }

  /**
   * Gets the geometry from the currently in-use boundary object(s).
   * 
   * @return <code>Geometry</code> object of the used boundary parameter.
   */
  public Geometry getGeometry() {
    Geometry geom;
    if (BoundaryType.NOBOUNDARY == processingData.getBoundaryType()) {
      geom = ProcessingData.getDataPolyGeom();
    } else {
      geom = processingData.getRequestGeom();
    }
    return geom;
  }

  public GeometryBuilder getGeomBuilder() {
    return geomBuilder;
  }

  public InputProcessingUtils getUtils() {
    return utils;
  }

  public void setUtils(InputProcessingUtils utils) {
    this.utils = utils;
  }

  public ProcessingData getProcessingData() {
    return processingData;
  }

  public void setProcessingData(ProcessingData processingData) {
    this.processingData = processingData;
  }

  public String getRequestUrl() {
    return requestUrl;
  }

  public boolean includeTags() {
    return includeTags;
  }

  public boolean includeOSMMetadata() {
    return includeOSMMetadata;
  }
}
