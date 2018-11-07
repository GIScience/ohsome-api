package org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.servlet.http.HttpServletRequest;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.DefaultSwaggerParameters;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.ParameterDescriptions;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.ElementsRequestExecutor;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.RequestParameters;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.RequestResource;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller containing the GET and POST request handling methods, which are mapped to
 * "/elements/length".
 */
@Api(tags = "elementsLength")
@RestController
@RequestMapping("/elements/length")
public class LengthController {

  /**
   * Gives the length of OSM objects.
   * 
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Length of OSM elements", nickname = "elementsLength")
  @RequestMapping(value = "", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response length(@ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) HttpServletRequest request) throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterArea(RequestResource.LENGTH,
        new RequestParameters(request.getMethod(), true, false, request.getParameter("bboxes"),
            request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys, values,
            userids, time, request.getParameter("showMetadata")));
  }

  /**
   * Gives the length of OSM objects grouped by the OSM type.
   * 
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Length of OSM elements grouped by the type",
      nickname = "elementsLengthGroupByType")
  @RequestMapping(value = "/groupBy/type", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response lengthGroupByType(
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) HttpServletRequest request) throws Exception {

    return ElementsRequestExecutor.executeCountPerimeterAreaGroupByType(RequestResource.LENGTH,
        new RequestParameters(request.getMethod(), true, false, request.getParameter("bboxes"),
            request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys, values,
            userids, time, request.getParameter("showMetadata")));
  }

  /**
   * Gives the length of OSM objects grouped by the user who was the last editor of the requested
   * elements.
   * 
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(
      value = "Length of OSM elements grouped by the user "
          + "who was the last editor of the requested elements",
      nickname = "elementsLengthGroupByUser")
  @RequestMapping(value = "/groupBy/user", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response lengthGroupByUser(
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) HttpServletRequest request) throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByUser(
        RequestResource.LENGTH,
        new RequestParameters(request.getMethod(), true, false, request.getParameter("bboxes"),
            request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys, values,
            userids, time, request.getParameter("showMetadata")));
  }

  /**
   * Gives the length of OSM objects grouped by the boundary parameter (bounding
   * box/circle/polygon).
   * 
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(
      value = "Length of OSM elements in meter grouped by "
          + "the boundary (bboxes, bcircles, or bpolys)",
      nickname = "elementsLengthGroupByBoundary")
  @RequestMapping(value = "/groupBy/boundary", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response lengthGroupByBoundary(
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) HttpServletRequest request) throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByBoundary(
        RequestResource.LENGTH,
        RequestParameters.of(request.getMethod(), true, false, request.getParameter("bboxes"),
            request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys, values,
            userids, time, request.getParameter("format"), request.getParameter("showMetadata")));
  }

  /**
   * Gives the length of OSM objects grouped by the key.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @param groupByKeys <code>String</code> array containing the key used to create the tags for the
   *        grouping. One or more keys can be provided.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Count of OSM elements grouped by the key",
      nickname = "elementsLengthGroupByKey")
  @ApiImplicitParams({@ApiImplicitParam(name = "groupByKeys",
      value = ParameterDescriptions.KEYS_DESCR, defaultValue = DefaultSwaggerParameters.HIGHWAY_KEY,
      paramType = "query", dataType = "string", required = true)})
  @RequestMapping(value = "/groupBy/key", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response lengthGroupByKey(
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) HttpServletRequest request, @RequestParam(value = "groupByKeys",
          defaultValue = "", required = false) String[] groupByKeys)
      throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByKey(RequestResource.LENGTH,
        new RequestParameters(request.getMethod(), true, false, request.getParameter("bboxes"),
            request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys, values,
            userids, time, request.getParameter("showMetadata")),
        groupByKeys);
  }

  /**
   * Gives the length of OSM objects grouped by the tag.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
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
  @ApiOperation(value = "Length of OSM elements grouped by the tag",
      nickname = "elementsLengthGroupByTag")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = DefaultSwaggerParameters.HIGHWAY_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/groupBy/tag", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response lengthGroupByTag(
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) HttpServletRequest request,
      @RequestParam(value = "groupByKey", defaultValue = "", required = false) String[] groupByKey,
      @RequestParam(value = "groupByValues", defaultValue = "",
          required = false) String[] groupByValues)
      throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByTag(RequestResource.LENGTH,
        new RequestParameters(request.getMethod(), true, false, request.getParameter("bboxes"),
            request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys, values,
            userids, time, request.getParameter("showMetadata")),
        groupByKey, groupByValues);
  }

  /**
   * Gives the length of items satisfying keys, values (+ other params) and part of items satisfying
   * keys2, values2.(+ other params).
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @param keys2 <code>String</code> array having the same format as keys and used to define the
   *        subgroup(share).
   * @param values2 <code>String</code> array having the same format as values and used to define
   *        the subgroup(share).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Share of length of elements satisfying keys2 and values2 "
      + "within elements selected by types, keys and values", nickname = "elementsLengthShare")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "keys2", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = "maxspeed", paramType = "query", dataType = "string", required = true),
      @ApiImplicitParam(name = "values2", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/share", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response lengthShare(
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) HttpServletRequest request,
      @RequestParam(value = "keys2", defaultValue = "", required = false) String[] keys2,
      @RequestParam(value = "values2", defaultValue = "", required = false) String[] values2)
      throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaRatio(RequestResource.LENGTH,
        new RequestParameters(request.getMethod(), true, false, request.getParameter("bboxes"),
            request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys, values,
            userids, time, request.getParameter("showMetadata")),
        types, keys2, values2, true);
  }

  /**
   * Gives the length of items satisfying keys, values (+ other params) and part of items satisfying
   * keys2, values2 (plus other parameters), grouped by the boundary.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @param keys2 <code>String</code> array having the same format as keys and used to define the
   *        subgroup(share).
   * @param values2 <code>String</code> array having the same format as values and used to define
   *        the subgroup(share).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Share results of OSM elements grouped by the boundary",
      nickname = "elementsLengthShareGroupByBoundary")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "keys2", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = "maxspeed", paramType = "query", dataType = "string", required = true),
      @ApiImplicitParam(name = "values2", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/share/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
  public Response lengthShareGroupByBoundary(
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) HttpServletRequest request,
      @RequestParam(value = "keys2", defaultValue = "", required = false) String[] keys2,
      @RequestParam(value = "values2", defaultValue = "", required = false) String[] values2)
      throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaRatioGroupByBoundary(
        RequestResource.LENGTH,
        RequestParameters.of(request.getMethod(), true, false, request.getParameter("bboxes"),
            request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys, values,
            userids, time, request.getParameter("format"), request.getParameter("showMetadata")),
        types, keys2, values2, true);
  }

  /**
   * Gives the density of selected items (length of items divided by the total area in
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
  @ApiOperation(value = "Density of OSM elements (length of elements divided by "
      + "the total area in square-kilometers)", nickname = "elementsLengthDensity")
  @RequestMapping(value = "/density", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response lengthDensity(
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) HttpServletRequest request) throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterArea(RequestResource.LENGTH,
        new RequestParameters(request.getMethod(), true, true, request.getParameter("bboxes"),
            request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys, values,
            userids, time, request.getParameter("showMetadata")));
  }

  /**
   * Gives the density of selected items grouped by the OSM type.
   * 
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Density of selected items grouped by the OSM type",
      nickname = "elementsLengthDensityGroupByType")
  @RequestMapping(value = "/density/groupBy/type", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response lengthDensityGroupByType(
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) HttpServletRequest request) throws Exception {
    return ElementsRequestExecutor.executeCountPerimeterAreaGroupByType(RequestResource.LENGTH,
        new RequestParameters(request.getMethod(), true, true, request.getParameter("bboxes"),
            request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys, values,
            userids, time, request.getParameter("showMetadata")));
  }

  /**
   * Gives density of selected items grouped by the boundary parameter (bounding
   * box/circle/polygon).
   * 
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(
      value = "Density of selected items grouped by the boundary (bboxes, bcircles, or bpolys)",
      nickname = "elementsLengthDensityGroupByBoundary")
  @RequestMapping(value = "/density/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
  public Response lengthDensityGroupByBoundary(
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) HttpServletRequest request) throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByBoundary(
        RequestResource.LENGTH,
        RequestParameters.of(request.getMethod(), true, true, request.getParameter("bboxes"),
            request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys, values,
            userids, time, request.getParameter("format"), request.getParameter("showMetadata")));
  }

  /**
   * Gives the density of selected items grouped by the tag.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
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
      nickname = "elementsLengthDensityGroupByTag")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = DefaultSwaggerParameters.HIGHWAY_KEY, paramType = "query",
          dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/density/groupBy/tag", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response lengthDensityGroupByTag(
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) HttpServletRequest request,
      @RequestParam(value = "groupByKey", defaultValue = "", required = false) String[] groupByKey,
      @RequestParam(value = "groupByValues", defaultValue = "",
          required = false) String[] groupByValues)
      throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByTag(RequestResource.LENGTH,
        new RequestParameters(request.getMethod(), true, true, request.getParameter("bboxes"),
            request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys, values,
            userids, time, request.getParameter("showMetadata")),
        groupByKey, groupByValues);
  }

  /**
   * Gives the ratio of selected items satisfying types2, keys2 and values2 within items selected by
   * types, keys and values.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @param types2 <code>String</code> array having the same format as types.
   * @param keys2 <code>String</code> array having the same format as keys.
   * @param values2 <code>String</code> array having the same format as values.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Ratio of the length of selected items", nickname = "elementsLengthRatio")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "types2", value = ParameterDescriptions.TYPES_DESCR,
          defaultValue = DefaultSwaggerParameters.TYPE, paramType = "query", dataType = "string",
          required = false),
      @ApiImplicitParam(name = "keys2", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = DefaultSwaggerParameters.HIGHWAY_KEY, paramType = "query",
          dataType = "string", required = false),
      @ApiImplicitParam(name = "values2", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "primary", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/ratio", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response lengthRatio(
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) HttpServletRequest request,
      @RequestParam(value = "types2", defaultValue = "", required = false) String[] types2,
      @RequestParam(value = "keys2", defaultValue = "", required = false) String[] keys2,
      @RequestParam(value = "values2", defaultValue = "", required = false) String[] values2)
      throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaRatio(RequestResource.LENGTH,
        new RequestParameters(request.getMethod(), true, false, request.getParameter("bboxes"),
            request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys, values,
            userids, time, request.getParameter("showMetadata")),
        types2, keys2, values2, false);
  }

  /**
   * Gives the ratio of the length of selected items satisfying types2, keys2 and values2 within
   * items selected by types, keys and values grouped by the boundary.
   * 
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#countRatio(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest, String[], String[], String[])
   * countRatio} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.output.dataaggregationresponse.Response
   *         Response}
   */
  @ApiOperation(value = "Ratio of the length of selected items grouped by the boundary",
      nickname = "elementsLengthRatioGroupByBoundary")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "types2", value = ParameterDescriptions.TYPES_DESCR,
          defaultValue = DefaultSwaggerParameters.TYPE, paramType = "query", dataType = "string",
          required = false),
      @ApiImplicitParam(name = "keys2", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = DefaultSwaggerParameters.HIGHWAY_KEY, paramType = "query",
          dataType = "string", required = false),
      @ApiImplicitParam(name = "values2", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/ratio/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
  public Response lengthRatioGroupByBoundary(
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) HttpServletRequest request,
      @RequestParam(value = "types2", defaultValue = "", required = false) String[] types2,
      @RequestParam(value = "keys2", defaultValue = "", required = false) String[] keys2,
      @RequestParam(value = "values2", defaultValue = "", required = false) String[] values2)
      throws Exception {
    return ElementsRequestExecutor.executeCountLengthPerimeterAreaRatioGroupByBoundary(
        RequestResource.LENGTH,
        RequestParameters.of(request.getMethod(), true, false, request.getParameter("bboxes"),
            request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys, values,
            userids, time, request.getParameter("format"), request.getParameter("showMetadata")),
        types2, keys2, values2, false);
  }
}
