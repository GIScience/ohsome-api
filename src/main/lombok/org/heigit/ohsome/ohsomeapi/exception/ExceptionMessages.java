package org.heigit.ohsome.ohsomeapi.exception;

/** Holds custom error messages used in several classes for different exceptions. */
public class ExceptionMessages {

  public static final String BOUNDARY_NOT_IN_DATA_EXTRACT = "The provided boundary parameter "
      + "does not lie completely within the underlying data-extract polygon.";
  public static final String BPOLYS_FORMAT = "The bpolys parameter must contain "
      + "double-parseable values in form of lon/lat coordinate pairs.";
  public static final String BOUNDARY_PARAM_GEOJSON_FORMAT = "The geometry of your given boundary "
      + "input could not be parsed for the creation of the response GeoJSON.";
  public static final String BOUNDARY_PARAM_FORMAT =
      "Error in processing the boundary parameter. Please "
          + "remember to follow the format, where you separate every coordinate with a comma, "
          + "each boundary object with a pipe-sign "
          + "and add optional custom ids to every first coordinate with a colon.";
  public static final String BPOLYS_PARAM_GEOMETRY =
      "The defined bpolys parameter contains some invalid geometry: ";
  public static final String BOUNDARY_IDS_FORMAT =
      "One or more boundary object(s) have a custom id "
          + "(or at least a colon), whereas other(s) don't. You can either set custom ids for all "
          + "your boundary objects, or for none.";
  public static final String BOUNDARY_PARAM_FORMAT_OR_COUNT =
      "Your provided boundary parameter (bboxes, "
          + "bcircles, or bpolys) does not fit its format, or you defined more than one boundary "
          + "parameter.";
  public static final String DATABASE_ACCESS = "Keytables not found or access to database failed";
  public static final String FILTER_PARAM = "The keys, values and types parameters must be empty, "
      + "when you set the filter parameter.";
  public static final String FILTER_SYNTAX = "Invalid filter syntax. Please look at the additional "
      + "info and examples about the filter parameter at https://docs.ohsome.org/ohsome-api.";
  public static final String GROUP_BY_KEY_PARAM =
      "You need to give one groupByKey parameter, if you want to use groupBy/tag.";
  public static final String GROUP_BY_KEYS_PARAM =
      "You need to give one groupByKeys parameter, if you want to use groupBy/key.";
  public static final String KEYS_VALUES_RATIO_INVALID = "There cannot be more input values in the "
      + "values|values2 than in the keys|keys2 parameter, as values_n must fit to keys_n.";
  public static final String NO_DEFINED_PARAMS = "The query did not specify any parameter. "
          + "Please remember: ";
  public static final String NO_BOUNDARY =
      "You need to define one of the boundary parameters (bboxes, bcircles, bpolys).";
  public static final String PAYLOAD_TOO_LARGE =
      "The given query is too large in respect to the given timeout. Please use a smaller region "
          + "and/or coarser time period.";
  public static final String PROPERTIES_PARAM =
      "The properties parameter of this resource can only contain the values 'tags' and/or "
          + "'metadata' and/or 'unclipped'.";
  public static final String PROPERTIES_PARAM_CONTR =
      "The properties parameter of this resource can only contain the values 'tags' and/or "
          + "'metadata' and/or 'contributionTypes' and/or 'unclipped'.";
  public static final String SHOWMETADATA_PARAM = "The showMetadata parameter can only contain the "
      + "values 'true', 'yes', 'false', or 'no'.";
  public static final String TIME_FORMAT = "The provided time parameter is not ISO-8601 conform.";
  public static final String TIME_FORMAT_CONTRS_EXTRACTION_AND_FULL_HISTORY =
      "Wrong time parameter. You need to give exactly two ISO-8601 conform timestamps.";
  public static final String TIME_FORMAT_CONTRIBUTION =
      "You need to give at least two timestamps or a time interval for this resource.";
  public static final String TIMEOUT = "The given timeout is too long. It has to be shorter than ";
  public static final String TIMEOUT_FORMAT = "The given timeout does not fit to its format. Please"
      + " give one value in seconds and use a point as the decimal delimiter, if needed.";
  public static final String TYPES_PARAM = "Parameter 'types' (and 'types2') can only have 'node' "
      + "and/or 'way' and/or 'relation' OR 'point' and/or 'line' and/or 'polygon' and/or 'other'";

  private ExceptionMessages() {
    throw new IllegalStateException("Utility class");
  }
}
