package org.heigit.ohsome.ohsomeapi.executor;

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
import org.heigit.ohsome.ohsomeapi.Application;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.ohsome.ohsomeapi.output.Attribution;
import org.heigit.ohsome.ohsomeapi.output.Description;
import org.heigit.ohsome.ohsomeapi.output.Metadata;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.contributions.ContributionsResult;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResponse;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResult;
import org.heigit.ohsome.oshdb.OSHDBTag;
import org.heigit.ohsome.oshdb.OSHDBTimestamp;
import org.heigit.ohsome.oshdb.api.generic.OSHDBCombinedIndex;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;
import org.heigit.ohsome.oshdb.osm.OSMType;
import org.heigit.ohsome.oshdb.util.function.SerializableFunction;
import org.heigit.ohsome.oshdb.util.mappable.OSMContribution;
import org.heigit.ohsome.oshdb.util.tagtranslator.TagTranslator;
import org.locationtech.jts.geom.Geometry;

/**
 * Includes the execute methods for requests mapped to /users.
 */
public class UsersRequestExecutor {

  private static final String URL = ExtractMetadata.attributionUrl;
  private static final String TEXT = ExtractMetadata.attributionShort;
  public static final DecimalFormat df = ExecutionUtils.defineDecimalFormat("#.##");
  private static final String CONTRIBUTION_TYPE_PARAMETER = "contributionType";

  private UsersRequestExecutor() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Performs a count calculation grouped by the OSM type.
   */
  public static Response countGroupByType(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse, boolean isDensity) throws Exception {
    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, OSMType>, Integer> result = null;
    MapReducer<OSMContribution> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor(servletRequest, false, isDensity);
    mapRed = inputProcessor.processParameters();
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    result = mapRed.filter(ContributionsExecutor.contributionsFilter(servletRequest.getParameter(
            CONTRIBUTION_TYPE_PARAMETER)))
        .aggregateByTimestamp()
        .aggregateBy(
            (SerializableFunction<OSMContribution, OSMType>) f -> f.getEntityAfter().getType(),
            processingData.getOsmTypes())
        .map(OSMContribution::getContributorUserId)
        .countUniq();
    SortedMap<OSMType, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    int count = 0;
    Geometry geom = inputProcessor.getGeometry();
    for (Entry<OSMType, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult.entrySet()) {
      ContributionsResult[] results = ExecutionUtils.fillContributionsResult(entry.getValue(),
          requestParameters.isDensity(), inputProcessor, df, geom);
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    Metadata metadata = null;
    if (processingData.isShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, Description.countUsersGroupByType(isDensity),
          inputProcessor.getRequestUrlIfGetRequest(servletRequest));
    }
    if ("csv".equalsIgnoreCase(requestParameters.getFormat())) {
      ExecutionUtils exeUtils = new ExecutionUtils(processingData);
      exeUtils.writeCsvResponse(resultSet, servletResponse,
          ExecutionUtils.createCsvTopComments(URL, TEXT, Application.API_VERSION, metadata));
      return null;
    }
    return new GroupByResponse(new Attribution(URL, TEXT), Application.API_VERSION, metadata,
        resultSet);
  }

  /**
   * Performs a count calculation grouped by the tag.
   *
   * @throws BadRequestException if the groupByKey parameter is not given.
   */
  public static Response countGroupByTag(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse, boolean isDensity) throws Exception {
    long startTime = System.currentTimeMillis();
    InputProcessor inputProcessor = new InputProcessor(servletRequest, false, isDensity);
    String[] groupByKey = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByKey")));
    if (groupByKey.length != 1) {
      throw new BadRequestException(ExceptionMessages.GROUP_BY_KEY_PARAM);
    }
    MapReducer<OSMContribution> mapRed = null;
    mapRed = inputProcessor.processParameters();
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    String[] groupByValues = inputProcessor.splitParamOnComma(inputProcessor.createEmptyArrayIfNull(
        servletRequest.getParameterValues("groupByValues")));
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
    SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, Pair<Integer, Integer>>, Integer> result = null;
    result = mapRed
        .filter(ContributionsExecutor.contributionsFilter(
            servletRequest.getParameter(CONTRIBUTION_TYPE_PARAMETER)))
        .flatMap(f -> {
          List<Pair<Pair<Integer, Integer>, OSMContribution>> res = new LinkedList<>();
          Iterable<OSHDBTag> tags = ExecutionUtils.extractContributionTags(f);
          for (OSHDBTag tag : tags) {
            int tagKeyId = tag.getKey();
            int tagValueId = tag.getValue();
            if (tagKeyId == keysInt) {
              if (valuesInt.length == 0) {
                res.add(new ImmutablePair<>(new ImmutablePair<>(tagKeyId, tagValueId), f));
              }
              for (int value : valuesInt) {
                if (tagValueId == value) {
                  res.add(new ImmutablePair<>(new ImmutablePair<>(tagKeyId, tagValueId), f));
                }
              }
            }
          }
          if (res.isEmpty()) {
            res.add(new ImmutablePair<>(new ImmutablePair<>(-1, -1), f));
          }
          res.add(new ImmutablePair<>(new ImmutablePair<>(-2, -2), f));
          return res;
        })
        .aggregateByTimestamp()
        .aggregateBy(Pair::getKey, zeroFill).map(Pair::getValue)
        .map(OSMContribution::getContributorUserId)
        .countUniq();
    Geometry geom = inputProcessor.getGeometry();
    SortedMap<Pair<Integer, Integer>, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    int count = 0;
    for (Entry<Pair<Integer, Integer>, SortedMap<OSHDBTimestamp, Integer>> entry :
        groupByResult.entrySet()) {
      ContributionsResult[] results = ExecutionUtils.fillContributionsResult(entry.getValue(),
          requestParameters.isDensity(), inputProcessor, df, geom);
      if (entry.getKey().getKey() == -2 && entry.getKey().getValue() == -2) {
        groupByName = "total";
      } else if (entry.getKey().getKey() == -1 && entry.getKey().getValue() == -1) {
        groupByName = "remainder";
      } else {
        groupByName = tt.getOSMTagOf(keysInt, entry.getKey().getValue()).toString();
      }
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    Metadata metadata = null;
    if (processingData.isShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, Description.countUsersGroupByTag(isDensity),
          inputProcessor.getRequestUrlIfGetRequest(servletRequest));
    }
    if ("csv".equalsIgnoreCase(requestParameters.getFormat())) {
      ExecutionUtils exeUtils = new ExecutionUtils(processingData);
      exeUtils.writeCsvResponse(resultSet, servletResponse,
          ExecutionUtils.createCsvTopComments(URL, TEXT, Application.API_VERSION, metadata));
      return null;
    }
    return new GroupByResponse(new Attribution(URL, TEXT), Application.API_VERSION, metadata,
        resultSet);
  }

  /**
   * Performs a count calculation grouped by the key.
   *
   * @throws BadRequestException if the groupByKeys parameter is not given.
   */
  public static Response countGroupByKey(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse, boolean isDensity) throws Exception {
    long startTime = System.currentTimeMillis();
    InputProcessor inputProcessor = new InputProcessor(servletRequest, false, isDensity);
    String[] groupByKeys = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByKeys")));
    if (groupByKeys == null || groupByKeys.length == 0) {
      throw new BadRequestException(ExceptionMessages.GROUP_BY_KEYS_PARAM);
    }
    MapReducer<OSMContribution> mapRed = null;
    mapRed = inputProcessor.processParameters();
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    TagTranslator tt = DbConnData.tagTranslator;
    Integer[] keysInt = new Integer[groupByKeys.length];
    for (int i = 0; i < groupByKeys.length; i++) {
      keysInt[i] = tt.getOSHDBTagKeyOf(groupByKeys[i]).toInt();
    }
    SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, Integer> result = null;
    result = mapRed
        .filter(ContributionsExecutor.contributionsFilter(
            servletRequest.getParameter(CONTRIBUTION_TYPE_PARAMETER)))
        .flatMap(f -> {
          List<Pair<Integer, OSMContribution>> res = new LinkedList<>();
          Iterable<OSHDBTag> tags = ExecutionUtils.extractContributionTags(f);
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
          res.add(new ImmutablePair<>(-2, f));
          return res;
        })
        .aggregateByTimestamp()
        .aggregateBy(Pair::getKey, Arrays.asList(keysInt))
        .map(Pair::getValue)
        .map(OSMContribution::getContributorUserId)
        .countUniq();
    Geometry geom = inputProcessor.getGeometry();
    SortedMap<Integer, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    int count = 0;
    for (Entry<Integer, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult.entrySet()) {
      ContributionsResult[] results = ExecutionUtils.fillContributionsResult(entry.getValue(),
          requestParameters.isDensity(), inputProcessor, df, geom);
      if (entry.getKey() == -2) {
        groupByName = "total";
      } else if (entry.getKey() == -1) {
        groupByName = "remainder";
      } else {
        groupByName = tt.getOSMTagKeyOf(entry.getKey().intValue()).toString();
      }
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    Metadata metadata = null;
    if (processingData.isShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, Description.countUsersGroupByKey(isDensity),
          inputProcessor.getRequestUrlIfGetRequest(servletRequest));
    }
    if ("csv".equalsIgnoreCase(requestParameters.getFormat())) {
      ExecutionUtils exeUtils = new ExecutionUtils(processingData);
      exeUtils.writeCsvResponse(resultSet, servletResponse,
          ExecutionUtils.createCsvTopComments(URL, TEXT, Application.API_VERSION, metadata));
      return null;
    }
    return new GroupByResponse(new Attribution(URL, TEXT), Application.API_VERSION, metadata,
        resultSet);
  }
}
