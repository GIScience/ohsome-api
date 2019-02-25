package org.heigit.bigspatialdata.ohsome.ohsomeapi.oshdb;

import org.locationtech.jts.geom.Geometry;
import com.fasterxml.jackson.databind.JsonNode;

/** Holds the metadata that is derived from the data-extract. */
public class ExtractMetadata {

  public static String fromTstamp = null;
  public static String toTstamp = null;
  public static String attributionShort = null;
  public static String attributionUrl = null;
  public static Geometry dataPoly = null;
  public static JsonNode dataPolyJson = null;

  private ExtractMetadata() {
    throw new IllegalStateException("Utility class");
  }
}
