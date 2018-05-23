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
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Attribution;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Metadata;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.RatioResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.RatioResult;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.elements.ElementsResult;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.elements.ShareResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.elements.ShareResult;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResult;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.RatioGroupByBoundaryResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.RatioGroupByResult;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.ShareGroupByBoundaryResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.ShareGroupByResult;
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
    String description = null;
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
    int count = 0;
    ElementsResult[] resultSet = new ElementsResult[result.size()];
    for (Entry<OSHDBTimestamp, ? extends Number> entry : result.entrySet()) {
      if (rPs.isDensity()) {
        resultSet[count] = new ElementsResult(
            TimestampFormatter.getInstance().isoDateTime(entry.getKey()), Double.parseDouble(
                df.format((entry.getValue().doubleValue() / (Geo.areaOf(geom) * 0.000001)))));
      } else {
        resultSet[count] =
            new ElementsResult(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
                Double.parseDouble(df.format((entry.getValue().doubleValue()))));
      }
      count++;
    }
    if (rPs.isDensity()) {
      description = "Density of selected items (" + requestResource.getLabel() + " of items in "
          + requestResource.getUnit() + " per square kilometer).";
    } else {
      description =
          "Total " + requestResource.getLabel() + " of items in " + requestResource.getUnit() + ".";
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
    String description = null;
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
    int innerCount = 0;
    for (Entry<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      ElementsResult[] results = new ElementsResult[entry.getValue().entrySet().size()];
      innerCount = 0;
      groupByName = boundaryIds[count];
      for (Entry<OSHDBTimestamp, ? extends Number> innerEntry : entry.getValue().entrySet()) {
        results[innerCount] =
            new ElementsResult(TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
                Double.parseDouble(df.format(innerEntry.getValue().doubleValue())));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    description = "Total " + requestResource.getLabel() + " of items in "
        + requestResource.getUnit() + " aggregated on the boundary.";
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
    String description = null;
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
    switch (requestResource) {
      case COUNT:
        result = preResult.count();
        break;
      case LENGTH:
        result = preResult.sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          return Geo.lengthOf(snapshot.getGeometry());
        });
        break;
      case PERIMETER:
        result = preResult.sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          if (snapshot.getGeometry() instanceof Polygonal)
            return Geo.lengthOf(snapshot.getGeometry().getBoundary());
          else
            return 0.0;
        });
        break;
      case AREA:
        result = preResult.sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          return Geo.areaOf(snapshot.getGeometry());
        });
        break;
    }
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    int count = 0;
    int innerCount = 0;
    for (Entry<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      ElementsResult[] results = new ElementsResult[entry.getValue().entrySet().size()];
      innerCount = 0;
      for (Entry<OSHDBTimestamp, ? extends Number> innerEntry : entry.getValue().entrySet()) {
        results[innerCount] =
            new ElementsResult(TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
                Double.parseDouble(df.format(innerEntry.getValue().doubleValue())));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    description = "Total " + requestResource.getLabel() + " of items in "
        + requestResource.getUnit() + " aggregated on the user.";
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
    switch (requestResource) {
      case COUNT:
        result = preResult.count();
        break;
      case LENGTH:
        result = preResult.sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          return Geo.lengthOf(snapshot.getGeometry());
        });
        break;
      case PERIMETER:
        result = preResult.sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          if (snapshot.getGeometry() instanceof Polygonal)
            return Geo.lengthOf(snapshot.getGeometry().getBoundary());
          else
            return 0.0;
        });
        break;
      case AREA:
        result = preResult.sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          return Geo.areaOf(snapshot.getGeometry());
        });
        break;
    }
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    GeometryBuilder geomBuilder = iP.getGeomBuilder();
    Geometry geom = exeUtils.getGeometry(iP.getBoundaryType(), geomBuilder);
    int count = 0;
    for (Entry<Pair<Integer, Integer>, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      ElementsResult[] results = new ElementsResult[entry.getValue().entrySet().size()];
      int innerCount = 0;
      // check for non-remainder objects (which do have the defined key and value)
      if (entry.getKey().getKey() != -1 && entry.getKey().getValue() != -1) {
        groupByName = tt.getOSMTagOf(keysInt, entry.getKey().getValue()).toString();
      } else {
        groupByName = "remainder";
      }
      for (Entry<OSHDBTimestamp, ? extends Number> innerEntry : entry.getValue().entrySet()) {
        if (rPs.isDensity())
          results[innerCount] = new ElementsResult(
              TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()), Double.parseDouble(
                  df.format((innerEntry.getValue().doubleValue() / (Geo.areaOf(geom) / 1000000)))));
        else
          results[innerCount] =
              new ElementsResult(TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
                  Double.parseDouble(df.format(innerEntry.getValue().doubleValue())));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    // used to remove null objects from the resultSet
    resultSet = Arrays.stream(resultSet).filter(Objects::nonNull).toArray(GroupByResult[]::new);
    if (rPs.isDensity()) {
      description = "Density of selected items (" + requestResource.getLabel() + " of items in "
          + requestResource.getUnit() + " per square kilometer) aggregated on the tag.";
    } else {
      description = "Total " + requestResource.getLabel() + " of items in "
          + requestResource.getUnit() + " aggregated on the tag.";
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
    SortedMap<OSMType, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String description = null;
    String requestURL = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    if (!rPs.isPost())
      requestURL = RequestInterceptor.requestUrl;
    mapRed = iP.processParameters(mapRed, rPs);
    switch (requestResource) {
      case COUNT:
        result = mapRed.aggregateByTimestamp()
            .aggregateBy((SerializableFunction<OSMEntitySnapshot, OSMType>) f -> {
              return f.getEntity().getType();
            }).zerofillIndices(iP.getOsmTypes()).count();
        break;
      case AREA:
        result = mapRed.aggregateByTimestamp()
            .aggregateBy((SerializableFunction<OSMEntitySnapshot, OSMType>) f -> {
              return f.getEntity().getType();
            }).zerofillIndices(iP.getOsmTypes())
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
              return Geo.areaOf(snapshot.getGeometry());
            });
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
        break;
      default:
        // should never reach this as requestResource is hard-coded in method call
        break;
    }
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    GeometryBuilder geomBuilder = iP.getGeomBuilder();
    Geometry geom = exeUtils.getGeometry(iP.getBoundaryType(), geomBuilder);
    int count = 0;
    int innerCount = 0;
    for (Entry<OSMType, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      ElementsResult[] results = new ElementsResult[entry.getValue().entrySet().size()];
      innerCount = 0;
      for (Entry<OSHDBTimestamp, ? extends Number> innerEntry : entry.getValue().entrySet()) {
        if (rPs.isDensity())
          results[innerCount] = new ElementsResult(
              TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()), Double.parseDouble(
                  df.format((innerEntry.getValue().doubleValue() / (Geo.areaOf(geom) / 1000000)))));
        else
          results[innerCount] =
              new ElementsResult(TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
                  Double.parseDouble(df.format(innerEntry.getValue().doubleValue())));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    if (rPs.isDensity()) {
      description = "Density of selected items (" + requestResource.getLabel() + " of items in "
          + requestResource.getUnit() + " per square kilometer) aggregated on the type.";
    } else {
      description = "Total " + requestResource.getLabel() + " of items in "
          + requestResource.getUnit() + " aggregated on the type.";
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
    String description = null;
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
    // group by key logic
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
    switch (requestResource) {
      case COUNT:
        result = preResult.count();
        break;
      case LENGTH:
        result = preResult.sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          return Geo.lengthOf(snapshot.getGeometry());
        });
        break;
      case PERIMETER:
        result = preResult.sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          if (snapshot.getGeometry() instanceof Polygonal)
            return Geo.lengthOf(snapshot.getGeometry().getBoundary());
          else
            return 0.0;
        });
        break;
      case AREA:
        result = preResult.sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          return Geo.areaOf(snapshot.getGeometry());
        });
        break;
    }
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    int count = 0;
    int innerCount = 0;
    for (Entry<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      ElementsResult[] results = new ElementsResult[entry.getValue().entrySet().size()];
      innerCount = 0;
      // check for non-remainder objects (which do have the defined key)
      if (entry.getKey() != -1) {
        groupByName = tt.getOSMTagKeyOf(entry.getKey().intValue()).toString();
      } else {
        groupByName = "remainder";
      }
      for (Entry<OSHDBTimestamp, ? extends Number> innerEntry : entry.getValue().entrySet()) {
        results[innerCount] =
            new ElementsResult(TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
                Double.parseDouble(df.format(innerEntry.getValue().doubleValue())));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    description = "Total " + requestResource.getLabel() + " of items in "
        + requestResource.getUnit() + " aggregated on the key.";
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
   * Performs a count|length|perimeter|area-share calculation.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCountShare(String, String, String, String[], String[], String[], String[], String[], String, String[], String[])
   * getCountShare} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (COUNT, LENGTH, PERIMETER,
   *        AREA).
   * @param rPs <code>RequestParameters</code> object, which holds those parameters that are used in
   *        every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   */
  public static ShareResponse executeCountLengthPerimeterAreaShare(RequestResource requestResource,
      RequestParameters rPs, String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();

    SortedMap<OSHDBTimestampAndIndex<Boolean>, ? extends Number> result = null;
    MapAggregatorByTimestampAndIndex<Boolean, OSMEntitySnapshot> preResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String description = null;
    String requestURL = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    TagTranslator tt = DbConnData.tagTranslator;
    iP.checkKeysValues(keys2, values2, true);
    values2 = iP.createEmptyArrayIfNull(values2);
    Integer[] keysInt2 = new Integer[keys2.length];
    Integer[] valuesInt2 = new Integer[values2.length];
    if (!rPs.isPost())
      requestURL = RequestInterceptor.requestUrl;
    for (int i = 0; i < keys2.length; i++) {
      keysInt2[i] = tt.getOSHDBTagKeyOf(keys2[i]).toInt();
      if (values2 != null && i < values2.length)
        valuesInt2[i] = tt.getOSHDBTagOf(keys2[i], values2[i]).getValue();
    }
    mapRed = iP.processParameters(mapRed, rPs);
    preResult = mapRed.aggregateByTimestamp().aggregateBy(f -> {
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
    }).zerofillIndices(Arrays.asList(true, false));
    switch (requestResource) {
      case COUNT:
        result = preResult.count();
        break;
      case LENGTH:
        result = preResult.sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          return Geo.lengthOf(snapshot.getGeometry());
        });
        break;
      case PERIMETER:
        result = preResult.sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          if (snapshot.getGeometry() instanceof Polygonal)
            return Geo.lengthOf(snapshot.getGeometry().getBoundary());
          else
            return 0.0;
        });
        break;
      case AREA:
        result = preResult.sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          return Geo.areaOf(snapshot.getGeometry());
        });
        break;
    }
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
    for (Entry<OSHDBTimestampAndIndex<Boolean>, ? extends Number> entry : result.entrySet()) {
      // this time array counts for each entry in the entrySet
      noPartTimeArray[timeCount] =
          TimestampFormatter.getInstance().isoDateTime(entry.getKey().getTimeIndex());
      if (entry.getKey().getOtherIndex()) {
        timeArray[partCount] =
            TimestampFormatter.getInstance().isoDateTime(entry.getKey().getTimeIndex());
        part[partCount] = Double.parseDouble(df.format(entry.getValue().doubleValue()));
        if (whole[partCount] == null || whole[partCount] == -1)
          whole[partCount] = Double.parseDouble(df.format(entry.getValue().doubleValue()));
        else
          whole[partCount] =
              whole[partCount] + Double.parseDouble(df.format(entry.getValue().doubleValue()));
        partCount++;
      } else {
        // else - set/increase only whole
        if (whole[wholeCount] == null || whole[wholeCount] == -1)
          whole[wholeCount] = Double.parseDouble(df.format(entry.getValue().doubleValue()));
        else
          whole[wholeCount] =
              whole[partCount] + Double.parseDouble(df.format(entry.getValue().doubleValue()));
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
    description = "Total " + requestResource.getLabel()
        + " of the whole and of a share of items in " + requestResource.getUnit()
        + " satisfying keys2 and values2 within items selected by types, keys, values.";
    Metadata metadata = null;
    if (iP.getShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, description, requestURL);
    }
    ShareResponse response =
        new ShareResponse(new Attribution(url, text), Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count|length|perimeter|area-share calculation grouped by the boundary.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCountShare(String, String, String, String[], String[], String[], String[], String[], String, String[], String[])
   * getCountShare} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (COUNT, LENGTH, PERIMETER,
   *        AREA).
   * @param rPs <code>RequestParameters</code> object, which holds those parameters that are used in
   *        every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.ShareGroupByBoundaryResponse
   *         ShareGroupByBoundaryResponse}
   */
  public static ShareGroupByBoundaryResponse executeCountLengthPerimeterAreaShareGroupByBoundary(
      RequestResource requestResource, RequestParameters rPs, String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    SortedMap<OSHDBTimestampAndIndex<Pair<Integer, Boolean>>, ? extends Number> result = null;
    SortedMap<Pair<Integer, Boolean>, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String description = null;
    String requestURL = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    TagTranslator tt = DbConnData.tagTranslator;
    iP.checkKeysValues(keys2, values2, true);
    values2 = iP.createEmptyArrayIfNull(values2);
    Integer[] keysInt2 = new Integer[keys2.length];
    Integer[] valuesInt2 = new Integer[values2.length];
    if (!rPs.isPost())
      requestURL = RequestInterceptor.requestUrl;
    for (int i = 0; i < keys2.length; i++) {
      keysInt2[i] = tt.getOSHDBTagKeyOf(keys2[i]).toInt();
      if (values2 != null && i < values2.length)
        valuesInt2[i] = tt.getOSHDBTagOf(keys2[i], values2[i]).getValue();
    }
    mapRed = iP.processParameters(mapRed, rPs);
    GeometryBuilder geomBuilder = iP.getGeomBuilder();
    Utils utils = iP.getUtils();
    result = exeUtils.computeCountLengthPerimeterAreaShareGBB(requestResource, iP.getBoundaryType(),
        mapRed, keysInt2, valuesInt2, geomBuilder);
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    ShareGroupByResult[] groupByResultSet = new ShareGroupByResult[groupByResult.size() / 2];
    String groupByName = "";
    String[] boundaryIds = utils.getBoundaryIds();
    Double[] whole = null;
    Double[] part = null;
    String[] timeArray = null;
    int count = 1;
    int gBNCount = 0;
    for (Entry<Pair<Integer, Boolean>, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      // on boundary param aggregated values (2x the same param)
      if (count == 1)
        timeArray = new String[entry.getValue().entrySet().size()];
      if (entry.getKey().getRight()) {
        // on true aggregated values
        part = new Double[entry.getValue().entrySet().size()];
        int partCount = 0;
        for (Entry<OSHDBTimestamp, ? extends Number> innerEntry : entry.getValue().entrySet()) {
          part[partCount] = Double.parseDouble(df.format(innerEntry.getValue().doubleValue()));
          partCount++;
        }
      } else {
        // on false aggregated values
        whole = new Double[entry.getValue().entrySet().size()];
        int wholeCount = 0;
        for (Entry<OSHDBTimestamp, ? extends Number> innerEntry : entry.getValue().entrySet()) {
          whole[wholeCount] = Double.parseDouble(df.format(innerEntry.getValue().doubleValue()));
          if (count == 1)
            timeArray[wholeCount] = innerEntry.getKey().toString();
          wholeCount++;
        }
      }
      if (count % 2 == 0) {
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
    description = "Total " + requestResource.getLabel()
        + " of the whole and of a share of items in " + requestResource.getUnit()
        + " satisfying keys2 and values2 within items selected by types, keys, values, aggregated on the boundary.";
    Metadata metadata = null;
    if (iP.getShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, description, requestURL);
    }
    ShareGroupByBoundaryResponse response = new ShareGroupByBoundaryResponse(
        new Attribution(url, text), Application.apiVersion, metadata, groupByResultSet);
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
  public static RatioResponse executeCountLengthPerimeterAreaRatio(RequestResource requestResource,
      RequestParameters rPs, String[] types2, String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    SortedMap<OSHDBTimestampAndIndex<MatchType>, ? extends Number> result = null;
    MapAggregatorByTimestampAndIndex<MatchType, OSMEntitySnapshot> preResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String description = null;
    String requestURL = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    DecimalFormat ratioDf = exeUtils.defineDecimalFormat("#.######");
    TagTranslator tt = DbConnData.tagTranslator;
    rPs = iP.fillWithEmptyIfNull(rPs);
    // for input processing/checking only
    iP.processParameters(mapRed, rPs);
    iP.checkKeysValues(keys2, values2, false);
    values2 = iP.createEmptyArrayIfNull(values2);
    keys2 = iP.createEmptyArrayIfNull(keys2);
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
    switch (requestResource) {
      case COUNT:
        result = preResult.count();
        break;
      case LENGTH:
        result = preResult.sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          return Geo.lengthOf(snapshot.getGeometry());
        });
        break;
      case PERIMETER:
        result = preResult.sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          if (snapshot.getGeometry() instanceof Polygonal)
            return Geo.lengthOf(snapshot.getGeometry().getBoundary());
          else
            return 0.0;
        });
        break;
      case AREA:
        result = preResult.sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          return Geo.areaOf(snapshot.getGeometry());
        });
        break;
    }
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
    RatioResult[] resultSet = new RatioResult[timeArray.length];
    for (int i = 0; i < timeArray.length; i++) {
      double ratio = value2[i] / value1[i];
      // in case ratio has the values "NaN", "Infinity", etc.
      try {
        ratio = Double.parseDouble(ratioDf.format(ratio));
      } catch (Exception e) {
        // do nothing --> just return ratio without rounding (trimming)
      }
      resultSet[i] = new RatioResult(timeArray[i], value1[i], value2[i], ratio);
    }
    description = "Total " + requestResource.getLabel() + " of items in "
        + requestResource.getUnit()
        + " satisfying types2, keys2, values2 parameters (= value2 output) "
        + "within items selected by types, keys, values parameters (= value output) and ratio of value2:value.";
    Metadata metadata = null;
    if (iP.getShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, description, requestURL);
    }
    RatioResponse response =
        new RatioResponse(new Attribution(url, text), Application.apiVersion, metadata, resultSet);
    return response;
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
  public static RatioGroupByBoundaryResponse executeCountLengthPerimeterAreaRatioGroupByBoundary(
      RequestResource requestResource, RequestParameters rPs, String[] types2, String[] keys2,
      String[] values2) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    MapAggregator<OSHDBTimestampAndIndex<Pair<Integer, MatchType>>, Geometry> preResult = null;
    SortedMap<OSHDBTimestampAndIndex<Pair<Integer, MatchType>>, ? extends Number> result = null;
    SortedMap<Pair<Integer, MatchType>, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String description = null;
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
    iP.checkKeysValues(keys2, values2, false);
    values2 = iP.createEmptyArrayIfNull(values2);
    keys2 = iP.createEmptyArrayIfNull(keys2);
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
    RatioGroupByResult[] groupByResultSet = new RatioGroupByResult[groupByResult.size() / 3];
    String groupByName = "";
    String[] boundaryIds = utils.getBoundaryIds();
    Double[] value1 = null;
    Double[] value2 = null;
    String[] timeArray = null;
    boolean timeArrayFilled = false;
    int count = 1;
    int gBNCount = 0;
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
        groupByName = boundaryIds[gBNCount];
        RatioResult[] resultSet = new RatioResult[timeArray.length];
        for (int i = 0; i < timeArray.length; i++) {
          double ratio = value2[i] / value1[i];
          // in case ratio has the values "NaN", "Infinity", etc.
          try {
            ratio = Double.parseDouble(ratioDf.format(ratio));
          } catch (Exception e) {
            // do nothing --> just return ratio without rounding (trimming)
          }
          resultSet[i] = new RatioResult(timeArray[i], value1[i], value2[i], ratio);
        }
        groupByResultSet[gBNCount] = new RatioGroupByResult(groupByName, resultSet);
        gBNCount++;
      }
      count++;
    }
    description = "Total " + requestResource.getLabel() + " of items in "
        + requestResource.getUnit()
        + " satisfying types2, keys2, values2 parameters (= value2 output) within items"
        + " selected by types, keys, values parameters (= value output) and ratio of value2:value grouped on the boundary objects.";

    Metadata metadata = null;
    if (iP.getShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, description, requestURL);
    }
    RatioGroupByBoundaryResponse response = new RatioGroupByBoundaryResponse(
        new Attribution(url, text), Application.apiVersion, metadata, groupByResultSet);
    return response;
  }

}
