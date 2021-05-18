package org.heigit.ohsome.ohsomeapi.controller.dataaggregation.users;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.ohsome.ohsomeapi.controller.DefaultSwaggerParameters;
import org.heigit.ohsome.ohsomeapi.controller.ParameterDescriptions;
import org.heigit.ohsome.ohsomeapi.executor.ContributionsExecutor;
import org.heigit.ohsome.ohsomeapi.executor.UsersRequestExecutor;
import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/** Controller containing the GET and POST servletRequests, which enter through "/users". */
@Api(tags = "Users Count")
@RestController
@RequestMapping("/users")
public class UsersController {

  /**
   * Gives the count of OSM users.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   * @throws Exception thrown by {@link
   *         org.heigit.ohsome.ohsomeapi.executor.ContributionsExecutor#count(boolean) count}
   */
  @ApiOperation(value = "Count of OSM users", nickname = "count",
      response = DefaultAggregationResponse.class)
  @RequestMapping(value = "/count", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response count(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
      throws Exception {
    ContributionsExecutor executor =
        new ContributionsExecutor(servletRequest, servletResponse, false);
    return executor.count(true, false);
  }

  /**
   * Gives the count of OSM users grouped by the OSM type.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.UsersRequestExecutor
   *         #countGroupByType(HttpServletRequest, HttpServletResponse, boolean) countGroupByType}
   */
  @ApiOperation(value = "Count of OSM users grouped by the type", nickname = "countGroupByType",
      response = GroupByResponse.class)
  @RequestMapping(value = "/count/groupBy/type", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response countGroupByType(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return UsersRequestExecutor.countGroupByType(servletRequest, servletResponse, false);
  }

  /**
   * Gives the count of OSM users grouped by the tag.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.UsersRequestExecutor
   *         #countGroupByTag(HttpServletRequest, HttpServletResponse, boolean) countGroupByTag}
   */
  @ApiOperation(value = "Count of OSM users grouped by the tag", nickname = "countGroupByTag",
      response = GroupByResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.GROUP_BY_KEY,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "count/groupBy/tag", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response countGroupByTag(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return UsersRequestExecutor.countGroupByTag(servletRequest, servletResponse, false);
  }

  /**
   * Gives the count of OSM users grouped by the key.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.UsersRequestExecutor
   *         #countGroupByKey(HttpServletRequest, HttpServletResponse, boolean) countGroupByKey}
   */
  @ApiOperation(value = "Count of OSM users grouped by the key", nickname = "countGroupByKey",
      response = GroupByResponse.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "groupByKeys", value = ParameterDescriptions.KEYS,
      defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
      dataType = "string", required = true)})
  @RequestMapping(value = "count/groupBy/key", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response countGroupByKey(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return UsersRequestExecutor.countGroupByKey(servletRequest, servletResponse, false);
  }

  /**
   * Gives the count of OSM users grouped by boundary geometries.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.UsersRequestExecutor
   *         #countGroupByBoundary(HttpServletRequest, HttpServletResponse, boolean)
   *         countGroupByBoundary}
   */
  @ApiOperation(value = "Count of OSM users grouped by boundary (bboxes, bcirlces, or bpolys)",
      nickname = "countGroupByBoundary", response = GroupByResponse.class)
  @RequestMapping(value = "/count/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response countGroupByBoundary(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return UsersRequestExecutor.countGroupByBoundary(servletRequest, servletResponse, false);
  }

  /**
   * Gives the density of OSM users (number of users divided by the total area in
   * square-kilometers).
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   * @throws Exception thrown by {@link
   *         org.heigit.ohsome.ohsomeapi.executor.ContributionsExecutor#count(boolean) count}
   */
  @ApiOperation(
      value = "Density of OSM users (number of users divided "
          + "by the total area in square-kilometers)",
      nickname = "countDensity", response = DefaultAggregationResponse.class)
  @RequestMapping(value = "/count/density", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response countDensity(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    ContributionsExecutor executor =
        new ContributionsExecutor(servletRequest, servletResponse, true);
    return executor.count(true, false);
  }

  /**
   * Gives the density of OSM users grouped by the OSM type.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.UsersRequestExecutor
   *         #countGroupByType(HttpServletRequest, HttpServletResponse, boolean) countGroupByType}
   */
  @ApiOperation(value = "Density of OSM users grouped by the type",
      nickname = "countDensityGroupByType", response = GroupByResponse.class)
  @RequestMapping(value = "/count/density/groupBy/type",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response countDensityGroupByType(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return UsersRequestExecutor.countGroupByType(servletRequest, servletResponse, true);
  }

  /**
   * Gives the density of OSM users grouped by the tag.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.UsersRequestExecutor
   *         #countGroupByTag(HttpServletRequest, HttpServletResponse, boolean) countGroupByTag}
   */
  @ApiOperation(value = "Density of OSM users grouped by the tag",
      nickname = "countDensityGroupByTag", response = GroupByResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.GROUP_BY_KEY,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/count/density/groupBy/tag",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response countDensityGroupByTag(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return UsersRequestExecutor.countGroupByTag(servletRequest, servletResponse, true);
  }

  /**
   * Gives the density of OSM users (number of users divided by the total area in square-kilometers)
   * grouped by boundary geometries.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.UsersRequestExecutor
   *         #countGroupByBoundary(HttpServletRequest, HttpServletResponse, boolean)
   *         countGroupByBoundary}
   */
  @ApiOperation(value = "Count of OSM users grouped by boundary (bboxes, bcirlces, or bpolys)",
      nickname = "countDensityGroupByBoundary", response = GroupByResponse.class)
  @RequestMapping(value = "/count/density/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response countDensityGroupByBoundary(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return UsersRequestExecutor.countGroupByBoundary(servletRequest, servletResponse, true);
  }
}
