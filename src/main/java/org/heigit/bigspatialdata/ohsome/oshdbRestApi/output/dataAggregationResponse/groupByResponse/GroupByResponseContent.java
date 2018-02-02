package org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.groupByResponse;

import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.Metadata;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents the JSON response object for the /groupBy data aggregation requests. It contains an
 * optional
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.Metadata
 * Metadata} or
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.groupByResponse.GroupByBoundaryMetadata
 * GroupByBoundaryMetadata} object, the requested
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.groupByResponse.GroupByResult
 * GroupByResult}, which is named after the used /groupBy resource (e.g. groupByBoundaryResult for
 * using /groupBy/boundary) and an identifier of the object plus the corresponding
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.Result
 * Result} objects.
 */
@JsonInclude(Include.NON_NULL)
public class GroupByResponseContent {

  private String license;
  private String copyright;
  private Metadata metadata;
  private GroupByBoundaryMetadata groupByBoundaryMetadata;
  private GroupByResult[] groupByBoundaryResult;
  private GroupByResult[] groupByTypeResult;
  private GroupByResult[] groupByKeyResult;
  private GroupByResult[] groupByTagResult;
  private GroupByResult[] groupByUserResult;

  public GroupByResponseContent(String license, String copyright, Metadata metadata,
      GroupByBoundaryMetadata groupByBoundaryMetadata, GroupByResult[] groupByBoundaryResult,
      GroupByResult[] groupByTypeResult, GroupByResult[] groupByKeyResult,
      GroupByResult[] groupByTagResult, GroupByResult[] groupByUserResult) {
    this.license = license;
    this.copyright = copyright;
    this.metadata = metadata;
    this.groupByBoundaryMetadata = groupByBoundaryMetadata;
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

  public GroupByBoundaryMetadata getGroupByBoundaryMetadata() {
    return groupByBoundaryMetadata;
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
