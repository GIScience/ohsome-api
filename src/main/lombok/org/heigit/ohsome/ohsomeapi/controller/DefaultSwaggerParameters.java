package org.heigit.ohsome.ohsomeapi.controller;

/** Holds the default values for the parameters to run test-requests in Swagger. */
public class DefaultSwaggerParameters {

  public static final String BBOX = "8.67,49.39,8.71,49.42";
  public static final String HIGHWAY_KEY = "highway";
  public static final String BUILDING_KEY = "building";
  public static final String GENERIC_FILTER = "type:way and natural=*";
  public static final String HIGHWAY_FILTER = "type:way and highway=residential";
  public static final String HIGHWAY_FILTER2 = "type:way and highway=*";
  public static final String BUILDING_FILTER = "geometry:polygon and building=*";
  public static final String BUILDING_FILTER2 = "geometry:polygon and building=house";
  public static final String HOUSENUMBER_FILTER = "type:node and \"addr:housenumber\"=*";
  public static final String TIME = "2014-01-01/2017-01-01/P1Y";

  private DefaultSwaggerParameters() {
    throw new IllegalStateException("Utility class");
  }
}
