package org.heigit.ohsome.ohsomeapi.inputprocessing;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ArrayUtils;
import org.geojson.GeoJsonObject;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.exception.ServiceUnavailableException;
import org.heigit.ohsome.ohsomeapi.executor.RequestParameters;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.ohsome.ohsomeapi.utils.RequestUtils;
import org.heigit.ohsome.oshdb.api.db.OSHDBIgnite;
import org.heigit.ohsome.oshdb.api.db.OSHDBIgnite.ComputeMode;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;
import org.heigit.ohsome.oshdb.api.mapreducer.OSMContributionView;
import org.heigit.ohsome.oshdb.api.mapreducer.OSMEntitySnapshotView;
import org.heigit.ohsome.oshdb.filter.FilterExpression;
import org.heigit.ohsome.oshdb.filter.FilterParser;
import org.heigit.ohsome.oshdb.util.geometry.OSHDBGeometryBuilder;
import org.heigit.ohsome.oshdb.util.mappable.OSHDBMapReducible;
import org.heigit.ohsome.oshdb.util.time.IsoDateTimeParser;
import org.heigit.ohsome.oshdb.util.time.OSHDBTimestamps;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.geom.TopologyException;
import org.wololo.jts2geojson.GeoJSONWriter;

/**
 * Holds general input processing and validation methods and validates specific parameters given by
 * the request. Uses geometry methods from {@link
 * org.heigit.ohsome.ohsomeapi.inputprocessing.GeometryBuilder GeometryBuilder} and
 * inputProcessingUtils from {@link
 * org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessingUtils InputProcessingUtils}. Throws
 * exceptions depending on their validity.
 */
public class InputProcessor {

  /*
   * Represents about 1/500 of 180° * 360°.
   */
  public static final int COMPUTE_MODE_THRESHOLD = 130;
  private GeometryBuilder geomBuilder;
  private InputProcessingUtils utils;
  private ProcessingData processingData;
  private boolean isSnapshot;
  private boolean isDensity;
  private String requestUrl;
  private String requestMethod;
  private String requestTimeout;
  private Map<String, String[]> requestParameters;
  private boolean includeTags;
  private boolean includeOSMMetadata;
  private boolean includeContributionTypes;
  private boolean clipGeometry = true;

  public InputProcessor(ProcessingData processingData) {
    this.processingData = processingData;
  }

  public InputProcessor(HttpServletRequest servletRequest, boolean isSnapshot, boolean isDensity) {
    if (DbConnData.db instanceof OSHDBIgnite) {
      checkClusterAvailability();
    }
    checkContentTypeHeader(servletRequest);
    checkParameters(servletRequest);
    this.isSnapshot = isSnapshot;
    this.isDensity = isDensity;
    processingData =
        new ProcessingData(new RequestParameters(servletRequest.getMethod(), isSnapshot, isDensity,
            servletRequest.getParameter("bboxes"), servletRequest.getParameter("bcircles"),
            servletRequest.getParameter("bpolys"),
            servletRequest.getParameterValues("time"), servletRequest.getParameter("format"),
            servletRequest.getParameter("showMetadata"), ProcessingData.getTimeout(),
            servletRequest.getParameter("filter")), servletRequest.getRequestURL().toString());
    this.requestUrl = RequestUtils.extractRequestUrl(servletRequest);
    this.requestMethod = servletRequest.getMethod();
    this.requestTimeout = servletRequest.getParameter("timeout");
    this.requestParameters = servletRequest.getParameterMap();
  }

  /**
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor
   *         #processParameters(ComputeMode) processParameters}
   */
  public <T extends OSHDBMapReducible> MapReducer<T> processParameters() throws Exception {
    return this.processParameters(null);
  }

  /**
   * Processes the input parameters from the given request.
   *
   * @return {@link org.heigit.ohsome.oshdb.api.mapreducer.MapReducer MapReducer} object
   *         including the settings derived from the given parameters.
   * @throws BadRequestException if the boundary parameter is not defined or it has an invalid
   *         format, if the geometry of given boundary cannot be parsed for the creation of the
   *         response GeoJSON or if the keys, values and types parameters are not empty, while the
   *         filter parameter is set.
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor
   *         #extractTime(MapReducer, String[], boolean) extractTime}
   */
  @SuppressWarnings("unchecked") // unchecked to allow cast of (MapReducer<T>) to mapRed
  public <T extends OSHDBMapReducible> MapReducer<T> processParameters(ComputeMode forceComputeMode)
      throws Exception {
    String bboxes = createEmptyStringIfNull(processingData.getRequestParameters().getBboxes());
    String bcircles = createEmptyStringIfNull(processingData.getRequestParameters().getBcircles());
    String bpolys = createEmptyStringIfNull(processingData.getRequestParameters().getBpolys());
    String[] time =
        splitParamOnComma(createEmptyArrayIfNull(processingData.getRequestParameters().getTime()));
    String format = createEmptyStringIfNull(processingData.getRequestParameters().getFormat());
    String showMetadata =
        createEmptyStringIfNull(processingData.getRequestParameters().getShowMetadata());
    double timeout = defineRequestTimeout();
    String filter = createEmptyStringIfNull(processingData.getRequestParameters().getFilter());
    // overwriting RequestParameters object with splitted/non-null parameters
    processingData
        .setRequestParameters(new RequestParameters(requestMethod, isSnapshot, isDensity, bboxes,
            bcircles, bpolys, time, format, showMetadata, timeout, filter));
    processingData.setFormat(format);
    MapReducer<? extends OSHDBMapReducible> mapRed;
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
          computeMode = ComputeMode.LOCAL_PEEK;
        } else {
          computeMode = ComputeMode.SCAN_QUERY;
        }
        dbIgnite.computeMode(computeMode);
      }
    }
    DbConnData.db.timeout(timeout);
    if (isSnapshot) {
      mapRed = OSMEntitySnapshotView.on(DbConnData.db);
    } else {
      mapRed = OSMContributionView.on(DbConnData.db);
    }
    if (boundary.isRectangle()) {
      mapRed =
          mapRed.areaOfInterest(OSHDBGeometryBuilder.boundingBoxOf(boundary.getEnvelopeInternal()));
    } else {
      try {
        mapRed = mapRed.areaOfInterest((Geometry & Polygonal) boundary);
      } catch (TopologyException e) {
        throw new BadRequestException(ExceptionMessages.BPOLYS_PARAM_GEOMETRY + e.getMessage());
      }
    }
    processShowMetadata(showMetadata);
    checkFormat(processingData.getFormat());
    if ("geojson".equalsIgnoreCase(processingData.getFormat())) {
      GeoJSONWriter writer = new GeoJSONWriter();
      Collection<Geometry> boundaryColl = processingData.getBoundaryList();
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
    mapRed = extractTime(mapRed, time, isSnapshot);
    // call translator and add filters to mapRed
    FilterParser fp = new FilterParser(DbConnData.tagTranslator);
    FilterExpression filterExpr = utils.parseFilter(fp, filter);
    processingData.setFilterExpression(filterExpr);
    if (!(processingData.isRatio()
        || processingData.isGroupByBoundary()
        || processingData.isFullHistory())) {
      // skip in ratio or groupByBoundary requests -> needs to be done later in the processing
      mapRed = mapRed.filter(filterExpr);
    }

    return (MapReducer<T>) mapRed;
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

  /**
   * Processes the properties parameter used in data-extraction resources and sets the respective
   * boolean values includeTags, includeOSMMetadata, unclippedGeometries, and
   * includeContributionTypes (only for the /contributions endpoints).
   *
   * @throws BadRequestException if the properties parameter contains invalid values
   */
  public void processPropertiesParam() {
    String[] properties =
        splitParamOnComma(createEmptyArrayIfNull(requestParameters.get("properties")));
    for (String property : properties) {
      if ("tags".equalsIgnoreCase(property)) {
        this.includeTags = true;
      } else if ("metadata".equalsIgnoreCase(property)) {
        this.includeOSMMetadata = true;
      } else if (RequestUtils.isContributionsExtraction(requestUrl)) {
        if ("contributionTypes".equalsIgnoreCase(property)) {
          this.includeContributionTypes = true;
        } else {
          throw new BadRequestException(ExceptionMessages.PROPERTIES_PARAM_CONTR);
        }
      } else {
        throw new BadRequestException(ExceptionMessages.PROPERTIES_PARAM);
      }
    }
  }

  /**
   * Processes the clipGeometry parameter used in data-extraction resources and sets the respective
   * boolean value 'clipGeometry'. Note: this method is called after processPropertiesParam() so it
   * could overwrite the previously defined value of 'clipGeometry'.
   */
  public void processIsUnclippedParam() {
    if (null != requestParameters.get("clipGeometry")) {
      this.clipGeometry =
          processBooleanParam("clipGeometry", requestParameters.get("clipGeometry")[0]);
    }
  }

  /** Returns the request URL if a GET request was sent. */
  public String getRequestUrlIfGetRequest(HttpServletRequest servletRequest) {
    if (!"post".equalsIgnoreCase(servletRequest.getMethod())) {
      return this.getRequestUrl();
    }
    return null;
  }

  /**
   * Checks the given filter parameter if it's null or blank. Currently used for filter2 parameter
   * of /ratio processing.
   *
   * @param filter parameter to be checked
   * @throws BadRequestException if the given filter parameter is null or blank.
   */
  public void checkFilter(String filter) {
    if (null == filter || filter.isBlank() && processingData.isRatio()) {
      throw new BadRequestException(
          "The filter2 parameter has to be defined when using a /ratio endpoint.");
    }
  }

  /**
   * Extracts the information from the given time array and fills the toTimestamps[] with content
   * (in case of isSnapshot=false).
   */
  private MapReducer<? extends OSHDBMapReducible> extractTime(
      MapReducer<? extends OSHDBMapReducible> mapRed, String[] time, boolean isSnapshot) {
    String[] toTimestamps = null;
    String[] timeData;
    if (time.length == 0 || time[0].replaceAll("\\s", "").length() == 0 && time.length == 1) {
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
        ZonedDateTime zdt = IsoDateTimeParser.parseIsoDateTime(timestamp);
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

  /**
   * Checks the content of the given format parameter.
   *
   * @throws BadRequestException if the given format parameter is invalid.
   */
  private void checkFormat(String format) {
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
   * @throws BadRequestException if the given timeout parameter is larger than the predefined one
   */
  private double defineRequestTimeout() {
    double timeout = ProcessingData.getTimeout();
    String requestTimeoutString = createEmptyStringIfNull(requestTimeout);
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
  private BoundaryType setBoundaryType(String bboxes, String bcircles, String bpolys) {
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
   * Checks, if the cluster has less active server nodes, than defined on startup.
   *
   * @throws ServiceUnavailableException in case one or more nodes are inactive.
   */
  private void checkClusterAvailability() {
    OSHDBIgnite igniteDb = (OSHDBIgnite) DbConnData.db;
    int definedNumberOfNodes = ProcessingData.getNumberOfClusterNodes();
    int currentNumberOfNodes =
        igniteDb.getIgnite().services().clusterGroup().metrics().getTotalNodes();
    if (currentNumberOfNodes < definedNumberOfNodes) {
      throw new ServiceUnavailableException("The cluster backend is currently not able to process "
          + "your request. Please try again later.");
    }
  }

  /**
   * Checks, if the given content-type header is either 'application/x-www-form-urlencoded' or
   * 'multipart/form-data'.
   *
   * @throws BadRequestException if an unsupported header is given.
   */
  private void checkContentTypeHeader(HttpServletRequest servletRequest) {
    String contentType = servletRequest.getHeader("content-type");
    if (contentType == null) {
      return;
    }
    if (!contentType.contains("application/x-www-form-urlencoded")
        && !contentType.contains("multipart/form-data")) {
      throw new BadRequestException("Unsupported content-type header found. Please make sure to "
          + "use either 'multipart/form-data' or 'application/x-www-form-urlencoded'.");
    }
  }

  /**
   * Checks, if the request does not specify any parameter or if it specifies false or repeated
   * parameters. In case of false or repeated parameters, it suggests possible parameters based on
   * fuzzy matching scores.
   *
   * @throws BadRequestException in case of no parameters, invalid parameters or if parameters are
   *     given more than once.
   */
  private void checkParameters(HttpServletRequest servletRequest) {
    if (servletRequest.getParameterMap().isEmpty()) {
      throw new BadRequestException(ExceptionMessages.NO_DEFINED_PARAMS);
    }
    List<String> possibleParameters = ResourceParameters.getResourceSpecificParams(servletRequest);
    List<String> unexpectedParams =
        ResourceParameters.checkUnexpectedParams(servletRequest, possibleParameters);
    if (!unexpectedParams.isEmpty()) {
      String unexpectedParam = unexpectedParams.get(0);
      throw new BadRequestException(
          StringSimilarity.findSimilarParameters(unexpectedParam, possibleParameters));
    }
    for (var parameter : servletRequest.getParameterMap().entrySet()) {
      if (parameter.getValue().length != 1) {
        throw new BadRequestException("Every parameter has to be unique. "
            + "You can't give more than one '" + parameter + "' parameter.");
      }
    }
  }

  /**
   * Processes the given showMetadata parameter and sets the respective value in the processingData
   * object.
   */
  private void processShowMetadata(String showMetadata) {
    processingData.setShowMetadata(processBooleanParam("showMetadata", showMetadata));
  }

  /**
   * Tries to extract and set a boolean value out of the given parameter. Assumes that the default
   * value of the parameter is false.
   *
   * @throws BadRequestException if the value of the parameter is not attributable to a boolean
   *         value.
   */
  private boolean processBooleanParam(String paramName, String paramValue) {
    if (paramValue == null) {
      return false;
    } else if ("true".equalsIgnoreCase(paramValue.replaceAll("\\s", ""))
        || "yes".equalsIgnoreCase(paramValue.replaceAll("\\s", ""))) {
      return true;
    } else if ("false".equalsIgnoreCase(paramValue.replaceAll("\\s", ""))
        || "".equals(paramValue.replaceAll("\\s", ""))
        || "no".equalsIgnoreCase(paramValue.replaceAll("\\s", ""))) {
      return false;
    } else {
      throw new BadRequestException("The given parameter " + paramName + " can only contain the "
          + "values 'true', 'yes', 'false', or 'no'.");
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

  public boolean includeContributionTypes() {
    return includeContributionTypes;
  }

  public boolean isClipGeometry() {
    return clipGeometry;
  }
}
