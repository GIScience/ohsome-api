package org.heigit.bigspatialdata.ohsome.ohsomeapi.controller;

/** Holds the test properties, which are provided as system properties. */
public class TestProperties {

  public static final String DB_FILE_PATH_PROPERTY = System.getProperty("dbFilePathProperty");
  public static final String PORT1 = System.getProperty("port1");
  public static final String PORT2 = System.getProperty("port2");
  public static final String JUNIT = System.getProperty("junit");
  public static final String INTEGRATION = System.getProperty("integration");
  public static final String SERVER = "http://localhost:";
}
