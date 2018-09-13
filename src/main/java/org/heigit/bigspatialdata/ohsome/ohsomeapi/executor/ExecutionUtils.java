package org.heigit.bigspatialdata.ohsome.ohsomeapi.executor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.SortedMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.Application;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.BoundaryType;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.GeometryBuilder;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.Description;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.Attribution;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.Metadata;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.RatioResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.RatioResult;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.Response;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.elements.ElementsResult;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.elements.ShareResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.elements.ShareResult;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.groupByResponse.GroupByResult;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.groupByResponse.RatioGroupByBoundaryResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.groupByResponse.RatioGroupByResult;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.groupByResponse.ShareGroupByBoundaryResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.groupByResponse.ShareGroupByResult;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.users.UsersResult;
import org.heigit.bigspatialdata.oshdb.api.generic.OSHDBTimestampAndIndex;
import org.heigit.bigspatialdata.oshdb.api.generic.function.SerializableFunction;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.object.OSHDBMapReducible;
import org.heigit.bigspatialdata.oshdb.api.object.OSMContribution;
import org.heigit.bigspatialdata.oshdb.api.object.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.osm.OSMEntity;
import org.heigit.bigspatialdata.oshdb.osm.OSMType;
import org.heigit.bigspatialdata.oshdb.util.OSHDBTimestamp;
import org.heigit.bigspatialdata.oshdb.util.celliterator.ContributionType;
import org.heigit.bigspatialdata.oshdb.util.geometry.Geo;
import org.heigit.bigspatialdata.oshdb.util.time.TimestampFormatter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Polygonal;
import com.vividsolutions.jts.geom.TopologyException;

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
        geom = geomBuilder.getDataPoly();
        break;
      case BBOXES:
        geom = geomBuilder.getBbox();
        break;
      case BCIRCLES:
        geom = geomBuilder.getBcircleGeom();
        break;
      case BPOLYS:
        geom = geomBuilder.getBpoly();
        break;
      default:
        geom = null;
    }
    return geom;
  }

  /** Computes the result for the /count|length|perimeter|area/groupBy/boundary resources. */
  public SortedMap<OSHDBTimestampAndIndex<Integer>, ? extends Number> computeCountLengthPerimeterAreaGbB(
      RequestResource requestResource, BoundaryType boundaryType,
      MapReducer<? extends OSHDBMapReducible> mapRed, GeometryBuilder geomBuilder,
      boolean isSnapshot) throws Exception {
    if (boundaryType == BoundaryType.NOBOUNDARY) {
      throw new BadRequestException(
          "You need to give at least one boundary parameter if you want to use /groupBy/boundary.");
    }
    SortedMap<OSHDBTimestampAndIndex<Integer>, ? extends Number> result = null;
    MapAggregator<OSHDBTimestampAndIndex<Integer>, Geometry> preResult;
    ArrayList<Geometry> geoms = geomBuilder.getGeometry();
    List<Integer> zeroFill = new LinkedList<>();
    for (int j = 0; j < geoms.size(); j++) {
      zeroFill.add(j);
    }
    preResult = mapRed.flatMap(f -> {
      List<Pair<Integer, Geometry>> res = new LinkedList<>();
      Geometry entityGeom;
      if (isSnapshot) {
        entityGeom = getSnapshotGeom(f);
        if (requestResource.equals(RequestResource.PERIMETER)) {
          entityGeom = entityGeom.getBoundary();
        }
      } else {
        entityGeom = getContributionGeom(f);
      }
      for (int i = 0; i < geoms.size(); i++) {
        if (myIntersects(entityGeom, geoms.get(i))) {
          if (myWithin(entityGeom, geoms.get(i))) {
            res.add(new ImmutablePair<>(i, entityGeom));
          } else {
            try {
              res.add(new ImmutablePair<>(i,
                  Geo.clip(entityGeom, (Geometry & Polygonal) geoms.get(i))));
            } catch (Exception e) {
              // do nothing
            }
          }
        }
      }
      return res;
    }).aggregateByTimestamp().aggregateBy(Pair::getKey).zerofillIndices(zeroFill)
        .map(Pair::getValue);
    switch (requestResource) {
      case COUNT:
        result = preResult.count();
        break;
      case LENGTH:
      case PERIMETER:
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

  /** Computes the result for the /count/share/groupBy/boundary resources. */
  public SortedMap<OSHDBTimestampAndIndex<Pair<Integer, Boolean>>, Integer> computeCountShareGbB(
      BoundaryType boundaryType, MapReducer<OSMEntitySnapshot> mapRed, Integer[] keysInt2,
      Integer[] valuesInt2, GeometryBuilder geomBuilder)
      throws UnsupportedOperationException, Exception {
    if (boundaryType == BoundaryType.NOBOUNDARY) {
      throw new BadRequestException(
          "You need to give at least one boundary parameter if you want to use /groupBy/boundary.");
    }
    ArrayList<Geometry> geoms = geomBuilder.getGeometry();
    ArrayList<Pair<Integer, Boolean>> zeroFill = new ArrayList<>();
    for (int j = 0; j < geoms.size(); j++) {
      zeroFill.add(new ImmutablePair<>(j, true));
      zeroFill.add(new ImmutablePair<>(j, false));
    }
    SortedMap<OSHDBTimestampAndIndex<Pair<Integer, Boolean>>, Integer> result =
        mapRed.aggregateByTimestamp().flatMap(f -> {
          List<Pair<Integer, OSMEntity>> boundaryList = new LinkedList<>();
          for (int i = 0; i < geoms.size(); i++) {
            if (myIntersects(f.getGeometry(), geoms.get(i))) {
              boundaryList.add(new ImmutablePair<>(i, f.getEntity()));
            }
          }
          return boundaryList;
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
        }).zerofillIndices(zeroFill).count();

    return result;
  }

  /** Computes the result for the /count|length|perimeter|area/share/groupBy/boundary resources. */
  public SortedMap<OSHDBTimestampAndIndex<Pair<Integer, Boolean>>, ? extends Number> computeCountLengthPerimeterAreaShareGbB(
      RequestResource requestResource, BoundaryType boundaryType,
      MapReducer<OSMEntitySnapshot> mapRed, Integer[] keysInt2, Integer[] valuesInt2,
      GeometryBuilder geomBuilder) throws UnsupportedOperationException, Exception {
    if (boundaryType == BoundaryType.NOBOUNDARY) {
      throw new BadRequestException(
          "You need to give at least one boundary parameter if you want to use /groupBy/boundary.");
    }
    ArrayList<Geometry> geoms = geomBuilder.getGeometry();
    List<Pair<Integer, Boolean>> zeroFill = new LinkedList<>();
    for (int j = 0; j < geoms.size(); j++) {
      zeroFill.add(new ImmutablePair<>(j, true));
      zeroFill.add(new ImmutablePair<>(j, false));
    }
    MapAggregator<OSHDBTimestampAndIndex<Pair<Integer, Boolean>>, Geometry> preResult = null;
    SortedMap<OSHDBTimestampAndIndex<Pair<Integer, Boolean>>, ? extends Number> result = null;
    preResult = mapRed.aggregateByTimestamp().flatMap(f -> {
      List<Pair<Pair<Integer, OSMEntity>, Geometry>> res = new LinkedList<>();
      Geometry entityGeom = f.getGeometry();
      if (requestResource.equals(RequestResource.PERIMETER)) {
        entityGeom = entityGeom.getBoundary();
      }
      for (int i = 0; i < geoms.size(); i++) {
        if (myIntersects(entityGeom, geoms.get(i))) {
          if (myWithin(entityGeom, geoms.get(i))) {
            res.add(new ImmutablePair<>(new ImmutablePair<>(i, f.getEntity()), entityGeom));
          } else {
            try {
              res.add(new ImmutablePair<>(new ImmutablePair<>(i, f.getEntity()),
                  Geo.clip(entityGeom, (Geometry & Polygonal) geoms.get(i))));
            } catch (Exception e) {
              // do nothing
            }
          }
        }
      }
      return res;
    }).aggregateBy(f -> {
      // result aggregated on true (if obj contains all tags) and false (if not)
      boolean hasTags = false;
      for (int i = 0; i < keysInt2.length; i++) {
        if (f.getLeft().getRight().hasTagKey(keysInt2[i])) {
          if (i >= valuesInt2.length) {
            // if more keys2 than values2 are given
            hasTags = true;
            continue;
          }
          if (f.getLeft().getRight().hasTagValue(keysInt2[i], valuesInt2[i])) {
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
      return new ImmutablePair<>(f.getLeft().getLeft(), hasTags);
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
      default:
        break;
    }
    return result;
  }

  /**
   * Computes the result depending on the <code>RequestResource</code> using a
   * <code>MapAggregator</code> object as input and returning a <code>SortedMap</code>.
   */
  @SuppressWarnings({"unchecked"}) // intentionally suppressed
  public <K extends OSHDBTimestampAndIndex<?>, V extends Number> SortedMap<K, V> computeResult(
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
      RequestParameters requestParameters, int groupByResultSize, String[] boundaryIds,
      String[] timeArray, ArrayList<Double[]> value1Arrays, ArrayList<Double[]> value2Arrays,
      DecimalFormat ratioDf, InputProcessor inputProcessor, long startTime, RequestResource reqRes,
      String requestUrl, Attribution attribution, GeoJsonObject[] geoJsonGeoms) {
    Metadata metadata = null;
    int count = 0;
    if (!isShare) {
      RatioGroupByResult[] groupByResultSet = new RatioGroupByResult[groupByResultSize / 3];
      for (int i = 0; i < groupByResultSize; i += 3) {
        Double[] value1 = value1Arrays.get(count);
        Double[] value2 = value2Arrays.get(count);
        String groupByName = boundaryIds[count];
        RatioResult[] resultSet = new RatioResult[timeArray.length];
        for (int j = 0; j < timeArray.length; j++) {
          double ratio = value2[j] / value1[j];
          // in case ratio has the values "NaN", "Infinity", etc.
          try {
            ratio = Double.parseDouble(ratioDf.format(ratio));
          } catch (Exception e) {
            // do nothing --> just return ratio without rounding (trimming)
          }
          resultSet[j] = new RatioResult(timeArray[j], value1[j], value2[j], ratio);
        }
        groupByResultSet[count] = new RatioGroupByResult(groupByName, resultSet);
        count++;
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
      ShareGroupByResult[] groupByResultSet = new ShareGroupByResult[groupByResultSize / 3];
      for (int i = 0; i < groupByResultSize; i += 3) {
        Double[] value1 = value1Arrays.get(count);
        Double[] value2 = value2Arrays.get(count);
        String groupByName = boundaryIds[count];
        ShareResult[] resultSet = new ShareResult[timeArray.length];
        for (int j = 0; j < timeArray.length; j++) {
          resultSet[j] = new ShareResult(timeArray[j], value1[j], value2[j]);
        }
        groupByResultSet[count] = new ShareGroupByResult(groupByName, resultSet);
        count++;
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

  /**
   * Checks if a Geometry is intersecting with another Geometry. Can also handle
   * GeometryCollections.
   */
  private boolean myIntersects(Geometry geomA, Geometry geomB) {
    if (geomA.getClass().equals(GeometryCollection.class)) {
      GeometryCollection geomColl = (GeometryCollection) geomA;
      int count = geomColl.getNumGeometries();
      for (int i = 0; i < count; i++) {
        Geometry childGeometry = geomColl.getGeometryN(i);
        if (myIntersects(childGeometry, geomB)) {
          return true;
        }
      }
      return false;
    }
    try {
      return geomA.intersects(geomB);
    } catch (TopologyException te) {
      // incorrect geometries get ignored in the computation
      return false;
    }
  }

  /**
   * Checks if a Geometry is within another Geometry. Can also handle GeometryCollections.
   */
  private boolean myWithin(Geometry geomA, Geometry geomB) {
    if (geomA.getClass().equals(GeometryCollection.class)) {
      GeometryCollection geomColl = (GeometryCollection) geomA;
      int count = geomColl.getNumGeometries();
      for (int i = 0; i < count; i++) {
        Geometry childGeometry = geomColl.getGeometryN(i);
        if (!myWithin(childGeometry, geomB)) {
          return false;
        }
      }
      return true;
    }
    return geomA.within(geomB);
  }

  /** Internal helper method to get the geometry from an OSMEntitySnapshot object. */
  private Geometry getSnapshotGeom(OSHDBMapReducible f) {
    return ((OSMEntitySnapshot) f).getGeometry();
  }

  /** Internal helper method to get the geometry from an OSMContribution object. */
  private Geometry getContributionGeom(OSHDBMapReducible f) {
    Geometry geom = ((OSMContribution) f).getGeometryAfter();
    if (geom == null) {
      geom = ((OSMContribution) f).getGeometryBefore();
    }
    return geom;
  }
}
