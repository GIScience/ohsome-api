package org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.groupByResponse;

import org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.Metadata;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents the outer JSON response object for the /groupBy data aggregation requests. It contains
 * the requested
 * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.groupByResponse.GroupByResult
 * GroupByResult} object, which is named after the used /groupBy resource (e.g.
 * groupByBoundaryResult for using /groupBy/boundary) and an identifier of the object plus
 * the corresponding
 * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.Result
 * Result} objects.
 */
@JsonInclude(Include.NON_NULL) // needed to exclude NULL objects from the result
public class GroupByResponseContent {

  private String license;
  private String copyright;
  private Metadata metadata;
  private GroupByResult[] groupByBoundaryResult;
  private GroupByResult[] groupByTypeResult;
  private GroupByResult[] groupByKeyResult;
  private GroupByResult[] groupByTagResult;
  private GroupByResult[] groupByUserResult;

  public GroupByResponseContent(String license, String copyright, Metadata metadata,
      GroupByResult[] groupByBoundaryResult, GroupByResult[] groupByTypeResult,
      GroupByResult[] groupByKeyResult, GroupByResult[] groupByTagResult,
      GroupByResult[] groupByUserResult) {
    this.license = license;
    this.copyright = copyright;
    this.metadata = metadata;
    this.groupByBoundaryResult = groupByBoundaryResult;
    this.groupByTypeResult = groupByTypeResult;
    this.groupByKeyResult = groupByKeyResult;
    this.groupByTagResult = groupByTagResult;
    this.groupByUserResult = groupByUserResult;
  }

  public String getLicense() {
    return license;
  }

  public String getCopyright() {
    return copyright;
  }

  public Metadata getMetadata() {
    return metadata;
  }

  public GroupByResult[] getGroupByBoundaryResult() {
    return groupByBoundaryResult;
  }

  public GroupByResult[] getGroupByTypeResult() {
    return groupByTypeResult;
  }

  public GroupByResult[] getGroupByKeyResult() {
    return groupByKeyResult;
  }

  public GroupByResult[] getGroupByTagResult() {
    return groupByTagResult;
  }

  public GroupByResult[] getGroupByUserResult() {
    return groupByUserResult;
  }

}
