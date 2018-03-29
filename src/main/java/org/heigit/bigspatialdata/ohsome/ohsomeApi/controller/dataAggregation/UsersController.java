package org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation;

import org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.ParameterDescriptions;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.executor.UsersRequestExecutor;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultUsersResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * REST controller containing the GET and POST requests, which enter through "/users".
 *
 */
@Api(tags = "/users")
@RestController
@RequestMapping("/users")
public class UsersController {

  /**
   * GET request giving the count of OSM users.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByTypeResponse
   *         GroupByTypeResponseContent}
   */
  @ApiOperation(value = "Count of OSM users")
  @RequestMapping(value = "/count", method = RequestMethod.GET, produces = "application/json")
  public DefaultUsersResponse getCountGroupByType(
      @ApiParam(hidden = true) @RequestParam(value = "bboxes", defaultValue = "",
          required = false) String bboxes,
      @ApiParam(hidden = true) @RequestParam(value = "bcircles", defaultValue = "",
          required = false) String bcircles,
      @ApiParam(hidden = true) @RequestParam(value = "bpolys", defaultValue = "",
          required = false) String bpolys,
      @ApiParam(hidden = true) @RequestParam(value = "types", defaultValue = "",
          required = false) String[] types,
      @ApiParam(hidden = true) @RequestParam(value = "keys", defaultValue = "",
          required = false) String[] keys,
      @ApiParam(hidden = true) @RequestParam(value = "values", defaultValue = "",
          required = false) String[] values,
      @ApiParam(hidden = true) @RequestParam(value = "userids", defaultValue = "",
          required = false) String[] userids,
      @ApiParam(hidden = true) @RequestParam(value = "time", defaultValue = "",
          required = false) String[] time,
      @ApiParam(hidden = true) @RequestParam(value = "showMetadata",
          defaultValue = "false") String showMetadata)
      throws UnsupportedOperationException, Exception {

    return UsersRequestExecutor.executeCount(false, bboxes, bcircles, bpolys, types, keys, values,
        userids, time, showMetadata);
  }

  /**
   * POST request giving the count of OSM users. POST requests should only be used if the request
   * URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   */
  @ApiOperation(value = "Count of OSM users")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
          defaultValue = "8.6128,49.3183,8.7294,49.4376", required = false,
          value = ParameterDescriptions.bboxesDescr),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false, value = ParameterDescriptions.bcirclesDescr),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = ParameterDescriptions.bpolysDescr),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way", required = false, value = ParameterDescriptions.typesDescr),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = "building", required = false, value = ParameterDescriptions.keysDescr),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string",
          defaultValue = "residential", required = false,
          value = ParameterDescriptions.valuesDescr),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = ParameterDescriptions.useridsDescr),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = "2010-01-01/2017-01-01/P1Y", required = false,
          value = ParameterDescriptions.timeDescr),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = "true", required = false,
          value = ParameterDescriptions.showMetadataDescr)})
  @RequestMapping(value = "/count", method = RequestMethod.POST, produces = "application/json",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public DefaultUsersResponse postCount(String bboxes, String bcircles, String bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata) throws UnsupportedOperationException, Exception {

    return UsersRequestExecutor.executeCount(true, bboxes, bcircles, bpolys, types, keys, values,
        userids, time, showMetadata);
  }

}
