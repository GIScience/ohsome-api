package org.heigit.ohsome.ohsomeapi.output;

/**
 * Interface for all Response classes.
 * <ul>
 * <li>{@link org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse
 * DefaultAggregationResponse}</li>
 * <li>{@link org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResponse
 * GroupByResponse}</li>
 * <li>{@link org.heigit.ohsome.ohsomeapi.output.ratio.RatioGroupByBoundaryResponse
 * RatioGroupByBoundaryResponse}</li>
 * <li>{@link org.heigit.ohsome.ohsomeapi.output.ratio.RatioResponse
 * RatioResponse}</li>
 * <li>{@link org.heigit.ohsome.ohsomeapi.output.ExtractionResponse
 * DataResponse}</li>
 * </ul>
 */
public interface Response {

  Attribution getAttribution();

  String getApiVersion();

  Metadata getMetadata();
}
