package org.heigit.ohsome.ohsomeapi.inputprocessing;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.geojson.GeoJsonObject;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.exception.ServiceUnavailableException;
import org.heigit.ohsome.ohsomeapi.geometrybuilders.BBoxBuilder;
import org.heigit.ohsome.ohsomeapi.geometrybuilders.BCircleBuilder;
import org.heigit.ohsome.ohsomeapi.geometrybuilders.BPolygonBuilder;
import org.heigit.ohsome.ohsomeapi.geometrybuilders.BPolygonFromGeoJSON;
import org.heigit.ohsome.ohsomeapi.geometrybuilders.GeometryFrom;
import org.heigit.ohsome.ohsomeapi.geometrybuilders.GeometryOfOSHDBExtent;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.ohsome.ohsomeapi.refactoring.viewsondata.ContributionView;
import org.heigit.ohsome.ohsomeapi.refactoring.viewsondata.SnapshotView;
import org.heigit.ohsome.ohsomeapi.refactoring.viewsondata.ViewOnData;
import org.heigit.ohsome.ohsomeapi.utilities.FilterUtility;
import org.heigit.ohsome.ohsomeapi.utilities.RequestUtils;
import org.heigit.ohsome.ohsomeapi.utilities.SpatialUtility;
import org.heigit.ohsome.ohsomeapi.utilities.TimeUtility;
import org.heigit.ohsome.oshdb.api.db.OSHDBIgnite;
import org.heigit.ohsome.oshdb.api.db.OSHDBIgnite.ComputeMode;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;
import org.heigit.ohsome.oshdb.api.mapreducer.Mappable;
import org.heigit.ohsome.oshdb.api.mapreducer.OSMContributionView;
import org.heigit.ohsome.oshdb.api.mapreducer.OSMEntitySnapshotView;
import org.heigit.ohsome.oshdb.filter.FilterExpression;
import org.heigit.ohsome.oshdb.filter.FilterParser;
import org.heigit.ohsome.oshdb.osm.OSMType;
import org.heigit.ohsome.oshdb.util.geometry.OSHDBGeometryBuilder;
import org.heigit.ohsome.oshdb.util.mappable.OSHDBMapReducible;
import org.heigit.ohsome.oshdb.util.mappable.OSMContribution;
import org.heigit.ohsome.oshdb.util.mappable.OSMEntitySnapshot;
import org.heigit.ohsome.oshdb.util.time.IsoDateTimeParser;
import org.heigit.ohsome.oshdb.util.time.OSHDBTimestamps;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.geom.TopologyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.wololo.jts2geojson.GeoJSONWriter;

/**
 * Holds general input processing and validation methods and validates specific parameters given by
 * the request. Uses geometry methods from {@link
 * org.heigit.ohsome.ohsomeapi.inputprocessing.GeometryBuilder GeometryBuilder} and
 * inputProcessingUtils from {@link
 * org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessingUtils InputProcessingUtils}. Throws
 * exceptions depending on their validity.
 */
@Getter
@Setter
@Component("inputProcessor")
@RequestScope
public class InputProcessor {

  private HttpServletRequest servletRequest;
  //Represents about 1/500 of 180° * 360°.
  public final int COMPUTE_MODE_THRESHOLD = 130;
  private BPolygonFromGeoJSON fromGeoJSONbuilder;
  private ProcessingData processingData;
  private String requestUrl;
  private String requestTimeout;
  private Map<String, String[]> requestParameters;
  private boolean includeTags;
  private boolean includeOSMMetadata;
  private boolean includeContributionTypes;
  private boolean clipGeometry = true;
  private boolean isDensity = false;
  private String[] time;
  private String[] keys;
  private String bboxes;
  private String bcircles;
  private String bpolys;
  private String[] types;
  private String format;
  private String showMetadata;
  private String filter;
  private String filter2;
  private double timeout;
  private String[] values;
  private RequestUtils requestUtils;
  private ExtractMetadata extractMetadata;
  private BBoxBuilder bboxBuilder;
  private GeometryOfOSHDBExtent geometryOfOSHDBExtent;
  private GeometryFrom geometryFrom;
  private ViewOnData viewOnData;
  private Geometry boundary;
  private MapReducer<? extends OSHDBMapReducible> mapRed;
  @Autowired
  private SpatialUtility spatialUtility;
  @Autowired
  private TimeUtility timeUtility;
  @Autowired
  private FilterUtility filterUtility;

  @Autowired
  public InputProcessor(HttpServletRequest servletRequest, RequestUtils requestUtils,
      BBoxBuilder bboxBuilder, BPolygonFromGeoJSON fromGeoJSONbuilder,
      ExtractMetadata extractMetadata, GeometryOfOSHDBExtent geometryOfOSHDBExtent) {
    if (DbConnData.db instanceof OSHDBIgnite) {
      checkClusterAvailability();
    }
    this.servletRequest = servletRequest;
    checkContentTypeHeader();
    checkParameters();
    this.requestUtils = requestUtils;
    this.bboxBuilder = bboxBuilder;
    this.fromGeoJSONbuilder = fromGeoJSONbuilder;
    this.extractMetadata = extractMetadata;
    this.geometryOfOSHDBExtent = geometryOfOSHDBExtent;
    this.requestUrl = requestUtils.extractRequestUrl();
    this.requestTimeout = servletRequest.getParameter("timeout");
    this.requestParameters = servletRequest.getParameterMap();
    if (servletRequest.getRequestURI().contains("density")) {
      this.setDensity(true);
    }
    processingData = new ProcessingData(servletRequest.getRequestURL().toString());
    processQuery();
    setBoundaries();
  }

  /**
   * Checks, if the given content-type header is either 'application/x-www-form-urlencoded' or
   * 'multipart/form-data'.
   *
   * @throws BadRequestException if an unsupported header is given.
   */
  private void checkContentTypeHeader() {
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
  private void checkParameters() {
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

  private void processQuery() {
    bboxes = createEmptyStringIfNull(servletRequest.getParameter("bboxes"));
    bcircles = createEmptyStringIfNull(servletRequest.getParameter("bcircles"));
    bpolys = createEmptyStringIfNull(servletRequest.getParameter("bpolys"));
    types =
        splitParamOnComma(createEmptyArrayIfNull(servletRequest.getParameterValues("types")));
    keys =
        splitParamOnComma(createEmptyArrayIfNull(servletRequest.getParameterValues("keys")));
    values = splitParamOnComma(
        createEmptyArrayIfNull(servletRequest.getParameterValues("values")));
    time =
        splitParamOnComma(createEmptyArrayIfNull(servletRequest.getParameterValues("time")));
    format = createEmptyStringIfNull(servletRequest.getParameter("format"));
    showMetadata =
        createEmptyStringIfNull(servletRequest.getParameter("showMetadata"));
    filter = createEmptyStringIfNull(servletRequest.getParameter("filter"));
    filter2 = createEmptyStringIfNull(servletRequest.getParameter("filter2"));
    timeout = defineRequestTimeout();
    processShowMetadata(showMetadata);
    checkFormat(servletRequest.getParameter("format"));
    formatIsGEOJSON();
    setQueryTimeout();
  }

  /**
   * Processes the given showMetadata parameter and sets the respective value in the processingData
   * object.
   */
  private void processShowMetadata(String showMetadata) {
    processingData.setShowMetadata(processBooleanParam("showMetadata", showMetadata));
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

  private void formatIsGEOJSON() {
    if ("geojson".equalsIgnoreCase(servletRequest.getParameter("format"))) {
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
  }

  private void setQueryTimeout() {
    DbConnData.db.timeout(timeout);
  }

  private void setBoundaries() {
    processingData.setBoundaryType(setBoundaryType(bboxes, bcircles, bpolys));
    try {
      switch (processingData.getBoundaryType()) {
        case NOBOUNDARY:
          if (extractMetadata.getDataPoly() == null) {
            throw new BadRequestException(ExceptionMessages.NO_BOUNDARY);
          }
          boundary = extractMetadata.getDataPoly();
          break;
        case BBOXES:
          processingData.setBoundaryValues(spatialUtility.splitBboxes(bboxes).toArray(new String[] {}));
          boundary = bboxBuilder.create(processingData.getBoundaryValues());
          this.getProcessingData().setBoundaryList(bboxBuilder.getGeometryList());
          this.getProcessingData().setRequestGeom(bboxBuilder.getUnifiedBbox());
          geometryFrom = bboxBuilder;
          break;
        case BCIRCLES:
          processingData.setBoundaryValues(spatialUtility.splitBcircles(bcircles).toArray(new String[] {}));
          BCircleBuilder bcircleBuilder = new BCircleBuilder();
          boundary = bcircleBuilder.create(processingData.getBoundaryValues());
          this.getProcessingData().setBoundaryList(bcircleBuilder.getGeometryList());
          this.getProcessingData().setRequestGeom(bcircleBuilder.getGeom());
          geometryFrom = bcircleBuilder;
          break;
        case BPOLYS:
          if (bpolys.matches("^\\s*\\{[\\s\\S]*")) {
            boundary = fromGeoJSONbuilder.create(bpolys);
            this.getProcessingData().setGeoJsonGeoms(fromGeoJSONbuilder.getGeoJsonGeoms());
            this.getProcessingData().setBoundaryList(fromGeoJSONbuilder.getGeometryList());
            this.getProcessingData().setRequestGeom(fromGeoJSONbuilder.getGeometry());
            spatialUtility.setBoundaryIds(fromGeoJSONbuilder.getBoundaryIds());
            geometryFrom = fromGeoJSONbuilder;
          } else {
            processingData.setBoundaryValues(spatialUtility.splitBpolys(bpolys).toArray(new String[] {}));
            BPolygonBuilder bpolyBuilder = new BPolygonBuilder();
            boundary = bpolyBuilder.create(processingData.getBoundaryValues());
            this.getProcessingData().setBoundaryList(bpolyBuilder.getGeometryList());
            this.getProcessingData().setRequestGeom(bpolyBuilder.getGeometry());
            geometryFrom = bpolyBuilder;
          }
          break;
        default:
          throw new BadRequestException(ExceptionMessages.BOUNDARY_PARAM_FORMAT_OR_COUNT);
      }
    } catch (ClassCastException e) {
      throw new BadRequestException(ExceptionMessages.BOUNDARY_PARAM_FORMAT);
    }
  }

  /**
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor
   *         #getMapReducer(ComputeMode) getMapReducer}
   */
  public <T extends OSHDBMapReducible> MapReducer<T> getMapReducer() throws Exception {
    return this.getMapReducer(null);
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
  public <T extends OSHDBMapReducible> MapReducer<T> getMapReducer(ComputeMode forceComputeMode)
      throws Exception {
    setComputeMode(forceComputeMode);
    setUpMapReducer();
    defineTypes();
    // the OSM type will be set in the ratio implementation within the ElementsRequestExecutor.java
    setOSMType();
    if (processingData.isContainingSimpleFeatureTypes()
        // skip in ratio or groupByBoundary requests -> needs to be done later in the processing
        && !processingData.isRatio() && !processingData.isGroupByBoundary()
        && !processingData.isFullHistory()) {
      mapRed = filterOnSimpleFeatures(mapRed);
    }
    extractTime();
    filterMapReducer(filter);
    return (MapReducer<T>) mapRed;
  }

  private void setComputeMode(ComputeMode forceComputeMode) {
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
  }

  private void setUpMapReducer() {
    if (viewOnData instanceof SnapshotView) {
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
      try {
        mapRed = mapRed.areaOfInterest((Geometry & Polygonal) boundary);
      } catch (TopologyException e) {
        throw new BadRequestException(ExceptionMessages.BPOLYS_PARAM_GEOMETRY + e.getMessage());
      }
    }
  }

  /**
   * Defines the type(s) out of the given String[].
   *
   * @param types <code>String</code> array containing one, two, or all 3 OSM types (node, way,
   *        relation), or simple feature types (point, line, polygon, other). If the array is empty,
   *        all three OSM types are used.
   */
  public void defineTypes() {
    types = createEmptyArrayIfNull(types);
    checkTypes(types);
    processingData.setOsmTypes(EnumSet.noneOf(OSMType.class));
    if (types.length == 0 || types.length == 1 && types[0].isEmpty()) {
      processingData.setOsmTypes(EnumSet.of(OSMType.NODE, OSMType.WAY, OSMType.RELATION));
    } else {
      if (!processingData.isContainingSimpleFeatureTypes()) {
        for (String type : types) {
          if ("node".equalsIgnoreCase(type)) {
            processingData.getOsmTypes().add(OSMType.NODE);
          } else if ("way".equalsIgnoreCase(type)) {
            processingData.getOsmTypes().add(OSMType.WAY);
          } else {
            processingData.getOsmTypes().add(OSMType.RELATION);
          }
        }
      } else {
        processingData.setSimpleFeatureTypes(defineSimpleFeatureTypes(types));
        if (!processingData.isRatio() && (processingData.getOsmTypes().contains(OSMType.WAY)
            || processingData.getOsmTypes().contains(OSMType.RELATION))) {
          mapRed = spatialUtility.filterOnPlanarRelations(mapRed);
        }
      }
    }
  }

  private void setOSMType() {
    if (!processingData.isRatio()) {
      mapRed = mapRed.osmType(processingData.getOsmTypes());
    }
  }

  /**
   * Applies respective Puntal|Lineal|Polygonal filter(s) on features of the given MapReducer.
   *
   * @return MapReducer with filtered geometries
   * @throws RuntimeException if {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor
   *         #filterOnSimpleFeatures(Mappable) filterOnSimpleFeatures} was called on mapped entries
   */
  // suppressed, as filter always returns the same mappable type T
  @SuppressWarnings("unchecked")
  public <T extends Mappable<? extends OSHDBMapReducible>> T filterOnSimpleFeatures(T mapRed) {
    Set<SimpleFeatureType> simpleFeatureTypes = processingData.getSimpleFeatureTypes();
    return (T) mapRed.filter(data -> {
      if (data instanceof OSMEntitySnapshot) {
        Geometry snapshotGeom;
        if (clipGeometry) {
          snapshotGeom = ((OSMEntitySnapshot) data).getGeometry();
        } else {
          snapshotGeom = ((OSMEntitySnapshot) data).getGeometryUnclipped();
        }
        return spatialUtility.checkGeometryOnSimpleFeatures(snapshotGeom, simpleFeatureTypes);
      } else if (data instanceof OSMContribution) {
        Geometry contribGeomBefore;
        Geometry contribGeomAfter;
        if (clipGeometry) {
          contribGeomBefore = ((OSMContribution) data).getGeometryBefore();
          contribGeomAfter = ((OSMContribution) data).getGeometryAfter();
        } else {
          contribGeomBefore = ((OSMContribution) data).getGeometryUnclippedBefore();
          contribGeomAfter = ((OSMContribution) data).getGeometryUnclippedAfter();
        }
        return contribGeomBefore != null
            && spatialUtility.checkGeometryOnSimpleFeatures(contribGeomBefore, simpleFeatureTypes)
            || contribGeomAfter != null
            && spatialUtility.checkGeometryOnSimpleFeatures(contribGeomAfter, simpleFeatureTypes);
      } else {
        assert false : "filterOnSimpleFeatures() called on mapped entries";
        throw new RuntimeException("filterOnSimpleFeatures() called on mapped entries");
      }
    });
  }

  /**
   * Extracts the information from the given time array and fills the toTimestamps[] with content
   * (in case of isSnapshot=false).
   */
  private void extractTime() {
    String[] toTimestamps = null;
    String[] timeData;
    if (time.length == 0 || time[0].replaceAll("\\s", "").length() == 0 && time.length == 1) {
      if (viewOnData instanceof ContributionView) {
        toTimestamps = new String[] {extractMetadata.getFromTstamp(), extractMetadata.getToTstamp()};
        mapRed = mapRed.timestamps(extractMetadata.getFromTstamp(), extractMetadata.getToTstamp());
      } else {
        mapRed = mapRed.timestamps(extractMetadata.getToTstamp());
      }
    } else if (time.length == 1) {
      timeData = timeUtility.extractIsoTime(time[0]);
      if (viewOnData instanceof ContributionView) {
        toTimestamps = timeUtility.defineToTimestamps(timeData);
      }
      if (timeData[2] != null) {
        // interval is given
        mapRed = mapRed.timestamps(new OSHDBTimestamps(timeData[0], timeData[1], timeData[2]));
      } else if (timeData[1] != null) {
        mapRed = mapRed.timestamps(timeData[0], timeData[1]);
      } else {
        if (viewOnData instanceof ContributionView) {
          throw new BadRequestException(ExceptionMessages.TIME_FORMAT_CONTRIBUTION);
        }
        mapRed = mapRed.timestamps(timeData[0]);
      }
    } else {
      timeUtility.checkTimestampsOnIsoConformity(time);
      for (String timestamp : time) {
        ZonedDateTime zdt = IsoDateTimeParser.parseIsoDateTime(timestamp);
        timeUtility.checkTemporalExtend(zdt.format(DateTimeFormatter.ISO_DATE_TIME));
      }
      timeData = timeUtility.sortTimestamps(time);
      if (viewOnData instanceof ContributionView) {
        toTimestamps = timeUtility.defineToTimestamps(timeData);
      }
      String firstElem = timeData[0];
      timeData = ArrayUtils.remove(timeData, 0);
      mapRed = mapRed.timestamps(firstElem, firstElem, timeData);
    }
    timeUtility.setToTimestamps(toTimestamps);
  }

  public void filterMapReducer(String filter) {
    if (!"".equals(filter)) {
      if (keys.length != 0 || values.length != 0 || types.length != 0) {
        throw new BadRequestException(ExceptionMessages.FILTER_PARAM);
      } else {
        // call translator and add filters to mapRed
        FilterParser fp = new FilterParser(DbConnData.tagTranslator);
        FilterExpression filterExpr = filterUtility.parseFilter(fp, filter);
        processingData.setFilterExpression(filterExpr);
        if (!(processingData.isRatio()
            || processingData.isGroupByBoundary()
            || processingData.isFullHistory())) {
          // skip in ratio or groupByBoundary requests -> needs to be done later in the processing
          mapRed = mapRed.filter(filterExpr);
        }
      }
    } else {
      mapRed = extractKeysValues(mapRed, keys, values);
    }
  }

  /** Defines the simple feature types and corresponding OSM types out of the given String array. */
  public EnumSet<SimpleFeatureType> defineSimpleFeatureTypes(String[] types) {
    EnumSet<SimpleFeatureType> simpleFeatures = EnumSet.noneOf(SimpleFeatureType.class);
    for (String type : types) {
      if ("point".equalsIgnoreCase(type)) {
        simpleFeatures.add(SimpleFeatureType.POINT);
        processingData.getOsmTypes().add(OSMType.NODE);
      } else if ("line".equalsIgnoreCase(type)) {
        simpleFeatures.add(SimpleFeatureType.LINE);
        processingData.getOsmTypes().add(OSMType.WAY);
      } else if ("polygon".equalsIgnoreCase(type)) {
        simpleFeatures.add(SimpleFeatureType.POLYGON);
        processingData.getOsmTypes().add(OSMType.WAY);
        processingData.getOsmTypes().add(OSMType.RELATION);
      } else if ("other".equalsIgnoreCase(type)) {
        simpleFeatures.add(SimpleFeatureType.OTHER);
        processingData.getOsmTypes().add(OSMType.RELATION);
      }
    }
    return simpleFeatures;
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
  public static String createEmptyStringIfNull(String toCheck) {
    if (toCheck == null) {
      toCheck = "";
    }
    return toCheck;
  }

  /**
   * Checks the given keys and values String[] on their length.
   *
   * @throws BadRequestException if values_n doesn't fit to keys_n. There cannot be more input
   *         values in the values|values2 than in the keys|keys2 parameter.
   */
  public void checkKeysValues(String[] keys, String[] values) {
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
    return Arrays.equals(keys, keys2) && Arrays.equals(values, values2);
  }

  /** Used in /ratio requests. */
  public Pair<String[], String[]> processKeys2Vals2(String[] keys2, String[] values2) {
    keys2 = createEmptyArrayIfNull(keys2);
    values2 = createEmptyArrayIfNull(values2);
    return new ImmutablePair<>(keys2, values2);
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
      @Deprecated
      boolean oldUnclippedParameter = "unclipped".equalsIgnoreCase(property);
      if ("tags".equalsIgnoreCase(property)) {
        this.includeTags = true;
      } else if ("metadata".equalsIgnoreCase(property)) {
        this.includeOSMMetadata = true;
      } else if (oldUnclippedParameter) {
        this.clipGeometry = false;
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
  public String getRequestUrlIfGetRequest() {
    if (!"post".equalsIgnoreCase(servletRequest.getMethod())) {
      return this.getRequestUrl();
    }
    return null;
  }

  /**
   * Checks the given keys and values parameters on their length and includes them in the
   * {@link org.heigit.ohsome.oshdb.api.mapreducer.MapReducer#osmTag(String) osmTag(key)},
   * or {@link org.heigit.ohsome.oshdb.api.mapreducer.MapReducer#osmTag(String, String)
   * osmTag(key, value)} method.
   *
   * @param mapRed current {@link org.heigit.ohsome.oshdb.api.mapreducer.MapReducer MapReducer}
   * @return {@link org.heigit.ohsome.oshdb.api.mapreducer.MapReducer MapReducer} object
   *         including the filters derived from the given parameters.
   */
  private MapReducer<? extends OSHDBMapReducible> extractKeysValues(
      MapReducer<? extends OSHDBMapReducible> mapRed, String[] keys, String[] values) {
    checkKeysValues(keys, values);
    if (keys.length != values.length) {
      String[] tempVal = new String[keys.length];
      System.arraycopy(values, 0, tempVal, 0, values.length);
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
   * Checks the given type(s) String[] on its length and content.
   *
   * @throws BadRequestException if the given types parameter is invalid.
   */
  private void checkTypes(String[] types) {
    if (types.length > 4) {
      throw new BadRequestException(
          "Parameter 'types' (and 'types2') cannot have more than 4 entries.");
    } else if (types.length == 0 || types.length == 1 && types[0].isEmpty()) {
      // do nothing
    } else {
      processingData.setContainingSimpleFeatureTypes(!"node".equalsIgnoreCase(types[0])
          && !"way".equalsIgnoreCase(types[0]) && !"relation".equalsIgnoreCase(types[0]));
      for (String type : types) {
        if (spatialUtility.isSimpleFeatureType(type)) {
          if (!processingData.isContainingSimpleFeatureTypes()) {
            throw new BadRequestException(ExceptionMessages.TYPES_PARAM);
          }
        } else {
          if (processingData.isContainingSimpleFeatureTypes()) {
            throw new BadRequestException(ExceptionMessages.TYPES_PARAM);
          }
        }
      }
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
      geom = geometryOfOSHDBExtent.getGeometry();
    } else {
      geom = processingData.getRequestGeom();
    }
    return geom;
  }
}
