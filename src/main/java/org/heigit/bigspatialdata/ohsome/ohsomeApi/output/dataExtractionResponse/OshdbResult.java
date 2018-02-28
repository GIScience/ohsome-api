package org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataExtractionResponse;

import java.util.ArrayList;

/**
 * Second level object in the POST JSON response. This was implemented before the detailed concept
 * of the REST API was defined in Confluence.
 *
 */
public class OshdbResult {

  private final String timestamp; // is the actual timestamp
  private final ArrayList<OshdbObjects> osmObjects;

  /**
   * @param timestamp
   * @param osmObjects
   */
  public OshdbResult(String timestamp, ArrayList<OshdbObjects> osmObjects) {
    this.timestamp = timestamp;
    this.osmObjects = osmObjects;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public ArrayList<OshdbObjects> getOsmObjects() {
    return osmObjects;
  }

}
