package org.heigit.bigspatialdata.ohsome.ohsomeapi.exception;

/** Holds custom error messages used in several classes for different exceptions. */
public class ExceptionMessages {

  public static String payloadTooLarge =
      "The given query is too large. Please use a smaller region and/or coarser time period.";
  public static String noBoundary =
      "You need to give at least one boundary parameter if you want to use /groupBy/boundary.";
  public static String boundaryNotInDataExtract = "The provided boundary parameter "
      + "does not lie completely within the underlying data-extract polygon.";
  public static String bpolysFormat = "The bpolys parameter must contain "
      + "double-parseable values in form of lon/lat coordinate pairs.";
  public static String boundaryParamFormat = "Error in processing the boundary parameter. Please "
      + "remember to follow the format, where you separate each boundary object with a pipe-sign "
      + "'|' and add optional custom ids to every first coordinate with a colon ':'.";
  public static String boundaryIdsFormat = "One or more boundary object(s) have a custom id "
      + "(or at least a colon), whereas other(s) don't. You can either set custom ids for all your "
      + "boundary objects, or for none.";
  public static String boundaryParamFormatOrCount = "Your provided boundary parameter (bboxes, "
      + "bcircles, or bpolys) does not fit its format, or you defined more than one boundary "
      + "parameter.";
  public static String groupByKeyParam =
      "You need to give one groupByKey parameter, if you want to use groupBy/tag.";
  public static String groupByKeysParam =
      "You need to give one groupByKeys parameter, if you want to use groupBy/key.";
}
