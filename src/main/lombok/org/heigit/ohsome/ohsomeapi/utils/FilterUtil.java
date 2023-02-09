package org.heigit.ohsome.ohsomeapi.utils;

import java.util.Set;
import java.util.stream.Collectors;
import org.heigit.ohsome.oshdb.osm.OSMType;

/**
 * Utility functions for OSHDB filters.
 */
public class FilterUtil {
  private FilterUtil() {}

  /**
   * Creates an OSHDB filter which selects OSM objects by their type.
   *
   * @param types the set of allowed OSM types
   * @return a string representing an OSHDB filter matching the given set of OSM types
   */
  public static String filter(Set<OSMType> types) {
    return types.stream()
      .map(type -> "type:" + type.toString().toLowerCase())
      .collect(Collectors.joining(" or ", "( ", " )"));
  }
}
