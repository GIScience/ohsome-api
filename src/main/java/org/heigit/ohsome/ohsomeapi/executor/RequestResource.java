package org.heigit.ohsome.ohsomeapi.executor;

/**
 * Enumeration defining the request resource (LENGTH, PERIMETER, AREA, COUNT, GROUPBYTAG,
 * GROUPBYKEY, RATIO, DATAEXTRACTION).
 */
public enum RequestResource {
  LENGTH("length", "meters"), PERIMETER("perimeter", "meters"), AREA("area",
      "square meters"), COUNT("count", "absolute values"), GROUPBYTAG("",
          ""), GROUPBYKEY("", ""), RATIO("", ""), DATAEXTRACTION("", "");

  private final String label;
  private final String unit;

  RequestResource(String label, String unit) {
    this.label = label;
    this.unit = unit;
  }

  public String getLabel() {
    return label;
  }

  public String getUnit() {
    return unit;
  }
}
