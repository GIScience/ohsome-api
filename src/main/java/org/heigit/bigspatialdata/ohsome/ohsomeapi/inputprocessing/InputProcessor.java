package org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.geojson.GeoJsonObject;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.ServiceUnavailableException;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.RequestParameters;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.RequestResource;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.utils.RequestUtils;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBIgnite;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBIgnite.ComputeMode;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.Mappable;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.OSMContributionView;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.OSMEntitySnapshotView;
import org.heigit.bigspatialdata.oshdb.api.object.OSHDBMapReducible;
import org.heigit.bigspatialdata.oshdb.api.object.OSMContribution;
import org.heigit.bigspatialdata.oshdb.api.object.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.osm.OSMEntity;
import org.heigit.bigspatialdata.oshdb.osm.OSMType;
import org.heigit.bigspatialdata.oshdb.util.geometry.OSHDBGeometryBuilder;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.OSMTag;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.OSMTagKey;
import org.heigit.bigspatialdata.oshdb.util.time.ISODateTimeParser;
import org.heigit.bigspatialdata.oshdb.util.time.OSHDBTimestamps;
import org.heigit.ohsome.filter.AndOperator;
import org.heigit.ohsome.filter.Filter;
import org.heigit.ohsome.filter.FilterExpression;
import org.heigit.ohsome.filter.FilterParser;
import org.heigit.ohsome.filter.GeometryTypeFilter;
import org.heigit.ohsome.filter.TagFilterEquals;
import org.heigit.ohsome.filter.TagFilterEqualsAny;
import org.heigit.ohsome.filter.TypeFilter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygonal;
import org.wololo.jts2geojson.GeoJSONWriter;

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
  private boolean isSnapshot;
  private boolean isDensity;
  private String requestUrl;
  private String requestMethod;
  private String requestTimeout;
  private Map<String, String[]> requestParameters;
  private boolean includeTags;
  private boolean includeOSMMetadata;
  private boolean unclipped;
  private final String[] genericParameters = {"bboxes", "bcircles", "bpolys", "types", "keys",
      "values", "timeout", "time", "format", "showMetadata", "filter"};

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
            servletRequest.getParameter("bpolys"), servletRequest.getParameterValues("types"),
            servletRequest.getParameterValues("keys"), servletRequest.getParameterValues("values"),
            servletRequest.getParameterValues("time"), servletRequest.getParameter("format"),
            servletRequest.getParameter("showMetadata"), ProcessingData.getTimeout(),
            servletRequest.getParameter("filter")), servletRequest.getRequestURL().toString());
    this.requestUrl = RequestUtils.extractRequestUrl(servletRequest);
    this.requestMethod = servletRequest.getMethod();
    this.requestTimeout = servletRequest.getParameter("timeout");
    this.requestParameters = servletRequest.getParameterMap();
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
    String filter = createEmptyStringIfNull(processingData.getRequestParameters().getFilter());
    // overwriting RequestParameters object with splitted/non-null parameters
    processingData
        .setRequestParameters(new RequestParameters(requestMethod, isSnapshot, isDensity, bboxes,
            bcircles, bpolys, types, keys, values, time, format, showMetadata, timeout, filter));
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
    mapRed = defineTypes(types, mapRed);
    // the OSM type will be set in the ratio implementation within the ElementsRequestExecutor.java
    if (!processingData.isRatio()) {
      mapRed = mapRed.osmType((EnumSet<OSMType>) processingData.getOsmTypes());
    }
    if (processingData.containsSimpleFeatureTypes()
        // skip in ratio or groupByBoundary requests -> needs to be done later in the processing
        && !processingData.isRatio() && !processingData.isGroupByBoundary()
        && !processingData.isFullHistory()) {
      mapRed = filterOnSimpleFeatures(mapRed);
    }
    mapRed = extractTime(mapRed, time, isSnapshot);
    if (!"".equals(filter)) {
      if (keys.length != 0 || values.length != 0 || types.length != 0) {
        throw new BadRequestException(ExceptionMessages.FILTER_PARAM);
      } else {
        // call translator and add filters to mapRed
        FilterParser fp = new FilterParser(DbConnData.tagTranslator);
        FilterExpression filterExpr = utils.parseFilter(fp, filter);
        processingData.setFilterExpression(filterExpr);
        mapRed = optimizeFilters0(mapRed, filterExpr);
        mapRed = optimizeFilters1(mapRed, filterExpr);
        mapRed = mapRed.osmEntityFilter(filterExpr::applyOSM);
        // execute this only if the filter has a geometry type subfilter
        if (ProcessingData.filterContainsGeometryTypeCheck(filterExpr)
            // skip in ratio or groupByBoundary requests -> needs to be done later in the processing
            && !processingData.isRatio() && !processingData.isGroupByBoundary()
            && !processingData.isFullHistory()) {
          processingData.setContainsSimpleFeatureTypes(true);
          mapRed = filterOnGeometryType(mapRed, filterExpr);
        }
      }
    } else {
      mapRed = extractKeysValues(mapRed, keys, values);
    }
    return (MapReducer<T>) mapRed;
  }

  private MapReducer<? extends OSHDBMapReducible> optimizeFilters0(
      MapReducer<? extends OSHDBMapReducible> mapRed, FilterExpression filter) {
    // performs basic optimizations “low hanging fruit”:
    // single filters, and-combination of single filters, etc.
    if (filter instanceof TagFilterEquals) {
      OSMTag tag = DbConnData.tagTranslator.getOSMTagOf(((TagFilterEquals) filter).getTag());
      return mapRed.osmTag(tag);
    }
    if (filter instanceof TagFilterEqualsAny) {
      OSMTagKey key =
          DbConnData.tagTranslator.getOSMTagKeyOf(((TagFilterEqualsAny) filter).getTag());
      return mapRed.osmTag(key);
    }
    if (filter instanceof TypeFilter) {
      return mapRed.osmType(((TypeFilter) filter).getType());
    }
    if (filter instanceof AndOperator) {
      return optimizeFilters0(optimizeFilters0(mapRed, ((AndOperator) filter).getLeftOperand()),
          ((AndOperator) filter).getRightOperand());
    }
    return mapRed;
  }

  private MapReducer<? extends OSHDBMapReducible> optimizeFilters1(
      MapReducer<? extends OSHDBMapReducible> mapRed, FilterExpression filter) {
    // performs more advanced optimizations that rely on analyzing the DNF of a filter expression
    // 1. convert to disjunctive normal form
    List<List<Filter>> filterNormalized = filter.normalize();
    // 2. collect all OSMTypes in all of the clauses
    EnumSet<OSMType> allTypes = EnumSet.noneOf(OSMType.class);
    for (List<Filter> andSubFilter : filterNormalized) {
      EnumSet<OSMType> subTypes = EnumSet.of(OSMType.NODE, OSMType.WAY, OSMType.RELATION);
      for (Filter subFilter : andSubFilter) {
        if (subFilter instanceof TypeFilter) {
          subTypes.retainAll(EnumSet.of(((TypeFilter) subFilter).getType()));
        } else if (subFilter instanceof GeometryTypeFilter) {
          subTypes.retainAll(((GeometryTypeFilter) subFilter).getOSMTypes());
        }
      }
      allTypes.addAll(subTypes);
    }
    mapRed = mapRed.osmType(allTypes);
    processingData.setOsmTypes(allTypes);
    // 3. (todo) intelligently group queried tags
    /*
     * here, we could optimize a few situations further: when a specific tag or key is used in all
     * branches of the filter: run mapRed.osmTag the set of tags which are present in any branches:
     * run mapRed.osmTag(list) (note that for this all branches need to have at least one
     * TagFilterEquals or TagFilterEqualsAny) related: https://github.com/GIScience/oshdb/pull/210
     */
    return mapRed;
  }

  /**
   * Defines the type(s) out of the given String[].
   * 
   * @param types <code>String</code> array containing one, two, or all 3 OSM types (node, way,
   *        relation), or simple feature types (point, line, polygon, other). If the array is empty,
   *        all three OSM types are used.
   * @throws BadRequestException if the content of the parameter does not represent one or more OSM
   *         types, OR one or more of point/line/polygon
   */
  public <T extends OSHDBMapReducible> MapReducer<T> defineTypes(String[] types,
      MapReducer<T> mapRed) throws BadRequestException {
    types = createEmptyArrayIfNull(types);
    checkTypes(types);
    processingData.setOsmTypes(EnumSet.noneOf(OSMType.class));
    if (types.length == 0 || types.length == 1 && types[0].isEmpty()) {
      processingData.setOsmTypes(EnumSet.of(OSMType.NODE, OSMType.WAY, OSMType.RELATION));
    } else {
      if (!processingData.containsSimpleFeatureTypes()) {
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
          mapRed = utils.filterOnPlanarRelations(mapRed);
        }
      }
    }
    return mapRed;
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
    return Arrays.equals(keys, keys2) && Arrays.equals(values, values2);
  }

  /** Used in /ratio requests. */
  public Pair<String[], String[]> processKeys2Vals2(String[] keys2, String[] values2,
      RequestParameters requestParams) {
    keys2 = createEmptyArrayIfNull(keys2);
    values2 = createEmptyArrayIfNull(values2);
    return new ImmutablePair<>(keys2, values2);
  }

  /**
   * Processes the properties parameter used in data-extraction ressources and sets the respective
   * boolean values includeTags, includeOSMMetadata and unclippedGeometries.
   * 
   * @throws BadRequestException if the properties parameter contains invalid content
   */
  public void processPropertiesParam() throws BadRequestException {
    String[] properties =
        splitParamOnComma(createEmptyArrayIfNull(requestParameters.get("properties")));
    if (properties.length > 3) {
      throw new BadRequestException(ExceptionMessages.PROPERTIES_PARAM);
    }
    for (String property : properties) {
      if ("tags".equalsIgnoreCase(property)) {
        this.includeTags = true;
      } else if ("metadata".equalsIgnoreCase(property)) {
        this.includeOSMMetadata = true;
      } else if ("unclipped".equalsIgnoreCase(property)) {
        this.unclipped = true;
      } else {
        throw new BadRequestException(ExceptionMessages.PROPERTIES_PARAM);
      }
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
   * Applies respective Puntal|Lineal|Polygonal filter(s) on features of the given MapReducer.
   *
   * @return MapReducer with filtered geometries
   */
  // suppressed, as filter always returns the same mappable type T
  @SuppressWarnings("unchecked")
  public <T extends Mappable<? extends OSHDBMapReducible>> T filterOnSimpleFeatures(T mapRed) {
    Set<SimpleFeatureType> simpleFeatureTypes = processingData.getSimpleFeatureTypes();
    return (T) mapRed.filter(data -> {
      if (data instanceof OSMEntitySnapshot) {
        Geometry snapshotGeom;
        if (unclipped) {
          snapshotGeom = ((OSMEntitySnapshot) data).getGeometryUnclipped();
        } else {
          snapshotGeom = ((OSMEntitySnapshot) data).getGeometry();
        }
        return utils.checkGeometryOnSimpleFeatures(snapshotGeom, simpleFeatureTypes);
      } else if (data instanceof OSMContribution) {
        Geometry contribGeomBefore;
        Geometry contribGeomAfter;
        if (unclipped) {
          contribGeomBefore = ((OSMContribution) data).getGeometryUnclippedBefore();
          contribGeomAfter = ((OSMContribution) data).getGeometryUnclippedAfter();
        } else {
          contribGeomBefore = ((OSMContribution) data).getGeometryBefore();
          contribGeomAfter = ((OSMContribution) data).getGeometryAfter();
        }
        return contribGeomBefore != null
            && utils.checkGeometryOnSimpleFeatures(contribGeomBefore, simpleFeatureTypes)
            || contribGeomAfter != null
                && utils.checkGeometryOnSimpleFeatures(contribGeomAfter, simpleFeatureTypes);
      } else {
        assert false : "filterOnSimpleFeatures() called on mapped entries";
        throw new RuntimeException("filterOnSimpleFeatures() called on mapped entries");
      }
    });
  }

  /**
   * Applies respective Puntal|Lineal|Polygonal filter(s) from a given filter expression on features
   * of the given MapReducer.
   *
   * @param mapRed the mapreducer to filter
   * @param filterExpr the filter expression to apply
   * @return MapReducer with filtered geometries
   */
  // suppressed, as filter always returns the same mappable type T
  @SuppressWarnings("unchecked")
  public <T extends Mappable<? extends OSHDBMapReducible>> T filterOnGeometryType(T mapRed,
      FilterExpression filterExpr) {
    return (T) mapRed.filter(data -> {
      if (data instanceof OSMEntitySnapshot) {
        OSMEntity snapshotEntity = ((OSMEntitySnapshot) data).getEntity();
        Geometry snapshotGeom;
        if (unclipped) {
          snapshotGeom = ((OSMEntitySnapshot) data).getGeometryUnclipped();
        } else {
          snapshotGeom = ((OSMEntitySnapshot) data).getGeometry();
        }
        return filterExpr.applyOSMGeometry(snapshotEntity, snapshotGeom);
      } else if (data instanceof OSMContribution) {
        OSMEntity entityBefore = ((OSMContribution) data).getEntityBefore();
        OSMEntity entityAfter = ((OSMContribution) data).getEntityAfter();
        Geometry contribGeomBefore;
        Geometry contribGeomAfter;
        if (unclipped) {
          contribGeomBefore = ((OSMContribution) data).getGeometryUnclippedBefore();
          contribGeomAfter = ((OSMContribution) data).getGeometryUnclippedAfter();
        } else {
          contribGeomBefore = ((OSMContribution) data).getGeometryBefore();
          contribGeomAfter = ((OSMContribution) data).getGeometryAfter();
        }
        return contribGeomBefore != null
            && filterExpr.applyOSMGeometry(entityBefore, contribGeomBefore)
            || contribGeomAfter != null
                && filterExpr.applyOSMGeometry(entityAfter, contribGeomAfter);
      } else {
        assert false : "geometry filter called on mapped entries";
        throw new RuntimeException("geometry filter called on mapped entries");
      }
    });
  }

  /**
   * Checks the given keys and values parameters on their length and includes them in the
   * {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer#osmTag(String) osmTag(key)},
   * or {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer#osmTag(String, String)
   * osmTag(key, value)} method.
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

  /** Checks the given type(s) String[] on its length and content. */
  private void checkTypes(String[] types) throws BadRequestException {
    if (types.length > 4) {
      throw new BadRequestException(
          "Parameter 'types' (and 'types2') cannot have more than 4 entries.");
    } else if (types.length == 0 || types.length == 1 && types[0].isEmpty()) {
      // do nothing
    } else {
      processingData.setContainsSimpleFeatureTypes(!"node".equalsIgnoreCase(types[0])
          && !"way".equalsIgnoreCase(types[0]) && !"relation".equalsIgnoreCase(types[0]));
      for (String type : types) {
        if (utils.isSimpleFeatureType(type)) {
          if (!processingData.containsSimpleFeatureTypes()) {
            throw new BadRequestException(ExceptionMessages.TYPES_PARAM);
          }
        } else {
          if (processingData.containsSimpleFeatureTypes()) {
            throw new BadRequestException(ExceptionMessages.TYPES_PARAM);
          }
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
   * Checks, if the cluster has less active server nodes, than defined on startup. Throws a 503
   * Service Unavailable exception, in case one or more nodes are inactive.
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
   * 'multipart/form-data'. Throws a 400 - BadRequestException if an unsupported header is given.
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
   * Checks, if the request parameters are valid. Throws a 400 - BadRequestException and suggests
   * possible parameters based on fuzzy matching scores if the request parameters are not valid.
   */
  private void checkParameters(HttpServletRequest servletRequest) throws BadRequestException {
    String[] possibleParameters;
    for (Map.Entry<String, String[]> entry : servletRequest.getParameterMap().entrySet()) {
      String parameterName = entry.getKey();
      if (isGenericParameter(parameterName) == false) {
        RequestResource resource = checkResource(servletRequest);
        String[] specificParameters = getResourceSpecificParameters(servletRequest);
        if (resource.equals(RequestResource.AREA)) {
          throw new BadRequestException(
              StringSimilarity.findSimilarParameters(parameterName, genericParameters));
        } else {
          possibleParameters = genericParameters;
          List<String> arrList = new ArrayList<String>(Arrays.asList(possibleParameters));
          for (int i = 0; i < specificParameters.length; i++) {
            arrList.add(specificParameters[i]);
          }
          if (!arrList.contains(parameterName)) {
            possibleParameters = arrList.toArray(new String[arrList.size()]);
            throw new BadRequestException(
                StringSimilarity.findSimilarParameters(parameterName, possibleParameters));
          }
        }
      }
    }
  }

  /** Checks, if the request parameter is a generic parameter. */
  private boolean isGenericParameter(String parameterName) {
    if (Arrays.asList(genericParameters).contains(parameterName)) {
      return true;
    }
    return false;
  }

  /**
   * Checks the kind of resource. The request resources LENGTH, PERIMETER, and COUNT are considered
   * as AREA.
   */
  private RequestResource checkResource(HttpServletRequest servletRequest) {
    String uri = servletRequest.getRequestURI();
    if (uri.contains("/groupBy/tag")) {
      return RequestResource.GROUPBYTAG;
    } else if (uri.contains("/groupBy/key")) {
      return RequestResource.GROUPBYKEY;
    } else if (uri.contains("/ratio")) {
      return RequestResource.RATIO;
    } else if (uri.contains("/bbox") || uri.contains("/centroid") || uri.contains("/geometry")) {
      return RequestResource.DATAEXTRACTION;
    } else {
      return RequestResource.AREA;
    }
  }

  /**
   * Checks the resource and returns a list of the corresponding specific parameters. Returns an
   * empty array if it is a generic resource (e.g. /elements/count).
   */
  private String[] getResourceSpecificParameters(HttpServletRequest servletRequest) {
    String uri = servletRequest.getRequestURI();
    if (uri.contains("/groupBy/tag")) {
      return new String[] {"groupByKey", "groupByValues"};
    } else if (uri.contains("/groupBy/key")) {
      return new String[] {"groupByKeys"};
    } else if (uri.contains("/ratio")) {
      if (null != servletRequest.getParameter("filter")) {
        return new String[] {"filter2"};
      }
      return new String[] {"keys2", "types2", "values2"};
    } else if (uri.contains("/bbox") || uri.contains("/centroid") || uri.contains("/geometry")) {
      return new String[] {"properties"};
    }
    return new String[] {};
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

  public boolean isUnclipped() {
    return unclipped;
  }
}
