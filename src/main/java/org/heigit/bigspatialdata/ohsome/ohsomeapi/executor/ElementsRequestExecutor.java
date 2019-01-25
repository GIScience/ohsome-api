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

  public static final String URL = ExtractMetadata.attributionUrl;
  public static final String TEXT = ExtractMetadata.attributionShort;
  private static final double MAX_STREAM_DATA_SIZE = 1E7;

  /**
   * Performs an OSM data extraction.
   * 
   * @param elemGeom
   *        {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.rawdata.ElementsGeometry
   *        ElementsGeometry} defining the geometry of the OSM elements
   * @param servletRequest {@link javax.servlet.http.HttpServletRequest HttpServletRequest} incoming
   *        request object
   * @param servletResponse {@link javax.servlet.http.HttpServletResponse HttpServletResponse]}
   *        outgoing response object
   * @throws Exception thrown by
   *         {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters},
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer#stream() stream}, or
   *         {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.ExecutionUtils#streamElementsResponse(HttpServletResponse, DataResponse, boolean, Stream, Stream)
   *         streamElementsResponse}
   */
  public static void executeElements(ElementsGeometry elemGeom, HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    InputProcessor inputProcessor = new InputProcessor(servletRequest, true, false);
    String requestUrl = null;
    if (!servletRequest.getMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    MapReducer<OSMEntitySnapshot> mapRed = null;
    String[] propertiesParam = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("properties")));
    final boolean includeTags =
        Arrays.stream(propertiesParam).anyMatch(p -> p.equalsIgnoreCase("tags"));
    final boolean includeOSMMetadata =
        Arrays.stream(propertiesParam).anyMatch(p -> p.equalsIgnoreCase("metadata"));
    if (DbConnData.db instanceof OSHDBIgnite) {
      final OSHDBIgnite dbIgnite = (OSHDBIgnite) DbConnData.db;
      ComputeMode previousComputeMode = dbIgnite.computeMode();
      // do a preflight to get an approximate result data size estimation:
      // for now just the sum of the average size of the objects versions in bytes is used
      // if that number is larger than 10MB, then fall back to the slightly slower, but much
      // less memory intensive streaming implementation (which is currently only available on
      // the ignite "AffinityCall" backend).
      Number approxResultSize =
          inputProcessor.processParameters().map(data -> ((OSMEntitySnapshot) data).getOSHEntity())
              .sum(data -> data.getLength() / data.getLatest().getVersion());
      if (approxResultSize.doubleValue() > MAX_STREAM_DATA_SIZE) {
        dbIgnite.computeMode(ComputeMode.AffinityCall);
      }
      mapRed = inputProcessor.processParameters();
      dbIgnite.computeMode(previousComputeMode);
    } else {
      mapRed = inputProcessor.processParameters();
    }
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    TagTranslator tt = DbConnData.tagTranslator;
    String[] keys = requestParameters.getKeys();
    int[] keysInt = new int[keys.length];
    if (keys.length != 0) {
      for (int i = 0; i < keys.length; i++) {
        keysInt[i] = tt.getOSHDBTagKeyOf(keys[i]).toInt();
      }
    }
    final MapReducer<Feature> preResult;
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
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
    if (processingData.showMetadata) {
      metadata = new Metadata(null, "OSM data as GeoJSON features.", requestUrl);
    }
    DataResponse osmData = new DataResponse(new Attribution(URL, TEXT), Application.apiVersion,
        metadata, "FeatureCollection", Collections.emptyList());
    exeUtils.streamElementsResponse(servletResponse, osmData, false, streamResult, null);
  }

  /**
   * Performs an OSM data extraction using the full-history of the data.
   * 
   * @param elemGeom
   *        {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.rawdata.ElementsGeometry
   *        ElementsGeometry} defining the geometry of the OSM elements
   * @param servletRequest {@link javax.servlet.http.HttpServletRequest HttpServletRequest} incoming
   *        request object
   * @param servletResponse {@link javax.servlet.http.HttpServletResponse HttpServletResponse]}
   *        outgoing response object
   * @throws Exception thrown by
   *         {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters},
   *         {@link org.heigit.bigspatialdata.oshdb.util.time.ISODateTimeParser#parseISODateTime(String)
   *         parseISODateTime},
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer#stream() stream}, or
   *         {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.ExecutionUtils#streamElementsResponse(HttpServletResponse, DataResponse, boolean, Stream, Stream)
   *         streamElementsResponse}
   */
  public static void executeElementsFullHistory(ElementsGeometry elemGeom,
      HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws Exception {
    InputProcessor inputProcessor = new InputProcessor(servletRequest, false, false);
    String requestUrl = null;
    if (!servletRequest.getMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    MapReducer<OSMEntitySnapshot> mapRedSnapshot = null;
    MapReducer<OSMContribution> mapRedContribution = null;
    String[] propertiesParam = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("properties")));
    final boolean includeTags =
        Arrays.stream(propertiesParam).anyMatch(p -> p.equalsIgnoreCase("tags"));
    final boolean includeOSMMetadata =
        Arrays.stream(propertiesParam).anyMatch(p -> p.equalsIgnoreCase("metadata"));
    if (DbConnData.db instanceof OSHDBIgnite) {
      final OSHDBIgnite dbIgnite = (OSHDBIgnite) DbConnData.db;
      ComputeMode previousComputeMode = dbIgnite.computeMode();
      final double maxStreamDataSize = 1E7;
      Number approxResultSize =
          inputProcessor.processParameters().map(data -> ((OSMEntitySnapshot) data).getOSHEntity())
              .sum(data -> data.getLength() / data.getLatest().getVersion());
      if (approxResultSize.doubleValue() > maxStreamDataSize) {
        dbIgnite.computeMode(ComputeMode.AffinityCall);
      }
      mapRedSnapshot = inputProcessor.processParameters();
      mapRedContribution = inputProcessor.processParameters();
      dbIgnite.computeMode(previousComputeMode);
    } else {
      mapRedSnapshot = inputProcessor.processParameters();
      mapRedContribution = inputProcessor.processParameters();
    }
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    String[] time = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("time")));
    if (time.length != 2) {
      throw new BadRequestException("Wrong time parameter. You need to give exactly two timestamps"
          + "that are ISO-8601 conform, if you want to use the full-history extraction.");
    }
    TagTranslator tt = DbConnData.tagTranslator;
    String[] keys = requestParameters.getKeys();
    int[] keysInt = new int[keys.length];
    if (keys.length != 0) {
      for (int i = 0; i < keys.length; i++) {
        keysInt[i] = tt.getOSHDBTagKeyOf(keys[i]).toInt();
      }
    }
    MapReducer<Feature> contributionPreResult = null;
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    GeoJSONWriter gjw = new GeoJSONWriter();
    RemoteTagTranslator mapTagTranslator = DbConnData.mapTagTranslator;
    String startTimestampWithZ =
        ISODateTimeParser.parseISODateTime(requestParameters.getTime()[0]).toString();
    String endTimestampWithZ =
        ISODateTimeParser.parseISODateTime(requestParameters.getTime()[1]).toString();
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
    if (processingData.showMetadata) {
      metadata = new Metadata(null, "Full-history OSM data as GeoJSON features.", requestUrl);
    }
    DataResponse osmData = new DataResponse(new Attribution(URL, TEXT), Application.apiVersion,
        metadata, "FeatureCollection", Collections.emptyList());
    exeUtils.streamElementsResponse(servletResponse, osmData, true, snapshotStream,
        contributionStream);
  }

  /**
   * Performs a count|length|perimeter|area calculation.
   * 
   * @param requestResource
   *        {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.RequestResource
   *        RequestResource} definition of the request resource
   * @param servletRequest {@link javax.servlet.http.HttpServletRequest HttpServletRequest} incoming
   *        request object
   * @param servletResponse {@link javax.servlet.http.HttpServletResponse HttpServletResponse]}
   *        outgoing response object
   * @param isSnapshot whether this request uses the snapshot-view (true), or contribution-view
   *        (false)
   * @param isDensity whether this request is accessed via the /density resource
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   * @throws Exception thrown by
   *         {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters},
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#count() count}, or
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#sum() sum}
   */
  public static Response executeCountLengthPerimeterArea(RequestResource requestResource,
      HttpServletRequest servletRequest, HttpServletResponse servletResponse, boolean isSnapshot,
      boolean isDensity) throws Exception {
    final long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, ? extends Number> result = null;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor(servletRequest, isSnapshot, isDensity);
    mapRed = inputProcessor.processParameters();
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    String requestUrl = null;
    if (!requestParameters.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    mapRed = inputProcessor.processParameters();
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
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    Geometry geom = inputProcessor.getGeometry();
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    ElementsResult[] resultSet =
        exeUtils.fillElementsResult(result, requestParameters.isDensity(), df, geom);
    Metadata metadata = null;
    if (processingData.showMetadata) {
      long duration = System.currentTimeMillis() - startTime;
      metadata =
          new Metadata(duration, Description.countLengthPerimeterArea(requestParameters.isDensity(),
              requestResource.getLabel(), requestResource.getUnit()), requestUrl);
    }
    if (requestParameters.getFormat() != null
        && requestParameters.getFormat().equalsIgnoreCase("csv")) {
      exeUtils.writeCsvResponse(resultSet, servletResponse,
          exeUtils.createCsvTopComments(URL, TEXT, Application.apiVersion, metadata));
      return null;
    }
    return DefaultAggregationResponse.of(new Attribution(URL, TEXT), Application.apiVersion,
        metadata, resultSet);
  }

  /**
   * Performs a count|length|perimeter|area calculation grouped by the boundary.
   * 
   * @param requestResource
   *        {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.RequestResource
   *        RequestResource} definition of the request resource
   * @param servletRequest {@link javax.servlet.http.HttpServletRequest HttpServletRequest} incoming
   *        request object
   * @param servletResponse {@link javax.servlet.http.HttpServletResponse HttpServletResponse]}
   *        outgoing response object
   * @param isSnapshot whether this request uses the snapshot-view (true), or contribution-view
   *        (false)
   * @param isDensity whether this request is accessed via the /density resource
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   * @throws Exception thrown by
   *         {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters} and
   *         {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.ExecutionUtils#computeCountLengthPerimeterAreaGbB(RequestResource, BoundaryType, MapReducer)
   *         computeCountLengthPerimeterAreaGbB}
   */
  public static Response executeCountLengthPerimeterAreaGroupByBoundary(
      RequestResource requestResource, HttpServletRequest servletRequest,
      HttpServletResponse servletResponse, boolean isSnapshot, boolean isDensity) throws Exception {
    final long startTime = System.currentTimeMillis();
    SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, ? extends Number> result;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor(servletRequest, isSnapshot, isDensity);
    mapRed = inputProcessor.processParameters();
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    String requestUrl = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    if (!requestParameters.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    mapRed = inputProcessor.processParameters();
    switch (requestResource) {
      case COUNT:
      default:
        result = exeUtils.computeCountLengthPerimeterAreaGbB(RequestResource.COUNT,
            processingData.boundary, mapRed);
        break;
      case LENGTH:
        result = exeUtils.computeCountLengthPerimeterAreaGbB(RequestResource.LENGTH,
            processingData.boundary, mapRed);
        break;
      case PERIMETER:
        result = exeUtils.computeCountLengthPerimeterAreaGbB(RequestResource.PERIMETER,
            processingData.boundary, mapRed);
        break;
      case AREA:
        result = exeUtils.computeCountLengthPerimeterAreaGbB(RequestResource.AREA,
            processingData.boundary, mapRed);
        break;
    }
    SortedMap<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    Object groupByName;
    InputProcessingUtils utils = inputProcessor.getUtils();
    Object[] boundaryIds = utils.getBoundaryIds();
    int count = 0;
    ArrayList<Geometry> boundaries = new ArrayList<>(processingData.boundaryColl);
    for (Entry<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      ElementsResult[] results = exeUtils.fillElementsResult(entry.getValue(),
          requestParameters.isDensity(), df, boundaries.get(count));
      groupByName = boundaryIds[count];
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    Metadata metadata = null;
    if (processingData.showMetadata) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration,
          Description.countLengthPerimeterAreaGroupByBoundary(requestParameters.isDensity(),
              requestResource.getLabel(), requestResource.getUnit()),
          requestUrl);
    }
    if (requestParameters.getFormat() != null) {
      if (requestParameters.getFormat().equalsIgnoreCase("geojson")) {
        return GroupByResponse.of(new Attribution(URL, TEXT), Application.apiVersion, metadata,
            "FeatureCollection",
            exeUtils.createGeoJsonFeatures(resultSet, processingData.geoJsonGeoms));
      } else if (requestParameters.getFormat().equalsIgnoreCase("csv")) {
        exeUtils.writeCsvResponse(resultSet, servletResponse,
            exeUtils.createCsvTopComments(URL, TEXT, Application.apiVersion, metadata));
        return null;
      }
    }
    return new GroupByResponse(new Attribution(URL, TEXT), Application.apiVersion, metadata,
        resultSet);
  }

  /**
   * Performs a count|length|perimeter|area calculation grouped by the user.
   * 
   * @param requestResource
   *        {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.RequestResource
   *        RequestResource} definition of the request resource
   * @param servletRequest {@link javax.servlet.http.HttpServletRequest HttpServletRequest} incoming
   *        request object
   * @param servletResponse {@link javax.servlet.http.HttpServletResponse HttpServletResponse]}
   *        outgoing response object
   * @param isSnapshot whether this request uses the snapshot-view (true), or contribution-view
   *        (false)
   * @param isDensity whether this request is accessed via the /density resource
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   * @throws Exception thrown by
   *         {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters} and
   *         {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.ExecutionUtils#computeResult(RequestResource, MapAggregator)
   *         computeResult}
   */
  public static Response executeCountLengthPerimeterAreaGroupByUser(RequestResource requestResource,
      HttpServletRequest servletRequest, HttpServletResponse servletResponse, boolean isSnapshot,
      boolean isDensity) throws Exception {
    final long startTime = System.currentTimeMillis();
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor(servletRequest, isSnapshot, isDensity);
    mapRed = inputProcessor.processParameters();
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    String requestUrl = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    ArrayList<Integer> useridsInt = new ArrayList<>();
    if (!requestParameters.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    if (requestParameters.getUserids() != null) {
      for (String user : requestParameters.getUserids()) {
        useridsInt.add(Integer.parseInt(user));
      }
    }
    SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, ? extends Number> result;
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
          exeUtils.fillElementsResult(entry.getValue(), requestParameters.isDensity(), df, null);
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    Metadata metadata = null;
    if (processingData.showMetadata) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, Description.countLengthPerimeterAreaGroupByUser(
          requestResource.getLabel(), requestResource.getUnit()), requestUrl);
    }
    if (requestParameters.getFormat() != null
        && requestParameters.getFormat().equalsIgnoreCase("csv")) {
      exeUtils.writeCsvResponse(resultSet, servletResponse,
          exeUtils.createCsvTopComments(URL, TEXT, Application.apiVersion, metadata));
      return null;
    }
    return new GroupByResponse(new Attribution(URL, TEXT), Application.apiVersion, metadata,
        resultSet);
  }

  /**
   * Performs a count|length|perimeter|area calculation grouped by the tag.
   * 
   * @param requestResource
   *        {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.RequestResource
   *        RequestResource} definition of the request resource
   * @param servletRequest {@link javax.servlet.http.HttpServletRequest HttpServletRequest} incoming
   *        request object
   * @param servletResponse {@link javax.servlet.http.HttpServletResponse HttpServletResponse]}
   *        outgoing response object
   * @param isSnapshot whether this request uses the snapshot-view (true), or contribution-view
   *        (false)
   * @param isDensity whether this request is accessed via the /density resource
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   * @throws Exception thrown by
   *         {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters} and
   *         {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.ExecutionUtils#computeResult(RequestResource, MapAggregator)
   *         computeResult}
   */
  public static Response executeCountLengthPerimeterAreaGroupByTag(RequestResource requestResource,
      HttpServletRequest servletRequest, HttpServletResponse servletResponse, boolean isSnapshot,
      boolean isDensity) throws Exception {
    final long startTime = System.currentTimeMillis();
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor(servletRequest, isSnapshot, isDensity);
    String[] groupByKey = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByKey")));
    String[] groupByValues = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByValues")));
    if (groupByKey == null || groupByKey.length != 1) {
      throw new BadRequestException(ExceptionMessages.groupByKeyParam);
    }
    mapRed = inputProcessor.processParameters();
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
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
    SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, Pair<Integer, Integer>>, ? extends Number> result;
    SortedMap<Pair<Integer, Integer>, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    result = exeUtils.computeResult(requestResource, preResult);
    groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    Geometry geom = inputProcessor.getGeometry();
    int count = 0;
    for (Entry<Pair<Integer, Integer>, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      ElementsResult[] results =
          exeUtils.fillElementsResult(entry.getValue(), requestParameters.isDensity(), df, geom);
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
    if (processingData.showMetadata) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration,
          Description.countLengthPerimeterAreaGroupByTag(requestParameters.isDensity(),
              requestResource.getLabel(), requestResource.getUnit()),
          requestUrl);
    }
    if (requestParameters.getFormat() != null
        && requestParameters.getFormat().equalsIgnoreCase("csv")) {
      exeUtils.writeCsvResponse(resultSet, servletResponse,
          exeUtils.createCsvTopComments(URL, TEXT, Application.apiVersion, metadata));
      return null;
    }
    return new GroupByResponse(new Attribution(URL, TEXT), Application.apiVersion, metadata,
        resultSet);
  }

  /**
   * Performs a count|perimeter|area calculation grouped by the OSM type.
   * 
   * @param requestResource
   *        {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.RequestResource
   *        RequestResource} definition of the request resource
   * @param servletRequest {@link javax.servlet.http.HttpServletRequest HttpServletRequest} incoming
   *        request object
   * @param servletResponse {@link javax.servlet.http.HttpServletResponse HttpServletResponse]}
   *        outgoing response object
   * @param isSnapshot whether this request uses the snapshot-view (true), or contribution-view
   *        (false)
   * @param isDensity whether this request is accessed via the /density resource
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   * @throws Exception thrown by
   *         {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters} and
   *         {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.ExecutionUtils#computeResult(RequestResource, MapAggregator)
   *         computeResult}
   */
  public static Response executeCountPerimeterAreaGroupByType(RequestResource requestResource,
      HttpServletRequest servletRequest, HttpServletResponse servletResponse, boolean isSnapshot,
      boolean isDensity) throws Exception {
    final long startTime = System.currentTimeMillis();
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor(servletRequest, isSnapshot, isDensity);
    mapRed = inputProcessor.processParameters();
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    String requestUrl = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    if (!requestParameters.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, OSMType>, OSMEntitySnapshot> preResult;
    preResult = mapRed.aggregateByTimestamp()
        .aggregateBy((SerializableFunction<OSMEntitySnapshot, OSMType>) f -> {
          return f.getEntity().getType();
        }, processingData.osmTypes);
    SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, OSMType>, ? extends Number> result;
    result = exeUtils.computeResult(requestResource, preResult);
    SortedMap<OSMType, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    Geometry geom = inputProcessor.getGeometry();
    int count = 0;
    for (Entry<OSMType, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      ElementsResult[] results =
          exeUtils.fillElementsResult(entry.getValue(), requestParameters.isDensity(), df, geom);
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    Metadata metadata = null;
    if (processingData.showMetadata) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration,
          Description.countPerimeterAreaGroupByType(requestParameters.isDensity(),
              requestResource.getLabel(), requestResource.getUnit()),
          requestUrl);
    }
    if (requestParameters.getFormat() != null
        && requestParameters.getFormat().equalsIgnoreCase("csv")) {
      exeUtils.writeCsvResponse(resultSet, servletResponse,
          exeUtils.createCsvTopComments(URL, TEXT, Application.apiVersion, metadata));
      return null;
    }
    return new GroupByResponse(new Attribution(URL, TEXT), Application.apiVersion, metadata,
        resultSet);
  }

  /**
   * Performs a count|length|perimeter|area calculation grouped by the key.
   * 
   * @param requestResource
   *        {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.RequestResource
   *        RequestResource} definition of the request resource
   * @param servletRequest {@link javax.servlet.http.HttpServletRequest HttpServletRequest} incoming
   *        request object
   * @param servletResponse {@link javax.servlet.http.HttpServletResponse HttpServletResponse]}
   *        outgoing response object
   * @param isSnapshot whether this request uses the snapshot-view (true), or contribution-view
   *        (false)
   * @param isDensity whether this request is accessed via the /density resource
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   * @throws Exception thrown by
   *         {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters} and
   *         {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.ExecutionUtils#computeResult(RequestResource, MapAggregator)
   *         computeResult}
   */
  public static Response executeCountLengthPerimeterAreaGroupByKey(RequestResource requestResource,
      HttpServletRequest servletRequest, HttpServletResponse servletResponse, boolean isSnapshot,
      boolean isDensity) throws Exception {
    final long startTime = System.currentTimeMillis();
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor(servletRequest, isSnapshot, isDensity);
    String[] groupByKeys = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByKeys")));
    if (groupByKeys == null || groupByKeys.length == 0) {
      throw new BadRequestException(ExceptionMessages.groupByKeysParam);
    }
    mapRed = inputProcessor.processParameters();
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    String requestUrl = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    if (!requestParameters.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    TagTranslator tt = DbConnData.tagTranslator;
    Integer[] keysInt = new Integer[groupByKeys.length];
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
    SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, ? extends Number> result;
    SortedMap<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> groupByResult;
    result = exeUtils.computeResult(requestResource, preResult);
    groupByResult = ExecutionUtils.nest(result);
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    int count = 0;
    for (Entry<Integer, ? extends SortedMap<OSHDBTimestamp, ? extends Number>> entry : groupByResult
        .entrySet()) {
      ElementsResult[] results =
          exeUtils.fillElementsResult(entry.getValue(), requestParameters.isDensity(), df, null);
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
    if (processingData.showMetadata) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, Description.countLengthPerimeterAreaGroupByKey(
          requestResource.getLabel(), requestResource.getUnit()), requestUrl);
    }
    if (requestParameters.getFormat() != null
        && requestParameters.getFormat().equalsIgnoreCase("csv")) {
      exeUtils.writeCsvResponse(resultSet, servletResponse,
          exeUtils.createCsvTopComments(URL, TEXT, Application.apiVersion, metadata));
      return null;
    }
    return new GroupByResponse(new Attribution(URL, TEXT), Application.apiVersion, metadata,
        resultSet);
  }

  /**
   * Performs a count|length|perimeter|area-share|ratio calculation.
   * 
   * @param requestResource
   *        {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.RequestResource
   *        RequestResource} definition of the request resource
   * @param servletRequest {@link javax.servlet.http.HttpServletRequest HttpServletRequest} incoming
   *        request object
   * @param servletResponse {@link javax.servlet.http.HttpServletResponse HttpServletResponse]}
   *        outgoing response object
   * @param isSnapshot whether this request uses the snapshot-view (true), or contribution-view
   *        (false)
   * @param isDensity whether this request is accessed via the /density resource
   * @param isShare whether this request is accessed via the /share (true), or /ratio (false)
   *        resource
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   * @throws Exception thrown by
   *         {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters} and
   *         {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.ExecutionUtils#computeResult(RequestResource, MapAggregator)
   *         computeResult}
   */
  public static Response executeCountLengthPerimeterAreaShareRatio(RequestResource requestResource,
      HttpServletRequest servletRequest, HttpServletResponse servletResponse, boolean isSnapshot,
      boolean isDensity, boolean isShare) throws Exception {
    final long startTime = System.currentTimeMillis();
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor(servletRequest, isSnapshot, isDensity);
    mapRed = inputProcessor.processParameters();
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    String requestUrl = null;
    TagTranslator tt = DbConnData.tagTranslator;
    String[] keys2 = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("keys2")));
    String[] values2 = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("values2")));
    inputProcessor.checkKeysValues(keys2, values2);
    Pair<String[], String[]> keys2Vals2 =
        inputProcessor.processKeys2Vals2(keys2, values2, isShare, requestParameters);
    keys2 = keys2Vals2.getKey();
    values2 = keys2Vals2.getValue();
    Integer[] keysInt1 = new Integer[requestParameters.getKeys().length];
    Integer[] valuesInt1 = new Integer[requestParameters.getValues().length];
    Integer[] keysInt2 = new Integer[keys2.length];
    Integer[] valuesInt2 = new Integer[values2.length];
    if (!requestParameters.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    for (int i = 0; i < requestParameters.getKeys().length; i++) {
      keysInt1[i] = tt.getOSHDBTagKeyOf(requestParameters.getKeys()[i]).toInt();
      if (requestParameters.getValues() != null && i < requestParameters.getValues().length) {
        valuesInt1[i] =
            tt.getOSHDBTagOf(requestParameters.getKeys()[i], requestParameters.getValues()[i])
                .getValue();
      }
    }
    for (int i = 0; i < keys2.length; i++) {
      keysInt2[i] = tt.getOSHDBTagKeyOf(keys2[i]).toInt();
      if (i < values2.length) {
        valuesInt2[i] = tt.getOSHDBTagOf(keys2[i], values2[i]).getValue();
      }
    }
    EnumSet<OSMType> osmTypes1 = inputProcessor.getProcessingData().osmTypes;
    if (!isShare) {
      inputProcessor.defineOSMTypes(servletRequest.getParameterValues("types2"));
    }
    EnumSet<OSMType> osmTypes2 = inputProcessor.getProcessingData().osmTypes;
    EnumSet<OSMType> osmTypes = osmTypes1.clone();
    osmTypes.addAll(osmTypes2);
    String[] osmTypesString =
        osmTypes.stream().map(OSMType::toString).map(String::toLowerCase).toArray(String[]::new);
    if (!inputProcessor.compareKeysValues(requestParameters.getKeys(), keys2,
        requestParameters.getValues(), values2)) {
      RequestParameters requestParams = new RequestParameters(servletRequest.getMethod(),
          isSnapshot, isDensity, servletRequest.getParameter("bboxes"),
          servletRequest.getParameter("bcircles"), servletRequest.getParameter("bpolys"),
          osmTypesString, new String[] {}, new String[] {},
          servletRequest.getParameterValues("userids"), servletRequest.getParameterValues("time"),
          servletRequest.getParameter("format"), servletRequest.getParameter("showMetadata"));
      ProcessingData pD = new ProcessingData(requestParams);
      InputProcessor iP = new InputProcessor(servletRequest, isSnapshot, isDensity);
      iP.setProcessingData(pD);
      mapRed = iP.processParameters();
      mapRed = mapRed.osmEntityFilter(entity -> {
        if (!exeUtils.entityMatches(entity, osmTypes1, keysInt1, valuesInt1)) {
          return exeUtils.entityMatches(entity, osmTypes2, keysInt2, valuesInt2);
        }
        return true;
      });
    } else {
      mapRed = inputProcessor.processParameters();
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
        requestResource, requestUrl, requestParameters, new Attribution(URL, TEXT),
        servletResponse);
  }

  /**
   * Performs a count|length|perimeter|area-ratio calculation grouped by the boundary.
   * 
   * @param requestResource
   *        {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.RequestResource
   *        RequestResource} definition of the request resource
   * @param servletRequest {@link javax.servlet.http.HttpServletRequest HttpServletRequest} incoming
   *        request object
   * @param servletResponse {@link javax.servlet.http.HttpServletResponse HttpServletResponse]}
   *        outgoing response object
   * @param isSnapshot whether this request uses the snapshot-view (true), or contribution-view
   *        (false)
   * @param isDensity whether this request is accessed via the /density resource
   * @param isShare whether this request is accessed via the /share (true), or /ratio (false)
   *        resource
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   * @throws Exception thrown by
   *         {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters},
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#count() count}, or
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#sum() sum}
   */
  @SuppressWarnings({"unchecked"}) // intentionally as check for P on Polygonal is already performed
  public static <P extends Geometry & Polygonal> Response executeCountLengthPerimeterAreaShareRatioGroupByBoundary(
      RequestResource requestResource, HttpServletRequest servletRequest,
      HttpServletResponse servletResponse, boolean isSnapshot, boolean isDensity, boolean isShare)
      throws Exception {
    final long startTime = System.currentTimeMillis();
    SortedMap<OSHDBCombinedIndex<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, MatchType>, ? extends Number> result =
        null;
    MapReducer<OSMEntitySnapshot> mapRed = null;
    InputProcessor inputProcessor = new InputProcessor(servletRequest, isSnapshot, isDensity);
    inputProcessor.processParameters();
    ProcessingData processingData = inputProcessor.getProcessingData();
    RequestParameters requestParameters = processingData.getRequestParameters();
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    String requestUrl = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    TagTranslator tt = DbConnData.tagTranslator;
    if (processingData.boundary == BoundaryType.NOBOUNDARY) {
      throw new BadRequestException(ExceptionMessages.noBoundary);
    }
    final GeoJsonObject[] geoJsonGeoms = processingData.geoJsonGeoms;
    String[] keys2 = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("keys2")));
    String[] values2 = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("values2")));
    inputProcessor.checkKeysValues(keys2, values2);
    Pair<String[], String[]> keys2Vals2 =
        inputProcessor.processKeys2Vals2(keys2, values2, isShare, requestParameters);
    keys2 = keys2Vals2.getKey();
    values2 = keys2Vals2.getValue();
    Integer[] keysInt1 = new Integer[requestParameters.getKeys().length];
    Integer[] valuesInt1 = new Integer[requestParameters.getValues().length];
    Integer[] keysInt2 = new Integer[keys2.length];
    Integer[] valuesInt2 = new Integer[values2.length];
    if (!requestParameters.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    for (int i = 0; i < requestParameters.getKeys().length; i++) {
      keysInt1[i] = tt.getOSHDBTagKeyOf(requestParameters.getKeys()[i]).toInt();
      if (requestParameters.getValues() != null && i < requestParameters.getValues().length) {
        valuesInt1[i] =
            tt.getOSHDBTagOf(requestParameters.getKeys()[i], requestParameters.getValues()[i])
                .getValue();
      }
    }
    for (int i = 0; i < keys2.length; i++) {
      keysInt2[i] = tt.getOSHDBTagKeyOf(keys2[i]).toInt();
      if (values2 != null && i < values2.length) {
        valuesInt2[i] = tt.getOSHDBTagOf(keys2[i], values2[i]).getValue();
      }
    }
    EnumSet<OSMType> osmTypes1 = processingData.osmTypes;
    if (!isShare) {
      inputProcessor.defineOSMTypes(servletRequest.getParameterValues("types2"));
    }
    EnumSet<OSMType> osmTypes2 = inputProcessor.getProcessingData().osmTypes;
    EnumSet<OSMType> osmTypes = osmTypes1.clone();
    osmTypes.addAll(osmTypes2);
    String[] osmTypesString =
        osmTypes.stream().map(OSMType::toString).map(String::toLowerCase).toArray(String[]::new);
    if (!inputProcessor.compareKeysValues(requestParameters.getKeys(), keys2,
        requestParameters.getValues(), values2)) {
      RequestParameters requestParams = new RequestParameters(servletRequest.getMethod(),
          isSnapshot, isDensity, servletRequest.getParameter("bboxes"),
          servletRequest.getParameter("bcircles"), servletRequest.getParameter("bpolys"),
          osmTypesString, new String[] {}, new String[] {},
          servletRequest.getParameterValues("userids"), servletRequest.getParameterValues("time"),
          servletRequest.getParameter("format"), servletRequest.getParameter("showMetadata"));
      ProcessingData pD = new ProcessingData(requestParams);
      InputProcessor iP = new InputProcessor(servletRequest, isSnapshot, isDensity);
      iP.setProcessingData(pD);
      mapRed = iP.processParameters();
      mapRed = mapRed.osmEntityFilter(entity -> {
        boolean matches1 = exeUtils.entityMatches(entity, osmTypes1, keysInt1, valuesInt1);
        boolean matches2 = exeUtils.entityMatches(entity, osmTypes2, keysInt2, valuesInt2);
        return matches1 || matches2;
      });
    } else {
      mapRed = inputProcessor.processParameters();
      mapRed = mapRed.osmType(osmTypes);
    }
    ArrayList<Geometry> arrGeoms = new ArrayList<>(processingData.boundaryColl);
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
    Object[] boundaryIds = utils.getBoundaryIds();
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
    return exeUtils.createRatioShareGroupByBoundaryResponse(isShare, requestParameters, boundaryIds,
        timeArray, resultValues1, resultValues2, ratioDf, startTime, requestResource, requestUrl,
        new Attribution(URL, TEXT), geoJsonGeoms, servletResponse);
  }
}
