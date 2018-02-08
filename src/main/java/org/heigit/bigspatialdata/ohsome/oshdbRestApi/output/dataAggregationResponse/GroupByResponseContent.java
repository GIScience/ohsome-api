package org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse;

import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.metadata.Metadata;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result.GroupByResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents the whole JSON response object for the data aggregation response using the /groupBy
 * resource (all but the /groupBy/boundary resource). It contains an optional
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.metadata.Metadata
 * Metadata} or
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.metadata.GroupByBoundaryMetadata
 * GroupByBoundaryMetadata} object, the requested
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result.GroupByResult
 * GroupByResult}, which is named after the used /groupBy resource (e.g. groupByBoundaryResult for
 * using /groupBy/boundary) and an identifier of the object plus the corresponding
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result.Result
 * Result} objects.
 */
@JsonInclude(Include.NON_NULL)
public class GroupByResponseContent {

  private String license;
  private String copyright;
  private Metadata metadata;
  private GroupByResult[] groupByTypeResult;
  private GroupByResult[] groupByKeyResult;
  private GroupByResult[] groupByTagResult;
  private GroupByResult[] groupByUserResult;

  public GroupByResponseContent(String license, String copyright, Metadata metadata,
      GroupByResult[] groupByTypeResult, GroupByResult[] groupByKeyResult,
      GroupByResult[] groupByTagResult, GroupByResult[] groupByUserResult) {
    this.license = license;
    this.copyright = copyright;
    this.metadata = metadata;
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
