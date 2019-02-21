package org.heigit.bigspatialdata.ohsome.ohsomeapi.controller;

/** Holds the descriptions for some parameters. */
public class ParameterDescriptions {

  public static final String BBOXES_DESCR = "WGS84 coordinates in the following format: "
      + "id1:lon1,lat1,lon2,lat2|id2:lon1,lat1,lon2,lat2|... OR lon1,lat1,lon2,lat2|"
      + "lon1,lat1,lon2,lat2|...; default: whole dataset (if all three boundary parameters "
      + "are empty)";
  public static final String BCIRCLES_DESCR =
      "WGS84 coordinates + radius in meter in the following format: "
          + "id1:lon,lat,r|id2:lon,lat,r|... OR lon,lat,r|lon,lat,r|...; "
          + "default: whole dataset (if all three boundary parameters are empty)";
  public static final String BPOLYS_DESCR =
      "WGS84 coordinates given as a list of coordinate pairs (as for bboxes) or GeoJSON "
          + "FeatureCollection. The first point has to be the same as the last point and "
          + "MultiPolygons are only supported in GeoJSON; default: whole dataset "
          + "(if all three boundary parameters are empty)";
  public static final String TYPES_DESCR =
      "OSM type(s) 'node' and/or 'way' and/or 'relation'; default: all three types";
  public static final String KEYS_DESCR = "OSM key(s) e.g.: 'highway', 'building'; default: no key";
  public static final String VALUES_DESCR =
      "OSM value(s) e.g.: 'primary', 'residential'; default: no value";
  public static final String USERIDS_DESCR = "OSM userids; default: no userid";
  public static final String TIME_DESCR =
      "ISO-8601 conform timestring(s); default: latest timestamp within dataset";
  public static final String FORMAT_DESCR =
      "Output format geojson (for /groupBy/boundary resources only), csv, or json; default: json";
  public static final String PROPERTIES_DESCR =
      "List of possible property-groups added to each OSM-element, e.g. 'tags', or 'metadata'; "
          + "default: 'tags'";
  public static final String SHOW_METADATA_DESCR =
      "Boolean operator 'true' or 'false'; default: 'false'";
  public static final String TIMEOUT_DESCR = "Custom timeout in seconds;";

  private ParameterDescriptions() {
    throw new IllegalStateException("Utility class");
  }
}
