package org.heigit.bigspatialdata.ohsome.ohsomeapi.output;

/**
 * Holds the description response information that is displayed in the metadata response object.
 */
public class Description {

  public static String countLengthPerimeterArea(boolean isDensity, String label, String unit) {
    if (isDensity) {
      return "Density of selected items (" + label + " of items in " + unit
          + " divided by the area in square kilometers).";
    }
    return "Total " + label + " of items in " + unit + ".";
  }

  public static String countLengthPerimeterAreaGroupByBoundary(boolean isDensity, String label,
      String unit) {
    if (isDensity) {
      return "Density of selected items (" + label + " of items in " + unit
          + " divided by the area in square kilometers), aggregated on the boundary.";
    }
    return "Total " + label + " of items in " + unit + ", aggregated on the boundary.";
  }

  public static String countLengthPerimeterAreaGroupByBoundaryGroupByTag(boolean isDensity,
      String label, String unit) {
    if (isDensity) {
      return "Density of selected items (" + label + " of items in " + unit + " divided by the "
          + "area in square kilometers), aggregated on the boundary and on the tag.";
    }
    return "Total " + label + " of items in " + unit
        + ", aggregated on the boundary and on the tag.";
  }

  public static String countLengthPerimeterAreaGroupByUser(String label, String unit) {
    return "Total " + label + " of items in " + unit + ", aggregated on the user.";
  }

  public static String countLengthPerimeterAreaGroupByTag(boolean isDensity, String label,
      String unit) {
    if (isDensity) {
      return "Density of selected items (" + label + " of items in " + unit
          + " divided by the area in square kilometers), aggregated on the tag.";
    }
    return "Total " + label + " of items in " + unit + ", aggregated on the tag.";
  }

  public static String countPerimeterAreaGroupByType(boolean isDensity, String label, String unit) {
    if (isDensity) {
      return "Density of selected items (" + label + " of items in " + unit
          + " divided by the area in square kilometers), aggregated on the type.";
    }
    return "Total " + label + " of items in " + unit + ", aggregated on the type.";
  }

  public static String countLengthPerimeterAreaGroupByKey(String label, String unit) {
    return "Total " + label + " of items in " + unit + ", aggregated on the key.";
  }

  public static String countLengthPerimeterAreaShare(String label, String unit) {
    return "Total " + label + " of the whole and of a share (= part) of items in " + unit
        + " satisfying keys2 and values2 within items selected by types, keys, values.";
  }

  public static String countLengthPerimeterAreaShareGroupByBoundary(String label, String unit) {
    return "Total " + label + " of the whole and of a share (= part) of items in " + unit
        + " satisfying keys2 and values2 within items selected by types, keys, values, "
        + "aggregated on the boundary.";
  }

  public static String countLengthPerimeterAreaRatio(String label, String unit) {
    return "Total " + label + " of items in " + unit
        + " satisfying types2, keys2, values2 parameters (= value2 output),"
        + " as well as items selected by types, keys, values parameters (= value output) "
        + "and ratio of value2:value.";
  }

  public static String countLengthPerimeterAreaRatioGroupByBoundary(String label, String unit) {
    return "Total " + label + " of items in " + unit
        + " satisfying types2, keys2, values2 parameters (= value2 output), as well as items"
        + " selected by types, keys, values parameters (= value output) and ratio of value2:value, "
        + "aggregated on the boundary objects.";
  }

  public static String usersCount(boolean isDensity) {
    if (isDensity) {
      return "Density of distinct active users per time interval "
          + "(number of users per square-kilometer).";
    }
    return "Number of distinct active users per time interval.";
  }

  public static String usersCountGroupByTag(boolean isDensity) {
    if (isDensity) {
      return "Density of distinct active users per time interval "
          + "(number of users per square-kilometer) aggregated on the tag.";
    }
    return "Number of distinct active users per time interval aggregated on the tag.";
  }

  public static String usersCountGroupByType(boolean isDensity) {
    if (isDensity) {
      return "Density of distinct active users per time interval "
          + "(number of users per square-kilometer) aggregated on the type.";
    }
    return "Number of distinct active users per time interval aggregated on the type.";
  }

  public static String usersCountGroupByKey(boolean isDensity) {
    if (isDensity) {
      return "Density of distinct active users per time interval "
          + "(number of users per square-kilometer) aggregated on the key.";
    }
    return "Number of distinct active users per time interval aggregated on the key.";
  }
  
  public static String usersCountGroupByBoundary(boolean isDensity) {
    if (isDensity) {
      return "Density of distinct active users per time interval "
          + "(number of users per square-kilometer) aggregated on the boundary.";
    }
    return "Number of distinct active users per time interval aggregated on the boundary.";
  }

  private Description() {
    throw new IllegalStateException("Utility class");
  }
}
