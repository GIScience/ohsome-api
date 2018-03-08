package org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataExtractionResponse;

import java.util.ArrayList;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.metadata.Metadata;

/**
 * First level object in the POST JSON response. This was implemented before the detailed concept of
 * the REST API was defined in Confluence. It follows this structure:
 * https://confluence.gistools.geog.uni-heidelberg.de/pages/viewpage.action?pageId=11894804
 *
 */
public class DataExtractionResponseContent {

  private final String status;
  private final Metadata metadata;
  private final ArrayList<OSHDBResult> results;

  /**
   * @param status
   * @param metadata
   * @param results
   */
  public DataExtractionResponseContent(String status, Metadata metadata,
      ArrayList<OSHDBResult> results) {
    this.status = status;
    this.metadata = metadata;
    this.results = results;
  }

  public String getStatus() {
    return status;
  }

  public Metadata getMetaData() {
    return metadata;
  }

  public ArrayList<OSHDBResult> getResults() {
    return results;
  }
}
