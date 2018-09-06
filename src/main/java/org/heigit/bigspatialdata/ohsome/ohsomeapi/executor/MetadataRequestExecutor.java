package org.heigit.bigspatialdata.ohsome.ohsomeapi.executor;

import org.heigit.bigspatialdata.ohsome.ohsomeapi.Application;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataAggregationResponse.Attribution;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.metadataResponse.ExtractRegion;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.metadataResponse.MetadataResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.metadataResponse.TemporalExtent;

/** Includes the execute method for requests mapped to /metadata. */
public class MetadataRequestExecutor {

  /**
   * Returns the metadata of the underlying extract-file.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.metadataResponse.MetadataResponse
   *         MetadataResponse}
   */
  public static MetadataResponse executeGetMetadata() {
    return new MetadataResponse(
        new Attribution(ExtractMetadata.attributionUrl, ExtractMetadata.attributionShort),
        Application.apiVersion, new ExtractRegion(ExtractMetadata.dataPolyJson,
            new TemporalExtent(ExtractMetadata.fromTstamp, ExtractMetadata.toTstamp)));
  }
}
