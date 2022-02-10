package org.heigit.ohsome.ohsomeapi.output;

import io.swagger.annotations.ApiModelProperty;
import javax.annotation.PostConstruct;
import lombok.Getter;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Represents the attribution information contained in every json response. It holds a link to the
 * copyright and license information and a short copyright text.
 */
@Getter
@Component
public class Attribution {

  @Autowired
  private ExtractMetadata extractMetadata;
  @ApiModelProperty(notes = "URL to the copyright and license info", required = true)
  private String url;
  @ApiModelProperty(notes = "Copyright info about the used data", required = true)
  private String text;

  @PostConstruct
  public void init(){
    url = extractMetadata.getAttributionUrl();
    text = extractMetadata.getAttributionShort();
  }
}
