package org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataExtractionResponse;

import java.util.ArrayList;

/**
 * Second level object in the POST JSON response. This was implemented before the detailed concept
 * of the REST API was defined in Confluence.
 *
 */
public class OSHDBResult {

  private final String timestamp; // is the actual timestamp
  private final ArrayList<OSHDBObjects> osmObjects;

  /**
   * @param timestamp
   * @param osmObjects
   */
  public OSHDBResult(String timestamp, ArrayList<OSHDBObjects> osmObjects) {
    this.timestamp = timestamp;
    this.osmObjects = osmObjects;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public ArrayList<OSHDBObjects> getOsmObjects() {
    return osmObjects;
  }

}
