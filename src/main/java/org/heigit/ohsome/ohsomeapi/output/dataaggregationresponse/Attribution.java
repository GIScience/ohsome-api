package org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

/**
 * Represents the attribution information contained in every json response. It holds a link to the
 * copyright and license information and a short copyright text.
 */
@Getter
public class Attribution {

  @ApiModelProperty(notes = "URL to the copyright and license info", required = true)
  private String url;
  @ApiModelProperty(notes = "Copyright info about the used data", required = true)
  private String text;

  public Attribution(String url, String text) {
    this.url = url;
    this.text = text;
  }
}
