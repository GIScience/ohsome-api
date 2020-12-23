package org.heigit.ohsome.ohsomeapi.controller;

/** Holds the test properties, which are provided as system properties. */
public class TestProperties {

  public static final String DB_FILE_PATH_PROPERTY = System.getProperty("dbFilePathProperty");
  public static final String PORT1 = System.getProperty("port_get");
  public static final String PORT2 = System.getProperty("port_post");
  public static final String PORT3 = System.getProperty("port_data");
  public static final String JUNIT = System.getProperty("junit");
  public static final String INTEGRATION = System.getProperty("integration");
  public static final String SERVER = "http://localhost:";
  public static final double DELTA_PERCENTAGE = 0.5 / 100; // 0.5 %
}
