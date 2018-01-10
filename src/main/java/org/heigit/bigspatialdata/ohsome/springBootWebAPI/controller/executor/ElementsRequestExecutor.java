package org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.executor;

import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.Map.Entry;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.Application;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.eventHolder.EventHolderBean;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.inputValidation.InputValidator;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.interceptor.ElementsRequestInterceptor;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.MetaData;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.GroupByResult;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.Result;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDB_H2;
import org.heigit.bigspatialdata.oshdb.api.generic.OSHDBTimestampAndOtherIndex;
import org.heigit.bigspatialdata.oshdb.api.generic.function.SerializableFunction;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapBiAggregatorByTimestamps;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.object.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.api.utils.OSHDBTimestamp;
import org.heigit.bigspatialdata.oshdb.osm.OSMType;
import org.heigit.bigspatialdata.oshdb.util.Geo;
import org.heigit.bigspatialdata.oshdb.util.TagTranslator;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygonal;

/**
 * Includes all execute methods for requests mapped to /elements.
 */
public class ElementsRequestExecutor {
 
  /**
   * Gets the input parameters of the request and performs a count calculation.
   */
  public ElementsResponseContent executeCount(boolean isPost, String[] bboxes, String[] bpoints,
      String[] bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Integer> result;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputValidator iV = new InputValidator();
    String requestURL = null;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    // input parameter processing
    mapRed =
        iV.processParameters(isPost, bboxes, bpoints, bpolys, types, keys, values, userids, time);
    // db result
    result = mapRed.aggregateByTimestamp().count();
    // output
    Result[] resultSet = new Result[result.size()];
    int count = 0;
    for (Entry<OSHDBTimestamp, Integer> entry : result.entrySet()) {
      resultSet[count] = new Result(entry.getKey().formatIsoDateTime(),
          String.valueOf(entry.getValue().intValue()));
      count++;
    }
    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response = new ElementsResponseContent(
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
        new MetaData(duration, "amount",
            "Total number of elements, which are selected by the parameters.", requestURL),
        null, resultSet);

    return response;
  }

  /**
   * Gets the input parameters of the request and performs a count grouped by type.
   */
  public ElementsResponseContent executeCountGroupByType(boolean isPost, String[] bboxes,
      String[] bpoints, String[] bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestampAndOtherIndex<OSMType>, Integer> result;
    SortedMap<OSMType, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputValidator iV = new InputValidator();
    String requestURL = null;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    // input parameter processing
    mapRed =
        iV.processParameters(isPost, bboxes, bpoints, bpolys, types, keys, values, userids, time);
    // db result
    result = mapRed.aggregateByTimestamp()
        .aggregateBy((SerializableFunction<OSMEntitySnapshot, OSMType>) f -> {
          return f.getEntity().getType();
        }).count();
    groupByResult = MapBiAggregatorByTimestamps.nest_IndexThenTime(result);
    // output
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    int count = 0;
    int innerCount = 0;
    // iterate over the entry objects aggregated by user
    for (Entry<OSMType, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult.entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      innerCount = 0;
      // iterate over the inner entry objects containing timestamp-value pairs
      for (Entry<OSHDBTimestamp, Integer> innerEntry : entry.getValue().entrySet()) {
        results[innerCount] = new Result(innerEntry.getKey().formatIsoDateTime(),
            String.valueOf(innerEntry.getValue()));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response = new ElementsResponseContent(
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
        new MetaData(duration, "amount", "Total number of items aggregated on the userids.",
            requestURL),
        resultSet, null);
    return response;
  }

  /**
   * Gets the input parameters of the request and performs a count grouped by boundary.
   */
  public ElementsResponseContent executeCountGroupByBoundary(boolean isPost, String[] bboxes,
      String[] bpoints, String[] bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    ArrayList<SortedMap<OSHDBTimestamp, Integer>> reqResults =
        new ArrayList<SortedMap<OSHDBTimestamp, Integer>>();
    String requestURL = null;
    String boundaryDescr;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    if (bboxes != null) {
      boundaryDescr = "bounding box ";
      for (int i = 0; i < bboxes.length; i += 4) {
        InputValidator iV = new InputValidator();
        SortedMap<OSHDBTimestamp, Integer> result;
        MapReducer<OSMEntitySnapshot> mapRed;
        // extraction of the bbox
        String[] bbox = new String[4];
        bbox[0] = bboxes[i];
        bbox[1] = bboxes[i + 1];
        bbox[2] = bboxes[i + 2];
        bbox[3] = bboxes[i + 3];
        // input parameter processing
        mapRed =
            iV.processParameters(isPost, bbox, bpoints, bpolys, types, keys, values, userids, time);
        // db result
        result = mapRed.aggregateByTimestamp().count();
        // add it to the array and increase counter
        reqResults.add(result);
      }
    } else if (bpoints != null) {
      boundaryDescr = "bounding point ";
      // bpoints given
    } else if (bpolys != null) {
      boundaryDescr = "bounding polygon ";
      // bpolys given
    } else {
      boundaryDescr = "whole dataset ";
      // no boundary --> default bbox == whole data
    }
    // output
    GroupByResult[] resultSet = new GroupByResult[reqResults.size()];
    int count = 1;
    int innerCount = 0;
    // iterate over each result
    for (SortedMap<OSHDBTimestamp, Integer> map : reqResults) {
      Result[] results = new Result[map.size()];
      innerCount = 0;
      // iterate over each entry in the map containing timestamp-value pairs
      for (Entry<OSHDBTimestamp, Integer> entry : map.entrySet()) {
        results[innerCount] =
            new Result(entry.getKey().formatIsoDateTime(), String.valueOf(entry.getValue()));
        innerCount++;
      }
      resultSet[count - 1] = new GroupByResult(boundaryDescr + String.valueOf(count), results);
      count++;
    }
    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response = new ElementsResponseContent(
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
        new MetaData(duration, "amount",
            "Total number of items aggregated on the bounding objects.", requestURL),
        resultSet, null);
    return response;
  }

  private ElementsResponseContent executeCountGroupByBoundaryyy(boolean isPost, String[] bboxes,
      String[] bpoints, String[] bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Integer> result;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputValidator iV = new InputValidator();
    String requestURL = null;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    // input parameter processing
    mapRed =
        iV.processParameters(isPost, bboxes, bpoints, bpolys, types, keys, values, userids, time);
    // db result
    result = mapRed.aggregateByTimestamp().count();


    // output
    Result[] resultSet = new Result[result.size()];
    int count = 0;
    for (Entry<OSHDBTimestamp, Integer> entry : result.entrySet()) {
      resultSet[count] = new Result(entry.getKey().formatIsoDateTime(),
          String.valueOf(entry.getValue().intValue()));
      count++;
    }



    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response = new ElementsResponseContent(
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
        new MetaData(duration, "amount",
            "Total number of elements, which are selected by the parameters.", requestURL),
        null, resultSet);
    return response;
  }

  /**
   * Gets the input parameters of the request and performs a count grouped by user.
   */
  public ElementsResponseContent executeCountGroupByUser(boolean isPost, String[] bboxes,
      String[] bpoints, String[] bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestampAndOtherIndex<Integer>, Integer> result;
    SortedMap<Integer, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputValidator iV = new InputValidator();
    String requestURL = null;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    // input parameter processing
    mapRed =
        iV.processParameters(isPost, bboxes, bpoints, bpolys, types, keys, values, userids, time);
    // db result
    result = mapRed.aggregateByTimestamp()
        .aggregateBy((SerializableFunction<OSMEntitySnapshot, Integer>) f -> {
          return f.getEntity().getUserId();
        }).count();
    groupByResult = MapBiAggregatorByTimestamps.nest_IndexThenTime(result);
    // output
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    int count = 0;
    int innerCount = 0;
    // iterate over the entry objects aggregated by user
    for (Entry<Integer, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult.entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      innerCount = 0;
      // iterate over the inner entry objects containing timestamp-value pairs
      for (Entry<OSHDBTimestamp, Integer> innerEntry : entry.getValue().entrySet()) {
        results[innerCount] = new Result(innerEntry.getKey().formatIsoDateTime(),
            String.valueOf(innerEntry.getValue()));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response = new ElementsResponseContent(
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
        new MetaData(duration, "amount", "Total number of items aggregated on the userids.",
            requestURL),
        resultSet, null);
    return response;
  }

  /**
   * Gets the input parameters of the request and performs a count grouped by tag.
   */
  public ElementsResponseContent executeCountGroupByTag(boolean isPost, String[] bboxes,
      String[] bpoints, String[] bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time, String[] groupByKey, String[] groupByValues)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestampAndOtherIndex<ImmutablePair<Integer, Integer>>, Integer> result;
    SortedMap<ImmutablePair<Integer, Integer>, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputValidator iV = new InputValidator();
    String requestURL = null;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    // needed to get access to the keytables
    EventHolderBean bean = Application.getEventHolderBean();
    OSHDB_H2[] dbConnObjects = bean.getDbConnObjects();
    TagTranslator tt = new TagTranslator(dbConnObjects[1].getConnection());
    Integer[] keysInt = new Integer[groupByKey.length];
    Integer[] valuesInt = new Integer[groupByValues.length];
    if (groupByKey == null || groupByKey.length == 0) {
      throw new BadRequestException(
          "You need to give one groupByKey parameters, if you want to use groupBy/tag");
    }
    // input parameter processing
    mapRed =
        iV.processParameters(isPost, bboxes, bpoints, bpolys, types, keys, values, userids, time);
    // get the integer values for the given keys
    for (int i = 0; i < groupByKey.length; i++) {
      keysInt[i] = tt.key2Int(groupByKey[i]);
      if (groupByValues != null) {
        // get the integer values for the given values
        for (int j = 0; j < groupByValues.length; j++) {
          valuesInt[j] = tt.tag2Int(groupByKey[i], groupByValues[j]).getValue();
        }
      }
    }
    // group by tag logic
    result = mapRed.map(f -> {
      int[] tags = f.getEntity().getTags();
      for (int i = 0; i < tags.length; i += 2) {
        int tagKeyId = tags[i];
        int tagValueId = tags[i + 1];
        for (int key : keysInt) {
          // if key in input key list
          if (tagKeyId == key) {
            if (valuesInt.length == 0) {
              return new ImmutablePair<>(new ImmutablePair<Integer, Integer>(tagKeyId, tagValueId),
                  f);
            }
            for (int value : valuesInt) {
              // if value in input value list
              if (tagValueId == value)
                return new ImmutablePair<>(
                    new ImmutablePair<Integer, Integer>(tagKeyId, tagValueId), f);
            }
          }
        }
      }
      return new ImmutablePair<>(new ImmutablePair<Integer, Integer>(-1, -1), f);
    }).aggregateByTimestamp().aggregateBy(Pair::getKey).map(Pair::getValue).count();

    groupByResult = MapBiAggregatorByTimestamps.nest_IndexThenTime(result);

    // output
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    int count = 0;
    int innerCount = 0;
    // iterate over the entry objects aggregated by tags
    for (Entry<ImmutablePair<Integer, Integer>, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult
        .entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      innerCount = 0;
      // check for non-remainder objects (which do have the defined key/tag)
      if (entry.getKey().getKey() != -1 && entry.getKey().getValue() != -1) {
        groupByName = tt.tag2String(entry.getKey()).getValue();
      } else {
        groupByName = "remainder";
      }
      // iterate over the inner entry objects containing timestamp-value pairs
      for (Entry<OSHDBTimestamp, Integer> innerEntry : entry.getValue().entrySet()) {
        results[innerCount] = new Result(innerEntry.getKey().formatIsoDateTime(),
            String.valueOf(innerEntry.getValue()));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response = new ElementsResponseContent(
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
        new MetaData(duration, "amount", "Total number of items aggregated on the tag.",
            requestURL),
        resultSet, null);
    return response;
  }

  /**
   * Gets the input parameters of the request and performs a count grouped by key.
   */
  public ElementsResponseContent executeCountGroupByKey(boolean isPost, String[] bboxes,
      String[] bpoints, String[] bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time, String[] groupByKey)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestampAndOtherIndex<Integer>, Integer> result;
    SortedMap<Integer, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputValidator iV = new InputValidator();
    String requestURL = null;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    // needed to get access to the keytables
    EventHolderBean bean = Application.getEventHolderBean();
    OSHDB_H2[] dbConnObjects = bean.getDbConnObjects();
    TagTranslator tt = new TagTranslator(dbConnObjects[1].getConnection());
    Integer[] keysInt = new Integer[groupByKey.length];
    if (groupByKey == null || groupByKey.length == 0) {
      throw new BadRequestException(
          "You need to give one groupByKey parameters, if you want to use groupBy/key");
    }
    // input parameter processing
    mapRed =
        iV.processParameters(isPost, bboxes, bpoints, bpolys, types, keys, values, userids, time);
    // get the integer values for the given keys
    for (int i = 0; i < groupByKey.length; i++) {
      keysInt[i] = tt.key2Int(groupByKey[i]);
    }
    // group by key logic
    result = mapRed.map(f -> {
      int[] tags = f.getEntity().getTags();
      for (int i = 0; i < tags.length; i += 2) {
        int tagKeyId = tags[i];
        for (int key : keysInt) {
          // if key in input key list
          if (tagKeyId == key) {
              return new ImmutablePair<>(tagKeyId, f);
          }
        }
      }
      return new ImmutablePair<>(-1, f);
    }).aggregateByTimestamp().aggregateBy(Pair::getKey).count();

    groupByResult = MapBiAggregatorByTimestamps.nest_IndexThenTime(result);

    // output
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    int count = 0;
    int innerCount = 0;
    // iterate over the entry objects aggregated by keys
    for (Entry<Integer, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult
        .entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      innerCount = 0;
      // check for non-remainder objects (which do have the defined key)
      if (entry.getKey() != -1) {
        groupByName = tt.key2String(entry.getKey());
      } else {
        groupByName = "remainder";
      }
      // iterate over the inner entry objects containing timestamp-value pairs
      for (Entry<OSHDBTimestamp, Integer> innerEntry : entry.getValue().entrySet()) {
        results[innerCount] = new Result(innerEntry.getKey().formatIsoDateTime(),
            String.valueOf(innerEntry.getValue()));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response = new ElementsResponseContent(
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
        new MetaData(duration, "amount", "Total number of items aggregated on the tag.",
            requestURL),
        resultSet, null);
    return response;
  }
  
  /**
   * Gets the input parameters of the request and performs a length or area calculation.
   * 
   * @param isArea <code>Boolean</code> defining an area (true) or a length (false) request.
   */
  public ElementsResponseContent executeLengthArea(boolean isArea, boolean isPost, String[] bboxes,
      String[] bpoints, String[] bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Number> result;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputValidator iV = new InputValidator();
    String unit;
    String description;
    String requestURL = null;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;

    // input parameter processing
    mapRed =
        iV.processParameters(isPost, bboxes, bpoints, bpolys, types, keys, values, userids, time);
    // db result
    result = mapRed.aggregateByTimestamp()
        .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          if (isArea) {
            return Geo.areaOf(snapshot.getGeometry());
          } else {
            return Geo.lengthOf(snapshot.getGeometry());
          }
        });
    // output
    Result[] resultSet = new Result[result.size()];
    int count = 0;
    for (Map.Entry<OSHDBTimestamp, Number> entry : result.entrySet()) {
      resultSet[count] = new Result(entry.getKey().formatIsoDateTime(),
          String.valueOf(entry.getValue().floatValue()));
      count++;
    }
    if (isArea) {
      unit = "square-meter";
      description = "Total area of polygons.";
    } else {
      unit = "meter";
      description = "Total length of lines.";
    }
    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response = new ElementsResponseContent(
        "-Hier könnte Ihre Lizenz stehen.-", "-Hier könnte Ihr Copyright stehen.-",
        new MetaData(duration, unit, description, requestURL), null, resultSet);
    return response;
  }

  /**
   * Gets the input parameters of the request and performs a perimeter calculation.
   */
  public ElementsResponseContent executePerimeter(boolean isPost, String[] bboxes,
      String[] bpoints, String[] bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Number> result;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputValidator iV = new InputValidator();
    String requestURL = null;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;

    // input parameter processing
    mapRed =
        iV.processParameters(isPost, bboxes, bpoints, bpolys, types, keys, values, userids, time);
    // db result
    result = mapRed.aggregateByTimestamp()
        .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          // checks if the geometry is polygonal (needed for OSM relations, which are not polygonal)
          if (snapshot.getGeometry() instanceof Polygonal)
            return Geo.lengthOf(snapshot.getGeometry().getBoundary());
          else
            return 0.0;
        });
    // output
    Result[] resultSet = new Result[result.size()];
    int count = 0;
    for (Map.Entry<OSHDBTimestamp, Number> entry : result.entrySet()) {
      resultSet[count] = new Result(entry.getKey().formatIsoDateTime(),
          String.valueOf(entry.getValue().floatValue()));
      count++;
    }
    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response =
        new ElementsResponseContent("-Hier könnte Ihre Lizenz stehen.-",
            "-Hier könnte Ihr Copyright stehen.-", new MetaData(duration, "meters",
                "Total length of the perimeter (polygon boundaries)", requestURL),
            null, resultSet);
    return response;
  }

  /**
   * Gets the input parameters of the request and computes the length, perimeter, or area results
   * grouped by the tag.
   * 
   * @param requestType <code>Byte</code> defining a length (1), perimeter (2), or area (3) request.
   */
  public ElementsResponseContent executeLengthPerimeterAreaGroupByTag(byte requestType,
      boolean isPost, String[] bboxes, String[] bpoints, String[] bpolys, String[] types,
      String[] keys, String[] values, String[] userids, String[] time, String[] groupByKey,
      String[] groupByValues) throws UnsupportedOperationException, Exception {
    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestampAndOtherIndex<ImmutablePair<Integer, Integer>>, Number> result;
    SortedMap<ImmutablePair<Integer, Integer>, SortedMap<OSHDBTimestamp, Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputValidator iV = new InputValidator();
    String unit = "";
    String description = "";
    String requestURL = null;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    // needed to get access to the keytables
    EventHolderBean bean = Application.getEventHolderBean();
    OSHDB_H2[] dbConnObjects = bean.getDbConnObjects();
    TagTranslator tt = new TagTranslator(dbConnObjects[1].getConnection());
    Integer[] keysInt = new Integer[groupByKey.length];
    Integer[] valuesInt = new Integer[groupByValues.length];
    if (groupByKey == null || groupByKey.length == 0) {
      throw new BadRequestException(
          "You need to give one groupByKey parameters, if you want to use groupBy/tag");
    }
    // input parameter processing
    mapRed =
        iV.processParameters(isPost, bboxes, bpoints, bpolys, types, keys, values, userids, time);
    // get the integer values for the given keys
    for (int i = 0; i < groupByKey.length; i++) {
      keysInt[i] = tt.key2Int(groupByKey[i]);
      if (groupByValues != null) {
        // get the integer values for the given values
        for (int j = 0; j < groupByValues.length; j++) {
          valuesInt[j] = tt.tag2Int(groupByKey[i], groupByValues[j]).getValue();
        }
      }
    }
    // group by tag logic
    result = mapRed.map(f -> {
      int[] tags = f.getEntity().getTags();
      for (int i = 0; i < tags.length; i += 2) {
        int tagKeyId = tags[i];
        int tagValueId = tags[i + 1];
        for (int key : keysInt) {
          // if key in input key list
          if (tagKeyId == key) {
            if (valuesInt.length == 0) {
              return new ImmutablePair<>(new ImmutablePair<Integer, Integer>(tagKeyId, tagValueId),
                  f);
            }
            for (int value : valuesInt) {
              // if value in input value list
              if (tagValueId == value)
                return new ImmutablePair<>(
                    new ImmutablePair<Integer, Integer>(tagKeyId, tagValueId), f);
            }
          }
        }
      }
      return new ImmutablePair<>(new ImmutablePair<Integer, Integer>(-1, -1), f);
    }).aggregateByTimestamp().aggregateBy(Pair::getKey).map(Pair::getValue)
        .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          switch (requestType) {
            case 1:
              return Geo.lengthOf(snapshot.getGeometry());
            case 2:
              if (snapshot.getGeometry() instanceof Polygonal)
                return Geo.lengthOf(snapshot.getGeometry().getBoundary());
              else
                return 0.0;
            case 3:
              return Geo.areaOf(snapshot.getGeometry());
            default:
              return 0.0;
          }
        });;

    groupByResult = MapBiAggregatorByTimestamps.nest_IndexThenTime(result);

    // output
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    int count = 0;
    int innerCount = 0;
    // iterate over the entry objects aggregated by tags
    for (Entry<ImmutablePair<Integer, Integer>, SortedMap<OSHDBTimestamp, Number>> entry : groupByResult
        .entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      innerCount = 0;
      // check for non-remainder objects (which do have the defined key/tag)
      if (entry.getKey().getKey() != -1 && entry.getKey().getValue() != -1) {
        groupByName = tt.tag2String(entry.getKey()).getValue();
      } else {
        groupByName = "remainder";
      }
      // iterate over the inner entry objects containing timestamp-value pairs
      for (Entry<OSHDBTimestamp, Number> innerEntry : entry.getValue().entrySet()) {
        results[innerCount] = new Result(innerEntry.getKey().formatIsoDateTime(),
            String.valueOf(innerEntry.getValue()));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    // setting of the unit and description output parameters
    switch (requestType) {
      case 1:
        unit = "meter";
        description = "Total length of items aggregated on the tag.";
        break;
      case 2:
        unit = "meter";
        description = "Total perimeter of polygonal items aggregated on the tag.";
        break;
      case 3:
        unit = "square-meter";
        description = "Total area of items aggregated on the tag.";
        break;
    }
    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response = new ElementsResponseContent(
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
        new MetaData(duration, unit, description, requestURL), resultSet, null);
    return response;
  }

  /**
   * Gets the input parameters of the request and computes the length, perimeter, or area results
   * grouped by the user.
   * 
   * @param requestType <code>Byte</code> defining a length (1), perimeter (2), or area (3) request.
   */
  public ElementsResponseContent executeLengthPerimeterAreaGroupByUser(byte requestType,
      boolean isPost, String[] bboxes, String[] bpoints, String[] bpolys, String[] types,
      String[] keys, String[] values, String[] userids, String[] time)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestampAndOtherIndex<Integer>, Number> result;
    SortedMap<Integer, SortedMap<OSHDBTimestamp, Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputValidator iV = new InputValidator();
    String unit = "";
    String description = "";
    String requestURL = null;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    // input parameter processing
    mapRed =
        iV.processParameters(isPost, bboxes, bpoints, bpolys, types, keys, values, userids, time);
    // db result
    result = mapRed.aggregateByTimestamp()
        .aggregateBy((SerializableFunction<OSMEntitySnapshot, Integer>) f -> {
          return f.getEntity().getUserId();
        }).sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          switch (requestType) {
            case 1:
              return Geo.lengthOf(snapshot.getGeometry());
            case 2:
              if (snapshot.getGeometry() instanceof Polygonal)
                return Geo.lengthOf(snapshot.getGeometry().getBoundary());
              else
                return 0.0;
            case 3:
              return Geo.areaOf(snapshot.getGeometry());
            default:
              return 0.0;
          }
        });
    groupByResult = MapBiAggregatorByTimestamps.nest_IndexThenTime(result);
    // output
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    int count = 0;
    int innerCount = 0;
    // iterate over the entry objects aggregated by type
    for (Entry<Integer, SortedMap<OSHDBTimestamp, Number>> entry : groupByResult.entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      innerCount = 0;
      // iterate over the inner entry objects containing timestamp-value pairs
      for (Entry<OSHDBTimestamp, Number> innerEntry : entry.getValue().entrySet()) {
        results[innerCount] = new Result(innerEntry.getKey().formatIsoDateTime(),
            String.valueOf(innerEntry.getValue().floatValue()));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    // setting of the unit and description output parameters
    switch (requestType) {
      case 1:
        unit = "meter";
        description = "Total length of items aggregated on the userid.";
        break;
      case 2:
        unit = "meter";
        description = "Total perimeter of polygonal items aggregated on the userid.";
        break;
      case 3:
        unit = "square-meter";
        description = "Total area of items aggregated on the userid.";
        break;
    }
    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response = new ElementsResponseContent(
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
        new MetaData(duration, unit, description, requestURL), resultSet, null);
    return response;
  }

  /**
   * Gets the input parameters of the request and computes the area, or the perimeter grouped by the
   * OSM type.
   * 
   * @param isArea <code>Boolean</code> defining an area (true) or a length (false) request.
   */
  public ElementsResponseContent executeAreaPerimeterGroupByType(boolean isArea, boolean isPost,
      String[] bboxes, String[] bpoints, String[] bpolys, String[] types, String[] keys,
      String[] values, String[] userids, String[] time)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestampAndOtherIndex<OSMType>, Number> result;
    SortedMap<OSMType, SortedMap<OSHDBTimestamp, Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputValidator iV = new InputValidator();
    String unit;
    String description;
    String requestURL = null;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    // input parameter processing
    mapRed =
        iV.processParameters(isPost, bboxes, bpoints, bpolys, types, keys, values, userids, time);
    // db result
    result = mapRed.aggregateByTimestamp()
        .aggregateBy((SerializableFunction<OSMEntitySnapshot, OSMType>) f -> {
          return f.getEntity().getType();
        }).sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          if (isArea) {
            return Geo.areaOf(snapshot.getGeometry());
          } else {
            if (snapshot.getGeometry() instanceof Polygonal)
              return Geo.lengthOf(snapshot.getGeometry().getBoundary());
            else
              return 0.0;
          }
        });
    groupByResult = MapBiAggregatorByTimestamps.nest_IndexThenTime(result);
    // output
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    int count = 0;
    int innerCount = 0;
    // iterate over the entry objects aggregated by type
    for (Entry<OSMType, SortedMap<OSHDBTimestamp, Number>> entry : groupByResult.entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      innerCount = 0;
      // iterate over the inner entry objects containing timestamp-value pairs
      for (Entry<OSHDBTimestamp, Number> innerEntry : entry.getValue().entrySet()) {
        results[innerCount] = new Result(innerEntry.getKey().formatIsoDateTime(),
            String.valueOf(innerEntry.getValue().floatValue()));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    // setting of the unit and description output parameters
    if (isArea) {
      unit = "square-meter";
      description = "Total area of items aggregated on the OSM type.";
    } else {
      unit = "meter";
      description = "Total perimeter of items aggregated on the OSM type.";
    }
    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response = new ElementsResponseContent(
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
        new MetaData(duration, unit, description, requestURL), resultSet, null);
    return response;
  }

  /**
   * Gets the input parameters of the request and performs a density calculation.
   */
  public ElementsResponseContent executeDensity(boolean isPost, String[] bboxes, String[] bpoints,
      String[] bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Integer> countResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputValidator iV = new InputValidator();
    String requestURL = null;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    // input parameter processing
    mapRed =
        iV.processParameters(isPost, bboxes, bpoints, bpolys, types, keys, values, userids, time);
    // count result
    countResult = mapRed.aggregateByTimestamp().count();
    int count = 0;
    Result[] countResultSet = new Result[countResult.size()];
    for (Entry<OSHDBTimestamp, Integer> entry : countResult.entrySet()) {
      countResultSet[count] = new Result(entry.getKey().formatIsoDateTime(),
          String.valueOf(entry.getValue().intValue()));
      count++;
    }
    // geometry
    Geometry geom = null;
    switch (iV.getBoundary()) {
      case 0:
        geom = iV.getBbox().getGeometry();
        break;
      case 1:
        geom = iV.getBbox().getGeometry();
        break;
      case 2:
        geom = iV.getBpoint();
        break;
      case 3:
        geom = iV.getBpoly();
        break;
    }
    // output
    Result[] resultSet = new Result[countResult.size()];
    for (int i = 0; i < resultSet.length; i++) {
      // gets the timestamp and the results from count and divides it through the area
      String date = countResultSet[i].getTimestamp();
      String value = String
          .valueOf((Float.parseFloat(countResultSet[i].getValue()) / (Geo.areaOf(geom) / 1000000)));
      resultSet[i] = new Result(date, value);
    }
    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response =
        new ElementsResponseContent("-Hier könnte Ihre Lizenz stehen.-",
            "-Hier könnte Ihr Copyright stehen.-",
            new MetaData(duration, "items per square-kilometer",
                "Density of selected items (number of items per area).", requestURL),
            null, resultSet);
    return response;
  }
  
  /**
   * Gets the input parameters of the request and performs a ratio calculation.
   */
  public ElementsResponseContent executeRatio(boolean isPost, String[] bboxes, String[] bpoints,
      String[] bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String[] types2, String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Integer> result1;
    SortedMap<OSHDBTimestamp, Integer> result2;
    MapReducer<OSMEntitySnapshot> mapRed1;
    MapReducer<OSMEntitySnapshot> mapRed2;
    InputValidator iV = new InputValidator();
    String requestURL = null;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    // input parameter processing 1 and result 1
    mapRed1 =
        iV.processParameters(isPost, bboxes, bpoints, bpolys, types, keys, values, userids, time);
    result1 = mapRed1.aggregateByTimestamp().count();
    // input parameter processing 2 and result 2
    mapRed2 = iV.processParameters(isPost, bboxes, bpoints, bpolys, types2, keys2, values2, userids,
        time);
    result2 = mapRed2.aggregateByTimestamp().count();
    // resultSet 1
    Result[] resultSet1 = new Result[result1.size()];
    int count = 0;
    for (Entry<OSHDBTimestamp, Integer> entry : result1.entrySet()) {
      resultSet1[count] = new Result(entry.getKey().formatIsoDateTime(),
          String.valueOf(entry.getValue().intValue()));
      count++;
    }
    // output
    Result[] resultSet = new Result[result1.size()];
    count = 0;
    for (Entry<OSHDBTimestamp, Integer> entry : result2.entrySet()) {
      // gets the timestamp and the results from both counts and divides 2 through 1
      String date = resultSet1[count].getTimestamp();
      String value = String
          .valueOf(entry.getValue().floatValue() / Float.parseFloat(resultSet1[count].getValue()));
      resultSet[count] = new Result(date, value);
      count++;
    }
    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response = new ElementsResponseContent(
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
        new MetaData(duration, "ratio",
            "Ratio of items satisfying types2, keys2, values2 within items are selected by types, keys, values.",
            requestURL),
        null, resultSet);
    return response;
  }

}
