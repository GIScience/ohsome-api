package org.heigit.ohsome.ohsomeapi.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.output.elements.ElementsResult;
import org.heigit.ohsome.oshdb.OSHDBTimestamp;
import org.heigit.ohsome.oshdb.util.geometry.Geo;
import org.heigit.ohsome.oshdb.util.time.TimestampFormatter;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResultUtility {

  @Autowired
  DecimalFormatDefiner formatDefiner;
  @Autowired
  InputProcessor inputProcessor;

  /** Fills the ElementsResult array with respective ElementsResult objects.
   */
  public <T> List<T> fillElementsResult(SortedMap<OSHDBTimestamp, ? extends Number> entryVal, Geometry geom) {
    List<T> results = new ArrayList<>();
    for (Entry<OSHDBTimestamp, ? extends Number> entry : entryVal.entrySet()) {
      if (inputProcessor.isDensity()) {
        ElementsResult elementsResult = new ElementsResult(
            TimestampFormatter.getInstance().isoDateTime(entry.getKey()), Double.parseDouble(
            formatDefiner.getDecimalFormat().format(entry.getValue().doubleValue() / (Geo.areaOf(geom) * 0.000001))));
        results.add((T) elementsResult);
      } else {
        ElementsResult elementsResult = new ElementsResult(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
            Double.parseDouble(formatDefiner.getDecimalFormat().format(entry.getValue().doubleValue())));
        results.add((T) elementsResult);
      }
    }
    return results;
  }
}
