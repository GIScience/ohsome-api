package org.heigit.ohsome.ohsomeapi.controller.dataaggregation.contributions;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.ohsome.ohsomeapi.controller.ParameterDescriptions;
import org.heigit.ohsome.ohsomeapi.executor.ContributionsExecutor;
import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller containing the methods, which are mapped to "/contributions/count" and used to
 * return the count of each contribution (creation, modification, deletion) of the OSM data.
 */
@Api(tags = "Contributions Count")
@RestController
@RequestMapping("/contributions")
public class ContributionsCountController {
  @Autowired
  ContributionsExecutor executor;
  /**
   * Gives the count of OSM contributions.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ContributionsExecutor
   *         #count(boolean, boolean) count}
   */
  @ApiOperation(value = "Count of OSM contributions", nickname = "contributionsCount",
      response = DefaultAggregationResponse.class)
  @RequestMapping(value = "/count", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "contributionType", value = ParameterDescriptions.CONTRIBUTION_TYPE,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  public Response contributionsCount(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    //var executor = new ContributionsExecutor(servletRequest, servletResponse, false);
    return executor.count(false, false);
  }

  /**
   * Gives the density of OSM contributions.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ContributionsExecutor
   *         #count(boolean, boolean) count}
   */
  @ApiOperation(
      value = "Density of OSM contributions (number of contributions divided by the "
          + "total area in square-kilometers)",
      nickname = "contributionsCountDensity", response = DefaultAggregationResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "contributionType", value = ParameterDescriptions.CONTRIBUTION_TYPE,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/count/density", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response contributionsCountDensity(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    //var executor = new ContributionsExecutor(servletRequest, servletResponse, true);
    return executor.count(false, false);
  }

  /**
   * Gives the count of latest OSM contributions.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link DefaultAggregationResponse}
   * @throws Exception thrown by {@link ContributionsExecutor#count(boolean,boolean)}
   */
  @ApiOperation(value = "Count of latest OSM contributions", nickname = "contributionsLatestCount",
      response = DefaultAggregationResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "contributionType", value = ParameterDescriptions.CONTRIBUTION_TYPE,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/latest/count", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response contributionsLatestCount(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    //var executor = new ContributionsExecutor(servletRequest, servletResponse, false);
    return executor.count(false, true);
  }

  /**
   * Gives the density of latest OSM contributions.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ContributionsExecutor
   *         #count(boolean, boolean) count}
   */
  @ApiOperation(
      value = "Density of the latest OSM contributions (number of contributions divided by the "
          + "total area in square-kilometers)",
      nickname = "contributionsLatestCountDensity", response = DefaultAggregationResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "contributionType", value = ParameterDescriptions.CONTRIBUTION_TYPE,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/latest/count/density", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response contributionsLatestCountDensity(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    //var executor = new ContributionsExecutor(servletRequest, servletResponse, true);
    return executor.count(false, true);
  }

  /**
   * Gives the count of OSM contributions grouped by boundary (bboxes, bcirlces, or bpolys).
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link GroupByResponse}
   * @throws Exception thrown by {@link ContributionsExecutor#countGroupByBoundary(boolean)}
   */
  @ApiOperation(
      value = "Count of OSM contributions grouped by boundary (bboxes, bcirlces, or bpolys)",
      nickname = "contributionsCountGroupByBoundary",
      response = GroupByResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "contributionType", value = ParameterDescriptions.CONTRIBUTION_TYPE,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/count/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response contributionsCountGroupByBoundary(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    //var executor = new ContributionsExecutor(servletRequest, servletResponse, false);
    return executor.countGroupByBoundary(false);
  }

  /**
   * Gives the count density of OSM contributions grouped by boundary (bboxes, bcirlces, or bpolys).
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link GroupByResponse}
   * @throws Exception thrown by {@link ContributionsExecutor#countGroupByBoundary(boolean)}
   */
  @ApiOperation(
      value =
          "Count density of OSM contributions grouped by boundary (bboxes, bcirlces, or bpolys)",
      nickname = "contributionsCountDensityGroupByBoundary",
      response = GroupByResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "contributionType", value = ParameterDescriptions.CONTRIBUTION_TYPE,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/count/density/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response contributionsCountDensityGroupByBoundary(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    //var executor = new ContributionsExecutor(servletRequest, servletResponse, true);
    return executor.countGroupByBoundary(false);
  }
}
