package org.heigit.ohsome.ohsomeapi.controller.metadata;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import org.heigit.ohsome.ohsomeapi.executor.MetadataRequestExecutor;
import org.heigit.ohsome.ohsomeapi.output.metadata.MetadataResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
  @Autowired
  MetadataRequestExecutor metadataRequestExecutor;

  /**
   * GET request giving the metadata of the underlying extract-region(s).
   *
   * @return {@link org.heigit.ohsome.ohsomeapi.output.metadata.MetadataResponse MetadataResponse}
   */
  @GetMapping(produces = "application/json")
  @ApiOperation(nickname = "Metadata", value = "Metadata of the underlying OSHDB data-extract")
  public MetadataResponse getMetadata(HttpServletRequest servletRequest) {
    return metadataRequestExecutor.executeGetMetadata(servletRequest);
  }
}
