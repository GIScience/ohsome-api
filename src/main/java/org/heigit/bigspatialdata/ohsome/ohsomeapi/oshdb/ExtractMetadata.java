package org.heigit.bigspatialdata.ohsome.ohsomeapi.oshdb;

import com.fasterxml.jackson.databind.JsonNode;
import org.locationtech.jts.geom.Geometry;

/** Holds the metadata that is derived from the data-extract. */
public class ExtractMetadata {

  public static String fromTstamp = null;
  public static String toTstamp = null;
  public static String attributionShort = null;
  public static String attributionUrl = null;
  public static Geometry dataPoly = null;
  public static JsonNode dataPolyJson = null;
  public static int replicationSequenceNumber;

  private ExtractMetadata() {
    throw new IllegalStateException("Utility class");
  }
}
