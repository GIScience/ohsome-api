package org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse;

/**
 * Interface for all Response classes.
 * <ul>
 * <li>{@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse
 * DefaultAggregationResponse}</li>
 * <li>{@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.elements.ShareResponse
 * ShareResponse}</li>
 * <li>{@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResponse
 * GroupByResponse}</li>
 * <li>{@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.RatioGroupByBoundaryResponse
 * RatioGroupByBoundaryResponse}</li>
 * <li>{@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.ShareGroupByBoundaryResponse
 * ShareGroupByBoundaryResponse}</li>
 * <li>{@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.RatioResponse
 * RatioResponse}</li>
 * </ul>
 */
public interface Response {

  public Attribution getAttribution();

  public String getApiVersion();

  public Metadata getMetadata();

}
