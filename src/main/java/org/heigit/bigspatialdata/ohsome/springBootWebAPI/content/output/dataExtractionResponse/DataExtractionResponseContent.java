package org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.dataExtractionResponse;

import java.util.ArrayList;

import org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.MetaData;

/**
 * First level object in the POST JSON response.
 * This was implemented before the detailed concept of the REST API was defined in Confluence.
 * It follows this structure: https://confluence.gistools.geog.uni-heidelberg.de/pages/viewpage.action?pageId=11894804
 *
 */
public class DataExtractionResponseContent {
	
	private final String status;
	private final MetaData metaData;
	private final ArrayList<OshdbResult> results;
	
    /**
     * @param status
     * @param metaData
     * @param results
     */
    public DataExtractionResponseContent(String status, MetaData metaData, ArrayList<OshdbResult>  results) {
        this.status = status;
        this.metaData = metaData;
        this.results = results;
    }
    
    public String getStatus() {
        return status;
    }

    public MetaData getMetaData() {
        return metaData;
    }
    
    public ArrayList<OshdbResult>  getResults() {
        return results;
    }
}
