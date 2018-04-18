package org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.executor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.Application;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.GeometryBuilder;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.InputProcessor;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.Utils;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.interceptor.RequestInterceptor;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Attribution;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByBoundaryResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByKeyResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByTagResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByTypeResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByUserResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.RatioGroupByBoundaryResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.RatioResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.ShareGroupByBoundaryResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.ShareResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.metadata.Metadata;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.result.GroupByResult;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.result.RatioGroupByResult;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.result.RatioResult;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.result.Result;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.result.ShareGroupByResult;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.result.ShareResult;
import org.heigit.bigspatialdata.oshdb.api.generic.OSHDBTimestampAndIndex;
import org.heigit.bigspatialdata.oshdb.api.generic.function.SerializableFunction;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregatorByTimestampAndIndex;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.object.OSMEntitySnapshot;
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

  private static final String url = Application.getAttributionUrl();
  private static final String text = Application.getAttributionShort();

  /**
   * Performs a count calculation.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   * @throws UnsupportedOperationException by
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer#aggregateByTimestamp()
   *         aggregateByTimestamp()}
   * @throws BadRequestException by
   *         {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.InputProcessor#processParameters(boolean, String, String, String, String[], String[], String[], String[], String[], String)
   *         processParameters()}
   * @throws Exception by
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#count() count()}
   */
  public static DefaultAggregationResponse executeCount(boolean isPost, String bboxes,
      String bcircles, String bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time, String showMetadata)
      throws UnsupportedOperationException, BadRequestException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Integer> result;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String requestURL = null;
    if (!isPost)
      requestURL = RequestInterceptor.requestUrl;
    mapRed = iP.processParameters(mapRed, true, isPost, bboxes, bcircles, bpolys, types, keys,
        values, userids, time, showMetadata);
    // db result
    result = mapRed.aggregateByTimestamp().count();
    Result[] resultSet = new Result[result.size()];
    int count = 0;

    for (Entry<OSHDBTimestamp, Integer> entry : result.entrySet()) {
      resultSet[count] = new Result(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
          entry.getValue().intValue());
      count++;
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      metadata = new Metadata(duration, "Total number of items.", requestURL);
    }
    DefaultAggregationResponse response =
        new DefaultAggregationResponse(new Attribution(url, text), Application.apiVersion, metadata, resultSet);

    return response;
  }

  /**
   * Performs a count or density calculation grouped by the type.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param isDensity <code>Boolean</code> parameter saying if this method was called from a density
   *        resource (true) or not (false).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByTypeResponse
   *         GroupByTypeResponseContent}
   */
  public static GroupByTypeResponse executeCountGroupByType(boolean isPost, boolean isDensity,
      String bboxes, String bcircles, String bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time, String showMetadata)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestampAndIndex<OSMType>, Integer> result;
    SortedMap<OSMType, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    ExecutionUtils exeUtils = new ExecutionUtils();
    String requestURL = null;
    if (!isPost)
      requestURL = RequestInterceptor.requestUrl;
    mapRed = iP.processParameters(mapRed, true, isPost, bboxes, bcircles, bpolys, types, keys,
        values, userids, time, showMetadata);
    // db result
    result = mapRed.aggregateByTimestamp()
        .aggregateBy((SerializableFunction<OSMEntitySnapshot, OSMType>) f -> {
          return f.getEntity().getType();
        }).zerofillIndices(iP.getOsmTypes()).count();
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    DecimalFormat densityDf = exeUtils.defineDecimalFormat("#.##");
    GeometryBuilder geomBuilder = iP.getGeomBuilder();
    Geometry geom = exeUtils.getGeometry(iP.getBoundaryType(), geomBuilder);
    int count = 0;
    int innerCount = 0;
    // iterate over the entry objects aggregated by user
    for (Entry<OSMType, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult.entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      innerCount = 0;
      // iterate over the timestamp-value pairs
      for (Entry<OSHDBTimestamp, Integer> innerEntry : entry.getValue().entrySet()) {
        if (isDensity)
          results[innerCount] =
              new Result(TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
                  Double.parseDouble(densityDf
                      .format((innerEntry.getValue().intValue() / (Geo.areaOf(geom) / 1000000)))));
        else
          results[innerCount] =
              new Result(TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
                  innerEntry.getValue().intValue());
        innerCount++;
      }
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      if (isDensity)
        metadata = new Metadata(duration,
            "Density of selected items (number of items per square kilometer) aggregated on the type.",
            requestURL);
      else
        metadata =
            new Metadata(duration, "Total number of items aggregated on the type.", requestURL);
    }
    GroupByTypeResponse response =
        new GroupByTypeResponse(new Attribution(url, text), Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count calculation grouped by the boundary.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByBoundaryResponse
   *         GroupByBoundaryResponseContent}
   */
  public static GroupByBoundaryResponse executeCountGroupByBoundary(boolean isPost, String bboxes,
      String bcircles, String bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time, String showMetadata)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestampAndIndex<Integer>, Integer> result = null;
    SortedMap<Integer, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    ExecutionUtils exeUtils = new ExecutionUtils();
    String requestURL = null;
    if (!isPost)
      requestURL = RequestInterceptor.requestUrl;
    mapRed = iP.processParameters(mapRed, true, isPost, bboxes, bcircles, bpolys, types, keys,
        values, userids, time, showMetadata);
    result = exeUtils.computeCountGBBResult(iP.getBoundaryType(), mapRed, iP.getGeomBuilder());
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    Utils utils = iP.getUtils();
    String[] boundaryIds = utils.getBoundaryIds();
    int count = 0;
    int innerCount = 0;
    // iterate over the entry objects aggregated by the boundary
    for (Entry<Integer, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult.entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      innerCount = 0;
      groupByName = boundaryIds[count];
      for (Entry<OSHDBTimestamp, Integer> innerEntry : entry.getValue().entrySet()) {
        results[innerCount] =
            new Result(TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
                innerEntry.getValue().intValue());
        innerCount++;
      }
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    Metadata metadata = null;
    if (iP.getShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, "Total number of items aggregated on the boundary object.",
          requestURL);
    }
    GroupByBoundaryResponse response =
        new GroupByBoundaryResponse(new Attribution(url, text), Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count calculation grouped by the key.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCountGroupByKey(String, String, String, String[], String[], String[], String[], String[], String, String[])
   * getCountGroupByKey} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByKeyResponse
   *         GroupByKeyResponseContent}
   */
  public static GroupByKeyResponse executeCountGroupByKey(boolean isPost, String bboxes,
      String bcircles, String bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time, String showMetadata, String[] groupByKeys)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    if (groupByKeys == null || groupByKeys.length == 0) {
      throw new BadRequestException(
          "You need to give one groupByKey parameters, if you want to use groupBy/key");
    }
    SortedMap<OSHDBTimestampAndIndex<Integer>, Integer> result;
    SortedMap<Integer, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String requestURL = null;
    if (!isPost)
      requestURL = RequestInterceptor.requestUrl;
    TagTranslator tt = Application.getTagTranslator();
    Integer[] keysInt = new Integer[groupByKeys.length];
    mapRed = iP.processParameters(mapRed, true, isPost, bboxes, bcircles, bpolys, types, keys,
        values, userids, time, showMetadata);
    for (int i = 0; i < groupByKeys.length; i++) {
      keysInt[i] = tt.getOSHDBTagKeyOf(groupByKeys[i]).toInt();
    }
    // group by key logic
    result = mapRed.flatMap(f -> {
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
        .map(Pair::getValue).count();
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    int count = 0;
    int innerCount = 0;
    // iterate over the entry objects aggregated by keys
    for (Entry<Integer, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult.entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      innerCount = 0;
      // check for non-remainder objects and not existing keys
      if (entry.getKey() != -1) {
        groupByName = tt.getOSMTagKeyOf(entry.getKey().intValue()).toString();
      } else {
        groupByName = "remainder";
      }
      // iterate over the timestamp-value pairs
      for (Entry<OSHDBTimestamp, Integer> innerEntry : entry.getValue().entrySet()) {
        results[innerCount] =
            new Result(TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
                innerEntry.getValue().intValue());
        innerCount++;
      }
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      metadata = new Metadata(duration, "Total number of items aggregated on the key.", requestURL);
    }
    GroupByKeyResponse response =
        new GroupByKeyResponse(new Attribution(url, text), Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count or density calculation grouped by the tag.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param isDensity <code>Boolean</code> parameter saying if this method was called from a density
   *        resource (true) or not (false).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByTagResponse
   *         GroupByTagResponseContent}
   */
  public static GroupByTagResponse executeCountGroupByTag(boolean isPost, boolean isDensity,
      String bboxes, String bcircles, String bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time, String showMetadata, String[] groupByKey,
      String[] groupByValues) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    if (groupByKey.length != 1)
      throw new BadRequestException("There has to be one groupByKey value given.");
    if (groupByValues == null)
      groupByValues = new String[0];
    SortedMap<OSHDBTimestampAndIndex<Pair<Integer, Integer>>, Integer> result;
    SortedMap<Pair<Integer, Integer>, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    ExecutionUtils exeUtils = new ExecutionUtils();
    String requestURL = null;
    if (!isPost)
      requestURL = RequestInterceptor.requestUrl;
    TagTranslator tt = Application.getTagTranslator();
    Integer[] valuesInt = new Integer[groupByValues.length];
    ArrayList<Pair<Integer, Integer>> zeroFill = new ArrayList<Pair<Integer, Integer>>();
    mapRed = iP.processParameters(mapRed, true, isPost, bboxes, bcircles, bpolys, types, keys,
        values, userids, time, showMetadata);
    int keysInt = tt.getOSHDBTagKeyOf(groupByKey[0]).toInt();
    if (groupByValues.length != 0) {
      for (int j = 0; j < groupByValues.length; j++) {
        valuesInt[j] = tt.getOSHDBTagOf(groupByKey[0], groupByValues[j]).getValue();
        zeroFill.add(new ImmutablePair<Integer, Integer>(keysInt, valuesInt[j]));
      }
    }
    // group by tag logic
    result = mapRed.map(f -> {
      int[] tags = f.getEntity().getRawTags();
      for (int i = 0; i < tags.length; i += 2) {
        int tagKeyId = tags[i];
        int tagValueId = tags[i + 1];
        if (tagKeyId == keysInt) {
          if (valuesInt.length == 0) {
            return new ImmutablePair<>(new ImmutablePair<Integer, Integer>(tagKeyId, tagValueId),
                f);
          }
          for (int value : valuesInt) {
            if (tagValueId == value)
              return new ImmutablePair<>(new ImmutablePair<Integer, Integer>(tagKeyId, tagValueId),
                  f);
          }
        }
      }
      return new ImmutablePair<>(new ImmutablePair<Integer, Integer>(-1, -1), f);
    }).aggregateByTimestamp().aggregateBy(Pair::getKey).zerofillIndices(zeroFill)
        .map(Pair::getValue).count();
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    int count = 0;
    DecimalFormat densityDf = exeUtils.defineDecimalFormat("#.##");
    GeometryBuilder geomBuilder = iP.getGeomBuilder();
    Geometry geom = exeUtils.getGeometry(iP.getBoundaryType(), geomBuilder);
    // iterate over the entry objects aggregated by tags
    for (Entry<Pair<Integer, Integer>, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult
        .entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      int innerCount = 0;
      // check for non-remainder objects (which do have the defined key and value)
      if (entry.getKey().getKey() != -1 && entry.getKey().getValue() != -1)
        groupByName = tt.getOSMTagOf(keysInt, entry.getKey().getValue()).toString();
      else
        groupByName = "remainder";
      // iterate over the timestamp-value pairs
      for (Entry<OSHDBTimestamp, Integer> innerEntry : entry.getValue().entrySet()) {
        if (isDensity)
          results[innerCount] =
              new Result(TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
                  Double.parseDouble(densityDf
                      .format((innerEntry.getValue().intValue() / (Geo.areaOf(geom) / 1000000)))));
        else
          results[innerCount] =
              new Result(TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
                  innerEntry.getValue().intValue());
        innerCount++;
      }
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      if (isDensity)
        metadata = new Metadata(duration,
            "Density of selected items (number of items per square kilometer) aggregated on the tag.",
            requestURL);
      else
        metadata =
            new Metadata(duration, "Total number of items aggregated on the tag.", requestURL);
    }
    GroupByTagResponse response =
        new GroupByTagResponse(new Attribution(url, text), Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count calculation grouped by the user.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByUserResponse
   *         GroupByUserResponseContent}
   */
  public static GroupByUserResponse executeCountGroupByUser(boolean isPost, String bboxes,
      String bcircles, String bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time, String showMetadata)
      throws UnsupportedOperationException, Exception {
    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestampAndIndex<Integer>, Integer> result;
    SortedMap<Integer, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String requestURL = null;
    ArrayList<Integer> useridsInt = new ArrayList<Integer>();
    if (!isPost)
      requestURL = RequestInterceptor.requestUrl;
    mapRed = iP.processParameters(mapRed, true, isPost, bboxes, bcircles, bpolys, types, keys,
        values, userids, time, showMetadata);
    if (userids != null)
      for (String user : userids)
        // converting userids to int for usage in zerofill
        useridsInt.add(Integer.parseInt(user));
    result = mapRed.aggregateByTimestamp()
        .aggregateBy((SerializableFunction<OSMEntitySnapshot, Integer>) f -> {
          return f.getEntity().getUserId();
        }).zerofillIndices(useridsInt).count();
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    int count = 0;
    int innerCount = 0;
    // iterate over the entry objects aggregated by user
    for (Entry<Integer, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult.entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      innerCount = 0;
      // iterate over the timestamp-value pairs
      for (Entry<OSHDBTimestamp, Integer> innerEntry : entry.getValue().entrySet()) {
        results[innerCount] =
            new Result(TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
                innerEntry.getValue().intValue());
        innerCount++;
      }
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      metadata =
          new Metadata(duration, "Total number of items aggregated on the userids.", requestURL);
    }
    GroupByUserResponse response =
        new GroupByUserResponse(new Attribution(url, text), Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count-share calculation.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCountShare(String, String, String, String[], String[], String[], String[], String[], String, String[], String[])
   * getCountShare} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   */
  public static ShareResponse executeCountShare(boolean isPost, String bboxes, String bcircles,
      String bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata, String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    values2 = exeUtils.shareParamEvaluation(keys2, values2);
    SortedMap<OSHDBTimestampAndIndex<Boolean>, Integer> result;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String requestURL = null;
    TagTranslator tt = Application.getTagTranslator();
    Integer[] keysInt2 = new Integer[keys2.length];
    Integer[] valuesInt2 = new Integer[values2.length];
    if (!isPost)
      requestURL = RequestInterceptor.requestUrl;
    for (int i = 0; i < keys2.length; i++) {
      keysInt2[i] = tt.getOSHDBTagKeyOf(keys2[i]).toInt();
      if (values2 != null && i < values2.length) {
        valuesInt2[i] = tt.getOSHDBTagOf(keys2[i], values2[i]).getValue();
      }
    }
    mapRed = iP.processParameters(mapRed, true, isPost, bboxes, bcircles, bpolys, types, keys,
        values, userids, time, showMetadata);
    result = mapRed.aggregateByTimestamp().aggregateBy(f -> {
      // result aggregated on true (if obj contains all tags) and false (if not all are contained)
      boolean hasTags = false;
      for (int i = 0; i < keysInt2.length; i++) {
        if (f.getEntity().hasTagKey(keysInt2[i])) {
          if (i >= valuesInt2.length) {
            // if more keys2 than values2 are given
            hasTags = true;
            continue;
          }
          if (f.getEntity().hasTagValue(keysInt2[i], valuesInt2[i])) {
            hasTags = true;
          } else {
            hasTags = false;
            break;
          }
        } else {
          hasTags = false;
          break;
        }
      }
      return hasTags;
    }).zerofillIndices(Arrays.asList(true, false)).count();
    Integer[] whole = new Integer[result.size()];
    Integer[] part = new Integer[result.size()];
    String[] timeArray = new String[result.size()];
    // needed time array in case no key can be found
    String[] noPartTimeArray = new String[result.size()];
    int partCount = 0;
    int wholeCount = 0;
    int timeCount = 0;
    // fill whole and part arrays with -1 values to indicate "no value"
    for (int i = 0; i < result.size(); i++) {
      whole[i] = -1;
      part[i] = -1;
    }
    // time and value extraction
    for (Entry<OSHDBTimestampAndIndex<Boolean>, Integer> entry : result.entrySet()) {
      // this time array counts for each entry in the entrySet
      noPartTimeArray[timeCount] = entry.getKey().getTimeIndex().toString();
      if (entry.getKey().getOtherIndex()) {
        // if true - set timestamp and set/increase part and/or whole
        timeArray[partCount] =
            TimestampFormatter.getInstance().isoDateTime(entry.getKey().getTimeIndex());
        part[partCount] = entry.getValue();
        if (whole[partCount] == null || whole[partCount] == -1)
          whole[partCount] = entry.getValue();
        else
          whole[partCount] = whole[partCount] + entry.getValue();
        partCount++;
      } else {
        // else - set/increase only whole
        if (whole[wholeCount] == null || whole[wholeCount] == -1)
          whole[wholeCount] = entry.getValue();
        else
          whole[wholeCount] = whole[wholeCount] + entry.getValue();
        wholeCount++;
      }
      timeCount++;
    }
    // remove the possible null values in the array
    timeArray = Arrays.stream(timeArray).filter(Objects::nonNull).toArray(String[]::new);
    // overwrite time array in case the given key for part is not existent in the whole for no
    // timestamp
    if (timeArray.length < 1) {
      timeArray = noPartTimeArray;
    }
    ShareResult[] resultSet = new ShareResult[timeArray.length];
    for (int i = 0; i < timeArray.length; i++) {
      if (whole[i] == -1)
        whole[i] = 0;
      if (part[i] == -1)
        part[i] = 0;
      resultSet[i] = new ShareResult(timeArray[i], whole[i], part[i]);
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      metadata = new Metadata(duration,
          "Share of items satisfying keys2 and values2 within items selected by types, keys, values.",
          requestURL);
    }
    ShareResponse response =
        new ShareResponse(new Attribution(url, text), Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count-share calculation grouped by the boundary.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCountShare(String, String, String, String[], String[], String[], String[], String[], String, String[], String[])
   * getCountShare} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   */
  public static ShareGroupByBoundaryResponse executeCountShareGroupByBoundary(boolean isPost,
      String bboxes, String bcircles, String bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time, String showMetadata, String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    values2 = exeUtils.shareParamEvaluation(keys2, values2);
    SortedMap<OSHDBTimestampAndIndex<Pair<Integer, Boolean>>, Integer> result = null;
    SortedMap<Pair<Integer, Boolean>, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String requestURL = null;
    TagTranslator tt = Application.getTagTranslator();
    Integer[] keysInt2 = new Integer[keys2.length];
    Integer[] valuesInt2 = new Integer[values2.length];
    if (!isPost)
      requestURL = RequestInterceptor.requestUrl;
    for (int i = 0; i < keys2.length; i++) {
      keysInt2[i] = tt.getOSHDBTagKeyOf(keys2[i]).toInt();
      if (values2 != null && i < values2.length)
        valuesInt2[i] = tt.getOSHDBTagOf(keys2[i], values2[i]).getValue();
    }
    mapRed = iP.processParameters(mapRed, true, isPost, bboxes, bcircles, bpolys, types, keys,
        values, userids, time, showMetadata);
    result = exeUtils.computeCountShareGBBResult(iP.getBoundaryType(), mapRed, keysInt2, valuesInt2,
        iP.getGeomBuilder());
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    ShareGroupByResult[] groupByResultSet = new ShareGroupByResult[groupByResult.size() / 2];
    String groupByName = "";
    Utils utils = iP.getUtils();
    String[] boundaryIds = utils.getBoundaryIds();
    Integer[] whole = null;
    Integer[] part = null;
    String[] timeArray = null;
    int count = 1;
    int gBNCount = 0;
    for (Entry<Pair<Integer, Boolean>, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult
        .entrySet()) {
      // on boundary param aggregated values (2x the same param)
      if (count == 1)
        timeArray = new String[entry.getValue().entrySet().size()];
      if (entry.getKey().getRight()) {
        // on true aggregated values
        part = new Integer[entry.getValue().entrySet().size()];
        int partCount = 0;
        for (Entry<OSHDBTimestamp, Integer> innerEntry : entry.getValue().entrySet()) {
          part[partCount] = innerEntry.getValue();
          partCount++;
        }
      } else {
        // on false aggregated values
        whole = new Integer[entry.getValue().entrySet().size()];
        int wholeCount = 0;
        for (Entry<OSHDBTimestamp, Integer> innerEntry : entry.getValue().entrySet()) {
          whole[wholeCount] = innerEntry.getValue();
          if (count == 1)
            timeArray[wholeCount] = innerEntry.getKey().toString();
          wholeCount++;
        }
      }
      if (count % 2 == 0) {
        // is only executed every second run
        groupByName = boundaryIds[gBNCount];
        ShareResult[] resultSet = new ShareResult[timeArray.length];
        for (int i = 0; i < timeArray.length; i++) {
          whole[i] = whole[i] + part[i];
          resultSet[i] = new ShareResult(timeArray[i], whole[i], part[i]);
        }
        groupByResultSet[gBNCount] = new ShareGroupByResult(groupByName, resultSet);
        gBNCount++;
      }
      count++;
    }
    Metadata metadata = null;
    if (iP.getShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration,
          "Share of items satisfying keys2 and values2 within items selected by types, keys, values, grouped on the boundary parameter.",
          requestURL);
    }
    ShareGroupByBoundaryResponse response = new ShareGroupByBoundaryResponse(
        new Attribution(url, text), Application.apiVersion, metadata, groupByResultSet);
    return response;
  }

  /**
   * Performs a count-density calculation.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param isPost <code>Boolean</code> defining if this method is called from a POST (true) or a
   *        GET (false) request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   */
  public static DefaultAggregationResponse executeCountDensity(boolean isPost, String bboxes,
      String bcircles, String bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time, String showMetadata)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Integer> result;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    ExecutionUtils exeUtils = new ExecutionUtils();
    String requestURL = null;
    if (!isPost)
      requestURL = RequestInterceptor.requestUrl;
    mapRed = iP.processParameters(mapRed, true, isPost, bboxes, bcircles, bpolys, types, keys,
        values, userids, time, showMetadata);
    GeometryBuilder geomBuilder = iP.getGeomBuilder();
    result = mapRed.aggregateByTimestamp().count();
    Geometry geom = exeUtils.getGeometry(iP.getBoundaryType(), geomBuilder);
    int count = 0;
    Result[] resultSet = new Result[result.size()];
    DecimalFormat densityDf = exeUtils.defineDecimalFormat("#.##");
    for (Entry<OSHDBTimestamp, Integer> entry : result.entrySet()) {
      resultSet[count] = new Result(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
          Double.parseDouble(
              densityDf.format((entry.getValue().intValue() / (Geo.areaOf(geom) / 1000000)))));
      count++;
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      metadata = new Metadata(duration,
          "Density of selected items (number of items per square kilometer).", requestURL);
    }
    DefaultAggregationResponse response =
        new DefaultAggregationResponse(new Attribution(url, text), Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count-ratio calculation.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCountRatio(String, String, String, String[], String[], String[], String[], String[], String, String[], String[], String[])
   * getCountRatio} method.
   * 
   * @param isPost <code>Boolean</code> defining if this method is called from a POST (true) or a
   *        GET (false) request.
   * @param types2 <code>String</code> array having the same format as types.
   * @param keys2 <code>String</code> array having the same format as keys.
   * @param values2 <code>String</code> array having the same format as values.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   */
  public static RatioResponse executeCountRatio(boolean isPost, String bboxes, String bcircles,
      String bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata, String[] types2, String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Integer> result1;
    SortedMap<OSHDBTimestamp, Integer> result2;
    MapReducer<OSMEntitySnapshot> mapRed1 = null;
    MapReducer<OSMEntitySnapshot> mapRed2 = null;
    InputProcessor iP = new InputProcessor();
    ExecutionUtils exeUtils = new ExecutionUtils();
    String requestURL = null;
    if (!isPost)
      requestURL = RequestInterceptor.requestUrl;
    mapRed1 = iP.processParameters(mapRed1, true, isPost, bboxes, bcircles, bpolys, types, keys,
        values, userids, time, showMetadata);
    result1 = mapRed1.aggregateByTimestamp().count();
    mapRed2 = iP.processParameters(mapRed2, true, isPost, bboxes, bcircles, bpolys, types2, keys2,
        values2, userids, time, showMetadata);
    result2 = mapRed2.aggregateByTimestamp().count();
    Result[] resultSet1 = new Result[result1.size()];
    int count = 0;
    for (Entry<OSHDBTimestamp, Integer> entry : result1.entrySet()) {
      resultSet1[count] = new Result(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
          entry.getValue().intValue());
      count++;
    }
    RatioResult[] resultSet = new RatioResult[result1.size()];
    DecimalFormat ratioDF = exeUtils.defineDecimalFormat("#.######");
    count = 0;
    for (Entry<OSHDBTimestamp, Integer> entry : result2.entrySet()) {
      String date = resultSet1[count].getTimestamp();
      double ratio = (entry.getValue().doubleValue() / resultSet1[count].getValue());
      // in case ratio has the value "NaN", "Infinity", etc.
      try {
        ratio = Double.parseDouble(ratioDF.format(ratio));
      } catch (Exception e) {
        // do nothing --> just return ratio without rounding (trimming)
      }
      resultSet[count] =
          new RatioResult(date, resultSet1[count].getValue(), entry.getValue().intValue(), ratio);
      count++;
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      metadata = new Metadata(duration,
          "Total number of items satisfying types2, keys2, values2 parameters (= value2 output) "
              + "within items selected by types, keys, values parameters (= value output) and ratio of value2:value.",
          requestURL);
    }
    RatioResponse response =
        new RatioResponse(new Attribution(url, text), Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count-ratio calculation grouped by the boundary.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param isPost <code>Boolean</code> defining if this method is called from a POST (true) or a
   *        GET (false) request.
   * @param types2 <code>String</code> array having the same format as types.
   * @param keys2 <code>String</code> array having the same format as keys.
   * @param values2 <code>String</code> array having the same format as values.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByBoundaryResponse
   *         GroupByBoundaryResponseContent}
   */
  public static RatioGroupByBoundaryResponse executeCountRatioGroupByBoundary(boolean isPost,
      String bboxes, String bcircles, String bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time, String showMetadata, String[] types2, String[] keys2,
      String[] values2) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestampAndIndex<Integer>, Integer> result1;
    SortedMap<OSHDBTimestampAndIndex<Integer>, Integer> result2;
    MapReducer<OSMEntitySnapshot> mapRed1 = null;
    MapReducer<OSMEntitySnapshot> mapRed2 = null;
    SortedMap<Integer, SortedMap<OSHDBTimestamp, Integer>> groupByResult1;
    SortedMap<Integer, SortedMap<OSHDBTimestamp, Integer>> groupByResult2;
    InputProcessor iP = new InputProcessor();
    ExecutionUtils exeUtils = new ExecutionUtils();
    String requestURL = null;
    if (!isPost)
      requestURL = RequestInterceptor.requestUrl;
    mapRed1 = iP.processParameters(mapRed1, true, isPost, bboxes, bcircles, bpolys, types, keys,
        values, userids, time, showMetadata);
    result1 = exeUtils.computeCountGBBResult(iP.getBoundaryType(), mapRed1, iP.getGeomBuilder());
    mapRed2 = iP.processParameters(mapRed2, true, isPost, bboxes, bcircles, bpolys, types2, keys2,
        values2, userids, time, showMetadata);
    result2 = exeUtils.computeCountGBBResult(iP.getBoundaryType(), mapRed2, iP.getGeomBuilder());

    groupByResult1 = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result1);
    groupByResult2 = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result2);
    GroupByResult[] resultSet = new GroupByResult[groupByResult1.size()];
    RatioGroupByResult[] ratioResultSet = new RatioGroupByResult[groupByResult1.size()];
    String groupByName = "";
    Utils utils = iP.getUtils();
    String[] boundaryIds = utils.getBoundaryIds();
    int count = 0;
    int innerCount = 0;
    // iterate over the entry objects of result1
    for (Entry<Integer, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult1.entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      innerCount = 0;
      groupByName = boundaryIds[count];
      for (Entry<OSHDBTimestamp, Integer> innerEntry : entry.getValue().entrySet()) {
        results[innerCount] =
            new Result(TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
                innerEntry.getValue().intValue());
        innerCount++;
      }
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    count = 0;
    innerCount = 0;
    DecimalFormat ratioDF = exeUtils.defineDecimalFormat("#.######");
    // iterate over the entry objects of result2
    for (Entry<Integer, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult2.entrySet()) {
      RatioResult[] ratioResults = new RatioResult[entry.getValue().entrySet().size()];
      innerCount = 0;
      groupByName = boundaryIds[count];
      for (Entry<OSHDBTimestamp, Integer> innerEntry : entry.getValue().entrySet()) {
        double value = resultSet[count].getResult()[innerCount].getValue();
        double value2 = innerEntry.getValue().doubleValue();
        double ratio = value2 / value;
        // in case ratio has the values "NaN", "Infinity", etc.
        try {
          ratio = Double.parseDouble(ratioDF.format(ratio));
        } catch (Exception e) {
          // do nothing --> just return ratio without rounding (trimming)
        }
        ratioResults[innerCount] =
            new RatioResult(TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
                value, value2, ratio);
        innerCount++;
      }
      ratioResultSet[count] = new RatioGroupByResult(groupByName, ratioResults);
      count++;
    }
    Metadata metadata = null;
    if (iP.getShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration,
          "Amount of items satisfying types2, keys2, values2 parameters (= value2 output) within items "
              + "selected by types, keys, values parameters (= value output) and ratio of value2:value grouped on the boundary objects.",
          requestURL);
    }
    RatioGroupByBoundaryResponse response = new RatioGroupByBoundaryResponse(
        new Attribution(url, text), Application.apiVersion, metadata, ratioResultSet);
    return response;
  }

  /**
   * Performs a length or area calculation.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (LENGTH, PERIMETER, AREA).
   * @param isPost <code>Boolean</code> defining if this method is called from a POST (true) or a
   *        GET (false) request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   */
  public static DefaultAggregationResponse executeLengthPerimeterArea(
      RequestResource requestResource, boolean isPost, boolean isDensity, String bboxes,
      String bcircles, String bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time, String showMetadata)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Number> result = null;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    ExecutionUtils exeUtils = new ExecutionUtils();
    String description = null;
    String requestURL = null;
    if (!isPost)
      requestURL = RequestInterceptor.requestUrl;
    mapRed = iP.processParameters(mapRed, true, isPost, bboxes, bcircles, bpolys, types, keys,
        values, userids, time, showMetadata);
    switch (requestResource) {
      case AREA:
        result = mapRed.aggregateByTimestamp()
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
              return Geo.areaOf(snapshot.getGeometry());
            });
        if (isDensity) {
          description =
              "Density of selected items (area of items in square meter per square kilometer).";
        } else {
          description = "Total area of polygons in square meter.";
        }
        break;
      case LENGTH:
        result = mapRed.aggregateByTimestamp()
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
              return Geo.lengthOf(snapshot.getGeometry());
            });
        if (isDensity) {
          description =
              "Density of selected items (length of items in meter per square kilometer).";
        } else {
          description = "Total length of lines in meter.";
        }
        break;
      case PERIMETER:
        result = mapRed.aggregateByTimestamp()
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
              if (snapshot.getGeometry() instanceof Polygonal)
                return Geo.lengthOf(snapshot.getGeometry().getBoundary());
              else
                return 0.0;
            });
        if (isDensity) {

          description =
              "Density of selected items (perimeter of items in meter per square kilometer).";
        } else {

          description = "Total perimeter of polygonal items in meter.";
        }
        break;
    }
    GeometryBuilder geomBuilder = iP.getGeomBuilder();
    Geometry geom = exeUtils.getGeometry(iP.getBoundaryType(), geomBuilder);
    int count = 0;
    Result[] resultSet = new Result[result.size()];
    DecimalFormat densityDf = exeUtils.defineDecimalFormat("#.##");
    for (Entry<OSHDBTimestamp, Number> entry : result.entrySet()) {
      if (isDensity)
        resultSet[count] = new Result(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
            Double.parseDouble(densityDf
                .format((entry.getValue().doubleValue() / (Geo.areaOf(geom) * 0.000001)))));
      else
        resultSet[count] = new Result(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
            Double.parseDouble(densityDf.format((entry.getValue().doubleValue()))));
      count++;
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      metadata = new Metadata(duration, description, requestURL);
    }
    DefaultAggregationResponse response =
        new DefaultAggregationResponse(new Attribution(url, text), Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a length, perimeter, or area calculation grouped by the boundary.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (LENGTH, PERIMETER, AREA).
   * @param isPost <code>Boolean</code> defining if this method is called from a POST (true) or a
   *        GET (false) request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByBoundaryResponse
   *         GroupByBoundaryResponseContent}
   */
  public static GroupByBoundaryResponse executeLengthPerimeterAreaGroupByBoundary(
      RequestResource requestResource, boolean isPost, String bboxes, String bcircles,
      String bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestampAndIndex<Integer>, Number> result = null;
    SortedMap<Integer, SortedMap<OSHDBTimestamp, Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    ExecutionUtils exeUtils = new ExecutionUtils();
    String description = null;
    String requestURL = null;
    if (!isPost)
      requestURL = RequestInterceptor.requestUrl;
    mapRed = iP.processParameters(mapRed, true, isPost, bboxes, bcircles, bpolys, types, keys,
        values, userids, time, showMetadata);
    switch (requestResource) {
      case LENGTH:
        result = exeUtils.computeLengthPerimeterAreaGBBResult(RequestResource.LENGTH,
            iP.getBoundaryType(), mapRed, iP.getGeomBuilder());
        description = "Total length of lines in meter aggregated on the boundary object.";
        break;
      case PERIMETER:
        result = exeUtils.computeLengthPerimeterAreaGBBResult(RequestResource.PERIMETER,
            iP.getBoundaryType(), mapRed, iP.getGeomBuilder());
        description =
            "Total perimeter of polygonal items in meter aggregated on the boundary object.";
        break;
      case AREA:
        result = exeUtils.computeLengthPerimeterAreaGBBResult(RequestResource.AREA,
            iP.getBoundaryType(), mapRed, iP.getGeomBuilder());
        description = "Total area of polygons in square meter aggregated on the boundary object.";
        break;
    }
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    Utils utils = iP.getUtils();
    String[] boundaryIds = utils.getBoundaryIds();
    DecimalFormat lengthPerimeterAreaDf = exeUtils.defineDecimalFormat("#.##");
    int count = 0;
    int innerCount = 0;
    // iterate over the entry objects aggregated by the boundary
    for (Entry<Integer, SortedMap<OSHDBTimestamp, Number>> entry : groupByResult.entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      innerCount = 0;
      groupByName = boundaryIds[count];
      for (Entry<OSHDBTimestamp, Number> innerEntry : entry.getValue().entrySet()) {
        results[innerCount] = new Result(
            TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
            Double.parseDouble(lengthPerimeterAreaDf.format(innerEntry.getValue().doubleValue())));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    Metadata metadata = null;
    if (iP.getShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, description, requestURL);
    }
    GroupByBoundaryResponse response =
        new GroupByBoundaryResponse(new Attribution(url, text), Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a length, perimeter, or area calculation grouped by the key.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCountGroupByKey(String, String, String, String[], String[], String[], String[], String[], String, String[])
   * groupByKey} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (LENGTH, PERIMETER, AREA).
   * @param isPost <code>Boolean</code> defining if this method is called from a POST (true) or a
   *        GET (false) request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByKeyResponse
   *         GroupByKeyResponseContent}
   */
  public static GroupByKeyResponse executeLengthPerimeterAreaGroupByKey(
      RequestResource requestResource, boolean isPost, String bboxes, String bcircles,
      String bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata, String[] groupByKeys)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    if (groupByKeys == null || groupByKeys.length == 0)
      throw new BadRequestException(
          "You need to give one groupByKey parameters, if you want to use groupBy/tag");
    SortedMap<OSHDBTimestampAndIndex<Integer>, Number> result;
    SortedMap<Integer, SortedMap<OSHDBTimestamp, Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    ExecutionUtils exeUtils = new ExecutionUtils();
    String description = "";
    String requestURL = null;
    if (!isPost)
      requestURL = RequestInterceptor.requestUrl;
    TagTranslator tt = Application.getTagTranslator();
    Integer[] keysInt = new Integer[groupByKeys.length];
    mapRed = iP.processParameters(mapRed, true, isPost, bboxes, bcircles, bpolys, types, keys,
        values, userids, time, showMetadata);


    for (int i = 0; i < groupByKeys.length; i++) {
      keysInt[i] = tt.getOSHDBTagKeyOf(groupByKeys[i]).toInt();
    }
    // group by key logic
    result = mapRed.flatMap(f -> {
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
        .map(Pair::getValue).sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          switch (requestResource) {
            case LENGTH:
              return Geo.lengthOf(snapshot.getGeometry());
            case PERIMETER:
              if (snapshot.getGeometry() instanceof Polygonal)
                return Geo.lengthOf(snapshot.getGeometry().getBoundary());
              else
                return 0.0;
            case AREA:
              return Geo.areaOf(snapshot.getGeometry());
            default:
              return 0.0;
          }
        });
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    DecimalFormat lengthPerimeterAreaDf = exeUtils.defineDecimalFormat("#.##");
    int count = 0;
    int innerCount = 0;
    // iterate over the entry objects aggregated by keys
    for (Entry<Integer, SortedMap<OSHDBTimestamp, Number>> entry : groupByResult.entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      innerCount = 0;
      // check for non-remainder objects (which do have the defined key)
      if (entry.getKey() != -1) {
        groupByName = tt.getOSMTagKeyOf(entry.getKey().intValue()).toString();
      } else {
        groupByName = "remainder";
      }
      // iterate over the timestamp-value pairs
      for (Entry<OSHDBTimestamp, Number> innerEntry : entry.getValue().entrySet()) {
        results[innerCount] = new Result(
            TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
            Double.parseDouble(lengthPerimeterAreaDf.format(innerEntry.getValue().doubleValue())));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    switch (requestResource) {
      case LENGTH:
        description = "Total length of items in meter aggregated on the key.";
        break;
      case PERIMETER:
        description = "Total perimeter of polygonal items in meter aggregated on the key.";
        break;
      case AREA:
        description = "Total area of items in square meter aggregated on the key.";
        break;
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      metadata = new Metadata(duration, description, requestURL);
    }
    GroupByKeyResponse response =
        new GroupByKeyResponse(new Attribution(url, text), Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a length, perimeter, or area calculation grouped by the tag.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCountGroupByTag(String, String, String, String[], String[], String[], String[], String[], String, String[], String[])
   * getCountGroupByTag} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (LENGTH, PERIMETER, AREA).
   * @param isPost <code>Boolean</code> defining if this method is called from a POST (true) or a
   *        GET (false) request.
   * @param isDensity <code>Boolean</code> parameter saying if this method was called from a density
   *        resource (true) or not (false).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByTagResponse
   *         GroupByTagResponseContent}
   */
  public static GroupByTagResponse executeLengthPerimeterAreaGroupByTag(
      RequestResource requestResource, boolean isPost, boolean isDensity, String bboxes,
      String bcircles, String bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time, String showMetadata, String[] groupByKey,
      String[] groupByValues) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    if (groupByKey == null || groupByKey.length == 0)
      throw new BadRequestException(
          "You need to give one groupByKey parameters, if you want to use groupBy/tag.");
    SortedMap<OSHDBTimestampAndIndex<Pair<Integer, Integer>>, Number> result;
    SortedMap<Pair<Integer, Integer>, SortedMap<OSHDBTimestamp, Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    ExecutionUtils exeUtils = new ExecutionUtils();

    String description = "";
    String requestURL = null;
    if (!isPost)
      requestURL = RequestInterceptor.requestUrl;
    if (groupByValues == null)
      groupByValues = new String[0];
    TagTranslator tt = Application.getTagTranslator();
    Integer[] valuesInt = new Integer[groupByValues.length];
    ArrayList<Pair<Integer, Integer>> zeroFill = new ArrayList<Pair<Integer, Integer>>();
    mapRed = iP.processParameters(mapRed, true, isPost, bboxes, bcircles, bpolys, types, keys,
        values, userids, time, showMetadata);
    int keysInt = tt.getOSHDBTagKeyOf(groupByKey[0]).toInt();
    if (groupByValues.length != 0) {
      for (int j = 0; j < groupByValues.length; j++) {
        valuesInt[j] = tt.getOSHDBTagOf(groupByKey[0], groupByValues[j]).getValue();
        zeroFill.add(new ImmutablePair<Integer, Integer>(keysInt, valuesInt[j]));
      }
    }
    // group by tag logic
    result = mapRed.map(f -> {
      int[] tags = f.getEntity().getRawTags();
      for (int i = 0; i < tags.length; i += 2) {
        int tagKeyId = tags[i];
        int tagValueId = tags[i + 1];
        if (tagKeyId == keysInt) {
          if (valuesInt.length == 0) {
            return new ImmutablePair<>(new ImmutablePair<Integer, Integer>(tagKeyId, tagValueId),
                f);
          }
          for (int value : valuesInt) {
            if (tagValueId == value)
              return new ImmutablePair<>(new ImmutablePair<Integer, Integer>(tagKeyId, tagValueId),
                  f);
          }
        }
      }
      return new ImmutablePair<>(new ImmutablePair<Integer, Integer>(-1, -1), f);
    }).aggregateByTimestamp().aggregateBy(Pair::getKey).zerofillIndices(zeroFill)
        .map(Pair::getValue).sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          switch (requestResource) {
            case LENGTH:
              return Geo.lengthOf(snapshot.getGeometry());
            case PERIMETER:
              if (snapshot.getGeometry() instanceof Polygonal)
                return Geo.lengthOf(snapshot.getGeometry().getBoundary());
              else
                return 0.0;
            case AREA:
              return Geo.areaOf(snapshot.getGeometry());
            default:
              return 0.0;
          }
        });
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    // +1 is needed in case the groupByKey is unresolved (not in keytables)
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    DecimalFormat lengthPerimeterAreaDf = exeUtils.defineDecimalFormat("#.##");
    GeometryBuilder geomBuilder = iP.getGeomBuilder();
    Geometry geom = exeUtils.getGeometry(iP.getBoundaryType(), geomBuilder);
    int count = 0;
    // iterate over the entry objects aggregated by tags
    for (Entry<Pair<Integer, Integer>, SortedMap<OSHDBTimestamp, Number>> entry : groupByResult
        .entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      int innerCount = 0;
      // check for non-remainder objects (which do have the defined key and value)
      if (entry.getKey().getKey() != -1 && entry.getKey().getValue() != -1) {
        groupByName = tt.getOSMTagOf(keysInt, entry.getKey().getValue()).toString();
      } else {
        groupByName = "remainder";
      }
      // iterate over the timestamp-value pairs
      for (Entry<OSHDBTimestamp, Number> innerEntry : entry.getValue().entrySet()) {
        if (isDensity)
          results[innerCount] = new Result(
              TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
              Double.parseDouble(lengthPerimeterAreaDf
                  .format((innerEntry.getValue().doubleValue() / (Geo.areaOf(geom) / 1000000)))));
        else
          results[innerCount] =
              new Result(TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()), Double
                  .parseDouble(lengthPerimeterAreaDf.format(innerEntry.getValue().doubleValue())));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    // remove null objects in the resultSet
    resultSet = Arrays.stream(resultSet).filter(Objects::nonNull).toArray(GroupByResult[]::new);
    switch (requestResource) {
      case LENGTH:
        if (isDensity) {

          description =
              "Density of selected items (length of items in meter per square kilometer).";
        } else {

          description = "Total length of items in meter aggregated on the tag.";
        }
        break;
      case PERIMETER:
        if (isDensity) {

          description =
              "Density of selected items (perimeter of items in meter per square kilometer).";
        } else {

          description = "Total perimeter of polygonal items in meter aggregated on the tag.";
        }
        break;
      case AREA:
        if (isDensity) {

          description =
              "Density of selected items (area of items in square meter per square kilometer).";
        } else {

          description = "Total area of items in square meter aggregated on the tag.";
        }
        break;
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      metadata = new Metadata(duration, description, requestURL);
    }
    GroupByTagResponse response =
        new GroupByTagResponse(new Attribution(url, text), Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a length, perimeter, or area calculation grouped by the user.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (LENGTH, PERIMETER, AREA).
   * @param isPost <code>Boolean</code> defining if this method is called from a POST (true) or a
   *        GET (false) request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByUserResponse
   *         GroupByUserResponseContent}
   */
  public static GroupByUserResponse executeLengthPerimeterAreaGroupByUser(
      RequestResource requestResource, boolean isPost, String bboxes, String bcircles,
      String bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestampAndIndex<Integer>, Number> result;
    SortedMap<Integer, SortedMap<OSHDBTimestamp, Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    ExecutionUtils exeUtils = new ExecutionUtils();
    String description = "";
    String requestURL = null;
    ArrayList<Integer> useridsInt = new ArrayList<Integer>();
    if (!isPost)
      requestURL = RequestInterceptor.requestUrl;
    mapRed = iP.processParameters(mapRed, true, isPost, bboxes, bcircles, bpolys, types, keys,
        values, userids, time, showMetadata);


    if (userids != null)
      for (String user : userids)
        // converting userids to int for usage in zerofill
        useridsInt.add(Integer.parseInt(user));
    result = mapRed.aggregateByTimestamp()
        .aggregateBy((SerializableFunction<OSMEntitySnapshot, Integer>) f -> {
          return f.getEntity().getUserId();
        }).zerofillIndices(useridsInt)
        .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          switch (requestResource) {
            case LENGTH:
              return Geo.lengthOf(snapshot.getGeometry());
            case PERIMETER:
              if (snapshot.getGeometry() instanceof Polygonal)
                return Geo.lengthOf(snapshot.getGeometry().getBoundary());
              else
                return 0.0;
            case AREA:
              return Geo.areaOf(snapshot.getGeometry());
            default:
              return 0.0;
          }
        });
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    DecimalFormat lengthPerimeterAreaDf = exeUtils.defineDecimalFormat("#.##");
    int count = 0;
    int innerCount = 0;
    // iterate over the entry objects aggregated by type
    for (Entry<Integer, SortedMap<OSHDBTimestamp, Number>> entry : groupByResult.entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      innerCount = 0;
      // iterate over the timestamp-value pairs
      for (Entry<OSHDBTimestamp, Number> innerEntry : entry.getValue().entrySet()) {
        results[innerCount] = new Result(
            TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
            Double.parseDouble(lengthPerimeterAreaDf.format(innerEntry.getValue().doubleValue())));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    switch (requestResource) {
      case LENGTH:
        description = "Total length of items in meter aggregated on the userid.";
        break;
      case PERIMETER:
        description = "Total perimeter of polygonal items in meter aggregated on the userid.";
        break;
      case AREA:
        description = "Total area of items in square meter aggregated on the userid.";
        break;
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      metadata = new Metadata(duration, description, requestURL);
    }
    GroupByUserResponse response =
        new GroupByUserResponse(new Attribution(url, text), Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a perimeter, or area calculation grouped by the OSM type.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (LENGTH, PERIMETER, AREA).
   * @param isPost <code>Boolean</code> defining if this method is called from a POST (true) or a
   *        GET (false) request.
   * @param isDensity <code>Boolean</code> parameter saying if this method was called from a density
   *        resource (true) or not (false).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByTypeResponse
   *         GroupByTypeResponseContent}
   */
  public static GroupByTypeResponse executePerimeterAreaGroupByType(RequestResource requestResource,
      boolean isPost, boolean isDensity, String bboxes, String bcircles, String bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestampAndIndex<OSMType>, Number> result = null;
    SortedMap<OSMType, SortedMap<OSHDBTimestamp, Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    ExecutionUtils exeUtils = new ExecutionUtils();
    String description = null;
    String requestURL = null;
    if (!isPost)
      requestURL = RequestInterceptor.requestUrl;
    mapRed = iP.processParameters(mapRed, true, isPost, bboxes, bcircles, bpolys, types, keys,
        values, userids, time, showMetadata);
    switch (requestResource) {
      case AREA:
        result = mapRed.aggregateByTimestamp()
            .aggregateBy((SerializableFunction<OSMEntitySnapshot, OSMType>) f -> {
              return f.getEntity().getType();
            }).zerofillIndices(iP.getOsmTypes())
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
              return Geo.areaOf(snapshot.getGeometry());
            });
        if (isDensity) {
          description =
              "Density of selected items (area of items in square meter per square kilometer) aggregated on the type.";
        } else {
          description = "Total area of items in square meter aggregated on the type.";
        }
        break;
      case PERIMETER:
        result = mapRed.aggregateByTimestamp()
            .aggregateBy((SerializableFunction<OSMEntitySnapshot, OSMType>) f -> {
              return f.getEntity().getType();
            }).zerofillIndices(iP.getOsmTypes())
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
              if (snapshot.getGeometry() instanceof Polygonal)
                return Geo.lengthOf(snapshot.getGeometry().getBoundary());
              else
                return 0.0;
            });
        if (isDensity) {
          description =
              "Density of selected items (perimeter of items in meter per square kilometer) aggregated on the type.";
        } else {
          description = "Total perimeter of items in meter aggregated on the type.";
        }
        break;
      default:
        // do nothing.. should never reach this :D
        break;
    }
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    DecimalFormat lengthPerimeterAreaDf = exeUtils.defineDecimalFormat("#.##");
    GeometryBuilder geomBuilder = iP.getGeomBuilder();
    Geometry geom = exeUtils.getGeometry(iP.getBoundaryType(), geomBuilder);
    int count = 0;
    int innerCount = 0;
    // iterate over the entry objects aggregated by type
    for (Entry<OSMType, SortedMap<OSHDBTimestamp, Number>> entry : groupByResult.entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      innerCount = 0;
      // iterate over the timestamp-value pairs
      for (Entry<OSHDBTimestamp, Number> innerEntry : entry.getValue().entrySet()) {
        if (isDensity)
          results[innerCount] = new Result(
              TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
              Double.parseDouble(lengthPerimeterAreaDf
                  .format((innerEntry.getValue().doubleValue() / (Geo.areaOf(geom) / 1000000)))));
        else
          results[innerCount] =
              new Result(TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()), Double
                  .parseDouble(lengthPerimeterAreaDf.format(innerEntry.getValue().doubleValue())));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      metadata = new Metadata(duration, description, requestURL);
    }
    GroupByTypeResponse response =
        new GroupByTypeResponse(new Attribution(url, text), Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a length|perimeter|area-share calculation.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCountShare(String, String, String, String[], String[], String[], String[], String[], String, String[], String[])
   * getCountShare} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (LENGTH, PERIMETER, AREA).
   * @param isPost <code>Boolean</code> defining if this method is called from a POST (true) or a
   *        GET (false) request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   */
  public static ShareResponse executeLengthPerimeterAreaShare(RequestResource requestResource,
      boolean isPost, String bboxes, String bcircles, String bpolys, String[] types, String[] keys,
      String[] values, String[] userids, String[] time, String showMetadata, String[] keys2,
      String[] values2) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    values2 = exeUtils.shareParamEvaluation(keys2, values2);
    SortedMap<OSHDBTimestampAndIndex<Boolean>, Number> result;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String description = "";
    String requestURL = null;
    TagTranslator tt = Application.getTagTranslator();
    Integer[] keysInt2 = new Integer[keys2.length];
    Integer[] valuesInt2 = new Integer[values2.length];
    if (!isPost)
      requestURL = RequestInterceptor.requestUrl;
    for (int i = 0; i < keys2.length; i++) {
      keysInt2[i] = tt.getOSHDBTagKeyOf(keys2[i]).toInt();
      if (values2 != null && i < values2.length)
        valuesInt2[i] = tt.getOSHDBTagOf(keys2[i], values2[i]).getValue();
    }
    mapRed = iP.processParameters(mapRed, true, isPost, bboxes, bcircles, bpolys, types, keys,
        values, userids, time, showMetadata);


    result = mapRed.aggregateByTimestamp().aggregateBy(f -> {
      // result aggregated on true (if obj contains all tags) and false (if not all are contained)
      boolean hasTags = false;
      for (int i = 0; i < keysInt2.length; i++) {
        if (f.getEntity().hasTagKey(keysInt2[i])) {
          if (i >= valuesInt2.length) {
            // if more keys2 than values2 are given
            hasTags = true;
            continue;
          }
          if (f.getEntity().hasTagValue(keysInt2[i], valuesInt2[i])) {
            hasTags = true;
          } else {
            hasTags = false;
            break;
          }
        } else {
          hasTags = false;
          break;
        }
      }
      return hasTags;
    }).zerofillIndices(Arrays.asList(true, false))
        .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          switch (requestResource) {
            case LENGTH:
              return Geo.lengthOf(snapshot.getGeometry());
            case PERIMETER:
              if (snapshot.getGeometry() instanceof Polygonal)
                return Geo.lengthOf(snapshot.getGeometry().getBoundary());
              else
                return 0.0;
            case AREA:
              return Geo.areaOf(snapshot.getGeometry());
            default:
              return 0.0;
          }
        });
    DecimalFormat lengthPerimeterAreaDf = exeUtils.defineDecimalFormat("#.##");
    Double[] whole = new Double[result.size()];
    Double[] part = new Double[result.size()];
    String[] timeArray = new String[result.size()];
    // needed time array in case no key can be found
    String[] noPartTimeArray = new String[result.size()];
    int partCount = 0;
    int wholeCount = 0;
    int timeCount = 0;
    // fill whole and part arrays with -1 values to indicate "no value"
    for (int i = 0; i < result.size(); i++) {
      whole[i] = -1.0;
      part[i] = -1.0;
    }
    // time and value extraction
    for (Entry<OSHDBTimestampAndIndex<Boolean>, Number> entry : result.entrySet()) {
      // this time array counts for each entry in the entrySet
      noPartTimeArray[timeCount] =
          TimestampFormatter.getInstance().isoDateTime(entry.getKey().getTimeIndex());
      if (entry.getKey().getOtherIndex()) {
        timeArray[partCount] =
            TimestampFormatter.getInstance().isoDateTime(entry.getKey().getTimeIndex());
        part[partCount] =
            Double.parseDouble(lengthPerimeterAreaDf.format(entry.getValue().doubleValue()));
        if (whole[partCount] == null || whole[partCount] == -1)
          whole[partCount] =
              Double.parseDouble(lengthPerimeterAreaDf.format(entry.getValue().doubleValue()));
        else
          whole[partCount] = whole[partCount]
              + Double.parseDouble(lengthPerimeterAreaDf.format(entry.getValue().doubleValue()));
        partCount++;
      } else {
        // else - set/increase only whole
        if (whole[wholeCount] == null || whole[wholeCount] == -1)
          whole[wholeCount] =
              Double.parseDouble(lengthPerimeterAreaDf.format(entry.getValue().doubleValue()));
        else
          whole[wholeCount] = whole[partCount]
              + Double.parseDouble(lengthPerimeterAreaDf.format(entry.getValue().doubleValue()));
        wholeCount++;
      }
      timeCount++;
    }
    // remove the possible null values in the array
    timeArray = Arrays.stream(timeArray).filter(Objects::nonNull).toArray(String[]::new);
    // overwrite in case the given key for part is not existent in the whole for no timestamp
    if (timeArray.length < 1) {
      timeArray = noPartTimeArray;
    }
    ShareResult[] resultSet = new ShareResult[timeArray.length];
    for (int i = 0; i < timeArray.length; i++) {
      // set whole or part to 0 if they have -1 (== no value)
      if (whole[i] == -1)
        whole[i] = 0.0;
      if (part[i] == -1)
        part[i] = 0.0;
      resultSet[i] = new ShareResult(timeArray[i], whole[i], part[i]);
    }
    switch (requestResource) {
      case LENGTH:
        description =
            "Total length of the whole and of a share of items in meter satisfying keys2 and values2 within items selected by types, keys, values.";
        break;
      case PERIMETER:
        description =
            "Total perimeter of the whole and of a share of items in meter satisfying keys2 and values2 within items selected by types, keys, values.";
        break;
      case AREA:
        description =
            "Total area of the whole and of a share of items in square meter satisfying keys2 and values2 within items selected by types, keys, values.";
        break;
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      metadata = new Metadata(duration, description, requestURL);
    }
    ShareResponse response =
        new ShareResponse(new Attribution(url, text), Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a length|perimeter|area-share calculation grouped by the boundary.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCountShare(String, String, String, String[], String[], String[], String[], String[], String, String[], String[])
   * getCountShare} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (LENGTH, PERIMETER, AREA).
   * @param isPost <code>Boolean</code> defining if this method is called from a POST (true) or a
   *        GET (false) request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.ShareGroupByBoundaryResponse
   *         ShareGroupByBoundaryResponse}
   */
  public static ShareGroupByBoundaryResponse executeLengthPerimeterAreaShareGroupByBoundary(
      RequestResource requestResource, boolean isPost, String bboxes, String bcircles,
      String bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata, String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    values2 = exeUtils.shareParamEvaluation(keys2, values2);
    SortedMap<OSHDBTimestampAndIndex<Pair<Integer, Boolean>>, Number> result = null;
    SortedMap<Pair<Integer, Boolean>, SortedMap<OSHDBTimestamp, Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String requestURL = null;
    TagTranslator tt = Application.getTagTranslator();
    Integer[] keysInt2 = new Integer[keys2.length];
    Integer[] valuesInt2 = new Integer[values2.length];
    if (!isPost)
      requestURL = RequestInterceptor.requestUrl;
    for (int i = 0; i < keys2.length; i++) {
      keysInt2[i] = tt.getOSHDBTagKeyOf(keys2[i]).toInt();
      if (values2 != null && i < values2.length)
        valuesInt2[i] = tt.getOSHDBTagOf(keys2[i], values2[i]).getValue();
    }
    mapRed = iP.processParameters(mapRed, true, isPost, bboxes, bcircles, bpolys, types, keys,
        values, userids, time, showMetadata);
    GeometryBuilder geomBuilder = iP.getGeomBuilder();
    Utils utils = iP.getUtils();
    result = exeUtils.computeLengthPerimeterAreaShareGBBResult(requestResource,
        iP.getBoundaryType(), mapRed, keysInt2, valuesInt2, geomBuilder);
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    ShareGroupByResult[] groupByResultSet = new ShareGroupByResult[groupByResult.size() / 2];
    DecimalFormat lengthPerimeterAreaDf = exeUtils.defineDecimalFormat("#.##");
    String groupByName = "";
    String[] boundaryIds = utils.getBoundaryIds();
    Double[] whole = null;
    Double[] part = null;
    String[] timeArray = null;
    int count = 1;
    int gBNCount = 0;
    for (Entry<Pair<Integer, Boolean>, SortedMap<OSHDBTimestamp, Number>> entry : groupByResult
        .entrySet()) {
      // on boundary param aggregated values (2x the same param)
      if (count == 1)
        timeArray = new String[entry.getValue().entrySet().size()];
      if (entry.getKey().getRight()) {
        // on true aggregated values
        part = new Double[entry.getValue().entrySet().size()];
        int partCount = 0;
        for (Entry<OSHDBTimestamp, Number> innerEntry : entry.getValue().entrySet()) {
          part[partCount] =
              Double.parseDouble(lengthPerimeterAreaDf.format(innerEntry.getValue().doubleValue()));
          partCount++;
        }
      } else {
        // on false aggregated values
        whole = new Double[entry.getValue().entrySet().size()];
        int wholeCount = 0;
        for (Entry<OSHDBTimestamp, Number> innerEntry : entry.getValue().entrySet()) {
          whole[wholeCount] =
              Double.parseDouble(lengthPerimeterAreaDf.format(innerEntry.getValue().doubleValue()));
          if (count == 1)
            timeArray[wholeCount] = innerEntry.getKey().toString();
          wholeCount++;
        }
      }
      if (count % 2 == 0) {
        // is only executed every second run
        groupByName = boundaryIds[gBNCount];
        ShareResult[] resultSet = new ShareResult[timeArray.length];
        for (int i = 0; i < timeArray.length; i++) {
          whole[i] = whole[i] + part[i];
          resultSet[i] = new ShareResult(timeArray[i], whole[i], part[i]);
        }
        groupByResultSet[gBNCount] = new ShareGroupByResult(groupByName, resultSet);
        gBNCount++;
      }
      count++;
    }
    Metadata metadata = null;
    if (iP.getShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration,
          "Share of items satisfying keys2 and values2 within items selected by types, keys, values, grouped on the boundary parameter.",
          requestURL);
    }
    ShareGroupByBoundaryResponse response = new ShareGroupByBoundaryResponse(
        new Attribution(url, text), Application.apiVersion, metadata, groupByResultSet);
    return response;
  }

  /**
   * Performs a length|perimeter|area-ratio calculation.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCountRatio(String, String, String, String[], String[], String[], String[], String[], String, String[], String[], String[])
   * getCountRatio} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (LENGTH, PERIMETER, AREA).
   * @param isPost <code>Boolean</code> defining if this method is called from a POST (true) or a
   *        GET (false) request.
   * @param types2 <code>String</code> array having the same format as types.
   * @param keys2 <code>String</code> array having the same format as keys.
   * @param values2 <code>String</code> array having the same format as values.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   */
  public static RatioResponse executeLengthPerimeterAreaRatio(RequestResource requestResource,
      boolean isPost, String bboxes, String bcircles, String bpolys, String[] types, String[] keys,
      String[] values, String[] userids, String[] time, String showMetadata, String[] types2,
      String[] keys2, String[] values2) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Number> result1 = null;
    SortedMap<OSHDBTimestamp, Number> result2 = null;
    MapReducer<OSMEntitySnapshot> mapRed1 = null;
    MapReducer<OSMEntitySnapshot> mapRed2 = null;
    InputProcessor iP = new InputProcessor();
    ExecutionUtils exeUtils = new ExecutionUtils();

    String description = "";
    String requestURL = null;
    if (!isPost)
      requestURL = RequestInterceptor.requestUrl;
    mapRed1 = iP.processParameters(mapRed1, true, isPost, bboxes, bcircles, bpolys, types, keys,
        values, userids, time, showMetadata);
    mapRed2 = iP.processParameters(mapRed2, true, isPost, bboxes, bcircles, bpolys, types2, keys2,
        values2, userids, time, showMetadata);
    switch (requestResource) {
      case AREA:
        result1 = mapRed1.aggregateByTimestamp()
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
              return Geo.areaOf(snapshot.getGeometry());
            });
        result2 = mapRed2.aggregateByTimestamp()
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
              return Geo.areaOf(snapshot.getGeometry());
            });
        description =
            "Area of items in square meter satisfying types2, keys2, values2 parameters (= value2 output) "
                + "within items selected by types, keys, values parameters (= value output) and ratio of value2:value.";
        break;
      case LENGTH:
        result1 = mapRed1.aggregateByTimestamp()
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
              return Geo.lengthOf(snapshot.getGeometry());
            });
        result2 = mapRed2.aggregateByTimestamp()
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
              return Geo.lengthOf(snapshot.getGeometry());
            });
        description =
            "Length of items in meter satisfying types2, keys2, values2 parameters (= value2 output) "
                + "within items selected by types, keys, values parameters (= value output) and ratio of value2:value.";
        break;
      case PERIMETER:
        result1 = mapRed1.aggregateByTimestamp()
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
              if (snapshot.getGeometry() instanceof Polygonal)
                return Geo.lengthOf(snapshot.getGeometry().getBoundary());
              else
                return 0.0;
            });
        result2 = mapRed2.aggregateByTimestamp()
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
              if (snapshot.getGeometry() instanceof Polygonal)
                return Geo.lengthOf(snapshot.getGeometry().getBoundary());
              else
                return 0.0;
            });
        description =
            "Perimeter of items in meter satisfying types2, keys2, values2 parameters (= value2 output) "
                + "within items selected by types, keys, values parameters (= value output) and ratio of value2:value.";
        break;
    }
    Result[] resultSet1 = new Result[result1.size()];
    int count = 0;
    for (Entry<OSHDBTimestamp, Number> entry : result1.entrySet()) {
      resultSet1[count] = new Result(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
          entry.getValue().doubleValue());
      count++;
    }
    RatioResult[] resultSet = new RatioResult[result1.size()];
    DecimalFormat ratioDF = exeUtils.defineDecimalFormat("#.######");
    DecimalFormat lengthPerimeterAreaDf = exeUtils.defineDecimalFormat("#.##");
    count = 0;
    for (Entry<OSHDBTimestamp, Number> entry : result2.entrySet()) {
      String date = resultSet1[count].getTimestamp();
      double ratio = (entry.getValue().doubleValue() / resultSet1[count].getValue());
      // in case ratio has the value "NaN", "Infinity", etc.
      try {
        ratio = Double.parseDouble(ratioDF.format(ratio));
      } catch (Exception e) {
        // do nothing --> just return ratio without rounding (trimming)
      }
      resultSet[count] = new RatioResult(date,
          Double.parseDouble(lengthPerimeterAreaDf.format(resultSet1[count].getValue())),
          Double.parseDouble(lengthPerimeterAreaDf.format(entry.getValue().doubleValue())), ratio);
      count++;
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      metadata = new Metadata(duration, description, requestURL);
    }
    RatioResponse response =
        new RatioResponse(new Attribution(url, text), Application.apiVersion, metadata, resultSet);
    return response;
  }

}
