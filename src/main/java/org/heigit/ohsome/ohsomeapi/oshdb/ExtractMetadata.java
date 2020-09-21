package org.heigit.ohsome.ohsomeapi.oshdb;

import com.fasterxml.jackson.databind.JsonNode;
import org.locationtech.jts.geom.Geometry;

/** Holds the metadata that is derived from the data-extract. */
public class ExtractMetadata {

  private static String fromTstamp = null;
  private static String toTstamp = null;
  private static String attributionShort = null;
  private static String attributionUrl = null;
  private static Geometry dataPoly = null;
  private static JsonNode dataPolyJson = null;
  private static int replicationSequenceNumber;

  private ExtractMetadata() {
    throw new IllegalStateException("Utility class");
  }

  public static String getFromTstamp() {
    return fromTstamp;
  }

  public static void setFromTstamp(String fromTstamp) {
    ExtractMetadata.fromTstamp = fromTstamp;
  }

  public static String getToTstamp() {
    return toTstamp;
  }

  public static void setToTstamp(String toTstamp) {
    ExtractMetadata.toTstamp = toTstamp;
  }

  public static String getAttributionShort() {
    return attributionShort;
  }

  public static void setAttributionShort(String attributionShort) {
    ExtractMetadata.attributionShort = attributionShort;
  }

  public static String getAttributionUrl() {
    return attributionUrl;
  }

  public static void setAttributionUrl(String attributionUrl) {
    ExtractMetadata.attributionUrl = attributionUrl;
  }

  public static Geometry getDataPoly() {
    return dataPoly;
  }

  public static void setDataPoly(Geometry dataPoly) {
    ExtractMetadata.dataPoly = dataPoly;
  }

  public static JsonNode getDataPolyJson() {
    return dataPolyJson;
  }

  public static void setDataPolyJson(JsonNode dataPolyJson) {
    ExtractMetadata.dataPolyJson = dataPolyJson;
  }

  public static int getReplicationSequenceNumber() {
    return replicationSequenceNumber;
  }

  public static void setReplicationSequenceNumber(int replicationSequenceNumber) {
    ExtractMetadata.replicationSequenceNumber = replicationSequenceNumber;
  }
}
