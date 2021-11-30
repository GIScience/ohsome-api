package org.heigit.ohsome.ohsomeapi.refactoring.results;

import io.swagger.annotations.ApiModelProperty;
import java.text.DecimalFormat;
import java.util.Map.Entry;
import java.util.SortedMap;
import lombok.Getter;
import org.heigit.ohsome.ohsomeapi.output.Result;
import org.heigit.ohsome.oshdb.OSHDBTimestamp;
import org.heigit.ohsome.oshdb.util.geometry.Geo;
import org.heigit.ohsome.oshdb.util.time.TimestampFormatter;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Component;

/**
   * Represents the result JSON object for most of the /elements resources containing the timestamp
   * together with the corresponding value.
   */
  @Getter
  //@AllArgsConstructor
  @Component
  public class ElementsResultRefactoring implements Result {

    @ApiModelProperty(notes = "Timestamp in the format YYYY-MM-DDThh:mm:ssZ", required = true)
    private String timestamp;
    @ApiModelProperty(notes = "Value corresponding to the filter parameters", required = true)
    private double value;

  /** Fills the ElementsResult array with respective ElementsResult objects. */
  public org.heigit.ohsome.ohsomeapi.output.elements.ElementsResult[] fillElementsResult(
      SortedMap<OSHDBTimestamp, ? extends Number> entryVal,
      boolean isDensity, DecimalFormat df, Geometry geom) {
    org.heigit.ohsome.ohsomeapi.output.elements.ElementsResult[] results = new org.heigit.ohsome.ohsomeapi.output.elements.ElementsResult[entryVal.entrySet().size()];
    int count = 0;
    for (Entry<OSHDBTimestamp, ? extends Number> entry : entryVal.entrySet()) {
      if (isDensity) {
        results[count] = new org.heigit.ohsome.ohsomeapi.output.elements.ElementsResult(
            TimestampFormatter.getInstance().isoDateTime(entry.getKey()), Double.parseDouble(
            df.format(entry.getValue().doubleValue() / (Geo.areaOf(geom) * 0.000001))));
      } else {
        results[count] =
            new org.heigit.ohsome.ohsomeapi.output.elements.ElementsResult(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
                Double.parseDouble(df.format(entry.getValue().doubleValue())));
      }
      count++;
    }
    return results;
  }
}
