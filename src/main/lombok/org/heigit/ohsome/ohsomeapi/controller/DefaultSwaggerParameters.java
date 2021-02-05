package org.heigit.ohsome.ohsomeapi.controller;

/** Holds the default values for the parameters to run test-requests in Swagger. */
public class DefaultSwaggerParameters {

  public static final String BBOX = "8.625,49.3711,8.7334,49.4397";
  public static final String HIGHWAY_KEY = "highway";
  public static final String BUILDING_KEY = "building";
  public static final String TYPE_FILTER = "type:way";
  public static final String HIGHWAY_FILTER = "highway=residential";
  public static final String BUILDING_FILTER = "building=*";
  public static final String HOUSENUMBER_FILTER = "type:node and \"addr:housenumber\"=*";
  public static final String TIME = "2014-01-01/2017-01-01/P1Y";
  
  private DefaultSwaggerParameters() {
    throw new IllegalStateException("Utility class");
  }
}
