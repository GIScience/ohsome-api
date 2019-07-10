package org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.DefaultSwaggerParameters;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.ParameterDescriptions;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.UsersRequestExecutor;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.DefaultAggregationResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.GroupByResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/** Controller containing the GET and POST servletRequests, which enter through "/users". */
@Api(tags = "Users")
@RestController
@RequestMapping("/users")
public class UsersController {

  /**
   * Gives the count of OSM users.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse
   *         Response}
   */
  @ApiOperation(value = "Count of OSM users", nickname = "count",
      response = DefaultAggregationResponse.class)
  @RequestMapping(value = "/count", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response count(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
      throws Exception {
    return UsersRequestExecutor.executeCount(servletRequest, servletResponse, false);
  }

  /**
   * Gives the count of OSM users grouped by the OSM type.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Count of OSM users grouped by the type", nickname = "countGroupByType",
      response = GroupByResponse.class)
  @RequestMapping(value = "/count/groupBy/type", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response countGroupByType(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return UsersRequestExecutor.executeCountGroupByType(servletRequest, servletResponse, false);
  }

  /**
   * Gives the count of OSM users grouped by the tag.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Count of OSM users grouped by the tag", nickname = "countGroupByTag",
      response = GroupByResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.GROUP_BY_KEY_DESCR,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "count/groupBy/tag", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response countGroupByTag(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return UsersRequestExecutor.executeCountGroupByTag(servletRequest, servletResponse, false);
  }

  /**
   * Gives the count of OSM users grouped by the key.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Count of OSM users grouped by the key", nickname = "countGroupByKey",
      response = GroupByResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKeys", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true)})
  @RequestMapping(value = "count/groupBy/key", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response countGroupByKey(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return UsersRequestExecutor.executeCountGroupByKey(servletRequest, servletResponse, false);
  }

  /**
   * Gives the count of OSM users grouped by boundary geometries.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Count of OSM users grouped by boundary (bboxes, bcirlces, or bpolys)", nickname = "countGroupByBoundary",
      response = GroupByResponse.class)
  @RequestMapping(value = "/count/groupBy/boundary", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response countGroupByBoundary(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return UsersRequestExecutor.executeCountGroupByBoundary(servletRequest, servletResponse, false);
  }

  /**
   * Gives the density of OSM users (number of users divided by the total area in
   * square-kilometers).
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse
   *         Response}
   */
  @ApiOperation(
      value = "Density of OSM users (number of users divided "
          + "by the total area in square-kilometers)",
      nickname = "countDensity", response = DefaultAggregationResponse.class)
  @RequestMapping(value = "/count/density", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response countDensity(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return UsersRequestExecutor.executeCount(servletRequest, servletResponse, true);
  }

  /**
   * Gives the density of OSM users grouped by the OSM type.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Density of OSM users grouped by the type",
      nickname = "countDensityGroupByType", response = GroupByResponse.class)
  @RequestMapping(value = "/count/density/groupBy/type",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response countDensityGroupByType(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return UsersRequestExecutor.executeCountGroupByType(servletRequest, servletResponse, true);
  }

  /**
   * Gives the density of OSM users grouped by the tag.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Density of OSM users grouped by the tag",
      nickname = "countDensityGroupByTag", response = GroupByResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.GROUP_BY_KEY_DESCR,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/count/density/groupBy/tag",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response countDensityGroupByTag(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return UsersRequestExecutor.executeCountGroupByTag(servletRequest, servletResponse, true);
  }
}
