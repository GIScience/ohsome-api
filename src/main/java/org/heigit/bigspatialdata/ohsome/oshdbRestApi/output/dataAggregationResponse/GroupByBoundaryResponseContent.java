package org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse;

import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.metadata.GroupByBoundaryMetadata;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result.GroupByResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents the whole JSON response object for the data aggregation response using the
 * /groupBy/boundary resource. It contains an optional
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.metadata.GroupByBoundaryMetadata
 * GroupByBoundaryMetadata} object, the requested
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result.GroupByResult
 * GroupByResult}, which is named after the used /groupBy resource (e.g. groupByBoundaryResult for
 * using /groupBy/boundary) and an identifier of the object plus the corresponding
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result.Result
 * Result} objects.
 */
@JsonInclude(Include.NON_NULL)
public class GroupByBoundaryResponseContent {

  private String license;
  private String copyright;
  private GroupByBoundaryMetadata groupByBoundaryMetadata;
  private GroupByResult[] groupByBoundaryResult;

  public GroupByBoundaryResponseContent(String license, String copyright,
      GroupByBoundaryMetadata groupByBoundaryMetadata, GroupByResult[] groupByBoundaryResult) {
    this.license = license;
    this.copyright = copyright;
    this.groupByBoundaryMetadata = groupByBoundaryMetadata;
    this.groupByBoundaryResult = groupByBoundaryResult;
  }

  public String getLicense() {
    return license;
  }

  public String getCopyright() {
    return copyright;
  }

  public GroupByBoundaryMetadata getGroupByBoundaryMetadata() {
    return groupByBoundaryMetadata;
  }

  public GroupByResult[] getGroupByBoundaryResult() {
    return groupByBoundaryResult;
  }

}
