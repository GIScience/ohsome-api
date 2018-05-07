package org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation;

import org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.DefaultSwaggerParameters;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.ParameterDescriptions;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.executor.RequestParameters;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.executor.UsersRequestExecutor;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResponse;
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

/** REST controller containing the GET and POST requests, which enter through "/users". */
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
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResponse
   *         GroupByTypeResponseContent}
   */
  @ApiOperation(value = "Count of OSM users")
  @RequestMapping(value = "/count", method = RequestMethod.GET, produces = "application/json")
  public DefaultAggregationResponse getCount(
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

    return UsersRequestExecutor.executeCount(new RequestParameters(false, false, false, bboxes,
        bcircles, bpolys, types, keys, values, userids, time, showMetadata));
  }

  /**
   * GET request giving the count of OSM users grouped by the OSM type.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResponse
   *         GroupByResponseContent}
   */
  @ApiOperation(value = "Count of OSM users grouped by the type")
  @RequestMapping(value = "/count/groupBy/type", method = RequestMethod.GET,
      produces = "application/json")
  public GroupByResponse getCountGroupByType(
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

    return UsersRequestExecutor.executeCountGroupByType(new RequestParameters(false, false, false,
        bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata));
  }

  /**
   * GET request giving the count of OSM users grouped by the tag.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResponse
   *         GroupByResponseContent}
   */
  @ApiOperation(value = "Count of OSM users grouped by the tag")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "count/groupBy/tag", method = RequestMethod.GET,
      produces = "application/json")
  public GroupByResponse getCountGroupByTag(
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
          defaultValue = "false") String showMetadata,
      @RequestParam(value = "groupByKey", defaultValue = "", required = false) String[] groupByKey,
      @RequestParam(value = "groupByValues", defaultValue = "",
          required = false) String[] groupByValues)
      throws UnsupportedOperationException, Exception {

    return UsersRequestExecutor.executeCountGroupByTag(new RequestParameters(false, false, false,
        bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata), groupByKey,
        groupByValues);
  }

  /**
   * GET request giving the density of OSM users (number of users per square-kilometers).
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   */
  @ApiOperation(value = "Density of OSM users (number of users per square-kilometers)")
  @RequestMapping(value = "/count/density", method = RequestMethod.GET,
      produces = "application/json")
  public DefaultAggregationResponse getCountDensity(
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

    return UsersRequestExecutor.executeCount(new RequestParameters(false, false, true, bboxes,
        bcircles, bpolys, types, keys, values, userids, time, showMetadata));
  }

  /**
   * GET request giving the density of OSM users grouped by the OSM type.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResponse
   *         GroupByResponseContent}
   */
  @ApiOperation(value = "Density of OSM users grouped by the type")
  @RequestMapping(value = "/count/density/groupBy/type", method = RequestMethod.GET,
      produces = "application/json")
  public GroupByResponse getCountDensityGroupByType(
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

    return UsersRequestExecutor.executeCountGroupByType(new RequestParameters(false, false, true,
        bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata));
  }

  /**
   * GET request giving the density of OSM users grouped by the tag.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResponse
   *         GroupByResponseContent}
   */
  @ApiOperation(value = "Density of OSM users grouped by the tag")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/count/density/groupBy/tag", method = RequestMethod.GET,
      produces = "application/json")
  public GroupByResponse getCountDensityGroupByTag(
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
          defaultValue = "false") String showMetadata,
      @RequestParam(value = "groupByKey", defaultValue = "", required = false) String[] groupByKey,
      @RequestParam(value = "groupByValues", defaultValue = "",
          required = false) String[] groupByValues)
      throws UnsupportedOperationException, Exception {

    return UsersRequestExecutor.executeCountGroupByTag(new RequestParameters(false, false, true,
        bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata), groupByKey,
        groupByValues);
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
          defaultValue = DefaultSwaggerParameters.BBOX, required = false,
          value = ParameterDescriptions.BBOXES_DESCR),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false, value = ParameterDescriptions.BCIRCLES_DESCR),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = ParameterDescriptions.BPOLYS_DESCR),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.TYPE, required = false,
          value = ParameterDescriptions.TYPES_DESCR),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = "building", required = false, value = ParameterDescriptions.KEYS_DESCR),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string",
          defaultValue = "residential", required = false,
          value = ParameterDescriptions.VALUES_DESCR),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = ParameterDescriptions.USERIDS_DESCR),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.TIME, required = false,
          value = ParameterDescriptions.TIME_DESCR),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.SHOW_METADATA, required = false,
          value = ParameterDescriptions.SHOW_METADATA_DESCR)})
  @RequestMapping(value = "/count", method = RequestMethod.POST, produces = "application/json",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public DefaultAggregationResponse postCount(String bboxes, String bcircles, String bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata) throws UnsupportedOperationException, Exception {

    return UsersRequestExecutor.executeCount(new RequestParameters(true, false, false, bboxes,
        bcircles, bpolys, types, keys, values, userids, time, showMetadata));
  }

  /**
   * POST request giving the count of OSM users grouped by the OSM type. POST requests should only
   * be used if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResponse
   *         GroupByResponseContent}
   */
  @ApiOperation(value = "Count of OSM users grouped by the type")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.BBOX, required = false,
          value = ParameterDescriptions.BBOXES_DESCR),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false, value = ParameterDescriptions.BCIRCLES_DESCR),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = ParameterDescriptions.BPOLYS_DESCR),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way, relation", required = false,
          value = ParameterDescriptions.TYPES_DESCR),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, required = false,
          value = ParameterDescriptions.KEYS_DESCR),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string", defaultValue = "",
          required = false, value = ParameterDescriptions.VALUES_DESCR),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = ParameterDescriptions.VALUES_DESCR),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.TIME, required = false,
          value = ParameterDescriptions.TIME_DESCR),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.SHOW_METADATA, required = false,
          value = ParameterDescriptions.SHOW_METADATA_DESCR)})
  @RequestMapping(value = "/count/groupBy/type", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByResponse postCountGroupByType(String bboxes, String bcircles, String bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata) throws UnsupportedOperationException, Exception {

    return UsersRequestExecutor.executeCountGroupByType(new RequestParameters(true, false, false,
        bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata));
  }

  /**
   * POST request giving the count of OSM users grouped by the tag. POST requests should only be
   * used if the request URL would be too long for a GET request.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param groupByKey <code>String</code> array containing the key used to create the tags for the
   *        grouping. At the current implementation, there must be one key given (not more and not
   *        less).
   * @param groupByValues <code>String</code> array containing the values used to create the tags
   *        for grouping. If a given value does not appear in the output, then there are no objects
   *        assigned to it (within the given filters).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResponse
   *         GroupByResponse Content}
   */
  @ApiOperation(value = "Count of OSM users grouped by the tag")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.BBOX, required = false,
          value = ParameterDescriptions.BBOXES_DESCR),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false, value = ParameterDescriptions.BCIRCLES_DESCR),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = ParameterDescriptions.BPOLYS_DESCR),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.TYPE, required = false,
          value = ParameterDescriptions.TYPES_DESCR),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, required = false,
          value = ParameterDescriptions.KEYS_DESCR),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string", defaultValue = "",
          required = false, value = ParameterDescriptions.VALUES_DESCR),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = ParameterDescriptions.USERIDS_DESCR),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.TIME, required = false,
          value = ParameterDescriptions.TIME_DESCR),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.SHOW_METADATA, required = false,
          value = ParameterDescriptions.SHOW_METADATA_DESCR),
      @ApiImplicitParam(name = "groupByKey", paramType = "form", dataType = "string",
          defaultValue = "height", required = true, value = ParameterDescriptions.KEYS_DESCR),
      @ApiImplicitParam(name = "groupByValues", paramType = "form", dataType = "string",
          defaultValue = "", required = false, value = ParameterDescriptions.VALUES_DESCR)})
  @RequestMapping(value = "/count/groupBy/tag", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByResponse postCountGroupByTag(String bboxes, String bcircles, String bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata, String[] groupByKey, String[] groupByValues)
      throws UnsupportedOperationException, Exception, BadRequestException {

    return UsersRequestExecutor.executeCountGroupByTag(new RequestParameters(true, false, false,
        bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata), groupByKey,
        groupByValues);
  }

  /**
   * POST request giving the density of OSM users (number of users per square-kilometers). POST
   * requests should only be used if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   */
  @ApiOperation(value = "Density of OSM users (number of users per square-kilometers)")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.BBOX, required = false,
          value = ParameterDescriptions.BBOXES_DESCR),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false, value = ParameterDescriptions.BCIRCLES_DESCR),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = ParameterDescriptions.BPOLYS_DESCR),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.TYPE, required = false,
          value = ParameterDescriptions.TYPES_DESCR),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, required = false,
          value = ParameterDescriptions.KEYS_DESCR),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.RESIDENTIAL_VALUE, required = false,
          value = ParameterDescriptions.VALUES_DESCR),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = ParameterDescriptions.USERIDS_DESCR),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.TIME, required = false,
          value = ParameterDescriptions.TIME_DESCR),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.SHOW_METADATA, required = false,
          value = ParameterDescriptions.SHOW_METADATA_DESCR)})
  @RequestMapping(value = "/count/density", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public DefaultAggregationResponse postCountDensity(String bboxes, String bcircles, String bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata) throws UnsupportedOperationException, Exception {

    return UsersRequestExecutor.executeCount(new RequestParameters(true, false, true, bboxes,
        bcircles, bpolys, types, keys, values, userids, time, showMetadata));
  }

  /**
   * POST request giving the density of OSM users grouped by the OSM type. POST requests should only
   * be used if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResponse
   *         GroupByResponseContent}
   */
  @ApiOperation(value = "Density of OSM users grouped by the type")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.BBOX, required = false,
          value = ParameterDescriptions.BBOXES_DESCR),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false, value = ParameterDescriptions.BCIRCLES_DESCR),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = ParameterDescriptions.BPOLYS_DESCR),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way, relation", required = false,
          value = ParameterDescriptions.TYPES_DESCR),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, required = false,
          value = ParameterDescriptions.KEYS_DESCR),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string", defaultValue = "",
          required = false, value = ParameterDescriptions.VALUES_DESCR),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = ParameterDescriptions.USERIDS_DESCR),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.TIME, required = false,
          value = ParameterDescriptions.TIME_DESCR),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.SHOW_METADATA, required = false,
          value = ParameterDescriptions.SHOW_METADATA_DESCR)})
  @RequestMapping(value = "/count/density/groupBy/type", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByResponse postCountDensityGroupByType(String bboxes, String bcircles, String bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata) throws UnsupportedOperationException, Exception {

    return UsersRequestExecutor.executeCountGroupByType(new RequestParameters(true, false, true,
        bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata));
  }

  /**
   * POST request giving the density of OSM users grouped by the tag. POST requests should only
   * be used if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.groupByResponse.GroupByResponse
   *         GroupByResponseContent}
   */
  @ApiOperation(value = "Density of OSM users grouped by the tag")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.BBOX, required = false,
          value = ParameterDescriptions.BBOXES_DESCR),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false, value = ParameterDescriptions.BCIRCLES_DESCR),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = ParameterDescriptions.BPOLYS_DESCR),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way, relation", required = false,
          value = ParameterDescriptions.TYPES_DESCR),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, required = false,
          value = ParameterDescriptions.KEYS_DESCR),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string", defaultValue = "",
          required = false, value = ParameterDescriptions.VALUES_DESCR),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = ParameterDescriptions.USERIDS_DESCR),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.TIME, required = false,
          value = ParameterDescriptions.TIME_DESCR),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = DefaultSwaggerParameters.SHOW_METADATA, required = false,
          value = ParameterDescriptions.SHOW_METADATA_DESCR),
          @ApiImplicitParam(name = "groupByKey", paramType = "form", dataType = "string",
          defaultValue = "height", required = true, value = ParameterDescriptions.KEYS_DESCR),
      @ApiImplicitParam(name = "groupByValues", paramType = "form", dataType = "string",
          defaultValue = "", required = false, value = ParameterDescriptions.VALUES_DESCR)})
  @RequestMapping(value = "/count/density/groupBy/tag", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByResponse postCountDensityGroupByTag(String bboxes, String bcircles, String bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata, String[] groupByKey, String[] groupByValues) throws UnsupportedOperationException, Exception {

    return UsersRequestExecutor.executeCountGroupByTag(new RequestParameters(true, false, true,
        bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata), groupByKey,
        groupByValues);
  }

}
