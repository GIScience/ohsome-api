package org.heigit.ohsome.ohsomeapi.controller.dataaggregation.elements;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.heigit.ohsome.ohsomeapi.controller.DefaultSwaggerParameters;
import org.heigit.ohsome.ohsomeapi.controller.ParameterDescriptions;
import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResponse;
import org.heigit.ohsome.ohsomeapi.output.ratio.RatioGroupByBoundaryResponse;
import org.heigit.ohsome.ohsomeapi.output.ratio.RatioResponse;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.GroupByBoundary;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.GroupByBoundaryGroupByTag;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.GroupByKey;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.GroupByTag;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.GroupByType;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.Perimeter;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.Ratio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller containing the GET and POST servletRequest handling methods, which are mapped to
 * "/elements/perimeter".
 */
@Api(tags = "Elements Perimeter")
@RestController
@RequestMapping("/elements/perimeter")
public class PerimeterController {

  @Autowired
  private Perimeter perimeter;
  @Autowired
  private Ratio ratio;
  @Autowired
  private GroupByBoundary groupByBoundary;
  @Autowired
  private GroupByTag groupByTag;
  @Autowired
  private GroupByType groupByType;
  @Autowired
  private GroupByKey groupByKey;
  @Autowired
  private GroupByBoundaryGroupByTag groupByBoundaryGroupByTag;

  /**
   * Gives the perimeter of polygonal OSM objects.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link
   *         org.heigit.ohsome.ohsomeapi.executor.AggregateRequestExecutor#aggregate() aggregate}
   */
  @ApiOperation(value = "Perimeter of OSM elements", nickname = "perimeter",
      response = DefaultAggregationResponse.class)
  @RequestMapping(value = "", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response perimeter() throws Exception {
    var result = perimeter.compute();
    return perimeter.getResponse(result);
  }

  /**
   * Gives the perimeter of polygonal OSM objects grouped by the OSM type.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateGroupByType(RequestResource, HttpServletRequest, HttpServletResponse,
   *         boolean, boolean) aggregateGroupByType}
   */
  @ApiOperation(value = "Perimeter of OSM elements grouped by the type",
      nickname = "perimeterGroupByType", response = GroupByResponse.class)
  @ApiImplicitParam(name = "filter", value = ParameterDescriptions.FILTER,
      defaultValue = DefaultSwaggerParameters.BUILDING_FILTER, paramType = "query",
      dataType = "string", required = false)
  @RequestMapping(value = "/groupBy/type", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response perimeterGroupByType() throws Exception {
    var mapAggregator = groupByType.compute();
    var perimeterResult = perimeter.getPerimeterGroupByResult(mapAggregator);
    var result = groupByType.getResult(perimeterResult);
    return groupByType.getResponse(result);
  }

  /**
   * Gives the perimeter of polygonal OSM objects grouped by the boundary parameter (bounding
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
      value = "Perimeter of OSM elements in grouped by the boundary "
          + "(bboxes, bcircles, or bpolys)",
      nickname = "perimeterGroupByBoundary", response = GroupByResponse.class)
  @RequestMapping(value = "/groupBy/boundary", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response perimeterGroupByBoundary() throws Exception {
    var mapAggregator = groupByBoundary.compute();
    var perimeterResult = perimeter.getPerimeterGroupByResult(mapAggregator);
    var result = groupByBoundary.getResult(perimeterResult);
    return groupByBoundary.getResponse(result);
  }

  /**
   * Gives the perimeter of polygonal OSM objects grouped by the boundary and the tag.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateGroupByBoundaryGroupByTag(RequestResource, HttpServletRequest,
   *         HttpServletResponse, boolean, boolean) aggregateGroupByBoundaryGroupByTag}
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
  public Response perimeterGroupByBoundaryGroupByTag() throws Exception {
    var mapAggregator = groupByBoundaryGroupByTag.compute();
    var perimeterResult = perimeter.getPerimeterGroupByBoundaryGroupByTagResult(mapAggregator);
    var result = groupByBoundaryGroupByTag.getResult(perimeterResult);
    return groupByBoundaryGroupByTag.getResponse(result);
  }

  /**
   * Gives the perimeter of polygonal OSM objects grouped by the key.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateGroupByKey(RequestResource, HttpServletRequest, HttpServletResponse,
   *         boolean, boolean) aggregateGroupByKey}
   */
  @ApiOperation(value = "Perimeter of OSM elements grouped by the key",
      nickname = "perimeterGroupByKey", response = GroupByResponse.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "groupByKeys", value = ParameterDescriptions.KEYS,
      defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
      dataType = "string", required = true)})
  @RequestMapping(value = "/groupBy/key", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response perimeterGroupByKey() throws Exception {
    var mapAggregator = groupByKey.compute();
    var perimeterResult = perimeter.getPerimeterGroupByResult(mapAggregator);
    var result = groupByKey.getResult(perimeterResult);
    return groupByKey.getResponse(result);
  }

  /**
   * Gives the perimeter of polygonal OSM objects grouped by the tag.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateGroupByTag(RequestResource, HttpServletRequest, HttpServletResponse,
   *         boolean, boolean) aggregateGroupByTag}
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
  public Response perimeterGroupByTag() throws Exception {
    var mapAggregator = groupByTag.compute();
    var perimeterResult = perimeter.getPerimeterGroupByResult(mapAggregator);
    var result = groupByTag.getResult(perimeterResult);
    return groupByTag.getResponse(result);
  }

  /**
   * Gives the density of OSM elements (perimeter of items divided by the total area in
   * square-kilometers).
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link
   *         org.heigit.ohsome.ohsomeapi.executor.AggregateRequestExecutor#aggregate() aggregate}
   */
//  @ApiOperation(
//      value = "Density of OSM elements (perimeter of elements divided by "
//          + "the total area in square-kilometers)",
//      nickname = "perimeterDensity", response = DefaultAggregationResponse.class)
//  @RequestMapping(value = "/density", method = {RequestMethod.GET, RequestMethod.POST},
//      produces = {"application/json", "text/csv"})
//  public Response perimeterDensity(HttpServletRequest servletRequest,
//      HttpServletResponse servletResponse) throws Exception {
////    AggregateRequestExecutor executor = new AggregateRequestExecutor(RequestResource.PERIMETER,
////        servletRequest, servletResponse, true);
//    operator.setOperation(perimeter);
//    operator.setOperation(density);
//    return operator.compute();
//    //return aggregateRequestExecutor.aggregate();
//  }

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
//  @ApiOperation(value = "Density of OSM elements grouped by the type",
//      nickname = "perimeterDensityGroupByType", response = GroupByResponse.class)
//  @ApiImplicitParam(name = "filter", value = ParameterDescriptions.FILTER,
//      defaultValue = DefaultSwaggerParameters.BUILDING_FILTER, paramType = "query",
//      dataType = "string", required = false)
//  @RequestMapping(value = "density/groupBy/type", method = {RequestMethod.GET, RequestMethod.POST},
//      produces = {"application/json", "text/csv"})
//  public Response perimeterDensityGroupByType(HttpServletRequest servletRequest,
//      HttpServletResponse servletResponse) throws Exception {
//    //ElementsRequestExecutor executor = new ElementsRequestExecutor();
//    operator.setOperation(perimeter);
//    operator.setOperation(density);
//    operator.setOperation(groupByType);
//    return operator.compute();
////    return elementsRequestExecutor.aggregateGroupByType(RequestResource.PERIMETER, servletRequest,
////        servletResponse, true, true);
//  }

  /**
   * Gives the density of OSM elements grouped by the boundary parameter (bounding
   * box/circle/polygon).
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link
   *         org.heigit.ohsome.ohsomeapi.executor.AggregateRequestExecutor
   *         #aggregateGroupByBoundary() aggregateGroupByBoundary}
   */
//  @ApiOperation(
//      value = "Density of OSM elements grouped by the boundary (bboxes, bcircles, or bpolys)",
//      nickname = "perimeterDensityGroupByBoundary", response = GroupByResponse.class)
//  @RequestMapping(value = "/density/groupBy/boundary",
//      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
//  public Response perimeterDensityGroupByBoundary(HttpServletRequest servletRequest,
//      HttpServletResponse servletResponse) throws Exception {
////    AggregateRequestExecutor executor = new AggregateRequestExecutor(RequestResource.PERIMETER,
////        servletRequest, servletResponse, true);
//    operator.setOperation(perimeter);
//    operator.setOperation(density);
//    operator.setOperation(groupByBoundary);
//    return operator.compute();
//    //return aggregateRequestExecutor.aggregateGroupByBoundary();
//  }

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
//  @ApiOperation(value = "Density of  grouped by the boundary and the tag",
//      nickname = "perimeterDensityGroupByBoundaryGroupByTag", response = GroupByResponse.class)
//  @ApiImplicitParams({
//      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.GROUP_BY_KEY,
//          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
//          dataType = "string", required = true),
//      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES,
//          defaultValue = "", paramType = "query", dataType = "string", required = false)})
//  @RequestMapping(value = "/density/groupBy/boundary/groupBy/tag",
//      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
//  public Response perimeterDensityGroupByBoundaryGroupByTag(HttpServletRequest servletRequest,
//      HttpServletResponse servletResponse) throws Exception {
//    //ElementsRequestExecutor executor = new ElementsRequestExecutor();
//    operator.setOperation(perimeter);
//    operator.setOperation(density);
//    operator.setOperation(groupByBoundary);
//    operator.setOperation(groupByTag);
//    return operator.compute();
////    return elementsRequestExecutor.aggregateGroupByBoundaryGroupByTag(RequestResource.PERIMETER,
////        servletRequest, servletResponse, true, true);
//  }

  /**
   * Gives the density of OSM elements grouped by the tag.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateGroupByTag(RequestResource, HttpServletRequest, HttpServletResponse, boolean,
   *         boolean) aggregateGroupByTag}
   */
//  @ApiOperation(value = "Density of OSM elements grouped by the tag",
//      nickname = "perimeterDensityGroupByTag", response = GroupByResponse.class)
//  @ApiImplicitParams({
//      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.GROUP_BY_KEY,
//          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
//          dataType = "string", required = true),
//      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES,
//          defaultValue = "", paramType = "query", dataType = "string", required = false)})
//  @RequestMapping(value = "/density/groupBy/tag", method = {RequestMethod.GET, RequestMethod.POST},
//      produces = {"application/json", "text/csv"})
//  public Response perimeterDensityGroupByTag(HttpServletRequest servletRequest,
//      HttpServletResponse servletResponse) throws Exception {
//    //ElementsRequestExecutor executor = new ElementsRequestExecutor();
//    operator.setOperation(perimeter);
//    operator.setOperation(density);
//    operator.setOperation(groupByTag);
//    return operator.compute();
////    return elementsRequestExecutor.aggregateGroupByTag(RequestResource.PERIMETER, servletRequest,
////        servletResponse, true, true);
//  }

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
      nickname = "perimeterRatio", response = RatioResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "filter", value = ParameterDescriptions.FILTER,
          defaultValue = DefaultSwaggerParameters.BUILDING_FILTER, paramType = "query",
          dataType = "string", required = false),
      @ApiImplicitParam(name = "filter2", value = ParameterDescriptions.FILTER,
          defaultValue = DefaultSwaggerParameters.BUILDING_FILTER2, paramType = "query",
          dataType = "string", required = true)})
  @RequestMapping(value = "/ratio", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response perimeterRatio() throws Exception {
    var mapReducer = ratio.compute();
    var mapAggregator = ratio.aggregateByFilterMatching(mapReducer.aggregateByTimestamp());
    var perimeterResult = perimeter.getPerimeterResult(mapAggregator);
    var values = ratio.getValues(perimeterResult);
    var result = ratio.getRatioResult(values);
    return ratio.getResponse(result);
  }

  /**
   * Gives the ratio of the perimeter of OSM elements satisfying filter2 within items selected by
   * filter grouped by the boundary.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateRatioGroupByBoundary(RequestResource, HttpServletRequest,
   *         HttpServletResponse) aggregateRatioGroupByBoundary}
   */
  @ApiOperation(value = "Ratio of the perimeter of OSM elements grouped by the boundary",
      nickname = "perimeterRatioGroupByBoundary", response = RatioGroupByBoundaryResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "filter", value = ParameterDescriptions.FILTER,
          defaultValue = DefaultSwaggerParameters.BUILDING_FILTER, paramType = "query",
          dataType = "string", required = false),
      @ApiImplicitParam(name = "filter2", value = ParameterDescriptions.FILTER,
          defaultValue = DefaultSwaggerParameters.BUILDING_FILTER2, paramType = "query",
          dataType = "string", required = true)})
  @RequestMapping(value = "/ratio/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response perimeterRatioGroupByBoundary() throws Exception {
    var mapReducer = ratio.compute();
    var mapAggregator = groupByBoundary.aggregate(mapReducer);
    var mapAggregatorEntitiesByFilterMatched = ratio.aggregateByFilterMatching(mapAggregator);
    var perimeterResult = perimeter.getPerimeterGroupByResult(mapAggregatorEntitiesByFilterMatched);
    var values = ratio.getValues(perimeterResult);
    var result = ratio.getRatioGroupByResult(values);
    return ratio.getResponse(result);
  }
}
