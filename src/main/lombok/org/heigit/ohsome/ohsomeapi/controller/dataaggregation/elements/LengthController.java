package org.heigit.ohsome.ohsomeapi.controller.dataaggregation.elements;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.ohsome.ohsomeapi.controller.DefaultSwaggerParameters;
import org.heigit.ohsome.ohsomeapi.controller.ParameterDescriptions;
import org.heigit.ohsome.ohsomeapi.executor.AggregateRequestExecutor;
import org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor;
import org.heigit.ohsome.ohsomeapi.executor.RequestResource;
import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResponse;
import org.heigit.ohsome.ohsomeapi.output.ratio.RatioGroupByBoundaryResponse;
import org.heigit.ohsome.ohsomeapi.output.ratio.RatioResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller containing the GET and POST request handling methods, which are mapped to
 * "/elements/length".
 */
@Api(tags = "Elements Length")
@RestController
@RequestMapping("/elements/length")
public class LengthController {

  /**
   * Gives the length of OSM elements.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link
   *         org.heigit.ohsome.ohsomeapi.executor.AggregateRequestExecutor#aggregate() aggregate}
   */
  @ApiOperation(value = "Length of OSM elements", nickname = "length",
      response = DefaultAggregationResponse.class)
  @RequestMapping(value = "", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response length(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
      throws Exception {
    AggregateRequestExecutor executor = new AggregateRequestExecutor(RequestResource.LENGTH,
        servletRequest, servletResponse, false);
    return executor.aggregate();
  }

  /**
   * Gives the length of OSM elements grouped by the OSM type.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateGroupByType(RequestResource, HttpServletRequest, HttpServletResponse,
   *         boolean, boolean) aggregateGroupByType}
   */
  @ApiOperation(value = "Length of OSM elements grouped by the type",
      nickname = "lengthGroupByType", response = GroupByResponse.class)
  @RequestMapping(value = "/groupBy/type", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response lengthGroupByType(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.aggregateGroupByType(RequestResource.LENGTH, servletRequest,
        servletResponse, true, false);
  }

  /**
   * Gives the length of OSM elements grouped by the boundary parameter (bounding
   * box/circle/polygon).
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link
   *         org.heigit.ohsome.ohsomeapi.executor.AggregateRequestExecutor
   *         #aggregateGroupByBoundary() aggregateGroupByBoundary}
   */
  @ApiOperation(
      value = "Length of OSM elements grouped by the boundary (bboxes, bcircles, or bpolys)",
      nickname = "lengthGroupByBoundary", response = GroupByResponse.class)
  @ApiImplicitParam(name = "format", value = ParameterDescriptions.FORMAT, defaultValue = "",
      paramType = "query", dataType = "string", required = false)
  @RequestMapping(value = "/groupBy/boundary", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response lengthGroupByBoundary(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    AggregateRequestExecutor executor = new AggregateRequestExecutor(RequestResource.LENGTH,
        servletRequest, servletResponse, false);
    return executor.aggregateGroupByBoundary();
  }

  /**
   * Gives the length of OSM elements grouped by the boundary and the tag.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateGroupByBoundaryGroupByTag(RequestResource, HttpServletRequest,
   *         HttpServletResponse, boolean, boolean) aggregateGroupByBoundaryGroupByTag}
   */
  @ApiOperation(value = "Length of OSM elements grouped by the boundary and the tag",
      nickname = "lengthGroupByBoundaryGroupByTag", response = GroupByResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.GROUP_BY_KEY,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/groupBy/boundary/groupBy/tag",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response lengthGroupByBoundaryGroupByTag(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.aggregateGroupByBoundaryGroupByTag(RequestResource.LENGTH,
        servletRequest, servletResponse, true, false);
  }

  /**
   * Gives the length of OSM elements grouped by the key.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateGroupByKey(RequestResource, HttpServletRequest, HttpServletResponse, boolean,
   *         boolean) aggregateGroupByKey}
   */
  @ApiOperation(value = "Length of OSM elements grouped by the key", nickname = "lengthGroupByKey",
      response = GroupByResponse.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "groupByKeys", value = ParameterDescriptions.KEYS,
      defaultValue = DefaultSwaggerParameters.HIGHWAY_KEY, paramType = "query", dataType = "string",
      required = true)})
  @RequestMapping(value = "/groupBy/key", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response lengthGroupByKey(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.aggregateGroupByKey(RequestResource.LENGTH, servletRequest,
        servletResponse, true, false);
  }

  /**
   * Gives the length of OSM elements grouped by the tag.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateGroupByTag(RequestResource, HttpServletRequest, HttpServletResponse, boolean,
   *         boolean) aggregateGroupByTag}
   */
  @ApiOperation(value = "Length of OSM elements grouped by the tag", nickname = "lengthGroupByTag",
      response = GroupByResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.GROUP_BY_KEY,
          defaultValue = DefaultSwaggerParameters.HIGHWAY_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/groupBy/tag", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response lengthGroupByTag(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.aggregateGroupByTag(RequestResource.LENGTH, servletRequest,
        servletResponse, true, false);
  }

  /**
   * Gives the density of OSM elements (length of items divided by the total area in
   * square-kilometers).
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link
   *         org.heigit.ohsome.ohsomeapi.executor.AggregateRequestExecutor#aggregate() aggregate}
   */
  @ApiOperation(
      value = "Density of OSM elements (length of elements divided by "
          + "the total area in square-kilometers)",
      nickname = "lengthDensity", response = DefaultAggregationResponse.class)
  @RequestMapping(value = "/density", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response lengthDensity(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    AggregateRequestExecutor executor =
        new AggregateRequestExecutor(RequestResource.LENGTH, servletRequest, servletResponse, true);
    return executor.aggregate();
  }

  /**
   * Gives the density of OSM elements grouped by the OSM type.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateGroupByType(RequestResource, HttpServletRequest, HttpServletResponse,
   *         boolean, boolean) aggregateGroupByType}
   */
  @ApiOperation(value = "Density of OSM elements grouped by the OSM type",
      nickname = "lengthDensityGroupByType", response = GroupByResponse.class)
  @RequestMapping(value = "/density/groupBy/type", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response lengthDensityGroupByType(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.aggregateGroupByType(RequestResource.LENGTH, servletRequest,
        servletResponse, true, true);
  }

  /**
   * Gives density of OSM elements grouped by the boundary parameter (bounding box/circle/polygon).
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link
   *         org.heigit.ohsome.ohsomeapi.executor.AggregateRequestExecutor
   *         #aggregateGroupByBoundary() aggregateGroupByBoundary}
   */
  @ApiOperation(
      value = "Density of OSM elements grouped by the boundary (bboxes, bcircles, or bpolys)",
      nickname = "lengthDensityGroupByBoundary", response = GroupByResponse.class)
  @ApiImplicitParam(name = "format", value = ParameterDescriptions.FORMAT, defaultValue = "",
      paramType = "query", dataType = "string", required = false)
  @RequestMapping(value = "/density/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response lengthDensityGroupByBoundary(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    AggregateRequestExecutor executor =
        new AggregateRequestExecutor(RequestResource.LENGTH, servletRequest, servletResponse, true);
    return executor.aggregateGroupByBoundary();
  }

  /**
   * Gives the density of OSM elements grouped by the boundary and the tag.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateGroupByBoundaryGroupByTag(RequestResource, HttpServletRequest,
   *         HttpServletResponse, boolean, boolean) aggregateGroupByBoundaryGroupByTag}
   */
  @ApiOperation(value = "Density of OSM elements grouped by the boundary and the tag",
      nickname = "lengthDensityGroupByBoundaryGroupByTag", response = GroupByResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.GROUP_BY_KEY,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/density/groupBy/boundary/groupBy/tag",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response lengthDensityGroupByBoundaryGroupByTag(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.aggregateGroupByBoundaryGroupByTag(RequestResource.LENGTH,
        servletRequest, servletResponse, true, true);
  }

  /**
   * Gives the density of OSM elements grouped by the tag.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateGroupByTag(RequestResource, HttpServletRequest, HttpServletResponse,
   *         boolean, boolean) aggregateGroupByTag}
   */
  @ApiOperation(value = "Density of OSM elements grouped by the tag",
      nickname = "lengthDensityGroupByTag", response = GroupByResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.GROUP_BY_KEY,
          defaultValue = DefaultSwaggerParameters.HIGHWAY_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/density/groupBy/tag", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response lengthDensityGroupByTag(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.aggregateGroupByTag(RequestResource.LENGTH, servletRequest,
        servletResponse, true, true);
  }

  /**
   * Gives the ratio of OSM elements satisfying filter2 within items selected by filter.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateRatio(RequestResource, HttpServletRequest, HttpServletResponse)
   *         aggregateRatio}
   */
  @ApiOperation(value = "Ratio of OSM elements satisfying filter2 within items selected by filter",
      nickname = "lengthRatio", response = RatioResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "filter", value = ParameterDescriptions.FILTER,
          defaultValue = DefaultSwaggerParameters.HIGHWAY_FILTER2, paramType = "query",
          dataType = "string", required = false),
      @ApiImplicitParam(name = "filter2", value = ParameterDescriptions.FILTER,
          defaultValue = DefaultSwaggerParameters.HIGHWAY_FILTER, paramType = "query",
          dataType = "string", required = false)})
  @RequestMapping(value = "/ratio", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response lengthRatio(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.aggregateRatio(RequestResource.LENGTH, servletRequest,
        servletResponse);
  }

  /**
   * Gives the ratio of the length of OSM elements satisfying filter2 within items selected by
   * filter grouped by the boundary.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateRatioGroupByBoundary(RequestResource, HttpServletRequest,
   *         HttpServletResponse) aggregateRatioGroupByBoundary}
   */
  @ApiOperation(value = "Ratio of the length of OSM elements grouped by the boundary",
      nickname = "lengthRatioGroupByBoundary", response = RatioGroupByBoundaryResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "filter", value = ParameterDescriptions.FILTER,
          defaultValue = DefaultSwaggerParameters.HIGHWAY_FILTER2, paramType = "query",
          dataType = "string", required = false),
      @ApiImplicitParam(name = "filter2", value = ParameterDescriptions.FILTER,
          defaultValue = DefaultSwaggerParameters.HIGHWAY_FILTER, paramType = "query",
          dataType = "string", required = false)})
  @RequestMapping(value = "/ratio/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response lengthRatioGroupByBoundary(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.aggregateRatioGroupByBoundary(RequestResource.LENGTH,
        servletRequest, servletResponse);
  }
}
