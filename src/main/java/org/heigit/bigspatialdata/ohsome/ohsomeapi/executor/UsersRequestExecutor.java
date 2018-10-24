package org.heigit.bigspatialdata.ohsome.ohsomeapi.executor;

import com.vividsolutions.jts.geom.Geometry;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.Application;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.GeometryBuilder;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.interceptor.RequestInterceptor;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Attribution;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.DefaultAggregationResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Metadata;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.GroupByResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.GroupByResult;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.users.UsersResult;
import org.heigit.bigspatialdata.oshdb.api.generic.OSHDBCombinedIndex;
import org.heigit.bigspatialdata.oshdb.api.generic.function.SerializableFunction;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.object.OSMContribution;
import org.heigit.bigspatialdata.oshdb.osm.OSMType;
import org.heigit.bigspatialdata.oshdb.util.OSHDBTimestamp;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;


/** Includes the execute methods for requests mapped to /users. */
public class UsersRequestExecutor {

  private static final String url = ExtractMetadata.attributionUrl;
  private static final String text = ExtractMetadata.attributionShort;

  /**
   * Performs a count calculation.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @param requestParameters <code>RequestParameters</code> object, which holds those parameters
   *        that are used in every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   */
  public static Response executeCount(RequestParameters requestParameters)
      throws UnsupportedOperationException, Exception {
    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    SortedMap<OSHDBTimestamp, Integer> result;
    MapReducer<OSMContribution> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor();
    String description = null;
    String requestUrl = null;
    if (!requestParameters.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    mapRed = inputProcessor.processParameters(mapRed, requestParameters);
    result = mapRed.aggregateByTimestamp().map(contrib -> {
      return contrib.getContributorUserId();
    }).countUniq();
    String[] toTimestamps = inputProcessor.getUtils().getToTimestamps();
    Geometry geom = null;
    if (requestParameters.isDensity()) {
      description =
          "Density of distinct users per time interval (number of users per square-kilometer).";
      GeometryBuilder geomBuilder = inputProcessor.getGeomBuilder();
      geom = exeUtils.getGeometry(ProcessingData.boundary, geomBuilder);
    } else {
      description = "Number of distinct users per time interval.";
    }
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    UsersResult[] results =
        exeUtils.fillUsersResult(result, requestParameters.isDensity(), toTimestamps, df, geom);
    Metadata metadata = null;
    if (ProcessingData.showMetadata) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, description, requestUrl);
    }
    Response response = DefaultAggregationResponse.of(new Attribution(url, text),
        Application.apiVersion, metadata, results);
    return response;
  }

  /**
   * Performs a count calculation grouped by the OSM type.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @param requestParameters <code>RequestParameters</code> object, which holds those parameters
   *        that are used in every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  public static Response executeCountGroupByType(RequestParameters requestParameters)
      throws UnsupportedOperationException, Exception {
    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, OSMType>, Integer> result = null;
    MapReducer<OSMContribution> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor();
    String description = null;
    String requestUrl = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    if (!requestParameters.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    mapRed = inputProcessor.processParameters(mapRed, requestParameters);
    result = mapRed.aggregateByTimestamp()
        .aggregateBy((SerializableFunction<OSMContribution, OSMType>) f -> {
          return f.getEntityAfter().getType();
        }, ProcessingData.osmTypes).map(contrib -> {
          return contrib.getContributorUserId();
        }).countUniq();
    SortedMap<OSMType, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    GeometryBuilder geomBuilder = inputProcessor.getGeomBuilder();
    Geometry geom = exeUtils.getGeometry(ProcessingData.boundary, geomBuilder);
    String[] toTimestamps = inputProcessor.getUtils().getToTimestamps();
    int count = 0;
    for (Entry<OSMType, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult.entrySet()) {
      UsersResult[] results = exeUtils.fillUsersResult(entry.getValue(),
          requestParameters.isDensity(), toTimestamps, df, geom);
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    if (requestParameters.isDensity()) {
      description =
          "Density of distinct users per time interval (number of users per square-kilometer) "
              + "aggregated on the type.";
    } else {
      description = "Number of distinct users per time interval aggregated on the type.";
    }
    Metadata metadata = null;
    if (ProcessingData.showMetadata) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, description, requestUrl);
    }
    Response response = new GroupByResponse(new Attribution(url, text), Application.apiVersion,
        metadata, resultSet);
    return response;
  }

  /**
   * Performs a count calculation grouped by the tag.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#countGroupByTag(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest, String[], String[])
   * countGroupByTag} method.
   * 
   * @param requestParameters <code>RequestParameters</code> object, which holds those parameters
   *        that are used in every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  public static Response executeCountGroupByTag(RequestParameters requestParameters,
      String[] groupByKey, String[] groupByValues) throws UnsupportedOperationException, Exception {
    long startTime = System.currentTimeMillis();
    if (groupByKey == null || groupByKey.length != 1) {
      throw new BadRequestException(
          "You need to give one groupByKey parameter, if you want to use groupBy/tag.");
    }
    ExecutionUtils exeUtils = new ExecutionUtils();
    MapReducer<OSMContribution> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor();
    String description = null;
    String requestUrl = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    if (!requestParameters.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    if (groupByValues == null) {
      groupByValues = new String[0];
    }
    TagTranslator tt = DbConnData.tagTranslator;
    Integer[] valuesInt = new Integer[groupByValues.length];
    ArrayList<Pair<Integer, Integer>> zeroFill = new ArrayList<Pair<Integer, Integer>>();
    mapRed = inputProcessor.processParameters(mapRed, requestParameters);
    int keysInt = tt.getOSHDBTagKeyOf(groupByKey[0]).toInt();
    if (groupByValues.length != 0) {
      for (int j = 0; j < groupByValues.length; j++) {
        valuesInt[j] = tt.getOSHDBTagOf(groupByKey[0], groupByValues[j]).getValue();
        zeroFill.add(new ImmutablePair<Integer, Integer>(keysInt, valuesInt[j]));
      }
    }
    SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, Pair<Integer, Integer>>, Integer> result = null;
    result = mapRed.flatMap(f -> {
      List<Pair<Pair<Integer, Integer>, OSMContribution>> res = new LinkedList<>();
      int[] tags = exeUtils.extractContributionTags(f);
      for (int i = 0; i < tags.length; i += 2) {
        int tagKeyId = tags[i];
        int tagValueId = tags[i + 1];
        if (tagKeyId == keysInt) {
          if (valuesInt.length == 0) {
            res.add(
                new ImmutablePair<>(new ImmutablePair<Integer, Integer>(tagKeyId, tagValueId), f));
          }
          for (int value : valuesInt) {
            if (tagValueId == value) {
              res.add(new ImmutablePair<>(new ImmutablePair<Integer, Integer>(tagKeyId, tagValueId),
                  f));
            }
          }
        }
      }
      if (res.isEmpty()) {
        res.add(new ImmutablePair<>(new ImmutablePair<Integer, Integer>(-1, -1), f));
      }
      return res;
    }).aggregateByTimestamp().aggregateBy(Pair::getKey, zeroFill).map(Pair::getValue)
        .map(contrib -> {
          return contrib.getContributorUserId();
        }).countUniq();
    SortedMap<Pair<Integer, Integer>, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    GeometryBuilder geomBuilder = inputProcessor.getGeomBuilder();
    Geometry geom = exeUtils.getGeometry(ProcessingData.boundary, geomBuilder);
    String[] toTimestamps = inputProcessor.getUtils().getToTimestamps();
    int count = 0;
    for (Entry<Pair<Integer, Integer>, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult
        .entrySet()) {
      UsersResult[] results = exeUtils.fillUsersResult(entry.getValue(),
          requestParameters.isDensity(), toTimestamps, df, geom);
      // check for non-remainder objects (which do have the defined key and value)
      if (entry.getKey().getKey() != -1 && entry.getKey().getValue() != -1) {
        groupByName = tt.getOSMTagOf(keysInt, entry.getKey().getValue()).toString();
      } else {
        groupByName = "remainder";
      }
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    if (requestParameters.isDensity()) {
      description =
          "Density of distinct users per time interval (number of users per square-kilometer) "
              + "aggregated on the tag.";
    } else {
      description = "Number of distinct users per time interval aggregated on the tag.";
    }
    Metadata metadata = null;
    if (ProcessingData.showMetadata) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, description, requestUrl);
    }
    Response response = new GroupByResponse(new Attribution(url, text), Application.apiVersion,
        metadata, resultSet);
    return response;
  }

  /**
   * Performs a count calculation grouped by the key.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#countGroupByKey(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest, String[])
   * countGroupByKey} method.
   * 
   * @param requestParameters <code>RequestParameters</code> object, which holds those parameters
   *        that are used in every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  public static Response executeCountGroupByKey(RequestParameters requestParameters,
      String[] groupByKeys) throws UnsupportedOperationException, Exception {
    long startTime = System.currentTimeMillis();
    if (groupByKeys == null || groupByKeys.length == 0) {
      throw new BadRequestException(
          "You need to give at least one groupByKey parameter, if you want to use groupBy/key");
    }
    ExecutionUtils exeUtils = new ExecutionUtils();
    MapReducer<OSMContribution> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor();
    String description = null;
    String requestUrl = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    if (!requestParameters.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    TagTranslator tt = DbConnData.tagTranslator;
    mapRed = inputProcessor.processParameters(mapRed, requestParameters);
    Integer[] keysInt = new Integer[groupByKeys.length];
    for (int i = 0; i < groupByKeys.length; i++) {
      keysInt[i] = tt.getOSHDBTagKeyOf(groupByKeys[i]).toInt();
    }
    SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, Integer> result = null;
    result = mapRed.flatMap(f -> {
      List<Pair<Integer, OSMContribution>> res = new LinkedList<>();
      int[] tags = exeUtils.extractContributionTags(f);
      for (int i = 0; i < tags.length; i += 2) {
        int tagKeyId = tags[i];
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
    }).aggregateByTimestamp().aggregateBy(Pair::getKey, Arrays.asList(keysInt)).map(Pair::getValue)
        .map(contrib -> {
          return contrib.getContributorUserId();
        }).countUniq();
    SortedMap<Integer, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    String[] toTimestamps = inputProcessor.getUtils().getToTimestamps();
    int count = 0;
    for (Entry<Integer, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult.entrySet()) {
      UsersResult[] results = exeUtils.fillUsersResult(entry.getValue(),
          requestParameters.isDensity(), toTimestamps, df, null);
      // check for non-remainder objects (which do have the defined key)
      if (entry.getKey() != -1) {
        groupByName = tt.getOSMTagKeyOf(entry.getKey().intValue()).toString();
      } else {
        groupByName = "remainder";
      }
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    if (requestParameters.isDensity()) {
      description =
          "Density of distinct users per time interval (number of users per square-kilometer) "
              + "aggregated on the key.";
    } else {
      description = "Number of distinct users per time interval aggregated on the key.";
    }
    Metadata metadata = null;
    if (ProcessingData.showMetadata) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, description, requestUrl);
    }
    Response response = new GroupByResponse(new Attribution(url, text), Application.apiVersion,
        metadata, resultSet);
    return response;
  }
}
