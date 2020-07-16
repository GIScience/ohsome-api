package org.heigit.ohsome.ohsomeapi.oshdb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBDatabase;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBJdbc;
import org.heigit.ohsome.ohsomeapi.inputprocessing.GeometryBuilder;
import org.heigit.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.locationtech.jts.geom.Geometry;


/** Holds the metadata that is derived from the data-extract. */

public class ExtractMetadata {

  private String fromTstamp = null;
  private String toTstamp = null;
  private String attributionShort = null;
  private String attributionUrl = null;
  private Geometry dataPoly = null;
  private JsonNode dataPolyJson = null;
  private int replicationSequenceNumber;
  
  public static ExtractMetadata extractOSHDBMetadata() throws IOException {
    ExtractMetadata metadata = new ExtractMetadata();
    OSHDBDatabase db = DbConnData.keytables;
    if (db.metadata("extract.region") != null) {
      String dataPolyString = db.metadata("extract.region");
      ObjectMapper mapper = new ObjectMapper();
      metadata.dataPolyJson = mapper.readTree(dataPolyString);
      GeometryBuilder geomBuilder = new GeometryBuilder();
      geomBuilder.createGeometryFromMetadataGeoJson(dataPolyString);
      metadata.dataPoly = ProcessingData.getDataPolyGeom();
    }
    if (db.metadata("extract.timerange") != null) {
      String[] timeranges = db.metadata("extract.timerange").split(",");
      metadata.fromTstamp = timeranges[0];
      metadata.toTstamp = timeranges[1];
    } else {
      throw new RuntimeException("The timerange metadata could not be retrieved from the db.");
    }
    if (db.metadata("attribution.short") != null) {
      metadata.attributionShort = db.metadata("attribution.short");
    } else {
      metadata.attributionShort = "© OpenStreetMap contributors";
    }
    if (db.metadata("attribution.url") != null) {
      metadata.attributionUrl = db.metadata("attribution.url");
    } else {
      metadata.attributionUrl = "https://ohsome.org/copyrights";
    }
    if (db.metadata("header.osmosis_replication_sequence_number") != null) {
      metadata.replicationSequenceNumber =
          Integer.parseInt(db.metadata("header.osmosis_replication_sequence_number"));
    }
    return metadata;
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
