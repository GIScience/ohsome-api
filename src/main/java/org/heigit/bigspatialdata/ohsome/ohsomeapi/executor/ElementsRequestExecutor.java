package org.heigit.bigspatialdata.ohsome.ohsomeapi.executor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedMap;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.geojson.GeoJsonObject;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.Application;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.ExecutionUtils.MatchType;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.BoundaryType;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.GeometryBuilder;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.InputProcessingUtils;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.interceptor.RequestInterceptor;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.Description;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.Attribution;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.DefaultAggregationResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.Metadata;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.Response;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.elements.ElementsResult;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.groupByResponse.GroupByResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.groupByResponse.GroupByResult;
import org.heigit.bigspatialdata.oshdb.api.generic.OSHDBTimestampAndIndex;
import org.heigit.bigspatialdata.oshdb.api.generic.function.SerializableFunction;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregatorByTimestampAndIndex;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.object.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.osm.OSMEntity;
import org.heigit.bigspatialdata.oshdb.osm.OSMType;
import org.heigit.bigspatialdata.oshdb.util.OSHDBTag;
import org.heigit.bigspatialdata.oshdb.util.OSHDBTimestamp;
import org.heigit.bigspatialdata.oshdb.util.geometry.Geo;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;
import org.heigit.bigspatialdata.oshdb.util.time.TimestampFormatter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygonal;

/** Includes all execute methods for requests mapped to /elements. */
public class ElementsRequestExecutor {

  private static final String url = ExtractMetadata.attributionUrl;
  private static final String text = ExtractMetadata.attributionShort;

  /**
   * Performs a count|length|perimeter|area calculation.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (COUNT, LENGTH, PERIMETER,
   *        AREA).
   * @param requestParams <code>RequestParameters</code> object, which holds those parameters that
   *        are used in every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   */
  public static Response executeCountLengthPerimeterArea(RequestResource requestResource,
      RequestParameters requestParams) throws UnsupportedOperationException, Exception {
    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, ? extends Number> result = null;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor();
    String requestUrl = null;
    if (!requestParams.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    mapRed = inputProcessor.processParameters(mapRed, requestParams);
    switch (requestResource) {
      case COUNT:
        result = mapRed.aggregateByTimestamp().count();
        break;
      case AREA:
        result = mapRed.aggregateByTimestamp()
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
              return Geo.areaOf(snapshot.getGeometry());
            });
        break;
      case LENGTH:
        result = mapRed.aggregateByTimestamp()
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
              return Geo.lengthOf(snapshot.getGeometry());
            });
        break;
      case PERIMETER:
        result = mapRed.aggregateByTimestamp()
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
              if (snapshot.getGeometry() instanceof Polygonal) {
                return Geo.lengthOf(snapshot.getGeometry().getBoundary());
              } else {
                return 0.0;
              }
            });
        break;
      default:
        break;
    }
    GeometryBuilder geomBuilder = inputProcessor.getGeomBuilder();
    ExecutionUtils exeUtils = new ExecutionUtils();
    Geometry geom = exeUtils.getGeometry(ProcessingData.boundary, geomBuilder);
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    ElementsResult[] resultSet =
        exeUtils.fillElementsResult(result, requestParams.isDensity(), df, geom);
    Metadata metadata = null;
    if (ProcessingData.showMetadata) {
      long duration = System.currentTimeMillis() - startTime;
      metadata =
          new Metadata(duration, Description.countLengthPerimeterArea(requestParams.isDensity(),
              requestResource.getLabel(), requestResource.getUnit()), requestUrl);
    }
    DefaultAggregationResponse response = DefaultAggregationResponse.of(new Attribution(url, text),
        Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count|length|perimeter|area calculation grouped by the boundary.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (COUNT, LENGTH, PERIMETER,
   *        AREA).
   * @param requestParams <code>RequestParameters</code> object, which holds those parameters that
   *        are used in every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.groupByResponse.GroupByResponse
   *         GroupByResponse Content}
   */
  public static Response executeCountLengthPerimeterAreaGroupByBoundary(
      RequestResource requestResource, RequestParameters requestParams)
      throws UnsupportedOperationException, Exception {
    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    SortedMap<OSHDBTimestampAndIndex<Integer>, ? extends Number> result = null;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor();
    String requestUrl = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    if (!requestParams.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    mapRed = inputProcessor.processParameters(mapRed, requestParams);
    switch (requestResource) {
      case COUNT:
        result = exeUtils.computeCountLengthPerimeterAreaGbB(RequestResource.COUNT,
            ProcessingData.boundary, mapRed, inputProcessor.getGeomBuilder(),
            requestParams.isSnapshot());
        break;
      case LENGTH:
        result = exeUtils.computeCountLengthPerimeterAreaGbB(RequestResource.LENGTH,
            ProcessingData.boundary, mapRed, inputProcessor.getGeomBuilder(),
            requestParams.isSnapshot());
        break;
      case PERIMETER:
        result = exeUtils.computeCountLengthPerimeterAreaGbB(RequestResource.PERIMETER,
            ProcessingData.boundary, mapRed, inputProcessor.getGeomBuilder(),
            requestParams.isSnapshot());
        break;
      case AREA:
        result = exeUtils.computeCountLengthPerimeterAreaGbB(RequestResource.AREA,
            ProcessingData.boundary, mapRed, inputProcessor.getGeomBuilder(),
            requestParams.isSnapshot());
        break;
      default:
        break;
    }
    SortedMap<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    InputProcessingUtils utils = inputProcessor.getUtils();
    String[] boundaryIds = utils.getBoundaryIds();
    int count = 0;
    for (Entry<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      ElementsResult[] results = exeUtils.fillElementsResult(entry.getValue(),
          requestParams.isDensity(), df, inputProcessor.getGeomBuilder().getGeometry().get(count));
      groupByName = boundaryIds[count];
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    Metadata metadata = null;
    if (ProcessingData.showMetadata) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration,
          Description.countLengthPerimeterAreaGroupByBoundary(requestParams.isDensity(),
              requestResource.getLabel(), requestResource.getUnit()),
          requestUrl);
    }
    if (requestParams.getFormat() != null
        && requestParams.getFormat().equalsIgnoreCase("geojson")) {
      return GroupByResponse.of(new Attribution(url, text), Application.apiVersion, metadata,
          "FeatureCollection", exeUtils.createGeoJsonFeatures(resultSet,
              inputProcessor.getGeomBuilder().getGeoJsonGeoms()));
    } else {
      return new GroupByResponse(new Attribution(url, text), Application.apiVersion, metadata,
          resultSet);
    }
  }

  /**
   * Performs a count|length|perimeter|area calculation grouped by the user.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (COUNT, LENGTH, PERIMETER,
   *        AREA).
   * @param requestParams <code>RequestParameters</code> object, which holds those parameters that
   *        are used in every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.groupByResponse.GroupByResponse
   *         GroupByResponse Content}
   */
  public static Response executeCountLengthPerimeterAreaGroupByUser(RequestResource requestResource,
      RequestParameters requestParams) throws UnsupportedOperationException, Exception {
    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor();
    String requestUrl = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    ArrayList<Integer> useridsInt = new ArrayList<Integer>();
    if (!requestParams.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    mapRed = inputProcessor.processParameters(mapRed, requestParams);
    if (requestParams.getUserids() != null) {
      for (String user : requestParams.getUserids()) {
        useridsInt.add(Integer.parseInt(user));
      }
    }
    SortedMap<OSHDBTimestampAndIndex<Integer>, ? extends Number> result = null;
    MapAggregatorByTimestampAndIndex<Integer, OSMEntitySnapshot> preResult;
    SortedMap<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    preResult = mapRed.aggregateByTimestamp()
        .aggregateBy((SerializableFunction<OSMEntitySnapshot, Integer>) f -> {
          return f.getEntity().getUserId();
        }).zerofillIndices(useridsInt);
    result = exeUtils.computeResult(requestResource, preResult);
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];

    int count = 0;
    for (Entry<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {

      ElementsResult[] results =
          exeUtils.fillElementsResult(entry.getValue(), requestParams.isDensity(), df, null);
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }

    Metadata metadata = null;
    if (ProcessingData.showMetadata) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, Description.countLengthPerimeterAreaGroupByUser(
          requestResource.getLabel(), requestResource.getUnit()), requestUrl);
    }
    GroupByResponse response = new GroupByResponse(new Attribution(url, text),
        Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count|length|perimeter|area calculation grouped by the tag.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#countGroupByTag(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest, String[], String[])
   * countGroupByTag} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (COUNT, LENGTH, PERIMETER,
   *        AREA).
   * @param requestParams <code>RequestParameters</code> object, which holds those parameters that
   *        are used in every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.groupByResponse.GroupByResponse
   *         GroupByResponse Content}
   */
  public static Response executeCountLengthPerimeterAreaGroupByTag(RequestResource requestResource,
      RequestParameters requestParams, String[] groupByKey, String[] groupByValues)
      throws UnsupportedOperationException, Exception {
    long startTime = System.currentTimeMillis();
    if (groupByKey == null || groupByKey.length != 1) {
      throw new BadRequestException(
          "You need to give one groupByKey parameter, if you want to use groupBy/tag.");
    }
    ExecutionUtils exeUtils = new ExecutionUtils();
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor();
    String requestUrl = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    if (!requestParams.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    if (groupByValues == null) {
      groupByValues = new String[0];
    }
    TagTranslator tt = DbConnData.tagTranslator;
    Integer[] valuesInt = new Integer[groupByValues.length];
    ArrayList<Pair<Integer, Integer>> zeroFill = new ArrayList<Pair<Integer, Integer>>();
    mapRed = inputProcessor.processParameters(mapRed, requestParams);
    int keysInt = tt.getOSHDBTagKeyOf(groupByKey[0]).toInt();
    if (groupByValues.length != 0) {
      for (int j = 0; j < groupByValues.length; j++) {
        valuesInt[j] = tt.getOSHDBTagOf(groupByKey[0], groupByValues[j]).getValue();
        zeroFill.add(new ImmutablePair<Integer, Integer>(keysInt, valuesInt[j]));
      }
    }
    MapAggregator<OSHDBTimestampAndIndex<Pair<Integer, Integer>>, OSMEntitySnapshot> preResult =
        mapRed.map(f -> {
          int[] tags = f.getEntity().getRawTags();
          for (int i = 0; i < tags.length; i += 2) {
            int tagKeyId = tags[i];
            int tagValueId = tags[i + 1];
            if (tagKeyId == keysInt) {
              if (valuesInt.length == 0) {
                return new ImmutablePair<>(
                    new ImmutablePair<Integer, Integer>(tagKeyId, tagValueId), f);
              }
              for (int value : valuesInt) {
                if (tagValueId == value) {
                  return new ImmutablePair<>(
                      new ImmutablePair<Integer, Integer>(tagKeyId, tagValueId), f);
                }
              }
            }
          }
          return new ImmutablePair<>(new ImmutablePair<Integer, Integer>(-1, -1), f);
        }).aggregateByTimestamp().aggregateBy(Pair::getKey).zerofillIndices(zeroFill)
            .map(Pair::getValue);
    SortedMap<OSHDBTimestampAndIndex<Pair<Integer, Integer>>, ? extends Number> result = null;
    SortedMap<Pair<Integer, Integer>, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    result = exeUtils.computeResult(requestResource, preResult);
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    GeometryBuilder geomBuilder = inputProcessor.getGeomBuilder();
    Geometry geom = exeUtils.getGeometry(ProcessingData.boundary, geomBuilder);
    int count = 0;
    for (Entry<Pair<Integer, Integer>, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      ElementsResult[] results =
          exeUtils.fillElementsResult(entry.getValue(), requestParams.isDensity(), df, geom);
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
    if (ProcessingData.showMetadata) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration,
          Description.countLengthPerimeterAreaGroupByTag(requestParams.isDensity(),
              requestResource.getLabel(), requestResource.getUnit()),
          requestUrl);
    }
    GroupByResponse response = new GroupByResponse(new Attribution(url, text),
        Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count|perimeter|area calculation grouped by the OSM type.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (COUNT, LENGTH, PERIMETER,
   *        AREA).
   * @param requestParams <code>RequestParameters</code> object, which holds those parameters that
   *        are used in every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.groupByResponse.GroupByResponse
   *         GroupByResponseContent}
   */
  public static Response executeCountPerimeterAreaGroupByType(RequestResource requestResource,
      RequestParameters requestParams) throws UnsupportedOperationException, Exception {
    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor();
    String requestUrl = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    if (!requestParams.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    mapRed = inputProcessor.processParameters(mapRed, requestParams);
    MapAggregatorByTimestampAndIndex<OSMType, OSMEntitySnapshot> preResult = null;
    preResult = mapRed.aggregateByTimestamp()
        .aggregateBy((SerializableFunction<OSMEntitySnapshot, OSMType>) f -> {
          return f.getEntity().getType();
        }).zerofillIndices(ProcessingData.osmTypes);
    SortedMap<OSHDBTimestampAndIndex<OSMType>, ? extends Number> result = null;
    result = exeUtils.computeResult(requestResource, preResult);
    SortedMap<OSMType, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    GeometryBuilder geomBuilder = inputProcessor.getGeomBuilder();
    Geometry geom = exeUtils.getGeometry(ProcessingData.boundary, geomBuilder);
    int count = 0;
    for (Entry<OSMType, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      ElementsResult[] results =
          exeUtils.fillElementsResult(entry.getValue(), requestParams.isDensity(), df, geom);
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    Metadata metadata = null;
    if (ProcessingData.showMetadata) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration,
          Description.countPerimeterAreaGroupByType(requestParams.isDensity(),
              requestResource.getLabel(), requestResource.getUnit()),
          requestUrl);
    }
    GroupByResponse response = new GroupByResponse(new Attribution(url, text),
        Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count|length|perimeter|area calculation grouped by the key.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#countGroupByKey(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest, String[])
   * countGroupByKey} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (COUNT, LENGTH, PERIMETER,
   *        AREA).
   * @param requestParams <code>RequestParameters</code> object, which holds those parameters that
   *        are used in every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.groupByResponse.GroupByResponse
   *         GroupByResponse Content}
   */
  public static Response executeCountLengthPerimeterAreaGroupByKey(RequestResource requestResource,
      RequestParameters requestParams, String[] groupByKeys)
      throws UnsupportedOperationException, Exception {
    long startTime = System.currentTimeMillis();
    if (groupByKeys == null || groupByKeys.length == 0) {
      throw new BadRequestException(
          "You need to give at least one groupByKey parameter, if you want to use groupBy/key");
    }
    ExecutionUtils exeUtils = new ExecutionUtils();
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor();
    String requestUrl = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    if (!requestParams.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    TagTranslator tt = DbConnData.tagTranslator;
    Integer[] keysInt = new Integer[groupByKeys.length];
    mapRed = inputProcessor.processParameters(mapRed, requestParams);
    for (int i = 0; i < groupByKeys.length; i++) {
      keysInt[i] = tt.getOSHDBTagKeyOf(groupByKeys[i]).toInt();
    }
    MapAggregator<OSHDBTimestampAndIndex<Integer>, OSMEntitySnapshot> preResult =
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
          if (res.size() == 0) {
            res.add(new ImmutablePair<>(-1, f));
          }
          return res;
        }).aggregateByTimestamp().aggregateBy(Pair::getKey).zerofillIndices(Arrays.asList(keysInt))
            .map(Pair::getValue);
    SortedMap<OSHDBTimestampAndIndex<Integer>, ? extends Number> result = null;
    SortedMap<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    result = exeUtils.computeResult(requestResource, preResult);
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    int count = 0;
    for (Entry<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      ElementsResult[] results =
          exeUtils.fillElementsResult(entry.getValue(), requestParams.isDensity(), df, null);
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
    if (ProcessingData.showMetadata) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, Description.countLengthPerimeterAreaGroupByKey(
          requestResource.getLabel(), requestResource.getUnit()), requestUrl);
    }
    GroupByResponse response = new GroupByResponse(new Attribution(url, text),
        Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count|length|perimeter|area-share|ratio calculation.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#countRatio(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest, String[], String[], String[])
   * countRatio} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (COUNT, LENGTH, PERIMETER,
   *        AREA).
   * @param requestParams <code>RequestParameters</code> object, which holds those parameters that
   *        are used in every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   */
  public static Response executeCountLengthPerimeterAreaRatio(RequestResource requestResource,
      RequestParameters requestParams, String[] types2, String[] keys2, String[] values2,
      boolean isShare) throws UnsupportedOperationException, Exception {
    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor();
    String requestUrl = null;
    TagTranslator tt = DbConnData.tagTranslator;
    requestParams = inputProcessor.fillWithEmptyIfNull(requestParams);
    // for input processing/checking only
    inputProcessor.processParameters(mapRed, requestParams);
    inputProcessor.checkKeysValues(keys2, values2);
    values2 = inputProcessor.createEmptyArrayIfNull(values2);
    keys2 = inputProcessor.createEmptyArrayIfNull(keys2);
    if (isShare) {
      List<Pair<String, String>> keys2Vals2;
      if (requestParams.getValues().length == 0) {
        keys2 = inputProcessor.addFilterKeys(requestParams.getKeys(), keys2);
      } else if (keys2.length == 0) {
        keys2 = requestParams.getKeys();
        values2 = requestParams.getValues();
      } else {
        keys2Vals2 = inputProcessor.addFilterKeysVals(requestParams.getKeys(),
            requestParams.getValues(), keys2, values2);
        String[] newKeys2 = new String[keys2Vals2.size()];
        String[] newValues2 = new String[keys2Vals2.size()];
        for (int i = 0; i < keys2Vals2.size(); i++) {
          Pair<String, String> tag = keys2Vals2.get(i);
          newKeys2[i] = tag.getKey();
          newValues2[i] = tag.getValue();
        }
        keys2 = newKeys2;
        values2 =
            Arrays.stream(newValues2).filter(value -> !value.equals("")).toArray(String[]::new);
      }
    }
    Integer[] keysInt1 = new Integer[requestParams.getKeys().length];
    Integer[] valuesInt1 = new Integer[requestParams.getValues().length];
    Integer[] keysInt2 = new Integer[keys2.length];
    Integer[] valuesInt2 = new Integer[values2.length];
    if (!requestParams.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    for (int i = 0; i < requestParams.getKeys().length; i++) {
      keysInt1[i] = tt.getOSHDBTagKeyOf(requestParams.getKeys()[i]).toInt();
      if (requestParams.getValues() != null && i < requestParams.getValues().length) {
        valuesInt1[i] =
            tt.getOSHDBTagOf(requestParams.getKeys()[i], requestParams.getValues()[i]).getValue();
      }
    }
    for (int i = 0; i < keys2.length; i++) {
      keysInt2[i] = tt.getOSHDBTagKeyOf(keys2[i]).toInt();
      if (values2 != null && i < values2.length) {
        valuesInt2[i] = tt.getOSHDBTagOf(keys2[i], values2[i]).getValue();
      }
    }
    EnumSet<OSMType> osmTypes1 = ProcessingData.osmTypes;
    inputProcessor.defineOSMTypes(types2);
    EnumSet<OSMType> osmTypes2 = ProcessingData.osmTypes;
    EnumSet<OSMType> osmTypes = osmTypes1.clone();
    osmTypes.addAll(osmTypes2);
    String[] osmTypesString =
        osmTypes.stream().map(OSMType::toString).map(String::toLowerCase).toArray(String[]::new);
    if (!inputProcessor.compareKeysValues(requestParams.getKeys(), keys2, requestParams.getValues(),
        values2)) {
      mapRed = inputProcessor.processParameters(mapRed,
          new RequestParameters(requestParams.getRequestMethod(), requestParams.isSnapshot(),
              requestParams.isDensity(), requestParams.getBboxes(), requestParams.getBcircles(),
              requestParams.getBpolys(), osmTypesString, new String[] {}, new String[] {},
              requestParams.getUserids(), requestParams.getTime(),
              requestParams.getShowMetadata()));
      mapRed = mapRed.osmEntityFilter(entity -> {
        if (!exeUtils.entityMatches(entity, osmTypes1, keysInt1, valuesInt1)) {
          return exeUtils.entityMatches(entity, osmTypes2, keysInt2, valuesInt2);
        }
        return true;
      });
    } else {
      mapRed = inputProcessor.processParameters(mapRed, requestParams);
      mapRed = mapRed.osmType(osmTypes);
    }
    MapAggregatorByTimestampAndIndex<MatchType, OSMEntitySnapshot> preResult;
    preResult = mapRed.aggregateByTimestamp().aggregateBy(f -> {
      OSMEntity entity = f.getEntity();
      boolean matches1 = exeUtils.entityMatches(entity, osmTypes1, keysInt1, valuesInt1);
      boolean matches2 = exeUtils.entityMatches(entity, osmTypes2, keysInt2, valuesInt2);
      if (matches1 && matches2) {
        return MatchType.MATCHESBOTH;
      } else if (matches1) {
        return MatchType.MATCHES1;
      } else if (matches2) {
        return MatchType.MATCHES2;
      } else {
        // this should never be reached
        assert false : "MatchType matches none.";
      }
      return null;
    }).zerofillIndices(
        Arrays.asList(MatchType.MATCHESBOTH, MatchType.MATCHES1, MatchType.MATCHES2));
    SortedMap<OSHDBTimestampAndIndex<MatchType>, ? extends Number> result = null;
    result = exeUtils.computeResult(requestResource, preResult);
    int resultSize = result.size();
    Double[] value1 = new Double[resultSize / 3];
    Double[] value2 = new Double[resultSize / 3];
    String[] timeArray = new String[resultSize / 3];
    int value1Count = 0;
    int value2Count = 0;
    int matchesBothCount = 0;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    DecimalFormat ratioDf = exeUtils.defineDecimalFormat("#.######");
    // time and value extraction
    for (Entry<OSHDBTimestampAndIndex<MatchType>, ? extends Number> entry : result.entrySet()) {
      if (entry.getKey().getOtherIndex() == MatchType.MATCHES2) {
        timeArray[value2Count] =
            TimestampFormatter.getInstance().isoDateTime(entry.getKey().getTimeIndex());
        value2[value2Count] = Double.parseDouble(df.format(entry.getValue().doubleValue()));
        value2Count++;
      }
      if (entry.getKey().getOtherIndex() == MatchType.MATCHES1) {
        value1[value1Count] = Double.parseDouble(df.format(entry.getValue().doubleValue()));
        value1Count++;
      }
      if (entry.getKey().getOtherIndex() == MatchType.MATCHESBOTH) {
        value1[matchesBothCount] = value1[matchesBothCount]
            + Double.parseDouble(df.format(entry.getValue().doubleValue()));
        value2[matchesBothCount] = value2[matchesBothCount]
            + Double.parseDouble(df.format(entry.getValue().doubleValue()));
        matchesBothCount++;
      }
    }
    return exeUtils.createRatioShareResponse(isShare, timeArray, value1, value2, ratioDf,
        inputProcessor, startTime, requestResource, requestUrl, new Attribution(url, text));
  }

  /**
   * Performs a count|length|perimeter|area-ratio calculation grouped by the boundary.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#countRatio(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest, String[], String[], String[])
   * countRatio} method.
   * 
   * @param requestParams <code>RequestParameters</code> object, which holds those parameters that
   *        are used in every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.groupByResponse.RatioGroupByBoundaryResponse
   *         RatioGroupByBoundaryResponse Content}
   */
  public static Response executeCountLengthPerimeterAreaRatioGroupByBoundary(
      RequestResource requestResource, RequestParameters requestParams, String[] types2,
      String[] keys2, String[] values2, boolean isShare)
      throws UnsupportedOperationException, Exception {
    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    SortedMap<OSHDBTimestampAndIndex<Pair<Integer, MatchType>>, ? extends Number> result = null;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor();
    String requestUrl = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    TagTranslator tt = DbConnData.tagTranslator;
    requestParams = inputProcessor.fillWithEmptyIfNull(requestParams);
    inputProcessor.processParameters(mapRed, requestParams);
    if (ProcessingData.boundary == BoundaryType.NOBOUNDARY) {
      throw new BadRequestException(
          "You need to give at least one boundary parameter if you want to use /groupBy/boundary.");
    }
    GeometryBuilder geomBuilder = inputProcessor.getGeomBuilder();
    final GeoJsonObject[] geoJsonGeoms = geomBuilder.getGeoJsonGeoms();
    inputProcessor.checkKeysValues(keys2, values2);
    values2 = inputProcessor.createEmptyArrayIfNull(values2);
    keys2 = inputProcessor.createEmptyArrayIfNull(keys2);
    if (isShare) {
      List<Pair<String, String>> keys2Vals2;
      if (requestParams.getValues().length == 0) {
        keys2 = inputProcessor.addFilterKeys(requestParams.getKeys(), keys2);
      } else if (keys2.length == 0) {
        keys2 = requestParams.getKeys();
        values2 = requestParams.getValues();
      } else {
        keys2Vals2 = inputProcessor.addFilterKeysVals(requestParams.getKeys(),
            requestParams.getValues(), keys2, values2);
        String[] newKeys2 = new String[keys2Vals2.size()];
        String[] newValues2 = new String[keys2Vals2.size()];
        for (int i = 0; i < keys2Vals2.size(); i++) {
          Pair<String, String> tag = keys2Vals2.get(i);
          newKeys2[i] = tag.getKey();
          newValues2[i] = tag.getValue();
        }
        keys2 = newKeys2;
        values2 =
            Arrays.stream(newValues2).filter(value -> !value.equals("")).toArray(String[]::new);
      }
    }
    Integer[] keysInt1 = new Integer[requestParams.getKeys().length];
    Integer[] valuesInt1 = new Integer[requestParams.getValues().length];
    Integer[] keysInt2 = new Integer[keys2.length];
    Integer[] valuesInt2 = new Integer[values2.length];
    if (!requestParams.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    for (int i = 0; i < requestParams.getKeys().length; i++) {
      keysInt1[i] = tt.getOSHDBTagKeyOf(requestParams.getKeys()[i]).toInt();
      if (requestParams.getValues() != null && i < requestParams.getValues().length) {
        valuesInt1[i] =
            tt.getOSHDBTagOf(requestParams.getKeys()[i], requestParams.getValues()[i]).getValue();
      }
    }
    for (int i = 0; i < keys2.length; i++) {
      keysInt2[i] = tt.getOSHDBTagKeyOf(keys2[i]).toInt();
      if (values2 != null && i < values2.length) {
        valuesInt2[i] = tt.getOSHDBTagOf(keys2[i], values2[i]).getValue();
      }
    }
    EnumSet<OSMType> osmTypes1 = ProcessingData.osmTypes;
    inputProcessor.defineOSMTypes(types2);
    EnumSet<OSMType> osmTypes2 = ProcessingData.osmTypes;
    EnumSet<OSMType> osmTypes = osmTypes1.clone();
    osmTypes.addAll(osmTypes2);
    String[] osmTypesString =
        osmTypes.stream().map(OSMType::toString).map(String::toLowerCase).toArray(String[]::new);
    if (!inputProcessor.compareKeysValues(requestParams.getKeys(), keys2, requestParams.getValues(),
        values2)) {
      mapRed = inputProcessor.processParameters(mapRed,
          new RequestParameters(requestParams.getRequestMethod(), requestParams.isSnapshot(),
              requestParams.isDensity(), requestParams.getBboxes(), requestParams.getBcircles(),
              requestParams.getBpolys(), osmTypesString, new String[] {}, new String[] {},
              requestParams.getUserids(), requestParams.getTime(),
              requestParams.getShowMetadata()));
      mapRed = mapRed.osmEntityFilter(entity -> {
        boolean matches1 = exeUtils.entityMatches(entity, osmTypes1, keysInt1, valuesInt1);
        boolean matches2 = exeUtils.entityMatches(entity, osmTypes2, keysInt2, valuesInt2);
        return matches1 || matches2;
      });
    } else {
      mapRed = inputProcessor.processParameters(mapRed, requestParams);
      mapRed = mapRed.osmType(osmTypes);
    }
    ArrayList<Geometry> geoms = geomBuilder.getGeometry();
    List<Pair<Integer, MatchType>> zeroFill = new LinkedList<>();
    for (int j = 0; j < geoms.size(); j++) {
      zeroFill.add(new ImmutablePair<>(j, MatchType.MATCHESBOTH));
      zeroFill.add(new ImmutablePair<>(j, MatchType.MATCHES1));
      zeroFill.add(new ImmutablePair<>(j, MatchType.MATCHES2));
    }
    MapAggregator<OSHDBTimestampAndIndex<Pair<Integer, MatchType>>, Geometry> preResult = null;
    preResult = mapRed.aggregateByTimestamp().flatMap(f -> {
      List<Pair<Pair<Integer, OSMEntity>, Geometry>> res = new LinkedList<>();
      Geometry entityGeom = f.getGeometry();
      if (requestResource.equals(RequestResource.PERIMETER)) {
        entityGeom = entityGeom.getBoundary();
      }
      for (int i = 0; i < geoms.size(); i++) {
        if (entityGeom.intersects(geoms.get(i))) {
          if (entityGeom.within(geoms.get(i))) {
            res.add(new ImmutablePair<>(new ImmutablePair<>(i, f.getEntity()), entityGeom));
          } else {
            res.add(new ImmutablePair<>(new ImmutablePair<>(i, f.getEntity()),
                Geo.clip(entityGeom, (Geometry & Polygonal) geoms.get(i))));
          }
        }
      }
      return res;
    }).aggregateBy(f -> {
      OSMEntity entity = f.getLeft().getRight();
      boolean matches1 = exeUtils.entityMatches(entity, osmTypes1, keysInt1, valuesInt1);
      boolean matches2 = exeUtils.entityMatches(entity, osmTypes2, keysInt2, valuesInt2);
      if (matches1 && matches2) {
        return new ImmutablePair<>(f.getLeft().getLeft(), MatchType.MATCHESBOTH);
      } else if (matches1) {
        return new ImmutablePair<>(f.getLeft().getLeft(), MatchType.MATCHES1);
      } else if (matches2) {
        return new ImmutablePair<>(f.getLeft().getLeft(), MatchType.MATCHES2);
      } else {
        assert false : "MatchType matches none.";
      }
      return new ImmutablePair<>(f.getLeft().getLeft(), MatchType.MATCHESNONE);
    }).zerofillIndices(zeroFill).map(Pair::getValue);
    switch (requestResource) {
      case COUNT:
        result = preResult.count();
        break;
      case LENGTH:
      case PERIMETER:
        result = preResult.sum(geom -> {
          return Geo.lengthOf(geom);
        });
        break;
      case AREA:
        result = preResult.sum(geom -> {
          return Geo.areaOf(geom);
        });
        break;
      default:
        break;
    }
    SortedMap<Pair<Integer, MatchType>, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    InputProcessingUtils utils = inputProcessor.getUtils();
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    ArrayList<Double[]> value1Arrays = new ArrayList<Double[]>();
    ArrayList<Double[]> value2Arrays = new ArrayList<Double[]>();
    String[] boundaryIds = utils.getBoundaryIds();
    Double[] value1 = null;
    Double[] value2 = null;
    String[] timeArray = null;
    boolean timeArrayFilled = false;
    int count = 1;
    for (Entry<Pair<Integer, MatchType>, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      if (!timeArrayFilled) {
        timeArray = new String[entry.getValue().entrySet().size()];
      }
      if (entry.getKey().getRight() == MatchType.MATCHES2) {
        value2 = new Double[entry.getValue().entrySet().size()];
        int value2Count = 0;
        for (Entry<OSHDBTimestamp, ? extends Number> innerEntry : entry.getValue().entrySet()) {
          value2[value2Count] = Double.parseDouble(df.format(innerEntry.getValue().doubleValue()));
          value2Count++;
        }
      } else if (entry.getKey().getRight() == MatchType.MATCHES1) {
        value1 = new Double[entry.getValue().entrySet().size()];
        int value1Count = 0;
        for (Entry<OSHDBTimestamp, ? extends Number> innerEntry : entry.getValue().entrySet()) {
          value1[value1Count] = Double.parseDouble(df.format(innerEntry.getValue().doubleValue()));
          value1Count++;
        }
      } else if (entry.getKey().getRight() == MatchType.MATCHESBOTH) {
        int matchesBothCount = 0;
        for (Entry<OSHDBTimestamp, ? extends Number> innerEntry : entry.getValue().entrySet()) {
          value1[matchesBothCount] = value1[matchesBothCount]
              + Double.parseDouble(df.format(innerEntry.getValue().doubleValue()));
          value2[matchesBothCount] = value2[matchesBothCount]
              + Double.parseDouble(df.format(innerEntry.getValue().doubleValue()));
          if (!timeArrayFilled) {
            timeArray[matchesBothCount] = innerEntry.getKey().toString();
          }
          matchesBothCount++;
        }
        timeArrayFilled = true;
      } else {
        // on MatchType.MATCHESNONE aggregated values are not needed / do not exist
      }
      if (count % 3 == 0) {
        value1Arrays.add(value1);
        value2Arrays.add(value2);
      }
      count++;
    }
    DecimalFormat ratioDf = exeUtils.defineDecimalFormat("#.######");
    return exeUtils.createRatioShareGroupByBoundaryResponse(isShare, requestParams,
        groupByResult.size(), boundaryIds, timeArray, value1Arrays, value2Arrays, ratioDf,
        inputProcessor, startTime, requestResource, requestUrl, new Attribution(url, text),
        geoJsonGeoms);
  }
}
