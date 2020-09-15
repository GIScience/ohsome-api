package org.heigit.ohsome.ohsomeapi.controller.dataaggregation;


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
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.RatioResponse;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.GroupByResponse;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.RatioGroupByBoundaryResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller containing the GET and POST servletRequest handling methods, which are mapped to
 * "/elements/area".
 */
@Api(tags = "Area")
@RestController
@RequestMapping("/elements/area")
public class AreaController {

  /**
   * Gives the area of OSM objects.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response}
   */
  @ApiOperation(value = "Area of OSM elements", nickname = "area",
      response = DefaultAggregationResponse.class)
  @RequestMapping(value = "", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response area(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
      throws Exception {
    AggregateRequestExecutor executor =
        new AggregateRequestExecutor(RequestResource.AREA, servletRequest, servletResponse, false);
    return executor.aggregate();
  }

  /**
   * Gives the area of OSM objects grouped by the OSM type.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response}
   */
  @ApiOperation(value = "Area of OSM elements grouped by the type", nickname = "areaGroupByType",
      response = GroupByResponse.class)
  @RequestMapping(value = "/groupBy/type", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response areaGroupByType(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.aggregateGroupByType(RequestResource.AREA, servletRequest,
        servletResponse, true, false);
  }

  /**
   * Gives the area OSM objects grouped by the boundary parameter (bounding box/circle/polygon).
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response}
   */
  @ApiOperation(
      value = "Area of OSM elements grouped by the boundary (bboxes, bcircles, or bpolys)",
      nickname = "areaGroupByBoundary", response = GroupByResponse.class)
  @RequestMapping(value = "/groupBy/boundary", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response areaGroupByBoundary(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    AggregateRequestExecutor executor =
        new AggregateRequestExecutor(RequestResource.AREA, servletRequest, servletResponse, false);
    return executor.aggregateGroupByBoundary();
  }

  /**
   * Gives the area of OSM objects grouped by the boundary and the tag.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response}
   */
  @ApiOperation(value = "Area of OSM elements grouped by the boundary and the tag",
      nickname = "areaGroupByBoundaryGroupByTag", response = GroupByResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.GROUP_BY_KEY,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/groupBy/boundary/groupBy/tag",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response areaGroupByBoundaryGroupByTag(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.aggregateGroupByBoundaryGroupByTag(RequestResource.AREA,
        servletRequest, servletResponse, true, false);
  }

  /**
   * Gives the area of OSM objects grouped by the key.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response}
   */
  @ApiOperation(value = "Area of OSM elements grouped by the key", nickname = "areaGroupByKey",
      response = GroupByResponse.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "groupByKeys", value = ParameterDescriptions.KEYS,
      defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
      dataType = "string", required = true)})
  @RequestMapping(value = "/groupBy/key", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response areaGroupByKey(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.aggregateGroupByKey(RequestResource.AREA, servletRequest,
        servletResponse, true, false);
  }

  /**
   * Gives the area of OSM objects grouped by the tag.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response }
   */
  @ApiOperation(value = "Area of OSM elements grouped by the tag", nickname = "areaGroupByTag",
      response = GroupByResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.GROUP_BY_KEY,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/groupBy/tag", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response areaGroupByTag(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.aggregateGroupByTag(RequestResource.AREA, servletRequest,
        servletResponse, true, false);
  }

  /**
   * Gives the density of OSM elements (area of items divided by the total area in
   * square-kilometers).
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response}
   */
  @ApiOperation(
      value = "Density of OSM elements (area of elements divided "
          + "by the total area in square-kilometers)",
      nickname = "areaDensity", response = DefaultAggregationResponse.class)
  @RequestMapping(value = "/density", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response areaDensity(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    AggregateRequestExecutor executor =
        new AggregateRequestExecutor(RequestResource.AREA, servletRequest, servletResponse, true);
    return executor.aggregate();
  }

  /**
   * Gives the density of OSM elements grouped by the OSM type.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response}
   */
  @ApiOperation(value = "Density of OSM elements grouped by the type",
      nickname = "areaDensityGroupByType", response = GroupByResponse.class)
  @RequestMapping(value = "/density/groupBy/type", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response areaDensityGroupByType(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.aggregateGroupByType(RequestResource.AREA, servletRequest,
        servletResponse, true, true);
  }

  /**
   * Gives the density of OSM elements grouped by the boundary parameter (bounding
   * box/circle/polygon).
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response}
   */
  @ApiOperation(
      value = "Density of OSM elements grouped by the boundary (bboxes, bcircles, or bpolys)",
      nickname = "areaDensityGroupByBoundary", response = GroupByResponse.class)
  @RequestMapping(value = "/density/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response areaDensityGroupByBoundary(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    AggregateRequestExecutor executor =
        new AggregateRequestExecutor(RequestResource.AREA, servletRequest, servletResponse, true);
    return executor.aggregateGroupByBoundary();
  }

  /**
   * Gives the density of OSM elements grouped by the boundary and the tag.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response}
   */
  @ApiOperation(value = "Density of OSM elements grouped by the boundary and the tag",
      nickname = "areaDensityGroupByBoundaryGroupByTag", response = GroupByResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.GROUP_BY_KEY,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/density/groupBy/boundary/groupBy/tag",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response areaDensityGroupByBoundaryGroupByTag(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.aggregateGroupByBoundaryGroupByTag(RequestResource.AREA,
        servletRequest, servletResponse, true, true);
  }

  /**
   * Gives the density of OSM elements grouped by the tag.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response}
   */
  @ApiOperation(value = "Density of OSM elements grouped by the tag",
      nickname = "areaDensityGroupByTag", response = GroupByResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.GROUP_BY_KEY,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/density/groupBy/tag", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response areaDensityGroupByTag(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.aggregateGroupByTag(RequestResource.AREA, servletRequest,
        servletResponse, true, true);
  }

  /**
   * Gives the ratio of OSM elements satisfying filter2 within items selected by filter.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response}
   */
  @ApiOperation(
      value = "Ratio of the area of OSM elements satisfying filter2 within items selected by "
          + "filter",
      nickname = "areaRatio", response = RatioResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "types2", value = ParameterDescriptions.DEPRECATED_USE_FILTER2,
          defaultValue = "", paramType = "query", dataType = "string", required = false),
      @ApiImplicitParam(name = "keys2", value = ParameterDescriptions.DEPRECATED_USE_FILTER2,
          defaultValue = "", paramType = "query", dataType = "string", required = false),
      @ApiImplicitParam(name = "values2", value = ParameterDescriptions.DEPRECATED_USE_FILTER2,
          defaultValue = "", paramType = "query", dataType = "string", required = false),
      @ApiImplicitParam(name = "filter2", value = ParameterDescriptions.FILTER,
          defaultValue = "type:relation and " + DefaultSwaggerParameters.BUILDING_FILTER,
          paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/ratio", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response areaRatio(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
      throws Exception {
    return ElementsRequestExecutor.aggregateRatio(RequestResource.AREA, servletRequest,
        servletResponse);
  }

  /**
   * Gives the ratio of the area of OSM elements satisfying filter2 within items selected by filter
   * grouped by the boundary.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response}
   */
  @ApiOperation(value = "Ratio of the area of OSM elements grouped by the boundary",
      nickname = "areaRatioGroupByBoundary", response = RatioGroupByBoundaryResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "types2", value = ParameterDescriptions.DEPRECATED_USE_FILTER2,
          defaultValue = "", paramType = "query", dataType = "string", required = false),
      @ApiImplicitParam(name = "keys2", value = ParameterDescriptions.DEPRECATED_USE_FILTER2,
          defaultValue = "", paramType = "query", dataType = "string", required = false),
      @ApiImplicitParam(name = "values2", value = ParameterDescriptions.DEPRECATED_USE_FILTER2,
          defaultValue = "", paramType = "query", dataType = "string", required = false),
      @ApiImplicitParam(name = "filter2", value = ParameterDescriptions.FILTER,
          defaultValue = DefaultSwaggerParameters.BUILDING_FILTER, paramType = "query",
          dataType = "string", required = false)})
  @RequestMapping(value = "/ratio/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response areaRatioGroupByBoundary(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.aggregateRatioGroupByBoundary(RequestResource.AREA,
        servletRequest, servletResponse);
  }
}
