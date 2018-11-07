package org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation;

import javax.servlet.http.HttpServletRequest;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.DefaultSwaggerParameters;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.ParameterDescriptions;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.RequestParameters;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.UsersRequestExecutor;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response;
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
@Api(tags = "users")
@RestController
@RequestMapping("/users")
public class UsersController {

  /**
   * Gives the count of OSM users.
   * 
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse
   *         Response}
   */
  @ApiOperation(value = "Count of OSM users", nickname = "usersCount")
  @RequestMapping(value = "/count", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response count(@ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) HttpServletRequest request) throws Exception {
    return UsersRequestExecutor.executeCount(
        new RequestParameters(request.getMethod(), false, false, request.getParameter("bboxes"),
            request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys, values,
            userids, time, request.getParameter("showMetadata")));
  }

  /**
   * Gives the count of OSM users grouped by the OSM type.
   * 
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Count of OSM users grouped by the type",
      nickname = "usersCountGroupByType")
  @RequestMapping(value = "/count/groupBy/type", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response countGroupByType(
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) HttpServletRequest request) throws Exception {
    return UsersRequestExecutor.executeCountGroupByType(
        new RequestParameters(request.getMethod(), false, false, request.getParameter("bboxes"),
            request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys, values,
            userids, time, request.getParameter("showMetadata")));
  }

  /**
   * Gives the count of OSM users grouped by the tag.
   * 
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Count of OSM users grouped by the tag", nickname = "usersCountGroupByTag")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "count/groupBy/tag", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response countGroupByTag(
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) HttpServletRequest request,
      @RequestParam(value = "groupByKey", defaultValue = "", required = false) String[] groupByKey,
      @RequestParam(value = "groupByValues", defaultValue = "",
          required = false) String[] groupByValues)
      throws Exception {
    return UsersRequestExecutor
        .executeCountGroupByTag(
            new RequestParameters(request.getMethod(), false, false, request.getParameter("bboxes"),
                request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys,
                values, userids, time, request.getParameter("showMetadata")),
            groupByKey, groupByValues);
  }

  /**
   * Gives the count of OSM users grouped by the key.
   * 
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Count of OSM users grouped by the tag", nickname = "usersCountGroupByKey")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKeys", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true)})
  @RequestMapping(value = "count/groupBy/key", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response countGroupByKey(
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) HttpServletRequest request,
      @RequestParam(value = "groupByKeys", defaultValue = "", required = false) String[] groupByKey)
      throws Exception {
    return UsersRequestExecutor.executeCountGroupByKey(
        new RequestParameters(request.getMethod(), false, false, request.getParameter("bboxes"),
            request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys, values,
            userids, time, request.getParameter("showMetadata")),
        groupByKey);
  }

  /**
   * Gives the density of OSM users (number of users divided by the total area in
   * square-kilometers).
   * 
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse
   *         Response}
   */
  @ApiOperation(value = "Density of OSM users (number of users divided "
      + "by the total area in square-kilometers)", nickname = "usersCountDensity")
  @RequestMapping(value = "/count/density", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response countDensity(
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) HttpServletRequest request) throws Exception {
    return UsersRequestExecutor.executeCount(
        new RequestParameters(request.getMethod(), false, true, request.getParameter("bboxes"),
            request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys, values,
            userids, time, request.getParameter("showMetadata")));
  }

  /**
   * Gives the density of OSM users grouped by the OSM type.
   * 
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Density of OSM users grouped by the type",
      nickname = "usersCountDensityGroupByType")
  @RequestMapping(value = "/count/density/groupBy/type",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
  public Response countDensityGroupByType(
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) HttpServletRequest request) throws Exception {
    return UsersRequestExecutor.executeCountGroupByType(
        new RequestParameters(request.getMethod(), false, true, request.getParameter("bboxes"),
            request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys, values,
            userids, time, request.getParameter("showMetadata")));
  }

  /**
   * Gives the density of OSM users grouped by the tag.
   * 
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Density of OSM users grouped by the tag",
      nickname = "usersCountDensityGroupByTag")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/count/density/groupBy/tag",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
  public Response countDensityGroupByTag(
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) HttpServletRequest request,
      @RequestParam(value = "groupByKey", defaultValue = "", required = false) String[] groupByKey,
      @RequestParam(value = "groupByValues", defaultValue = "",
          required = false) String[] groupByValues)
      throws Exception {
    return UsersRequestExecutor
        .executeCountGroupByTag(
            new RequestParameters(request.getMethod(), false, true, request.getParameter("bboxes"),
                request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys,
                values, userids, time, request.getParameter("showMetadata")),
            groupByKey, groupByValues);
  }
}
