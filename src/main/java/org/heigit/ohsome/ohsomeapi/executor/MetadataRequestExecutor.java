package org.heigit.ohsome.ohsomeapi.executor;

import javax.servlet.http.HttpServletRequest;
import org.heigit.ohsome.ohsomeapi.Application;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Attribution;
import org.heigit.ohsome.ohsomeapi.output.metadataresponse.ExtractRegion;
import org.heigit.ohsome.ohsomeapi.output.metadataresponse.MetadataResponse;
import org.heigit.ohsome.ohsomeapi.output.metadataresponse.TemporalExtent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Includes the execute method for requests mapped to /metadata. */
@Component
public class MetadataRequestExecutor {
  
  private final ExtractMetadata extractMetadata;
  
  
  @Autowired
  public MetadataRequestExecutor(ExtractMetadata extractMetadata) {
    this.extractMetadata = extractMetadata;
  }



  /**
   * Returns the metadata of the underlying extract-file.
   * 
   * @return {@link org.heigit.ohsome.ohsomeapi.output.metadataresponse.MetadataResponse
   *         MetadataResponse}
   */
  public MetadataResponse executeGetMetadata(HttpServletRequest servletRequest) {
    if (!servletRequest.getParameterMap().isEmpty()) {
      throw new BadRequestException("The endpoint 'metadata' does not require parameters");
    }
    return new MetadataResponse(
        new Attribution(extractMetadata.getAttributionUrl(), extractMetadata.getAttributionShort()),
        Application.API_VERSION,
        new ExtractRegion(extractMetadata.getDataPolyJson(),
            new TemporalExtent(extractMetadata.getFromTstamp(), extractMetadata.getToTstamp()),
            extractMetadata.getReplicationSequenceNumber()));
  }
}
