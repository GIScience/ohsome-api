package org.heigit.ohsome.ohsomeapi.controller.dataaggregation;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.ohsome.ohsomeapi.controller.DefaultSwaggerParameters;
import org.heigit.ohsome.ohsomeapi.controller.ParameterDescriptions;
import org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor;
import org.heigit.ohsome.ohsomeapi.executor.RequestResource;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.RatioResponse;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.GroupByResponse;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.RatioGroupByBoundaryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * Controller containing the GET and POST servletRequest handling methods, which are mapped to
 * "/elements/perimeter".
 */
@Api(tags = "Perimeter")
@RestController
@RequestMapping("/elements/perimeter")
public class PerimeterController {

  @Autowired
  private ElementsRequestExecutor elementsRequestExecutor;
  
  public void setElementsRequestExecutor(ElementsRequestExecutor elementsRequestExecutor) {
    this.elementsRequestExecutor = elementsRequestExecutor;
  }
  
  /**
   * Gives the perimeter of polygonal OSM objects.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Perimeter of OSM elements", nickname = "perimeter",
      response = DefaultAggregationResponse.class)
  @RequestMapping(value = "", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response perimeter(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
      throws Exception {
    return elementsRequestExecutor.aggregate(RequestResource.PERIMETER, servletRequest,
        servletResponse, true, false);
  }

  /**
   * Gives the perimeter of polygonal OSM objects grouped by the OSM type.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Perimeter of OSM elements grouped by the type",
      nickname = "perimeterGroupByType", response = GroupByResponse.class)
  @RequestMapping(value = "/groupBy/type", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response perimeterGroupByType(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return elementsRequestExecutor.aggregateGroupByType(RequestResource.PERIMETER, servletRequest,
        servletResponse, true, false);
  }

  /**
   * Gives the perimeter of polygonal OSM objects grouped by the boundary parameter (bounding
   * box/circle/polygon).
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(
      value = "Perimeter of OSM elements in grouped by the boundary "
          + "(bboxes, bcircles, or bpolys)",
      nickname = "perimeterGroupByBoundary", response = GroupByResponse.class)
  @RequestMapping(value = "/groupBy/boundary", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response perimeterGroupByBoundary(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return elementsRequestExecutor.aggregateGroupByBoundary(RequestResource.PERIMETER,
        servletRequest, servletResponse, true, false);
  }

  /**
   * Gives the perimeter of polygonal OSM objects grouped by the boundary and the tag.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Perimeter of OSM elements grouped by the boundary and the tag",
      nickname = "perimeterGroupByBoundaryGroupByTag", response = GroupByResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.GROUP_BY_KEY,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/groupBy/boundary/groupBy/tag",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response countGroupByBoundaryGroupByTag(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return elementsRequestExecutor.aggregateGroupByBoundaryGroupByTag(RequestResource.PERIMETER,
        servletRequest, servletResponse, true, false);
  }

  /**
   * Gives the perimeter of polygonal OSM objects grouped by the key.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Perimeter of OSM elements grouped by the key",
      nickname = "perimeterGroupByKey", response = GroupByResponse.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "groupByKeys", value = ParameterDescriptions.KEYS,
      defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
      dataType = "string", required = true)})
  @RequestMapping(value = "/groupBy/key", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response perimeterGroupByKey(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return elementsRequestExecutor.aggregateGroupByKey(RequestResource.PERIMETER, servletRequest,
        servletResponse, true, false);
  }

  /**
   * Gives the perimeter of polygonal OSM objects grouped by the tag.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Perimeter of OSM elements grouped by the tag",
      nickname = "perimeterGroupByTag", response = GroupByResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.GROUP_BY_KEY,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/groupBy/tag", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response perimeterGroupByTag(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return elementsRequestExecutor.aggregateGroupByTag(RequestResource.PERIMETER, servletRequest,
        servletResponse, true, false);
  }

  /**
   * Gives the density of OSM elements (perimeter of items divided by the total area in
   * square-kilometers).
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(
      value = "Density of OSM elements (perimeter of elements divided by "
          + "the total area in square-kilometers)",
      nickname = "perimeterDensity", response = DefaultAggregationResponse.class)
  @RequestMapping(value = "/density", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response perimeterDensity(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {

    return elementsRequestExecutor.aggregate(RequestResource.PERIMETER, servletRequest,
        servletResponse, true, true);
  }

  /**
   * Gives the density of OSM elements grouped by the OSM type.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Density of OSM elements grouped by the type",
      nickname = "perimeterDensityGroupByType", response = GroupByResponse.class)
  @RequestMapping(value = "density/groupBy/type", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response perimeterDensityGroupByType(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return elementsRequestExecutor.aggregateGroupByType(RequestResource.PERIMETER, servletRequest,
        servletResponse, true, true);
  }

  /**
   * Gives the density of OSM elements grouped by the boundary parameter (bounding
   * box/circle/polygon).
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(
      value = "Density of OSM elements grouped by the boundary (bboxes, bcircles, or bpolys)",
      nickname = "perimeterDensityGroupByBoundary", response = GroupByResponse.class)
  @RequestMapping(value = "/density/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response perimeterDensityGroupByBoundary(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return elementsRequestExecutor.aggregateGroupByBoundary(RequestResource.PERIMETER,
        servletRequest, servletResponse, true, true);
  }

  /**
   * Gives the density of OSM elements grouped by the boundary and the tag.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Density of  grouped by the boundary and the tag",
      nickname = "perimeterDensityGroupByBoundaryGroupByTag", response = GroupByResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.GROUP_BY_KEY,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/density/groupBy/boundary/groupBy/tag",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response countDensityGroupByBoundaryGroupByTag(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return elementsRequestExecutor.aggregateGroupByBoundaryGroupByTag(RequestResource.PERIMETER,
        servletRequest, servletResponse, true, true);
  }

  /**
   * Gives the density of OSM elements grouped by the tag.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Density of OSM elements grouped by the tag",
      nickname = "perimeterDensityGroupByTag", response = GroupByResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.GROUP_BY_KEY,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/density/groupBy/tag", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response perimeterDensityGroupByTag(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return elementsRequestExecutor.aggregateGroupByTag(RequestResource.PERIMETER, servletRequest,
        servletResponse, true, true);
  }

  /**
   * Gives the ratio of OSM elements satisfying types2, keys2 and values2 (or filter2) within items
   * selected by types, keys and values (or filter).
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(
      value = "Ratio of OSM elements satisfying types2, keys2 and values2 (or filter2) within items "
          + "selected by types, keys and values (or filter)",
      nickname = "perimeterRatio", response = RatioResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "types2", value = ParameterDescriptions.TYPES,
          defaultValue = DefaultSwaggerParameters.TYPE, paramType = "query", dataType = "string",
          required = false),
      @ApiImplicitParam(name = "keys2", value = ParameterDescriptions.KEYS,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = false),
      @ApiImplicitParam(name = "values2", value = ParameterDescriptions.VALUES,
          defaultValue = DefaultSwaggerParameters.RESIDENTIAL_VALUE, paramType = "query",
          dataType = "string", required = false),
      @ApiImplicitParam(name = "filter2", value = ParameterDescriptions.FILTER, defaultValue = "",
          paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/ratio", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response perimeterRatio(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return elementsRequestExecutor.aggregateRatio(RequestResource.PERIMETER, servletRequest,
        servletResponse);
  }

  /**
   * Gives the ratio of the perimeter of OSM elements satisfying types2, keys2 and values2 (or
   * filter2) within items selected by types, keys and values (or filter) grouped by the boundary.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Ratio of the perimeter of OSM elements grouped by the boundary",
      nickname = "perimeterRatioGroupByBoundary", response = RatioGroupByBoundaryResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "types2", value = ParameterDescriptions.TYPES,
          defaultValue = DefaultSwaggerParameters.TYPE, paramType = "query", dataType = "string",
          required = false),
      @ApiImplicitParam(name = "keys2", value = ParameterDescriptions.KEYS,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = false),
      @ApiImplicitParam(name = "values2", value = ParameterDescriptions.VALUES, defaultValue = "",
          paramType = "query", dataType = "string", required = false),
      @ApiImplicitParam(name = "filter2", value = ParameterDescriptions.FILTER, defaultValue = "",
          paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/ratio/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response perimeterRatioGroupByBoundary(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return elementsRequestExecutor.aggregateRatioGroupByBoundary(RequestResource.PERIMETER,
        servletRequest, servletResponse);
  }
}
