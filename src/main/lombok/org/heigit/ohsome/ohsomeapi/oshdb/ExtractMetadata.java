package org.heigit.ohsome.ohsomeapi.oshdb;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Holds the metadata that is derived from the data-extract. */
@Getter
@Setter
@Component
public class ExtractMetadata {

  private String fromTstamp = null;
  private String toTstamp = null;
  private String attributionShort = null;
  private String attributionUrl = null;
  private Geometry dataPoly = null;
  private JsonNode dataPolyJson = null;
  private int replicationSequenceNumber;
  @Autowired
  private String apiVersion;
}
