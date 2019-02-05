package org.heigit.bigspatialdata.ohsome.ohsomeapi.executor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.Application;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.ExceptionMessages;
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
import com.vividsolutions.jts.geom.Geometry;

/** Includes the execute methods for requests mapped to /users. */
public class UsersRequestExecutor {

  private static final String URL = ExtractMetadata.attributionUrl;
  private static final String TEXT = ExtractMetadata.attributionShort;

  private UsersRequestExecutor() {
    throw new IllegalStateException("Utility class");
  }

  /** Performs a count calculation. */
  public static Response executeCount(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse, boolean isDensity) throws Exception {
    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Integer> result;
    MapReducer<OSMContribution> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor(servletRequest, false, isDensity);
    mapRed = inputProcessor.processParameters();
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    String requestUrl = null;
    if (!"post".equalsIgnoreCase(requestParameters.getRequestMethod())) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    result = mapRed.aggregateByTimestamp().map(OSMContribution::getContributorUserId).countUniq();
    String[] toTimestamps = inputProcessor.getUtils().getToTimestamps();
    Geometry geom = null;
    String description = null;
    if (requestParameters.isDensity()) {
      description =
          "Density of distinct users per time interval (number of users per square-kilometer).";
      geom = inputProcessor.getGeometry();
    } else {
      description = "Number of distinct users per time interval.";
    }
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    UsersResult[] results =
        exeUtils.fillUsersResult(result, requestParameters.isDensity(), toTimestamps, df, geom);
    Metadata metadata = null;
    if (processingData.isShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, description, requestUrl);
    }
    if ("csv".equalsIgnoreCase(requestParameters.getFormat())) {
      exeUtils.writeCsvResponse(results, servletResponse,
          exeUtils.createCsvTopComments(URL, TEXT, Application.API_VERSION, metadata));
      return null;
    }
    return DefaultAggregationResponse.of(new Attribution(URL, TEXT), Application.API_VERSION,
        metadata, results);
  }

  /** Performs a count calculation grouped by the OSM type. */
  public static Response executeCountGroupByType(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse, boolean isDensity) throws Exception {
    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, OSMType>, Integer> result = null;
    MapReducer<OSMContribution> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor(servletRequest, false, isDensity);
    mapRed = inputProcessor.processParameters();
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    String requestUrl = null;
    if (!"post".equalsIgnoreCase(requestParameters.getRequestMethod())) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    result = mapRed.aggregateByTimestamp()
        .aggregateBy((SerializableFunction<OSMContribution, OSMType>) f -> {
          return f.getEntityAfter().getType();
        }, processingData.getOsmTypes()).map(OSMContribution::getContributorUserId).countUniq();
    SortedMap<OSMType, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    Geometry geom = inputProcessor.getGeometry();
    String[] toTimestamps = inputProcessor.getUtils().getToTimestamps();
    int count = 0;
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    for (Entry<OSMType, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult.entrySet()) {
      UsersResult[] results = exeUtils.fillUsersResult(entry.getValue(),
          requestParameters.isDensity(), toTimestamps, df, geom);
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    String description = null;
    if (requestParameters.isDensity()) {
      description =
          "Density of distinct users per time interval (number of users per square-kilometer) "
              + "aggregated on the type.";
    } else {
      description = "Number of distinct users per time interval aggregated on the type.";
    }
    Metadata metadata = null;
    if (processingData.isShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, description, requestUrl);
    }
    if ("csv".equalsIgnoreCase(requestParameters.getFormat())) {
      exeUtils.writeCsvResponse(resultSet, servletResponse,
          exeUtils.createCsvTopComments(URL, TEXT, Application.API_VERSION, metadata));
      return null;
    }
    return new GroupByResponse(new Attribution(URL, TEXT), Application.API_VERSION, metadata,
        resultSet);
  }

  /** Performs a count calculation grouped by the tag. */
  public static Response executeCountGroupByTag(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse, boolean isDensity) throws Exception {
    long startTime = System.currentTimeMillis();
    String[] groupByKey = servletRequest.getParameterValues("groupByKey");
    if (groupByKey == null || groupByKey.length != 1) {
      throw new BadRequestException(ExceptionMessages.GROUP_BY_KEY_PARAM);
    }
    MapReducer<OSMContribution> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor(servletRequest, false, isDensity);
    mapRed = inputProcessor.processParameters();
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    String requestUrl = null;
    if (!"post".equalsIgnoreCase(requestParameters.getRequestMethod())) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    String[] groupByValues = servletRequest.getParameterValues("groupByValues");
    if (groupByValues == null) {
      groupByValues = new String[0];
    }
    TagTranslator tt = DbConnData.tagTranslator;
    Integer[] valuesInt = new Integer[groupByValues.length];
    ArrayList<Pair<Integer, Integer>> zeroFill = new ArrayList<>();
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
        .map(OSMContribution::getContributorUserId).countUniq();
    SortedMap<Pair<Integer, Integer>, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    Geometry geom = inputProcessor.getGeometry();
    String[] toTimestamps = inputProcessor.getUtils().getToTimestamps();
    int count = 0;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
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
    String description = null;
    if (requestParameters.isDensity()) {
      description =
          "Density of distinct users per time interval (number of users per square-kilometer) "
              + "aggregated on the tag.";
    } else {
      description = "Number of distinct users per time interval aggregated on the tag.";
    }
    Metadata metadata = null;
    if (processingData.isShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, description, requestUrl);
    }
    if ("csv".equalsIgnoreCase(requestParameters.getFormat())) {
      exeUtils.writeCsvResponse(resultSet, servletResponse,
          exeUtils.createCsvTopComments(URL, TEXT, Application.API_VERSION, metadata));
      return null;
    }
    return new GroupByResponse(new Attribution(URL, TEXT), Application.API_VERSION, metadata,
        resultSet);
  }

  /** Performs a count calculation grouped by the key. */
  public static Response executeCountGroupByKey(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse, boolean isDensity) throws Exception {
    long startTime = System.currentTimeMillis();
    String[] groupByKeys = servletRequest.getParameterValues("groupByKeys");
    if (groupByKeys == null || groupByKeys.length == 0) {
      throw new BadRequestException(ExceptionMessages.GROUP_BY_KEYS_PARAM);
    }
    MapReducer<OSMContribution> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor(servletRequest, false, isDensity);
    mapRed = inputProcessor.processParameters();
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    String requestUrl = null;
    if (!"post".equalsIgnoreCase(requestParameters.getRequestMethod())) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    TagTranslator tt = DbConnData.tagTranslator;
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
        .map(OSMContribution::getContributorUserId).countUniq();
    SortedMap<Integer, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    String[] toTimestamps = inputProcessor.getUtils().getToTimestamps();
    int count = 0;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
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
    String description = null;
    if (requestParameters.isDensity()) {
      description =
          "Density of distinct users per time interval (number of users per square-kilometer) "
              + "aggregated on the key.";
    } else {
      description = "Number of distinct users per time interval aggregated on the key.";
    }
    Metadata metadata = null;
    if (processingData.isShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, description, requestUrl);
    }
    if ("csv".equalsIgnoreCase(requestParameters.getFormat())) {
      exeUtils.writeCsvResponse(resultSet, servletResponse,
          exeUtils.createCsvTopComments(URL, TEXT, Application.API_VERSION, metadata));
      return null;
    }
    return new GroupByResponse(new Attribution(URL, TEXT), Application.API_VERSION, metadata,
        resultSet);
  }
}
