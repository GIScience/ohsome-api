package org.heigit.ohsome.ohsomeapi.executor;

import javax.servlet.http.HttpServletRequest;
import org.heigit.ohsome.ohsomeapi.Application;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.ohsome.ohsomeapi.output.Attribution;
import org.heigit.ohsome.ohsomeapi.output.metadata.ExtractRegion;
import org.heigit.ohsome.ohsomeapi.output.metadata.MetadataResponse;
import org.heigit.ohsome.ohsomeapi.output.metadata.TemporalExtent;

/** Includes the execute method for requests mapped to /metadata. */
public class MetadataRequestExecutor {

  /**
   * Returns the metadata of the underlying extract-file.
   * 
   * @return {@link org.heigit.ohsome.ohsomeapi.output.metadata.MetadataResponse
   *         MetadataResponse}
   * @throws BadRequestException if parameters are given in the query.
   */
  public static MetadataResponse executeGetMetadata(HttpServletRequest servletRequest) {
    if (!servletRequest.getParameterMap().isEmpty()) {
      throw new BadRequestException("The endpoint 'metadata' does not require parameters");
    }
    return new MetadataResponse(
        new Attribution(ExtractMetadata.attributionUrl, ExtractMetadata.attributionShort),
        Application.API_VERSION, ExtractMetadata.timeout,
        new ExtractRegion(ExtractMetadata.dataPolyJson,
            new TemporalExtent(ExtractMetadata.fromTstamp, ExtractMetadata.toTstamp),
            ExtractMetadata.replicationSequenceNumber));
  }

  private MetadataRequestExecutor() {
    throw new IllegalStateException("Utility class");
  }
}
