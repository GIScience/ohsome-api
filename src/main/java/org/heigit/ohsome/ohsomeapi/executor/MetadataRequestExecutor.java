package org.heigit.ohsome.ohsomeapi.executor;

import javax.servlet.http.HttpServletRequest;
import org.heigit.ohsome.ohsomeapi.Application;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Attribution;
import org.heigit.ohsome.ohsomeapi.output.metadataresponse.ExtractRegion;
import org.heigit.ohsome.ohsomeapi.output.metadataresponse.MetadataResponse;
import org.heigit.ohsome.ohsomeapi.output.metadataresponse.TemporalExtent;

/** Includes the execute method for requests mapped to /metadata. */
public class MetadataRequestExecutor {

  /**
   * Returns the metadata of the underlying extract-file.
   * 
   * @return {@link org.heigit.ohsome.ohsomeapi.output.metadataresponse.MetadataResponse
   *         MetadataResponse}
   * @throws BadRequestException if parameters are given in the query.
   */
  public static MetadataResponse executeGetMetadata(HttpServletRequest servletRequest) {
    if (!servletRequest.getParameterMap().isEmpty()) {
      throw new BadRequestException("The endpoint 'metadata' does not require parameters");
    }
    return new MetadataResponse(
        new Attribution(ExtractMetadata.getAttributionUrl(), ExtractMetadata.getAttributionShort()),
        Application.API_VERSION,
        new ExtractRegion(ExtractMetadata.getDataPolyJson(),
            new TemporalExtent(ExtractMetadata.getFromTstamp(), ExtractMetadata.getToTstamp()),
            ExtractMetadata.getReplicationSequenceNumber()));
  }

  private MetadataRequestExecutor() {
    throw new IllegalStateException("Utility class");
  }
}
