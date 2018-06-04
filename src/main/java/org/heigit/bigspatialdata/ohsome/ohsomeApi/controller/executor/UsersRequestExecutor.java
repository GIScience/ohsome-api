package org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.executor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.SortedMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.Application;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.GeometryBuilder;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.InputProcessor;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.interceptor.RequestInterceptor;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.oshdb.DbConnData;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.oshdb.ExtractMetadata;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Attribution;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Metadata;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResult;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.users.UsersResult;
import org.heigit.bigspatialdata.oshdb.api.generic.OSHDBTimestampAndIndex;
import org.heigit.bigspatialdata.oshdb.api.generic.function.SerializableFunction;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregatorByTimestampAndIndex;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.object.OSMContribution;
import org.heigit.bigspatialdata.oshdb.osm.OSMType;
import org.heigit.bigspatialdata.oshdb.util.OSHDBTimestamp;
import org.heigit.bigspatialdata.oshdb.util.geometry.Geo;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;
import org.heigit.bigspatialdata.oshdb.util.time.TimestampFormatter;
import com.vividsolutions.jts.geom.Geometry;

/** Includes the execute methods for requests mapped to /users. */
public class UsersRequestExecutor {

  private static final String url = ExtractMetadata.attributionUrl;
  private static final String text = ExtractMetadata.attributionShort;

  /**
   * Performs a count calculation.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param rPs <code>RequestParameters</code> object, which holds those parameters that are used in
   *        every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   */
  public static DefaultAggregationResponse executeCount(RequestParameters rPs)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    SortedMap<OSHDBTimestamp, Integer> result;
    MapReducer<OSMContribution> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String description = null;
    String requestURL = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    if (!rPs.isPost())
      requestURL = RequestInterceptor.requestUrl;
    mapRed = iP.processParameters(mapRed, rPs);
    result = mapRed.aggregateByTimestamp().map(contrib -> {
      return contrib.getContributorUserId();
    }).countUniq();
    UsersResult[] resultSet = new UsersResult[result.size()];
    String[] toTimestamps = iP.getUtils().getToTimestamps();
    GeometryBuilder geomBuilder = iP.getGeomBuilder();
    Geometry geom = exeUtils.getGeometry(iP.getBoundaryType(), geomBuilder);
    int count = 0;
    for (Entry<OSHDBTimestamp, Integer> entry : result.entrySet()) {
      if (rPs.isDensity()) {
        resultSet[count] =
            new UsersResult(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
                toTimestamps[count + 1], Double.parseDouble(
                    df.format((entry.getValue().doubleValue() / (Geo.areaOf(geom) * 0.000001)))));
      } else {
        resultSet[count] =
            new UsersResult(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
                toTimestamps[count + 1], entry.getValue().intValue());
      }
      count++;
    }
    if (rPs.isDensity()) {
      description =
          "Density of distinct users per time interval (number of users per square-kilometer).";
    } else {
      description = "Number of distinct users per time interval.";
    }
    Metadata metadata = null;
    if (iP.getShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, description, requestURL);
    }
    DefaultAggregationResponse response = new DefaultAggregationResponse(new Attribution(url, text),
        Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count calculation grouped by the OSM type.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param rPs <code>RequestParameters</code> object, which holds those parameters that are used in
   *        every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResponse
   *         GroupByResponseContent}
   */
  public static GroupByResponse executeCountGroupByType(RequestParameters rPs)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    SortedMap<OSHDBTimestampAndIndex<OSMType>, Integer> result = null;
    SortedMap<OSMType, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    MapReducer<OSMContribution> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String description = null;
    String requestURL = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    if (!rPs.isPost())
      requestURL = RequestInterceptor.requestUrl;
    mapRed = iP.processParameters(mapRed, rPs);
    result = mapRed.aggregateByTimestamp()
        .aggregateBy((SerializableFunction<OSMContribution, OSMType>) f -> {
          return f.getEntityAfter().getType();
        }).zerofillIndices(iP.getOsmTypes()).count();
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    GeometryBuilder geomBuilder = iP.getGeomBuilder();
    Geometry geom = exeUtils.getGeometry(iP.getBoundaryType(), geomBuilder);
    String[] toTimestamps = iP.getUtils().getToTimestamps();
    int count = 0;
    int innerCount = 0;
    for (Entry<OSMType, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult.entrySet()) {
      UsersResult[] results = new UsersResult[entry.getValue().entrySet().size()];
      innerCount = 0;
      for (Entry<OSHDBTimestamp, ? extends Number> innerEntry : entry.getValue().entrySet()) {
        if (rPs.isDensity())
          results[innerCount] = new UsersResult(
              TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
              toTimestamps[innerCount + 1], Double.parseDouble(
                  df.format((innerEntry.getValue().doubleValue() / (Geo.areaOf(geom) / 1000000)))));
        else
          results[innerCount] =
              new UsersResult(TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
                  toTimestamps[innerCount + 1],
                  Double.parseDouble(df.format(innerEntry.getValue().doubleValue())));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    if (rPs.isDensity()) {
      description =
          "Density of distinct users per time interval (number of users per square-kilometer) aggregated on the type.";
    } else {
      description = "Number of distinct users per time interval aggregated on the type.";
    }
    Metadata metadata = null;
    if (iP.getShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, description, requestURL);
    }
    GroupByResponse response = new GroupByResponse(new Attribution(url, text),
        Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count calculation grouped by the tag.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCountGroupByTag(String, String, String, String[], String[], String[], String[], String[], String, String[], String[])
   * getCountGroupByTag} method.
   * 
   * @param rPs <code>RequestParameters</code> object, which holds those parameters that are used in
   *        every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResponse
   *         GroupByResponseContent}
   */
  public static GroupByResponse executeCountGroupByTag(RequestParameters rPs, String[] groupByKey,
      String[] groupByValues) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    if (groupByKey == null || groupByKey.length != 1)
      throw new BadRequestException(
          "You need to give one groupByKey parameter, if you want to use groupBy/tag.");
    ExecutionUtils exeUtils = new ExecutionUtils();
    SortedMap<OSHDBTimestampAndIndex<Pair<Integer, Integer>>, Integer> result = null;
    SortedMap<Pair<Integer, Integer>, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    MapReducer<OSMContribution> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String description = null;
    String requestURL = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    if (!rPs.isPost())
      requestURL = RequestInterceptor.requestUrl;
    if (groupByValues == null)
      groupByValues = new String[0];
    TagTranslator tt = DbConnData.tagTranslator;
    Integer[] valuesInt = new Integer[groupByValues.length];
    ArrayList<Pair<Integer, Integer>> zeroFill = new ArrayList<Pair<Integer, Integer>>();
    int keysInt = tt.getOSHDBTagKeyOf(groupByKey[0]).toInt();
    if (groupByValues.length != 0) {
      for (int j = 0; j < groupByValues.length; j++) {
        valuesInt[j] = tt.getOSHDBTagOf(groupByKey[0], groupByValues[j]).getValue();
        zeroFill.add(new ImmutablePair<Integer, Integer>(keysInt, valuesInt[j]));
      }
    }
    mapRed = iP.processParameters(mapRed, rPs);
    result = mapRed.map(f -> {
      
      int[] tags;
      if (!f.getEntityAfter().isVisible())
        tags = f.getEntityBefore().getRawTags();
      else
        tags = f.getEntityAfter().getRawTags();
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
        .map(Pair::getValue).countUniq();
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    GeometryBuilder geomBuilder = iP.getGeomBuilder();
    Geometry geom = exeUtils.getGeometry(iP.getBoundaryType(), geomBuilder);
    String[] toTimestamps = iP.getUtils().getToTimestamps();
    int count = 0;
    for (Entry<Pair<Integer, Integer>, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult
        .entrySet()) {
      UsersResult[] results = new UsersResult[entry.getValue().entrySet().size()];
      int innerCount = 0;
      // check for non-remainder objects (which do have the defined key and value)
      if (entry.getKey().getKey() != -1 && entry.getKey().getValue() != -1) {
        groupByName = tt.getOSMTagOf(keysInt, entry.getKey().getValue()).toString();
      } else {
        groupByName = "remainder";
      }
      for (Entry<OSHDBTimestamp, ? extends Number> innerEntry : entry.getValue().entrySet()) {
        if (rPs.isDensity())
          results[innerCount] = new UsersResult(
              TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
              toTimestamps[innerCount + 1], Double.parseDouble(
                  df.format((innerEntry.getValue().doubleValue() / (Geo.areaOf(geom) / 1000000)))));
        else
          results[innerCount] =
              new UsersResult(TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
                  toTimestamps[innerCount + 1],
                  Double.parseDouble(df.format(innerEntry.getValue().doubleValue())));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    if (rPs.isDensity()) {
      description =
          "Density of distinct users per time interval (number of users per square-kilometer) aggregated on the tag.";
    } else {
      description = "Number of distinct users per time interval aggregated on the tag.";
    }
    Metadata metadata = null;
    if (iP.getShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, description, requestURL);
    }
    GroupByResponse response = new GroupByResponse(new Attribution(url, text),
        Application.apiVersion, metadata, resultSet);
    return response;
  }

}
