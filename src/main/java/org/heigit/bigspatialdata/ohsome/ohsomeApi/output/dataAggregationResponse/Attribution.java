package org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse;

import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the attribution information contained in every json response. It holds a link to the
 * copyright and license information and a short copyright text.
 */
public class Attribution {

  @ApiModelProperty(notes = "URL to the copyright and license info", required = true, position = 0)
  private String url;
  @ApiModelProperty(notes = "Copyright info about the used data", required = true, position = 1)
  private String text;

  public Attribution(String url, String text) {
    this.url = url;
    this.text = text;
  }

  public String getUrl() {
    return url;
  }

  public String getText() {
    return text;
  }

}
