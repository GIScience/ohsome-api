package org.heigit.bigspatialdata.ohsome.ohsomeapi.executor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.geojson.GeoJsonObject;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.Application;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.rawdata.ElementsGeometry;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.ExecutionUtils.MatchType;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.BoundaryType;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.InputProcessingUtils;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.interceptor.RequestInterceptor;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.oshdb.RemoteTagTranslator;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.Description;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Attribution;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.DefaultAggregationResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Metadata;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.elements.ElementsResult;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.GroupByResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.GroupByResult;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.rawdataresponse.DataResponse;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBIgnite;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBIgnite.ComputeMode;
import org.heigit.bigspatialdata.oshdb.api.generic.OSHDBCombinedIndex;
import org.heigit.bigspatialdata.oshdb.api.generic.function.SerializableFunction;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.object.OSMContribution;
import org.heigit.bigspatialdata.oshdb.api.object.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.osm.OSMEntity;
import org.heigit.bigspatialdata.oshdb.osm.OSMType;
import org.heigit.bigspatialdata.oshdb.util.OSHDBTag;
import org.heigit.bigspatialdata.oshdb.util.OSHDBTimestamp;
import org.heigit.bigspatialdata.oshdb.util.celliterator.ContributionType;
import org.heigit.bigspatialdata.oshdb.util.geometry.Geo;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;
import org.heigit.bigspatialdata.oshdb.util.time.ISODateTimeParser;
import org.heigit.bigspatialdata.oshdb.util.time.TimestampFormatter;
import org.wololo.geojson.Feature;
import org.wololo.jts2geojson.GeoJSONWriter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygonal;

/** Includes all execute methods for requests mapped to /elements. */
public class ElementsRequestExecutor {

  private static final String URL = ExtractMetadata.attributionUrl;
  private static final String TEXT = ExtractMetadata.attributionShort;
  private static final double MAX_STREAM_DATA_SIZE = 1E7;

  /**
   * Performs an OSM data extraction.
   * 
   * <p>
   * 
   * @param requestParams <code>RequestParameters</code> object, which holds those parameters that
   *        are used in every request.
   * @param response <code>HttpServletResponse</code> object, which is used to send the response as
   *        a stream.
   */
  public static void executeElements(RequestParameters requestParams, ElementsGeometry elemGeom,
      String[] propertiesParameter, HttpServletResponse response)
      throws UnsupportedOperationException, Exception {
    InputProcessor inputProcessor = new InputProcessor();
    String requestUrl = null;
    if (!requestParams.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    MapReducer<OSMEntitySnapshot> mapRed = null;
    final boolean includeTags =
        Arrays.stream(propertiesParameter).anyMatch(p -> p.equalsIgnoreCase("tags"));
    final boolean includeOSMMetadata =
        Arrays.stream(propertiesParameter).anyMatch(p -> p.equalsIgnoreCase("metadata"));
    if (DbConnData.db instanceof OSHDBIgnite) {
      final OSHDBIgnite dbIgnite = (OSHDBIgnite) DbConnData.db;
      ComputeMode previousComputeMode = dbIgnite.computeMode();
      // do a preflight to get an approximate result data size estimation:
      // for now just the sum of the average size of the objects versions in bytes is used
      // if that number is larger than 10MB, then fall back to the slightly slower, but much
      // less memory intensive streaming implementation (which is currently only available on
      // the ignite "AffinityCall" backend).

      Number approxResultSize = inputProcessor.processParameters(requestParams)
          .map(data -> ((OSMEntitySnapshot) data).getOSHEntity())
          .sum(data -> data.getLength() / data.getLatest().getVersion());
      if (approxResultSize.doubleValue() > MAX_STREAM_DATA_SIZE) {
        dbIgnite.computeMode(ComputeMode.AffinityCall);
      }
      mapRed = inputProcessor.processParameters(requestParams);
      dbIgnite.computeMode(previousComputeMode);
    } else {
      mapRed = inputProcessor.processParameters(requestParams);
    }
    TagTranslator tt = DbConnData.tagTranslator;
    String[] keys = requestParams.getKeys();
    int[] keysInt = new int[keys.length];
    if (keys.length != 0) {
      for (int i = 0; i < keys.length; i++) {
        keysInt[i] = tt.getOSHDBTagKeyOf(keys[i]).toInt();
      }
    }
    final MapReducer<Feature> preResult;
    ExecutionUtils exeUtils = new ExecutionUtils();
    GeoJSONWriter gjw = new GeoJSONWriter();
    RemoteTagTranslator mapTagTranslator = DbConnData.mapTagTranslator;
    preResult = mapRed.map(snapshot -> {
      Map<String, Object> properties = new TreeMap<>();
      if (includeOSMMetadata) {
        properties.put("@lastEdit", snapshot.getEntity().getTimestamp().toString());
      }
      properties.put("@snapshotTimestamp", snapshot.getTimestamp().toString());
      return exeUtils.createOSMFeature(snapshot.getEntity(), snapshot.getGeometry(), properties,
          keysInt, includeTags, includeOSMMetadata, elemGeom, mapTagTranslator.get(), gjw);
    });
    Stream<Feature> streamResult = preResult.stream().filter(Objects::nonNull);
    Metadata metadata = null;
    if (ProcessingData.showMetadata) {
      metadata = new Metadata(null, "OSM data as GeoJSON features.", requestUrl);
    }
    DataResponse osmData = new DataResponse(new Attribution(URL, TEXT), Application.apiVersion,
        metadata, "FeatureCollection", Collections.emptyList());
    exeUtils.streamElementsResponse(response, osmData, false, streamResult, null);
  }

  /**
   * Performs an OSM data extraction using the full-history of the data.
   * 
   * <p>
   * 
   * @param requestParams <code>RequestParameters</code> object, which holds those parameters that
   *        are used in every request.
   * @param response <code>HttpServletResponse</code> object, which is used to send the response as
   *        a stream.
   */
  public static void executeElementsFullHistory(RequestParameters contributionRequestParams,
      ElementsGeometry elemGeom, String[] propertiesParameter, HttpServletResponse response)
      throws UnsupportedOperationException, Exception {
    InputProcessor inputProcessor = new InputProcessor();
    String requestUrl = null;
    if (!contributionRequestParams.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    if (contributionRequestParams.getTime().length != 2) {
      throw new BadRequestException("Wrong time parameter. You need to give exactly "
          + "two timestamps that are ISO-8601 conform, if you want to use this resource.");
    }
    MapReducer<OSMEntitySnapshot> mapRedSnapshot = null;
    MapReducer<OSMContribution> mapRedContribution = null;

    RequestParameters snapshotRequestParams =
        new RequestParameters(contributionRequestParams.getRequestMethod(), true,
            contributionRequestParams.isDensity(), contributionRequestParams.getBboxes(),
            contributionRequestParams.getBcircles(), contributionRequestParams.getBpolys(),
            contributionRequestParams.getTypes(), contributionRequestParams.getKeys(),
            contributionRequestParams.getValues(), contributionRequestParams.getUserids(),
            contributionRequestParams.getTime(), contributionRequestParams.getShowMetadata());

    boolean noFinalIncludeTags = false;
    boolean noFinalIncludeOSMMetadata = false;
    for (String text : propertiesParameter) {
      if (text.equalsIgnoreCase("tags")) {
        noFinalIncludeTags = true;
      } else if (text.equalsIgnoreCase("metadata")) {
        noFinalIncludeOSMMetadata = true;
      }
    }
    final boolean includeTags = noFinalIncludeTags;
    final boolean includeOSMMetadata = noFinalIncludeOSMMetadata;
    if (DbConnData.db instanceof OSHDBIgnite) {
      final OSHDBIgnite dbIgnite = (OSHDBIgnite) DbConnData.db;
      ComputeMode previousComputeMode = dbIgnite.computeMode();
      final double maxStreamDataSize = 1E7;
      Number approxResultSize = inputProcessor.processParameters(snapshotRequestParams)
          .map(data -> ((OSMEntitySnapshot) data).getOSHEntity())
          .sum(data -> data.getLength() / data.getLatest().getVersion());
      if (approxResultSize.doubleValue() > maxStreamDataSize) {
        dbIgnite.computeMode(ComputeMode.AffinityCall);
      }
      mapRedSnapshot = inputProcessor.processParameters(snapshotRequestParams);
      mapRedContribution = inputProcessor.processParameters(contributionRequestParams);
      dbIgnite.computeMode(previousComputeMode);
    } else {
      mapRedSnapshot = inputProcessor.processParameters(snapshotRequestParams);
      mapRedContribution = inputProcessor.processParameters(contributionRequestParams);
    }
    TagTranslator tt = DbConnData.tagTranslator;
    String[] keys = contributionRequestParams.getKeys();
    int[] keysInt = new int[keys.length];
    if (keys.length != 0) {
      for (int i = 0; i < keys.length; i++) {
        keysInt[i] = tt.getOSHDBTagKeyOf(keys[i]).toInt();
      }
    }
    MapReducer<Feature> contributionPreResult = null;
    ExecutionUtils exeUtils = new ExecutionUtils();
    GeoJSONWriter gjw = new GeoJSONWriter();
    RemoteTagTranslator mapTagTranslator = DbConnData.mapTagTranslator;
    String startTimestampWithZ =
        ISODateTimeParser.parseISODateTime(contributionRequestParams.getTime()[0]).toString();
    String endTimestampWithZ =
        ISODateTimeParser.parseISODateTime(contributionRequestParams.getTime()[1]).toString();
    String startTimestamp = startTimestampWithZ.substring(0, startTimestampWithZ.length() - 1);
    String endTimestamp = endTimestampWithZ.substring(0, endTimestampWithZ.length() - 1);

    contributionPreResult = mapRedContribution.groupByEntity().flatMap(contributions -> {
      List<Feature> output = new LinkedList<>();

      Map<String, Object> properties;
      Geometry currentGeom = null;
      OSMEntity currentEntity = null;
      String validFrom = null;
      String validTo;
      boolean skipNext = false;

      // first contribution:
      if (contributions.get(0).is(ContributionType.CREATION)) {
        // if creation: skip next output
        skipNext = true;
      } else {
        // if not "creation": take "before" as starting "row" (geom, tags), valid_from = t_start
        currentEntity = contributions.get(0).getEntityBefore();
        currentGeom = contributions.get(0).getGeometryBefore();
        validFrom = startTimestamp;
      }

      // then for each contribution:
      for (OSMContribution contribution : contributions) {
        // set valid_to of previous row, add to output list (output.add(…))
        validTo = contribution.getTimestamp().toString();
        if (!skipNext) {
          properties = new TreeMap<>();
          properties.put("@validFrom", validFrom);
          properties.put("@validTo", validTo);
          output.add(exeUtils.createOSMFeature(currentEntity, currentGeom, properties, keysInt,
              includeTags, includeOSMMetadata, elemGeom, mapTagTranslator.get(), gjw));
        }
        skipNext = false;
        if (contribution.is(ContributionType.DELETION)) {
          // if deletion: skip output of next row
          skipNext = true;
        } else {
          // else: take "after" as next row
          currentEntity = contribution.getEntityAfter();
          currentGeom = contribution.getGeometryAfter();
          validFrom = contribution.getTimestamp().toString();
        }
      }
      // after loop:
      if (!contributions.get(contributions.size() - 1).is(ContributionType.DELETION)) {
        // if last contribution was not "deletion": set valid_to = t_end, add row to output list
        validTo = endTimestamp;
        properties = new TreeMap<>();
        properties.put("@validFrom", validFrom);
        properties.put("@validTo", validTo);
        output.add(exeUtils.createOSMFeature(currentEntity, currentGeom, properties, keysInt,
            includeTags, includeOSMMetadata, elemGeom, mapTagTranslator.get(), gjw));
      }
      return output;
    });

    MapReducer<Feature> snapshotPreResult = null;

    snapshotPreResult = mapRedSnapshot.groupByEntity().filter(snapshots -> snapshots.size() == 2)
        .filter(snapshots -> snapshots.get(0).getGeometry() == snapshots.get(1).getGeometry()
            && snapshots.get(0).getEntity().getVersion() == snapshots.get(1).getEntity()
                .getVersion())
        .map(snapshots -> snapshots.get(0)).map(snapshot -> {
          Map<String, Object> properties = new TreeMap<>();
          OSMEntity entity = snapshot.getEntity();
          if (includeOSMMetadata) {
            properties.put("@lastEdit", entity.getTimestamp().toString());
          }
          Geometry geom = snapshot.getGeometry();
          properties.put("@snapshotTimestamp", snapshot.getTimestamp().toString());
          properties.put("@validFrom", startTimestamp);
          properties.put("@validTo", endTimestamp);
          return exeUtils.createOSMFeature(entity, geom, properties, keysInt, includeTags,
              includeOSMMetadata, elemGeom, mapTagTranslator.get(), gjw);
        }); // valid_from = t_start, valid_to = t_end

    Stream<Feature> contributionStream = contributionPreResult.stream().filter(Objects::nonNull);
    Stream<Feature> snapshotStream = snapshotPreResult.stream().filter(Objects::nonNull);

    Metadata metadata = null;
    if (ProcessingData.showMetadata) {
      metadata = new Metadata(null, "Full-history OSM data as GeoJSON features.", requestUrl);
    }
    DataResponse osmData = new DataResponse(new Attribution(URL, TEXT), Application.apiVersion,
        metadata, "FeatureCollection", Collections.emptyList());

    exeUtils.streamElementsResponse(response, osmData, true, snapshotStream, contributionStream);
  }

  /**
   * Performs a count|length|perimeter|area calculation.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (COUNT, LENGTH, PERIMETER,
   *        AREA).
   * @param requestParams <code>RequestParameters</code> object, which holds those parameters that
   *        are used in every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   */
  public static Response executeCountLengthPerimeterArea(RequestResource requestResource,
      RequestParameters requestParams) throws UnsupportedOperationException, Exception {
    final long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, ? extends Number> result = null;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor();
    String requestUrl = null;
    if (!requestParams.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    mapRed = inputProcessor.processParameters(requestParams);
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
              if (snapshot.getGeometry() instanceof Polygonal) {
                return Geo.lengthOf(snapshot.getGeometry().getBoundary());
              } else {
                return 0.0;
              }
            });
        break;
      default:
        break;
    }
    ExecutionUtils exeUtils = new ExecutionUtils();
    Geometry geom = exeUtils.getGeometry(ProcessingData.boundary);
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    ElementsResult[] resultSet =
        exeUtils.fillElementsResult(result, requestParams.isDensity(), df, geom);
    Metadata metadata = null;
    if (ProcessingData.showMetadata) {
      long duration = System.currentTimeMillis() - startTime;
      metadata =
          new Metadata(duration, Description.countLengthPerimeterArea(requestParams.isDensity(),
              requestResource.getLabel(), requestResource.getUnit()), requestUrl);
    }
    DefaultAggregationResponse response = DefaultAggregationResponse.of(new Attribution(URL, TEXT),
        Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count|length|perimeter|area calculation grouped by the boundary.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (COUNT, LENGTH, PERIMETER,
   *        AREA).
   * @param requestParams <code>RequestParameters</code> object, which holds those parameters that
   *        are used in every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.GroupByResponse
   *         GroupByResponse Content}
   */
  public static Response executeCountLengthPerimeterAreaGroupByBoundary(
      RequestResource requestResource, RequestParameters requestParams)
      throws UnsupportedOperationException, Exception {
    final long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, ? extends Number> result = null;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor();
    String requestUrl = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    if (!requestParams.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    mapRed = inputProcessor.processParameters(requestParams);
    switch (requestResource) {
      case COUNT:
        result = exeUtils.computeCountLengthPerimeterAreaGbB(RequestResource.COUNT,
            ProcessingData.boundary, mapRed);
        break;
      case LENGTH:
        result = exeUtils.computeCountLengthPerimeterAreaGbB(RequestResource.LENGTH,
            ProcessingData.boundary, mapRed);
        break;
      case PERIMETER:
        result = exeUtils.computeCountLengthPerimeterAreaGbB(RequestResource.PERIMETER,
            ProcessingData.boundary, mapRed);
        break;
      case AREA:
        result = exeUtils.computeCountLengthPerimeterAreaGbB(RequestResource.AREA,
            ProcessingData.boundary, mapRed);
        break;
      default:
        break;
    }
    SortedMap<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    InputProcessingUtils utils = inputProcessor.getUtils();
    String[] boundaryIds = utils.getBoundaryIds();
    int count = 0;
    ArrayList<Geometry> boundaries = new ArrayList<>(ProcessingData.boundaryColl);
    for (Entry<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      ElementsResult[] results = exeUtils.fillElementsResult(entry.getValue(),
          requestParams.isDensity(), df, boundaries.get(count));
      groupByName = boundaryIds[count];
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    Metadata metadata = null;
    if (ProcessingData.showMetadata) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration,
          Description.countLengthPerimeterAreaGroupByBoundary(requestParams.isDensity(),
              requestResource.getLabel(), requestResource.getUnit()),
          requestUrl);
    }
    if (requestParams.getFormat() != null
        && requestParams.getFormat().equalsIgnoreCase("geojson")) {
      return GroupByResponse.of(new Attribution(URL, TEXT), Application.apiVersion, metadata,
          "FeatureCollection",
          exeUtils.createGeoJsonFeatures(resultSet, ProcessingData.geoJsonGeoms));
    }
    return new GroupByResponse(new Attribution(URL, TEXT), Application.apiVersion, metadata,
        resultSet);
  }

  /**
   * Performs a count|length|perimeter|area calculation grouped by the user.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (COUNT, LENGTH, PERIMETER,
   *        AREA).
   * @param requestParams <code>RequestParameters</code> object, which holds those parameters that
   *        are used in every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.GroupByResponse
   *         GroupByResponse Content}
   */
  public static Response executeCountLengthPerimeterAreaGroupByUser(RequestResource requestResource,
      RequestParameters requestParams) throws UnsupportedOperationException, Exception {
    final long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor();
    String requestUrl = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    ArrayList<Integer> useridsInt = new ArrayList<Integer>();
    if (!requestParams.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    mapRed = inputProcessor.processParameters(requestParams);
    if (requestParams.getUserids() != null) {
      for (String user : requestParams.getUserids()) {
        useridsInt.add(Integer.parseInt(user));
      }
    }
    SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, ? extends Number> result = null;
    MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, OSMEntitySnapshot> preResult;
    SortedMap<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    preResult = mapRed.aggregateByTimestamp()
        .aggregateBy((SerializableFunction<OSMEntitySnapshot, Integer>) f -> {
          return f.getEntity().getUserId();
        }, useridsInt);
    result = exeUtils.computeResult(requestResource, preResult);
    groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];

    int count = 0;
    for (Entry<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {

      ElementsResult[] results =
          exeUtils.fillElementsResult(entry.getValue(), requestParams.isDensity(), df, null);
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }

    Metadata metadata = null;
    if (ProcessingData.showMetadata) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, Description.countLengthPerimeterAreaGroupByUser(
          requestResource.getLabel(), requestResource.getUnit()), requestUrl);
    }
    GroupByResponse response = new GroupByResponse(new Attribution(URL, TEXT),
        Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count|length|perimeter|area calculation grouped by the tag.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#countGroupByTag(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest, String[], String[])
   * countGroupByTag} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (COUNT, LENGTH, PERIMETER,
   *        AREA).
   * @param requestParams <code>RequestParameters</code> object, which holds those parameters that
   *        are used in every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.GroupByResponse
   *         GroupByResponse Content}
   */
  public static Response executeCountLengthPerimeterAreaGroupByTag(RequestResource requestResource,
      RequestParameters requestParams, String[] groupByKey, String[] groupByValues)
      throws UnsupportedOperationException, Exception {
    final long startTime = System.currentTimeMillis();
    if (groupByKey == null || groupByKey.length != 1) {
      throw new BadRequestException(ExceptionMessages.groupByKeyParam);
    }
    ExecutionUtils exeUtils = new ExecutionUtils();
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor();
    String requestUrl = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    if (!requestParams.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    if (groupByValues == null) {
      groupByValues = new String[0];
    }
    TagTranslator tt = DbConnData.tagTranslator;
    Integer[] valuesInt = new Integer[groupByValues.length];
    ArrayList<Pair<Integer, Integer>> zeroFill = new ArrayList<Pair<Integer, Integer>>();
    mapRed = inputProcessor.processParameters(requestParams);
    int keysInt = tt.getOSHDBTagKeyOf(groupByKey[0]).toInt();
    if (groupByValues.length != 0) {
      for (int j = 0; j < groupByValues.length; j++) {
        valuesInt[j] = tt.getOSHDBTagOf(groupByKey[0], groupByValues[j]).getValue();
        zeroFill.add(new ImmutablePair<Integer, Integer>(keysInt, valuesInt[j]));
      }
    }
    MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, Pair<Integer, Integer>>, OSMEntitySnapshot> preResult =
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
                if (tagValueId == value) {
                  return new ImmutablePair<>(
                      new ImmutablePair<Integer, Integer>(tagKeyId, tagValueId), f);
                }
              }
            }
          }
          return new ImmutablePair<>(new ImmutablePair<Integer, Integer>(-1, -1), f);
        }).aggregateByTimestamp().aggregateBy(Pair::getKey, zeroFill).map(Pair::getValue);
    SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, Pair<Integer, Integer>>, ? extends Number> result =
        null;
    SortedMap<Pair<Integer, Integer>, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    result = exeUtils.computeResult(requestResource, preResult);
    groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    Geometry geom = exeUtils.getGeometry(ProcessingData.boundary);
    int count = 0;
    for (Entry<Pair<Integer, Integer>, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      ElementsResult[] results =
          exeUtils.fillElementsResult(entry.getValue(), requestParams.isDensity(), df, geom);
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
    if (ProcessingData.showMetadata) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration,
          Description.countLengthPerimeterAreaGroupByTag(requestParams.isDensity(),
              requestResource.getLabel(), requestResource.getUnit()),
          requestUrl);
    }
    GroupByResponse response = new GroupByResponse(new Attribution(URL, TEXT),
        Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count|perimeter|area calculation grouped by the OSM type.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (COUNT, LENGTH, PERIMETER,
   *        AREA).
   * @param requestParams <code>RequestParameters</code> object, which holds those parameters that
   *        are used in every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.GroupByResponse
   *         GroupByResponseContent}
   */
  public static Response executeCountPerimeterAreaGroupByType(RequestResource requestResource,
      RequestParameters requestParams) throws UnsupportedOperationException, Exception {
    final long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor();
    String requestUrl = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    if (!requestParams.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    mapRed = inputProcessor.processParameters(requestParams);
    MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, OSMType>, OSMEntitySnapshot> preResult;
    preResult = mapRed.aggregateByTimestamp()
        .aggregateBy((SerializableFunction<OSMEntitySnapshot, OSMType>) f -> {
          return f.getEntity().getType();
        }, ProcessingData.osmTypes);
    SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, OSMType>, ? extends Number> result = null;
    result = exeUtils.computeResult(requestResource, preResult);
    SortedMap<OSMType, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    Geometry geom = exeUtils.getGeometry(ProcessingData.boundary);
    int count = 0;
    for (Entry<OSMType, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      ElementsResult[] results =
          exeUtils.fillElementsResult(entry.getValue(), requestParams.isDensity(), df, geom);
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    Metadata metadata = null;
    if (ProcessingData.showMetadata) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration,
          Description.countPerimeterAreaGroupByType(requestParams.isDensity(),
              requestResource.getLabel(), requestResource.getUnit()),
          requestUrl);
    }
    GroupByResponse response = new GroupByResponse(new Attribution(URL, TEXT),
        Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count|length|perimeter|area calculation grouped by the key.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#countGroupByKey(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest, String[])
   * countGroupByKey} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (COUNT, LENGTH, PERIMETER,
   *        AREA).
   * @param requestParams <code>RequestParameters</code> object, which holds those parameters that
   *        are used in every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.GroupByResponse
   *         GroupByResponse Content}
   */
  public static Response executeCountLengthPerimeterAreaGroupByKey(RequestResource requestResource,
      RequestParameters requestParams, String[] groupByKeys)
      throws UnsupportedOperationException, Exception {
    final long startTime = System.currentTimeMillis();
    if (groupByKeys == null || groupByKeys.length == 0) {
      throw new BadRequestException(ExceptionMessages.groupByKeysParam);
    }
    ExecutionUtils exeUtils = new ExecutionUtils();
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor();
    String requestUrl = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    if (!requestParams.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    TagTranslator tt = DbConnData.tagTranslator;
    Integer[] keysInt = new Integer[groupByKeys.length];
    mapRed = inputProcessor.processParameters(requestParams);
    for (int i = 0; i < groupByKeys.length; i++) {
      keysInt[i] = tt.getOSHDBTagKeyOf(groupByKeys[i]).toInt();
    }
    MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, OSMEntitySnapshot> preResult =
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
          if (res.size() == 0) {
            res.add(new ImmutablePair<>(-1, f));
          }
          return res;
        }).aggregateByTimestamp().aggregateBy(Pair::getKey, Arrays.asList(keysInt))
            .map(Pair::getValue);
    SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, ? extends Number> result = null;
    SortedMap<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    result = exeUtils.computeResult(requestResource, preResult);
    groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    int count = 0;
    for (Entry<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      ElementsResult[] results =
          exeUtils.fillElementsResult(entry.getValue(), requestParams.isDensity(), df, null);
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
    if (ProcessingData.showMetadata) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, Description.countLengthPerimeterAreaGroupByKey(
          requestResource.getLabel(), requestResource.getUnit()), requestUrl);
    }
    GroupByResponse response = new GroupByResponse(new Attribution(URL, TEXT),
        Application.apiVersion, metadata, resultSet);
    return response;
  }

  /**
   * Performs a count|length|perimeter|area-share|ratio calculation.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#countRatio(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest, String[], String[], String[])
   * countRatio} method.
   * 
   * @param requestResource <code>Enum</code> defining the request type (COUNT, LENGTH, PERIMETER,
   *        AREA).
   * @param requestParams <code>RequestParameters</code> object, which holds those parameters that
   *        are used in every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   */
  public static Response executeCountLengthPerimeterAreaRatio(RequestResource requestResource,
      RequestParameters requestParams, String[] types2, String[] keys2, String[] values2,
      boolean isShare) throws UnsupportedOperationException, Exception {
    final long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor();
    String requestUrl = null;
    TagTranslator tt = DbConnData.tagTranslator;
    requestParams = inputProcessor.fillWithEmptyIfNull(requestParams);
    // for input processing/checking only
    inputProcessor.processParameters(requestParams);
    inputProcessor.checkKeysValues(keys2, values2);
    values2 = inputProcessor.createEmptyArrayIfNull(values2);
    keys2 = inputProcessor.createEmptyArrayIfNull(keys2);
    if (isShare) {
      List<Pair<String, String>> keys2Vals2;
      if (requestParams.getValues().length == 0) {
        keys2 = inputProcessor.addFilterKeys(requestParams.getKeys(), keys2);
      } else if (keys2.length == 0) {
        keys2 = requestParams.getKeys();
        values2 = requestParams.getValues();
      } else {
        keys2Vals2 = inputProcessor.addFilterKeysVals(requestParams.getKeys(),
            requestParams.getValues(), keys2, values2);
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
    Integer[] keysInt1 = new Integer[requestParams.getKeys().length];
    Integer[] valuesInt1 = new Integer[requestParams.getValues().length];
    Integer[] keysInt2 = new Integer[keys2.length];
    Integer[] valuesInt2 = new Integer[values2.length];
    if (!requestParams.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    for (int i = 0; i < requestParams.getKeys().length; i++) {
      keysInt1[i] = tt.getOSHDBTagKeyOf(requestParams.getKeys()[i]).toInt();
      if (requestParams.getValues() != null && i < requestParams.getValues().length) {
        valuesInt1[i] =
            tt.getOSHDBTagOf(requestParams.getKeys()[i], requestParams.getValues()[i]).getValue();
      }
    }
    for (int i = 0; i < keys2.length; i++) {
      keysInt2[i] = tt.getOSHDBTagKeyOf(keys2[i]).toInt();
      if (i < values2.length) {
        valuesInt2[i] = tt.getOSHDBTagOf(keys2[i], values2[i]).getValue();
      }
    }
    EnumSet<OSMType> osmTypes1 = ProcessingData.osmTypes;
    inputProcessor.defineOSMTypes(types2);
    EnumSet<OSMType> osmTypes2 = ProcessingData.osmTypes;
    EnumSet<OSMType> osmTypes = osmTypes1.clone();
    osmTypes.addAll(osmTypes2);
    String[] osmTypesString =
        osmTypes.stream().map(OSMType::toString).map(String::toLowerCase).toArray(String[]::new);
    if (!inputProcessor.compareKeysValues(requestParams.getKeys(), keys2, requestParams.getValues(),
        values2)) {
      mapRed =
          inputProcessor.processParameters(new RequestParameters(requestParams.getRequestMethod(),
              requestParams.isSnapshot(), requestParams.isDensity(), requestParams.getBboxes(),
              requestParams.getBcircles(), requestParams.getBpolys(), osmTypesString,
              new String[] {}, new String[] {}, requestParams.getUserids(), requestParams.getTime(),
              requestParams.getShowMetadata()));
      mapRed = mapRed.osmEntityFilter(entity -> {
        if (!exeUtils.entityMatches(entity, osmTypes1, keysInt1, valuesInt1)) {
          return exeUtils.entityMatches(entity, osmTypes2, keysInt2, valuesInt2);
        }
        return true;
      });
    } else {
      mapRed = inputProcessor.processParameters(requestParams);
      mapRed = mapRed.osmType(osmTypes);
    }
    MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, MatchType>, OSMEntitySnapshot> preResult;
    preResult = mapRed.aggregateByTimestamp().aggregateBy(f -> {
      OSMEntity entity = f.getEntity();
      boolean matches1 = exeUtils.entityMatches(entity, osmTypes1, keysInt1, valuesInt1);
      boolean matches2 = exeUtils.entityMatches(entity, osmTypes2, keysInt2, valuesInt2);
      if (matches1 && matches2) {
        return MatchType.MATCHESBOTH;
      } else if (matches1) {
        return MatchType.MATCHES1;
      } else if (matches2) {
        return MatchType.MATCHES2;
      } else {
        // this should never be reached
        assert false : "MatchType matches none.";
      }
      return null;
    }, Arrays.asList(MatchType.MATCHESBOTH, MatchType.MATCHES1, MatchType.MATCHES2));
    SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, MatchType>, ? extends Number> result = null;
    result = exeUtils.computeResult(requestResource, preResult);
    int resultSize = result.size();
    Double[] value1 = new Double[resultSize / 3];
    Double[] value2 = new Double[resultSize / 3];
    String[] timeArray = new String[resultSize / 3];
    int value1Count = 0;
    int value2Count = 0;
    int matchesBothCount = 0;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    DecimalFormat ratioDf = exeUtils.defineDecimalFormat("#.######");
    // time and value extraction
    for (Entry<OSHDBCombinedIndex<OSHDBTimestamp, MatchType>, ? extends Number> entry : result
        .entrySet()) {
      if (entry.getKey().getSecondIndex() == MatchType.MATCHES2) {
        timeArray[value2Count] =
            TimestampFormatter.getInstance().isoDateTime(entry.getKey().getFirstIndex());
        value2[value2Count] = Double.parseDouble(df.format(entry.getValue().doubleValue()));
        value2Count++;
      }
      if (entry.getKey().getSecondIndex() == MatchType.MATCHES1) {
        value1[value1Count] = Double.parseDouble(df.format(entry.getValue().doubleValue()));
        value1Count++;
      }
      if (entry.getKey().getSecondIndex() == MatchType.MATCHESBOTH) {
        value1[matchesBothCount] = value1[matchesBothCount]
            + Double.parseDouble(df.format(entry.getValue().doubleValue()));
        value2[matchesBothCount] = value2[matchesBothCount]
            + Double.parseDouble(df.format(entry.getValue().doubleValue()));
        matchesBothCount++;
      }
    }
    return exeUtils.createRatioShareResponse(isShare, timeArray, value1, value2, ratioDf, startTime,
        requestResource, requestUrl, new Attribution(URL, TEXT));
  }

  /**
   * Performs a count|length|perimeter|area-ratio calculation grouped by the boundary.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#countRatio(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest, String[], String[], String[])
   * countRatio} method.
   * 
   * @param requestParams <code>RequestParameters</code> object, which holds those parameters that
   *        are used in every request.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.RatioGroupByBoundaryResponse
   *         RatioGroupByBoundaryResponse Content}
   */
  @SuppressWarnings({"unchecked"}) // intentionally as check for P on Polygonal is already performed
  public static <P extends Geometry & Polygonal> Response executeCountLengthPerimeterAreaRatioGroupByBoundary(
      RequestResource requestResource, RequestParameters requestParams, String[] types2,
      String[] keys2, String[] values2, boolean isShare)
      throws UnsupportedOperationException, Exception {
    final long startTime = System.currentTimeMillis();
    ExecutionUtils exeUtils = new ExecutionUtils();
    SortedMap<OSHDBCombinedIndex<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, MatchType>, ? extends Number> result =
        null;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor();
    String requestUrl = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    TagTranslator tt = DbConnData.tagTranslator;
    requestParams = inputProcessor.fillWithEmptyIfNull(requestParams);
    inputProcessor.processParameters(requestParams);
    if (ProcessingData.boundary == BoundaryType.NOBOUNDARY) {
      throw new BadRequestException(ExceptionMessages.noBoundary);
    }
    final GeoJsonObject[] geoJsonGeoms = ProcessingData.geoJsonGeoms;
    inputProcessor.checkKeysValues(keys2, values2);
    values2 = inputProcessor.createEmptyArrayIfNull(values2);
    keys2 = inputProcessor.createEmptyArrayIfNull(keys2);
    if (isShare) {
      List<Pair<String, String>> keys2Vals2;
      if (requestParams.getValues().length == 0) {
        keys2 = inputProcessor.addFilterKeys(requestParams.getKeys(), keys2);
      } else if (keys2.length == 0) {
        keys2 = requestParams.getKeys();
        values2 = requestParams.getValues();
      } else {
        keys2Vals2 = inputProcessor.addFilterKeysVals(requestParams.getKeys(),
            requestParams.getValues(), keys2, values2);
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
    Integer[] keysInt1 = new Integer[requestParams.getKeys().length];
    Integer[] valuesInt1 = new Integer[requestParams.getValues().length];
    Integer[] keysInt2 = new Integer[keys2.length];
    Integer[] valuesInt2 = new Integer[values2.length];
    if (!requestParams.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    for (int i = 0; i < requestParams.getKeys().length; i++) {
      keysInt1[i] = tt.getOSHDBTagKeyOf(requestParams.getKeys()[i]).toInt();
      if (requestParams.getValues() != null && i < requestParams.getValues().length) {
        valuesInt1[i] =
            tt.getOSHDBTagOf(requestParams.getKeys()[i], requestParams.getValues()[i]).getValue();
      }
    }
    for (int i = 0; i < keys2.length; i++) {
      keysInt2[i] = tt.getOSHDBTagKeyOf(keys2[i]).toInt();
      if (values2 != null && i < values2.length) {
        valuesInt2[i] = tt.getOSHDBTagOf(keys2[i], values2[i]).getValue();
      }
    }
    EnumSet<OSMType> osmTypes1 = ProcessingData.osmTypes;
    inputProcessor.defineOSMTypes(types2);
    EnumSet<OSMType> osmTypes2 = ProcessingData.osmTypes;
    EnumSet<OSMType> osmTypes = osmTypes1.clone();
    osmTypes.addAll(osmTypes2);
    String[] osmTypesString =
        osmTypes.stream().map(OSMType::toString).map(String::toLowerCase).toArray(String[]::new);
    if (!inputProcessor.compareKeysValues(requestParams.getKeys(), keys2, requestParams.getValues(),
        values2)) {
      mapRed =
          inputProcessor.processParameters(new RequestParameters(requestParams.getRequestMethod(),
              requestParams.isSnapshot(), requestParams.isDensity(), requestParams.getBboxes(),
              requestParams.getBcircles(), requestParams.getBpolys(), osmTypesString,
              new String[] {}, new String[] {}, requestParams.getUserids(), requestParams.getTime(),
              requestParams.getShowMetadata()));
      mapRed = mapRed.osmEntityFilter(entity -> {
        boolean matches1 = exeUtils.entityMatches(entity, osmTypes1, keysInt1, valuesInt1);
        boolean matches2 = exeUtils.entityMatches(entity, osmTypes2, keysInt2, valuesInt2);
        return matches1 || matches2;
      });
    } else {
      mapRed = inputProcessor.processParameters(requestParams);
      mapRed = mapRed.osmType(osmTypes);
    }
    ArrayList<Geometry> arrGeoms = new ArrayList<>(ProcessingData.boundaryColl);
    ArrayList<MatchType> zeroFill = new ArrayList<>();
    for (int j = 0; j < arrGeoms.size(); j++) {
      zeroFill.add(MatchType.MATCHESBOTH);
      zeroFill.add(MatchType.MATCHES1);
      zeroFill.add(MatchType.MATCHES2);
    }
    MapAggregator<OSHDBCombinedIndex<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, MatchType>, Geometry> preResult =
        null;
    Map<Integer, P> geoms = arrGeoms.stream()
        .collect(Collectors.toMap(geom -> arrGeoms.indexOf(geom), geom -> (P) geom));
    preResult = mapRed.aggregateByTimestamp().aggregateByGeometry(geoms)
        .aggregateBy((SerializableFunction<OSMEntitySnapshot, MatchType>) f -> {
          OSMEntity entity = f.getEntity();
          boolean matches1 = exeUtils.entityMatches(entity, osmTypes1, keysInt1, valuesInt1);
          boolean matches2 = exeUtils.entityMatches(entity, osmTypes2, keysInt2, valuesInt2);
          if (matches1 && matches2) {
            return MatchType.MATCHESBOTH;
          } else if (matches1) {
            return MatchType.MATCHES1;
          } else if (matches2) {
            return MatchType.MATCHES2;
          } else {
            assert false : "MatchType matches none.";
          }
          return MatchType.MATCHESNONE;
        }, zeroFill).map(x -> x.getGeometry());
    switch (requestResource) {
      case COUNT:
        result = preResult.count();
        break;
      case LENGTH:
        result = preResult.sum(geom -> {
          return Geo.lengthOf(geom);
        });
        break;
      case PERIMETER:
        result = preResult.sum(geom -> {
          if (!(geom instanceof Polygonal)) {
            return 0.0;
          }
          return Geo.lengthOf(geom.getBoundary());
        });
        break;
      case AREA:
        result = preResult.sum(geom -> {
          return Geo.areaOf(geom);
        });
        break;
      default:
        break;
    }
    SortedMap<MatchType, ? extends SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, ? extends Number>> groupByResult;
    InputProcessingUtils utils = inputProcessor.getUtils();
    groupByResult = ExecutionUtils.nest(result);
    String[] boundaryIds = utils.getBoundaryIds();
    Double[] resultValues1 = null;
    Double[] resultValues2 = null;
    String[] timeArray = null;
    boolean timeArrayFilled = false;
    for (Entry<MatchType, ? extends SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, ? extends Number>> entry : groupByResult
        .entrySet()) {
      if (!timeArrayFilled) {
        timeArray = new String[entry.getValue().entrySet().size()];
      }
      if (entry.getKey() == MatchType.MATCHES2) {
        resultValues2 = new Double[entry.getValue().entrySet().size()];
        int value2Count = 0;
        for (Entry<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, ? extends Number> innerEntry : entry
            .getValue().entrySet()) {
          resultValues2[value2Count] =
              Double.parseDouble(df.format(innerEntry.getValue().doubleValue()));
          value2Count++;
        }
      } else if (entry.getKey() == MatchType.MATCHES1) {
        resultValues1 = new Double[entry.getValue().entrySet().size()];
        int value1Count = 0;
        for (Entry<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, ? extends Number> innerEntry : entry
            .getValue().entrySet()) {
          resultValues1[value1Count] =
              Double.parseDouble(df.format(innerEntry.getValue().doubleValue()));
          value1Count++;
        }
      } else if (entry.getKey() == MatchType.MATCHESBOTH) {
        int matchesBothCount = 0;
        int timeArrayCount = 0;
        for (Entry<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, ? extends Number> innerEntry : entry
            .getValue().entrySet()) {
          resultValues1[matchesBothCount] = resultValues1[matchesBothCount]
              + Double.parseDouble(df.format(innerEntry.getValue().doubleValue()));
          resultValues2[matchesBothCount] = resultValues2[matchesBothCount]
              + Double.parseDouble(df.format(innerEntry.getValue().doubleValue()));
          if (!timeArrayFilled) {
            String time = innerEntry.getKey().getFirstIndex().toString();
            if (matchesBothCount == 0 || !timeArray[timeArrayCount - 1].equals(time)) {
              timeArray[timeArrayCount] = innerEntry.getKey().getFirstIndex().toString();
              timeArrayCount++;
            }
          }
          matchesBothCount++;
        }
        timeArray = Arrays.stream(timeArray).filter(Objects::nonNull).toArray(String[]::new);
        timeArrayFilled = true;
      } else {
        // on MatchType.MATCHESNONE aggregated values are not needed / do not exist
      }
    }
    DecimalFormat ratioDf = exeUtils.defineDecimalFormat("#.######");
    return exeUtils.createRatioShareGroupByBoundaryResponse(isShare, requestParams, boundaryIds,
        timeArray, resultValues1, resultValues2, ratioDf, startTime, requestResource, requestUrl,
        new Attribution(URL, TEXT), geoJsonGeoms);
  }
}
