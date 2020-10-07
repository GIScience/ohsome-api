package org.heigit.ohsome.ohsomeapi.executor;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration defining the request resource (LENGTH, PERIMETER, AREA, COUNT, GROUPBYTAG,
 * GROUPBYKEY, RATIO, DATAEXTRACTION).
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public enum RequestResource {
  LENGTH("length", "meters"), PERIMETER("perimeter", "meters"), AREA("area",
      "square meters"), COUNT("count", "absolute values"), GROUPBYTAG("",
          ""), GROUPBYKEY("", ""), RATIO("", ""), DATAEXTRACTION("", "");

  private final String label;
  private final String unit;
}
