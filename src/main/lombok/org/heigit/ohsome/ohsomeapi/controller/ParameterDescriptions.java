package org.heigit.ohsome.ohsomeapi.controller;

/** Holds the descriptions for some parameters. */
public class ParameterDescriptions {

  public static final String BBOXES = "WGS84 coordinates in the following format: "
      + "id1:lon1,lat1,lon2,lat2|id2:lon1,lat1,lon2,lat2|... OR lon1,lat1,lon2,lat2|"
      + "lon1,lat1,lon2,lat2|...; no default value (one boundary parameter must be defined)";
  public static final String BCIRCLES =
      "WGS84 coordinates + radius in meter in the following format: "
          + "id1:lon,lat,r|id2:lon,lat,r|... OR lon,lat,r|lon,lat,r|...; "
          + "no default value (one boundary parameter must be defined)";
  public static final String BPOLYS =
      "WGS84 coordinates given as a list of coordinate pairs (as for bboxes) or GeoJSON "
          + "FeatureCollection. The first point has to be the same as the last point and "
          + "MultiPolygons are only supported in GeoJSON; no default value "
          + "(one boundary parameter must be defined)";
  public static final String TYPES =
      "OSM type(s) 'node' and/or 'way' and/or 'relation' OR simple feature type(s) 'point' and/or "
          + "'line' and/or 'polygon'; default: all three OSM types";
  public static final String KEYS = "OSM key(s) e.g.: 'highway', 'building'; default: no key";
  public static final String GROUP_BY_KEY = "OSM key e.g.: 'highway', 'building'; no default "
      + "value (one groupByKey parameter must be defined)";
  public static final String VALUES =
      "OSM value(s) e.g.: 'primary', 'residential'; default: no value";
  public static final String TIME =
      "ISO-8601 conform timestring(s); default: latest timestamp within dataset";
  public static final String TIME_DATA_EXTRACTION =
      "ISO-8601 conform timestring(s) defining timestamps (/elements), "
          + "or intervals (/elementsFullHistory); no default value";
  public static final String FORMAT =
      "Output format geojson (for /groupBy/boundary resources only), csv, or json; default: json";
  public static final String PROPERTIES =
      "List of possible property-groups added to each OSM-element: 'tags' and/or 'metadata' "
          + "and/or 'contributionTypes' (only for the /contributions/{geometryType} endpoints); "
          + "default: no property";
  public static final String SHOW_METADATA = "Boolean operator 'true' or 'false'; default: 'false'";
  public static final String TIMEOUT = "Custom timeout in seconds; no default value";
  public static final String FILTER = "Combines several attributive filters, e.g. OSM type, "
      + "the geometry (simple feature) type, as well as the OSM tag; no default value";
  public static final String DEPRECATED_USE_FILTER = "This parameter has been deprecated since "
      + "v1.0. We encourage you to use the new parameter 'filter' instead.";
  public static final String DEPRECATED_USE_FILTER2 = "This parameter has been deprecated since "
      + "v1.0. We encourage you to use the new parameter 'filter2' instead.";
  public static final String CLIP_GEOMETRY = "Boolean operator to specify whether the returned "
      + "geometries of the features should be clipped to the query's spatial boundary (‘true’), "
      + "or not (‘false’); default: ‘true’";
  public static final String CONTRIBUTION_TYPE = "Filter contributions by contribution type: "
      + "'creation', 'deletion', 'tagChange', 'geometryChange' or 'otherChanges'; no default value";

  private ParameterDescriptions() {
    throw new IllegalStateException("Utility class");
  }
}
