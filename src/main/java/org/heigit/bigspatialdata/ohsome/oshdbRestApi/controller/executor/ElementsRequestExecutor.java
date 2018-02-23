package org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.executor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.inputProcessing.BoundaryType;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.inputProcessing.GeometryBuilder;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.inputProcessing.InputProcessor;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.inputProcessing.Utils;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.interceptor.ElementsRequestInterceptor;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponse;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByBoundaryResponse;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByKeyResponse;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByTagResponse;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByTypeResponse;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByUserResponse;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.RatioResponse;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.ShareGroupByBoundaryResponse;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.ShareResponse;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.metadata.GroupByBoundaryMetadata;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.metadata.Metadata;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result.GroupByResult;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result.RatioResult;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result.Result;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result.ShareGroupByResult;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result.ShareResult;
import org.heigit.bigspatialdata.oshdb.api.generic.OSHDBTimestampAndIndex;
import org.heigit.bigspatialdata.oshdb.api.generic.function.SerializableFunction;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregatorByTimestampAndIndex;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.object.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.osm.OSMEntity;
import org.heigit.bigspatialdata.oshdb.osm.OSMType;
import org.heigit.bigspatialdata.oshdb.util.OSHDBTag;
import org.heigit.bigspatialdata.oshdb.util.OSHDBTimestamp;
import org.heigit.bigspatialdata.oshdb.util.geometry.Geo;
import org.heigit.bigspatialdata.oshdb.util.geometry.OSHDBGeometryBuilder;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;
import org.heigit.bigspatialdata.oshdb.util.time.TimestampFormatter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygonal;

/** Includes all execute methods for requests mapped to /elements. */
public class ElementsRequestExecutor {

  private final String license = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,";
  private final String copyright =
      "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.";

  /**
   * Performs a count calculation.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         ElementsResponseContent}
   * @throws UnsupportedOperationException by
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer#aggregateByTimestamp()
   *         aggregateByTimestamp()}
   * @throws BadRequestException by
   *         {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.inputProcessing.InputProcessor#processParameters(boolean, String, String, String, String[], String[], String[], String[], String[], String)
   *         processParameters()}
   * @throws Exception by
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#count() count()}
   */
  public DefaultAggregationResponse executeCount(boolean isPost, String bboxes, String bcircles,
      String bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata)
      throws UnsupportedOperationException, BadRequestException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Integer> result;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputProcessor iP = new InputProcessor();
    String requestURL = null;
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    mapRed = iP.processParameters(isPost, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata);
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
      metadata = new Metadata(duration, "amount",
          "Total number of elements, which are selected by the parameters.", requestURL);
    }
    DefaultAggregationResponse response =
        new DefaultAggregationResponse(license, copyright, metadata, resultSet);

    return response;
  }

  /**
   * Performs a count calculation grouped by the type.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByTypeResponse
   *         GroupByTypeResponseContent}
   */
  public GroupByTypeResponse executeCountGroupByType(boolean isPost, String bboxes, String bcircles,
      String bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestampAndIndex<OSMType>, Integer> result;
    SortedMap<OSMType, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputProcessor iP = new InputProcessor();
    String requestURL = null;
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    mapRed = iP.processParameters(isPost, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata);
    // db result
    result = mapRed.aggregateByTimestamp()
        .aggregateBy((SerializableFunction<OSMEntitySnapshot, OSMType>) f -> {
          return f.getEntity().getType();
        }).zerofillIndices(iP.getOsmTypes()).count();
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    int count = 0;
    int innerCount = 0;
    // iterate over the entry objects aggregated by user
    for (Entry<OSMType, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult.entrySet()) {
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
      metadata = new Metadata(duration, "amount", "Total number of items aggregated on the type.",
          requestURL);
    }
    GroupByTypeResponse response = new GroupByTypeResponse(license, copyright, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count calculation grouped by the boundary.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByBoundaryResponse
   *         GroupByBoundaryResponseContent}
   */
  public GroupByBoundaryResponse executeCountGroupByBoundary(boolean isPost, String bboxes,
      String bcircles, String bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time, String showMetadata)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestampAndIndex<Integer>, Integer> result = null;
    SortedMap<Integer, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputProcessor iP = new InputProcessor();
    String requestURL = null;
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    mapRed = iP.processParameters(isPost, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata);
    GeometryBuilder geomBuilder = iP.getGeomBuilder();
    Utils utils = iP.getUtils();
    switch (iP.getBoundaryType()) {
      case NOBOUNDARY:
        throw new BadRequestException(
            "You need to give at least one boundary parameter if you want to use /groupBy/boundary.");
      case BBOXES:
        ArrayList<Geometry> bboxGeoms = geomBuilder.getGeometry(BoundaryType.BBOXES);
        ArrayList<Integer> zeroBboxFill = new ArrayList<Integer>();
        for (int j = 0; j < bboxGeoms.size(); j++)
          zeroBboxFill.add(j);
        result = mapRed.aggregateByTimestamp().flatMap(f -> {
          List<Integer> bboxesList = new LinkedList<>();
          for (int i = 0; i < bboxGeoms.size(); i++)
            if (f.getGeometry().intersects(bboxGeoms.get(i)))
              bboxesList.add(i);
          return bboxesList;
        }).aggregateBy(f -> f).zerofillIndices(zeroBboxFill).count();
        break;
      case BCIRCLES:
        ArrayList<Geometry> bcircleGeoms = geomBuilder.getGeometry(BoundaryType.BCIRCLES);
        ArrayList<Integer> zerobcircleFill = new ArrayList<Integer>();
        for (int j = 0; j < bcircleGeoms.size(); j++)
          zerobcircleFill.add(j);
        result = mapRed.aggregateByTimestamp().flatMap(f -> {
          List<Integer> bcirclesList = new LinkedList<>();
          for (int i = 0; i < bcircleGeoms.size(); i++)
            if (f.getGeometry().intersects(bcircleGeoms.get(i)))
              bcirclesList.add(i);
          return bcirclesList;
        }).aggregateBy(f -> f).zerofillIndices(zerobcircleFill).count();
        break;
      case BPOLYS:
        ArrayList<Geometry> bpolyGeoms = geomBuilder.getGeometry(BoundaryType.BPOLYS);
        ArrayList<Integer> zeroBpolyFill = new ArrayList<Integer>();
        for (int j = 0; j < bpolyGeoms.size(); j++)
          zeroBpolyFill.add(j);
        result = mapRed.aggregateByTimestamp().flatMap(f -> {
          List<Integer> bpolysList = new LinkedList<>();
          for (int i = 0; i < bpolyGeoms.size(); i++)
            if (f.getGeometry().intersects(bpolyGeoms.get(i)))
              bpolysList.add(i);
          return bpolysList;
        }).aggregateBy(f -> f).zerofillIndices(zeroBpolyFill).count();
        break;
    }
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
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
    GroupByBoundaryMetadata gBBMetadata = null;
    if (iP.getShowMetadata()) {
      Map<String, double[]> boundaries = new HashMap<String, double[]>();
      switch (iP.getBoundaryType()) {
        case NOBOUNDARY:
          double[] singleBboxValues = new double[4];
          for (int i = 0; i < 4; i++)
            singleBboxValues[i] = Double.parseDouble(iP.getBoundaryValues()[i]);
          boundaries.put(boundaryIds[0], singleBboxValues);
          break;
        case BBOXES:
          int bboxCount = 0;
          for (int i = 0; i < iP.getBoundaryValues().length; i += 4) {
            double[] bboxValues = new double[4];
            for (int j = 0; j < 4; j++)
              bboxValues[j] = Double.parseDouble(iP.getBoundaryValues()[i + j]);
            boundaries.put(boundaryIds[bboxCount], bboxValues);
            bboxCount++;
          }
          break;
        case BCIRCLES:
          int bcircleCount = 0;
          for (int i = 0; i < iP.getBoundaryValues().length; i += 3) {
            double[] bcircleValues = new double[3];
            for (int j = 0; j < 3; j++)
              bcircleValues[j] = Double.parseDouble(iP.getBoundaryValues()[i + j]);
            boundaries.put(boundaryIds[bcircleCount], bcircleValues);
            bcircleCount++;
          }
          break;
        case BPOLYS:
          // TODO implement for bpolys (should be done together with WKT implementation)
          boundaries = null;
          break;
      }
      long duration = System.currentTimeMillis() - startTime;
      gBBMetadata = new GroupByBoundaryMetadata(duration, "amount", boundaries,
          "Total number of items aggregated on the boundary object.", requestURL);
    }

    GroupByBoundaryResponse response =
        new GroupByBoundaryResponse(license, copyright, gBBMetadata, resultSet);
    return response;
  }

  /**
   * Performs a count calculation grouped by the key.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCountGroupByKey(String, String, String, String[], String[], String[], String[], String[], String, String[])
   * getCountGroupByKey} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByKeyResponse
   *         GroupByKeyResponseContent}
   */
  public GroupByKeyResponse executeCountGroupByKey(boolean isPost, String bboxes, String bcircles,
      String bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata, String[] groupByKeys)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    if (groupByKeys == null || groupByKeys.length == 0) {
      throw new BadRequestException(
          "You need to give one groupByKey parameters, if you want to use groupBy/key");
    }
    SortedMap<OSHDBTimestampAndIndex<Integer>, Integer> result;
    SortedMap<Integer, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputProcessor iP = new InputProcessor();
    ExecutionUtils exeUtils = new ExecutionUtils();
    String requestURL = null;
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    TagTranslator tt = exeUtils.createTagTranslator();
    Integer[] keysInt = new Integer[groupByKeys.length];
    mapRed = iP.processParameters(isPost, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata);
    for (int i = 0; i < groupByKeys.length; i++) {
      keysInt[i] = tt.oshdbTagKeyOf(groupByKeys[i]).toInt();
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
        groupByName = tt.osmTagKeyOf(entry.getKey().intValue()).toString();
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
      metadata = new Metadata(duration, "amount", "Total number of items aggregated on the key.",
          requestURL);
    }
    GroupByKeyResponse response = new GroupByKeyResponse(license, copyright, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count calculation grouped by the tag.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByTagResponse
   *         GroupByTagResponseContent}
   */
  public GroupByTagResponse executeCountGroupByTag(boolean isPost, String bboxes, String bcircles,
      String bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata, String[] groupByKey, String[] groupByValues)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    if (groupByKey.length != 1)
      throw new BadRequestException("There has to be one groupByKey value given.");
    if (groupByValues == null)
      groupByValues = new String[0];
    SortedMap<OSHDBTimestampAndIndex<Pair<Integer, Integer>>, Integer> result;
    SortedMap<Pair<Integer, Integer>, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputProcessor iP = new InputProcessor();
    ExecutionUtils exeUtils = new ExecutionUtils();
    String requestURL = null;
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    TagTranslator tt = exeUtils.createTagTranslator();
    Integer[] valuesInt = new Integer[groupByValues.length];
    ArrayList<Pair<Integer, Integer>> zeroFill = new ArrayList<Pair<Integer, Integer>>();
    mapRed = iP.processParameters(isPost, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata);
    int keysInt = tt.oshdbTagKeyOf(groupByKey[0]).toInt();
    if (groupByValues.length != 0) {
      for (int j = 0; j < groupByValues.length; j++) {
        valuesInt[j] = tt.oshdbTagOf(groupByKey[0], groupByValues[j]).getValue();
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
    // iterate over the entry objects aggregated by tags
    for (Entry<Pair<Integer, Integer>, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult
        .entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      int innerCount = 0;
      // check for non-remainder objects (which do have the defined key and value)
      if (entry.getKey().getKey() != -1 && entry.getKey().getValue() != -1)
        groupByName = tt.osmTagOf(keysInt, entry.getKey().getValue()).toString();
      else
        groupByName = "remainder";
      // iterate over the timestamp-value pairs
      for (Entry<OSHDBTimestamp, Integer> innerEntry : entry.getValue().entrySet()) {
        results[innerCount] =
            new Result(TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
                innerEntry.getValue().doubleValue());
        innerCount++;
      }
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      metadata = new Metadata(duration, "amount", "Total number of items aggregated on the tag.",
          requestURL);
    }
    GroupByTagResponse response = new GroupByTagResponse(license, copyright, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count calculation grouped by the user.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByUserResponse
   *         GroupByUserResponseContent}
   */
  public GroupByUserResponse executeCountGroupByUser(boolean isPost, String bboxes, String bcircles,
      String bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata) throws UnsupportedOperationException, Exception {
    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestampAndIndex<Integer>, Integer> result;
    SortedMap<Integer, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputProcessor iP = new InputProcessor();
    String requestURL = null;
    ArrayList<Integer> useridsInt = new ArrayList<Integer>();
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    mapRed = iP.processParameters(isPost, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata);
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
      metadata = new Metadata(duration, "amount",
          "Total number of items aggregated on the userids.", requestURL);
    }
    GroupByUserResponse response = new GroupByUserResponse(license, copyright, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count-share calculation.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCountShare(String, String, String, String[], String[], String[], String[], String[], String, String[], String[])
   * getCountShare} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         ElementsResponseContent}
   */
  public ShareResponse executeCountShare(boolean isPost, String bboxes, String bcircles,
      String bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata, String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    values2 = exeUtils.shareParamEvaluation(keys2, values2);
    SortedMap<OSHDBTimestampAndIndex<Boolean>, Integer> result;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputProcessor iP = new InputProcessor();
    String requestURL = null;
    TagTranslator tt = exeUtils.createTagTranslator();
    Integer[] keysInt2 = new Integer[keys2.length];
    Integer[] valuesInt2 = new Integer[values2.length];
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    for (int i = 0; i < keys2.length; i++) {
      keysInt2[i] = tt.oshdbTagKeyOf(keys2[i]).toInt();
      if (values2 != null && i < values2.length) {
        valuesInt2[i] = tt.oshdbTagOf(keys2[i], values2[i]).getValue();
      }
    }
    mapRed = iP.processParameters(isPost, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata);
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
      metadata = new Metadata(duration, "amount",
          "Share of items satisfying keys2 and values2 within items selected by types, keys, values.",
          requestURL);
    }
    ShareResponse response = new ShareResponse(license, copyright, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count-share calculation grouped by the boundary.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCountShare(String, String, String, String[], String[], String[], String[], String[], String, String[], String[])
   * getCountShare} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         ElementsResponseContent}
   */
  public ShareGroupByBoundaryResponse executeCountShareGroupByBoundary(boolean isPost,
      String bboxes, String bcircles, String bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time, String showMetadata, String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    values2 = exeUtils.shareParamEvaluation(keys2, values2);
    SortedMap<OSHDBTimestampAndIndex<Pair<Integer, Boolean>>, Integer> result = null;
    SortedMap<Pair<Integer, Boolean>, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputProcessor iP = new InputProcessor();
    String requestURL = null;
    TagTranslator tt = exeUtils.createTagTranslator();
    Integer[] keysInt2 = new Integer[keys2.length];
    Integer[] valuesInt2 = new Integer[values2.length];
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    for (int i = 0; i < keys2.length; i++) {
      keysInt2[i] = tt.oshdbTagKeyOf(keys2[i]).toInt();
      if (values2 != null && i < values2.length)
        valuesInt2[i] = tt.oshdbTagOf(keys2[i], values2[i]).getValue();
    }
    mapRed = iP.processParameters(isPost, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata);
    GeometryBuilder geomBuilder = iP.getGeomBuilder();
    Utils utils = iP.getUtils();
    switch (iP.getBoundaryType()) {
      case NOBOUNDARY:
        throw new BadRequestException(
            "You need to give at least one boundary parameter if you want to use /groupBy/boundary.");
      case BBOXES:
        ArrayList<Geometry> bboxGeoms = geomBuilder.getGeometry(BoundaryType.BBOXES);
        ArrayList<Pair<Integer, Boolean>> zeroBboxFill = new ArrayList<>();
        for (int j = 0; j < bboxGeoms.size(); j++) {
          zeroBboxFill.add(new ImmutablePair<>(j, true));
          zeroBboxFill.add(new ImmutablePair<>(j, false));
        }
        result = mapRed.aggregateByTimestamp().flatMap(f -> {
          List<Pair<Integer, OSMEntity>> bboxesList = new LinkedList<>();
          for (int i = 0; i < bboxGeoms.size(); i++)
            if (f.getGeometry().intersects(bboxGeoms.get(i)))
              bboxesList.add(new ImmutablePair<>(i, f.getEntity()));
          return bboxesList;
        }).aggregateBy(f -> {
          // result aggregated on true (if obj contains all tags) and false (if not all are
          // contained)
          boolean hasTags = false;
          for (int i = 0; i < keysInt2.length; i++) {
            if (f.getRight().hasTagKey(keysInt2[i])) {
              if (i >= valuesInt2.length) {
                // if more keys2 than values2 are given
                hasTags = true;
                continue;
              }
              if (f.getRight().hasTagValue(keysInt2[i], valuesInt2[i])) {
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
          return new ImmutablePair<>(f.getLeft(), hasTags);
        }).zerofillIndices(zeroBboxFill).count();
        break;
      case BCIRCLES:
        ArrayList<Geometry> bcircleGeoms = geomBuilder.getGeometry(BoundaryType.BCIRCLES);
        ArrayList<Pair<Integer, Boolean>> zeroBcircleFill = new ArrayList<>();
        for (int j = 0; j < bcircleGeoms.size(); j++) {
          zeroBcircleFill.add(new ImmutablePair<>(j, true));
          zeroBcircleFill.add(new ImmutablePair<>(j, false));
        }
        result = mapRed.aggregateByTimestamp().flatMap(f -> {
          List<Pair<Integer, OSMEntity>> bcirclesList = new LinkedList<>();
          for (int i = 0; i < bcircleGeoms.size(); i++)
            if (f.getGeometry().intersects(bcircleGeoms.get(i)))
              bcirclesList.add(new ImmutablePair<>(i, f.getEntity()));
          return bcirclesList;
        }).aggregateBy(f -> {
          // result aggregated on true (if obj contains all tags) and false (if not all are
          // contained)
          boolean hasTags = false;
          for (int i = 0; i < keysInt2.length; i++) {
            if (f.getRight().hasTagKey(keysInt2[i])) {
              if (i >= valuesInt2.length) {
                // if more keys2 than values2 are given
                hasTags = true;
                continue;
              }
              if (f.getRight().hasTagValue(keysInt2[i], valuesInt2[i])) {
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
          return new ImmutablePair<>(f.getLeft(), hasTags);
        }).zerofillIndices(zeroBcircleFill).count();
        break;
      case BPOLYS:
        ArrayList<Geometry> bpolyGeoms = geomBuilder.getGeometry(BoundaryType.BPOLYS);
        ArrayList<Pair<Integer, Boolean>> zeroBpolyFill = new ArrayList<>();
        for (int j = 0; j < bpolyGeoms.size(); j++) {
          zeroBpolyFill.add(new ImmutablePair<>(j, true));
          zeroBpolyFill.add(new ImmutablePair<>(j, false));
        }
        result = mapRed.aggregateByTimestamp().flatMap(f -> {
          List<Pair<Integer, OSMEntity>> bpolysList = new LinkedList<>();
          for (int i = 0; i < bpolyGeoms.size(); i++)
            if (f.getGeometry().intersects(bpolyGeoms.get(i)))
              bpolysList.add(new ImmutablePair<>(i, f.getEntity()));
          return bpolysList;
        }).aggregateBy(f -> {
          // result aggregated on true (if obj contains all tags) and false (if not all are
          // contained)
          boolean hasTags = false;
          for (int i = 0; i < keysInt2.length; i++) {
            if (f.getRight().hasTagKey(keysInt2[i])) {
              if (i >= valuesInt2.length) {
                // if more keys2 than values2 are given
                hasTags = true;
                continue;
              }
              if (f.getRight().hasTagValue(keysInt2[i], valuesInt2[i])) {
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
          return new ImmutablePair<>(f.getLeft(), hasTags);
        }).zerofillIndices(zeroBpolyFill).count();
        break;
    }
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    ShareGroupByResult[] groupByResultSet = new ShareGroupByResult[groupByResult.size() / 2];
    String groupByName = "";
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
    GroupByBoundaryMetadata gBBMetadata = null;
    if (iP.getShowMetadata()) {
      Map<String, double[]> boundaries = new HashMap<String, double[]>();
      switch (iP.getBoundaryType()) {
        case NOBOUNDARY:
          double[] singleBboxValues = new double[4];
          for (int i = 0; i < 4; i++)
            singleBboxValues[i] = Double.parseDouble(iP.getBoundaryValues()[i]);
          boundaries.put(boundaryIds[0], singleBboxValues);
          break;
        case BBOXES:
          int bboxCount = 0;
          for (int i = 0; i < iP.getBoundaryValues().length; i += 4) {
            double[] bboxValues = new double[4];
            for (int j = 0; j < 4; j++)
              bboxValues[j] = Double.parseDouble(iP.getBoundaryValues()[i + j]);
            boundaries.put(boundaryIds[bboxCount], bboxValues);
            bboxCount++;
          }
          break;
        case BCIRCLES:
          int bcircleCount = 0;
          for (int i = 0; i < iP.getBoundaryValues().length; i += 3) {
            double[] bcircleValues = new double[3];
            for (int j = 0; j < 3; j++)
              bcircleValues[j] = Double.parseDouble(iP.getBoundaryValues()[i + j]);
            boundaries.put(boundaryIds[bcircleCount], bcircleValues);
            bcircleCount++;
          }
          break;
        case BPOLYS:
          // TODO implement for bpolys (should be done together with WKT implementation)
          boundaries = null;
          break;
      }
      long duration = System.currentTimeMillis() - startTime;
      gBBMetadata = new GroupByBoundaryMetadata(duration, "amount", boundaries,
          "Share of items satisfying keys2 and values2 within items selected by types, keys, values, grouped on the boundary parameter.",
          requestURL);
    }
    ShareGroupByBoundaryResponse response =
        new ShareGroupByBoundaryResponse(license, copyright, gBBMetadata, groupByResultSet);
    return response;
  }

  /**
   * Performs a length or area calculation.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (LENGTH, PERIMETER, AREA).
   * @param isPost <code>Boolean</code> defining if this method is called from a POST (true) or a
   *        GET (false) request.
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         ElementsResponseContent}
   */
  public DefaultAggregationResponse executeLengthArea(RequestResource requestResource,
      boolean isPost, String bboxes, String bcircles, String bpolys, String[] types, String[] keys,
      String[] values, String[] userids, String[] time, String showMetadata)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Number> result;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputProcessor iP = new InputProcessor();
    ExecutionUtils exeUtils = new ExecutionUtils();
    String unit = null;
    String description = null;
    String requestURL = null;
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    mapRed = iP.processParameters(isPost, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata);
    result = mapRed.aggregateByTimestamp()
        .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          switch (requestResource) {
            case AREA:
              return Geo.areaOf(snapshot.getGeometry());
            case LENGTH:
              return Geo.lengthOf(snapshot.getGeometry());
            default:
              return null;
          }
        });
    Result[] resultSet = new Result[result.size()];
    DecimalFormat lengthPerimeterAreaDf = exeUtils.defineDecimalFormat("#.####");
    int count = 0;
    for (Map.Entry<OSHDBTimestamp, Number> entry : result.entrySet()) {
      resultSet[count] = new Result(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
          Double.parseDouble(lengthPerimeterAreaDf.format(entry.getValue().doubleValue())));
      count++;
    }
    switch (requestResource) {
      case AREA:
        unit = "square-meter";
        description = "Total area of polygons.";
        break;
      case LENGTH:
        unit = "meter";
        description = "Total length of lines.";
        break;
      default:
        // do nothing.. should never reach this :D
        break;
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      metadata = new Metadata(duration, unit, description, requestURL);
    }
    DefaultAggregationResponse response =
        new DefaultAggregationResponse(license, copyright, metadata, resultSet);
    return response;
  }

  /**
   * Performs a perimeter calculation.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param isPost <code>Boolean</code> defining if this method is called from a POST (true) or a
   *        GET (false) request.
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         ElementsResponseContent}
   */
  public DefaultAggregationResponse executePerimeter(boolean isPost, String bboxes, String bcircles,
      String bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Number> result;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputProcessor iP = new InputProcessor();
    ExecutionUtils exeUtils = new ExecutionUtils();
    String requestURL = null;
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    mapRed = iP.processParameters(isPost, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata);
    result = mapRed.aggregateByTimestamp()
        .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          // checks if the geometry is polygonal (needed for OSM relations, which are not polygonal)
          if (snapshot.getGeometry() instanceof Polygonal)
            return Geo.lengthOf(snapshot.getGeometry().getBoundary());
          else
            return 0.0;
        });
    Result[] resultSet = new Result[result.size()];
    DecimalFormat lengthPerimeterAreaDf = exeUtils.defineDecimalFormat("#.####");
    int count = 0;
    for (Map.Entry<OSHDBTimestamp, Number> entry : result.entrySet()) {
      resultSet[count] = new Result(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
          Double.parseDouble(lengthPerimeterAreaDf.format(entry.getValue().doubleValue())));
      count++;
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      metadata =
          new Metadata(duration, "meters", "Total perimeter of polygonal items.", requestURL);
    }
    DefaultAggregationResponse response =
        new DefaultAggregationResponse(license, copyright, metadata, resultSet);
    return response;
  }

  /**
   * Performs a length, perimeter, or area calculation grouped by the key.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCountGroupByKey(String, String, String, String[], String[], String[], String[], String[], String, String[])
   * groupByKey} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (LENGTH, PERIMETER, AREA).
   * @param isPost <code>Boolean</code> defining if this method is called from a POST (true) or a
   *        GET (false) request.
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByKeyResponse
   *         GroupByKeyResponseContent}
   */
  public GroupByKeyResponse executeLengthPerimeterAreaGroupByKey(RequestResource requestResource,
      boolean isPost, String bboxes, String bcircles, String bpolys, String[] types, String[] keys,
      String[] values, String[] userids, String[] time, String showMetadata, String[] groupByKeys)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    if (groupByKeys == null || groupByKeys.length == 0)
      throw new BadRequestException(
          "You need to give one groupByKey parameters, if you want to use groupBy/tag");
    SortedMap<OSHDBTimestampAndIndex<Integer>, Number> result;
    SortedMap<Integer, SortedMap<OSHDBTimestamp, Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputProcessor iP = new InputProcessor();
    ExecutionUtils exeUtils = new ExecutionUtils();
    String unit = "";
    String description = "";
    String requestURL = null;
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    TagTranslator tt = exeUtils.createTagTranslator();
    Integer[] keysInt = new Integer[groupByKeys.length];
    mapRed = iP.processParameters(isPost, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata);
    for (int i = 0; i < groupByKeys.length; i++) {
      keysInt[i] = tt.oshdbTagKeyOf(groupByKeys[i]).toInt();
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
    DecimalFormat lengthPerimeterAreaDf = exeUtils.defineDecimalFormat("#.####");
    int count = 0;
    int innerCount = 0;
    // iterate over the entry objects aggregated by keys
    for (Entry<Integer, SortedMap<OSHDBTimestamp, Number>> entry : groupByResult.entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      innerCount = 0;
      // check for non-remainder objects (which do have the defined key)
      if (entry.getKey() != -1) {
        groupByName = tt.osmTagKeyOf(entry.getKey().intValue()).toString();
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
        unit = "meter";
        description = "Total length of items aggregated on the key.";
        break;
      case PERIMETER:
        unit = "meter";
        description = "Total perimeter of polygonal items aggregated on the key.";
        break;
      case AREA:
        unit = "square-meter";
        description = "Total area of items aggregated on the key.";
        break;
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      metadata = new Metadata(duration, unit, description, requestURL);
    }
    GroupByKeyResponse response = new GroupByKeyResponse(license, copyright, metadata, resultSet);
    return response;
  }

  /**
   * Performs a length, perimeter, or area calculation grouped by the tag.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCountGroupByTag(String, String, String, String[], String[], String[], String[], String[], String, String[], String[])
   * getCountGroupByTag} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (LENGTH, PERIMETER, AREA).
   * @param isPost <code>Boolean</code> defining if this method is called from a POST (true) or a
   *        GET (false) request.
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByTagResponse
   *         GroupByTagResponseContent}
   */
  public GroupByTagResponse executeLengthPerimeterAreaGroupByTag(RequestResource requestResource,
      boolean isPost, String bboxes, String bcircles, String bpolys, String[] types, String[] keys,
      String[] values, String[] userids, String[] time, String showMetadata, String[] groupByKey,
      String[] groupByValues) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    if (groupByKey == null || groupByKey.length == 0)
      throw new BadRequestException(
          "You need to give one groupByKey parameters, if you want to use groupBy/tag.");
    SortedMap<OSHDBTimestampAndIndex<Pair<Integer, Integer>>, Number> result;
    SortedMap<Pair<Integer, Integer>, SortedMap<OSHDBTimestamp, Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputProcessor iP = new InputProcessor();
    ExecutionUtils exeUtils = new ExecutionUtils();
    String unit = "";
    String description = "";
    String requestURL = null;
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    if (groupByValues == null)
      groupByValues = new String[0];
    TagTranslator tt = exeUtils.createTagTranslator();
    Integer[] valuesInt = new Integer[groupByValues.length];
    ArrayList<Pair<Integer, Integer>> zeroFill = new ArrayList<Pair<Integer, Integer>>();
    mapRed = iP.processParameters(isPost, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata);
    int keysInt = tt.oshdbTagKeyOf(groupByKey[0]).toInt();
    if (groupByValues.length != 0) {
      for (int j = 0; j < groupByValues.length; j++) {
        valuesInt[j] = tt.oshdbTagOf(groupByKey[0], groupByValues[j]).getValue();
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
    DecimalFormat lengthPerimeterAreaDf = exeUtils.defineDecimalFormat("#.####");
    int count = 0;
    ArrayList<String> resultTimestamps = new ArrayList<String>();
    // iterate over the entry objects aggregated by tags
    for (Entry<Pair<Integer, Integer>, SortedMap<OSHDBTimestamp, Number>> entry : groupByResult
        .entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      int innerCount = 0;
      // check for non-remainder objects (which do have the defined key and value)
      if (entry.getKey().getKey() != -1 && entry.getKey().getValue() != -1) {
        groupByName = tt.osmTagOf(keysInt, entry.getKey().getValue()).toString();
      } else {
        groupByName = "remainder";
      }
      // iterate over the timestamp-value pairs
      for (Entry<OSHDBTimestamp, Number> innerEntry : entry.getValue().entrySet()) {
        if (count == 0)
          // write the timestamps into an ArrayList for later usage
          resultTimestamps.add(TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()));
        results[innerCount] = new Result(
            TimestampFormatter.getInstance().isoDateTime(innerEntry.getKey()),
            Double.parseDouble(lengthPerimeterAreaDf.format(innerEntry.getValue().doubleValue())));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    // remove null objects in the resultSet
    resultSet = Arrays.stream(resultSet).filter(Objects::nonNull).toArray(GroupByResult[]::new);
    switch (requestResource) {
      case LENGTH:
        unit = "meter";
        description = "Total length of items aggregated on the tag.";
        break;
      case PERIMETER:
        unit = "meter";
        description = "Total perimeter of polygonal items aggregated on the tag.";
        break;
      case AREA:
        unit = "square-meter";
        description = "Total area of items aggregated on the tag.";
        break;
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      metadata = new Metadata(duration, unit, description, requestURL);
    }
    GroupByTagResponse response = new GroupByTagResponse(license, copyright, metadata, resultSet);
    return response;
  }

  /**
   * Performs a length, perimeter, or area calculation grouped by the user.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (LENGTH, PERIMETER, AREA).
   * @param isPost <code>Boolean</code> defining if this method is called from a POST (true) or a
   *        GET (false) request.
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByUserResponse
   *         GroupByUserResponseContent}
   */
  public GroupByUserResponse executeLengthPerimeterAreaGroupByUser(RequestResource requestResource,
      boolean isPost, String bboxes, String bcircles, String bpolys, String[] types, String[] keys,
      String[] values, String[] userids, String[] time, String showMetadata)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestampAndIndex<Integer>, Number> result;
    SortedMap<Integer, SortedMap<OSHDBTimestamp, Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputProcessor iP = new InputProcessor();
    ExecutionUtils exeUtils = new ExecutionUtils();
    String unit = "";
    String description = "";
    String requestURL = null;
    ArrayList<Integer> useridsInt = new ArrayList<Integer>();
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    mapRed = iP.processParameters(isPost, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata);
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
    DecimalFormat lengthPerimeterAreaDf = exeUtils.defineDecimalFormat("#.####");
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
        unit = "meter";
        description = "Total length of items aggregated on the userid.";
        break;
      case PERIMETER:
        unit = "meter";
        description = "Total perimeter of polygonal items aggregated on the userid.";
        break;
      case AREA:
        unit = "square-meter";
        description = "Total area of items aggregated on the userid.";
        break;
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      metadata = new Metadata(duration, unit, description, requestURL);
    }
    GroupByUserResponse response = new GroupByUserResponse(license, copyright, metadata, resultSet);
    return response;
  }

  /**
   * Performs a perimeter, or area calculation grouped by the OSM type.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (LENGTH, PERIMETER, AREA).
   * @param isPost <code>Boolean</code> defining if this method is called from a POST (true) or a
   *        GET (false) request.
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByTypeResponse
   *         GroupByTypeResponseContent}
   */
  public GroupByTypeResponse executeAreaPerimeterGroupByType(RequestResource requestResource,
      boolean isPost, String bboxes, String bcircles, String bpolys, String[] types, String[] keys,
      String[] values, String[] userids, String[] time, String showMetadata)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestampAndIndex<OSMType>, Number> result;
    SortedMap<OSMType, SortedMap<OSHDBTimestamp, Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputProcessor iP = new InputProcessor();
    ExecutionUtils exeUtils = new ExecutionUtils();
    String unit = null;
    String description = null;
    String requestURL = null;
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    mapRed = iP.processParameters(isPost, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata);
    result = mapRed.aggregateByTimestamp()
        .aggregateBy((SerializableFunction<OSMEntitySnapshot, OSMType>) f -> {
          return f.getEntity().getType();
        }).zerofillIndices(iP.getOsmTypes())
        .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          switch (requestResource) {
            case AREA:
              return Geo.areaOf(snapshot.getGeometry());
            case PERIMETER:
              if (snapshot.getGeometry() instanceof Polygonal)
                return Geo.lengthOf(snapshot.getGeometry().getBoundary());
              else
                return 0.0;
            default:
              return null;
          }
        });
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    DecimalFormat lengthPerimeterAreaDf = exeUtils.defineDecimalFormat("#.####");
    int count = 0;
    int innerCount = 0;
    // iterate over the entry objects aggregated by type
    for (Entry<OSMType, SortedMap<OSHDBTimestamp, Number>> entry : groupByResult.entrySet()) {
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
      case AREA:
        unit = "square-meter";
        description = "Total area of items aggregated on the type.";
        break;
      case PERIMETER:
        unit = "meter";
        description = "Total perimeter of items aggregated on the type.";
        break;
      default:
        // do nothing.. should never reach this :D
        break;
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      metadata = new Metadata(duration, unit, description, requestURL);
    }
    GroupByTypeResponse response = new GroupByTypeResponse(license, copyright, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count-density calculation.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param isPost <code>Boolean</code> defining if this method is called from a POST (true) or a
   *        GET (false) request.
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         ElementsResponseContent}
   */
  public DefaultAggregationResponse executeCountDensity(boolean isPost, String bboxes,
      String bcircles, String bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time, String showMetadata)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Integer> countResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputProcessor iP = new InputProcessor();
    ExecutionUtils exeUtils = new ExecutionUtils();
    String requestURL = null;
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    mapRed = iP.processParameters(isPost, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata);
    GeometryBuilder geomBuilder = iP.getGeomBuilder();
    countResult = mapRed.aggregateByTimestamp().count();
    int count = 0;
    Result[] countResultSet = new Result[countResult.size()];
    for (Entry<OSHDBTimestamp, Integer> entry : countResult.entrySet()) {
      countResultSet[count] =
          new Result(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
              entry.getValue().intValue());
      count++;
    }
    Geometry geom = null;
    switch (iP.getBoundaryType()) {
      case NOBOUNDARY:
        geom = OSHDBGeometryBuilder.getGeometry(geomBuilder.getBbox());
        break;
      case BBOXES:
        geom = OSHDBGeometryBuilder.getGeometry(geomBuilder.getBbox());
        break;
      case BCIRCLES:
        geom = geomBuilder.getbcircleGeom();
        break;
      case BPOLYS:
        geom = geomBuilder.getBpoly();
        break;
    }
    Result[] resultSet = new Result[countResult.size()];
    DecimalFormat densityDf = exeUtils.defineDecimalFormat("#.######");
    for (int i = 0; i < resultSet.length; i++) {
      String date = countResultSet[i].getTimestamp();
      double value = Double.parseDouble(
          densityDf.format((countResultSet[i].getValue() / (Geo.areaOf(geom) / 1000000))));
      resultSet[i] = new Result(date, value);
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      metadata = new Metadata(duration, "items per square-kilometer",
          "Density of selected items (number of items per area).", requestURL);
    }
    DefaultAggregationResponse response =
        new DefaultAggregationResponse(license, copyright, metadata, resultSet);
    return response;
  }

  /**
   * Performs a length|perimeter|area-share calculation.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCountShare(String, String, String, String[], String[], String[], String[], String[], String, String[], String[])
   * getCountShare} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (LENGTH, PERIMETER, AREA).
   * @param isPost <code>Boolean</code> defining if this method is called from a POST (true) or a
   *        GET (false) request.
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         ElementsResponseContent}
   */
  public ShareResponse executeLengthPerimeterAreaShare(RequestResource requestResource,
      boolean isPost, String bboxes, String bcircles, String bpolys, String[] types, String[] keys,
      String[] values, String[] userids, String[] time, String showMetadata, String[] keys2,
      String[] values2) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    values2 = exeUtils.shareParamEvaluation(keys2, values2);
    SortedMap<OSHDBTimestampAndIndex<Boolean>, Number> result;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputProcessor iP = new InputProcessor();
    String unit = "";
    String description = "";
    String requestURL = null;
    TagTranslator tt = exeUtils.createTagTranslator();
    Integer[] keysInt2 = new Integer[keys2.length];
    Integer[] valuesInt2 = new Integer[values2.length];
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    for (int i = 0; i < keys2.length; i++) {
      keysInt2[i] = tt.oshdbTagKeyOf(keys2[i]).toInt();
      if (values2 != null && i < values2.length)
        valuesInt2[i] = tt.oshdbTagOf(keys2[i], values2[i]).getValue();
    }
    mapRed = iP.processParameters(isPost, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata);
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
    DecimalFormat lengthPerimeterAreaDf = exeUtils.defineDecimalFormat("#.####");
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
        unit = "meter";
        description =
            "Total length of the whole and of a share of items satisfying keys2 and values2 within items selected by types, keys, values.";
        break;
      case PERIMETER:
        unit = "meter";
        description =
            "Total perimeter of the whole and of a share of items satisfying keys2 and values2 within items selected by types, keys, values.";
        break;
      case AREA:
        unit = "square-meter";
        description =
            "Total area of the whole and of a share of items satisfying keys2 and values2 within items selected by types, keys, values.";
        break;
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      metadata = new Metadata(duration, unit, description, requestURL);
    }
    ShareResponse response = new ShareResponse(license, copyright, metadata, resultSet);
    return response;
  }

  /**
   * Performs a length|perimeter|area-share calculation grouped by the boundary.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCountShare(String, String, String, String[], String[], String[], String[], String[], String, String[], String[])
   * getCountShare} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (LENGTH, PERIMETER, AREA).
   * @param isPost <code>Boolean</code> defining if this method is called from a POST (true) or a
   *        GET (false) request.
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.ShareGroupByBoundaryResponse
   *         ShareGroupByBoundaryResponse}
   */
  public ShareGroupByBoundaryResponse executeLengthPerimeterAreaShareGroupByBoundary(
      RequestResource requestResource, boolean isPost, String bboxes, String bcircles,
      String bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata, String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    values2 = exeUtils.shareParamEvaluation(keys2, values2);
    SortedMap<OSHDBTimestampAndIndex<Pair<Integer, Boolean>>, Number> result = null;
    SortedMap<Pair<Integer, Boolean>, SortedMap<OSHDBTimestamp, Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputProcessor iP = new InputProcessor();
    String requestURL = null;
    TagTranslator tt = exeUtils.createTagTranslator();
    Integer[] keysInt2 = new Integer[keys2.length];
    Integer[] valuesInt2 = new Integer[values2.length];
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    for (int i = 0; i < keys2.length; i++) {
      keysInt2[i] = tt.oshdbTagKeyOf(keys2[i]).toInt();
      if (values2 != null && i < values2.length)
        valuesInt2[i] = tt.oshdbTagOf(keys2[i], values2[i]).getValue();
    }
    mapRed = iP.processParameters(isPost, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata);
    GeometryBuilder geomBuilder = iP.getGeomBuilder();
    Utils utils = iP.getUtils();
    switch (iP.getBoundaryType()) {
      case NOBOUNDARY:
        throw new BadRequestException(
            "You need to give at least one boundary parameter if you want to use /groupBy/boundary.");
      case BBOXES:
        ArrayList<Geometry> bboxGeoms = geomBuilder.getGeometry(BoundaryType.BBOXES);
        ArrayList<Pair<Integer, Boolean>> zeroBboxFill = new ArrayList<>();
        for (int j = 0; j < bboxGeoms.size(); j++) {
          zeroBboxFill.add(new ImmutablePair<>(j, true));
          zeroBboxFill.add(new ImmutablePair<>(j, false));
        }
        result = mapRed.aggregateByTimestamp().flatMap(f -> {
          List<Pair<Integer, OSMEntitySnapshot>> bboxesList = new LinkedList<>();
          for (int i = 0; i < bboxGeoms.size(); i++)
            if (f.getGeometry().intersects(bboxGeoms.get(i)))
              bboxesList.add(new ImmutablePair<>(i, f));
          return bboxesList;
        }).aggregateBy(f -> {
          // result aggregated on true (if obj contains all tags) and false (if not all are
          // contained)
          boolean hasTags = false;
          for (int i = 0; i < keysInt2.length; i++) {
            if (f.getRight().getEntity().hasTagKey(keysInt2[i])) {
              if (i >= valuesInt2.length) {
                // if more keys2 than values2 are given
                hasTags = true;
                continue;
              }
              if (f.getRight().getEntity().hasTagValue(keysInt2[i], valuesInt2[i])) {
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
          return new ImmutablePair<>(f.getLeft(), hasTags);
        }).zerofillIndices(zeroBboxFill)
            .sum((SerializableFunction<Pair<Integer, OSMEntitySnapshot>, Number>) snapshot -> {
              switch (requestResource) {
                case LENGTH:
                  return Geo.lengthOf(snapshot.getRight().getGeometry());
                case PERIMETER:
                  if (snapshot.getRight().getGeometry() instanceof Polygonal)
                    return Geo.lengthOf(snapshot.getRight().getGeometry().getBoundary());
                  else
                    return 0.0;
                case AREA:
                  return Geo.areaOf(snapshot.getRight().getGeometry());
                default:
                  return 0.0;
              }
            });
        break;
      case BCIRCLES:
        ArrayList<Geometry> bcircleGeoms = geomBuilder.getGeometry(BoundaryType.BCIRCLES);
        ArrayList<Pair<Integer, Boolean>> zeroBcircleFill = new ArrayList<>();
        for (int j = 0; j < bcircleGeoms.size(); j++) {
          zeroBcircleFill.add(new ImmutablePair<>(j, true));
          zeroBcircleFill.add(new ImmutablePair<>(j, false));
        }
        result = mapRed.aggregateByTimestamp().flatMap(f -> {
          List<Pair<Integer, OSMEntitySnapshot>> bcirclesList = new LinkedList<>();
          for (int i = 0; i < bcircleGeoms.size(); i++)
            if (f.getGeometry().intersects(bcircleGeoms.get(i)))
              bcirclesList.add(new ImmutablePair<>(i, f));
          return bcirclesList;
        }).aggregateBy(f -> {
          // result aggregated on true (if obj contains all tags) and false (if not all are
          // contained)
          boolean hasTags = false;
          for (int i = 0; i < keysInt2.length; i++) {
            if (f.getRight().getEntity().hasTagKey(keysInt2[i])) {
              if (i >= valuesInt2.length) {
                // if more keys2 than values2 are given
                hasTags = true;
                continue;
              }
              if (f.getRight().getEntity().hasTagValue(keysInt2[i], valuesInt2[i])) {
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
          return new ImmutablePair<>(f.getLeft(), hasTags);
        }).zerofillIndices(zeroBcircleFill)
            .sum((SerializableFunction<Pair<Integer, OSMEntitySnapshot>, Number>) snapshot -> {
              switch (requestResource) {
                case LENGTH:
                  return Geo.lengthOf(snapshot.getRight().getGeometry());
                case PERIMETER:
                  if (snapshot.getRight().getGeometry() instanceof Polygonal)
                    return Geo.lengthOf(snapshot.getRight().getGeometry().getBoundary());
                  else
                    return 0.0;
                case AREA:
                  return Geo.areaOf(snapshot.getRight().getGeometry());
                default:
                  return 0.0;
              }
            });
        break;
      case BPOLYS:
        ArrayList<Geometry> bpolyGeoms = geomBuilder.getGeometry(BoundaryType.BPOLYS);
        ArrayList<Pair<Integer, Boolean>> zeroBpolyFill = new ArrayList<>();
        for (int j = 0; j < bpolyGeoms.size(); j++) {
          zeroBpolyFill.add(new ImmutablePair<>(j, true));
          zeroBpolyFill.add(new ImmutablePair<>(j, false));
        }
        result = mapRed.aggregateByTimestamp().flatMap(f -> {
          List<Pair<Integer, OSMEntitySnapshot>> bpolysList = new LinkedList<>();
          for (int i = 0; i < bpolyGeoms.size(); i++)
            if (f.getGeometry().intersects(bpolyGeoms.get(i)))
              bpolysList.add(new ImmutablePair<>(i, f));
          return bpolysList;
        }).aggregateBy(f -> {
          // result aggregated on true (if obj contains all tags) and false (if not all are
          // contained)
          boolean hasTags = false;
          for (int i = 0; i < keysInt2.length; i++) {
            if (f.getRight().getEntity().hasTagKey(keysInt2[i])) {
              if (i >= valuesInt2.length) {
                // if more keys2 than values2 are given
                hasTags = true;
                continue;
              }
              if (f.getRight().getEntity().hasTagValue(keysInt2[i], valuesInt2[i])) {
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
          return new ImmutablePair<>(f.getLeft(), hasTags);
        }).zerofillIndices(zeroBpolyFill)
            .sum((SerializableFunction<Pair<Integer, OSMEntitySnapshot>, Number>) snapshot -> {
              switch (requestResource) {
                case LENGTH:
                  return Geo.lengthOf(snapshot.getRight().getGeometry());
                case PERIMETER:
                  if (snapshot.getRight().getGeometry() instanceof Polygonal)
                    return Geo.lengthOf(snapshot.getRight().getGeometry().getBoundary());
                  else
                    return 0.0;
                case AREA:
                  return Geo.areaOf(snapshot.getRight().getGeometry());
                default:
                  return 0.0;
              }
            });
        break;
    }
    groupByResult = MapAggregatorByTimestampAndIndex.nest_IndexThenTime(result);
    ShareGroupByResult[] groupByResultSet = new ShareGroupByResult[groupByResult.size() / 2];
    DecimalFormat lengthPerimeterAreaDf = exeUtils.defineDecimalFormat("#.####");
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
    GroupByBoundaryMetadata gBBMetadata = null;
    if (iP.getShowMetadata()) {
      Map<String, double[]> boundaries = new HashMap<String, double[]>();
      switch (iP.getBoundaryType()) {
        case NOBOUNDARY:
          double[] singleBboxValues = new double[4];
          for (int i = 0; i < 4; i++)
            singleBboxValues[i] = Double.parseDouble(iP.getBoundaryValues()[i]);
          boundaries.put(boundaryIds[0], singleBboxValues);
          break;
        case BBOXES:
          int bboxCount = 0;
          for (int i = 0; i < iP.getBoundaryValues().length; i += 4) {
            double[] bboxValues = new double[4];
            for (int j = 0; j < 4; j++)
              bboxValues[j] = Double.parseDouble(iP.getBoundaryValues()[i + j]);
            boundaries.put(boundaryIds[bboxCount], bboxValues);
            bboxCount++;
          }
          break;
        case BCIRCLES:
          int bcircleCount = 0;
          for (int i = 0; i < iP.getBoundaryValues().length; i += 3) {
            double[] bcircleValues = new double[3];
            for (int j = 0; j < 3; j++)
              bcircleValues[j] = Double.parseDouble(iP.getBoundaryValues()[i + j]);
            boundaries.put(boundaryIds[bcircleCount], bcircleValues);
            bcircleCount++;
          }
          break;
        case BPOLYS:
          // TODO implement for bpolys (should be done together with WKT implementation)
          boundaries = null;
          break;
      }
      long duration = System.currentTimeMillis() - startTime;
      gBBMetadata = new GroupByBoundaryMetadata(duration, "amount", boundaries,
          "Share of items satisfying keys2 and values2 within items selected by types, keys, values, grouped on the boundary parameter.",
          requestURL);
    }
    ShareGroupByBoundaryResponse response =
        new ShareGroupByBoundaryResponse(license, copyright, gBBMetadata, groupByResultSet);
    return response;
  }

  /**
   * Performs a count-ratio calculation.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCountRatio(String, String, String, String[], String[], String[], String[], String[], String, String[], String[], String[])
   * getCountRatio} method.
   * 
   * @param isPost <code>Boolean</code> defining if this method is called from a POST (true) or a
   *        GET (false) request.
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         ElementsResponseContent}
   */
  public RatioResponse executeCountRatio(boolean isPost, String bboxes, String bcircles,
      String bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata, String[] types2, String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Integer> result1;
    SortedMap<OSHDBTimestamp, Integer> result2;
    MapReducer<OSMEntitySnapshot> mapRed1;
    MapReducer<OSMEntitySnapshot> mapRed2;
    InputProcessor iP = new InputProcessor();
    ExecutionUtils exeUtils = new ExecutionUtils();
    String requestURL = null;
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    mapRed1 = iP.processParameters(isPost, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata);
    result1 = mapRed1.aggregateByTimestamp().count();
    mapRed2 = iP.processParameters(isPost, bboxes, bcircles, bpolys, types2, keys2, values2,
        userids, time, showMetadata);
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
      metadata = new Metadata(duration, "amount and ratio",
          "Amount of items satisfying types2, keys2, values2 parameters (= value2 output) "
              + "within items selected by types, keys, values parameters (= value output) and ratio of value2:value.",
          requestURL);
    }
    RatioResponse response = new RatioResponse(license, copyright, metadata, resultSet);
    return response;
  }
}
