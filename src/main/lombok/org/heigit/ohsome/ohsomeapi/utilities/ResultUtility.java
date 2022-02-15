package org.heigit.ohsome.ohsomeapi.utilities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.output.Result;
import org.heigit.ohsome.ohsomeapi.output.elements.ElementsResult;
import org.heigit.ohsome.oshdb.util.geometry.Geo;
import org.heigit.ohsome.oshdb.util.time.TimestampFormatter;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResultUtility {

  private final DecimalFormatDefiner formatDefiner;

  @Autowired
  public ResultUtility(DecimalFormatDefiner formatDefiner) {
    this.formatDefiner = formatDefiner;
  }

  /** Fills the ElementsResult array with respective ElementsResult objects.
   */
  public <U> List<Result> fillElementsResult(SortedMap<U, ? extends Number> entryVal,
      Geometry geom, InputProcessor inputProcessor) {
    List<Result> results = new ArrayList<>();
    for (Entry<U, ? extends Number> entry : entryVal.entrySet()) {
      if (inputProcessor.isDensity()) {
        ElementsResult elementsResult = new ElementsResult(
            TimestampFormatter.getInstance().isoDateTime((Date) entry.getKey()), Double.parseDouble(
            formatDefiner.getDecimalFormat().format(entry.getValue().doubleValue() / (Geo.areaOf(geom) * 0.000001))));
        results.add(elementsResult);
      } else {
        ElementsResult elementsResult = new ElementsResult(TimestampFormatter.getInstance().isoDateTime(
            (Date) entry.getKey()),
            Double.parseDouble(formatDefiner.getDecimalFormat().format(entry.getValue().doubleValue())));
        results.add(elementsResult);
      }
    }
    return results;
  }
}
