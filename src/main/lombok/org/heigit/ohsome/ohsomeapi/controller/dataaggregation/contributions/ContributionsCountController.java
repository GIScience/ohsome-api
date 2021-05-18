package org.heigit.ohsome.ohsomeapi.controller.dataaggregation.contributions;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.ohsome.ohsomeapi.executor.ContributionsExecutor;
import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.output.Response;
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

  /**
   * Gives the count of OSM contributions.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ContributionsExecutor#count(boolean) count}
   */
  @ApiOperation(value = "Count of OSM contributions", nickname = "contributionsCount",
      response = DefaultAggregationResponse.class)
  @RequestMapping(value = "/count", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response contributionsCount(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    ContributionsExecutor executor =
        new ContributionsExecutor(servletRequest, servletResponse, false);
    return executor.count(false, false);
  }

  /**
   * Gives the density of OSM contributions.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ContributionsExecutor#count(boolean) count}
   */
  @ApiOperation(
      value = "Density of OSM contributions (number of contributions divided by the "
          + "total area in square-kilometers)",
      nickname = "contributionsCountDensity", response = DefaultAggregationResponse.class)
  @RequestMapping(value = "/count/density", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response contributionsCountDensity(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    ContributionsExecutor executor =
        new ContributionsExecutor(servletRequest, servletResponse, true);
    return executor.count(false, false);
  }

  /**
   * Gives the count of latest OSM contributions.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ContributionsExecutor#count(boolean) count}
   */
  @ApiOperation(value = "Count of latest OSM contributions", nickname = "contributionsLatestCount",
      response = DefaultAggregationResponse.class)
  @RequestMapping(value = "/latest/count", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response contributionsLatestCount(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    ContributionsExecutor executor =
        new ContributionsExecutor(servletRequest, servletResponse, false);
    return executor.count(false, true);
  }
}
