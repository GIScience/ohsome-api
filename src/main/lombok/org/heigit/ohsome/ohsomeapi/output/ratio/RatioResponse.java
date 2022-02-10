package org.heigit.ohsome.ohsomeapi.output.ratio;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.ohsome.ohsomeapi.output.Attribution;
import org.heigit.ohsome.ohsomeapi.output.Metadata;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Represents the whole JSON response object for the data aggregation response using the /ratio
 * resource. It contains the license and copyright, optional {@link
 * org.heigit.ohsome.ohsomeapi.output.Metadata Metadata}, as well as the results section showing
 * {@link org.heigit.ohsome.ohsomeapi.output.ratio.RatioResult RatioResult} objects.
 */
@Getter
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class RatioResponse extends Response {

  @Autowired
  private ExtractMetadata extractMetadata;
  @ApiModelProperty(notes = "License and copyright info", required = true)
  @Autowired
  private Attribution attribution;
  @ApiModelProperty(notes = "Version of this api", required = true)
  private String apiVersion = extractMetadata.getApiVersion();
  @ApiModelProperty(notes = "Metadata describing the output")
  private Metadata metadata;
  @ApiModelProperty(notes = "ElementsResult for /ratio requests", required = true)
  private List<RatioResult> result;

  public RatioResponse(List<RatioResult> result, Operation operation) {
    this.result = result;
    this.metadata = this.generateMetadata(operation.getMetadataDescription(), operation);
  }
}
