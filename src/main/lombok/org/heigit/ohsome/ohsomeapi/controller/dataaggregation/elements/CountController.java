package org.heigit.ohsome.ohsomeapi.controller.dataaggregation.elements;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.SortedMap;
import org.heigit.ohsome.ohsomeapi.controller.DefaultSwaggerParameters;
import org.heigit.ohsome.ohsomeapi.controller.ParameterDescriptions;
import org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils.MatchType;
import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResponse;
import org.heigit.ohsome.ohsomeapi.output.ratio.RatioGroupByBoundaryResponse;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operator;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.Count;
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
 * Controller containing the GET and POST request handling methods, which are mapped to
 * "/elements/count".
 */
@Api(tags = "Elements Count")
@RestController
@RequestMapping("/elements/count")
public class CountController {

  @Autowired
  private Operator operator;
  @Autowired
  private Count count;
  @Autowired
  private GroupByBoundary groupByBoundary;
  @Autowired
  private GroupByKey groupByKey;
  @Autowired
  private GroupByType groupByType;
  @Autowired
  private GroupByTag groupByTag;
  @Autowired
  private GroupByBoundaryGroupByTag groupByBoundaryGroupByTag;
  @Autowired
  private Ratio ratio;

  /**
   * Gives the count of OSM objects.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link
   *         org.heigit.ohsome.ohsomeapi.executor.AggregateRequestExecutor#aggregate() aggregate}
   */
  @ApiOperation(value = "Count of OSM elements", nickname = "count",
      response = DefaultAggregationResponse.class)
  @RequestMapping(value = "", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response count() throws Exception {
    List result = count.compute();
    return count.getResponse(result);
  }

  /**
   * Gives the count of OSM objects grouped by the OSM type.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateGroupByType(RequestResource, HttpServletRequest, HttpServletResponse,
   *         boolean, boolean) aggregateGroupByType}
   */
  @ApiOperation(value = "Count of OSM elements grouped by the type", nickname = "countGroupByType",
      response = GroupByResponse.class)
  @ApiImplicitParam(name = "filter", value = ParameterDescriptions.FILTER,
      defaultValue = DefaultSwaggerParameters.BUILDING_FILTER, paramType = "query",
      dataType = "string", required = false)
  @RequestMapping(value = "/groupBy/type", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response countGroupByType() throws Exception {
    var mapAggregator = groupByType.compute();
    var sortedMap = count.getCountGroupByResult(mapAggregator);
    var result = groupByType.getResult(sortedMap);
    return groupByType.getResponse(result);
  }

  /**
   * Gives the count of OSM objects grouped by the tag.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateGroupByTag(RequestResource, HttpServletRequest, HttpServletResponse, boolean,
   *         boolean) aggregateGroupByTag}
   */
  @ApiOperation(value = "Count of OSM elements grouped by the tag", nickname = "countGroupByTag",
      response = GroupByResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.GROUP_BY_KEY,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/groupBy/tag", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response countGroupByTag() throws Exception {
    var mapAggregator = groupByTag.compute();
    var sortedMap = count.getCountGroupByResult(mapAggregator);
    var result = groupByTag.getResult(sortedMap);
    return groupByTag.getResponse(result);
  }

  /**
   * Gives the count of OSM objects grouped by the key.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateGroupByKey(RequestResource, HttpServletRequest, HttpServletResponse, boolean,
   *         boolean) aggregateGroupByKey}
   */
  @ApiOperation(value = "Count of OSM elements grouped by the key", nickname = "countGroupByKey",
      response = GroupByResponse.class)
  @ApiImplicitParams({@ApiImplicitParam(name = "groupByKeys", value = ParameterDescriptions.KEYS,
      defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
      dataType = "string", required = true)})
  @RequestMapping(value = "/groupBy/key", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response countGroupByKey() throws Exception {
    var mapAggregator = groupByKey.compute();
    var sortedMap = count.getCountGroupByResult(mapAggregator);
    var result = groupByKey.getResult(sortedMap);
    return groupByKey.getResponse(result);
  }

  /**
   * Gives the count of OSM objects grouped by the boundary parameter (bounding box/circle/polygon).
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link
   *         org.heigit.ohsome.ohsomeapi.executor.AggregateRequestExecutor
   *         #aggregateGroupByBoundary() aggregateGroupByBoundary}
   */
  @ApiOperation(
      value = "Count of OSM elements grouped by the boundary (bboxes, bcircles, or bpolys)",
      nickname = "countGroupByBoundary", response = GroupByResponse.class)
  @ApiImplicitParam(name = "format", value = ParameterDescriptions.FORMAT, defaultValue = "",
      paramType = "query", dataType = "string", required = false)
  @RequestMapping(value = "/groupBy/boundary", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response countGroupByBoundary() throws Exception {
    var mapAggregator = groupByBoundary.compute();
    var countResult = count.getCountGroupByResult(mapAggregator);
    var result = groupByBoundary.getResult(countResult);
    return groupByBoundary.getResponse(result);
  }

  /**
   * Gives the count of OSM objects grouped by the boundary and the tag.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateGroupByBoundaryGroupByTag(RequestResource, HttpServletRequest,
   *         HttpServletResponse, boolean, boolean) aggregateGroupByBoundaryGroupByTag}
   */
  @ApiOperation(value = "Count of OSM elements grouped by the boundary and the tag",
      nickname = "countGroupByBoundaryGroupByTag", response = GroupByResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.GROUP_BY_KEY,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/groupBy/boundary/groupBy/tag",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response countGroupByBoundaryGroupByTag() throws Exception {
    var mapAggregator = groupByBoundaryGroupByTag.compute();
    var countResult = count.getCountGroupByBoundaryByTagResult(mapAggregator);
    var result = groupByBoundaryGroupByTag.getResult(countResult);
    return groupByBoundaryGroupByTag.getResponse(result);
  }

  /**
   * Gives the density of OSM elements (number of items divided by the total area in
   * square-kilometers).
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link
   *         org.heigit.ohsome.ohsomeapi.executor.AggregateRequestExecutor#aggregate() aggregate}
   */
  @ApiOperation(
      value = "Density of OSM elements (number of elements divided by "
          + "the total area in square-kilometers)",
      nickname = "countDensity", response = DefaultAggregationResponse.class)
  @RequestMapping(value = "/density", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
//  public Response countDensity(HttpServletRequest servletRequest,
//      HttpServletResponse servletResponse) throws Exception {
////    AggregateRequestExecutor executor =
////        new AggregateRequestExecutor(RequestResource.COUNT, servletRequest, servletResponse, true);
//    operator.setOperation(count);
//    operator.setOperation(density);
//    return operator.compute();
//    //return aggregateRequestExecutor.aggregate();
//  }

  /**
   * Gives the density of OSM objects grouped by the OSM type.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateGroupByType(RequestResource, HttpServletRequest, HttpServletResponse,
   *         boolean, boolean) aggregateGroupByType}
   */
//  @ApiOperation(value = "Density of OSM elements grouped by the type",
//      nickname = "countDensityGroupByType", response = GroupByResponse.class)
//  @ApiImplicitParam(name = "filter", value = ParameterDescriptions.FILTER,
//      defaultValue = DefaultSwaggerParameters.BUILDING_FILTER, paramType = "query",
//      dataType = "string", required = false)
//  @RequestMapping(value = "density/groupBy/type", method = {RequestMethod.GET, RequestMethod.POST},
//      produces = {"application/json", "text/csv"})
//  public Response countDensityGroupByType(HttpServletRequest servletRequest,
//      HttpServletResponse servletResponse) throws Exception {
//    //ElementsRequestExecutor executor = new ElementsRequestExecutor();
//    operator.setOperation(count);
//    operator.setOperation(density);
//    operator.setOperation(groupByBoundary);
//    return operator.compute();
////    return elementsRequestExecutor.aggregateGroupByType(RequestResource.COUNT, servletRequest,
////        servletResponse, true, true);
//  }

  /**
   * Gives the density of OSM objects grouped by the boundary parameter (bounding
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
//      nickname = "countDensityGroupByBoundary", response = GroupByResponse.class)
//  @ApiImplicitParam(name = "format", value = ParameterDescriptions.FORMAT, defaultValue = "",
//      paramType = "query", dataType = "string", required = false)
//  @RequestMapping(value = "/density/groupBy/boundary",
//      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
//  public Response countDensityGroupByBoundary(HttpServletRequest servletRequest,
//      HttpServletResponse servletResponse) throws Exception {
////    AggregateRequestExecutor executor =
////        new AggregateRequestExecutor(RequestResource.COUNT, servletRequest, servletResponse, true);
//    operator.setOperation(count);
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
//  @ApiOperation(value = "Density of OSM elements grouped by the boundary and the tag",
//      nickname = "countDensityGroupByBoundaryGroupByTag", response = GroupByResponse.class)
//  @ApiImplicitParams({
//      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.GROUP_BY_KEY,
//          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
//          dataType = "string", required = true),
//      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES,
//          defaultValue = "", paramType = "query", dataType = "string", required = false)})
//  @RequestMapping(value = "/density/groupBy/boundary/groupBy/tag",
//      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
//  public Response countDensityGroupByBoundaryGroupByTag(HttpServletRequest servletRequest,
//      HttpServletResponse servletResponse) throws Exception {
//    //ElementsRequestExecutor executor = new ElementsRequestExecutor();
//    operator.setOperation(count);
//    operator.setOperation(density);
//    operator.setOperation(groupByBoundary);
//    operator.setOperation(groupByTag);
//    return operator.compute();
////    return elementsRequestExecutor.aggregateGroupByBoundaryGroupByTag(RequestResource.COUNT,
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
//      nickname = "countDensityGroupByTag", response = GroupByResponse.class)
//  @ApiImplicitParams({
//      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.GROUP_BY_KEY,
//          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
//          dataType = "string", required = true),
//      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES,
//          defaultValue = "", paramType = "query", dataType = "string", required = false)})
//  @RequestMapping(value = "/density/groupBy/tag", method = {RequestMethod.GET, RequestMethod.POST},
//      produces = {"application/json", "text/csv"})
//  public Response countDensityGroupByTag(HttpServletRequest servletRequest,
//      HttpServletResponse servletResponse) throws Exception {
//    //ElementsRequestExecutor executor = new ElementsRequestExecutor();
//    operator.setOperation(count);
//    operator.setOperation(density);
//    operator.setOperation(groupByTag);
//    return operator.compute();
////    return elementsRequestExecutor.aggregateGroupByTag(RequestResource.COUNT, servletRequest,
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
//  @ApiOperation(value = "Ratio of OSM elements satisfying filter2 within items selected by filter",
//      nickname = "countRatio", response = RatioResponse.class)
//  @ApiImplicitParams({
//      @ApiImplicitParam(name = "filter", value = ParameterDescriptions.FILTER,
//          defaultValue = DefaultSwaggerParameters.BUILDING_FILTER, paramType = "query",
//          dataType = "string", required = false),
//      @ApiImplicitParam(name = "filter2", value = ParameterDescriptions.FILTER,
//          defaultValue = DefaultSwaggerParameters.HOUSENUMBER_FILTER, paramType = "query",
//          dataType = "string", required = true)})
//  @RequestMapping(value = "/ratio", method = {RequestMethod.GET, RequestMethod.POST},
//      produces = {"application/json", "text/csv"})
  public Response countRatio() throws Exception {
    var mapReducer = ratio.compute();
    var mapAggregator = ratio.aggregateByFilterMatching(mapReducer.aggregateByTimestamp());
    var countResult = count.getCountResult(mapAggregator);
    var values = ratio.getValues(
        (SortedMap<OSHDBCombinedIndex<OSHDBTimestamp, MatchType>, ? extends Number>) countResult);
    var result = ratio.getRatioResult(values);
    return ratio.getResponse(result);
  }

  /**
   * Gives the ratio of OSM elements satisfying filter2 within items selected by filter grouped by
   * the boundary.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @return {@link org.heigit.ohsome.ohsomeapi.output.Response Response}
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #aggregateRatioGroupByBoundary(RequestResource, HttpServletRequest,
   *         HttpServletResponse) aggregateRatioGroupByBoundary}
   */
  @ApiOperation(value = "Ratio of OSM elements grouped by the boundary",
      nickname = "countRatioGroupByBoundary", response = RatioGroupByBoundaryResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "filter", value = ParameterDescriptions.FILTER,
          defaultValue = DefaultSwaggerParameters.BUILDING_FILTER, paramType = "query",
          dataType = "string", required = false),
      @ApiImplicitParam(name = "filter2", value = ParameterDescriptions.FILTER,
          defaultValue = DefaultSwaggerParameters.HOUSENUMBER_FILTER, paramType = "query",
          dataType = "string", required = true)})
  @RequestMapping(value = "/ratio/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response countRatioGroupByBoundary() throws Exception {
    var mapReducer = ratio.compute();
    var mapAggregator = groupByBoundary.aggregate(mapReducer);
    var mapAggregatorEntitiesByFilterMatched = ratio.aggregateByFilterMatching(mapAggregator);
    var countResult = count.getCountGroupByResult(mapAggregatorEntitiesByFilterMatched);
    var values = ratio.getValues(countResult);
    var result = ratio.getRatioGroupByResult(values);
    return ratio.getResponse(result);
  }
}
