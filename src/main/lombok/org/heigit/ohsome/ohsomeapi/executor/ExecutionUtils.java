package org.heigit.ohsome.ohsomeapi.executor;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import com.opencsv.CSVWriter;
import com.zaxxer.hikari.HikariDataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.geojson.GeoJsonObject;
import org.heigit.bigspatialdata.oshdb.api.generic.OSHDBCombinedIndex;
import org.heigit.bigspatialdata.oshdb.api.generic.function.SerializableFunction;
import org.heigit.bigspatialdata.oshdb.api.generic.function.SerializableSupplier;
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
import org.heigit.bigspatialdata.oshdb.util.exceptions.OSHDBKeytablesNotFoundException;
import org.heigit.bigspatialdata.oshdb.util.geometry.Geo;
import org.heigit.bigspatialdata.oshdb.util.geometry.OSHDBGeometryBuilder;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.OSMTag;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;
import org.heigit.bigspatialdata.oshdb.util.time.TimestampFormatter;
import org.heigit.ohsome.ohsomeapi.Application;
import org.heigit.ohsome.ohsomeapi.controller.rawdata.ElementsGeometry;
import org.heigit.ohsome.ohsomeapi.exception.DatabaseAccessException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.ohsome.ohsomeapi.inputprocessing.SimpleFeatureType;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.ohsome.ohsomeapi.output.Description;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Attribution;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Metadata;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.RatioResponse;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.RatioResult;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Result;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.elements.ElementsResult;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.GroupByObject;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.GroupByResult;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.RatioGroupByBoundaryResponse;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.RatioGroupByResult;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.users.UsersResult;
import org.heigit.ohsome.ohsomeapi.output.rawdataresponse.DataResponse;
import org.heigit.ohsome.ohsomeapi.utils.GroupByBoundaryGeoJsonGenerator;
import org.heigit.ohsome.ohsomeapi.utils.RequestUtils;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Lineal;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.geom.Puntal;
import org.wololo.jts2geojson.GeoJSONWriter;

/** Holds helper methods that are used by the executor classes. */
@RequiredArgsConstructor
public class ExecutionUtils {
  private AtomicReference<Boolean> isFirst;
  private final ProcessingData processingData;
  private final DecimalFormat ratioDf = defineDecimalFormat("#.######");

  /** Applies a filter on the given MapReducer object using the given parameters. */
  public MapReducer<OSMEntitySnapshot> snapshotFilter(MapReducer<OSMEntitySnapshot> mapRed,
      Set<OSMType> osmTypes1, Set<OSMType> osmTypes2, Set<SimpleFeatureType> simpleFeatureTypes1,
      Set<SimpleFeatureType> simpleFeatureTypes2, Integer[] keysInt1, Integer[] keysInt2,
      Integer[] valuesInt1, Integer[] valuesInt2) {
    return mapRed.filter(
        snapshot -> snapshotMatches(snapshot, osmTypes1, simpleFeatureTypes1, keysInt1, valuesInt1)
            || snapshotMatches(snapshot, osmTypes2, simpleFeatureTypes2, keysInt2, valuesInt2));
  }

  /**
   * Applies a filter on the given MapReducer object using the given parameters. Used in
   * /ratio/groupBy/boundary requests.
   */
  public MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, OSMEntitySnapshot>
      snapshotFilter(
      MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, OSMEntitySnapshot> mapRed,
      Set<OSMType> osmTypes1, Set<OSMType> osmTypes2, Set<SimpleFeatureType> simpleFeatureTypes1,
      Set<SimpleFeatureType> simpleFeatureTypes2, Integer[] keysInt1, Integer[] keysInt2,
      Integer[] valuesInt1, Integer[] valuesInt2) {
    return mapRed.filter(
        snapshot -> snapshotMatches(snapshot, osmTypes1, simpleFeatureTypes1, keysInt1, valuesInt1)
            || snapshotMatches(snapshot, osmTypes2, simpleFeatureTypes2, keysInt2, valuesInt2));
  }

  /** Compares the type(s) and tag(s) of the given snapshot to the given types|tags. */
  public boolean snapshotMatches(OSMEntitySnapshot snapshot, Set<OSMType> osmTypes,
      Set<SimpleFeatureType> simpleFeatureTypes, Integer[] keysInt, Integer[] valuesInt) {
    boolean matchesTags = true;
    OSMEntity entity = snapshot.getEntity();
    if (osmTypes.contains(entity.getType())) {
      for (int i = 0; i < keysInt.length; i++) {
        boolean matchesTag;
        if (i < valuesInt.length) {
          matchesTag = entity.hasTagValue(keysInt[i], valuesInt[i]);
        } else {
          matchesTag = entity.hasTagKey(keysInt[i]);
        }
        if (!matchesTag) {
          matchesTags = false;
          break;
        }
      }
    } else {
      matchesTags = false;
    }
    if (!simpleFeatureTypes.isEmpty()) {
      boolean[] simpleFeatures = setRequestedSimpleFeatures(simpleFeatureTypes);
      return matchesTags && (simpleFeatures[0] && snapshot.getGeometry() instanceof Puntal
          || simpleFeatures[1] && snapshot.getGeometry() instanceof Lineal
          || simpleFeatures[2] && snapshot.getGeometry() instanceof Polygonal || simpleFeatures[3]
              && "GeometryCollection".equalsIgnoreCase(snapshot.getGeometry().getGeometryType()));
    }
    return matchesTags;
  }

  /**
   * Defines a certain decimal format.
   * 
   * @param format <code>String</code> defining the format (e.g.: "#.####" for getting 4 digits
   *        after the comma)
   * @return <code>DecimalFormat</code> object with the defined format.
   */
  public static DecimalFormat defineDecimalFormat(String format) {
    DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
    otherSymbols.setDecimalSeparator('.');
    return new DecimalFormat(format, otherSymbols);
  }

  /**
   * Caches the given mapper value in the user data of the <code>Geometry</code> object.
   * 
   * @param geom <code>Geometry</code> of an OSMEntitySnapshot object
   * @param mapper arbitrary function that returns a time-independent value from a snapshot object,
   *        for example lenght, area, perimeter
   * @return evaluated mapper function or cached value stored in the user data of the
   *         <code>Geometry</code> object
   */
  public static Double cacheInUserData(Geometry geom, SerializableSupplier<Double> mapper) {
    if (geom.getUserData() == null) {
      geom.setUserData(mapper.get());
    }
    return (Double) geom.getUserData();
  }

  /**
   * Adapted helper function, which works like
   * {@link org.heigit.bigspatialdata.oshdb.api.generic.OSHDBCombinedIndex#nest(Map) nest} but has
   * switched &lt;U&gt; and &lt;V&gt; parameters.
   *
   * @param result the "flat" result data structure that should be converted to a nested structure
   * @param <A> an arbitrary data type, used for the data value items
   * @param <U> an arbitrary data type, used for the index'es key items
   * @param <V> an arbitrary data type, used for the index'es key items
   * @return a nested data structure: for each index part there is a separate level of nested maps
   */
  public static <A, U extends Comparable<U> & Serializable, V extends Comparable<V> & Serializable>
      SortedMap<V, SortedMap<U, A>> nest(Map<OSHDBCombinedIndex<U, V>, A> result) {
    TreeMap<V, SortedMap<U, A>> ret = new TreeMap<>();
    result.forEach((index, data) -> {
      if (!ret.containsKey(index.getSecondIndex())) {
        ret.put(index.getSecondIndex(), new TreeMap<>());
      }
      ret.get(index.getSecondIndex()).put(index.getFirstIndex(), data);
    });
    return ret;
  }

  /**
   * Streams the result of /elements, /elementsFullHistory and /contributions endpoints as an
   * outputstream.
   * 
   * @throws RuntimeException which only wraps {@link java.io.IOException IOException}
   * @throws IOException thrown by {@link JsonGenerator
   *         com.fasterxml.jackson.core.JsonFactory#createGenerator(java.io.OutputStream,
   *         JsonEncoding) createGenerator},
   *         {@link com.fasterxml.jackson.core.JsonGenerator#writeObject(Object) writeObject},
   *         {@link javax.servlet.ServletResponse#getOutputStream() getOutputStream},
   *         {@link java.io.OutputStream#write(byte[]) write},
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils
   *         #writeStreamResponse(ThreadLocal, Stream, ThreadLocal, ServletOutputStream)
   *         writeStreamResponse}, {@link javax.servlet.ServletOutputStream#print(String) print},
   *         and {@link javax.servlet.ServletResponse#flushBuffer() flushBuffer}
   * @throws ExecutionException thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils#writeStreamResponse(
   *         ThreadLocal, Stream, ThreadLocal, ServletOutputStream) writeStreamResponse}
   * @throws InterruptedException thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils#writeStreamResponse(
   *         ThreadLocal, Stream, ThreadLocal, ServletOutputStream) writeStreamResponse}
   */
  public void streamResponse(HttpServletResponse servletResponse, DataResponse osmData,
      Stream<org.wololo.geojson.Feature> resultStream) throws Exception {
    JsonFactory jsonFactory = new JsonFactory();
    ByteArrayOutputStream tempStream = new ByteArrayOutputStream();

    ObjectMapper objMapper = new ObjectMapper();
    objMapper.enable(SerializationFeature.INDENT_OUTPUT);
    jsonFactory.createGenerator(tempStream, JsonEncoding.UTF8).setCodec(objMapper)
        .writeObject(osmData);

    String scaffold =
        tempStream.toString(StandardCharsets.UTF_8).replaceFirst("\\s*]\\s*}\\s*$", "");

    servletResponse.setContentType("application/geo+json; charset=utf-8");
    ServletOutputStream outputStream = servletResponse.getOutputStream();
    outputStream.write(scaffold.getBytes(StandardCharsets.UTF_8));

    ThreadLocal<ByteArrayOutputStream> outputBuffers =
        ThreadLocal.withInitial(ByteArrayOutputStream::new);
    ThreadLocal<JsonGenerator> outputJsonGen = ThreadLocal.withInitial(() -> {
      try {
        DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter() {
          @Override
          public void writeIndentation(JsonGenerator g, int level) throws IOException {
            super.writeIndentation(g, level + 1);
          }
        };
        DefaultPrettyPrinter printer =
            new DefaultPrettyPrinter("").withArrayIndenter(indenter).withObjectIndenter(indenter);
        return jsonFactory.createGenerator(outputBuffers.get(), JsonEncoding.UTF8)
            .setCodec(objMapper).setPrettyPrinter(printer);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
    isFirst = new AtomicReference<>(true);
    writeStreamResponse(outputJsonGen, resultStream, outputBuffers, outputStream);
    outputStream.print("]\n}\n");
    servletResponse.flushBuffer();
  }

  /** Writes a response in the csv format for /groupBy requests. */
  public void writeCsvResponse(GroupByObject[] resultSet, HttpServletResponse servletResponse,
      List<String[]> comments) {
    try {
      servletResponse = setCsvSettingsInServletResponse(servletResponse);
      CSVWriter writer = writeComments(servletResponse, comments);
      Pair<List<String>, List<String[]>> rows;
      if (resultSet instanceof GroupByResult[]) {
        if (resultSet.length == 0) {
          writer.writeNext(new String[] {"timestamp"}, false);
          writer.close();
          return;
        } else {
          GroupByResult result = (GroupByResult) resultSet[0];
          if (result.getResult() instanceof UsersResult[]) {
            rows = createCsvResponseForUsersGroupBy(resultSet);
          } else {
            rows = createCsvResponseForElementsGroupBy(resultSet);
          }
        }
      } else {
        rows = createCsvResponseForElementsRatioGroupBy(resultSet);
      }
      writer.writeNext(rows.getLeft().toArray(new String[rows.getLeft().size()]), false);
      writer.writeAll(rows.getRight(), false);
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Writes a response in the csv format for /count|length|perimeter|area(/density)(/ratio)
   * requests.
   */
  public void writeCsvResponse(Result[] resultSet, HttpServletResponse servletResponse,
      List<String[]> comments) {
    try {
      servletResponse = setCsvSettingsInServletResponse(servletResponse);
      CSVWriter writer = writeComments(servletResponse, comments);
      if (resultSet instanceof ElementsResult[]) {
        writer.writeNext(new String[] {"timestamp", "value"}, false);
        for (Result result : resultSet) {
          ElementsResult elementsResult = (ElementsResult) result;
          writer.writeNext(new String[] {elementsResult.getTimestamp(),
              String.valueOf(elementsResult.getValue())});
        }
      } else if (resultSet instanceof UsersResult[]) {
        writer.writeNext(new String[] {"fromTimestamp", "toTimestamp", "value"}, false);
        for (Result result : resultSet) {
          UsersResult usersResult = (UsersResult) result;
          writer.writeNext(new String[] {usersResult.getFromTimestamp(),
              usersResult.getToTimestamp(), String.valueOf(usersResult.getValue())});
        }
      } else if (resultSet instanceof RatioResult[]) {
        writer.writeNext(new String[] {"timestamp", "value", "value2", "ratio"}, false);
        for (Result result : resultSet) {
          RatioResult ratioResult = (RatioResult) result;
          writer.writeNext(
              new String[] {ratioResult.getTimestamp(), String.valueOf(ratioResult.getValue()),
                  String.valueOf(ratioResult.getValue2()), String.valueOf(ratioResult.getRatio())});
        }
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Sets the boolean values for the respective simple feature type: 0 = hasPoint, 1 = hasLine, 2 =
   * hasPolygon, 3 = hasOther.
   */
  public boolean[] setRequestedSimpleFeatures(Set<SimpleFeatureType> simpleFeatureTypes) {
    boolean[] simpleFeatureArray = new boolean[] {false, false, false, false};
    for (SimpleFeatureType type : simpleFeatureTypes) {
      if (type.equals(SimpleFeatureType.POINT)) {
        simpleFeatureArray[0] = true;
      } else if (type.equals(SimpleFeatureType.LINE)) {
        simpleFeatureArray[1] = true;
      } else if (type.equals(SimpleFeatureType.POLYGON)) {
        simpleFeatureArray[2] = true;
      } else if (type.equals(SimpleFeatureType.OTHER)) {
        simpleFeatureArray[3] = true;
      }
    }
    return simpleFeatureArray;
  }

  /** Creates the comments of the csv response (Attribution, API-Version and optional Metadata). */
  public List<String[]> createCsvTopComments(String url, String text, String apiVersion,
      Metadata metadata) {
    List<String[]> comments = new LinkedList<>();
    comments.add(new String[] {"# Copyright URL: " + url});
    comments.add(new String[] {"# Copyright Text: " + text});
    comments.add(new String[] {"# API Version: " + apiVersion});
    if (metadata != null) {
      comments.add(new String[] {"# Execution Time: " + metadata.getExecutionTime()});
      comments.add(new String[] {"# Description: " + metadata.getDescription()});
      if (metadata.getRequestUrl() != null) {
        comments.add(new String[] {"# Request URL: " + metadata.getRequestUrl()});
      }
    }
    return comments;
  }

  /** Creates the <code>Feature</code> objects in the OSM data response. */
  public org.wololo.geojson.Feature createOSMFeature(OSMEntity entity, Geometry geometry,
      Map<String, Object> properties, Set<Integer> keysInt, boolean includeTags,
      boolean includeOSMMetadata, boolean isContributionsEndpoint, ElementsGeometry elemGeom,
      EnumSet<ContributionType> contributionTypes) {
    if (geometry.isEmpty() && !contributionTypes.contains(ContributionType.DELETION)) {
      // skip invalid geometries (e.g. ways with 0 nodes)
      return null;
    }
    if (includeTags) {
      properties.put("@tags", Iterables.toArray(entity.getTags(), OSHDBTag.class));
    } else if (!keysInt.isEmpty()) {
      properties.put("@tags", Streams.stream(entity.getTags())
          .filter(t -> keysInt.contains(t.getKey())).toArray(OSHDBTag[]::new));
    }
    if (includeOSMMetadata) {
      properties =
          addAdditionalProperties(entity, properties, isContributionsEndpoint, contributionTypes);
    }
    properties.put("@osmId", entity.getType().toString().toLowerCase() + "/" + entity.getId());
    GeoJSONWriter gjw = new GeoJSONWriter();
    boolean deletionHandling =
        isContributionsEndpoint && contributionTypes.contains(ContributionType.DELETION);
    Geometry outputGeometry;
    switch (elemGeom) {
      case BBOX:
        if (deletionHandling) {
          return new org.wololo.geojson.Feature(null, properties);
        }
        Envelope envelope = geometry.getEnvelopeInternal();
        OSHDBBoundingBox bbox = OSHDBGeometryBuilder.boundingBoxOf(envelope);
        outputGeometry = OSHDBGeometryBuilder.getGeometry(bbox);
        break;
      case CENTROID:
        if (deletionHandling) {
          return new org.wololo.geojson.Feature(null, properties);
        }
        outputGeometry = geometry.getCentroid();
        break;
      case RAW:
      default:
        if (deletionHandling) {
          return new org.wololo.geojson.Feature(null, properties);
        }
        outputGeometry = geometry;
    }
    return new org.wololo.geojson.Feature(gjw.write(outputGeometry), properties);
  }

  /**
   * Computes the result depending on the <code>RequestResource</code> using a
   * <code>MapAggregator</code> object as input and returning a <code>SortedMap</code>.
   * 
   * @throws Exception thrown by
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#count() count}, and
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#sum() sum}
   */
  @SuppressWarnings({"unchecked"}) // intentionally suppressed as type format is valid
  public <K extends Comparable<K> & Serializable, V extends Number>
      SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, K>, V> computeResult(
      RequestResource requestResource,
      MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, K>, OSMEntitySnapshot> preResult)
      throws Exception {
    switch (requestResource) {
      case COUNT:
        return (SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, K>, V>) preResult.count();
      case LENGTH:
        return (SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, K>, V>) preResult
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> cacheInUserData(
                snapshot.getGeometry(), () -> Geo.lengthOf(snapshot.getGeometry())));
      case PERIMETER:
        return (SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, K>, V>) preResult
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
              if (snapshot.getGeometry() instanceof Polygonal) {
                return cacheInUserData(snapshot.getGeometry(),
                    () -> Geo.lengthOf(snapshot.getGeometry().getBoundary()));
              }
              return 0.0;
            });
      case AREA:
        return (SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, K>, V>) preResult
            .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> cacheInUserData(
                snapshot.getGeometry(), () -> Geo.areaOf(snapshot.getGeometry())));
      default:
        return null;
    }
  }

  /**
   * Computes the result depending on the <code>RequestResource</code> using a
   * <code>MapAggregator</code> object as input and returning a <code>SortedMap</code>.
   */
  @SuppressWarnings({"unchecked"}) // intentionally suppressed as type format is valid
  public <K extends Comparable<K> & Serializable, V extends Number>
      SortedMap<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, K>, OSHDBTimestamp>, V>
      computeNestedResult(
      RequestResource requestResource,
      MapAggregator<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, K>, OSHDBTimestamp>, Geometry>
          preResult) throws Exception {
    switch (requestResource) {
      case COUNT:
        return (SortedMap<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, K>, OSHDBTimestamp>, V>)
            preResult.count();
      case PERIMETER:
        return (SortedMap<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, K>, OSHDBTimestamp>, V>)
            preResult.sum(geom -> {
              if (!(geom instanceof Polygonal)) {
                return 0.0;
              }
              return cacheInUserData(geom, () -> Geo.lengthOf(geom.getBoundary()));
            });
      case LENGTH:
        return (SortedMap<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, K>, OSHDBTimestamp>, V>)
            preResult.sum(geom -> cacheInUserData(geom, () -> Geo.lengthOf(geom)));
      case AREA:
        return (SortedMap<OSHDBCombinedIndex<OSHDBCombinedIndex<Integer, K>, OSHDBTimestamp>, V>)
            preResult.sum(geom -> cacheInUserData(geom, () -> Geo.areaOf(geom)));
      default:
        return null;
    }
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

  /** Fills the ElementsResult array with respective ElementsResult objects. */
  public ElementsResult[] fillElementsResult(SortedMap<OSHDBTimestamp, ? extends Number> entryVal,
      boolean isDensity, DecimalFormat df, Geometry geom) {
    ElementsResult[] results = new ElementsResult[entryVal.entrySet().size()];
    int count = 0;
    for (Entry<OSHDBTimestamp, ? extends Number> entry : entryVal.entrySet()) {
      if (isDensity) {
        results[count] = new ElementsResult(
            TimestampFormatter.getInstance().isoDateTime(entry.getKey()), Double.parseDouble(
                df.format(entry.getValue().doubleValue() / (Geo.areaOf(geom) * 0.000001))));
      } else {
        results[count] =
            new ElementsResult(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
                Double.parseDouble(df.format(entry.getValue().doubleValue())));
      }
      count++;
    }
    return results;
  }

  /** Fills the UsersResult array with respective UsersResult objects. */
  public UsersResult[] fillUsersResult(SortedMap<OSHDBTimestamp, ? extends Number> entryVal,
      boolean isDensity, InputProcessor inputProcessor, DecimalFormat df, Geometry geom) {
    UsersResult[] results = new UsersResult[entryVal.entrySet().size()];
    int count = 0;
    String[] toTimestamps = inputProcessor.getUtils().getToTimestamps();
    for (Entry<OSHDBTimestamp, ? extends Number> entry : entryVal.entrySet()) {
      if (isDensity) {
        results[count] =
            new UsersResult(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
                toTimestamps[count + 1], Double.parseDouble(
                    df.format(entry.getValue().doubleValue() / (Geo.areaOf(geom) / 1000000))));
      } else {
        results[count] = new UsersResult(
            TimestampFormatter.getInstance().isoDateTime(entry.getKey()), toTimestamps[count + 1],
            Double.parseDouble(df.format(entry.getValue().doubleValue())));
      }
      count++;
    }
    return results;
  }

  /**
   * Fills the result value arrays for the ratio/groupBy/boundary response.
   * 
   * @param resultSet <code>Set</code> containing the result values
   * @param df <code>DecimalFormat</code> defining the number of digits of the result values
   * @return <code>Double[]</code> containing the formatted result values
   */
  public Double[] fillElementsRatioGroupByBoundaryResultValues(
      Set<? extends Entry<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, ? extends Number>> resultSet,
      DecimalFormat df) {
    Double[] resultValues = new Double[resultSet.size()];
    int valueCount = 0;
    for (Entry<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, ? extends Number> innerEntry :
        resultSet) {
      resultValues[valueCount] = Double.parseDouble(df.format(innerEntry.getValue().doubleValue()));
      valueCount++;
    }
    return resultValues;
  }

  /**
   * Maps the given <code>OSMEntitySnapshot</code> to a given tag, or to the remainder (having -1,
   * -1 as identifier) if none of the given tags is included.
   * 
   * @param keysInt int value of the groupByKey parameter
   * @param valuesInt Integer[] of the groupByValues parameter
   * @param f <code>OSMEntitySnapshot</code>
   * @return nested <code>Pair</code> containing the integer values of the tag category and the
   *         <code>OSMEntitySnapshot</code>
   */
  public Pair<Pair<Integer, Integer>, OSMEntitySnapshot> mapSnapshotToTags(int keysInt,
      Integer[] valuesInt, OSMEntitySnapshot f) {
    int[] tags = f.getEntity().getRawTags();
    for (int i = 0; i < tags.length; i += 2) {
      int tagKeyId = tags[i];
      int tagValueId = tags[i + 1];
      if (tagKeyId == keysInt) {
        if (valuesInt.length == 0) {
          return new ImmutablePair<>(new ImmutablePair<>(tagKeyId, tagValueId), f);
        }
        for (int value : valuesInt) {
          if (tagValueId == value) {
            return new ImmutablePair<>(new ImmutablePair<>(tagKeyId, tagValueId), f);
          }
        }
      }
    }
    return new ImmutablePair<>(new ImmutablePair<>(-1, -1), f);
  }

  /** Creates a RatioResponse. */
  public Response createRatioResponse(String[] timeArray, Double[] value1, Double[] value2,
      long startTime, RequestResource reqRes, String requestUrl,
      HttpServletResponse servletResponse) {
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
    Metadata metadata = null;
    if (processingData.isShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration,
          Description.aggregateRatio(reqRes.getDescription(), reqRes.getUnit()), requestUrl);
    }
    RequestParameters requestParameters = processingData.getRequestParameters();
    if ("csv".equalsIgnoreCase(requestParameters.getFormat())) {
      writeCsvResponse(resultSet, servletResponse, createCsvTopComments(ElementsRequestExecutor.URL,
          ElementsRequestExecutor.TEXT, Application.API_VERSION, metadata));
      return null;
    }
    return new RatioResponse(
        new Attribution(ExtractMetadata.attributionUrl, ExtractMetadata.attributionShort),
        Application.API_VERSION, metadata, resultSet);
  }

  /** Creates a RatioGroupByBoundaryResponse. */
  public Response createRatioGroupByBoundaryResponse(Object[] boundaryIds, String[] timeArray,
      Double[] resultValues1, Double[] resultValues2, long startTime, RequestResource reqRes,
      String requestUrl, HttpServletResponse servletResponse) {
    Metadata metadata = null;
    int boundaryIdsLength = boundaryIds.length;
    int timeArrayLenth = timeArray.length;
    RatioGroupByResult[] groupByResultSet = new RatioGroupByResult[boundaryIdsLength];
    for (int i = 0; i < boundaryIdsLength; i++) {
      Object groupByName = boundaryIds[i];
      RatioResult[] ratioResultSet = new RatioResult[timeArrayLenth];
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
    if (processingData.isShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration,
          Description.aggregateRatioGroupByBoundary(reqRes.getDescription(), reqRes.getUnit()),
          requestUrl);
    }
    RequestParameters requestParameters = processingData.getRequestParameters();
    Attribution attribution =
        new Attribution(ExtractMetadata.attributionUrl, ExtractMetadata.attributionShort);
    if ("geojson".equalsIgnoreCase(requestParameters.getFormat())) {
      GeoJsonObject[] geoJsonGeoms = processingData.getGeoJsonGeoms();
      return RatioGroupByBoundaryResponse.of(attribution, Application.API_VERSION, metadata,
          "FeatureCollection",
          GroupByBoundaryGeoJsonGenerator.createGeoJsonFeatures(groupByResultSet, geoJsonGeoms));
    } else if ("csv".equalsIgnoreCase(requestParameters.getFormat())) {
      writeCsvResponse(groupByResultSet, servletResponse,
          createCsvTopComments(ElementsRequestExecutor.URL, ElementsRequestExecutor.TEXT,
              Application.API_VERSION, metadata));
      return null;
    }
    return new RatioGroupByBoundaryResponse(attribution, Application.API_VERSION, metadata,
        groupByResultSet);
  }

  /**
   * Extracts and returns a geometry out of the given contribution. The boolean values specify if it
   * should be clipped/unclipped and if the geometry before/after a contribution should be taken.
   */
  public Geometry getGeometry(OSMContribution contribution, boolean clipGeometries,
      boolean before) {
    Geometry geom = null;
    if (clipGeometries) {
      if (before) {
        geom = contribution.getGeometryBefore();
      } else {
        geom = contribution.getGeometryAfter();
      }
    } else {
      if (before) {
        geom = contribution.getGeometryUnclippedBefore();
      } else {
        geom = contribution.getGeometryUnclippedAfter();
      }
    }
    return geom;
  }

  /** Combines the two given filters with an OR operation. Used in /ratio computation. */
  public String combineFiltersWithOr(String firstFilter, String secondFilter) {
    if (firstFilter.isBlank() || secondFilter.isBlank()) {
      // definition of an empty combined filter if filter1 or filter2 is empty
      return "";
    }
    return "(" + firstFilter + ") or (" + secondFilter + ")";
  }

  /**
   * Creates the csv response for /elements/_/groupBy requests.
   * 
   * @param resultSet <code>GroupByObject</code> array containing <code>GroupByResult</code> objects
   *        containing <code>ElementsResult</code> objects
   * @return <code>Pair</code> containing the column names (left) and the data rows (right)
   */
  private ImmutablePair<List<String>, List<String[]>> createCsvResponseForElementsGroupBy(
      GroupByObject[] resultSet) {
    List<String> columnNames = new LinkedList<>();
    columnNames.add("timestamp");
    List<String[]> rows = new LinkedList<>();
    for (int i = 0; i < resultSet.length; i++) {
      GroupByResult groupByResult = (GroupByResult) resultSet[i];
      Object groupByObject = groupByResult.getGroupByObject();
      if (groupByObject instanceof Object[]) {
        Object[] groupByObjectArr = (Object[]) groupByObject;
        columnNames.add(groupByObjectArr[0].toString() + "_" + groupByObjectArr[1].toString());
      } else {
        columnNames.add(groupByObject.toString());
      }
      for (int j = 0; j < groupByResult.getResult().length; j++) {
        ElementsResult elemResult = (ElementsResult) groupByResult.getResult()[j];
        if (i == 0) {
          String[] row = new String[resultSet.length + 1];
          row[0] = elemResult.getTimestamp();
          row[1] = String.valueOf(elemResult.getValue());
          rows.add(row);
        } else {
          rows.get(j)[i + 1] = String.valueOf(elemResult.getValue());
        }
      }
    }
    return new ImmutablePair<>(columnNames, rows);
  }

  /**
   * Creates the csv response for /elements/_/ratio/groupBy requests.
   * 
   * @param resultSet <code>GroupByObject</code> array containing <code>RatioGroupByResult</code>
   *        objects containing <code>RatioResult</code> objects
   * @return <code>Pair</code> containing the column names (left) and the data rows (right)
   */
  private ImmutablePair<List<String>, List<String[]>> createCsvResponseForElementsRatioGroupBy(
      GroupByObject[] resultSet) {
    List<String> columnNames = new LinkedList<>();
    columnNames.add("timestamp");
    List<String[]> rows = new LinkedList<>();
    for (int i = 0; i < resultSet.length; i++) {
      RatioGroupByResult ratioGroupByResult = (RatioGroupByResult) resultSet[i];
      columnNames.add(ratioGroupByResult.getGroupByObject() + "_value");
      columnNames.add(ratioGroupByResult.getGroupByObject() + "_value2");
      columnNames.add(ratioGroupByResult.getGroupByObject() + "_ratio");
      for (int j = 0; j < ratioGroupByResult.getRatioResult().length; j++) {
        RatioResult ratioResult = ratioGroupByResult.getRatioResult()[j];
        if (i == 0) {
          String[] row = new String[resultSet.length * 3 + 1];
          row[0] = ratioResult.getTimestamp();
          row[1] = String.valueOf(ratioResult.getValue());
          row[2] = String.valueOf(ratioResult.getValue2());
          row[3] = String.valueOf(ratioResult.getRatio());
          rows.add(row);
        } else {
          int count = i * 3 + 1;
          rows.get(j)[count] = String.valueOf(ratioResult.getValue());
          rows.get(j)[count + 1] = String.valueOf(ratioResult.getValue2());
          rows.get(j)[count + 2] = String.valueOf(ratioResult.getRatio());
        }
      }
    }
    return new ImmutablePair<>(columnNames, rows);
  }

  /**
   * Creates the csv response for /users/_/groupBy requests.
   * 
   * @param resultSet <code>GroupByObject</code> array containing <code>GroupByResult</code> objects
   *        containing <code>UsersResult</code> objects
   * @return <code>Pair</code> containing the column names (left) and the data rows (right)
   */
  private ImmutablePair<List<String>, List<String[]>> createCsvResponseForUsersGroupBy(
      GroupByObject[] resultSet) {
    List<String> columnNames = new LinkedList<>();
    columnNames.add("fromTimestamp");
    columnNames.add("toTimestamp");
    List<String[]> rows = new LinkedList<>();
    for (int i = 0; i < resultSet.length; i++) {
      GroupByResult groupByResult = (GroupByResult) resultSet[i];
      columnNames.add(groupByResult.getGroupByObject().toString());
      for (int j = 0; j < groupByResult.getResult().length; j++) {
        UsersResult usersResult = (UsersResult) groupByResult.getResult()[j];
        if (i == 0) {
          String[] row = new String[resultSet.length + 2];
          row[0] = usersResult.getFromTimestamp();
          row[1] = usersResult.getToTimestamp();
          row[2] = String.valueOf(usersResult.getValue());
          rows.add(row);
        } else {
          int count = i + 2;
          rows.get(j)[count] = String.valueOf(usersResult.getValue());
        }
      }
    }
    return new ImmutablePair<>(columnNames, rows);
  }

  /**
   * Fills the given stream with output data using multiple parallel threads.
   * 
   * @throws RuntimeException if any one thread experiences an exception, or it only wraps
   *         {@link IOException}
   * @throws DatabaseAccessException if the access to keytables or database is not possible
   * @throws ExecutionException thrown by {@link java.util.concurrent.ForkJoinTask#get() get}
   * @throws InterruptedException thrown by {@link java.util.concurrent.ForkJoinTask#get() get}
   * @throws IOException thrown by {@link java.io.OutputStream#flush() flush}
   */
  private void writeStreamResponse(ThreadLocal<JsonGenerator> outputJsonGen,
      Stream<org.wololo.geojson.Feature> stream, ThreadLocal<ByteArrayOutputStream> outputBuffers,
      final ServletOutputStream outputStream)
      throws ExecutionException, InterruptedException, IOException {
    ThreadLocal<TagTranslator> tts;
    HikariDataSource keytablesConnectionPool;
    if (DbConnData.keytablesDbPoolConfig != null) {
      keytablesConnectionPool = new HikariDataSource(DbConnData.keytablesDbPoolConfig);
      tts = ThreadLocal.withInitial(() -> {
        try {
          return new TagTranslator(keytablesConnectionPool.getConnection());
        } catch (OSHDBKeytablesNotFoundException | SQLException e) {
          throw new DatabaseAccessException(ExceptionMessages.DATABASE_ACCESS);
        }
      });
    } else {
      keytablesConnectionPool = null;
      tts = ThreadLocal.withInitial(() -> DbConnData.tagTranslator);
    }
    ReentrantLock lock = new ReentrantLock();
    AtomicBoolean errored = new AtomicBoolean(false);
    ForkJoinPool threadPool = new ForkJoinPool(ProcessingData.getNumberOfDataExtractionThreads());
    try {
      threadPool.submit(() -> stream.parallel().map(data -> {
        // 0. resolve tags
        Map<String, Object> props = data.getProperties();
        OSHDBTag[] tags = (OSHDBTag[]) props.remove("@tags");
        if (tags != null) {
          for (OSHDBTag tag : tags) {
            OSMTag osmTag = tts.get().getOSMTagOf(tag);
            String key = osmTag.getKey();
            props.put(key.startsWith("@") ? "@" + key : key, osmTag.getValue());
          }
        }
        // 1. convert features to geojson
        try {
          outputBuffers.get().reset();
          outputJsonGen.get().writeObject(data);
          return outputBuffers.get().toByteArray();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }).forEach(data -> {
        // 2. write data out to client
        // only 1 thread is allowed to write at once!
        lock.lock();
        if (errored.get()) {
          // when any one thread experienced an exception (e.g. a client disconnects):
          // the "errored" flag is set and all threads abort themselves by throwing an exception
          lock.unlock();
          throw new RuntimeException();
        }
        try {
          // separate features in the result by a comma, except for the very first one
          if (isFirst.get()) {
            isFirst.set(false);
          } else {
            outputStream.print(", ");
          }
          // write the feature
          outputStream.write(data);
        } catch (IOException e) {
          errored.set(true);
          throw new RuntimeException(e);
        } finally {
          lock.unlock();
        }
      })).get();
    } finally {
      threadPool.shutdown();
      if (keytablesConnectionPool != null) {
        keytablesConnectionPool.close();
      }
      outputStream.flush();
    }
  }

  /** Defines character encoding, content type and cache header in given servlet response object. */
  private HttpServletResponse setCsvSettingsInServletResponse(HttpServletResponse servletResponse) {
    servletResponse.setCharacterEncoding("UTF-8");
    servletResponse.setContentType("text/csv");
    if (!RequestUtils.cacheNotAllowed(processingData.getRequestUrl(),
        processingData.getRequestParameters().getTime())) {
      servletResponse.setHeader("Cache-Control", "no-transform, public, max-age=31556926");
    }
    return servletResponse;
  }

  /**
   * Creates a new CSVWriter, writes the given comments and returns the writer object.
   * 
   * @throws IOException thrown by {@link javax.servlet.ServletResponse#getWriter() getWriter}
   */
  private CSVWriter writeComments(HttpServletResponse servletResponse, List<String[]> comments)
      throws IOException {
    CSVWriter writer =
        new CSVWriter(servletResponse.getWriter(), ';', CSVWriter.DEFAULT_QUOTE_CHARACTER,
            CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
    writer.writeAll(comments, false);
    return writer;
  }

  /** Adds additional properties like the version or the changeset ID to the feature. */
  private Map<String, Object> addAdditionalProperties(OSMEntity entity,
      Map<String, Object> properties, boolean isContributionsEndpoint,
      EnumSet<ContributionType> contributionTypes) {
    properties.put("@version", entity.getVersion());
    properties.put("@osmType", entity.getType());
    properties.put("@changesetId", entity.getChangesetId());
    if (isContributionsEndpoint) {
      if (contributionTypes.contains(ContributionType.CREATION)) {
        properties.put("@creation", "true");
      }
      if (contributionTypes.contains(ContributionType.DELETION)) {
        properties.put("@deletion", "true");
      }
      if (contributionTypes.contains(ContributionType.TAG_CHANGE)) {
        properties.put("@tagChange", "true");
      }
      if (contributionTypes.contains(ContributionType.GEOMETRY_CHANGE)) {
        properties.put("@geometryChange", "true");
      }
    }
    return properties;
  }

  static Set<Integer> keysToKeysInt(String[] keys, TagTranslator tt) {
    final Set<Integer> keysInt;
    if (keys.length != 0) {
      keysInt = new HashSet<>(keys.length);
      for (int i = 0; i < keys.length; i++) {
        keysInt.add(tt.getOSHDBTagKeyOf(keys[i]).toInt());
      }
    } else {
      keysInt = Collections.emptySet();
    }
    return keysInt;
  }

  /** Enum type used in /ratio computation. */
  public enum MatchType {
    MATCHES1, MATCHES2, MATCHESBOTH, MATCHESNONE
  }
}
