package org.heigit.bigspatialdata.ohsome.ohsomeapi.executor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.ServletOutputStream;
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
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.GeometryBuilder;
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
import org.heigit.bigspatialdata.oshdb.api.object.OSHDBMapReducible;
import org.heigit.bigspatialdata.oshdb.api.object.OSMContribution;
import org.heigit.bigspatialdata.oshdb.api.object.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.osm.OSMEntity;
import org.heigit.bigspatialdata.oshdb.osm.OSMType;
import org.heigit.bigspatialdata.oshdb.util.OSHDBTag;
import org.heigit.bigspatialdata.oshdb.util.OSHDBTimestamp;
import org.heigit.bigspatialdata.oshdb.util.celliterator.ContributionType;
import org.heigit.bigspatialdata.oshdb.util.geometry.Geo;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;
import org.heigit.bigspatialdata.oshdb.util.time.TimestampFormatter;
import org.wololo.geojson.Feature;
import org.wololo.jts2geojson.GeoJSONWriter;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygonal;

/** Includes all execute methods for requests mapped to /elements. */
public class ElementsRequestExecutor {

  private static final String url = ExtractMetadata.attributionUrl;
  private static final String text = ExtractMetadata.attributionShort;

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
  @SuppressWarnings("unchecked") // intentionally suppressed
  public static void executeElements(RequestParameters requestParams, ElementsGeometry elemGeom,
      String[] propertiesParameter, HttpServletResponse response)
      throws UnsupportedOperationException, Exception {
    InputProcessor inputProcessor = new InputProcessor();
    String requestUrl = null;
    boolean isSnapshot = requestParams.isSnapshot();
    if (!requestParams.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    MapReducer<? extends OSHDBMapReducible> mapRed = null;
    if (isSnapshot) {
      mapRed = (MapReducer<OSMEntitySnapshot>) mapRed;
    } else {
      mapRed = (MapReducer<OSMContribution>) mapRed;
    }
    boolean iT = false;
    boolean iOM = false;
    for (String text : propertiesParameter) {
      if (text.equalsIgnoreCase("tags")) {
        iT = true;
      } else if (text.equalsIgnoreCase("metadata")) {
        iOM = true;
      }
    }
    final boolean includeTags = iT;
    final boolean includeOSMMetadata = iOM;
    if (DbConnData.db instanceof OSHDBIgnite) {
      final OSHDBIgnite dbIgnite = (OSHDBIgnite) DbConnData.db;
      ComputeMode previousCM = dbIgnite.computeMode();
      // do a preflight to get an approximate result data size estimation:
      // for now just the sum of the average size of the objects versions in bytes is used
      // if that number is larger than 10MB, then fall back to the slightly slower, but much
      // less memory intensive streaming implementation (which is currently only available on
      // the ignite "AffinityCall" backend).
      final double MAX_STREAM_DATA_SIZE = 1E7;
      Number approxResultSize = inputProcessor.processParameters(mapRed, requestParams)
          .map(data -> ((OSMEntitySnapshot) data).getOSHEntity())
          .sum(data -> data.getLength() / data.getLatest().getVersion());
      if (approxResultSize.doubleValue() > MAX_STREAM_DATA_SIZE) {
        dbIgnite.computeMode(ComputeMode.AffinityCall);
      }
      mapRed = inputProcessor.processParameters(mapRed, requestParams);
      dbIgnite.computeMode(previousCM);
    } else {
      mapRed = inputProcessor.processParameters(mapRed, requestParams);
    }
    TagTranslator tt = DbConnData.tagTranslator;
    String[] keys = requestParams.getKeys();
    String[] values = requestParams.getValues();
    int[] keysInt = new int[keys.length];
    int[] valuesInt = new int[values.length];
    if (keys.length != 0) {
      for (int i = 0; i < keys.length; i++) {
        keysInt[i] = tt.getOSHDBTagKeyOf(keys[i]).toInt();
        if (values.length != 0 && i < values.length) {
          // works as the relation between keys:values must be n:(m<=n)
          valuesInt[i] = tt.getOSHDBTagOf(keys[i], values[i]).getValue();
        }
      }
    }
    final MapReducer<Feature> preResult;
    ExecutionUtils exeUtils = new ExecutionUtils();
    GeoJSONWriter gjw = new GeoJSONWriter();
    RemoteTagTranslator mapTagTranslator = DbConnData.mapTagTranslator;
    if (includeOSMMetadata) {
      preResult = mapRed.map(view -> {
        Map<String, Object> properties = new TreeMap<>();
        if (isSnapshot) {
          properties.put("version", ((OSMEntitySnapshot) view).getEntity().getVersion());
          properties.put("osmType", ((OSMEntitySnapshot) view).getEntity().getType());
          properties.put("lastEdit",
              ((OSMEntitySnapshot) view).getEntity().getTimestamp().toString());
          properties.put("changesetId", ((OSMEntitySnapshot) view).getEntity().getChangeset());
          return exeUtils.createOSMDataFeature(keys, values, mapTagTranslator.get(), keysInt,
              valuesInt, view, isSnapshot, properties, gjw, includeTags, elemGeom);
        } else {
          if (((OSMContribution) view).getContributionTypes().contains(ContributionType.DELETION)) {
            return null;
          } else {
            properties.put("version", ((OSMContribution) view).getEntityAfter().getVersion());
            properties.put("osmType", ((OSMContribution) view).getEntityAfter().getType());
            properties.put("changesetId", ((OSMContribution) view).getEntityAfter().getChangeset());
            return exeUtils.createOSMDataFeature(keys, values, mapTagTranslator.get(), keysInt,
                valuesInt, view, isSnapshot, properties, gjw, includeTags, elemGeom);
          }
        }
      });
    } else {
      preResult = mapRed.map(view -> {
        Map<String, Object> properties = new TreeMap<>();
        return exeUtils.createOSMDataFeature(keys, values, mapTagTranslator.get(), keysInt,
            valuesInt, view, isSnapshot, properties, gjw, includeTags, elemGeom);
      });
    }

    Stream<Feature> streamResult = preResult.stream().filter(feature -> {
      if (feature == null)
        return false;
      return true;
    });
    Metadata metadata = null;
    if (ProcessingData.showMetadata) {
      metadata = new Metadata(null, "OSM data as GeoJSON features.", requestUrl);
    }
    DataResponse osmData = new DataResponse(new Attribution(url, text), Application.apiVersion,
        metadata, "FeatureCollection", Collections.emptyList());
    JsonFactory jsonFactory = new JsonFactory();
    ByteArrayOutputStream tempStream = new ByteArrayOutputStream();

    ObjectMapper objMapper = new ObjectMapper();
    objMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objMapper.setSerializationInclusion(Include.NON_NULL);
    jsonFactory.createGenerator(tempStream, JsonEncoding.UTF8).setCodec(objMapper)
        .writeObject(osmData);

    String scaffold = tempStream.toString("UTF-8").replaceFirst("]\\r?\\n?\\W*}\\r?\\n?\\W*$", "");

    response.addHeader("Content-disposition", "attachment;filename=ohsome.geojson");
    response.setContentType("application/geo+json; charset=utf-8");
    ServletOutputStream outputStream = response.getOutputStream();
    outputStream.write(scaffold.getBytes("UTF-8"));

    ThreadLocal<ByteArrayOutputStream> outputBuffers =
        ThreadLocal.withInitial(ByteArrayOutputStream::new);
    ThreadLocal<JsonGenerator> outputJsonGen = ThreadLocal.withInitial(() -> {
      try {
        return jsonFactory.createGenerator(outputBuffers.get(), JsonEncoding.UTF8)
            .setCodec(objMapper);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
    outputStream.print("\n");
    AtomicReference<Boolean> isFirst = new AtomicReference<>(true);
    streamResult.map(data -> {
      try {
        outputBuffers.get().reset();
        outputJsonGen.get().writeObject(data);
        return outputBuffers.get().toByteArray();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }).sequential().forEach(data -> {
      try {
        if (isFirst.get()) {
          isFirst.set(false);
        } else {
          outputStream.print(",");
        }
        outputStream.write(data);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
    outputStream.print("\n  ]\n}\n");
    response.flushBuffer();
  }

  public static void executeElementsFullHistory(RequestParameters requestParams,
      ElementsGeometry elemGeom, String[] propertiesParameter, HttpServletResponse response)
      throws UnsupportedOperationException, Exception {
    InputProcessor inputProcessor = new InputProcessor();
    String requestUrl = null;
    if (!requestParams.getRequestMethod().equalsIgnoreCase("post")) {
      requestUrl = RequestInterceptor.requestUrl;
    }
    MapReducer<OSMEntitySnapshot> mapRedSnapshot = null;
    MapReducer<OSMContribution> mapRedContribution = null;

    boolean iT = false;
    boolean iOM = false;
    for (String text : propertiesParameter) {
      if (text.equalsIgnoreCase("tags")) {
        iT = true;
      } else if (text.equalsIgnoreCase("metadata")) {
        iOM = true;
      }
    }
    final boolean includeTags = iT;
    final boolean includeOSMMetadata = iOM;
    if (DbConnData.db instanceof OSHDBIgnite) {
      final OSHDBIgnite dbIgnite = (OSHDBIgnite) DbConnData.db;
      ComputeMode previousCM = dbIgnite.computeMode();
      final double MAX_STREAM_DATA_SIZE = 1E7;
      Number approxResultSize = inputProcessor.processParameters(mapRedSnapshot, requestParams)
          .map(data -> ((OSMEntitySnapshot) data).getOSHEntity())
          .sum(data -> data.getLength() / data.getLatest().getVersion());
      if (approxResultSize.doubleValue() > MAX_STREAM_DATA_SIZE) {
        dbIgnite.computeMode(ComputeMode.AffinityCall);
      }
      mapRedSnapshot = inputProcessor.processParameters(mapRedSnapshot, requestParams);
      mapRedContribution = inputProcessor.processParameters(mapRedContribution, requestParams);
      dbIgnite.computeMode(previousCM);
    } else {
      mapRedSnapshot = inputProcessor.processParameters(mapRedSnapshot, requestParams);
      mapRedContribution = inputProcessor.processParameters(mapRedContribution, requestParams);
    }
    TagTranslator tt = DbConnData.tagTranslator;
    String[] keys = requestParams.getKeys();
    String[] values = requestParams.getValues();
    int[] keysInt = new int[keys.length];
    int[] valuesInt = new int[values.length];
    if (keys.length != 0) {
      for (int i = 0; i < keys.length; i++) {
        keysInt[i] = tt.getOSHDBTagKeyOf(keys[i]).toInt();
        if (values.length != 0 && i < values.length) {
          // works as the relation between keys:values must be n:(m<=n)
          valuesInt[i] = tt.getOSHDBTagOf(keys[i], values[i]).getValue();
        }
      }
    }
    MapReducer<Feature> preResult = null;
    ExecutionUtils exeUtils = new ExecutionUtils();
    GeoJSONWriter gjw = new GeoJSONWriter();
    RemoteTagTranslator mapTagTranslator = DbConnData.mapTagTranslator;
    
    
    preResult = mapRedContribution.groupByEntity().flatMap(contributions -> {
      Map<String, Object> properties = new TreeMap<>();
      List<Feature> output = new LinkedList<>();
      int startIndex = 1;
      // first contribution:
      // if not "creation": take "before" as starting "row" (geom, tags), validFrom = t_start
      if (!contributions.get(0).getContributionTypes().contains(ContributionType.CREATION)) {
        properties.put("validFrom", "useFirstTimestampFromInputParameterHere");
        properties.put("version", contributions.get(0).getEntityBefore().getVersion());
        properties.put("osmType", contributions.get(0).getEntityBefore().getType());
        properties.put("changesetId", contributions.get(0).getEntityBefore().getChangeset());
      } else {
        // if creation
        properties.put("validFrom",
            contributions.get(0).getEntityAfter().getTimestamp().toString());
        properties.put("version", contributions.get(0).getEntityAfter().getVersion());
        properties.put("osmType", contributions.get(0).getEntityAfter().getType());
        properties.put("changesetId", contributions.get(0).getEntityAfter().getChangeset());
        if (contributions.size() == 1) {
          // use latest timestamp from input parameter for validTo
          properties.put("validTo", "useLastTimestampFromInputParameterHere");
          output.add(exeUtils.createOSMDataFeature(keys, values, mapTagTranslator.get(), keysInt,
              valuesInt, contributions.get(0), false, properties, gjw, includeTags, elemGeom));
          return output;
        } else {
          // use timestamp of contribution 2 for validTo
          properties.put("validTo",
              contributions.get(1).getEntityAfter().getTimestamp().toString());
          output.add(exeUtils.createOSMDataFeature(keys, values, mapTagTranslator.get(), keysInt,
              valuesInt, contributions.get(0), false, properties, gjw, includeTags, elemGeom));
        }
        if (!contributions.get(1).getContributionTypes().contains(ContributionType.DELETION)) {
          properties.put("validFrom",
              contributions.get(1).getEntityAfter().getTimestamp().toString());
          properties.put("version", contributions.get(1).getEntityAfter().getVersion());
          properties.put("osmType", contributions.get(1).getEntityAfter().getType());
          properties.put("changesetId", contributions.get(1).getEntityAfter().getChangeset());
        }
        // to skip the 2nd contribution in the following loop
        startIndex = 2;
      }
      for (int i = startIndex; i < contributions.size(); i++) {
        // for each contribution:
        // set valid_to of previous row, add to output list (output.add(…))
        properties.put("validTo", contributions.get(i).getEntityAfter().getTimestamp().toString());
        output.add(exeUtils.createOSMDataFeature(keys, values, mapTagTranslator.get(), keysInt,
            valuesInt, contributions.get(0), false, properties, gjw, includeTags, elemGeom));
        // if deletion: skip output of next row
        if (contributions.get(i).getContributionTypes().contains(ContributionType.DELETION)) {
          i++;
        } else {
          // else: take "after" as next row
          properties.put("validFrom",
              contributions.get(i).getEntityAfter().getTimestamp().toString());
          properties.put("version", contributions.get(i).getEntityAfter().getVersion());
          properties.put("osmType", contributions.get(i).getEntityAfter().getType());
          properties.put("changesetId", contributions.get(i).getEntityAfter().getChangeset());
        }
      }
      // after loop:
      // if last contribution was not "deletion": set valid_to = t_end, add row to output list
      if (!contributions.get(contributions.size() - 1).getContributionTypes()
          .contains(ContributionType.DELETION)) {
        properties.put("validTo", "useLastTimestampFromInputParameterHere");
        output.add(exeUtils.createOSMDataFeature(keys, values, mapTagTranslator.get(), keysInt,
            valuesInt, contributions.get(0), false, properties, gjw, includeTags, elemGeom));
      }
      return output;
    });
    // .stream(output -> ...);

//    mapRedSnapshot
//    .groupByEntity()
//    .filter(snapshots -> {
//      if (snapshots.size() != 2)
//        return false;
//      return snapshots.get(0).getGeometry() == snapshots.get(1).getGeometry() //todo: check if really the case in CellIterator
//          //snapshots.get(0).getGeometry().equals(snapshots.get(1).getGeometry())
//          && snapshots.get(0).getEntity().getVersion() == snapshots.get(1).getEntity().getVersion();
//    })
//    .map(snapshots -> snapshots.get(0))
//    .map(snapshot -> new Feature(…)) // valid_from = t_start, valid_to = t_end
//    .steam(output -> ...);
    

    Stream<Feature> streamResult = preResult.stream().filter(feature -> {
      if (feature == null)
        return false;
      return true;
    });
    Metadata metadata = null;
    if (ProcessingData.showMetadata) {
      metadata = new Metadata(null, "OSM data as GeoJSON features.", requestUrl);
    }
    DataResponse osmData = new DataResponse(new Attribution(url, text), Application.apiVersion,
        metadata, "FeatureCollection", Collections.emptyList());
    JsonFactory jsonFactory = new JsonFactory();
    ByteArrayOutputStream tempStream = new ByteArrayOutputStream();

    ObjectMapper objMapper = new ObjectMapper();
    objMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objMapper.setSerializationInclusion(Include.NON_NULL);
    jsonFactory.createGenerator(tempStream, JsonEncoding.UTF8).setCodec(objMapper)
        .writeObject(osmData);

    String scaffold = tempStream.toString("UTF-8").replaceFirst("]\\r?\\n?\\W*}\\r?\\n?\\W*$", "");

    response.addHeader("Content-disposition", "attachment;filename=ohsome.geojson");
    response.setContentType("application/geo+json; charset=utf-8");
    ServletOutputStream outputStream = response.getOutputStream();
    outputStream.write(scaffold.getBytes("UTF-8"));

    ThreadLocal<ByteArrayOutputStream> outputBuffers =
        ThreadLocal.withInitial(ByteArrayOutputStream::new);
    ThreadLocal<JsonGenerator> outputJsonGen = ThreadLocal.withInitial(() -> {
      try {
        return jsonFactory.createGenerator(outputBuffers.get(), JsonEncoding.UTF8)
            .setCodec(objMapper);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
    outputStream.print("\n");
    AtomicReference<Boolean> isFirst = new AtomicReference<>(true);
    streamResult.map(data -> {
      try {
        outputBuffers.get().reset();
        outputJsonGen.get().writeObject(data);
        return outputBuffers.get().toByteArray();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }).sequential().forEach(data -> {
      try {
        if (isFirst.get()) {
          isFirst.set(false);
        } else {
          outputStream.print(",");
        }
        outputStream.write(data);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
    outputStream.print("\n  ]\n}\n");
    response.flushBuffer();
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
    mapRed = inputProcessor.processParameters(mapRed, requestParams);
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
    GeometryBuilder geomBuilder = inputProcessor.getGeomBuilder();
    ExecutionUtils exeUtils = new ExecutionUtils();
    Geometry geom = exeUtils.getGeometry(ProcessingData.boundary, geomBuilder);
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
    DefaultAggregationResponse response = DefaultAggregationResponse.of(new Attribution(url, text),
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
    mapRed = inputProcessor.processParameters(mapRed, requestParams);
    switch (requestResource) {
      case COUNT:
        result = exeUtils.computeCountLengthPerimeterAreaGbB(RequestResource.COUNT,
            ProcessingData.boundary, mapRed, inputProcessor.getGeomBuilder(),
            requestParams.isSnapshot());
        break;
      case LENGTH:
        result = exeUtils.computeCountLengthPerimeterAreaGbB(RequestResource.LENGTH,
            ProcessingData.boundary, mapRed, inputProcessor.getGeomBuilder(),
            requestParams.isSnapshot());
        break;
      case PERIMETER:
        result = exeUtils.computeCountLengthPerimeterAreaGbB(RequestResource.PERIMETER,
            ProcessingData.boundary, mapRed, inputProcessor.getGeomBuilder(),
            requestParams.isSnapshot());
        break;
      case AREA:
        result = exeUtils.computeCountLengthPerimeterAreaGbB(RequestResource.AREA,
            ProcessingData.boundary, mapRed, inputProcessor.getGeomBuilder(),
            requestParams.isSnapshot());
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
    ArrayList<Geometry> boundaries = inputProcessor.getGeomBuilder().getGeometry();
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
      return GroupByResponse.of(new Attribution(url, text), Application.apiVersion, metadata,
          "FeatureCollection",
          exeUtils.createGeoJsonFeatures(resultSet, ProcessingData.geoJsonGeoms));
    } else {
      return new GroupByResponse(new Attribution(url, text), Application.apiVersion, metadata,
          resultSet);
    }
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
    mapRed = inputProcessor.processParameters(mapRed, requestParams);
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
    GroupByResponse response = new GroupByResponse(new Attribution(url, text),
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
    mapRed = inputProcessor.processParameters(mapRed, requestParams);
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
    GeometryBuilder geomBuilder = inputProcessor.getGeomBuilder();
    Geometry geom = exeUtils.getGeometry(ProcessingData.boundary, geomBuilder);
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
    GroupByResponse response = new GroupByResponse(new Attribution(url, text),
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
    mapRed = inputProcessor.processParameters(mapRed, requestParams);
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
    GeometryBuilder geomBuilder = inputProcessor.getGeomBuilder();
    Geometry geom = exeUtils.getGeometry(ProcessingData.boundary, geomBuilder);
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
    GroupByResponse response = new GroupByResponse(new Attribution(url, text),
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
    mapRed = inputProcessor.processParameters(mapRed, requestParams);
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
    GroupByResponse response = new GroupByResponse(new Attribution(url, text),
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
    inputProcessor.processParameters(mapRed, requestParams);
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
      mapRed = inputProcessor.processParameters(mapRed,
          new RequestParameters(requestParams.getRequestMethod(), requestParams.isSnapshot(),
              requestParams.isDensity(), requestParams.getBboxes(), requestParams.getBcircles(),
              requestParams.getBpolys(), osmTypesString, new String[] {}, new String[] {},
              requestParams.getUserids(), requestParams.getTime(),
              requestParams.getShowMetadata()));
      mapRed = mapRed.osmEntityFilter(entity -> {
        if (!exeUtils.entityMatches(entity, osmTypes1, keysInt1, valuesInt1)) {
          return exeUtils.entityMatches(entity, osmTypes2, keysInt2, valuesInt2);
        }
        return true;
      });
    } else {
      mapRed = inputProcessor.processParameters(mapRed, requestParams);
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
    return exeUtils.createRatioShareResponse(isShare, timeArray, value1, value2, ratioDf,
        inputProcessor, startTime, requestResource, requestUrl, new Attribution(url, text));
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
    inputProcessor.processParameters(mapRed, requestParams);
    if (ProcessingData.boundary == BoundaryType.NOBOUNDARY) {
      throw new BadRequestException(ExceptionMessages.noBoundary);
    }
    GeometryBuilder geomBuilder = inputProcessor.getGeomBuilder();
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
      mapRed = inputProcessor.processParameters(mapRed,
          new RequestParameters(requestParams.getRequestMethod(), requestParams.isSnapshot(),
              requestParams.isDensity(), requestParams.getBboxes(), requestParams.getBcircles(),
              requestParams.getBpolys(), osmTypesString, new String[] {}, new String[] {},
              requestParams.getUserids(), requestParams.getTime(),
              requestParams.getShowMetadata()));
      mapRed = mapRed.osmEntityFilter(entity -> {
        boolean matches1 = exeUtils.entityMatches(entity, osmTypes1, keysInt1, valuesInt1);
        boolean matches2 = exeUtils.entityMatches(entity, osmTypes2, keysInt2, valuesInt2);
        return matches1 || matches2;
      });
    } else {
      mapRed = inputProcessor.processParameters(mapRed, requestParams);
      mapRed = mapRed.osmType(osmTypes);
    }
    ArrayList<Geometry> arrGeoms = geomBuilder.getGeometry();
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
          } else {
            return Geo.lengthOf(geom.getBoundary());
          }
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
        timeArray, resultValues1, resultValues2, ratioDf, inputProcessor, startTime,
        requestResource, requestUrl, new Attribution(url, text), geoJsonGeoms);
  }
}
