package org.heigit.ohsome.ohsomeapi.output;

/**
 * Holds the description response information that is displayed in the metadata response object.
 */
@SuppressWarnings("checkstyle:MissingJavadocMethod") // all methods here are "simple, obvious"
public class Description {

  private static final String DENSITY_OF_ITEMS_IN_UNIT =
      "Density of selected items (%s of items in %s divided by the area in square kilometers)";
  private static final String NUMBER_OF_ITEMS_IN_UNIT =
      "Total %s of items in %s";
  private static final String DENSITY_OF_USERS =
      "Density of distinct active users  per time interval (number of users per square-kilometer)";
  private static final String NUMBER_OF_USERS =
      "Number of distinct active users per time interval";
  private static final String DENSITY_OF_CONTRIBUTIONS =
      "Density of performed contributions per time interval (number of edits per square-kilometer)";
  private static final String NUMBER_OF_CONTRIBUTIONS =
      "Number of performed contributions per time interval";
  public static final String AGGREGATED_ON_THE_TAG =
      ", aggregated on the tag.";
  public static final String AGGREGATED_ON_THE_BOUNDARY =
      ", aggregated on the boundary.";
  private static final String AGGREGATED_ON_THE_BOUNDARY_AND_TAG =
      ", aggregated on the boundary and on the tag.";
  private static final String AGGREGATED_ON_THE_USER =
      ", aggregated on the user.";
  private static final String AGGREGATED_ON_THE_TYPE =
      ", aggregated on the type.";
  private static final String AGGREGATED_ON_THE_KEY =
      ", aggregated on the key.";
  public static final String RATIO = " satisfying the filter2 parameter (= value2 output),"
      + " as well as items selected by the filter parameter (= value output)"
      + " and the quotient (= ratio output) of value2 to value";

  public static String aggregate(boolean isDensity, String label, String unit) {
    if (isDensity) {
      return String.format(DENSITY_OF_ITEMS_IN_UNIT, label, unit) + ".";
    }
    return String.format(NUMBER_OF_ITEMS_IN_UNIT, label, unit) + ".";
  }

  public static String aggregateGroupByBoundary(boolean isDensity, String label, String unit) {
    if (isDensity) {
      return String.format(DENSITY_OF_ITEMS_IN_UNIT, label, unit) + AGGREGATED_ON_THE_BOUNDARY;
    } else {
      return String.format(NUMBER_OF_ITEMS_IN_UNIT, label, unit) + AGGREGATED_ON_THE_BOUNDARY;
    }
  }

  public static String aggregateGroupByBoundaryGroupByTag(boolean isDensity, String label,
      String unit) {
    if (isDensity) {
      return String.format(DENSITY_OF_ITEMS_IN_UNIT, label, unit)
          + AGGREGATED_ON_THE_BOUNDARY_AND_TAG;
    } else {
      return String.format(NUMBER_OF_ITEMS_IN_UNIT, label, unit)
          + AGGREGATED_ON_THE_BOUNDARY_AND_TAG;
    }
  }

  public static String aggregateGroupByUser(String label, String unit) {
    return String.format(NUMBER_OF_ITEMS_IN_UNIT, label, unit) + AGGREGATED_ON_THE_USER;
  }

  public static String aggregateGroupByTag(boolean isDensity, String label, String unit) {
    if (isDensity) {
      return String.format(DENSITY_OF_ITEMS_IN_UNIT, label, unit) + AGGREGATED_ON_THE_TAG;
    } else {
      return String.format(NUMBER_OF_ITEMS_IN_UNIT, label, unit) + AGGREGATED_ON_THE_TAG;
    }
  }

  public static String countPerimeterAreaGroupByType(boolean isDensity, String label, String unit) {
    if (isDensity) {
      return String.format(DENSITY_OF_ITEMS_IN_UNIT, label, unit) + AGGREGATED_ON_THE_TYPE;
    } else {
      return String.format(NUMBER_OF_ITEMS_IN_UNIT, label, unit) + AGGREGATED_ON_THE_TYPE;
    }
  }

  public static String aggregateGroupByKey(String label, String unit) {
    return String.format(NUMBER_OF_ITEMS_IN_UNIT, label, unit) + AGGREGATED_ON_THE_KEY;
  }

  public static String aggregateRatio(String label, String unit) {
    return String.format(NUMBER_OF_ITEMS_IN_UNIT, label, unit) + RATIO + ".";
  }

  public static String aggregateRatioGroupByBoundary(String label, String unit) {
    return String.format(NUMBER_OF_ITEMS_IN_UNIT, label, unit) + RATIO + AGGREGATED_ON_THE_BOUNDARY;
  }

  public static String countUsers(boolean isDensity) {
    if (isDensity) {
      return DENSITY_OF_USERS + ".";
    } else {
      return NUMBER_OF_USERS + ".";
    }
  }

  public static String countUsersGroupByTag(boolean isDensity) {
    if (isDensity) {
      return DENSITY_OF_USERS + AGGREGATED_ON_THE_TAG;
    } else {
      return NUMBER_OF_USERS + AGGREGATED_ON_THE_TAG;
    }
  }

  public static String countUsersGroupByType(boolean isDensity) {
    if (isDensity) {
      return DENSITY_OF_USERS + AGGREGATED_ON_THE_TYPE;
    } else {
      return NUMBER_OF_USERS + AGGREGATED_ON_THE_TYPE;
    }
  }

  public static String countUsersGroupByKey(boolean isDensity) {
    if (isDensity) {
      return DENSITY_OF_USERS + AGGREGATED_ON_THE_KEY;
    } else {
      return NUMBER_OF_USERS + AGGREGATED_ON_THE_KEY;
    }
  }

  public static String countUsersGroupByBoundary(boolean isDensity) {
    if (isDensity) {
      return DENSITY_OF_USERS + AGGREGATED_ON_THE_BOUNDARY;
    } else {
      return NUMBER_OF_USERS + AGGREGATED_ON_THE_BOUNDARY;
    }
  }

  public static String countContributions(boolean isDensity) {
    if (isDensity) {
      return DENSITY_OF_CONTRIBUTIONS + ".";
    } else {
      return NUMBER_OF_CONTRIBUTIONS + ".";
    }
  }

  public static String countContributionsGroupByBoundary(boolean isDensity) {
    if (isDensity) {
      return DENSITY_OF_CONTRIBUTIONS + AGGREGATED_ON_THE_BOUNDARY;
    } else {
      return NUMBER_OF_CONTRIBUTIONS + AGGREGATED_ON_THE_BOUNDARY;
    }
  }

  private Description() {
    throw new IllegalStateException("Utility class");
  }
}
