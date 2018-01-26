package org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse;

import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.Metadata;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.groupByResponse.GroupByResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents the outer JSON response object for the data aggregation requests that do not use the
 * /groupBy resource. It contains the license and copyright, optional
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.Metadata Metadata} as well as the
 * results section showing either
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.Result
 * Result},
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.RatioResult
 * RatioResult}, or
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.ShareResult
 * ShareResult} objects.
 *
 */
@JsonInclude(Include.NON_NULL) // needed to exclude NULL objects from the result
public class ElementsResponseContent {

  private String license;
  private String copyright;
  private Metadata metadata;
  private GroupByResult[] groupByResult;
  private Result[] result;
  private RatioResult[] ratioResult;
  private ShareResult[] shareResult;

  public ElementsResponseContent(String license, String copyright, Metadata metadata,
      GroupByResult[] groupByResult, Result[] result, RatioResult[] ratioResult,
      ShareResult[] shareResult) {
    this.license = license;
    this.copyright = copyright;
    this.metadata = metadata;
    this.groupByResult = groupByResult;
    this.result = result;
    this.ratioResult = ratioResult;
    this.shareResult = shareResult;
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

  public GroupByResult[] getGroupByResult() {
    return groupByResult;
  }

  public Result[] getResult() {
    return result;
  }

  public RatioResult[] getRatioResult() {
    return ratioResult;
  }

  public ShareResult[] getShareResult() {
    return shareResult;
  }

}
