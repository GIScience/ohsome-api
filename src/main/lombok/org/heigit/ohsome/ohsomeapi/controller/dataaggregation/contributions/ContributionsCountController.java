package org.heigit.ohsome.ohsomeapi.controller.dataaggregation.contributions;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.ohsome.ohsomeapi.executor.ContributionsExecutor;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller containing the methods, which are mapped to "/contributions/count" and used to
 * return the count of each contribution (creation, modification, deletion) of the OSM data.
 */
@Api(tags = "ContributionsCount")
@RestController
@RequestMapping("/contributions/count")
public class ContributionsCountController {

  /**
   * Gives the count of OSM contributions as JSON.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response}
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ContributionsExecutor#count(boolean) count}
   */
  @ApiOperation(value = "count of OSM contributions", nickname = "contributionsCount",
      response = DefaultAggregationResponse.class)
  @RequestMapping(value = "", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response contributionsCount(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    ContributionsExecutor executor =
        new ContributionsExecutor(servletRequest, servletResponse, false);
    return executor.count(false);
  }
  
  /**
   * Gives the density of OSM contributions as JSON.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response}
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.executor.ContributionsExecutor#count(boolean) count}
   */
  @ApiOperation(value = "density of OSM contributions", nickname = "contributionsCountDensity",
      response = DefaultAggregationResponse.class)
  @RequestMapping(value = "/density", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response contributionsCountDensity(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    ContributionsExecutor executor =
        new ContributionsExecutor(servletRequest, servletResponse, true);
    return executor.count(false);
  }
  
}
