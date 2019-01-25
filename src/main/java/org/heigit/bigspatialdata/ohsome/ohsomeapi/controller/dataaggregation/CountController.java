package org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.DefaultSwaggerParameters;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.ParameterDescriptions;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.ElementsRequestExecutor;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.RequestResource;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;


/**
 * Controller containing the GET and POST request handling methods, which are mapped to
 * "/elements/count".
 */
@Api(tags = "elementsCount")
@RestController
@RequestMapping("/elements/count")
public class CountController {

  /**
   * Gives the count of OSM objects.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   * @throws UnsupportedOperationException thrown by
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer#aggregateByTimestamp()
   *         aggregateByTimestamp()}
   * @throws Exception thrown by
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#count() count()}
   */
  @ApiOperation(value = "Count of OSM elements", nickname = "elementsCount")
  @RequestMapping(value = "", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response count(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
      throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterArea(RequestResource.COUNT,
        servletRequest, servletResponse, true, false);
  }

  /**
   * Gives the count of OSM objects grouped by the OSM type.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Count of OSM elements grouped by the type",
      nickname = "elementsCountGroupByType")
  @RequestMapping(value = "/groupBy/type", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response countGroupByType(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.executeCountPerimeterAreaGroupByType(RequestResource.COUNT,
        servletRequest, servletResponse, true, false);
  }

  /**
   * Gives the count of OSM objects grouped by the userId.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Count of OSM elements grouped by the user who was the last editor "
      + "of the requested elements", nickname = "elementsCountGroupByUser")
  @RequestMapping(value = "/groupBy/user", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response countGroupByUser(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByUser(RequestResource.COUNT,
        servletRequest, servletResponse, true, false);
  }

  /**
   * Gives the count of OSM objects grouped by the boundary parameter (bounding box/circle/polygon).
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(
      value = "Count of OSM elements grouped by the boundary (bboxes, bcircles, or bpolys)",
      nickname = "elementsCountGroupByBoundary")
  @ApiImplicitParam(name = "format", value = ParameterDescriptions.FORMAT_DESCR, defaultValue = "",
      paramType = "query", dataType = "string", required = false)
  @RequestMapping(value = "/groupBy/boundary", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response countGroupByBoundary(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByBoundary(
        RequestResource.COUNT, servletRequest, servletResponse, true, false);
  }

  /**
   * Gives the count of OSM objects grouped by the key.
   * 
   * @param groupByKeys <code>String</code> array containing the key used to create the tags for the
   *        grouping. One or more keys can be provided.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Count of OSM elements grouped by the key",
      nickname = "elementsCountGroupByKey")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKeys", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true)})
  @RequestMapping(value = "/groupBy/key", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response countGroupByKey(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByKey(RequestResource.COUNT,
        servletRequest, servletResponse, true, false);
  }

  /**
   * Gives the count of OSM objects grouped by the tag.
   * 
   * @param groupByKey <code>String</code> array containing the key used to create the tags for the
   *        grouping. At the current implementation, there must be one key given (not more and not
   *        less).
   * @param groupByValues <code>String</code> array containing the values used to create the tags
   *        for grouping. If a given value does not appear in the output, then there are no objects
   *        assigned to it (within the given filters).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Count of OSM elements grouped by the tag",
      nickname = "elementsCountGroupByTag")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/groupBy/tag", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response countGroupByTag(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByTag(RequestResource.COUNT,
        servletRequest, servletResponse, true, false);
  }

  /**
   * Gives the share of selected items satisfying keys2 and values2 within items selected by types,
   * keys and values.
   * 
   * @param keys2 <code>String</code> array having the same format as keys and used to define the
   *        subgroup(share).
   * @param values2 <code>String</code> array having the same format as values and used to define
   *        the subgroup(share).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Share of count of elements satisfying keys2 and values2 "
      + "within elements selected by types, keys and values", nickname = "elementsCountShare")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "keys2", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = "maxspeed", paramType = "query", dataType = "string", required = true),
      @ApiImplicitParam(name = "values2", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/share", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response countShare(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
      throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaShareRatio(RequestResource.COUNT,
        servletRequest, servletResponse, true, false, true);
  }

  /**
   * Gives the share of selected items satisfying keys2 and values2 within items selected by types,
   * keys and values grouped by the boundary.
   * 
   * @param keys2 <code>String</code> array having the same format as keys and used to define the
   *        subgroup(share).
   * @param values2 <code>String</code> array having the same format as values and used to define
   *        the subgroup(share).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Share results of OSM elements grouped by the boundary",
      nickname = "elementsCountShareGroupByBoundary")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "keys2", value = ParameterDescriptions.KEYS_DESCR, defaultValue = "",
          paramType = "query", dataType = "string", required = true),
      @ApiImplicitParam(name = "format", value = ParameterDescriptions.FORMAT_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false),
      @ApiImplicitParam(name = "values2", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/share/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
  public Response countShareGroupByBoundary(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaShareRatioGroupByBoundary(
        RequestResource.COUNT, servletRequest, servletResponse, true, false, true);
  }

  /**
   * Gives the density of selected items (number of items divided by the total area in
   * square-kilometers).
   * 
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Density of OSM elements (number of elements divided by "
      + "the total area in square-kilometers)", nickname = "elementsCountDensity")
  @RequestMapping(value = "/density", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response countDensity(HttpServletRequest request, HttpServletResponse servletResponse)
      throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterArea(RequestResource.COUNT, request,
        servletResponse, true, true);
  }

  /**
   * Gives the density of OSM objects grouped by the OSM type.
   * 
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Density of OSM elements grouped by the type",
      nickname = "elementsCountDensityGroupByType")
  @RequestMapping(value = "density/groupBy/type", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response countDensityGroupByType(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.executeCountPerimeterAreaGroupByType(RequestResource.COUNT,
        servletRequest, servletResponse, true, true);
  }

  /**
   * Gives the density of OSM objects grouped by the boundary parameter (bounding
   * box/circle/polygon).
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(
      value = "Density of OSM elements grouped by the boundary (bboxes, bcircles, or bpolys)",
      nickname = "elementsCountDensityGroupByBoundary")
  @ApiImplicitParam(name = "format", value = ParameterDescriptions.FORMAT_DESCR, defaultValue = "",
      paramType = "query", dataType = "string", required = false)
  @RequestMapping(value = "/density/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response countDensityGroupByBoundary(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByBoundary(
        RequestResource.COUNT, servletRequest, servletResponse, true, true);
  }

  /**
   * Gives the density of selected items grouped by the tag.
   * 
   * @param groupByKey <code>String</code> array containing the key used to create the tags for the
   *        grouping. At the current implementation, there must be one key given (not more and not
   *        less).
   * @param groupByValues <code>String</code> array containing the values used to create the tags
   *        for grouping. If a given value does not appear in the output, then there are no objects
   *        assigned to it (within the given filters).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Density of OSM elements grouped by the tag",
      nickname = "elementsCountDensityGroupByTag")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/density/groupBy/tag", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response countDensityGroupByTag(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByTag(RequestResource.COUNT,
        servletRequest, servletResponse, true, true);
  }

  /**
   * Gives the ratio of selected items satisfying types2, keys2 and values2 within items selected by
   * types, keys and values.
   * 
   * @param types2 <code>String</code> array having the same format as types.
   * @param keys2 <code>String</code> array having the same format as keys.
   * @param values2 <code>String</code> array having the same format as values.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Ratio of selected items satisfying types2, keys2 and values2 "
      + "within items selected by types, keys and values", nickname = "elementsCountRatio")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "types2", value = ParameterDescriptions.TYPES_DESCR,
          defaultValue = "node", paramType = "query", dataType = "string", required = false),
      @ApiImplicitParam(name = "keys2", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = DefaultSwaggerParameters.HOUSENUMBER_KEY, paramType = "query",
          dataType = "string", required = false),
      @ApiImplicitParam(name = "values2", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/ratio", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response countRatio(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
      throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaShareRatio(RequestResource.COUNT,
        servletRequest, servletResponse, true, false, false);
  }

  /**
   * Gives the ratio of selected items satisfying types2, keys2 and values2 within items selected by
   * types, keys and values grouped by the boundary.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Ratio of selected items grouped by the boundary",
      nickname = "elementsCountRatioGroupByBoundary")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "format", value = ParameterDescriptions.FORMAT_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false),
      @ApiImplicitParam(name = "types2", value = ParameterDescriptions.TYPES_DESCR,
          defaultValue = "node", paramType = "query", dataType = "string", required = false),
      @ApiImplicitParam(name = "keys2", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = DefaultSwaggerParameters.HOUSENUMBER_KEY, paramType = "query",
          dataType = "string", required = false),
      @ApiImplicitParam(name = "values2", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/ratio/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
  public Response countRatioGroupByBoundary(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaShareRatioGroupByBoundary(
        RequestResource.COUNT, servletRequest, servletResponse, true, false, false);
  }
}
