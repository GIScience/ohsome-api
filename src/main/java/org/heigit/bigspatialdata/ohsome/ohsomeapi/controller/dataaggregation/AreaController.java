package org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.DefaultSwaggerParameters;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.ParameterDescriptions;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.ElementsRequestExecutor;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.RequestResource;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.DefaultAggregationResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.RatioResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.elements.ShareResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.GroupByResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.RatioGroupByBoundaryResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.groupbyresponse.ShareGroupByBoundaryResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * Controller containing the GET and POST servletRequest handling methods, which are mapped to
 * "/elements/area".
 */
@Api(tags = "elementsArea")
@RestController
@RequestMapping("/elements/area")
public class AreaController {

  /**
   * Gives the area of OSM objects.
   * 
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Area of OSM elements", nickname = "area",
      response = DefaultAggregationResponse.class)
  @RequestMapping(value = "", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response area(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
      throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterArea(RequestResource.AREA,
        servletRequest, servletResponse, true, false);
  }

  /**
   * Gives the area of OSM objects grouped by the OSM type.
   * 
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Area of OSM elements grouped by the type", nickname = "areaGroupByType",
      response = GroupByResponse.class)
  @RequestMapping(value = "/groupBy/type", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response areaGroupByType(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByType(RequestResource.AREA,
        servletRequest, servletResponse, true, false);
  }

  /**
   * Gives the area OSM objects grouped by the boundary parameter (bounding box/circle/polygon).
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(
      value = "Area of OSM elements in meter grouped by the boundary (bboxes, bcircles, or bpolys)",
      nickname = "areaGroupByBoundary", response = GroupByResponse.class)
  @RequestMapping(value = "/groupBy/boundary", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response areaGroupByBoundary(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByBoundary(
        RequestResource.AREA, servletRequest, servletResponse, true, false);
  }

  /**
   * Gives the area of OSM objects grouped by the key.
   * 
   * @param groupByKeys <code>String</code> array containing the key used to create the tags for the
   *        grouping. One or more keys can be provided.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Count of OSM elements grouped by the key", nickname = "areaGroupByKey",
      response = GroupByResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKeys", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true)})
  @RequestMapping(value = "/groupBy/key", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response areaGroupByKey(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByKey(RequestResource.AREA,
        servletRequest, servletResponse, true, false);
  }

  /**
   * Gives the area of OSM objects grouped by the tag.
   * 
   * @param groupByKey <code>String</code> array containing the key used to create the tags for the
   *        grouping. At the current implementation, there must be one key given (not more and not
   *        less).
   * @param groupByValues <code>String</code> array containing the values used to create the tags
   *        for grouping. If a given value does not appear in the output, then there are no objects
   *        assigned to it (within the given filters).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response }
   */
  @ApiOperation(value = "Area of OSM elements grouped by the tag", nickname = "areaGroupByTag",
      response = GroupByResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/groupBy/tag", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response areaGroupByTag(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByTag(RequestResource.AREA,
        servletRequest, servletResponse, true, false);
  }

  /**
   * Gives the area of items satisfying keys, values (plus other parameters) and part of items
   * satisfying keys2, values2 (plus other parameters).
   * 
   * @param keys2 <code>String</code> array having the same format as keys and used to define the
   *        subgroup(share).
   * @param values2 <code>String</code> array having the same format as values and used to define
   *        the subgroup(share).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(
      value = "Share of area of elements satisfying keys2 and values2 "
          + "within elements selected by types, keys and values",
      nickname = "areaShare", response = ShareResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "keys2", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = "addr:street", paramType = "query", dataType = "string", required = true),
      @ApiImplicitParam(name = "values2", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/share", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response areaShare(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
      throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaShareRatio(RequestResource.AREA,
        servletRequest, servletResponse, true, false, true);
  }

  /**
   * Gives the area of items satisfying keys, values (plus other parameters) and part of items
   * satisfying keys2, values2 (plus other parameters), grouped by the boundary.
   * 
   * @param keys2 <code>String</code> array having the same format as keys and used to define the
   *        subgroup(share).
   * @param values2 <code>String</code> array having the same format as values and used to define
   *        the subgroup(share).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Share results of OSM elements grouped by the boundary",
      nickname = "areaShareGroupByBoundary", response = ShareGroupByBoundaryResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "keys2", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = "addr:street", paramType = "query", dataType = "string", required = true),
      @ApiImplicitParam(name = "values2", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/share/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response areaShareGroupByBoundary(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaShareRatioGroupByBoundary(
        RequestResource.AREA, servletRequest, servletResponse, true, false, true);
  }

  /**
   * Gives the density of selected items (area of items divided by the total area in
   * square-kilometers).
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(
      value = "Density of OSM elements (area of elements divided "
          + "by the total area in square-kilometers)",
      nickname = "areaDensity", response = DefaultAggregationResponse.class)
  @RequestMapping(value = "/density", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response areaDensity(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterArea(RequestResource.AREA,
        servletRequest, servletResponse, true, true);
  }

  /**
   * Gives the density of selected items grouped by the OSM type.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Density of OSM elements grouped by the type",
      nickname = "areaDensityGroupByType", response = GroupByResponse.class)
  @RequestMapping(value = "/density/groupBy/type", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response areaDensityGroupByType(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByType(RequestResource.AREA,
        servletRequest, servletResponse, true, true);
  }

  /**
   * Gives the density of selected items grouped by the boundary parameter (bounding
   * box/circle/polygon).
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(
      value = "Density of selected items grouped by the boundary (bboxes, bcircles, or bpolys)",
      nickname = "areaDensityGroupByBoundary", response = GroupByResponse.class)
  @RequestMapping(value = "/density/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response areaDensityGroupByBoundary(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByBoundary(
        RequestResource.AREA, servletRequest, servletResponse, true, true);
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
  @ApiOperation(value = "Density of selected items grouped by the tag",
      nickname = "areaDensityGroupByTag", response = GroupByResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/density/groupBy/tag", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response areaDensityGroupByTag(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByTag(RequestResource.AREA,
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
  @ApiOperation(
      value = "Ratio of selected items satisfying types2, keys2 and values2 "
          + "within items selected by types, keys and values",
      nickname = "areaRatio", response = RatioResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "types2", value = ParameterDescriptions.TYPES_DESCR,
          defaultValue = "relation", paramType = "query", dataType = "string", required = false),
      @ApiImplicitParam(name = "keys2", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = false),
      @ApiImplicitParam(name = "values2", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/ratio", method = {RequestMethod.GET, RequestMethod.POST},
      produces = {"application/json", "text/csv"})
  public Response areaRatio(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
      throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaShareRatio(RequestResource.AREA,
        servletRequest, servletResponse, true, false, false);
  }

  /**
   * Gives the ratio of the area of selected items satisfying types2, keys2 and values2 within items
   * selected by types, keys and values grouped by the boundary.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Ratio of the area of selected items grouped by the boundary",
      nickname = "areaRatioGroupByBoundary", response = RatioGroupByBoundaryResponse.class)
  @ApiImplicitParams({
      @ApiImplicitParam(name = "types2", value = ParameterDescriptions.TYPES_DESCR,
          defaultValue = DefaultSwaggerParameters.TYPE, paramType = "query", dataType = "string",
          required = false),
      @ApiImplicitParam(name = "keys2", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = DefaultSwaggerParameters.BUILDING_KEY, paramType = "query",
          dataType = "string", required = false),
      @ApiImplicitParam(name = "values2", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/ratio/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = {"application/json", "text/csv"})
  public Response areaRatioGroupByBoundary(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaShareRatioGroupByBoundary(
        RequestResource.AREA, servletRequest, servletResponse, true, false, false);
  }
}
