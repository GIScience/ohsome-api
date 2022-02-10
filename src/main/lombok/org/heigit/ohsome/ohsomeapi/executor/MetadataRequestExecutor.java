package org.heigit.ohsome.ohsomeapi.executor;

import javax.servlet.http.HttpServletRequest;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.ohsome.ohsomeapi.output.Attribution;
import org.heigit.ohsome.ohsomeapi.output.metadata.ExtractRegion;
import org.heigit.ohsome.ohsomeapi.output.metadata.MetadataResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Includes the execute method for requests mapped to /metadata. */
@Component
public class MetadataRequestExecutor {

  @Autowired
  private ExtractMetadata extractMetadata;
  @Autowired
  private ExtractRegion extractRegion;
  @Autowired
  private Attribution attribution;
  @Autowired
  private MetadataResponse metadataResponse;

  /**
   * Returns the metadata of the underlying extract-file.
   *
   * @return {@link org.heigit.ohsome.ohsomeapi.output.metadata.MetadataResponse MetadataResponse}
   * @throws BadRequestException if parameters are given in the query.
   */
  public MetadataResponse executeGetMetadata(HttpServletRequest servletRequest) {
    if (!servletRequest.getParameterMap().isEmpty()) {
      throw new BadRequestException("The endpoint 'metadata' does not require parameters");
    }
    metadataResponse.setAttribution(attribution);
    metadataResponse.setExtractRegion(extractRegion);
    metadataResponse.setTimeout(ProcessingData.getTimeout());
    metadataResponse.getExtractRegion().setSpatialExtent(extractMetadata.getDataPolyJson());
    return metadataResponse;
  }
}
