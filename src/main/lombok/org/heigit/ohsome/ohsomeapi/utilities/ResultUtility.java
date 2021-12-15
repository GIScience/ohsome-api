package org.heigit.ohsome.ohsomeapi.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
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

  /** Fills the ElementsResult array with respective ElementsResult objects.
   */
  public <T> List<T> fillElementsResult(SortedMap<OSHDBTimestamp, ? extends Number> entryVal,
      boolean isDensity, Geometry geom) {
    List<T> results = new ArrayList<>();
    //Result[] results = new ElementsResult[entryVal.entrySet().size()];
    int count = 0;
    for (Entry<OSHDBTimestamp, ? extends Number> entry : entryVal.entrySet()) {
      if (isDensity) {
        ElementsResult elementsResult = new ElementsResult(
            TimestampFormatter.getInstance().isoDateTime(entry.getKey()), Double.parseDouble(
            formatDefiner.getDecimalFormatForRatioRequests().format(entry.getValue().doubleValue() / (Geo.areaOf(geom) * 0.000001))));
        results.add((T) elementsResult);
      } else {
        ElementsResult elementsResult = new ElementsResult(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
            Double.parseDouble(formatDefiner.getDecimalFormat().format(entry.getValue().doubleValue())));
        results.add((T) elementsResult);
      }
      count++;
    }
    return results;
  }
}
