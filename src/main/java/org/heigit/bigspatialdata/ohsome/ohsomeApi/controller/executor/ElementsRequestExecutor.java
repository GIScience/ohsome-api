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
        result = exeUtils.computeCountLengthPerimeterAreaGBBResult(RequestResource.COUNT,
            iP.getBoundaryType(), mapRed, iP.getGeomBuilder());
        break;
      case LENGTH:
        result = exeUtils.computeCountLengthPerimeterAreaGBBResult(RequestResource.LENGTH,
            iP.getBoundaryType(), mapRed, iP.getGeomBuilder());
        break;
      case PERIMETER:
        result = exeUtils.computeCountLengthPerimeterAreaGBBResult(RequestResource.PERIMETER,
            iP.getBoundaryType(), mapRed, iP.getGeomBuilder());
        break;
      case AREA:
        result = exeUtils.computeCountLengthPerimeterAreaGBBResult(RequestResource.AREA,
            iP.getBoundaryType(), mapRed, iP.getGeomBuilder());
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
   * Performs a count|length|perimeter|area-ratio calculation.
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
    SortedMap<OSHDBTimestamp, ? extends Number> result1 = null;
    SortedMap<OSHDBTimestamp, ? extends Number> result2 = null;
    MapReducer<OSMEntitySnapshot> mapRed1 = null;
    MapReducer<OSMEntitySnapshot> mapRed2 = null;
    InputProcessor iP = new InputProcessor();
    String description = null;
    String requestURL = null;
    DecimalFormat ratioDf = exeUtils.defineDecimalFormat("#.######");
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    RequestParameters rPs2 = new RequestParameters(rPs.isPost(), rPs.isSnapshot(), rPs.isDensity(),
        rPs.getBboxes(), rPs.getBcircles(), rPs.getBpolys(), types2, keys2, values2,
        rPs.getUserids(), rPs.getTime(), rPs.getShowMetadata());
    if (!rPs.isPost())
      requestURL = RequestInterceptor.requestUrl;
    mapRed1 = iP.processParameters(mapRed1, rPs);
    mapRed2 = iP.processParameters(mapRed2, rPs2);
    switch (requestResource) {
      case COUNT:
        result1 = mapRed1.aggregateByTimestamp().count();
        result2 = mapRed2.aggregateByTimestamp().count();
        break;
      case AREA:
        result1 = mapRed1.aggregateByTimestamp()
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
              return Geo.areaOf(snapshot.getGeometry());
            });
        result2 = mapRed2.aggregateByTimestamp()
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
              return Geo.areaOf(snapshot.getGeometry());
            });
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
        break;
    }
    ElementsResult[] resultSet1 = new ElementsResult[result1.size()];
    int count = 0;
    for (Entry<OSHDBTimestamp, ? extends Number> entry : result1.entrySet()) {
      resultSet1[count] =
          new ElementsResult(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
              entry.getValue().doubleValue());
      count++;
    }
    RatioResult[] resultSet = new RatioResult[result1.size()];
    count = 0;
    for (Entry<OSHDBTimestamp, ? extends Number> entry : result2.entrySet()) {
      String date = resultSet1[count].getTimestamp();
      double ratio = (entry.getValue().doubleValue() / resultSet1[count].getValue());
      // in case ratio has the value "NaN", "Infinity", etc.
      try {
        ratio = Double.parseDouble(ratioDf.format(ratio));
      } catch (Exception e) {
        // do nothing --> just return ratio without rounding (trimming)
      }
      resultSet[count] =
          new RatioResult(date, Double.parseDouble(df.format(resultSet1[count].getValue())),
              Double.parseDouble(df.format(entry.getValue().doubleValue())), ratio);
      count++;
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
   * Performs a count-ratio calculation grouped by the boundary.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCountRatio(String, String, String, String[], String[], String[], String[], String[], String, String[], String[], String[])
   * getCountRatio} method.
   * 
   * @param rPs <code>RequestParameters</code> object, which holds those parameters that are used in
   *        every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResponse
   *         GroupByResponse Content}
   */
  public static RatioGroupByBoundaryResponse executeCountRatioGroupByBoundary(RequestParameters rPs,
      String[] types2, String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    SortedMap<OSHDBTimestampAndIndex<Integer>, ? extends Number> result1;
    SortedMap<OSHDBTimestampAndIndex<Integer>, ? extends Number> result2;
    MapReducer<OSMEntitySnapshot> mapRed1 = null;
    MapReducer<OSMEntitySnapshot> mapRed2 = null;
    SortedMap<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult1;
    SortedMap<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult2;
    InputProcessor iP = new InputProcessor();
    String requestURL = null;
    DecimalFormat ratioDF = exeUtils.defineDecimalFormat("#.######");
    RequestParameters rPs2 = new RequestParameters(rPs.isPost(), rPs.isSnapshot(), rPs.isDensity(),
        rPs.getBboxes(), rPs.getBcircles(), rPs.getBpolys(), types2, keys2, values2,
        rPs.getUserids(), rPs.getTime(), rPs.getShowMetadata());
    if (!rPs.isPost())
      requestURL = RequestInterceptor.requestUrl;
    mapRed1 = iP.processParameters(mapRed1, rPs);
    result1 = exeUtils.computeCountLengthPerimeterAreaGBBResult(RequestResource.COUNT,
        iP.getBoundaryType(), mapRed1, iP.getGeomBuilder());
    mapRed2 = iP.processParameters(mapRed2, rPs2);
    result2 = exeUtils.computeCountLengthPerimeterAreaGBBResult(RequestResource.COUNT,
        iP.getBoundaryType(), mapRed2, iP.getGeomBuilder());
    groupByResult1 = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result1);
    groupByResult2 = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result2);
    GroupByResult[] resultSet = new GroupByResult[groupByResult1.size()];
    RatioGroupByResult[] ratioResultSet = new RatioGroupByResult[groupByResult1.size()];
    String groupByName = "";
    Utils utils = iP.getUtils();
    String[] boundaryIds = utils.getBoundaryIds();
    int count = 0;
    int innerCount = 0;
    for (Entry<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult1
        .entrySet()) {
      ElementsResult[] results = new ElementsResult[entry.getValue().entrySet().size()];
      innerCount = 0;
      groupByName = boundaryIds[count];
      for (Entry<OSHDBTimestamp, ? extends Number> innerEntry : entry.getValue().entrySet()) {
        results[innerCount] =
            new ElementsResult(TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
                innerEntry.getValue().intValue());
        innerCount++;
      }
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    count = 0;
    innerCount = 0;
    for (Entry<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult2
        .entrySet()) {
      RatioResult[] ratioResults = new RatioResult[entry.getValue().entrySet().size()];
      innerCount = 0;
      groupByName = boundaryIds[count];
      for (Entry<OSHDBTimestamp, ? extends Number> innerEntry : entry.getValue().entrySet()) {
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
    values2 = exeUtils.shareParamEvaluation(keys2, values2);
    SortedMap<OSHDBTimestampAndIndex<Boolean>, ? extends Number> result = null;
    MapAggregatorByTimestampAndIndex<Boolean, OSMEntitySnapshot> preResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String description = null;
    String requestURL = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    TagTranslator tt = DbConnData.tagTranslator;
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
    values2 = exeUtils.shareParamEvaluation(keys2, values2);
    SortedMap<OSHDBTimestampAndIndex<Pair<Integer, Boolean>>, ? extends Number> result = null;
    SortedMap<Pair<Integer, Boolean>, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String description = null;
    String requestURL = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    TagTranslator tt = DbConnData.tagTranslator;
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
    result = exeUtils.computeCountLengthPerimeterAreaShareGBBResult(requestResource,
        iP.getBoundaryType(), mapRed, keysInt2, valuesInt2, geomBuilder);
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

}
