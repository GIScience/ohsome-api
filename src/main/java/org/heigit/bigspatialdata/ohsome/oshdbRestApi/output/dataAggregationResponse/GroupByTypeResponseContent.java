package org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse;

import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.metadata.Metadata;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result.GroupByResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents the whole JSON response object for the data aggregation response using the
 * /groupBy/type resource. It contains an optional
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.metadata.Metadata
 * Metadata}, the requested
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result.GroupByResult
 * GroupByResult} and an identifier of the object plus the corresponding
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.result.Result
 * Result} objects.
 */
@JsonInclude(Include.NON_NULL)
public class GroupByTypeResponseContent {

  private String license;
  private String copyright;
  private Metadata metadata;
  private GroupByResult[] groupByTypeResult;

  public GroupByTypeResponseContent(String license, String copyright, Metadata metadata,
      GroupByResult[] groupByTypeResult) {
    this.license = license;
    this.copyright = copyright;
    this.metadata = metadata;
    this.groupByTypeResult = groupByTypeResult;
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

}
