package org.heigit.ohsome.ohsomeapi.executor;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBIgnite;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBIgnite.ComputeMode;
import org.heigit.bigspatialdata.oshdb.api.generic.OSHDBCombinedIndex;
import org.heigit.bigspatialdata.oshdb.api.generic.function.SerializableFunction;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.object.OSMContribution;
import org.heigit.bigspatialdata.oshdb.api.object.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.osm.OSMEntity;
import org.heigit.bigspatialdata.oshdb.osm.OSMType;
import org.heigit.bigspatialdata.oshdb.util.OSHDBTag;
import org.heigit.bigspatialdata.oshdb.util.OSHDBTimestamp;
import org.heigit.bigspatialdata.oshdb.util.celliterator.ContributionType;
import org.heigit.bigspatialdata.oshdb.util.geometry.Geo;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;
import org.heigit.bigspatialdata.oshdb.util.time.ISODateTimeParser;
import org.heigit.bigspatialdata.oshdb.util.time.TimestampFormatter;
import org.heigit.ohsome.filter.FilterExpression;
import org.heigit.ohsome.filter.FilterParser;
import org.heigit.ohsome.ohsomeapi.Application;
import org.heigit.ohsome.ohsomeapi.controller.rawdata.ElementsGeometry;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils.MatchType;
import org.heigit.ohsome.ohsomeapi.inputprocessing.BoundaryType;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessingUtils;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.ohsome.ohsomeapi.inputprocessing.SimpleFeatureType;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.ohsome.ohsomeapi.output.Description;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Attribution;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Metadata;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.elements.ElementsResult;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.GroupByResponse;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.GroupByResult;
import org.heigit.ohsome.ohsomeapi.output.rawdataresponse.DataResponse;
import org.heigit.ohsome.ohsomeapi.utils.GroupByBoundaryGeoJsonGenerator;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygonal;
import org.wololo.geojson.Feature;

/** Includes all execute methods for requests mapped to /elements. */
public class ElementsRequestExecutor {

  public static final String URL = ExtractMetadata.attributionUrl;
  public static final String TEXT = ExtractMetadata.attributionShort;
  public static final DecimalFormat df = ExecutionUtils.defineDecimalFormat("#.##");

  private ElementsRequestExecutor() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Performs an OSM data extraction.
   * 
   * @param elemGeom {@link org.heigit.ohsome.ohsomeapi.controller.rawdata.ElementsGeometry
   *        ElementsGeometry} defining the geometry of the OSM elements
   * @param servletRequest {@link javax.servlet.http.HttpServletRequest HttpServletRequest} incoming
   *        request object
   * @param servletResponse {@link javax.servlet.http.HttpServletResponse HttpServletResponse]}
   *        outgoing response object
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters},
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer#stream() stream}, or
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils#streamElementsResponse(HttpServletResponse, DataResponse, boolean, Stream, Stream)
   *         streamElementsResponse}
   */
  public static void extract(ElementsGeometry elemGeom, HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    InputProcessor inputProcessor = new InputProcessor(servletRequest, true, false);
    MapReducer<OSMEntitySnapshot> mapRed = null;
    inputProcessor.processPropertiesParam();
    inputProcessor.processIsUnclippedParam();
    final boolean includeTags = inputProcessor.includeTags();
    final boolean includeOSMMetadata = inputProcessor.includeOSMMetadata();
    final boolean clipGeometries = inputProcessor.isClipGeometry();
    if (DbConnData.db instanceof OSHDBIgnite) {
      // on ignite: Use AffinityCall backend, which is the only one properly supporting streaming
      // of result data, without buffering the whole result in memory before returning the result.
      // This allows to write data out to the client via a chunked HTTP response.
      mapRed = inputProcessor.processParameters(ComputeMode.AffinityCall);
    } else {
      mapRed = inputProcessor.processParameters();
    }
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    TagTranslator tt = DbConnData.tagTranslator;
    String[] keys = requestParameters.getKeys();
    int[] keysInt = new int[keys.length];
    if (keys.length != 0) {
      for (int i = 0; i < keys.length; i++) {
        keysInt[i] = tt.getOSHDBTagKeyOf(keys[i]).toInt();
      }
    }
    final MapReducer<Feature> preResult;
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    preResult = mapRed.map(snapshot -> {
      Map<String, Object> properties = new TreeMap<>();
      if (includeOSMMetadata) {
        properties.put("@lastEdit",
            TimestampFormatter.getInstance().isoDateTime(snapshot.getEntity().getTimestamp()));
      }
      properties.put("@snapshotTimestamp",
          TimestampFormatter.getInstance().isoDateTime(snapshot.getTimestamp()));
      Geometry geom = snapshot.getGeometry();
      if (!clipGeometries) {
        geom = snapshot.getGeometryUnclipped();
      }
      return exeUtils.createOSMFeature(snapshot.getEntity(), geom, properties, keysInt, includeTags,
          includeOSMMetadata, elemGeom);
    }).filter(Objects::nonNull);
    Metadata metadata = null;
    if (processingData.isShowMetadata()) {
      metadata = new Metadata(null, "OSM data as GeoJSON features.",
          inputProcessor.getRequestUrlIfGetRequest(servletRequest));
    }
    DataResponse osmData = new DataResponse(new Attribution(URL, TEXT), Application.API_VERSION,
        metadata, "FeatureCollection", Collections.emptyList());
    try (Stream<Feature> streamResult = preResult.stream()) {
      exeUtils.streamElementsResponse(servletResponse, osmData, false, streamResult);
    }
  }

  /**
   * Performs an OSM data extraction using the full-history of the data.
   * 
   * @param elemGeom {@link org.heigit.ohsome.ohsomeapi.controller.rawdata.ElementsGeometry
   *        ElementsGeometry} defining the geometry of the OSM elements
   * @param servletRequest {@link javax.servlet.http.HttpServletRequest HttpServletRequest} incoming
   *        request object
   * @param servletResponse {@link javax.servlet.http.HttpServletResponse HttpServletResponse]}
   *        outgoing response object
   * @throws BadRequestException if the given time parameter is invalid
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters},
   *         {@link org.heigit.bigspatialdata.oshdb.util.time.ISODateTimeParser#parseISODateTime(String)
   *         parseISODateTime},
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer#stream() stream}, or
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils#streamElementsResponse(HttpServletResponse, DataResponse, boolean, Stream, Stream)
   *         streamElementsResponse}
   */
  public static void extractFullHistory(ElementsGeometry elemGeom,
      HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws Exception {
    InputProcessor inputProcessor = new InputProcessor(servletRequest, false, false);
    inputProcessor.getProcessingData().setIsFullHistory(true);
    InputProcessor snapshotInputProcessor = new InputProcessor(servletRequest, true, false);
    snapshotInputProcessor.getProcessingData().setIsFullHistory(true);
    MapReducer<OSMEntitySnapshot> mapRedSnapshot = null;
    MapReducer<OSMContribution> mapRedContribution = null;
    if (DbConnData.db instanceof OSHDBIgnite) {
      // on ignite: Use AffinityCall backend, which is the only one properly supporting streaming
      // of result data, without buffering the whole result in memory before returning the result.
      // This allows to write data out to the client via a chunked HTTP response.
      mapRedSnapshot = snapshotInputProcessor.processParameters(ComputeMode.AffinityCall);
      mapRedContribution = inputProcessor.processParameters(ComputeMode.AffinityCall);
    } else {
      mapRedSnapshot = snapshotInputProcessor.processParameters();
      mapRedContribution = inputProcessor.processParameters();
    }
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    String[] time = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("time")));
    if (time.length != 2) {
      throw new BadRequestException(ExceptionMessages.TIME_FORMAT_FULL_HISTORY);
    }
    TagTranslator tt = DbConnData.tagTranslator;
    String[] keys = requestParameters.getKeys();
    int[] keysInt = new int[keys.length];
    if (keys.length != 0) {
      for (int i = 0; i < keys.length; i++) {
        keysInt[i] = tt.getOSHDBTagKeyOf(keys[i]).toInt();
      }
    }
    MapReducer<Feature> contributionPreResult = null;
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    inputProcessor.processPropertiesParam();
    inputProcessor.processIsUnclippedParam();
    InputProcessingUtils utils = inputProcessor.getUtils();
    final boolean includeTags = inputProcessor.includeTags();
    final boolean includeOSMMetadata = inputProcessor.includeOSMMetadata();
    final boolean clipGeometries = inputProcessor.isClipGeometry();
    final Set<SimpleFeatureType> simpleFeatureTypes = processingData.getSimpleFeatureTypes();
    Optional<FilterExpression> filter = processingData.getFilterExpression();
    final boolean requiresGeometryTypeCheck =
        filter.isPresent() && ProcessingData.filterContainsGeometryTypeCheck(filter.get());
    FilterExpression filterExpression = processingData.getFilterExpression().orElse(null);
    String startTimestamp = ISODateTimeParser.parseISODateTime(requestParameters.getTime()[0])
        .format(DateTimeFormatter.ISO_DATE_TIME);
    String endTimestamp = ISODateTimeParser.parseISODateTime(requestParameters.getTime()[1])
        .format(DateTimeFormatter.ISO_DATE_TIME);
    MapReducer<List<OSMContribution>> mapRedContributions = mapRedContribution.groupByEntity();
    contributionPreResult = mapRedContributions.flatMap(contributions -> {
      List<Feature> output = new LinkedList<>();
      Map<String, Object> properties;
      Geometry currentGeom = null;
      OSMEntity currentEntity = null;
      String validFrom = null;
      String validTo;
      boolean skipNext = false;
      // first contribution:
      if (contributions.get(0).is(ContributionType.CREATION)) {
        // if creation: skip next output
        skipNext = true;
      } else {
        // if not "creation": take "before" as starting "row" (geom, tags), valid_from = t_start
        currentEntity = contributions.get(0).getEntityBefore();
        currentGeom = exeUtils.getGeometry(contributions.get(0), clipGeometries, true);
        validFrom = startTimestamp;
      }
      // then for each contribution:
      for (OSMContribution contribution : contributions) {
        // set valid_to of previous row, add to output list (output.add(…))
        validTo = TimestampFormatter.getInstance().isoDateTime(contribution.getTimestamp());
        if (!skipNext) {
          properties = new TreeMap<>();
          // deactivating the adding of the contrib type as it could deliver false results
          // properties = exeUtils.addContribType(contribution, properties, includeOSMMetadata);
          properties.put("@validFrom", validFrom);
          properties.put("@validTo", validTo);
          if (!currentGeom.isEmpty()) {
            boolean addToOutput;
            if (processingData.containsSimpleFeatureTypes()) {
              addToOutput = utils.checkGeometryOnSimpleFeatures(currentGeom, simpleFeatureTypes);
            } else if (requiresGeometryTypeCheck) {
              addToOutput = filterExpression.applyOSMGeometry(currentEntity, currentGeom);
            } else {
              addToOutput = true;
            }
            if (addToOutput) {
              output.add(exeUtils.createOSMFeature(currentEntity, currentGeom, properties, keysInt,
                  includeTags, includeOSMMetadata, elemGeom));
            }
          }
        }
        skipNext = false;
        if (contribution.is(ContributionType.DELETION)) {
          // if deletion: skip output of next row
          skipNext = true;
        } else {
          // else: take "after" as next row
          currentEntity = contribution.getEntityAfter();
          currentGeom = exeUtils.getGeometry(contribution, clipGeometries, false);
          validFrom = TimestampFormatter.getInstance().isoDateTime(contribution.getTimestamp());
        }
      }
      // after loop:
      OSMContribution lastContribution = contributions.get(contributions.size() - 1);
      if (!lastContribution.is(ContributionType.DELETION)) {
        // if last contribution was not "deletion": set valid_to = t_end, add row to output list
        validTo = endTimestamp;
        properties = new TreeMap<>();
        // deactivating the adding of the contrib type as it could deliver false results
        // properties = exeUtils.addContribType(lastContribution, properties, includeOSMMetadata);
        properties.put("@validFrom", validFrom);
        properties.put("@validTo", validTo);
        if (!currentGeom.isEmpty()) {
          boolean addToOutput;
          if (processingData.containsSimpleFeatureTypes()) {
            addToOutput = utils.checkGeometryOnSimpleFeatures(currentGeom, simpleFeatureTypes);
          } else if (requiresGeometryTypeCheck) {
            addToOutput = filterExpression.applyOSMGeometry(currentEntity, currentGeom);
          } else {
            addToOutput = true;
          }
          if (addToOutput) {
            output.add(exeUtils.createOSMFeature(currentEntity, currentGeom, properties, keysInt,
                includeTags, includeOSMMetadata, elemGeom));
          }
        }
      }
      return output;
    }).filter(Objects::nonNull);
    MapReducer<Feature> snapshotPreResult = null;
    // handles cases where valid_from = t_start, valid_to = t_end
    snapshotPreResult = mapRedSnapshot.groupByEntity().filter(snapshots -> snapshots.size() == 2)
        .filter(snapshots -> snapshots.get(0).getGeometry() == snapshots.get(1).getGeometry()
            && snapshots.get(0).getEntity().getVersion() == snapshots.get(1).getEntity()
                .getVersion())
        .map(snapshots -> snapshots.get(0)).flatMap(snapshot -> {
          Map<String, Object> properties = new TreeMap<>();
          OSMEntity entity = snapshot.getEntity();
          if (includeOSMMetadata) {
            properties.put("@lastEdit", entity.getTimestamp().toString());
          }
          Geometry geom = snapshot.getGeometry();
          if (!clipGeometries) {
            geom = snapshot.getGeometryUnclipped();
          }
          properties.put("@snapshotTimestamp",
              TimestampFormatter.getInstance().isoDateTime(snapshot.getTimestamp()));
          properties.put("@validFrom", startTimestamp);
          properties.put("@validTo", endTimestamp);
          boolean addToOutput;
          if (processingData.containsSimpleFeatureTypes()) {
            addToOutput = utils.checkGeometryOnSimpleFeatures(geom, simpleFeatureTypes);
          } else if (requiresGeometryTypeCheck) {
            addToOutput = filterExpression.applyOSMGeometry(entity, geom);
          } else {
            addToOutput = true;
          }
          if (addToOutput) {
            return Collections.singletonList(exeUtils.createOSMFeature(entity, geom, properties,
                keysInt, includeTags, includeOSMMetadata, elemGeom));
          } else {
            return Collections.emptyList();
          }
        }).filter(Objects::nonNull);
    Metadata metadata = null;
    if (processingData.isShowMetadata()) {
      metadata = new Metadata(null, "Full-history OSM data as GeoJSON features.",
          inputProcessor.getRequestUrlIfGetRequest(servletRequest));
    }
    DataResponse osmData = new DataResponse(new Attribution(URL, TEXT), Application.API_VERSION,
        metadata, "FeatureCollection", Collections.emptyList());
    try (Stream<Feature> contributionStream = contributionPreResult.stream();
        Stream<Feature> snapshotStream = snapshotPreResult.stream()) {
      exeUtils.streamElementsResponse(servletResponse, osmData, true,
          Stream.concat(contributionStream, snapshotStream));
    }
  }

  /**
   * Performs a count|length|perimeter|area calculation grouped by the boundary and the tag.
   * 
   * @param requestResource {@link org.heigit.ohsome.ohsomeapi.executor.RequestResource
   *        RequestResource} definition of the request resource
   * @param servletRequest {@link javax.servlet.http.HttpServletRequest HttpServletRequest} incoming
   *        request object
   * @param servletResponse {@link javax.servlet.http.HttpServletResponse HttpServletResponse]}
   *        outgoing response object
   * @param isSnapshot whether this request uses the snapshot-view (true), or contribution-view
   *        (false)
   * @param isDensity whether this request is accessed via the /density resource
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response}
   * @throws BadRequestException if groupByKey parameter is not given
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters}
   */
  public static <P extends Geometry & Polygonal> Response aggregateGroupByBoundaryGroupByTag(
      RequestResource requestResource, HttpServletRequest servletRequest,
      HttpServletResponse servletResponse, boolean isSnapshot, boolean isDensity) throws Exception {
    final long startTime = System.currentTimeMillis();
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor(servletRequest, isSnapshot, isDensity);
    inputProcessor.getProcessingData().setIsGroupByBoundary(true);
    String[] groupByKey = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByKey")));
    if (groupByKey.length != 1) {
      throw new BadRequestException(ExceptionMessages.GROUP_BY_KEY_PARAM);
    }
    mapRed = inputProcessor.processParameters();
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    String[] groupByValues = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByValues")));
    TagTranslator tt = DbConnData.tagTranslator;
    Integer[] valuesInt = new Integer[groupByValues.length];
    ArrayList<Pair<Integer, Integer>> zeroFill = new ArrayList<>();
    int keysInt = tt.getOSHDBTagKeyOf(groupByKey[0]).toInt();
    if (groupByValues.length != 0) {
      for (int j = 0; j < groupByValues.length; j++) {
        valuesInt[j] = tt.getOSHDBTagOf(groupByKey[0], groupByValues[j]).getValue();
        zeroFill.add(new ImmutablePair<>(keysInt, valuesInt[j]));
      }
    }
    var arrGeoms = new ArrayList<>(processingData.getBoundaryList());
    @SuppressWarnings("unchecked") // intentionally as check for P on Polygonal is already performed
    Map<Integer, P> geoms = IntStream.range(0, arrGeoms.size()).boxed()
        .collect(Collectors.toMap(idx -> idx, idx -> (P) arrGeoms.get(idx)));
    MapAggregator<Integer, OSMEntitySnapshot> mapAgg = mapRed.aggregateByGeometry(geoms);
    if (processingData.containsSimpleFeatureTypes()) {
      mapAgg = inputProcessor.filterOnSimpleFeatures(mapAgg);
    }
    Optional<FilterExpression> filter = processingData.getFilterExpression();
    if (filter.isPresent() && ProcessingData.filterContainsGeometryTypeCheck(filter.get())) {
      mapAgg = inputProcessor.filterOnGeometryType(mapAgg, filter.get());
    }
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    var preResult = mapAgg.map(f -> exeUtils.mapSnapshotToTags(keysInt, valuesInt, f))
        .aggregateBy(Pair::getKey, zeroFill).map(Pair::getValue)
        .aggregateByTimestamp(OSMEntitySnapshot::getTimestamp).map(OSMEntitySnapshot::getGeometry);
    var result = exeUtils.computeNestedResult(requestResource, preResult);
    var groupByResult = OSHDBCombinedIndex.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.entrySet().size()];
    InputProcessingUtils utils = inputProcessor.getUtils();
    Object[] boundaryIds = utils.getBoundaryIds();
    int count = 0;
    ArrayList<Geometry> boundaries = new ArrayList<>(processingData.getBoundaryList());
    for (var entry : groupByResult.entrySet()) {
      int boundaryIdentifier = entry.getKey().getFirstIndex();
      ElementsResult[] results = exeUtils.fillElementsResult(entry.getValue(),
          requestParameters.isDensity(), df, boundaries.get(boundaryIdentifier));
      int tagValue = entry.getKey().getSecondIndex().getValue();
      String tagIdentifier;
      // check for non-remainder objects (which do have the defined key and value)
      if (entry.getKey().getSecondIndex().getKey() != -1 && tagValue != -1) {
        tagIdentifier = tt.getOSMTagOf(keysInt, tagValue).toString();
      } else {
        tagIdentifier = "remainder";
      }
      resultSet[count] =
          new GroupByResult(new Object[] {boundaryIds[boundaryIdentifier], tagIdentifier}, results);
      count++;
    }
    // used to remove null objects from the resultSet
    resultSet = Arrays.stream(resultSet).filter(Objects::nonNull).toArray(GroupByResult[]::new);
    Metadata metadata = null;
    if (processingData.isShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration,
          Description.aggregateGroupByBoundaryGroupByTag(requestParameters.isDensity(),
              requestResource.getLabel(), requestResource.getUnit()),
          inputProcessor.getRequestUrlIfGetRequest(servletRequest));
    }
    if ("csv".equalsIgnoreCase(requestParameters.getFormat())) {
      exeUtils.writeCsvResponse(resultSet, servletResponse,
          exeUtils.createCsvTopComments(URL, TEXT, Application.API_VERSION, metadata));
      return null;
    } else if ("geojson".equalsIgnoreCase(requestParameters.getFormat())) {
      return GroupByResponse.of(new Attribution(URL, TEXT), Application.API_VERSION, metadata,
          "FeatureCollection", GroupByBoundaryGeoJsonGenerator.createGeoJsonFeatures(resultSet,
              processingData.getGeoJsonGeoms()));
    }
    return new GroupByResponse(new Attribution(URL, TEXT), Application.API_VERSION, metadata,
        resultSet);
  }

  /**
   * Performs a count|length|perimeter|area calculation grouped by the tag.
   * 
   * @param requestResource {@link org.heigit.ohsome.ohsomeapi.executor.RequestResource
   *        RequestResource} definition of the request resource
   * @param servletRequest {@link javax.servlet.http.HttpServletRequest HttpServletRequest} incoming
   *        request object
   * @param servletResponse {@link javax.servlet.http.HttpServletResponse HttpServletResponse]}
   *        outgoing response object
   * @param isSnapshot whether this request uses the snapshot-view (true), or contribution-view
   *        (false)
   * @param isDensity whether this request is accessed via the /density resource
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response}
   * @throws BadRequestException if groupByKey parameter is not given
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters} and
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils#computeResult(RequestResource, MapAggregator)
   *         computeResult}
   */
  public static Response aggregateGroupByTag(RequestResource requestResource,
      HttpServletRequest servletRequest, HttpServletResponse servletResponse, boolean isSnapshot,
      boolean isDensity) throws Exception {
    final long startTime = System.currentTimeMillis();
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor(servletRequest, isSnapshot, isDensity);
    String[] groupByKey = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByKey")));
    if (groupByKey.length != 1) {
      throw new BadRequestException(ExceptionMessages.GROUP_BY_KEY_PARAM);
    }
    mapRed = inputProcessor.processParameters();
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    String[] groupByValues = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByValues")));
    TagTranslator tt = DbConnData.tagTranslator;
    Integer[] valuesInt = new Integer[groupByValues.length];
    ArrayList<Pair<Integer, Integer>> zeroFill = new ArrayList<>();
    int keysInt = tt.getOSHDBTagKeyOf(groupByKey[0]).toInt();
    if (groupByValues.length != 0) {
      for (int j = 0; j < groupByValues.length; j++) {
        valuesInt[j] = tt.getOSHDBTagOf(groupByKey[0], groupByValues[j]).getValue();
        zeroFill.add(new ImmutablePair<>(keysInt, valuesInt[j]));
      }
    }
    var preResult = mapRed.map(f -> exeUtils.mapSnapshotToTags(keysInt, valuesInt, f))
        .aggregateByTimestamp().aggregateBy(Pair::getKey, zeroFill).map(Pair::getValue);
    var result = exeUtils.computeResult(requestResource, preResult);
    var groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    Geometry geom = inputProcessor.getGeometry();
    int count = 0;
    for (var entry : groupByResult.entrySet()) {
      ElementsResult[] results =
          exeUtils.fillElementsResult(entry.getValue(), requestParameters.isDensity(), df, geom);
      // check for non-remainder objects (which do have the defined key and value)
      if (entry.getKey().getKey() != -1 && entry.getKey().getValue() != -1) {
        groupByName = tt.getOSMTagOf(keysInt, entry.getKey().getValue()).toString();
      } else {
        groupByName = "remainder";
      }
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    // used to remove null objects from the resultSet
    resultSet = Arrays.stream(resultSet).filter(Objects::nonNull).toArray(GroupByResult[]::new);
    Metadata metadata = null;
    if (processingData.isShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(
          duration, Description.aggregateGroupByTag(requestParameters.isDensity(),
              requestResource.getLabel(), requestResource.getUnit()),
          inputProcessor.getRequestUrlIfGetRequest(servletRequest));
    }
    if ("csv".equalsIgnoreCase(requestParameters.getFormat())) {
      exeUtils.writeCsvResponse(resultSet, servletResponse,
          exeUtils.createCsvTopComments(URL, TEXT, Application.API_VERSION, metadata));
      return null;
    }
    return new GroupByResponse(new Attribution(URL, TEXT), Application.API_VERSION, metadata,
        resultSet);
  }

  /**
   * Performs a count|length|perimeter|area calculation grouped by the OSM type.
   * 
   * @param requestResource {@link org.heigit.ohsome.ohsomeapi.executor.RequestResource
   *        RequestResource} definition of the request resource
   * @param servletRequest {@link javax.servlet.http.HttpServletRequest HttpServletRequest} incoming
   *        request object
   * @param servletResponse {@link javax.servlet.http.HttpServletResponse HttpServletResponse]}
   *        outgoing response object
   * @param isSnapshot whether this request uses the snapshot-view (true), or contribution-view
   *        (false)
   * @param isDensity whether this request is accessed via the /density resource
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response}
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters} and
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils#computeResult(RequestResource, MapAggregator)
   *         computeResult}
   */
  public static Response aggregateGroupByType(RequestResource requestResource,
      HttpServletRequest servletRequest, HttpServletResponse servletResponse, boolean isSnapshot,
      boolean isDensity) throws Exception {
    final long startTime = System.currentTimeMillis();
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor(servletRequest, isSnapshot, isDensity);
    mapRed = inputProcessor.processParameters();
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, OSMType>, OSMEntitySnapshot> preResult;
    preResult = mapRed.aggregateByTimestamp().aggregateBy(
        (SerializableFunction<OSMEntitySnapshot, OSMType>) f -> f.getEntity().getType(),
        processingData.getOsmTypes());
    var result = exeUtils.computeResult(requestResource, preResult);
    var groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    Geometry geom = inputProcessor.getGeometry();
    int count = 0;
    for (var entry : groupByResult.entrySet()) {
      ElementsResult[] results =
          exeUtils.fillElementsResult(entry.getValue(), requestParameters.isDensity(), df, geom);
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    Metadata metadata = null;
    if (processingData.isShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration,
          Description.countPerimeterAreaGroupByType(requestParameters.isDensity(),
              requestResource.getLabel(), requestResource.getUnit()),
          inputProcessor.getRequestUrlIfGetRequest(servletRequest));
    }
    if ("csv".equalsIgnoreCase(requestParameters.getFormat())) {
      exeUtils.writeCsvResponse(resultSet, servletResponse,
          exeUtils.createCsvTopComments(URL, TEXT, Application.API_VERSION, metadata));
      return null;
    }
    return new GroupByResponse(new Attribution(URL, TEXT), Application.API_VERSION, metadata,
        resultSet);
  }

  /**
   * Performs a count|length|perimeter|area calculation grouped by the key.
   * 
   * @param requestResource {@link org.heigit.ohsome.ohsomeapi.executor.RequestResource
   *        RequestResource} definition of the request resource
   * @param servletRequest {@link javax.servlet.http.HttpServletRequest HttpServletRequest} incoming
   *        request object
   * @param servletResponse {@link javax.servlet.http.HttpServletResponse HttpServletResponse]}
   *        outgoing response object
   * @param isSnapshot whether this request uses the snapshot-view (true), or contribution-view
   *        (false)
   * @param isDensity whether this request is accessed via the /density resource
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response}
   * @throws BadRequestException if groupByKeys parameter is not given
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters} and
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils#computeResult(RequestResource, MapAggregator)
   *         computeResult}
   */
  public static Response aggregateGroupByKey(RequestResource requestResource,
      HttpServletRequest servletRequest, HttpServletResponse servletResponse, boolean isSnapshot,
      boolean isDensity) throws Exception {
    final long startTime = System.currentTimeMillis();
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor(servletRequest, isSnapshot, isDensity);
    String[] groupByKeys = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByKeys")));
    if (groupByKeys == null || groupByKeys.length == 0) {
      throw new BadRequestException(ExceptionMessages.GROUP_BY_KEYS_PARAM);
    }
    mapRed = inputProcessor.processParameters();
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    TagTranslator tt = DbConnData.tagTranslator;
    Integer[] keysInt = new Integer[groupByKeys.length];
    for (int i = 0; i < groupByKeys.length; i++) {
      keysInt[i] = tt.getOSHDBTagKeyOf(groupByKeys[i]).toInt();
    }
    MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, OSMEntitySnapshot> preResult =
        mapRed.flatMap(f -> {
          List<Pair<Integer, OSMEntitySnapshot>> res = new LinkedList<>();
          Iterable<OSHDBTag> tags = f.getEntity().getTags();
          for (OSHDBTag tag : tags) {
            int tagKeyId = tag.getKey();
            for (int key : keysInt) {
              if (tagKeyId == key) {
                res.add(new ImmutablePair<>(tagKeyId, f));
              }
            }
          }
          if (res.isEmpty()) {
            res.add(new ImmutablePair<>(-1, f));
          }
          return res;
        }).aggregateByTimestamp().aggregateBy(Pair::getKey, Arrays.asList(keysInt))
            .map(Pair::getValue);
    var result = exeUtils.computeResult(requestResource, preResult);
    var groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    int count = 0;
    for (var entry : groupByResult.entrySet()) {
      ElementsResult[] results =
          exeUtils.fillElementsResult(entry.getValue(), requestParameters.isDensity(), df, null);
      // check for non-remainder objects (which do have the defined key)
      if (entry.getKey() != -1) {
        groupByName = tt.getOSMTagKeyOf(entry.getKey().intValue()).toString();
      } else {
        groupByName = "remainder";
      }
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    Metadata metadata = null;
    if (processingData.isShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration,
          Description.aggregateGroupByKey(requestResource.getLabel(), requestResource.getUnit()),
          inputProcessor.getRequestUrlIfGetRequest(servletRequest));
    }
    if ("csv".equalsIgnoreCase(requestParameters.getFormat())) {
      exeUtils.writeCsvResponse(resultSet, servletResponse,
          exeUtils.createCsvTopComments(URL, TEXT, Application.API_VERSION, metadata));
      return null;
    }
    return new GroupByResponse(new Attribution(URL, TEXT), Application.API_VERSION, metadata,
        resultSet);
  }

  /**
   * Performs a count|length|perimeter|area|ratio calculation.
   * 
   * @deprecated Will be removed in next major version update.
   * 
   * @param requestResource {@link org.heigit.ohsome.ohsomeapi.executor.RequestResource
   *        RequestResource} definition of the request resource
   * @param servletRequest {@link javax.servlet.http.HttpServletRequest HttpServletRequest} incoming
   *        request object
   * @param servletResponse {@link javax.servlet.http.HttpServletResponse HttpServletResponse]}
   *        outgoing response object
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response}
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters} and
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils#computeResult(RequestResource, MapAggregator)
   *         computeResult}
   */
  @Deprecated(forRemoval = true)
  public static Response aggregateBasicFiltersRatio(RequestResource requestResource,
      HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws Exception {
    final long startTime = System.currentTimeMillis();
    // these 2 parameters always have these values for /ratio requests
    final boolean isSnapshot = true;
    final boolean isDensity = false;
    InputProcessor inputProcessor = new InputProcessor(servletRequest, isSnapshot, isDensity);
    inputProcessor.getProcessingData().setIsRatio(true);
    final MapReducer<OSMEntitySnapshot> intermediateMapRed = inputProcessor.processParameters();
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    TagTranslator tt = DbConnData.tagTranslator;
    String[] keys2 = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("keys2")));
    String[] values2 = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("values2")));
    inputProcessor.checkKeysValues(keys2, values2);
    Pair<String[], String[]> keys2Vals2 = inputProcessor.processKeys2Vals2(keys2, values2);
    keys2 = keys2Vals2.getKey();
    values2 = keys2Vals2.getValue();
    Integer[] keysInt1 = new Integer[requestParameters.getKeys().length];
    Integer[] valuesInt1 = new Integer[requestParameters.getValues().length];
    Integer[] keysInt2 = new Integer[keys2.length];
    Integer[] valuesInt2 = new Integer[values2.length];
    for (int i = 0; i < requestParameters.getKeys().length; i++) {
      keysInt1[i] = tt.getOSHDBTagKeyOf(requestParameters.getKeys()[i]).toInt();
      if (requestParameters.getValues() != null && i < requestParameters.getValues().length) {
        valuesInt1[i] =
            tt.getOSHDBTagOf(requestParameters.getKeys()[i], requestParameters.getValues()[i])
                .getValue();
      }
    }
    for (int i = 0; i < keys2.length; i++) {
      keysInt2[i] = tt.getOSHDBTagKeyOf(keys2[i]).toInt();
      if (i < values2.length) {
        valuesInt2[i] = tt.getOSHDBTagOf(keys2[i], values2[i]).getValue();
      }
    }
    EnumSet<OSMType> osmTypes1 =
        (EnumSet<OSMType>) inputProcessor.getProcessingData().getOsmTypes();
    String[] types1 = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("types")));
    String[] types2 = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("types2")));
    final EnumSet<SimpleFeatureType> simpleFeatureTypes1 =
        inputProcessor.defineSimpleFeatureTypes(types1);
    inputProcessor.defineTypes(types2, intermediateMapRed);
    EnumSet<OSMType> osmTypes2 =
        (EnumSet<OSMType>) inputProcessor.getProcessingData().getOsmTypes();
    final EnumSet<SimpleFeatureType> simpleFeatureTypes2 =
        inputProcessor.defineSimpleFeatureTypes(types2);
    EnumSet<OSMType> osmTypes = osmTypes1.clone();
    osmTypes.addAll(osmTypes2);
    String[] osmTypesString =
        osmTypes.stream().map(OSMType::toString).map(String::toLowerCase).toArray(String[]::new);
    ExecutionUtils tempExecutionUtils = new ExecutionUtils(processingData);
    MapReducer<OSMEntitySnapshot> mapRed = null;
    if (!inputProcessor.compareKeysValues(requestParameters.getKeys(), keys2,
        requestParameters.getValues(), values2)) {
      RequestParameters requestParams =
          new RequestParameters(servletRequest.getMethod(), isSnapshot, isDensity,
              servletRequest.getParameter("bboxes"), servletRequest.getParameter("bcircles"),
              servletRequest.getParameter("bpolys"), osmTypesString, new String[] {},
              new String[] {}, servletRequest.getParameterValues("time"),
              servletRequest.getParameter("format"), servletRequest.getParameter("showMetadata"),
              ProcessingData.getTimeout(), servletRequest.getParameter("filter"));
      ProcessingData secondProcessingData =
          new ProcessingData(requestParams, servletRequest.getRequestURL().toString());
      InputProcessor secondInputProcessor =
          new InputProcessor(servletRequest, isSnapshot, isDensity);
      secondInputProcessor.setProcessingData(secondProcessingData);
      mapRed = secondInputProcessor.processParameters();
    } else {
      mapRed = inputProcessor.processParameters();
    }
    mapRed = mapRed.osmType(osmTypes);
    mapRed = tempExecutionUtils.snapshotFilter(mapRed, osmTypes1, osmTypes2, simpleFeatureTypes1,
        simpleFeatureTypes2, keysInt1, keysInt2, valuesInt1, valuesInt2);
    var preResult = mapRed.aggregateByTimestamp().aggregateBy(snapshot -> {
      boolean matches1 = tempExecutionUtils.snapshotMatches(snapshot, osmTypes1,
          simpleFeatureTypes1, keysInt1, valuesInt1);
      boolean matches2 = tempExecutionUtils.snapshotMatches(snapshot, osmTypes2,
          simpleFeatureTypes2, keysInt2, valuesInt2);
      if (matches1 && matches2) {
        return MatchType.MATCHESBOTH;
      } else if (matches1) {
        return MatchType.MATCHES1;
      } else if (matches2) {
        return MatchType.MATCHES2;
      } else {
        // this should never be reached
        assert false : "MatchType matches none.";
        return MatchType.MATCHESNONE;
      }
    }, EnumSet.allOf(MatchType.class));
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    var result = exeUtils.computeResult(requestResource, preResult);
    int resultSize = result.size();
    Double[] value1 = new Double[resultSize / 4];
    Double[] value2 = new Double[resultSize / 4];
    String[] timeArray = new String[resultSize / 4];
    int value1Count = 0;
    int value2Count = 0;
    int matchesBothCount = 0;
    // time and value extraction
    for (var entry : result.entrySet()) {
      if (entry.getKey().getSecondIndex() == MatchType.MATCHES2) {
        timeArray[value2Count] =
            TimestampFormatter.getInstance().isoDateTime(entry.getKey().getFirstIndex());
        value2[value2Count] = Double.parseDouble(df.format(entry.getValue().doubleValue()));
        value2Count++;
      }
      if (entry.getKey().getSecondIndex() == MatchType.MATCHES1) {
        value1[value1Count] = Double.parseDouble(df.format(entry.getValue().doubleValue()));
        value1Count++;
      }
      if (entry.getKey().getSecondIndex() == MatchType.MATCHESBOTH) {
        value1[matchesBothCount] = value1[matchesBothCount]
            + Double.parseDouble(df.format(entry.getValue().doubleValue()));
        value2[matchesBothCount] = value2[matchesBothCount]
            + Double.parseDouble(df.format(entry.getValue().doubleValue()));
        matchesBothCount++;
      }
    }
    return exeUtils.createRatioResponse(timeArray, value1, value2, startTime, requestResource,
        inputProcessor.getRequestUrlIfGetRequest(servletRequest), servletResponse);
  }

  /**
   * Performs a count|length|perimeter|area|ratio calculation using the filter and filter2
   * parameters.
   * 
   * @param requestResource {@link org.heigit.ohsome.ohsomeapi.executor.RequestResource
   *        RequestResource} definition of the request resource
   * @param servletRequest {@link javax.servlet.http.HttpServletRequest HttpServletRequest} incoming
   *        request object
   * @param servletResponse {@link javax.servlet.http.HttpServletResponse HttpServletResponse]}
   *        outgoing response object
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response}
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters} and
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils#computeResult(RequestResource, MapAggregator)
   *         computeResult}
   */
  public static Response aggregateRatio(RequestResource requestResource,
      HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws Exception {
    if (null == servletRequest.getParameter("filter")
        && (null != servletRequest.getParameter("types")
            || null != servletRequest.getParameter("keys"))) {
      return aggregateBasicFiltersRatio(requestResource, servletRequest, servletResponse);
    }
    final long startTime = System.currentTimeMillis();
    // these 2 parameters always have these values for /ratio requests
    final boolean isSnapshot = true;
    final boolean isDensity = false;
    InputProcessor inputProcessor = new InputProcessor(servletRequest, isSnapshot, isDensity);
    inputProcessor.getProcessingData().setIsRatio(true);
    inputProcessor.processParameters();
    ProcessingData processingData = inputProcessor.getProcessingData();
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    String filter1 = inputProcessor.getProcessingData().getRequestParameters().getFilter();
    String filter2 = inputProcessor.createEmptyStringIfNull(servletRequest.getParameter("filter2"));
    inputProcessor.checkFilter(filter2);
    String combinedFilter = exeUtils.combineFiltersWithOr(filter1, filter2);
    FilterParser fp = new FilterParser(DbConnData.tagTranslator);
    FilterExpression filterExpr1 = inputProcessor.getUtils().parseFilter(fp, filter1);
    FilterExpression filterExpr2 = inputProcessor.getUtils().parseFilter(fp, filter2);
    RequestParameters requestParamsCombined = new RequestParameters(servletRequest.getMethod(),
        isSnapshot, isDensity, servletRequest.getParameter("bboxes"),
        servletRequest.getParameter("bcircles"), servletRequest.getParameter("bpolys"),
        new String[] {}, new String[] {}, new String[] {},
        servletRequest.getParameterValues("time"), servletRequest.getParameter("format"),
        servletRequest.getParameter("showMetadata"), ProcessingData.getTimeout(), combinedFilter);
    ProcessingData processingDataCombined =
        new ProcessingData(requestParamsCombined, servletRequest.getRequestURL().toString());
    InputProcessor inputProcessorCombined =
        new InputProcessor(servletRequest, isSnapshot, isDensity);
    inputProcessorCombined.setProcessingData(processingDataCombined);
    MapReducer<OSMEntitySnapshot> mapRed = inputProcessorCombined.processParameters();

    mapRed = exeUtils.newSnapshotFilter(mapRed, filterExpr1, filterExpr2);
    var preResult = mapRed.aggregateByTimestamp().aggregateBy(snapshot -> {
      OSMEntity entity = snapshot.getEntity();
      boolean matches1 = filterExpr1.applyOSMGeometry(entity, snapshot.getGeometry());
      boolean matches2 = filterExpr2.applyOSMGeometry(entity, snapshot.getGeometry());
      if (matches1 && matches2) {
        return MatchType.MATCHESBOTH;
      } else if (matches1) {
        return MatchType.MATCHES1;
      } else if (matches2) {
        return MatchType.MATCHES2;
      } else {
        // this should never be reached
        assert false : "MatchType matches none.";
        return MatchType.MATCHESNONE;
      }
    }, EnumSet.allOf(MatchType.class));
    var result = exeUtils.computeResult(requestResource, preResult);
    int resultSize = result.size();
    int matchTypeSize = 4;
    Double[] value1 = new Double[resultSize / matchTypeSize];
    Double[] value2 = new Double[resultSize / matchTypeSize];
    String[] timeArray = new String[resultSize / matchTypeSize];
    int value1Count = 0;
    int value2Count = 0;
    int matchesBothCount = 0;
    // time and value extraction
    for (var entry : result.entrySet()) {
      if (entry.getKey().getSecondIndex() == MatchType.MATCHES2) {
        timeArray[value2Count] =
            TimestampFormatter.getInstance().isoDateTime(entry.getKey().getFirstIndex());
        value2[value2Count] = Double.parseDouble(df.format(entry.getValue().doubleValue()));
        value2Count++;
      }
      if (entry.getKey().getSecondIndex() == MatchType.MATCHES1) {
        value1[value1Count] = Double.parseDouble(df.format(entry.getValue().doubleValue()));
        value1Count++;
      }
      if (entry.getKey().getSecondIndex() == MatchType.MATCHESBOTH) {
        value1[matchesBothCount] = value1[matchesBothCount]
            + Double.parseDouble(df.format(entry.getValue().doubleValue()));
        value2[matchesBothCount] = value2[matchesBothCount]
            + Double.parseDouble(df.format(entry.getValue().doubleValue()));
        matchesBothCount++;
      }
    }
    return exeUtils.createRatioResponse(timeArray, value1, value2, startTime, requestResource,
        inputProcessor.getRequestUrlIfGetRequest(servletRequest), servletResponse);
  }

  /**
   * Performs a count|length|perimeter|area-ratio calculation grouped by the boundary.
   * 
   * @deprecated Will be removed in next major version update.
   * 
   * @param requestResource {@link org.heigit.ohsome.ohsomeapi.executor.RequestResource
   *        RequestResource} definition of the request resource
   * @param servletRequest {@link javax.servlet.http.HttpServletRequest HttpServletRequest} incoming
   *        request object
   * @param servletResponse {@link javax.servlet.http.HttpServletResponse HttpServletResponse]}
   *        outgoing response object
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response}
   * @throws BadRequestException if a boundary parameter (bboxes, bcircles, bpolys) is not defined
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters},
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#count() count}, or
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#sum() sum}
   */
  @Deprecated(forRemoval = true)
  public static <P extends Geometry & Polygonal> Response aggregateBasicFiltersRatioGroupByBoundary(
      RequestResource requestResource, HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    final long startTime = System.currentTimeMillis();
    final boolean isSnapshot = true;
    final boolean isDensity = false;
    InputProcessor inputProcessor = new InputProcessor(servletRequest, isSnapshot, isDensity);
    inputProcessor.getProcessingData().setIsGroupByBoundary(true);
    inputProcessor.getProcessingData().setIsRatio(true);
    final MapReducer<OSMEntitySnapshot> intermediateMapRed = inputProcessor.processParameters();
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    if (processingData.getBoundaryType() == BoundaryType.NOBOUNDARY) {
      throw new BadRequestException(ExceptionMessages.NO_BOUNDARY);
    }
    String[] keys2 = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("keys2")));
    String[] values2 = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("values2")));
    inputProcessor.checkKeysValues(keys2, values2);
    Pair<String[], String[]> keys2Vals2 = inputProcessor.processKeys2Vals2(keys2, values2);
    keys2 = keys2Vals2.getKey();
    values2 = keys2Vals2.getValue();
    Integer[] keysInt1 = new Integer[requestParameters.getKeys().length];
    Integer[] valuesInt1 = new Integer[requestParameters.getValues().length];
    Integer[] keysInt2 = new Integer[keys2.length];
    Integer[] valuesInt2 = new Integer[values2.length];
    TagTranslator tt = DbConnData.tagTranslator;
    for (int i = 0; i < requestParameters.getKeys().length; i++) {
      keysInt1[i] = tt.getOSHDBTagKeyOf(requestParameters.getKeys()[i]).toInt();
      if (requestParameters.getValues() != null && i < requestParameters.getValues().length) {
        valuesInt1[i] =
            tt.getOSHDBTagOf(requestParameters.getKeys()[i], requestParameters.getValues()[i])
                .getValue();
      }
    }
    for (int i = 0; i < keys2.length; i++) {
      keysInt2[i] = tt.getOSHDBTagKeyOf(keys2[i]).toInt();
      if (i < values2.length) {
        valuesInt2[i] = tt.getOSHDBTagOf(keys2[i], values2[i]).getValue();
      }
    }
    EnumSet<OSMType> osmTypes1 = (EnumSet<OSMType>) processingData.getOsmTypes();
    String[] types1 = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("types")));
    String[] types2 = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("types2")));
    final EnumSet<SimpleFeatureType> simpleFeatureTypes1 =
        inputProcessor.defineSimpleFeatureTypes(types1);
    inputProcessor.defineTypes(types2, intermediateMapRed);
    EnumSet<OSMType> osmTypes2 =
        (EnumSet<OSMType>) inputProcessor.getProcessingData().getOsmTypes();
    EnumSet<OSMType> osmTypes = osmTypes1.clone();
    final EnumSet<SimpleFeatureType> simpleFeatureTypes2 =
        inputProcessor.defineSimpleFeatureTypes(types2);
    osmTypes.addAll(osmTypes2);
    String[] osmTypesString =
        osmTypes.stream().map(OSMType::toString).map(String::toLowerCase).toArray(String[]::new);
    MapReducer<OSMEntitySnapshot> mapRed = null;
    if (!inputProcessor.compareKeysValues(requestParameters.getKeys(), keys2,
        requestParameters.getValues(), values2)) {
      RequestParameters requestParams =
          new RequestParameters(servletRequest.getMethod(), isSnapshot, isDensity,
              servletRequest.getParameter("bboxes"), servletRequest.getParameter("bcircles"),
              servletRequest.getParameter("bpolys"), osmTypesString, new String[] {},
              new String[] {}, servletRequest.getParameterValues("time"),
              servletRequest.getParameter("format"), servletRequest.getParameter("showMetadata"),
              ProcessingData.getTimeout(), servletRequest.getParameter("filter"));
      ProcessingData secondProcessingData =
          new ProcessingData(requestParams, servletRequest.getRequestURL().toString());
      InputProcessor secondInputProcessor =
          new InputProcessor(servletRequest, isSnapshot, isDensity);
      secondInputProcessor.setProcessingData(secondProcessingData);
      mapRed = secondInputProcessor.processParameters();
    } else {
      mapRed = inputProcessor.processParameters();
    }
    mapRed = mapRed.osmType(osmTypes);
    ArrayList<Geometry> arrGeoms = new ArrayList<>(processingData.getBoundaryList());
    // intentionally as check for P on Polygonal is already performed
    @SuppressWarnings({"unchecked"})
    Map<Integer, P> geoms =
        arrGeoms.stream().collect(Collectors.toMap(arrGeoms::indexOf, geom -> (P) geom));
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    var mapRed2 = mapRed.aggregateByTimestamp().aggregateByGeometry(geoms);
    mapRed2 = exeUtils.snapshotFilter(mapRed2, osmTypes1, osmTypes2, simpleFeatureTypes1,
        simpleFeatureTypes2, keysInt1, keysInt2, valuesInt1, valuesInt2);
    var preResult =
        mapRed2.aggregateBy((SerializableFunction<OSMEntitySnapshot, MatchType>) snapshot -> {
          boolean matches1 = exeUtils.snapshotMatches(snapshot, osmTypes1, simpleFeatureTypes1,
              keysInt1, valuesInt1);
          boolean matches2 = exeUtils.snapshotMatches(snapshot, osmTypes2, simpleFeatureTypes2,
              keysInt2, valuesInt2);
          if (matches1 && matches2) {
            return MatchType.MATCHESBOTH;
          } else if (matches1) {
            return MatchType.MATCHES1;
          } else if (matches2) {
            return MatchType.MATCHES2;
          } else {
            assert false : "MatchType matches none.";
          }
          return MatchType.MATCHESNONE;
        }, EnumSet.allOf(MatchType.class)).map(OSMEntitySnapshot::getGeometry);
    SortedMap<OSHDBCombinedIndex<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, MatchType>, ? extends Number> result =
        null;
    switch (requestResource) {
      case COUNT:
        result = preResult.count();
        break;
      case LENGTH:
        result =
            preResult.sum(geom -> ExecutionUtils.cacheInUserData(geom, () -> Geo.lengthOf(geom)));
        break;
      case PERIMETER:
        result = preResult.sum(geom -> {
          if (!(geom instanceof Polygonal)) {
            return 0.0;
          }
          return ExecutionUtils.cacheInUserData(geom, () -> Geo.lengthOf(geom.getBoundary()));
        });
        break;
      case AREA:
        result =
            preResult.sum(geom -> ExecutionUtils.cacheInUserData(geom, () -> Geo.areaOf(geom)));
        break;
      default:
        break;
    }
    InputProcessingUtils utils = inputProcessor.getUtils();
    var groupByResult = ExecutionUtils.nest(result);
    Object[] boundaryIds = utils.getBoundaryIds();
    Double[] resultValues1 = null;
    Double[] resultValues2 = null;
    String[] timeArray = null;
    boolean timeArrayFilled = false;
    for (var entry : groupByResult.entrySet()) {
      var resultSet = entry.getValue().entrySet();
      if (!timeArrayFilled) {
        timeArray = new String[resultSet.size()];
      }
      if (entry.getKey() == MatchType.MATCHES2) {
        resultValues2 = exeUtils.fillElementsRatioGroupByBoundaryResultValues(resultSet, df);
      } else if (entry.getKey() == MatchType.MATCHES1) {
        resultValues1 = exeUtils.fillElementsRatioGroupByBoundaryResultValues(resultSet, df);
      } else if (entry.getKey() == MatchType.MATCHESBOTH) {
        int matchesBothCount = 0;
        int timeArrayCount = 0;
        for (var innerEntry : resultSet) {
          resultValues1[matchesBothCount] = resultValues1[matchesBothCount]
              + Double.parseDouble(df.format(innerEntry.getValue().doubleValue()));
          resultValues2[matchesBothCount] = resultValues2[matchesBothCount]
              + Double.parseDouble(df.format(innerEntry.getValue().doubleValue()));
          if (!timeArrayFilled) {
            String time = innerEntry.getKey().getFirstIndex().toString();
            if (matchesBothCount == 0 || !timeArray[timeArrayCount - 1].equals(time)) {
              timeArray[timeArrayCount] = innerEntry.getKey().getFirstIndex().toString();
              timeArrayCount++;
            }
          }
          matchesBothCount++;
        }
        timeArray = Arrays.stream(timeArray).filter(Objects::nonNull).toArray(String[]::new);
        timeArrayFilled = true;
      } else {
        // on MatchType.MATCHESNONE aggregated values are not needed / do not exist
      }
    }
    return exeUtils.createRatioGroupByBoundaryResponse(boundaryIds, timeArray, resultValues1,
        resultValues2, startTime, requestResource,
        inputProcessor.getRequestUrlIfGetRequest(servletRequest), servletResponse);
  }

  /**
   * Performs a count|length|perimeter|area-ratio calculation grouped by the boundary using the
   * filter and filter2 parameters.
   * 
   * @param requestResource {@link org.heigit.ohsome.ohsomeapi.executor.RequestResource
   *        RequestResource} definition of the request resource
   * @param servletRequest {@link javax.servlet.http.HttpServletRequest HttpServletRequest} incoming
   *        request object
   * @param servletResponse {@link javax.servlet.http.HttpServletResponse HttpServletResponse]}
   *        outgoing response object
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response}
   * @throws BadRequestException if a boundary parameter (bboxes, bcircles, bpolys) is not defined
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters},
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#count() count}, or
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#sum() sum}
   */
  public static <P extends Geometry & Polygonal> Response aggregateRatioGroupByBoundary(
      RequestResource requestResource, HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    if (null == servletRequest.getParameter("filter")
        && (null != servletRequest.getParameter("types")
            || null != servletRequest.getParameter("keys"))) {
      return aggregateBasicFiltersRatioGroupByBoundary(requestResource, servletRequest,
          servletResponse);
    }
    final long startTime = System.currentTimeMillis();
    // these 2 parameters always have these values for /ratio requests
    final boolean isSnapshot = true;
    final boolean isDensity = false;
    InputProcessor inputProcessor = new InputProcessor(servletRequest, isSnapshot, isDensity);
    inputProcessor.getProcessingData().setIsGroupByBoundary(true);
    inputProcessor.getProcessingData().setIsRatio(true);
    inputProcessor.processParameters();
    ProcessingData processingData = inputProcessor.getProcessingData();
    if (processingData.getBoundaryType() == BoundaryType.NOBOUNDARY) {
      throw new BadRequestException(ExceptionMessages.NO_BOUNDARY);
    }
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    String filter1 = inputProcessor.getProcessingData().getRequestParameters().getFilter();
    String filter2 = inputProcessor.createEmptyStringIfNull(servletRequest.getParameter("filter2"));
    inputProcessor.checkFilter(filter2);
    String combinedFilter = exeUtils.combineFiltersWithOr(filter1, filter2);
    FilterParser fp = new FilterParser(DbConnData.tagTranslator);
    FilterExpression filterExpr1 = inputProcessor.getUtils().parseFilter(fp, filter1);
    FilterExpression filterExpr2 = inputProcessor.getUtils().parseFilter(fp, filter2);
    RequestParameters requestParamsCombined = new RequestParameters(servletRequest.getMethod(),
        isSnapshot, isDensity, servletRequest.getParameter("bboxes"),
        servletRequest.getParameter("bcircles"), servletRequest.getParameter("bpolys"),
        new String[] {}, new String[] {}, new String[] {},
        servletRequest.getParameterValues("time"), servletRequest.getParameter("format"),
        servletRequest.getParameter("showMetadata"), ProcessingData.getTimeout(), combinedFilter);
    ProcessingData processingDataCombined =
        new ProcessingData(requestParamsCombined, servletRequest.getRequestURL().toString());
    InputProcessor inputProcessorCombined =
        new InputProcessor(servletRequest, isSnapshot, isDensity);
    inputProcessorCombined.setProcessingData(processingDataCombined);
    inputProcessorCombined.getProcessingData().setIsRatio(true);
    inputProcessorCombined.getProcessingData().setIsGroupByBoundary(true);
    MapReducer<OSMEntitySnapshot> mapRed = inputProcessorCombined.processParameters();
    ArrayList<Geometry> arrGeoms = new ArrayList<>(processingData.getBoundaryList());
    // intentionally as check for P on Polygonal is already performed
    @SuppressWarnings({"unchecked"})
    Map<Integer, P> geoms =
        arrGeoms.stream().collect(Collectors.toMap(arrGeoms::indexOf, geom -> (P) geom));
    var mapRed2 = mapRed.aggregateByTimestamp().aggregateByGeometry(geoms);
    mapRed2 = exeUtils.newSnapshotFilter(mapRed2, filterExpr1, filterExpr2);
    var preResult =
        mapRed2.aggregateBy((SerializableFunction<OSMEntitySnapshot, MatchType>) snapshot -> {
          OSMEntity entity = snapshot.getEntity();
          boolean matches1 = filterExpr1.applyOSMGeometry(entity, snapshot.getGeometry());
          boolean matches2 = filterExpr2.applyOSMGeometry(entity, snapshot.getGeometry());
          if (matches1 && matches2) {
            return MatchType.MATCHESBOTH;
          } else if (matches1) {
            return MatchType.MATCHES1;
          } else if (matches2) {
            return MatchType.MATCHES2;
          } else {
            assert false : "MatchType matches none.";
          }
          return MatchType.MATCHESNONE;
        }, EnumSet.allOf(MatchType.class)).map(OSMEntitySnapshot::getGeometry);
    SortedMap<OSHDBCombinedIndex<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, MatchType>, ? extends Number> result =
        null;
    switch (requestResource) {
      case COUNT:
        result = preResult.count();
        break;
      case LENGTH:
        result =
            preResult.sum(geom -> ExecutionUtils.cacheInUserData(geom, () -> Geo.lengthOf(geom)));
        break;
      case PERIMETER:
        result = preResult.sum(geom -> {
          if (!(geom instanceof Polygonal)) {
            return 0.0;
          }
          return ExecutionUtils.cacheInUserData(geom, () -> Geo.lengthOf(geom.getBoundary()));
        });
        break;
      case AREA:
        result =
            preResult.sum(geom -> ExecutionUtils.cacheInUserData(geom, () -> Geo.areaOf(geom)));
        break;
      default:
        break;
    }
    InputProcessingUtils utils = inputProcessor.getUtils();
    var groupByResult = ExecutionUtils.nest(result);
    Object[] boundaryIds = utils.getBoundaryIds();
    Double[] resultValues1 = null;
    Double[] resultValues2 = null;
    String[] timeArray = null;
    boolean timeArrayFilled = false;
    for (var entry : groupByResult.entrySet()) {
      var resultSet = entry.getValue().entrySet();
      if (!timeArrayFilled) {
        timeArray = new String[resultSet.size()];
      }
      if (entry.getKey() == MatchType.MATCHES2) {
        resultValues2 = exeUtils.fillElementsRatioGroupByBoundaryResultValues(resultSet, df);
      } else if (entry.getKey() == MatchType.MATCHES1) {
        resultValues1 = exeUtils.fillElementsRatioGroupByBoundaryResultValues(resultSet, df);
      } else if (entry.getKey() == MatchType.MATCHESBOTH) {
        int matchesBothCount = 0;
        int timeArrayCount = 0;
        for (var innerEntry : resultSet) {
          resultValues1[matchesBothCount] = resultValues1[matchesBothCount]
              + Double.parseDouble(df.format(innerEntry.getValue().doubleValue()));
          resultValues2[matchesBothCount] = resultValues2[matchesBothCount]
              + Double.parseDouble(df.format(innerEntry.getValue().doubleValue()));
          if (!timeArrayFilled) {
            String time = innerEntry.getKey().getFirstIndex().toString();
            if (matchesBothCount == 0 || !timeArray[timeArrayCount - 1].equals(time)) {
              timeArray[timeArrayCount] = innerEntry.getKey().getFirstIndex().toString();
              timeArrayCount++;
            }
          }
          matchesBothCount++;
        }
        timeArray = Arrays.stream(timeArray).filter(Objects::nonNull).toArray(String[]::new);
        timeArrayFilled = true;
      } else {
        // on MatchType.MATCHESNONE aggregated values are not needed / do not exist
      }
    }
    return exeUtils.createRatioGroupByBoundaryResponse(boundaryIds, timeArray, resultValues1,
        resultValues2, startTime, requestResource,
        inputProcessor.getRequestUrlIfGetRequest(servletRequest), servletResponse);
  }
}
