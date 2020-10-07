package org.heigit.ohsome.ohsomeapi.executor;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration defining the request resource (LENGTH, PERIMETER, AREA, COUNT, GROUPBYTAG,
 * GROUPBYKEY, RATIO, DATAEXTRACTION, CONTRIBUTION).
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public enum RequestResource {
  LENGTH("length", "meters"), PERIMETER("perimeter", "meters"), AREA("area",
      "square meters"), COUNT("count", "absolute values"), GROUPBYTAG("", ""), GROUPBYKEY("",
          ""), RATIO("", ""), DATAEXTRACTION("OSM data as GeoJSON features.",
              ""), DATAEXTRACTIONFFULLHISTORY("Full-history OSM data as GeoJSON features.",
                  ""), CONTRIBUTIONS("Contributions as GeoJSON features.",
                      ""), CONTRIBUTIONSLATEST("Latest contributions as GeoJSON features.", "");

  private final String description;
  private final String unit;

}
