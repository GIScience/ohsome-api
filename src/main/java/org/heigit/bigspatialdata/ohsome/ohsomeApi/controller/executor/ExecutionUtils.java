package org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.executor;

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
import org.heigit.bigspatialdata.ohsome.ohsomeApi.Application;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.BoundaryType;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.GeometryBuilder;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.InputProcessor;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.Description;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Attribution;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Metadata;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.RatioResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.RatioResult;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.RatioShareResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.elements.ElementsResult;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.elements.ShareResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.elements.ShareResult;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.RatioGroupByBoundaryResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.RatioGroupByResult;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.ShareGroupByBoundaryResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.ShareGroupByResult;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.users.UsersResult;
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
import org.heigit.bigspatialdata.oshdb.util.geometry.Geo;
import org.heigit.bigspatialdata.oshdb.util.geometry.OSHDBGeometryBuilder;
import org.heigit.bigspatialdata.oshdb.util.time.TimestampFormatter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygonal;

/** Holds helper methods that are used in the executor-classes. */
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
        geom = OSHDBGeometryBuilder.getGeometry(geomBuilder.getBbox());
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
  public SortedMap<OSHDBTimestampAndIndex<Integer>, ? extends Number> computeCountLengthPerimeterAreaGBB(
      RequestResource requestResource, BoundaryType bType,
      MapReducer<? extends OSHDBMapReducible> mapRed, GeometryBuilder geomBuilder,
      boolean isSnapshot) throws Exception {

    if (bType == BoundaryType.NOBOUNDARY)
      throw new BadRequestException(
          "You need to give at least one boundary parameter if you want to use /groupBy/boundary.");
    SortedMap<OSHDBTimestampAndIndex<Integer>, ? extends Number> result = null;
    MapAggregator<OSHDBTimestampAndIndex<Integer>, Geometry> preResult;
    ArrayList<Geometry> geoms = geomBuilder.getGeometry(bType);
    List<Integer> zeroFill = new LinkedList<>();
    for (int j = 0; j < geoms.size(); j++)
      zeroFill.add(j);
    preResult = mapRed.flatMap(f -> {
      List<Pair<Integer, Geometry>> res = new LinkedList<>();
      Geometry entityGeom;
      if (isSnapshot) {
        entityGeom = getSnapshotGeom(f);
        if (requestResource.equals(RequestResource.PERIMETER))
          entityGeom = entityGeom.getBoundary();
      } else {
        entityGeom = getContributionGeom(f);
      }
      for (int i = 0; i < geoms.size(); i++) {
        if (entityGeom.intersects(geoms.get(i))) {
          if (entityGeom.within(geoms.get(i)))
            res.add(new ImmutablePair<>(i, entityGeom));
          else
            res.add(
                new ImmutablePair<>(i, Geo.clip(entityGeom, (Geometry & Polygonal) geoms.get(i))));
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
    }
    return result;
  }

  /** Computes the result for the /count/share/groupBy/boundary resources. */
  public SortedMap<OSHDBTimestampAndIndex<Pair<Integer, Boolean>>, Integer> computeCountShareGBB(
      BoundaryType bType, MapReducer<OSMEntitySnapshot> mapRed, Integer[] keysInt2,
      Integer[] valuesInt2, GeometryBuilder geomBuilder)
      throws UnsupportedOperationException, Exception {

    if (bType == BoundaryType.NOBOUNDARY)
      throw new BadRequestException(
          "You need to give at least one boundary parameter if you want to use /groupBy/boundary.");
    ArrayList<Geometry> geoms = geomBuilder.getGeometry(bType);
    ArrayList<Pair<Integer, Boolean>> zeroFill = new ArrayList<>();
    for (int j = 0; j < geoms.size(); j++) {
      zeroFill.add(new ImmutablePair<>(j, true));
      zeroFill.add(new ImmutablePair<>(j, false));
    }
    SortedMap<OSHDBTimestampAndIndex<Pair<Integer, Boolean>>, Integer> result =
        mapRed.aggregateByTimestamp().flatMap(f -> {
          List<Pair<Integer, OSMEntity>> boundaryList = new LinkedList<>();
          for (int i = 0; i < geoms.size(); i++)
            if (f.getGeometry().intersects(geoms.get(i)))
              boundaryList.add(new ImmutablePair<>(i, f.getEntity()));
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
  public SortedMap<OSHDBTimestampAndIndex<Pair<Integer, Boolean>>, ? extends Number> computeCountLengthPerimeterAreaShareGBB(
      RequestResource requestResource, BoundaryType bType, MapReducer<OSMEntitySnapshot> mapRed,
      Integer[] keysInt2, Integer[] valuesInt2, GeometryBuilder geomBuilder)
      throws UnsupportedOperationException, Exception {

    if (bType == BoundaryType.NOBOUNDARY)
      throw new BadRequestException(
          "You need to give at least one boundary parameter if you want to use /groupBy/boundary.");
    ArrayList<Geometry> geoms = geomBuilder.getGeometry(bType);
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
        if (entityGeom.intersects(geoms.get(i))) {
          if (entityGeom.within(geoms.get(i)))
            res.add(new ImmutablePair<>(new ImmutablePair<>(i, f.getEntity()), entityGeom));
          else
            res.add(new ImmutablePair<>(new ImmutablePair<>(i, f.getEntity()),
                Geo.clip(entityGeom, (Geometry & Polygonal) geoms.get(i))));
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
    }
    return result;
  }

  public SortedMap<OSHDBTimestampAndIndex<Integer>, ? extends Number> computeKeyTagResult(
      RequestResource requestResource,
      MapAggregator<OSHDBTimestampAndIndex<Integer>, OSMEntitySnapshot> preResult)
      throws Exception {

    switch (requestResource) {
      case COUNT:
        return preResult.count();
      case LENGTH:
        return preResult.sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          return Geo.lengthOf(snapshot.getGeometry());
        });
      case PERIMETER:
        return preResult.sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          if (snapshot.getGeometry() instanceof Polygonal)
            return Geo.lengthOf(snapshot.getGeometry().getBoundary());
          else
            return 0.0;
        });
      case AREA:
        return preResult.sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          return Geo.areaOf(snapshot.getGeometry());
        });
      default:
        return null;
    }
  }

  /** Compares an OSMType with an EnumSet of OSMTypes. */
  public boolean isOSMType(EnumSet<OSMType> types, OSMType currentElementType) {

    for (OSMType type : types)
      if (currentElementType.equals(type)) {
        return true;
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
        if (i < valuesInt.length)
          matchesTag = entity.hasTagValue(keysInt[i], valuesInt[i]);
        else
          matchesTag = entity.hasTagKey(keysInt[i]);
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
  public RatioShareResponse createRatioShareResponse(boolean isShare, String[] timeArray,
      Double[] value1, Double[] value2, DecimalFormat df, InputProcessor iP, long startTime,
      RequestResource reqRes, String requestURL, Attribution attribution) {

    RatioShareResponse response;
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
      if (iP.getShowMetadata()) {
        long duration = System.currentTimeMillis() - startTime;
        metadata = new Metadata(duration,
            Description.countLengthPerimeterAreaRatio(reqRes.getLabel(), reqRes.getUnit()),
            requestURL);
      }
      response = new RatioResponse(attribution, Application.apiVersion, metadata, resultSet);
    } else {
      ShareResult[] resultSet = new ShareResult[timeArray.length];
      for (int i = 0; i < timeArray.length; i++) {
        resultSet[i] = new ShareResult(timeArray[i], value1[i], value2[i]);
      }
      Metadata metadata = null;
      if (iP.getShowMetadata()) {
        long duration = System.currentTimeMillis() - startTime;
        metadata = new Metadata(duration,
            Description.countLengthPerimeterAreaShare(reqRes.getLabel(), reqRes.getUnit()),
            requestURL);
      }
      response = new ShareResponse(attribution, Application.apiVersion, metadata, resultSet);
    }
    return response;
  }

  /**
   * Creates either a RatioGroupByBoundaryResponse or a ShareGroupByBoundaryResponse depending on
   * the request.
   */
  public RatioShareResponse createRatioShareGroupByBoundaryResponse(boolean isShare,
      int groupByResultSize, String[] boundaryIds, String[] timeArray,
      ArrayList<Double[]> value1Arrays, ArrayList<Double[]> value2Arrays, DecimalFormat ratioDf,
      InputProcessor iP, long startTime, RequestResource reqRes, String requestURL,
      Attribution attribution) {

    RatioShareResponse response = null;
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
      if (iP.getShowMetadata()) {
        long duration = System.currentTimeMillis() - startTime;
        metadata = new Metadata(duration, Description.countLengthPerimeterAreaRatioGroupByBoundary(
            reqRes.getLabel(), reqRes.getUnit()), requestURL);
      }
      response = new RatioGroupByBoundaryResponse(attribution, Application.apiVersion, metadata,
          groupByResultSet);
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
      if (iP.getShowMetadata()) {
        long duration = System.currentTimeMillis() - startTime;
        metadata = new Metadata(duration, Description.countLengthPerimeterAreaRatioGroupByBoundary(
            reqRes.getLabel(), reqRes.getUnit()), requestURL);
      }
      response = new ShareGroupByBoundaryResponse(attribution, Application.apiVersion, metadata,
          groupByResultSet);
    }

    return response;
  }

  /** Enum type used in /ratio computation. */
  public enum MatchType {
    MATCHES1, MATCHES2, MATCHESBOTH, MATCHESNONE
  }

  /** Internal helper method to get the geometry from an OSMEntitySnapshot object. */
  private Geometry getSnapshotGeom(OSHDBMapReducible f) {
    return ((OSMEntitySnapshot) f).getGeometry();
  }

  /** Internal helper method to get the geometry from an OSMContribution object. */
  private Geometry getContributionGeom(OSHDBMapReducible f) {
    Geometry geom = ((OSMContribution) f).getGeometryAfter();
    if (geom == null)
      geom = ((OSMContribution) f).getGeometryBefore();
    return geom;
  }

}
