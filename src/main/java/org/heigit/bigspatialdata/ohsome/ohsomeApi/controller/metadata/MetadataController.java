package org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.metadata;

import org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.executor.MetadataRequestExecutor;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.metadataResponse.MetadataResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;

/**
 * REST controller containing the GET request, which enter through "/metadata".
 */
@Api(tags = "/metadata")
@RestController
@RequestMapping("/metadata")
public class MetadataController {

  /**
   * GET request giving the metadata of the underlying extract-region(s).
   * <p>
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.metadataResponse.MetadataResponse
   *         MetadataResponse}
   */
  @RequestMapping(method = RequestMethod.GET, produces = "application/json")
  public MetadataResponse getMetadata() throws UnsupportedOperationException, Exception {

    return MetadataRequestExecutor.executeGetMetadata();
  }

}
