package org.heigit.ohsome.ohsomeapi.executor;

import java.text.DecimalFormat;
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
import org.heigit.ohsome.ohsomeapi.Application;
import org.heigit.ohsome.ohsomeapi.controller.dataextraction.elements.ElementsGeometry;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils.MatchType;
import org.heigit.ohsome.ohsomeapi.inputprocessing.BoundaryType;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessingUtils;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.output.Attribution;
import org.heigit.ohsome.ohsomeapi.output.Description;
import org.heigit.ohsome.ohsomeapi.output.ExtractionResponse;
import org.heigit.ohsome.ohsomeapi.output.Metadata;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.elements.ElementsResult;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResponse;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResult;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.SnapshotView;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.Area;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.Count;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.Length;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.Perimeter;
import org.heigit.ohsome.ohsomeapi.utils.GroupByBoundaryGeoJsonGenerator;
import org.heigit.ohsome.oshdb.OSHDBTag;
import org.heigit.ohsome.oshdb.OSHDBTimestamp;
import org.heigit.ohsome.oshdb.api.db.OSHDBIgnite;
import org.heigit.ohsome.oshdb.api.db.OSHDBIgnite.ComputeMode;
import org.heigit.ohsome.oshdb.api.generic.OSHDBCombinedIndex;
import org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;
import org.heigit.ohsome.oshdb.filter.FilterExpression;
import org.heigit.ohsome.oshdb.filter.FilterParser;
import org.heigit.ohsome.oshdb.osm.OSMEntity;
import org.heigit.ohsome.oshdb.osm.OSMType;
import org.heigit.ohsome.oshdb.util.celliterator.ContributionType;
import org.heigit.ohsome.oshdb.util.function.SerializableFunction;
import org.heigit.ohsome.oshdb.util.geometry.Geo;
import org.heigit.ohsome.oshdb.util.mappable.OSMEntitySnapshot;
import org.heigit.ohsome.oshdb.util.tagtranslator.TagTranslator;
import org.heigit.ohsome.oshdb.util.time.TimestampFormatter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygonal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.wololo.geojson.Feature;

/** Includes all execute methods for requests mapped to /elements. */
@Component
public class ElementsRequestExecutor {

//  public static final String URL = ExtractMetadata.attributionUrl;
//  public static final String TEXT = ExtractMetadata.attributionShort;
  @Autowired
  Attribution attribution;
  @Autowired
  public static final DecimalFormat df = ExecutionUtils.defineDecimalFormat("#.##");
  @Autowired
  HttpServletRequest servletRequest;
  @Autowired
  InputProcessor inputProcessor;
  @Autowired
  InputProcessingUtils utils;
  @Autowired
  SnapshotView snapshotView;

  /**
   * Performs an OSM data extraction.
   *
   * @param elemGeom {@link
   *        org.heigit.ohsome.ohsomeapi.controller.dataextraction.elements.ElementsGeometry
   *        ElementsGeometry} defining the geometry of the OSM elements
   * @param servletRequest {@link javax.servlet.http.HttpServletRequest HttpServletRequest} incoming
   *        request object
   * @param servletResponse {@link javax.servlet.http.HttpServletResponse HttpServletResponse]}
   *        outgoing response object
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor
   *         #processParameters() processParameters},
   *         {@link org.heigit.ohsome.oshdb.api.mapreducer.MapReducer#stream() stream}, or
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils
   *         #streamResponse(HttpServletResponse, ExtractionResponse, Stream)
   *         streamElementsResponse}
   */
  public void extract(Operation operation, ElementsGeometry elemGeom,
      HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws Exception {
    inputProcessor.setSnapshot(true);
    inputProcessor.setDensity(false);
    //InputProcessor inputProcessor = new InputProcessor(servletRequest, true, false);
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
      mapRed = inputProcessor.processParameters(ComputeMode.AFFINITY_CALL, snapshotView);
    } else {
      mapRed = inputProcessor.processParameters(snapshotView);
    }
    ProcessingData processingData = inputProcessor.getProcessingData();
    //RequestParameters requestParameters = processingData.getRequestParameters();
    TagTranslator tt = DbConnData.tagTranslator;
    String[] keys = servletRequest.getParameterValues("keys");
    final Set<Integer> keysInt = ExecutionUtils.keysToKeysInt(keys, tt);
    final MapReducer<Feature> preResult;
    final ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    preResult = mapRed.map(snapshot -> {
      Map<String, Object> properties = new TreeMap<>();
      if (includeOSMMetadata) {
        properties.put("@lastEdit",
            TimestampFormatter.getInstance().isoDateTime(snapshot.getEntity().getEpochSecond()));
      }
      properties.put("@snapshotTimestamp",
          TimestampFormatter.getInstance().isoDateTime(snapshot.getTimestamp()));
      Geometry geom = snapshot.getGeometry();
      if (!clipGeometries) {
        geom = snapshot.getGeometryUnclipped();
      }
      return exeUtils.createOSMFeature(snapshot.getEntity(), geom, properties, keysInt, includeTags,
          includeOSMMetadata, false, false, elemGeom,
          EnumSet.noneOf(ContributionType.class));
    }).filter(Objects::nonNull);
    Metadata metadata = null;
    if (processingData.isShowMetadata()) {
      metadata = new Metadata(null, operation.getDescription(),
          inputProcessor.getRequestUrlIfGetRequest());
    }
    ExtractionResponse osmData = new ExtractionResponse(attribution,
        Application.API_VERSION, metadata, "FeatureCollection", Collections.emptyList());
    try (Stream<Feature> streamResult = preResult.stream()) {
      exeUtils.streamResponse(servletResponse, osmData, streamResult);
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
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws BadRequestException if groupByKey parameter is not given
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor
   *         #processParameters() processParameters}
   */
  public <P extends Geometry & Polygonal> Response aggregateGroupByBoundaryGroupByTag(
      Operation operation, HttpServletRequest servletRequest,
      HttpServletResponse servletResponse, boolean isSnapshot, boolean isDensity) throws Exception {
    final long startTime = System.currentTimeMillis();
    MapReducer<OSMEntitySnapshot> mapRed = null;
    inputProcessor.setSnapshot(isSnapshot);
    inputProcessor.setDensity(isDensity);
    //InputProcessor inputProcessor = new InputProcessor(servletRequest, isSnapshot, isDensity);
    inputProcessor.getProcessingData().setGroupByBoundary(true);
    String[] groupByKey = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByKey")));
    if (groupByKey.length != 1) {
      throw new BadRequestException(ExceptionMessages.GROUP_BY_KEY_PARAM);
    }
    mapRed = inputProcessor.processParameters(snapshotView);
    ProcessingData processingData = inputProcessor.getProcessingData();
    //RequestParameters requestParameters = processingData.getRequestParameters();
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
    if (processingData.isContainingSimpleFeatureTypes()) {
      mapAgg = inputProcessor.filterOnSimpleFeatures(mapAgg);
    }
    Optional<FilterExpression> filter = processingData.getFilterExpression();
    if (filter.isPresent()) {
      mapAgg = mapAgg.filter(filter.get());
    }
    var result = ExecutionUtils.computeNestedResult(operation,
        mapAgg.map(f -> ExecutionUtils.mapSnapshotToTags(keysInt, valuesInt, f))
            .aggregateBy(Pair::getKey, zeroFill).map(Pair::getValue)
            .aggregateByTimestamp(OSMEntitySnapshot::getTimestamp));
    var groupByResult = OSHDBCombinedIndex.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.entrySet().size()];
    //InputProcessingUtils utils = inputProcessor.getUtils();
    Object[] boundaryIds = utils.getBoundaryIds();
    int count = 0;
    ArrayList<Geometry> boundaries = new ArrayList<>(processingData.getBoundaryList());
    for (var entry : groupByResult.entrySet()) {
      int boundaryIdentifier = entry.getKey().getFirstIndex();
      ElementsResult[] results = ExecutionUtils.fillElementsResult(
          entry.getValue(), inputProcessor.isDensity(), df, boundaries.get(boundaryIdentifier));
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
          Description.aggregateGroupByBoundaryGroupByTag(inputProcessor.isDensity(),
              operation.getDescription(), operation.getUnit()),
          inputProcessor.getRequestUrlIfGetRequest());
    }
    if ("csv".equalsIgnoreCase(servletRequest.getParameter("format"))) {
      ExecutionUtils exeUtils = new ExecutionUtils(processingData);
      exeUtils.writeCsvResponse(resultSet, servletResponse,
          ExecutionUtils.createCsvTopComments(attribution.getUrl(), attribution.getText(), Application.API_VERSION, metadata));
      return null;
    } else if ("geojson".equalsIgnoreCase(servletRequest.getParameter("format"))) {
      return GroupByResponse.of(attribution, Application.API_VERSION, metadata,
          "FeatureCollection", GroupByBoundaryGeoJsonGenerator.createGeoJsonFeatures(resultSet,
              processingData.getGeoJsonGeoms()));
    }
    return new GroupByResponse(attribution, Application.API_VERSION, metadata,
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
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws BadRequestException if groupByKey parameter is not given
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor
   *         #processParameters() processParameters} and
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils
   *         #computeResult(RequestResource, MapAggregator) computeResult}
   */
  public Response aggregateGroupByTag(Operation operation,
      HttpServletRequest servletRequest, HttpServletResponse servletResponse, boolean isSnapshot,
      boolean isDensity) throws Exception {
    final long startTime = System.currentTimeMillis();
    MapReducer<OSMEntitySnapshot> mapRed = null;
    inputProcessor.setSnapshot(isSnapshot);
    inputProcessor.setDensity(isDensity);
    //InputProcessor inputProcessor = new InputProcessor(servletRequest, isSnapshot, isDensity);
    String[] groupByKey = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByKey")));
    if (groupByKey.length != 1) {
      throw new BadRequestException(ExceptionMessages.GROUP_BY_KEY_PARAM);
    }
    mapRed = inputProcessor.processParameters(snapshotView);
    ProcessingData processingData = inputProcessor.getProcessingData();
    //RequestParameters requestParameters = processingData.getRequestParameters();
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
    var preResult = mapRed.map(f -> ExecutionUtils.mapSnapshotToTags(keysInt, valuesInt, f))
        .aggregateByTimestamp().aggregateBy(Pair::getKey, zeroFill).map(Pair::getValue);
    var result = ExecutionUtils.computeResult(operation, preResult);
    var groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    Geometry geom = inputProcessor.getGeometry();
    int count = 0;
    for (var entry : groupByResult.entrySet()) {
      ElementsResult[] results = ExecutionUtils.fillElementsResult(
          entry.getValue(), inputProcessor.isDensity(), df, geom);
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
      metadata = new Metadata(duration,
          Description.aggregateGroupByTag(inputProcessor.isDensity(),
              operation.getDescription(), operation.getUnit()),
          inputProcessor.getRequestUrlIfGetRequest());
    }
    if ("csv".equalsIgnoreCase(servletRequest.getParameter("format"))) {
      ExecutionUtils exeUtils = new ExecutionUtils(processingData);
      exeUtils.writeCsvResponse(resultSet, servletResponse,
          ExecutionUtils.createCsvTopComments(attribution.getUrl(), attribution.getText(), Application.API_VERSION, metadata));
      return null;
    }
    return new GroupByResponse(attribution, Application.API_VERSION, metadata,
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
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor
   *         #processParameters() processParameters} and
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils
   *         #computeResult(RequestResource, MapAggregator) computeResult}
   */
  public Response aggregateGroupByType(Operation operation,
      HttpServletRequest servletRequest, HttpServletResponse servletResponse, boolean isSnapshot,
      boolean isDensity) throws Exception {
    final long startTime = System.currentTimeMillis();
    MapReducer<OSMEntitySnapshot> mapRed = null;
    inputProcessor.setSnapshot(isSnapshot);
    inputProcessor.setDensity(isDensity);
    //InputProcessor inputProcessor = new InputProcessor(servletRequest, isSnapshot, isDensity);
    mapRed = inputProcessor.processParameters(snapshotView);
    ProcessingData processingData = inputProcessor.getProcessingData();
    //RequestParameters requestParameters = processingData.getRequestParameters();
    MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, OSMType>, OSMEntitySnapshot> preResult;
    preResult = mapRed.aggregateByTimestamp().aggregateBy(
        (SerializableFunction<OSMEntitySnapshot, OSMType>) f -> f.getEntity().getType(),
        processingData.getOsmTypes());
    var result = ExecutionUtils.computeResult(operation, preResult);
    var groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    Geometry geom = inputProcessor.getGeometry();
    int count = 0;
    for (var entry : groupByResult.entrySet()) {
      ElementsResult[] results = ExecutionUtils.fillElementsResult(
          entry.getValue(), inputProcessor.isDensity(), df, geom);
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    Metadata metadata = null;
    if (processingData.isShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration,
          Description.countPerimeterAreaGroupByType(inputProcessor.isDensity(),
              operation.getDescription(), operation.getUnit()),
          inputProcessor.getRequestUrlIfGetRequest());
    }
    if ("csv".equalsIgnoreCase(servletRequest.getParameter("format"))) {
      ExecutionUtils exeUtils = new ExecutionUtils(processingData);
      exeUtils.writeCsvResponse(resultSet, servletResponse,
          ExecutionUtils.createCsvTopComments(attribution.getUrl(), attribution.getText(), Application.API_VERSION, metadata));
      return null;
    }
    return new GroupByResponse(attribution, Application.API_VERSION, metadata,
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
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws BadRequestException if groupByKeys parameter is not given
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor
   *         #processParameters() processParameters} and
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils
   *         #computeResult(RequestResource, MapAggregator) computeResult}
   */
  public Response aggregateGroupByKey(Operation operation,
      HttpServletRequest servletRequest, HttpServletResponse servletResponse, boolean isSnapshot,
      boolean isDensity) throws Exception {
    final long startTime = System.currentTimeMillis();
    MapReducer<OSMEntitySnapshot> mapRed = null;
    inputProcessor.setSnapshot(isSnapshot);
    inputProcessor.setDensity(isDensity);
    //InputProcessor inputProcessor = new InputProcessor(servletRequest, isSnapshot, isDensity);
    String[] groupByKeys = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByKeys")));
    if (groupByKeys == null || groupByKeys.length == 0) {
      throw new BadRequestException(ExceptionMessages.GROUP_BY_KEYS_PARAM);
    }
    mapRed = inputProcessor.processParameters(snapshotView);
    ProcessingData processingData = inputProcessor.getProcessingData();
    //RequestParameters requestParameters = processingData.getRequestParameters();
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
    var result = ExecutionUtils.computeResult(operation, preResult);
    var groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    int count = 0;
    for (var entry : groupByResult.entrySet()) {
      ElementsResult[] results = ExecutionUtils.fillElementsResult(
          entry.getValue(), inputProcessor.isDensity(), df, null);
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
      metadata =
          new Metadata(duration,
              Description.aggregateGroupByKey(operation.getDescription(),
                  operation.getUnit()),
              inputProcessor.getRequestUrlIfGetRequest());
    }
    if ("csv".equalsIgnoreCase(servletRequest.getParameter("format"))) {
      ExecutionUtils exeUtils = new ExecutionUtils(processingData);
      exeUtils.writeCsvResponse(resultSet, servletResponse,
          ExecutionUtils.createCsvTopComments(attribution.getUrl(), attribution.getText(), Application.API_VERSION, metadata));
      return null;
    }
    return new GroupByResponse(metadata,
        resultSet);
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
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor
   *         #processParameters() processParameters} and
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils
   *         #computeResult(RequestResource, MapAggregator) computeResult}
   */
  public Response aggregateRatio(Operation operation,
      HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws Exception {
//    if (null == servletRequest.getParameter("filter")
//        && (null != servletRequest.getParameter("types")
//            || null != servletRequest.getParameter("keys"))) {
//      return aggregateBasicFiltersRatio(requestResource, servletRequest, servletResponse);
//    }
    final long startTime = System.currentTimeMillis();
    // these 2 parameters always have these values for /ratio requests
    final boolean isSnapshot = true;
    final boolean isDensity = false;
    inputProcessor.setSnapshot(isSnapshot);
    inputProcessor.setDensity(isDensity);
    //InputProcessor inputProcessor = new InputProcessor(servletRequest, isSnapshot, isDensity);
    inputProcessor.getProcessingData().setRatio(true);
    inputProcessor.processParameters(snapshotView);
    final ProcessingData processingData = inputProcessor.getProcessingData();
    String filter1 = inputProcessor.getFilter();
    String filter2 = InputProcessor.createEmptyStringIfNull(servletRequest.getParameter("filter2"));
    inputProcessor.checkFilter(filter2);
    String combinedFilter = ExecutionUtils.combineFiltersWithOr(filter1, filter2);
    FilterParser fp = new FilterParser(DbConnData.tagTranslator);
    FilterExpression filterExpr1 = inputProcessor.getUtils().parseFilter(fp, filter1);
    FilterExpression filterExpr2 = inputProcessor.getUtils().parseFilter(fp, filter2);
//    RequestParameters requestParamsCombined = new RequestParameters(isSnapshot, isDensity,
//        servletRequest.getParameter("bboxes"),
//        servletRequest.getParameter("bcircles"), servletRequest.getParameter("bpolys"),
//        new String[] {}, new String[] {}, new String[] {},
//        servletRequest.getParameterValues("time"), servletRequest.getParameter("format"),
//        servletRequest.getParameter("showMetadata"), ProcessingData.getTimeout(), combinedFilter);
//    InputProcessor inputProcessorCombined =
//        new InputProcessor(servletRequest, isSnapshot, isDensity);
//    ProcessingData processingDataCombined = inputProcessorCombined.getProcessingData();
        //new ProcessingData(requestParamsCombined, servletRequest.getRequestURL().toString());
inputProcessor.setFilter(combinedFilter);
  //  inputProcessorCombined.setProcessingData(processingDataCombined);
   // MapReducer<OSMEntitySnapshot> mapRed = inputProcessorCombined.processParameters();
    MapReducer<OSMEntitySnapshot> mapRed = inputProcessor.processParameters();
    mapRed = mapRed.filter(combinedFilter);
    var preResult = mapRed.aggregateByTimestamp().aggregateBy(snapshot -> {
      OSMEntity entity = snapshot.getEntity();
      boolean matches1 = filterExpr1.applyOSMGeometry(entity, snapshot::getGeometry);
      boolean matches2 = filterExpr2.applyOSMGeometry(entity, snapshot::getGeometry);
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
    var result = ExecutionUtils.computeResult(operation, preResult);
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
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    return exeUtils.createRatioResponse(timeArray, value1, value2, startTime, operation,
        inputProcessor.getRequestUrlIfGetRequest(), servletResponse);
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
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws BadRequestException if a boundary parameter (bboxes, bcircles, bpolys) is not defined
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor
   *         #processParameters() processParameters},
   *         {@link org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator#count() count}, or
   *         {@link org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator#sum() sum}
   */
  public <P extends Geometry & Polygonal> Response aggregateRatioGroupByBoundary(
      Operation operation, HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
//    if (null == servletRequest.getParameter("filter")
//        && (null != servletRequest.getParameter("types")
//            || null != servletRequest.getParameter("keys"))) {
//      return aggregateBasicFiltersRatioGroupByBoundary(requestResource, servletRequest,
//          servletResponse);
//    }
    final long startTime = System.currentTimeMillis();
    // these 2 parameters always have these values for /ratio requests
    final boolean isSnapshot = true;
    final boolean isDensity = false;
    inputProcessor.setSnapshot(isSnapshot);
    inputProcessor.setDensity(isDensity);
    //InputProcessor inputProcessor = new InputProcessor(servletRequest, isSnapshot, isDensity);
    inputProcessor.getProcessingData().setGroupByBoundary(true);
    inputProcessor.getProcessingData().setRatio(true);
    inputProcessor.processParameters(snapshotView);
    ProcessingData processingData = inputProcessor.getProcessingData();
    if (processingData.getBoundaryType() == BoundaryType.NOBOUNDARY) {
      throw new BadRequestException(ExceptionMessages.NO_BOUNDARY);
    }
    final String filter1 = inputProcessor.getFilter();
    final String filter2 =
        InputProcessor.createEmptyStringIfNull(servletRequest.getParameter("filter2"));
    inputProcessor.checkFilter(filter2);
    final String combinedFilter = ExecutionUtils.combineFiltersWithOr(filter1, filter2);
    final FilterParser fp = new FilterParser(DbConnData.tagTranslator);
    final FilterExpression filterExpr1 = inputProcessor.getUtils().parseFilter(fp, filter1);
    final FilterExpression filterExpr2 = inputProcessor.getUtils().parseFilter(fp, filter2);
//    RequestParameters requestParamsCombined = new RequestParameters(isSnapshot, isDensity,
//        servletRequest.getParameter("bboxes"),
//        servletRequest.getParameter("bcircles"), servletRequest.getParameter("bpolys"),
//        new String[] {}, new String[] {}, new String[] {},
//        servletRequest.getParameterValues("time"), servletRequest.getParameter("format"),
//        servletRequest.getParameter("showMetadata"), ProcessingData.getTimeout(), combinedFilter);
//    InputProcessor inputProcessorCombined =
//        new InputProcessor(servletRequest, isSnapshot, isDensity);
//    ProcessingData processingDataCombined = inputProcessorCombined.getProcessingData();
   //     new ProcessingData(requestParamsCombined, servletRequest.getRequestURL().toString());
//    InputProcessor inputProcessorCombined =
//        new InputProcessor(servletRequest, isSnapshot, isDensity);
//    inputProcessorCombined.setProcessingData(processingDataCombined);
//    inputProcessorCombined.getProcessingData().setRatio(true);
//    inputProcessorCombined.getProcessingData().setGroupByBoundary(true);
    inputProcessor.setFilter(combinedFilter);
    MapReducer<OSMEntitySnapshot> mapRed = inputProcessor.processParameters();
    ArrayList<Geometry> arrGeoms = new ArrayList<>(processingData.getBoundaryList());
    // intentionally as check for P on Polygonal is already performed
    @SuppressWarnings({"unchecked"})
    Map<Integer, P> geoms =
        arrGeoms.stream().collect(Collectors.toMap(arrGeoms::indexOf, geom -> (P) geom));
    var mapRed2 = mapRed.aggregateByTimestamp().aggregateByGeometry(geoms);
    mapRed2 = mapRed2.filter(combinedFilter);
    var mapRed3 =
        mapRed2.aggregateBy((SerializableFunction<OSMEntitySnapshot, MatchType>) snapshot -> {
          OSMEntity entity = snapshot.getEntity();
          boolean matches1 = filterExpr1.applyOSMGeometry(entity, snapshot::getGeometry);
          boolean matches2 = filterExpr2.applyOSMGeometry(entity, snapshot::getGeometry);
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
        }, EnumSet.allOf(MatchType.class));
    var mapRed3Geom = mapRed3.map(OSMEntitySnapshot::getGeometry);
    SortedMap<OSHDBCombinedIndex<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, MatchType>, ? extends
        Number> result = null;
    if (operation instanceof Count) {
      result = mapRed3.count();
    } else if (operation instanceof Length) {
      result =
          mapRed3Geom.sum(geom -> ExecutionUtils.cacheInUserData(geom, () -> Geo.lengthOf(geom)));

    } else if (operation instanceof Perimeter) {
      result = mapRed3Geom.sum(geom -> {
        if (!(geom instanceof Polygonal)) {
          return 0.0;
        }
        return ExecutionUtils.cacheInUserData(geom, () -> Geo.lengthOf(geom.getBoundary()));
      });
    } else if (operation instanceof Area) {
      result =
          mapRed3Geom.sum(geom -> ExecutionUtils.cacheInUserData(geom, () -> Geo.areaOf(geom)));
    } else {
    }
    //InputProcessingUtils utils = inputProcessor.getUtils();
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
        resultValues2 = ExecutionUtils.fillElementsRatioGroupByBoundaryResultValues(resultSet, df);
      } else if (entry.getKey() == MatchType.MATCHES1) {
        resultValues1 = ExecutionUtils.fillElementsRatioGroupByBoundaryResultValues(resultSet, df);
      } else if (entry.getKey() == MatchType.MATCHESBOTH) {
        int matchesBothCount = 0;
        int timeArrayCount = 0;
        for (var innerEntry : resultSet) {
          assert resultValues1 != null;
          assert resultValues2 != null;
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
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    return exeUtils.createRatioGroupByBoundaryResponse(boundaryIds, timeArray, resultValues1,
        resultValues2, startTime, operation,
        inputProcessor.getRequestUrlIfGetRequest(), servletResponse);
  }
}
