package org.heigit.bigspatialdata.ohsome.ohsomeapi.executor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.Application;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.rawdata.ElementsGeometry;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.BoundaryType;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.Description;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Attribution;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Metadata;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.RatioResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.RatioResult;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Result;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.elements.ElementsResult;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.elements.ShareResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.elements.ShareResult;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.GroupByResult;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.RatioGroupByBoundaryResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.RatioGroupByResult;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.ShareGroupByBoundaryResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.ShareGroupByResult;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.users.UsersResult;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.rawdataresponse.DataResponse;
import org.heigit.bigspatialdata.oshdb.api.generic.OSHDBCombinedIndex;
import org.heigit.bigspatialdata.oshdb.api.generic.function.SerializableFunction;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.object.OSMContribution;
import org.heigit.bigspatialdata.oshdb.api.object.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.osm.OSMEntity;
import org.heigit.bigspatialdata.oshdb.osm.OSMType;
import org.heigit.bigspatialdata.oshdb.util.OSHDBBoundingBox;
import org.heigit.bigspatialdata.oshdb.util.OSHDBTag;
import org.heigit.bigspatialdata.oshdb.util.OSHDBTimestamp;
import org.heigit.bigspatialdata.oshdb.util.celliterator.ContributionType;
import org.heigit.bigspatialdata.oshdb.util.geometry.Geo;
import org.heigit.bigspatialdata.oshdb.util.geometry.OSHDBGeometryBuilder;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.OSMTag;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;
import org.heigit.bigspatialdata.oshdb.util.time.TimestampFormatter;
import org.wololo.jts2geojson.GeoJSONWriter;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.opencsv.CSVWriter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygonal;

/** Holds helper methods that are used by the executor classes. */
public class ExecutionUtils {

  AtomicReference<Boolean> isFirst;
  private final ProcessingData processingData;

  public ExecutionUtils(ProcessingData processingData) {
    this.processingData = processingData;
  }

  /** Streams the result of /elements and /elementsFullHistory respones as an outputstream. */
  public void streamElementsResponse(HttpServletResponse servletResponse, DataResponse osmData,
      boolean isFullHistory, Stream<org.wololo.geojson.Feature> snapshotStream,
      Stream<org.wololo.geojson.Feature> contributionStream) throws Exception {

    JsonFactory jsonFactory = new JsonFactory();
    ByteArrayOutputStream tempStream = new ByteArrayOutputStream();

    ObjectMapper objMapper = new ObjectMapper();
    objMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objMapper.setSerializationInclusion(Include.NON_NULL);
    jsonFactory.createGenerator(tempStream, JsonEncoding.UTF8).setCodec(objMapper)
        .writeObject(osmData);

    String scaffold = tempStream.toString("UTF-8").replaceFirst("]\\r?\\n?\\W*}\\r?\\n?\\W*$", "");

    servletResponse.addHeader("Content-disposition", "attachment;filename=ohsome.geojson");
    servletResponse.setContentType("application/geo+json; charset=utf-8");
    ServletOutputStream outputStream = servletResponse.getOutputStream();
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
    isFirst = new AtomicReference<>(true);
    outputStream.print("\n");
    if (isFullHistory) {
      contributionStream =
          writeStreamResponse(outputJsonGen, contributionStream, outputBuffers, outputStream);
    }
    snapshotStream =
        writeStreamResponse(outputJsonGen, snapshotStream, outputBuffers, outputStream);
    outputStream.print("\n  ]\n}\n");
    servletResponse.flushBuffer();
  }

  /** Sends a response in the csv format for /groupBy requests. */
  public void sendCsvResponse(GroupByResult[] resultSet, HttpServletResponse servletResponse,
      List<String[]> comments) {
    CSVWriter writer;
    try {
      writer = new CSVWriter(servletResponse.getWriter(), ';', CSVWriter.NO_QUOTE_CHARACTER,
          CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
      writer.writeAll(comments);
      writer.writeNext(new String[] {"groupByObject", "timestamp", "value"});
      for (GroupByResult groupByResult : resultSet) {
        for (Result result : groupByResult.getResult()) {
          ElementsResult elemResult = (ElementsResult) result;
          writer.writeNext(new String[] {groupByResult.getGroupByObject().toString(),
              elemResult.getTimestamp(), String.valueOf(elemResult.getValue())});
        }
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Sends a response in the csv format for /count|length|perimeter|area(/density) requests. */
  public void sendCsvResponse(ElementsResult[] resultSet, HttpServletResponse servletResponse,
      List<String[]> comments) {
    CSVWriter writer;
    try {
      writer = new CSVWriter(servletResponse.getWriter(), ';', CSVWriter.NO_QUOTE_CHARACTER,
          CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
      writer.writeAll(comments);
      writer.writeNext(new String[] {"timestamp", "value"});
      for (ElementsResult elementsResult : resultSet) {
        writer.writeNext(new String[] {elementsResult.getTimestamp(),
            String.valueOf(elementsResult.getValue())});
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Sends a response in the csv format for /share requests. */
  public void sendCsvResponse(ShareResult[] resultSet, HttpServletResponse servletResponse,
      List<String[]> comments) {
    CSVWriter writer;
    try {
      writer = new CSVWriter(servletResponse.getWriter(), ';', CSVWriter.NO_QUOTE_CHARACTER,
          CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
      writer.writeAll(comments);
      writer.writeNext(new String[] {"timestamp", "whole", "part"});
      for (ShareResult shareResult : resultSet) {
        writer.writeNext(new String[] {shareResult.getTimestamp(),
            String.valueOf(shareResult.getWhole()), String.valueOf(shareResult.getPart())});
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Sends a response in the csv format for /ratio requests. */
  public void sendCsvResponse(RatioResult[] resultSet, HttpServletResponse servletResponse,
      List<String[]> comments) {
    CSVWriter writer;
    try {
      writer = new CSVWriter(servletResponse.getWriter(), ';', CSVWriter.NO_QUOTE_CHARACTER,
          CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
      writer.writeAll(comments);
      writer.writeNext(new String[] {"timestamp", "value", "value2", "ratio"});
      for (RatioResult ratioResult : resultSet) {
        writer.writeNext(
            new String[] {ratioResult.getTimestamp(), String.valueOf(ratioResult.getValue()),
                String.valueOf(ratioResult.getValue2()), String.valueOf(ratioResult.getRatio())});
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Creates the comments of the csv response (Attribution, API-Version and optional Metadata). */
  public List<String[]> createCsvTopComments(String url, String text, String apiVersion,
      Metadata metadata) {
    List<String[]> comments = new LinkedList<String[]>();
    comments.add(new String[] {"# Copyright URL: " + url});
    comments.add(new String[] {"# Copyright Text: " + text});
    comments.add(new String[] {"# API Version: " + apiVersion});
    if (metadata != null) {
      comments.add(new String[] {"# Execution Time: " + metadata.getExecutionTime()});
      comments.add(new String[] {"# Description: " + metadata.getDescription()});
      comments.add(new String[] {"# Request URL: " + metadata.getRequestUrl()});
    }
    return comments;
  }

  /**
   * Defines a certain decimal format.
   * 
   * @param format <code>String</code> defining the format (e.g.: "#.####" for getting 4 digits
   *        after the comma)
   * @return <code>DecimalFormat</code> object with the defined format.
   */
  public DecimalFormat defineDecimalFormat(String format) {
    DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
    otherSymbols.setDecimalSeparator('.');
    DecimalFormat decForm = new DecimalFormat(format, otherSymbols);
    return decForm;
  }

  /** Creates the <code>Feature</code> objects in the OSM data response. */
  public org.wololo.geojson.Feature createOSMFeature(OSMEntity entity, Geometry geometry,
      Map<String, Object> properties, int[] keysInt, boolean includeTags,
      boolean includeOSMMetadata, ElementsGeometry elemGeom, TagTranslator tt, GeoJSONWriter gjw) {
    if (includeTags) {
      for (OSHDBTag oshdbTag : entity.getTags()) {
        OSMTag tag = tt.getOSMTagOf(oshdbTag);
        properties.put(tag.getKey(), tag.getValue());
      }
    } else if (keysInt != null && keysInt.length != 0) {
      int[] tags = entity.getRawTags();
      for (int i = 0; i < tags.length; i += 2) {
        int tagKeyId = tags[i];
        int tagValueId = tags[i + 1];
        for (int key : keysInt) {
          if (tagKeyId == key) {
            OSMTag tag = tt.getOSMTagOf(tagKeyId, tagValueId);
            properties.put(tag.getKey(), tag.getValue());
          }
        }
      }
    }
    properties.put("@osmId", entity.getType().toString().toLowerCase() + "/" + entity.getId());
    if (includeOSMMetadata) {
      properties.put("@version", entity.getVersion());
      properties.put("@osmType", entity.getType());
      properties.put("@changesetId", entity.getChangeset());
    }
    switch (elemGeom) {
      case BBOX:
        Envelope envelope = geometry.getEnvelopeInternal();
        OSHDBBoundingBox bbox = OSHDBGeometryBuilder.boundingBoxOf(envelope);
        return new org.wololo.geojson.Feature(gjw.write(OSHDBGeometryBuilder.getGeometry(bbox)),
            properties);
      case CENTROID:
        return new org.wololo.geojson.Feature(gjw.write(geometry.getCentroid()), properties);
      case RAW:
      default:
        return new org.wololo.geojson.Feature(gjw.write(geometry), properties);
    }
  }

  /** Computes the result for the /count|length|perimeter|area/groupBy/boundary resources. */
  @SuppressWarnings({"unchecked"}) // intentionally as check for P on Polygonal is already performed
  public <P extends Geometry & Polygonal> SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, ? extends Number> computeCountLengthPerimeterAreaGbB(
      RequestResource requestResource, BoundaryType boundaryType,
      MapReducer<OSMEntitySnapshot> mapRed) throws Exception {
    if (boundaryType == BoundaryType.NOBOUNDARY) {
      throw new BadRequestException(ExceptionMessages.noBoundary);
    }
    SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, ? extends Number> result = null;
    MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, Geometry> preResult;
    ArrayList<Geometry> arrGeoms = new ArrayList<>(processingData.boundaryColl);
    Map<Integer, P> geoms = IntStream.range(0, arrGeoms.size()).boxed()
        .collect(Collectors.toMap(idx -> idx, idx -> (P) arrGeoms.get(idx)));
    preResult = mapRed.aggregateByTimestamp().aggregateByGeometry(geoms).map(x -> x.getGeometry());
    switch (requestResource) {
      case COUNT:
        result = preResult.count();
        break;
      case PERIMETER:
        result = preResult.sum(geom -> {
          if (!(geom instanceof Polygonal)) {
            return 0.0;
          }
          return Geo.lengthOf(geom.getBoundary());
        });
        break;
      case LENGTH:
        result = preResult.sum(Geo::lengthOf);
        break;
      case AREA:
        result = preResult.sum(Geo::areaOf);
        break;
      default:
        break;
    }
    return result;
  }

  /**
   * Adapted helper function, which works like {@link OSHBCombinedIndex#nest(Map) nest} but has
   * switched &lt;U&gt; and &lt;V&gt; parameters.
   *
   * @param result the "flat" result data structure that should be converted to a nested structure
   * @param <A> an arbitrary data type, used for the data value items
   * @param <U> an arbitrary data type, used for the index'es key items
   * @param <V> an arbitrary data type, used for the index'es key items
   * @return a nested data structure: for each index part there is a separate level of nested maps
   * 
   */
  public static <A, U, V> SortedMap<V, SortedMap<U, A>> nest(
      Map<OSHDBCombinedIndex<U, V>, A> result) {
    TreeMap<V, SortedMap<U, A>> ret = new TreeMap<>();
    result.forEach((index, data) -> {
      if (!ret.containsKey(index.getSecondIndex())) {
        ret.put(index.getSecondIndex(), new TreeMap<U, A>());
      }
      ret.get(index.getSecondIndex()).put(index.getFirstIndex(), data);
    });
    return ret;
  }

  /**
   * Computes the result depending on the <code>RequestResource</code> using a
   * <code>MapAggregator</code> object as input and returning a <code>SortedMap</code>.
   */
  @SuppressWarnings({"unchecked"}) // intentionally suppressed
  public <K extends OSHDBCombinedIndex<OSHDBTimestamp, ?>, V extends Number> SortedMap<K, V> computeResult(
      RequestResource requestResource, MapAggregator<?, OSMEntitySnapshot> preResult)
      throws Exception {
    switch (requestResource) {
      case COUNT:
        return (SortedMap<K, V>) preResult.count();
      case LENGTH:
        return (SortedMap<K, V>) preResult
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
              return Geo.lengthOf(snapshot.getGeometry());
            });
      case PERIMETER:
        return (SortedMap<K, V>) preResult
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
              if (snapshot.getGeometry() instanceof Polygonal) {
                return Geo.lengthOf(snapshot.getGeometry().getBoundary());
              }
              return 0.0;
            });
      case AREA:
        return (SortedMap<K, V>) preResult
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
              return Geo.areaOf(snapshot.getGeometry());
            });
      default:
        return null;
    }
  }

  /** Compares an OSMType with an EnumSet of OSMTypes. */
  public boolean isOSMType(EnumSet<OSMType> types, OSMType currentElementType) {
    for (OSMType type : types) {
      if (currentElementType.equals(type)) {
        return true;
      }
    }
    return false;
  }

  /** Compares the OSM type and tag(s) of the given entity to the given types|tags. */
  public boolean entityMatches(OSMEntity entity, EnumSet<OSMType> osmTypes, Integer[] keysInt,
      Integer[] valuesInt) {
    boolean matches = true;
    if (osmTypes.contains(entity.getType())) {
      for (int i = 0; i < keysInt.length; i++) {
        boolean matchesTag;
        if (i < valuesInt.length) {
          matchesTag = entity.hasTagValue(keysInt[i], valuesInt[i]);
        } else {
          matchesTag = entity.hasTagKey(keysInt[i]);
        }
        if (!matchesTag) {
          matches = false;
          break;
        }
      }
    } else {
      matches = false;
    }
    return matches;
  }

  /**
   * Extracts the tags from the given <code>OSMContribution</code> depending on the
   * <code>ContributionType</code>.
   */
  public int[] extractContributionTags(OSMContribution contrib) {
    int[] tags;
    if (contrib.getContributionTypes().contains(ContributionType.DELETION)) {
      tags = contrib.getEntityBefore().getRawTags();
    } else if (contrib.getContributionTypes().contains(ContributionType.CREATION)) {
      tags = contrib.getEntityAfter().getRawTags();
    } else {
      int[] tagsBefore = contrib.getEntityBefore().getRawTags();
      int[] tagsAfter = contrib.getEntityAfter().getRawTags();
      tags = new int[tagsBefore.length + tagsAfter.length];
      System.arraycopy(tagsBefore, 0, tags, 0, tagsBefore.length);
      System.arraycopy(tagsAfter, 0, tags, tagsBefore.length, tagsAfter.length);
    }
    return tags;
  }

  /**
   * Creates the GeoJson features used in the GeoJson response for a
   * count|length|perimeter|area/groupBy/boundary request.
   */
  public Feature[] createGeoJsonFeatures(GroupByResult[] groupByResults,
      GeoJsonObject[] geoJsonGeoms) {
    int groupByResultsLength = groupByResults.length;
    int resultLength = groupByResults[0].getResult().length;
    int featuresLength = groupByResultsLength * resultLength;
    Feature[] features = new Feature[featuresLength];
    int groupByResultCount = 0;
    int tstampCount = 0;
    for (int i = 0; i < featuresLength; i++) {
      ElementsResult result =
          (ElementsResult) groupByResults[groupByResultCount].getResult()[tstampCount];
      Object groupByBoundaryId = groupByResults[groupByResultCount].getGroupByObject();
      String tstamp = result.getTimestamp();
      Feature feature = new Feature();
      feature.setId(groupByBoundaryId + "@" + tstamp);
      feature.setProperty("groupByBoundaryId", groupByBoundaryId);
      feature.setProperty("timestamp", tstamp);
      feature.setProperty("value", result.getValue());
      GeoJsonObject geom = geoJsonGeoms[groupByResultCount];
      feature.setGeometry(geom);
      tstampCount++;
      if (tstampCount == resultLength) {
        tstampCount = 0;
        groupByResultCount++;
      }
      features[i] = feature;
    }
    return features;
  }

  /**
   * Creates the GeoJson features used in the GeoJson response for a
   * count|length|perimeter|area/ratio/groupBy/boundary request.
   */
  public Feature[] createGeoJsonFeatures(RatioGroupByResult[] groupByResults,
      GeoJsonObject[] geoJsonGeoms) {
    int groupByResultsLength = groupByResults.length;
    int resultLength = groupByResults[0].getRatioResult().length;
    int featuresLength = groupByResultsLength * resultLength;
    Feature[] features = new Feature[featuresLength];
    int groupByResultCount = 0;
    int tstampCount = 0;
    for (int i = 0; i < featuresLength; i++) {
      RatioResult result =
          (RatioResult) groupByResults[groupByResultCount].getRatioResult()[tstampCount];
      Object groupByBoundaryId = groupByResults[groupByResultCount].getGroupByObject();
      String tstamp = result.getTimestamp();
      Feature feature = new Feature();
      feature.setId(groupByBoundaryId + "@" + tstamp);
      feature.setProperty("groupByBoundaryId", groupByBoundaryId);
      feature.setProperty("timestamp", tstamp);
      feature.setProperty("value", result.getValue());
      feature.setProperty("value2", result.getValue2());
      feature.setProperty("ratio", result.getRatio());
      GeoJsonObject geom = geoJsonGeoms[groupByResultCount];
      feature.setGeometry(geom);
      tstampCount++;
      if (tstampCount == resultLength) {
        tstampCount = 0;
        groupByResultCount++;
      }
      features[i] = feature;
    }
    return features;
  }

  /**
   * Creates the GeoJson features used in the GeoJson response for a
   * count|length|perimeter|area/share/groupBy/boundary request.
   */
  public Feature[] createGeoJsonFeatures(ShareGroupByResult[] groupByResults,
      GeoJsonObject[] geoJsonGeoms) {
    int groupByResultsLength = groupByResults.length;
    int resultLength = groupByResults[0].getShareResult().length;
    int featuresLength = groupByResultsLength * resultLength;
    Feature[] features = new Feature[featuresLength];
    int groupByResultCount = 0;
    int tstampCount = 0;
    for (int i = 0; i < featuresLength; i++) {
      ShareResult result =
          (ShareResult) groupByResults[groupByResultCount].getShareResult()[tstampCount];
      Object groupByBoundaryId = groupByResults[groupByResultCount].getGroupByObject();
      String tstamp = result.getTimestamp();
      Feature feature = new Feature();
      feature.setId(groupByBoundaryId + "@" + tstamp);
      feature.setProperty("groupByBoundaryId", groupByBoundaryId);
      feature.setProperty("timestamp", tstamp);
      feature.setProperty("whole", result.getWhole());
      feature.setProperty("part", result.getPart());
      GeoJsonObject geom = geoJsonGeoms[groupByResultCount];
      feature.setGeometry(geom);
      tstampCount++;
      if (tstampCount == resultLength) {
        tstampCount = 0;
        groupByResultCount++;
      }
      features[i] = feature;
    }
    return features;
  }

  /** Fills the ElementsResult array with respective ElementsResult objects. */
  public ElementsResult[] fillElementsResult(SortedMap<OSHDBTimestamp, ? extends Number> entryVal,
      boolean isDensity, DecimalFormat df, Geometry geom) {
    ElementsResult[] results = new ElementsResult[entryVal.entrySet().size()];
    int count = 0;
    for (Entry<OSHDBTimestamp, ? extends Number> entry : entryVal.entrySet()) {
      if (isDensity) {
        results[count] = new ElementsResult(
            TimestampFormatter.getInstance().isoDateTime(entry.getKey()), Double.parseDouble(
                df.format((entry.getValue().doubleValue() / (Geo.areaOf(geom) * 0.000001)))));
      } else {
        results[count] =
            new ElementsResult(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
                Double.parseDouble(df.format((entry.getValue().doubleValue()))));
      }
      count++;
    }
    return results;
  }

  /** Fills the UsersResult array with respective UsersResult objects. */
  public UsersResult[] fillUsersResult(SortedMap<OSHDBTimestamp, ? extends Number> entryVal,
      boolean isDensity, String[] toTimestamps, DecimalFormat df, Geometry geom) {
    UsersResult[] results = new UsersResult[entryVal.entrySet().size()];
    int count = 0;
    for (Entry<OSHDBTimestamp, ? extends Number> entry : entryVal.entrySet()) {
      if (isDensity) {
        results[count] =
            new UsersResult(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
                toTimestamps[count + 1], Double.parseDouble(
                    df.format((entry.getValue().doubleValue() / (Geo.areaOf(geom) / 1000000)))));
      } else {
        results[count] = new UsersResult(
            TimestampFormatter.getInstance().isoDateTime(entry.getKey()), toTimestamps[count + 1],
            Double.parseDouble(df.format(entry.getValue().doubleValue())));
      }
      count++;
    }
    return results;
  }

  /** Fills the given stream with output data. */
  private Stream<org.wololo.geojson.Feature> writeStreamResponse(
      ThreadLocal<JsonGenerator> outputJsonGen, Stream<org.wololo.geojson.Feature> stream,
      ThreadLocal<ByteArrayOutputStream> outputBuffers, ServletOutputStream outputStream) {
    stream.map(data -> {
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
    return stream;
  }

  /** Creates either a RatioResponse or a ShareResponse depending on the request. */
  public Response createRatioShareResponse(boolean isShare, String[] timeArray, Double[] value1,
      Double[] value2, DecimalFormat df, long startTime, RequestResource reqRes, String requestUrl,
      RequestParameters requestParameters, Attribution attribution,
      HttpServletResponse servletResponse) {
    Response response;
    if (!isShare) {
      RatioResult[] resultSet = new RatioResult[timeArray.length];
      for (int i = 0; i < timeArray.length; i++) {
        double ratio = value2[i] / value1[i];
        // in case ratio has the values "NaN", "Infinity", etc.
        try {
          ratio = Double.parseDouble(df.format(ratio));
        } catch (Exception e) {
          // do nothing --> just return ratio without rounding (trimming)
        }
        resultSet[i] = new RatioResult(timeArray[i], value1[i], value2[i], ratio);
      }
      Metadata metadata = null;
      if (processingData.showMetadata) {
        long duration = System.currentTimeMillis() - startTime;
        metadata = new Metadata(duration,
            Description.countLengthPerimeterAreaRatio(reqRes.getLabel(), reqRes.getUnit()),
            requestUrl);
      }
      response = new RatioResponse(attribution, Application.apiVersion, metadata, resultSet);
    } else {
      ShareResult[] resultSet = new ShareResult[timeArray.length];
      for (int i = 0; i < timeArray.length; i++) {
        resultSet[i] = new ShareResult(timeArray[i], value1[i], value2[i]);
      }
      Metadata metadata = null;
      if (processingData.showMetadata) {
        long duration = System.currentTimeMillis() - startTime;
        metadata = new Metadata(duration,
            Description.countLengthPerimeterAreaShare(reqRes.getLabel(), reqRes.getUnit()),
            requestUrl);
      }
      if (requestParameters.getFormat() != null
          && requestParameters.getFormat().equalsIgnoreCase("csv")) {
        sendCsvResponse(resultSet, servletResponse,
            createCsvTopComments(ElementsRequestExecutor.URL, ElementsRequestExecutor.TEXT,
                Application.apiVersion, metadata));
        return null;
      }
      response = new ShareResponse(attribution, Application.apiVersion, metadata, resultSet);
    }
    return response;
  }

  /**
   * Creates either a RatioGroupByBoundaryResponse or a ShareGroupByBoundaryResponse depending on
   * the <code>isShare</code> parameter.
   */
  public Response createRatioShareGroupByBoundaryResponse(boolean isShare,
      RequestParameters requestParameters, Object[] boundaryIds, String[] timeArray,
      Double[] resultValues1, Double[] resultValues2, DecimalFormat ratioDf, long startTime,
      RequestResource reqRes, String requestUrl, Attribution attribution,
      GeoJsonObject[] geoJsonGeoms, HttpServletResponse servletResponse) {
    Metadata metadata = null;
    int boundaryIdsLength = boundaryIds.length;
    int timeArrayLenth = timeArray.length;
    RatioResult[] ratioResultSet = new RatioResult[0];
    ShareResult[] shareResultSet = new ShareResult[0];
    if (!isShare) {
      RatioGroupByResult[] groupByResultSet = new RatioGroupByResult[boundaryIdsLength];
      for (int i = 0; i < boundaryIdsLength; i++) {
        Object groupByName = boundaryIds[i];
        ratioResultSet = new RatioResult[timeArrayLenth];
        int innerCount = 0;
        for (int j = i; j < timeArrayLenth * boundaryIdsLength; j += boundaryIdsLength) {
          double ratio = resultValues2[j] / resultValues1[j];
          // in case ratio has the values "NaN", "Infinity", etc.
          try {
            ratio = Double.parseDouble(ratioDf.format(ratio));
          } catch (Exception e) {
            // do nothing --> just return ratio without rounding (trimming)
          }
          ratioResultSet[innerCount] =
              new RatioResult(timeArray[innerCount], resultValues1[j], resultValues2[j], ratio);
          innerCount++;
        }
        groupByResultSet[i] = new RatioGroupByResult(groupByName, ratioResultSet);
      }
      if (processingData.showMetadata) {
        long duration = System.currentTimeMillis() - startTime;
        metadata = new Metadata(duration, Description.countLengthPerimeterAreaRatioGroupByBoundary(
            reqRes.getLabel(), reqRes.getUnit()), requestUrl);
      }
      if (requestParameters.getFormat() != null) {
        if (requestParameters.getFormat().equalsIgnoreCase("geojson")) {
          return RatioGroupByBoundaryResponse.of(attribution, Application.apiVersion, metadata,
              "FeatureCollection", createGeoJsonFeatures(groupByResultSet, geoJsonGeoms));
        } else if (requestParameters.getFormat() != null
            && requestParameters.getFormat().equalsIgnoreCase("csv")) {
          sendCsvResponse(ratioResultSet, servletResponse,
              createCsvTopComments(ElementsRequestExecutor.URL, ElementsRequestExecutor.TEXT,
                  Application.apiVersion, metadata));
          return null;
        }
      }
      return new RatioGroupByBoundaryResponse(attribution, Application.apiVersion, metadata,
          groupByResultSet);
    }
    ShareGroupByResult[] groupByResultSet = new ShareGroupByResult[boundaryIdsLength];
    for (int i = 0; i < boundaryIdsLength; i++) {
      Object groupByName = boundaryIds[i];
      shareResultSet = new ShareResult[timeArrayLenth];
      int innerCount = 0;
      for (int j = i; j < timeArrayLenth * boundaryIdsLength; j += boundaryIdsLength) {
        shareResultSet[innerCount] =
            new ShareResult(timeArray[innerCount], resultValues1[j], resultValues2[j]);
        innerCount++;
      }
      groupByResultSet[i] = new ShareGroupByResult(groupByName, shareResultSet);
    }
    if (processingData.showMetadata) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, Description.countLengthPerimeterAreaShareGroupByBoundary(
          reqRes.getLabel(), reqRes.getUnit()), requestUrl);
    }
    if (requestParameters.getFormat() != null) {
      if (requestParameters.getFormat().equalsIgnoreCase("geojson")) {
        return ShareGroupByBoundaryResponse.of(attribution, Application.apiVersion, metadata,
            "FeatureCollection", createGeoJsonFeatures(groupByResultSet, geoJsonGeoms));
      } else if (requestParameters.getFormat().equalsIgnoreCase("csv")) {
        sendCsvResponse(shareResultSet, servletResponse,
            createCsvTopComments(ElementsRequestExecutor.URL, ElementsRequestExecutor.TEXT,
                Application.apiVersion, metadata));
        return null;
      }
    }

    return new ShareGroupByBoundaryResponse(attribution, Application.apiVersion, metadata,
        groupByResultSet);
  }

  /** Enum type used in /ratio computation. */
  public enum MatchType {
    MATCHES1, MATCHES2, MATCHESBOTH, MATCHESNONE
  }
}
