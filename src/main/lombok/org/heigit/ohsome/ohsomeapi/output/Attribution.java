package org.heigit.ohsome.ohsomeapi.output;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Represents the attribution information contained in every json response. It holds a link to the
 * copyright and license information and a short copyright text.
 */
@Getter
//@AllArgsConstructor
@Component
public class Attribution {

  @Autowired
  ExtractMetadata extractMetadata;

  @ApiModelProperty(notes = "URL to the copyright and license info", required = true)
  private final String url = extractMetadata.getAttributionUrl();
  @ApiModelProperty(notes = "Copyright info about the used data", required = true)
  private final String text = extractMetadata.getAttributionShort();

//  @Autowired
//  public Attribution(String url, String text){
//    this.url = url;
//    this.text = text;
//  }
}
