package org.heigit.ohsome.ohsomeapi.oshdb;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Component;

/** Holds the metadata that is derived from the data-extract. */
@Component
@Getter
@Setter
public class ExtractMetadata {

  private String fromTstamp = null;
  private String toTstamp = null;
  private String attributionShort = null;
  private String attributionUrl = null;
  private Geometry dataPoly = null;
  private JsonNode dataPolyJson = null;
  private int replicationSequenceNumber;
}
