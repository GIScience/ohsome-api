package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.output.elements.ElementsResult;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.utilities.DecimalFormatDefiner;
import org.heigit.ohsome.oshdb.OSHDBTimestamp;
import org.heigit.ohsome.oshdb.api.generic.OSHDBCombinedIndex;
import org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator;
import org.heigit.ohsome.oshdb.util.geometry.Geo;
import org.heigit.ohsome.oshdb.util.mappable.OSMEntitySnapshot;
import org.heigit.ohsome.oshdb.util.tagtranslator.TagTranslator;
import org.heigit.ohsome.oshdb.util.time.TimestampFormatter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygonal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class Group {

  @Autowired
  DecimalFormatDefiner df;
  @Autowired
  InputProcessor inputProcessor;
  @Autowired
  HttpServletRequest servletRequest;


  public int getOSHDBKeyOfOneTag() throws Exception {
    String[] groupByKey = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByKey")));
    if (groupByKey.length != 1) {
      throw new BadRequestException(ExceptionMessages.GROUP_BY_KEY_PARAM);
    }
    TagTranslator tt = DbConnData.tagTranslator;
    int keysInt = tt.getOSHDBTagKeyOf(groupByKey[0]).toInt();
   return keysInt;
  }

  public Integer[] getOSHDBKeysOfMultipleTags() {
    String[] groupByKeys = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByKeys")));
    if (groupByKeys == null || groupByKeys.length == 0) {
      throw new BadRequestException(ExceptionMessages.GROUP_BY_KEYS_PARAM);
    }
    TagTranslator tt = DbConnData.tagTranslator;
    Integer[] keysInt = new Integer[groupByKeys.length];
    for (int i = 0; i < groupByKeys.length; i++) {
      keysInt[i] = tt.getOSHDBTagKeyOf(groupByKeys[i]).toInt();
    }
    return keysInt;
  }

  public Integer[] getOSHDBTag() {
    String[] groupByKey = inputProcessor.splitParamOnComma(
    inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByKey")));
    if (groupByKey.length != 1) {
      throw new BadRequestException(ExceptionMessages.GROUP_BY_KEY_PARAM);
    }
    String[] groupByValues = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByValues")));
    Integer[] valuesInt = new Integer[groupByValues.length];
    TagTranslator tt = DbConnData.tagTranslator;
    if (groupByValues.length != 0) {
      for (int j = 0; j < groupByValues.length; j++) {
        valuesInt[j] = tt.getOSHDBTagOf(groupByKey[0], groupByValues[j]).getValue();
      }
    }
    return valuesInt;
  }

  public List<Pair<Integer, Integer>> getListOfKeyValuePair(int keysInt, Integer[] valuesInt) {
    ArrayList<Pair<Integer, Integer>> zeroFill = new ArrayList<>();
    String[] groupByValues = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByValues")));
    if (groupByValues.length != 0) {
      for (int j = 0; j < groupByValues.length; j++) {
        zeroFill.add(new ImmutablePair<>(keysInt, valuesInt[j]));
      }
    }
    return zeroFill;
  }

  /**
   * Computes the result depending on the <code>RequestResource</code> using a
   * <code>MapAggregator</code> object as input and returning a <code>SortedMap</code>.
   *
   * @throws Exception thrown by {@link org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator
   *         #count() count}, and
   *         {@link org.heigit.ohsome.oshdb.api.mapreducer.MapAggregator#sum() sum}
   */
  @SuppressWarnings({"unchecked"}) // intentionally suppressed as type format is valid
  public static <K extends Comparable<K> & Serializable, V extends Number>
  SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, K>, V> computeResult(
      Operation operation,
      MapAggregator<OSHDBCombinedIndex<OSHDBTimestamp, K>, OSMEntitySnapshot> mapAgg)
      throws Exception {
    var mapAggGeom = mapAgg.map(OSMEntitySnapshot::getGeometry);
    if (operation instanceof Count) {
      return (SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, K>, V>) mapAgg.count();
    } else if (operation instanceof Perimeter) {
      return (SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, K>, V>) mapAggGeom
          .sum(geom -> {
            if (!(geom instanceof Polygonal)) {
              return 0.0;
            }
            return cacheInUserData(geom, () -> Geo.lengthOf(geom.getBoundary()));
          });
    } else if (operation instanceof Length) {
      return (SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, K>, V>) mapAggGeom
          .sum(geom -> cacheInUserData(geom, () -> Geo.lengthOf(geom)));
    } else if (operation instanceof Area) {
      return (SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, K>, V>) mapAggGeom
          .sum(geom -> cacheInUserData(geom, () -> Geo.areaOf(geom)));
    } else {
      return null;
    }
  }

  /**
   * Adapted helper function, which works like
   * {@link org.heigit.ohsome.oshdb.api.generic.OSHDBCombinedIndex#nest(Map) nest} but has
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

  /** Fills the ElementsResult array with respective ElementsResult objects.
   * @return*/
  public List<ElementsResult> fillElementsResult(SortedMap<OSHDBTimestamp, ? extends Number> entryVal,
      boolean isDensity, Geometry geom) {
    List<ElementsResult> results = new ArrayList<>();
    int count = 0;
    for (Entry<OSHDBTimestamp, ? extends Number> entry : entryVal.entrySet()) {
      if (isDensity) {
        results.add(new ElementsResult(
            TimestampFormatter.getInstance().isoDateTime(entry.getKey()), Double.parseDouble(
            df.getDecimalFormat().format(entry.getValue().doubleValue() / (Geo.areaOf(geom) * 0.000001)))));
      } else {
        results.add(new ElementsResult(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
                Double.parseDouble(df.getDecimalFormat().format(entry.getValue().doubleValue()))));
      }
      count++;
    }
    return results;
  }
}
