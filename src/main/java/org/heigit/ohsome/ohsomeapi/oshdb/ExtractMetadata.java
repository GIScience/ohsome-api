package org.heigit.ohsome.ohsomeapi.oshdb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBJdbc;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;
import org.heigit.ohsome.ohsomeapi.exception.DatabaseAccessException;
import org.heigit.ohsome.ohsomeapi.inputprocessing.GeometryBuilder;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/** Holds the metadata that is derived from the data-extract. */
public class ExtractMetadata {

  private final String fromTstamp;
  private final String toTstamp;
  private final String attributionShort;
  private final String attributionUrl;
  private final Geometry dataPoly;
  private final JsonNode dataPolyJson;
  private final int replicationSequenceNumber;

  /**
   * Extracts some metadata from the OSHDB keytables or db and adds it to the corresponding objects.
   * 
   * @throws DatabaseAccessException if the keytables are missing
   * @throws RuntimeException if the timerange metadata cannot be retrieved from the db
   * @throws IOException thrown by
   *         {@link com.fasterxml.jackson.databind.ObjectMapper#readTree(String) readTree}
   */
  public ExtractMetadata(OSHDBJdbc keytables,
      TagTranslator tagTranslator) throws IOException {
    if (keytables.metadata("extract.region") != null) {
      String dataPolyString = keytables.metadata("extract.region");
      ObjectMapper mapper = new ObjectMapper();
      dataPolyJson = mapper.readTree(dataPolyString);
      GeometryBuilder geomBuilder = new GeometryBuilder();
      dataPoly = geomBuilder.createGeometryFromMetadataGeoJson(dataPolyString);
    } else {
      dataPolyJson = null;
      dataPoly = null;
    }
    if (keytables.metadata("extract.timerange") != null) {
      String[] timeranges = keytables.metadata("extract.timerange").split(",");
      fromTstamp = timeranges[0];
      toTstamp = timeranges[1];
    } else {
      throw new RuntimeException("The timerange metadata could not be retrieved from the db.");
    }
    if (keytables.metadata("attribution.short") != null) {
      attributionShort = keytables.metadata("attribution.short");
    } else {
      attributionShort = "© OpenStreetMap contributors";
    }
    if (keytables.metadata("attribution.url") != null) {
      attributionUrl = keytables.metadata("attribution.url");
    } else {
      attributionUrl = "https://ohsome.org/copyrights";
    }
    if (keytables.metadata("header.osmosis_replication_sequence_number") != null) {
      replicationSequenceNumber =
          Integer.parseInt(keytables.metadata("header.osmosis_replication_sequence_number"));
    } else {
      replicationSequenceNumber = -1;
    }
  }

  public String getFromTstamp() {
    return fromTstamp;
  }

  public String getToTstamp() {
    return toTstamp;
  }

  public String getAttributionShort() {
    return attributionShort;
  }

  public String getAttributionUrl() {
    return attributionUrl;
  }
  
  public Geometry getDataPoly() {
    return dataPoly;
  }

  public JsonNode getDataPolyJson() {
    return dataPolyJson;
  }

  public int getReplicationSequenceNumber() {
    return replicationSequenceNumber;
  }
}
