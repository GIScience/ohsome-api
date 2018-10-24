package org.heigit.bigspatialdata.ohsome.ohsomeapi.executor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.Application;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.rawdata.ElementsGeometry;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.BoundaryType;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.GeometryBuilder;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.Description;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Attribution;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Metadata;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.RatioResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.RatioResult;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.elements.ElementsResult;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.elements.ShareResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.elements.ShareResult;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.GroupByResult;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.RatioGroupByBoundaryResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.RatioGroupByResult;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.ShareGroupByBoundaryResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.ShareGroupByResult;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.users.UsersResult;
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
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygonal;


/** Holds helper methods that are used by the executor classes. */
public class ExecutionUtils {

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

  /**
   * Gets the geometry from the currently in-use boundary object(s).
   * 
   * @param boundary <code>BoundaryType</code> object (NOBOUNDARY, BBOXES, BCIRCLES, BPOLYS).
   * @param geomBuilder <code>GeometryBuilder</code> object.
   * @return <code>Geometry</code> object of the used boundary parameter.
   */
  public Geometry getGeometry(BoundaryType boundary, GeometryBuilder geomBuilder) {
    Geometry geom;
    switch (boundary) {
      case NOBOUNDARY:
        geom = ProcessingData.dataPolyGeom;
        break;
      case BBOXES:
        geom = ProcessingData.bboxesGeom;
        break;
      case BCIRCLES:
        geom = ProcessingData.bcirclesGeom;
        break;
      case BPOLYS:
        geom = ProcessingData.bpolysGeom;
        break;
      default:
        geom = null;
    }
    return geom;
  }

  /** Creates the <code>Feature</code> objects in the OSM data response. */
  public org.wololo.geojson.Feature createOSMDataFeature(String[] keys, String[] values,
      TagTranslator tt, int[] keysInt, int[] valuesInt, OSMEntitySnapshot snapshot,
      Map<String, Object> properties, GeoJSONWriter gjw, boolean includeTags,
      ElementsGeometry elemGeom) {
    properties.put("snapshotTimestamp", snapshot.getTimestamp().toString());
    properties.put("osmId", snapshot.getEntity().getType().toString().toLowerCase() + "/"
        + snapshot.getEntity().getId());
    if (includeTags) {
      for (OSHDBTag oshdbTag : snapshot.getEntity().getTags()) {
        OSMTag tag = tt.getOSMTagOf(oshdbTag.getKey(), oshdbTag.getValue());
        properties.put(tag.getKey(), tag.getValue());
      }
    } else if (!keys.equals(null) && keys.length != 0) {
      int[] tags = snapshot.getEntity().getRawTags();
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
    switch (elemGeom) {
      case RAW:
        return new org.wololo.geojson.Feature(gjw.write(snapshot.getGeometry()), properties);
      case BBOX:
        Envelope envelope = snapshot.getGeometry().getEnvelopeInternal();
        OSHDBBoundingBox bbox = OSHDBGeometryBuilder.boundingBoxOf(envelope);
        return new org.wololo.geojson.Feature(gjw.write(OSHDBGeometryBuilder.getGeometry(bbox)),
            properties);
      case CENTROID:
        return new org.wololo.geojson.Feature(gjw.write(snapshot.getGeometry().getCentroid()),
            properties);
      default:
        return new org.wololo.geojson.Feature(gjw.write(snapshot.getGeometry()), properties);
    }
  }

  /** Computes the result for the /count|length|perimeter|area/groupBy/boundary resources. */
  @SuppressWarnings({"unchecked"}) // intentionally as check for P on Polygonal is already performed
  public <P extends Geometry & Polygonal> SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, ? extends Number> computeCountLengthPerimeterAreaGbB(
      RequestResource requestResource, BoundaryType boundaryType,
      MapReducer<OSMEntitySnapshot> mapRed, GeometryBuilder geomBuilder, boolean isSnapshot)
      throws Exception {
    if (boundaryType == BoundaryType.NOBOUNDARY) {
      throw new BadRequestException(
          "You need to give at least one boundary parameter if you want to use /groupBy/boundary.");
    }
    SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, ? extends Number> result = null;
    MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, Integer>, Geometry> preResult;
    ArrayList<Geometry> arrGeoms = geomBuilder.getGeometry();
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
          } else {
            return Geo.lengthOf(geom.getBoundary());
          }
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
              } else {
                return 0.0;
              }
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
      String groupByBoundaryId = groupByResults[groupByResultCount].getGroupByObject();
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
      String groupByBoundaryId = groupByResults[groupByResultCount].getGroupByObject();
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
      String groupByBoundaryId = groupByResults[groupByResultCount].getGroupByObject();
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

  /** Creates either a RatioResponse or a ShareResponse depending on the request. */
  public Response createRatioShareResponse(boolean isShare, String[] timeArray, Double[] value1,
      Double[] value2, DecimalFormat df, InputProcessor inputProcessor, long startTime,
      RequestResource reqRes, String requestUrl, Attribution attribution) {
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
      if (ProcessingData.showMetadata) {
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
      if (ProcessingData.showMetadata) {
        long duration = System.currentTimeMillis() - startTime;
        metadata = new Metadata(duration,
            Description.countLengthPerimeterAreaShare(reqRes.getLabel(), reqRes.getUnit()),
            requestUrl);
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
      RequestParameters requestParameters, String[] boundaryIds, String[] timeArray,
      Double[] resultValues1, Double[] resultValues2, DecimalFormat ratioDf,
      InputProcessor inputProcessor, long startTime, RequestResource reqRes, String requestUrl,
      Attribution attribution, GeoJsonObject[] geoJsonGeoms) {
    Metadata metadata = null;
    int boundaryIdsLength = boundaryIds.length;
    int timeArrayLenth = timeArray.length;
    if (!isShare) {
      RatioGroupByResult[] groupByResultSet = new RatioGroupByResult[boundaryIdsLength];
      for (int i = 0; i < boundaryIdsLength; i++) {
        String groupByName = boundaryIds[i];
        RatioResult[] resultSet = new RatioResult[timeArrayLenth];
        int innerCount = 0;
        for (int j = i; j < timeArrayLenth * boundaryIdsLength; j += boundaryIdsLength) {
          double ratio = resultValues2[j] / resultValues1[j];
          // in case ratio has the values "NaN", "Infinity", etc.
          try {
            ratio = Double.parseDouble(ratioDf.format(ratio));
          } catch (Exception e) {
            // do nothing --> just return ratio without rounding (trimming)
          }
          resultSet[innerCount] =
              new RatioResult(timeArray[innerCount], resultValues1[j], resultValues2[j], ratio);
          innerCount++;
        }
        groupByResultSet[i] = new RatioGroupByResult(groupByName, resultSet);
      }
      if (ProcessingData.showMetadata) {
        long duration = System.currentTimeMillis() - startTime;
        metadata = new Metadata(duration, Description.countLengthPerimeterAreaRatioGroupByBoundary(
            reqRes.getLabel(), reqRes.getUnit()), requestUrl);
      }
      if (requestParameters.getFormat() != null
          && requestParameters.getFormat().equalsIgnoreCase("geojson")) {
        return RatioGroupByBoundaryResponse.of(attribution, Application.apiVersion, metadata,
            "FeatureCollection", createGeoJsonFeatures(groupByResultSet, geoJsonGeoms));
      } else {
        return new RatioGroupByBoundaryResponse(attribution, Application.apiVersion, metadata,
            groupByResultSet);
      }

    } else {
      ShareGroupByResult[] groupByResultSet = new ShareGroupByResult[boundaryIdsLength];
      for (int i = 0; i < boundaryIdsLength; i++) {
        String groupByName = boundaryIds[i];
        ShareResult[] resultSet = new ShareResult[timeArrayLenth];
        int innerCount = 0;
        for (int j = i; j < timeArrayLenth * boundaryIdsLength; j += boundaryIdsLength) {
          resultSet[innerCount] =
              new ShareResult(timeArray[innerCount], resultValues1[j], resultValues2[j]);
          innerCount++;
        }
        groupByResultSet[i] = new ShareGroupByResult(groupByName, resultSet);
      }
      if (ProcessingData.showMetadata) {
        long duration = System.currentTimeMillis() - startTime;
        metadata = new Metadata(duration, Description.countLengthPerimeterAreaRatioGroupByBoundary(
            reqRes.getLabel(), reqRes.getUnit()), requestUrl);
      }
      if (requestParameters.getFormat() != null
          && requestParameters.getFormat().equalsIgnoreCase("geojson")) {
        return ShareGroupByBoundaryResponse.of(attribution, Application.apiVersion, metadata,
            "FeatureCollection", createGeoJsonFeatures(groupByResultSet, geoJsonGeoms));
      } else {
        return new ShareGroupByBoundaryResponse(attribution, Application.apiVersion, metadata,
            groupByResultSet);
      }
    }
  }

  /** Enum type used in /ratio computation. */
  public enum MatchType {
    MATCHES1, MATCHES2, MATCHESBOTH, MATCHESNONE
  }
}
