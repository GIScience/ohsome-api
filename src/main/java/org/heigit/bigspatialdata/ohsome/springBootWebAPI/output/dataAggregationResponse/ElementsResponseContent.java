package org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse;

import org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.MetaData;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents the outer JSON response object for the data aggregation requests. It contains the
 * requested
 * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.Result
 * Result} (in a groupBy response the
 * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.GroupByResult
 * GroupByResult}, in a ratio response the
 * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.RatioResult
 * RatioResult}, or in a share response the
 * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ShareResult
 * ShareResult}), as well as additional
 * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.MetaData MetaData}.
 *
 */
@JsonInclude(Include.NON_NULL) // needed to exclude NULL objects from the result
public class ElementsResponseContent {

  private String license;
  private String copyright;
  private MetaData metaData;
  private GroupByResult[] groupByResult;
  private Result[] result;
  private RatioResult[] ratioResult;
  private ShareResult[] shareResult;

  public ElementsResponseContent(String license, String copyright, MetaData metaData,
      GroupByResult[] groupByResult, Result[] result, RatioResult[] ratioResult,
      ShareResult[] shareResult) {
    this.license = license;
    this.copyright = copyright;
    this.metaData = metaData;
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

  public MetaData getMetaData() {
    return metaData;
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
