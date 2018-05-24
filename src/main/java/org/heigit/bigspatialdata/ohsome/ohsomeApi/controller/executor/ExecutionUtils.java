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
import org.heigit.bigspatialdata.ohsome.ohsomeApi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.BoundaryType;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.GeometryBuilder;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.elements.ElementsResult;
import org.heigit.bigspatialdata.oshdb.api.generic.OSHDBTimestampAndIndex;
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
