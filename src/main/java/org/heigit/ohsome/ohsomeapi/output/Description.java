package org.heigit.ohsome.ohsomeapi.output;

/**
 * Holds the description response information that is displayed in the metadata response object.
 */
public class Description {

  public static String aggregate(boolean isDensity, String label, String unit) {
    if (isDensity) {
      return "Density of selected items (" + label + " of items in " + unit
          + " divided by the area in square kilometers).";
    }
    return "Total " + label + " of items in " + unit + ".";
  }

  public static String aggregateGroupByBoundary(boolean isDensity, String label, String unit) {
    if (isDensity) {
      return "Density of selected items (" + label + " of items in " + unit
          + " divided by the area in square kilometers), aggregated on the boundary.";
    }
    return "Total " + label + " of items in " + unit + ", aggregated on the boundary.";
  }

  public static String aggregateGroupByBoundaryGroupByTag(boolean isDensity, String label,
      String unit) {
    if (isDensity) {
      return "Density of selected items (" + label + " of items in " + unit + " divided by the "
          + "area in square kilometers), aggregated on the boundary and on the tag.";
    }
    return "Total " + label + " of items in " + unit
        + ", aggregated on the boundary and on the tag.";
  }

  public static String aggregateGroupByUser(String label, String unit) {
    return "Total " + label + " of items in " + unit + ", aggregated on the user.";
  }

  public static String aggregateGroupByTag(boolean isDensity, String label, String unit) {
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

  public static String aggregateGroupByKey(String label, String unit) {
    return "Total " + label + " of items in " + unit + ", aggregated on the key.";
  }

  public static String aggregateRatio(String label, String unit) {
    return "Total " + label + " of items in " + unit
        + " satisfying types2, keys2, values2 parameters (= value2 output),"
        + " as well as items selected by types, keys, values parameters (= value output) "
        + "and ratio of value2:value.";
  }

  public static String aggregateRatioGroupByBoundary(String label, String unit) {
    return "Total " + label + " of items in " + unit
        + " satisfying types2, keys2, values2 parameters (= value2 output), as well as items"
        + " selected by types, keys, values parameters (= value output) and ratio of value2:value, "
        + "aggregated on the boundary objects.";
  }

  public static String countUsers(boolean isDensity) {
    if (isDensity) {
      return "Density of distinct active users per time interval "
          + "(number of users per square-kilometer).";
    }
    return "Number of distinct active users per time interval.";
  }

  public static String countUsersGroupByTag(boolean isDensity) {
    if (isDensity) {
      return "Density of distinct active users per time interval "
          + "(number of users per square-kilometer) aggregated on the tag.";
    }
    return "Number of distinct active users per time interval aggregated on the tag.";
  }

  public static String countUsersGroupByType(boolean isDensity) {
    if (isDensity) {
      return "Density of distinct active users per time interval "
          + "(number of users per square-kilometer) aggregated on the type.";
    }
    return "Number of distinct active users per time interval aggregated on the type.";
  }

  public static String countUsersGroupByKey(boolean isDensity) {
    if (isDensity) {
      return "Density of distinct active users per time interval "
          + "(number of users per square-kilometer) aggregated on the key.";
    }
    return "Number of distinct active users per time interval aggregated on the key.";
  }

  public static String countUsersGroupByBoundary(boolean isDensity) {
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
