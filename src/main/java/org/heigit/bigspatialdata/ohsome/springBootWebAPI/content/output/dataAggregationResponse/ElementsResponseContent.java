package org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.dataAggregationResponse;

import org.heigit.bigspatialdata.ohsome.springBootWebAPI.content.output.MetaData;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents the whole JSON response object for the data aggregation requests. It contains the
 * requested data as well as information about the license, the copyright and additional meta data.
 *
 */
@JsonInclude(Include.NON_NULL) // needed to exclude NULL objects from the result
public class ElementsResponseContent {

  private String license;
  private String copyright;
  private MetaData metaData;
  private GroupByResult[] groupByResult;
  private Result[] result;

  public ElementsResponseContent(String license, String copyright, MetaData metaData,
      GroupByResult[] groupByResult, Result[] result) {
    this.license = license;
    this.copyright = copyright;
    this.metaData = metaData;
    this.groupByResult = groupByResult;
    this.result = result;
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

}
