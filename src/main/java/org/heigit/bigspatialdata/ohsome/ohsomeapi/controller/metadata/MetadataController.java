package org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.metadata;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.MetadataRequestExecutor;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.metadataresponse.MetadataResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller containing the GET request, which enters through "/metadata".
 */
@Api(tags = "Metadata")
@RestController
@RequestMapping("/metadata")
public class MetadataController {

  /**
   * GET request giving the metadata of the underlying extract-region(s).
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.metadataresponse.MetadataResponse
   *         MetadataResponse}
   */
  @GetMapping(produces = "application/json")
  @ApiOperation(nickname = "Metadata", value = "Metadata of the underlying OSHDB data-extract")
  public MetadataResponse getMetadata() {
    return MetadataRequestExecutor.executeGetMetadata();
  }
}
