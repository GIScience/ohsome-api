package org.heigit.ohsome.ohsomeapi.controller.dataaggregation.elements;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.SortedMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.ohsome.ohsomeapi.controller.DefaultSwaggerParameters;
import org.heigit.ohsome.ohsomeapi.controller.ParameterDescriptions;
import org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils.MatchType;
import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResponse;
import org.heigit.ohsome.ohsomeapi.output.ratio.RatioGroupByBoundaryResponse;
import org.heigit.ohsome.ohsomeapi.output.ratio.RatioResponse;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.Area;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.GroupByBoundary;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.GroupByBoundaryGroupByTag;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.GroupByKey;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.GroupByTag;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.GroupByType;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.Ratio;
import org.heigit.ohsome.oshdb.OSHDBTimestamp;
import org.heigit.ohsome.oshdb.api.generic.OSHDBCombinedIndex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller containing the GET and POST servletRequest handling methods, which are mapped to
 * "/elements/area".
 */
@Api(tags = "Elements Area")
@RestController
@RequestMapping("/elements/area")
public class AreaController {

  @Autowired
  private Area area;
  @Autowired
  private GroupByBoundary groupByBoundary;
  @Autowired
  private GroupByType groupByType;
  @Autowired
  private GroupByKey groupByKey;
  @Autowired
  private GroupByTag groupByTag;
  @Autowired
  private GroupByBoundaryGroupByTag groupByBoundaryGroupByTag;
  @Autowired
  private Ratio ratio;

  /**
   * Gives the area of OSM objects.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link
   *         org.heigit.ohsome.ohsomeapi.executor.AggregateRequestExecutor#aggregate() aggregate}
   */
  @ApiOperation(value = "Area of OSM elements", nickname = "area",
      response = DefaultAggregationResponse.class)
  @RequestMapping(value = "", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response area() throws Exception {
    var result = area.compute();
    return area.getResponse(result);
  }

  /**
   * Gives the area of OSM objects grouped by the OSM type.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateGroupByType(RequestResource, HttpServletRequest, HttpServletResponse,
   *         boolean, boolean) aggregateGroupByType}
   */
  @ApiOperation(value = "Area of OSM elements grouped by the type", nickname = "areaGroupByType",
      response = GroupByResponse.class)
  @ApiImplicitParam(name = "filter", value = ParameterDescriptions.FILTER,
      defaultValue = DefaultSwaggerParameters.BUILDING_FILTER, paramType = "query",
      dataType = "string", required = false)
  @RequestMapping(value = "/groupBy/type", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response areaGroupByType() throws Exception {
    var mapAggregator = groupByType.compute();
    var areaResult = area.getAreaGroupByResult(mapAggregator);
    var result = groupByType.getResult(areaResult);
    return groupByType.getResponse(result);
  }

  /**
   * Gives the area OSM objects grouped by the boundary parameter (bounding box/circle/polygon).
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link
   *         org.heigit.ohsome.ohsomeapi.executor.AggregateRequestExecutor
   *         #aggregateGroupByBoundary() aggregateGroupByBoundary}
   */
  @ApiOperation(
      value = "Area of OSM elements grouped by the boundary (bboxes, bcircles, or bpolys)",
      nickname = "areaGroupByBoundary", response = GroupByResponse.class)
  @RequestMapping(value = "/groupBy/boundary", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response areaGroupByBoundary() throws Exception {
    var mapAggregator = groupByBoundary.compute();
    var areaResult = area.getAreaGroupByResult(mapAggregator);
    var result = groupByBoundary.getResult(areaResult);
    return groupByBoundary.getResponse(result);
  }

  /**
   * Gives the area of OSM objects grouped by the boundary and the tag.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateGroupByBoundaryGroupByTag(RequestResource, HttpServletRequest,
   *         HttpServletResponse, boolean, boolean) aggregateGroupByBoundaryGroupByTag}
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
    var mapAggregator = groupByBoundaryGroupByTag.compute();
    var areaResult = area.getAreaGroupByBoundaryByTagResult(mapAggregator);
    var result = groupByBoundaryGroupByTag.getResult(areaResult);
    return groupByBoundaryGroupByTag.getResponse(result);
  }

  /**
   * Gives the area of OSM objects grouped by the key.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateGroupByKey(RequestResource, HttpServletRequest, HttpServletResponse, boolean,
   *         boolean) aggregateGroupByKey}
   */
  @ApiOperation(value = "Area of OSM elements grouped by the key", nickname = "areaGroupByKey",
      response = GroupByResponse.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "groupByKeys", value = ParameterDescriptions.KEYS,
      defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
      dataType = "string", required = true)})
  @RequestMapping(value = "/groupBy/key", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response areaGroupByKey() throws Exception {
    var mapAggregator = groupByKey.compute();
    var areaResult = area.getAreaGroupByResult(mapAggregator);
    var result = groupByKey.getResult(areaResult);
    return groupByKey.getResponse(result);
  }

  /**
   * Gives the area of OSM objects grouped by the tag.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response }
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateGroupByTag(RequestResource, HttpServletRequest, HttpServletResponse, boolean,
   *         boolean) aggregateGroupByTag}
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
  public Response areaGroupByTag() throws Exception {
    var mapAggregator = groupByTag.compute();
    var areaResult = area.getAreaGroupByResult(mapAggregator);
    var result = groupByTag.getResult(areaResult);
    return groupByTag.getResponse(result);
  }

  /**
   * Gives the density of OSM elements (area of items divided by the total area in
   * square-kilometers).
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link
   *         org.heigit.ohsome.ohsomeapi.executor.AggregateRequestExecutor#aggregate() aggregate}
   */
//  @ApiOperation(
//      value = "Density of OSM elements (area of elements divided "
//          + "by the total area in square-kilometers)",
//      nickname = "areaDensity", response = DefaultAggregationResponse.class)
//  @RequestMapping(value = "/density", method = {RequestMethod.GET, RequestMethod.POST},
//      produces = {"application/json", "text/csv"})
//  public Response areaDensity(HttpServletRequest servletRequest,
//      HttpServletResponse servletResponse) throws Exception {
////    AggregateRequestExecutor executor =
////        new AggregateRequestExecutor(RequestResource.AREA, servletRequest, servletResponse, true);
//    return aggregateRequestExecutor.aggregate(area);
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
//      nickname = "areaDensityGroupByType", response = GroupByResponse.class)
//  @ApiImplicitParam(name = "filter", value = ParameterDescriptions.FILTER,
//      defaultValue = DefaultSwaggerParameters.BUILDING_FILTER, paramType = "query",
//      dataType = "string", required = false)
//  @RequestMapping(value = "/density/groupBy/type", method = {RequestMethod.GET, RequestMethod.POST},
//      produces = {"application/json", "text/csv"})
//  public Response areaDensityGroupByType(HttpServletRequest servletRequest,
//      HttpServletResponse servletResponse) throws Exception {
//   // ElementsRequestExecutor executor = new ElementsRequestExecutor();
//    return elementsRequestExecutor.aggregateGroupByType(area, servletRequest,
//        servletResponse, true, true);
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
//      nickname = "areaDensityGroupByBoundary", response = GroupByResponse.class)
//  @RequestMapping(value = "/density/groupBy/boundary",
//      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
//  public Response areaDensityGroupByBoundary(HttpServletRequest servletRequest,
//      HttpServletResponse servletResponse) throws Exception {
//  Operator operator = new Operator();
//  operator.setOperation(area);
//  operator.setOperation(density);
//  operator.setOperation(groupByBoundary);
//  return operator.operate();
//  //    AggregateRequestExecutor executor =
////        new AggregateRequestExecutor(RequestResource.AREA, servletRequest, servletResponse, true);
//    //return aggregateRequestExecutor.aggregateGroupByBoundary(groupBy);
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
//  @ApiOperation(value = "Density of OSM elements grouped by the boundary and the tag",
//      nickname = "areaDensityGroupByBoundaryGroupByTag", response = GroupByResponse.class)
//  @ApiImplicitParams({
//      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.GROUP_BY_KEY,
//          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
//          dataType = "string", required = true),
//      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES,
//          defaultValue = "", paramType = "query", dataType = "string", required = false)})
//  @RequestMapping(value = "/density/groupBy/boundary/groupBy/tag",
//      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
//  public Response areaDensityGroupByBoundaryGroupByTag(HttpServletRequest servletRequest,
//      HttpServletResponse servletResponse) throws Exception {
//    //ElementsRequestExecutor executor = new ElementsRequestExecutor();
//    return elementsRequestExecutor.aggregateGroupByBoundaryGroupByTag(area,
//        servletRequest, servletResponse, true, true);
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
//      nickname = "areaDensityGroupByTag", response = GroupByResponse.class)
//  @ApiImplicitParams({
//      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.GROUP_BY_KEY,
//          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
//          dataType = "string", required = true),
//      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES,
//          defaultValue = "", paramType = "query", dataType = "string", required = false)})
//  @RequestMapping(value = "/density/groupBy/tag", method = {RequestMethod.GET, RequestMethod.POST},
//      produces = {"application/json", "text/csv"})
//  public Response areaDensityGroupByTag(HttpServletRequest servletRequest,
//      HttpServletResponse servletResponse) throws Exception {
//    //ElementsRequestExecutor executor = new ElementsRequestExecutor();
//    return elementsRequestExecutor.aggregateGroupByTag(area, servletRequest,
//        servletResponse, true, true);
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
  @ApiOperation(
      value = "Ratio of the area of OSM elements satisfying filter2 within items selected by "
          + "filter",
      nickname = "areaRatio", response = RatioResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "filter", value = ParameterDescriptions.FILTER,
          defaultValue = DefaultSwaggerParameters.BUILDING_FILTER, paramType = "query",
          dataType = "string", required = false),
      @ApiImplicitParam(name = "filter2", value = ParameterDescriptions.FILTER,
          defaultValue = DefaultSwaggerParameters.BUILDING_FILTER2,
          paramType = "query", dataType = "string", required = true)})
  @RequestMapping(value = "/ratio", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response areaRatio() throws Exception {
    var mapReducer = ratio.compute();
    var mapAggregator = ratio.aggregateByFilterMatching(mapReducer.aggregateByTimestamp());
    var areaResult = area.getAreaResult(mapAggregator);
    var values = ratio.getValues(
        (SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, MatchType>, ? extends Number>) areaResult);
    var result = ratio.getRatioResult(values);
    return ratio.getResponse(result);
  }

  /**
   * Gives the ratio of the area of OSM elements satisfying filter2 within items selected by filter
   * grouped by the boundary.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *     #aggregateRatioGroupByBoundary(RequestResource, HttpServletRequest,
   *     HttpServletResponse) aggregateRatioGroupByBoundary}
   */
  @ApiOperation(value = "Ratio of the area of OSM elements grouped by the boundary",
      nickname = "areaRatioGroupByBoundary", response = RatioGroupByBoundaryResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "filter", value = ParameterDescriptions.FILTER,
          defaultValue = DefaultSwaggerParameters.BUILDING_FILTER, paramType = "query",
          dataType = "string", required = false),
      @ApiImplicitParam(name = "filter2", value = ParameterDescriptions.FILTER,
          defaultValue = DefaultSwaggerParameters.BUILDING_FILTER2, paramType = "query",
          dataType = "string", required = true)})
  @RequestMapping(value = "/ratio/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response areaRatioGroupByBoundary() throws Exception {
    var mapReducer = ratio.compute();
    var mapAggregator = groupByBoundary.aggregate(mapReducer);
    var mapAggregatorEntitiesByFilterMatched = ratio.aggregateByFilterMatching(mapAggregator);
    var areaResult = area.getAreaGroupByResult(mapAggregatorEntitiesByFilterMatched);
    var ratioResult = ratio.getValues(areaResult);
    var groupByResult = ratio.getRatioGroupByResult(ratioResult);
    return ratio.getResponse(groupByResult);
  }
}
