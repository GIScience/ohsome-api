package org.heigit.ohsome.ohsomeapi.utils;

import java.util.Set;
import java.util.stream.Collectors;
import org.heigit.ohsome.oshdb.osm.OSMType;

public class FilterUtil {
  private FilterUtil() {}

  public static String filter(Set<OSMType> types) {
    return types.stream()
      .map(type -> "type:" + type.toString().toLowerCase())
      .collect(Collectors.joining(" or ", "( "," )"));
  }
}
