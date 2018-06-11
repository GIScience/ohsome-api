package org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.executor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.Application;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.executor.ExecutionUtils.MatchType;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.BoundaryType;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.GeometryBuilder;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.InputProcessor;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.Utils;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.interceptor.RequestInterceptor;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.oshdb.DbConnData;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.oshdb.ExtractMetadata;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.Description;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Attribution;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Metadata;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.RatioShareResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.elements.ElementsResult;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResult;
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
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (COUNT, LENGTH, PERIMETER,
   *        AREA).
   * @param rPs <code>RequestParameters</code> object, which holds those parameters that are used in
   *        every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   */
  public static DefaultAggregationResponse executeCountLengthPerimeterArea(
      RequestResource requestResource, RequestParameters rPs)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    SortedMap<OSHDBTimestamp, ? extends Number> result = null;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String requestURL = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    if (!rPs.isPost())
      requestURL = RequestInterceptor.requestUrl;
    mapRed = iP.processParameters(mapRed, rPs);
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
              if (snapshot.getGeometry() instanceof Polygonal)
                return Geo.lengthOf(snapshot.getGeometry().getBoundary());
              else
                return 0.0;
            });
        break;
    }
    GeometryBuilder geomBuilder = iP.getGeomBuilder();
    Geometry geom = exeUtils.getGeometry(iP.getBoundaryType(), geomBuilder);
    ElementsResult[] resultSet = exeUtils.fillElementsResult(result, rPs.isDensity(), df, geom);
    Metadata metadata = null;
    if (iP.getShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, Description.countLengthPerimeterArea(rPs.isDensity(),
          requestResource.getLabel(), requestResource.getUnit()), requestURL);
    }
    DefaultAggregationResponse response = new DefaultAggregationResponse(new Attribution(url, text),
        Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count|length|perimeter|area calculation grouped by the boundary.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (COUNT, LENGTH, PERIMETER,
   *        AREA).
   * @param rPs <code>RequestParameters</code> object, which holds those parameters that are used in
   *        every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResponse
   *         GroupByResponse Content}
   */
  public static GroupByResponse executeCountLengthPerimeterAreaGroupByBoundary(
      RequestResource requestResource, RequestParameters rPs)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    SortedMap<OSHDBTimestampAndIndex<Integer>, ? extends Number> result = null;
    SortedMap<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String requestURL = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    if (!rPs.isPost())
      requestURL = RequestInterceptor.requestUrl;
    mapRed = iP.processParameters(mapRed, rPs);
    switch (requestResource) {
      case COUNT:
        result = exeUtils.computeCountLengthPerimeterAreaGBB(RequestResource.COUNT,
            iP.getBoundaryType(), mapRed, iP.getGeomBuilder(), rPs.isSnapshot());
        break;
      case LENGTH:
        result = exeUtils.computeCountLengthPerimeterAreaGBB(RequestResource.LENGTH,
            iP.getBoundaryType(), mapRed, iP.getGeomBuilder(), rPs.isSnapshot());
        break;
      case PERIMETER:
        result = exeUtils.computeCountLengthPerimeterAreaGBB(RequestResource.PERIMETER,
            iP.getBoundaryType(), mapRed, iP.getGeomBuilder(), rPs.isSnapshot());
        break;
      case AREA:
        result = exeUtils.computeCountLengthPerimeterAreaGBB(RequestResource.AREA,
            iP.getBoundaryType(), mapRed, iP.getGeomBuilder(), rPs.isSnapshot());
        break;
    }
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    Utils utils = iP.getUtils();
    String[] boundaryIds = utils.getBoundaryIds();
    int count = 0;
    for (Entry<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      ElementsResult[] results =
          exeUtils.fillElementsResult(entry.getValue(), rPs.isDensity(), df, null);
      groupByName = boundaryIds[count];
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    Metadata metadata = null;
    if (iP.getShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, Description.countLengthPerimeterAreaGroupByBoundary(
          requestResource.getLabel(), requestResource.getUnit()), requestURL);
    }
    GroupByResponse response = new GroupByResponse(new Attribution(url, text),
        Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count|length|perimeter|area calculation grouped by the user.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (COUNT, LENGTH, PERIMETER,
   *        AREA).
   * @param rPs <code>RequestParameters</code> object, which holds those parameters that are used in
   *        every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResponse
   *         GroupByResponse Content}
   */
  public static GroupByResponse executeCountLengthPerimeterAreaGroupByUser(
      RequestResource requestResource, RequestParameters rPs)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    SortedMap<OSHDBTimestampAndIndex<Integer>, ? extends Number> result = null;
    MapAggregatorByTimestampAndIndex<Integer, OSMEntitySnapshot> preResult;
    SortedMap<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String requestURL = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    ArrayList<Integer> useridsInt = new ArrayList<Integer>();
    if (!rPs.isPost())
      requestURL = RequestInterceptor.requestUrl;
    mapRed = iP.processParameters(mapRed, rPs);
    if (rPs.getUserids() != null)
      for (String user : rPs.getUserids())
        useridsInt.add(Integer.parseInt(user));
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
          exeUtils.fillElementsResult(entry.getValue(), rPs.isDensity(), df, null);
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }

    Metadata metadata = null;
    if (iP.getShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, Description.countLengthPerimeterAreaGroupByUser(
          requestResource.getLabel(), requestResource.getUnit()), requestURL);
    }
    GroupByResponse response = new GroupByResponse(new Attribution(url, text),
        Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count|length|perimeter|area calculation grouped by the tag.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCountGroupByTag(String, String, String, String[], String[], String[], String[], String[], String, String[], String[])
   * getCountGroupByTag} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (COUNT, LENGTH, PERIMETER,
   *        AREA).
   * @param rPs <code>RequestParameters</code> object, which holds those parameters that are used in
   *        every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResponse
   *         GroupByResponse Content}
   */
  public static GroupByResponse executeCountLengthPerimeterAreaGroupByTag(
      RequestResource requestResource, RequestParameters rPs, String[] groupByKey,
      String[] groupByValues) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    if (groupByKey == null || groupByKey.length != 1)
      throw new BadRequestException(
          "You need to give one groupByKey parameter, if you want to use groupBy/tag.");
    ExecutionUtils exeUtils = new ExecutionUtils();
    SortedMap<OSHDBTimestampAndIndex<Pair<Integer, Integer>>, ? extends Number> result = null;
    SortedMap<Pair<Integer, Integer>, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String requestURL = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    if (!rPs.isPost())
      requestURL = RequestInterceptor.requestUrl;
    if (groupByValues == null)
      groupByValues = new String[0];
    TagTranslator tt = DbConnData.tagTranslator;
    Integer[] valuesInt = new Integer[groupByValues.length];
    ArrayList<Pair<Integer, Integer>> zeroFill = new ArrayList<Pair<Integer, Integer>>();
    mapRed = iP.processParameters(mapRed, rPs);
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
                if (tagValueId == value)
                  return new ImmutablePair<>(
                      new ImmutablePair<Integer, Integer>(tagKeyId, tagValueId), f);
              }
            }
          }
          return new ImmutablePair<>(new ImmutablePair<Integer, Integer>(-1, -1), f);
        }).aggregateByTimestamp().aggregateBy(Pair::getKey).zerofillIndices(zeroFill)
            .map(Pair::getValue);
    result = exeUtils.computeResult(requestResource, preResult);
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    GeometryBuilder geomBuilder = iP.getGeomBuilder();
    Geometry geom = exeUtils.getGeometry(iP.getBoundaryType(), geomBuilder);
    int count = 0;
    for (Entry<Pair<Integer, Integer>, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      ElementsResult[] results =
          exeUtils.fillElementsResult(entry.getValue(), rPs.isDensity(), df, geom);
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
    if (iP.getShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata =
          new Metadata(duration, Description.countLengthPerimeterAreaGroupByTag(rPs.isDensity(),
              requestResource.getLabel(), requestResource.getUnit()), requestURL);
    }
    GroupByResponse response = new GroupByResponse(new Attribution(url, text),
        Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count|perimeter|area calculation grouped by the OSM type.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (COUNT, LENGTH, PERIMETER,
   *        AREA).
   * @param rPs <code>RequestParameters</code> object, which holds those parameters that are used in
   *        every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResponse
   *         GroupByResponseContent}
   */
  public static GroupByResponse executeCountPerimeterAreaGroupByType(
      RequestResource requestResource, RequestParameters rPs)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    SortedMap<OSHDBTimestampAndIndex<OSMType>, ? extends Number> result = null;
    MapAggregatorByTimestampAndIndex<OSMType, OSMEntitySnapshot> preResult = null;
    SortedMap<OSMType, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String requestURL = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    if (!rPs.isPost())
      requestURL = RequestInterceptor.requestUrl;
    mapRed = iP.processParameters(mapRed, rPs);
    preResult = mapRed.aggregateByTimestamp()
        .aggregateBy((SerializableFunction<OSMEntitySnapshot, OSMType>) f -> {
          return f.getEntity().getType();
        }).zerofillIndices(iP.getOsmTypes());
    result = exeUtils.computeResult(requestResource, preResult);
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    GeometryBuilder geomBuilder = iP.getGeomBuilder();
    Geometry geom = exeUtils.getGeometry(iP.getBoundaryType(), geomBuilder);
    int count = 0;
    for (Entry<OSMType, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      ElementsResult[] results =
          exeUtils.fillElementsResult(entry.getValue(), rPs.isDensity(), df, geom);
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    Metadata metadata = null;
    if (iP.getShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, Description.countPerimeterAreaGroupByType(rPs.isDensity(),
          requestResource.getLabel(), requestResource.getUnit()), requestURL);
    }
    GroupByResponse response = new GroupByResponse(new Attribution(url, text),
        Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count|length|perimeter|area calculation grouped by the key.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCountGroupByKey(String, String, String, String[], String[], String[], String[], String[], String, String[])
   * groupByKey} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (COUNT, LENGTH, PERIMETER,
   *        AREA).
   * @param rPs <code>RequestParameters</code> object, which holds those parameters that are used in
   *        every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResponse
   *         GroupByResponse Content}
   */
  public static GroupByResponse executeCountLengthPerimeterAreaGroupByKey(
      RequestResource requestResource, RequestParameters rPs, String[] groupByKeys)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    if (groupByKeys == null || groupByKeys.length == 0)
      throw new BadRequestException(
          "You need to give at least one groupByKey parameter, if you want to use groupBy/key");
    ExecutionUtils exeUtils = new ExecutionUtils();
    SortedMap<OSHDBTimestampAndIndex<Integer>, ? extends Number> result = null;
    SortedMap<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String requestURL = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    if (!rPs.isPost())
      requestURL = RequestInterceptor.requestUrl;
    TagTranslator tt = DbConnData.tagTranslator;
    Integer[] keysInt = new Integer[groupByKeys.length];
    mapRed = iP.processParameters(mapRed, rPs);
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
          if (res.size() == 0)
            res.add(new ImmutablePair<>(-1, f));
          return res;
        }).aggregateByTimestamp().aggregateBy(Pair::getKey).zerofillIndices(Arrays.asList(keysInt))
            .map(Pair::getValue);
    result = exeUtils.computeResult(requestResource, preResult);
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    int count = 0;
    for (Entry<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      ElementsResult[] results =
          exeUtils.fillElementsResult(entry.getValue(), rPs.isDensity(), df, null);
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
    if (iP.getShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, Description.countLengthPerimeterAreaGroupByKey(
          requestResource.getLabel(), requestResource.getUnit()), requestURL);
    }
    GroupByResponse response = new GroupByResponse(new Attribution(url, text),
        Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count|length|perimeter|area-share|ratio calculation.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCountRatio(String, String, String, String[], String[], String[], String[], String[], String, String[], String[], String[])
   * getCountRatio} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (COUNT, LENGTH, PERIMETER,
   *        AREA).
   * @param rPs <code>RequestParameters</code> object, which holds those parameters that are used in
   *        every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   */
  public static RatioShareResponse executeCountLengthPerimeterAreaRatio(
      RequestResource requestResource, RequestParameters rPs, String[] types2, String[] keys2,
      String[] values2, boolean isShare) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    SortedMap<OSHDBTimestampAndIndex<MatchType>, ? extends Number> result = null;
    MapAggregatorByTimestampAndIndex<MatchType, OSMEntitySnapshot> preResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String requestURL = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    DecimalFormat ratioDf = exeUtils.defineDecimalFormat("#.######");
    TagTranslator tt = DbConnData.tagTranslator;
    rPs = iP.fillWithEmptyIfNull(rPs);
    // for input processing/checking only
    iP.processParameters(mapRed, rPs);
    iP.checkKeysValues(keys2, values2);
    values2 = iP.createEmptyArrayIfNull(values2);
    keys2 = iP.createEmptyArrayIfNull(keys2);
    if (isShare) {
      List<Pair<String, String>> keys2Vals2;
      if (rPs.getValues().length == 0) {
        keys2 = iP.addFilterKeys(rPs.getKeys(), keys2);
      } else if (keys2.length == 0) {
        keys2 = rPs.getKeys();
        values2 = rPs.getValues();
      } else {
        keys2Vals2 = iP.addFilterKeysVals(rPs.getKeys(), rPs.getValues(), keys2, values2);
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
    Integer[] keysInt1 = new Integer[rPs.getKeys().length];
    Integer[] valuesInt1 = new Integer[rPs.getValues().length];
    Integer[] keysInt2 = new Integer[keys2.length];
    Integer[] valuesInt2 = new Integer[values2.length];
    if (!rPs.isPost())
      requestURL = RequestInterceptor.requestUrl;
    for (int i = 0; i < rPs.getKeys().length; i++) {
      keysInt1[i] = tt.getOSHDBTagKeyOf(rPs.getKeys()[i]).toInt();
      if (rPs.getValues() != null && i < rPs.getValues().length)
        valuesInt1[i] = tt.getOSHDBTagOf(rPs.getKeys()[i], rPs.getValues()[i]).getValue();
    }
    for (int i = 0; i < keys2.length; i++) {
      keysInt2[i] = tt.getOSHDBTagKeyOf(keys2[i]).toInt();
      if (values2 != null && i < values2.length)
        valuesInt2[i] = tt.getOSHDBTagOf(keys2[i], values2[i]).getValue();
    }
    EnumSet<OSMType> osmTypes1 = iP.getOsmTypes();
    EnumSet<OSMType> osmTypes2 = iP.extractOSMTypes(types2);
    EnumSet<OSMType> osmTypes = osmTypes1.clone();
    osmTypes.addAll(osmTypes2);
    String[] osmTypesString =
        osmTypes.stream().map(OSMType::toString).map(String::toLowerCase).toArray(String[]::new);
    if (!iP.compareKeysValues(rPs.getKeys(), keys2, rPs.getValues(), values2)) {
      mapRed = iP.processParameters(mapRed,
          new RequestParameters(rPs.isPost(), rPs.isSnapshot(), rPs.isDensity(), rPs.getBboxes(),
              rPs.getBcircles(), rPs.getBpolys(), osmTypesString, new String[] {}, new String[] {},
              rPs.getUserids(), rPs.getTime(), rPs.getShowMetadata()));
      mapRed = mapRed.where(entity -> {
        if (!exeUtils.entityMatches(entity, osmTypes1, keysInt1, valuesInt1))
          return exeUtils.entityMatches(entity, osmTypes2, keysInt2, valuesInt2);
        return true;
      });
    } else {
      mapRed = iP.processParameters(mapRed, rPs);
      mapRed = mapRed.osmTypes(osmTypes);
    }
    preResult = mapRed.aggregateByTimestamp().aggregateBy(f -> {
      OSMEntity entity = f.getEntity();
      boolean matches1 = exeUtils.entityMatches(entity, osmTypes1, keysInt1, valuesInt1);
      boolean matches2 = exeUtils.entityMatches(entity, osmTypes2, keysInt2, valuesInt2);
      if (matches1 && matches2)
        return MatchType.MATCHESBOTH;
      else if (matches1)
        return MatchType.MATCHES1;
      else if (matches2)
        return MatchType.MATCHES2;
      else
        assert false : "MatchType matches none.";
      // this should never be reached
      return null;
    }).zerofillIndices(
        Arrays.asList(MatchType.MATCHESBOTH, MatchType.MATCHES1, MatchType.MATCHES2));
    result = exeUtils.computeResult(requestResource, preResult);
    int resultSize = result.size();
    Double[] value1 = new Double[resultSize / 3];
    Double[] value2 = new Double[resultSize / 3];
    String[] timeArray = new String[resultSize / 3];
    int value1Count = 0;
    int value2Count = 0;
    int matchesBothCount = 0;
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

    return exeUtils.createRatioShareResponse(isShare, timeArray, value1, value2, ratioDf, iP,
        startTime, requestResource, requestURL, new Attribution(url, text));
  }

  /**
   * Performs a count|length|perimeter|area-ratio calculation grouped by the boundary.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCountRatio(String, String, String, String[], String[], String[], String[], String[], String, String[], String[], String[])
   * getCountRatio} method.
   * 
   * @param rPs <code>RequestParameters</code> object, which holds those parameters that are used in
   *        every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.RatioGroupByBoundaryResponse
   *         RatioGroupByBoundaryResponse Content}
   */
  public static RatioShareResponse executeCountLengthPerimeterAreaRatioGroupByBoundary(
      RequestResource requestResource, RequestParameters rPs, String[] types2, String[] keys2,
      String[] values2, boolean isShare) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    MapAggregator<OSHDBTimestampAndIndex<Pair<Integer, MatchType>>, Geometry> preResult = null;
    SortedMap<OSHDBTimestampAndIndex<Pair<Integer, MatchType>>, ? extends Number> result = null;
    SortedMap<Pair<Integer, MatchType>, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String requestURL = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    DecimalFormat ratioDf = exeUtils.defineDecimalFormat("#.######");
    TagTranslator tt = DbConnData.tagTranslator;
    rPs = iP.fillWithEmptyIfNull(rPs);
    iP.processParameters(mapRed, rPs);
    if (iP.getBoundaryType() == BoundaryType.NOBOUNDARY)
      throw new BadRequestException(
          "You need to give at least one boundary parameter if you want to use /groupBy/boundary.");
    GeometryBuilder geomBuilder = iP.getGeomBuilder();
    iP.checkKeysValues(keys2, values2);
    values2 = iP.createEmptyArrayIfNull(values2);
    keys2 = iP.createEmptyArrayIfNull(keys2);
    if (isShare) {
      List<Pair<String, String>> keys2Vals2;
      if (rPs.getValues().length == 0) {
        keys2 = iP.addFilterKeys(rPs.getKeys(), keys2);
      } else if (keys2.length == 0) {
        keys2 = rPs.getKeys();
        values2 = rPs.getValues();
      } else {
        keys2Vals2 = iP.addFilterKeysVals(rPs.getKeys(), rPs.getValues(), keys2, values2);
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
    Integer[] keysInt1 = new Integer[rPs.getKeys().length];
    Integer[] valuesInt1 = new Integer[rPs.getValues().length];
    Integer[] keysInt2 = new Integer[keys2.length];
    Integer[] valuesInt2 = new Integer[values2.length];
    if (!rPs.isPost())
      requestURL = RequestInterceptor.requestUrl;
    for (int i = 0; i < rPs.getKeys().length; i++) {
      keysInt1[i] = tt.getOSHDBTagKeyOf(rPs.getKeys()[i]).toInt();
      if (rPs.getValues() != null && i < rPs.getValues().length)
        valuesInt1[i] = tt.getOSHDBTagOf(rPs.getKeys()[i], rPs.getValues()[i]).getValue();
    }
    for (int i = 0; i < keys2.length; i++) {
      keysInt2[i] = tt.getOSHDBTagKeyOf(keys2[i]).toInt();
      if (values2 != null && i < values2.length)
        valuesInt2[i] = tt.getOSHDBTagOf(keys2[i], values2[i]).getValue();
    }
    EnumSet<OSMType> osmTypes1 = iP.getOsmTypes();
    EnumSet<OSMType> osmTypes2 = iP.extractOSMTypes(types2);
    EnumSet<OSMType> osmTypes = osmTypes1.clone();
    osmTypes.addAll(osmTypes2);
    String[] osmTypesString =
        osmTypes.stream().map(OSMType::toString).map(String::toLowerCase).toArray(String[]::new);
    if (!iP.compareKeysValues(rPs.getKeys(), keys2, rPs.getValues(), values2)) {
      mapRed = iP.processParameters(mapRed,
          new RequestParameters(rPs.isPost(), rPs.isSnapshot(), rPs.isDensity(), rPs.getBboxes(),
              rPs.getBcircles(), rPs.getBpolys(), osmTypesString, new String[] {}, new String[] {},
              rPs.getUserids(), rPs.getTime(), rPs.getShowMetadata()));
      mapRed = mapRed.where(entity -> {
        boolean matches1 = exeUtils.entityMatches(entity, osmTypes1, keysInt1, valuesInt1);
        boolean matches2 = exeUtils.entityMatches(entity, osmTypes2, keysInt2, valuesInt2);
        return matches1 || matches2;
      });
    } else {
      mapRed = iP.processParameters(mapRed, rPs);
      mapRed = mapRed.osmTypes(osmTypes);
    }
    Utils utils = iP.getUtils();
    ArrayList<Geometry> geoms = geomBuilder.getGeometry(iP.getBoundaryType());
    List<Pair<Integer, MatchType>> zeroFill = new LinkedList<>();
    for (int j = 0; j < geoms.size(); j++) {
      zeroFill.add(new ImmutablePair<>(j, MatchType.MATCHESBOTH));
      zeroFill.add(new ImmutablePair<>(j, MatchType.MATCHES1));
      zeroFill.add(new ImmutablePair<>(j, MatchType.MATCHES2));
    }
    preResult = mapRed.aggregateByTimestamp().flatMap(f -> {
      List<Pair<Pair<Integer, OSMEntity>, Geometry>> res = new LinkedList<>();
      Geometry entityGeom = f.getGeometry();
      if (requestResource.equals(RequestResource.PERIMETER)) {
        entityGeom = entityGeom.getBoundary();
      }
      for (int i = 0; i < geoms.size(); i++) {
        if (entityGeom.intersects(geoms.get(i))) {
          if (entityGeom.within(geoms.get(i)))
            res.add(new ImmutablePair<>(new ImmutablePair<>(i, f.getEntity()), entityGeom));
          else
            res.add(new ImmutablePair<>(new ImmutablePair<>(i, f.getEntity()),
                Geo.clip(entityGeom, (Geometry & Polygonal) geoms.get(i))));
        }
      }
      return res;
    }).aggregateBy(f -> {
      OSMEntity entity = f.getLeft().getRight();
      boolean matches1 = exeUtils.entityMatches(entity, osmTypes1, keysInt1, valuesInt1);
      boolean matches2 = exeUtils.entityMatches(entity, osmTypes2, keysInt2, valuesInt2);
      if (matches1 && matches2)
        return new ImmutablePair<>(f.getLeft().getLeft(), MatchType.MATCHESBOTH);
      else if (matches1)
        return new ImmutablePair<>(f.getLeft().getLeft(), MatchType.MATCHES1);
      else if (matches2)
        return new ImmutablePair<>(f.getLeft().getLeft(), MatchType.MATCHES2);
      else
        assert false : "MatchType matches none.";
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
    }
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
      if (!timeArrayFilled)
        timeArray = new String[entry.getValue().entrySet().size()];
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
          if (!timeArrayFilled)
            timeArray[matchesBothCount] = innerEntry.getKey().toString();
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
    return exeUtils.createRatioShareGroupByBoundaryResponse(isShare, groupByResult.size(),
        boundaryIds, timeArray, value1Arrays, value2Arrays, ratioDf, iP, startTime, requestResource,
        requestURL, new Attribution(url, text));
  }

}
