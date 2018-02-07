package org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse;

import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.groupByResponse.GroupByResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the outer JSON response object for the data aggregation requests that do not use the
 * /groupBy resource. It contains the license and copyright, optional
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.Metadata
 * Metadata} as well as the results section showing either
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.Result
 * Result},
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.RatioResult
 * RatioResult}, or
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.ShareResult
 * ShareResult} objects.
 */
@JsonInclude(Include.NON_NULL)
public class ElementsResponseContent {

  @ApiModelProperty(notes = "The license of the used data.", required = true, position = 0)
  private String license;
  @ApiModelProperty(notes = "The copyright of the used data.", required = true, position = 1)
  private String copyright;
  @ApiModelProperty(notes = "The metadata describing the output.", position = 2)
  private Metadata metadata;
  @ApiModelProperty(notes = "The result for /count|length|area|perimeter/groupBy requests.")
  private GroupByResult[] groupByResult;
  @ApiModelProperty(notes = "The result for /count|length|area|perimeter or /density requests.")
  private Result[] result;
  @ApiModelProperty(notes = "The result for /count/ratio requests.")
  private RatioResult[] ratioResult;
  @ApiModelProperty(notes = "The result for /count|length|area|perimeter/share requests.")
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
