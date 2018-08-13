package org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse;

/**
 * Interface for most Response classes.
 * 
 * @see {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse
 *      DefaultAggregationResponse}
 * @see {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.elements.ShareResponse
 *      ShareResponse}
 * @see {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResponse
 *      GroupByResponse}
 * @see {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.RatioGroupByBoundaryResponse
 *      RatioGroupByBoundaryResponse}
 * @see {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.ShareGroupByBoundaryResponse
 *      ShareGroupByBoundaryResponse}
 * @see {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.RatioResponse
 *      RatioResponse}
 * 
 */
public interface Response {

  public Attribution getAttribution();

  public String getApiVersion();

  public Metadata getMetadata();

}
