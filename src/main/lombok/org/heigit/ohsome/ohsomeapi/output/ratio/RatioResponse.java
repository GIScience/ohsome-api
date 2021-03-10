package org.heigit.ohsome.ohsomeapi.output.ratio;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.heigit.ohsome.ohsomeapi.output.Attribution;
import org.heigit.ohsome.ohsomeapi.output.Metadata;
import org.heigit.ohsome.ohsomeapi.output.Response;

/**
 * Represents the whole JSON response object for the data aggregation response using the /ratio
 * resource. It contains the license and copyright, optional
 * {@link org.heigit.ohsome.ohsomeapi.output.Metadata Metadata}, as well as
 * the results section showing
 * {@link org.heigit.ohsome.ohsomeapi.output.ratio.RatioResult RatioResult}
 * objects.
 */
@Getter
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class RatioResponse implements Response {

  @ApiModelProperty(notes = "License and copyright info", required = true)
  private Attribution attribution;
  @ApiModelProperty(notes = "Version of this api", required = true)
  private String apiVersion;
  @ApiModelProperty(notes = "Metadata describing the output")
  private Metadata metadata;
  @ApiModelProperty(notes = "ElementsResult for /ratio requests", required = true)
  private RatioResult[] result;
}
