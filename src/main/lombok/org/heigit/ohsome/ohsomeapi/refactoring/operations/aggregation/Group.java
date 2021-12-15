package org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.output.elements.ElementsResult;
import org.heigit.ohsome.ohsomeapi.utilities.DecimalFormatDefiner;
import org.heigit.ohsome.oshdb.OSHDBTimestamp;
import org.heigit.ohsome.oshdb.util.geometry.Geo;
import org.heigit.ohsome.oshdb.util.tagtranslator.TagTranslator;
import org.heigit.ohsome.oshdb.util.time.TimestampFormatter;
import org.locationtech.jts.geom.Geometry;
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


  public int getOSHDBTagKey() throws Exception {
    String[] groupByKey = inputProcessor.splitParamOnComma(
        inputProcessor.createEmptyArrayIfNull(servletRequest.getParameterValues("groupByKey")));
    if (groupByKey.length != 1) {
      throw new BadRequestException(ExceptionMessages.GROUP_BY_KEY_PARAM);
    }
    TagTranslator tt = DbConnData.tagTranslator;
    int keysInt = tt.getOSHDBTagKeyOf(groupByKey[0]).toInt();
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

  /** Fills the ElementsResult array with respective ElementsResult objects. */
  public ElementsResult[] fillElementsResult(SortedMap<OSHDBTimestamp, ? extends Number> entryVal,
      boolean isDensity, Geometry geom) {
    ElementsResult[] results = new ElementsResult[entryVal.entrySet().size()];
    int count = 0;
    for (Entry<OSHDBTimestamp, ? extends Number> entry : entryVal.entrySet()) {
      if (isDensity) {
        results[count] = new ElementsResult(
            TimestampFormatter.getInstance().isoDateTime(entry.getKey()), Double.parseDouble(
            df.getDecimalFormat().format(entry.getValue().doubleValue() / (Geo.areaOf(geom) * 0.000001))));
      } else {
        results[count] =
            new ElementsResult(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
                Double.parseDouble(df.getDecimalFormat().format(entry.getValue().doubleValue())));
      }
      count++;
    }
    return results;
  }
}
