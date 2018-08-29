package org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation;

import javax.servlet.http.HttpServletRequest;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.DefaultSwaggerParameters;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.ParameterDescriptions;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.executor.ElementsRequestExecutor;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.executor.RequestParameters;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.executor.RequestResource;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Response;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * REST controller containing the GET and POST request handling methods, which are mapped to
 * "/elements/count".
 */
@Api(tags = "elementsCount")
@RestController
@RequestMapping("/elements/count")
public class CountController {

  /**
   * Gives the count of OSM objects.
   * 
   * @param bboxes <code>String</code> array containing lon1, lat1, lon2, lat2 values, which have to
   *        be <code>double</code> parse-able. The coordinates refer to the bottom-left and
   *        top-right corner points of a bounding box. If bboxes is given, bcircles and bpolys must
   *        be <code>null</code> or <code>empty</code>. If neither of these parameters is given, a
   *        global request is computed.
   * @param bcircles <code>String</code> array containing lon, lat and radius values, which have to
   *        be <code>double</code> parse-able. If bcircles is given, bboxes and bpolys must be
   *        <code>null</code> or <code>empty</code>.
   * @param bpolys <code>String</code> array containing lon1, lat1, ..., lonN, latN values, which
   *        have to be <code>double</code> parse-able. The first and the last coordinate pair of
   *        each polygon have to be the same. If bpolys is given, bboxes and bcircles must be
   *        <code>null</code> or <code>empty</code>.
   * @param types <code>String</code> array containing one or more OSMTypes. It can contain "node"
   *        and/or "way" and/or "relation". If types is <code>null</code> or <code>empty</code>, all
   *        three are used.
   * @param keys <code>String</code> array containing one or more keys.
   * @param values <code>String</code> array containing one or more values. Must be less or equal
   *        than <code>keys.length()</code> and values[n] must pair with keys[n].
   * @param userids <code>String</code> array containing one or more user-IDs.
   * @param time <code>String</code> array that holds a list of timestamps or a datetimestring,
   *        which fits to one of the formats used by the method
   *        {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.InputProcessingUtils#extractIsoTime(String)
   *        extractIsoTime(String time)}.
   * @param showMetadata <code>String</code> containing the value "true" or "false".
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Response
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
  public Response count(
      @ApiParam(hidden = true) @RequestParam(value = "bboxes", defaultValue = "",
          required = false) String bboxes,
      @ApiParam(hidden = true) @RequestParam(value = "bcircles", defaultValue = "",
          required = false) String bcircles,
      @ApiParam(hidden = true) @RequestParam(value = "bpolys", defaultValue = "",
          required = false) String bpolys,
      @ApiParam(hidden = true) @RequestParam(value = "types", defaultValue = "",
          required = false) String[] types,
      @ApiParam(hidden = true) @RequestParam(value = "keys", defaultValue = "",
          required = false) String[] keys,
      @ApiParam(hidden = true) @RequestParam(value = "values", defaultValue = "",
          required = false) String[] values,
      @ApiParam(hidden = true) @RequestParam(value = "userids", defaultValue = "",
          required = false) String[] userids,
      @ApiParam(hidden = true) @RequestParam(value = "time", defaultValue = "",
          required = false) String[] time,
      @ApiParam(hidden = true) @RequestParam(value = "showMetadata", defaultValue = "false",
          required = false) String showMetadata,
      @ApiParam(hidden = true) HttpServletRequest request)
      throws UnsupportedOperationException, Exception {

    return ElementsRequestExecutor.executeCountLengthPerimeterArea(RequestResource.COUNT,
        new RequestParameters(request.getMethod(), true, false, bboxes, bcircles, bpolys, types,
            keys, values, userids, time, showMetadata));
  }

  /**
   * Gives the count of OSM objects grouped by the OSM type.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Response
   *         Response}
   */
  @ApiOperation(value = "Count of OSM elements grouped by the type",
      nickname = "elementsCountGroupByType")
  @RequestMapping(value = "/groupBy/type", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response countGroupByType(
      @ApiParam(hidden = true) @RequestParam(value = "bboxes", defaultValue = "",
          required = false) String bboxes,
      @ApiParam(hidden = true) @RequestParam(value = "bcircles", defaultValue = "",
          required = false) String bcircles,
      @ApiParam(hidden = true) @RequestParam(value = "bpolys", defaultValue = "",
          required = false) String bpolys,
      @ApiParam(hidden = true) @RequestParam(value = "types", defaultValue = "",
          required = false) String[] types,
      @ApiParam(hidden = true) @RequestParam(value = "keys", defaultValue = "",
          required = false) String[] keys,
      @ApiParam(hidden = true) @RequestParam(value = "values", defaultValue = "",
          required = false) String[] values,
      @ApiParam(hidden = true) @RequestParam(value = "userids", defaultValue = "",
          required = false) String[] userids,
      @ApiParam(hidden = true) @RequestParam(value = "time", defaultValue = "",
          required = false) String[] time,
      @ApiParam(hidden = true) @RequestParam(value = "showMetadata",
          defaultValue = "false") String showMetadata,
      @ApiParam(hidden = true) HttpServletRequest request)
      throws UnsupportedOperationException, Exception {

    return ElementsRequestExecutor.executeCountPerimeterAreaGroupByType(RequestResource.COUNT,
        new RequestParameters(request.getMethod(), true, false, bboxes, bcircles, bpolys, types,
            keys, values, userids, time, showMetadata));
  }

  /**
   * Gives the count of OSM objects grouped by the userId.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Response
   *         Response}
   */
  @ApiOperation(value = "Count of OSM elements grouped by the user who was the last editor of the requested elements",
      nickname = "elementsCountGroupByUser")
  @RequestMapping(value = "/groupBy/user", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response countGroupByUser(
      @ApiParam(hidden = true) @RequestParam(value = "bboxes", defaultValue = "",
          required = false) String bboxes,
      @ApiParam(hidden = true) @RequestParam(value = "bcircles", defaultValue = "",
          required = false) String bcircles,
      @ApiParam(hidden = true) @RequestParam(value = "bpolys", defaultValue = "",
          required = false) String bpolys,
      @ApiParam(hidden = true) @RequestParam(value = "types", defaultValue = "",
          required = false) String[] types,
      @ApiParam(hidden = true) @RequestParam(value = "keys", defaultValue = "",
          required = false) String[] keys,
      @ApiParam(hidden = true) @RequestParam(value = "values", defaultValue = "",
          required = false) String[] values,
      @ApiParam(hidden = true) @RequestParam(value = "userids", defaultValue = "",
          required = false) String[] userids,
      @ApiParam(hidden = true) @RequestParam(value = "time", defaultValue = "",
          required = false) String[] time,
      @ApiParam(hidden = true) @RequestParam(value = "showMetadata",
          defaultValue = "false") String showMetadata,
      @ApiParam(hidden = true) HttpServletRequest request)
      throws UnsupportedOperationException, Exception {

    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByUser(RequestResource.COUNT,
        new RequestParameters(request.getMethod(), true, false, bboxes, bcircles, bpolys, types,
            keys, values, userids, time, showMetadata));
  }

  /**
   * Gives the count of OSM objects grouped by the boundary parameter (bounding box/circle/polygon).
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Response
   *         Response}
   */
  @ApiOperation(
      value = "Count of OSM elements grouped by the boundary (bboxes, bcircles, or bpolys)",
      nickname = "elementsCountGroupByBoundary")
  @RequestMapping(value = "/groupBy/boundary", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response countGroupByBoundary(
      @ApiParam(hidden = true) @RequestParam(value = "bboxes", defaultValue = "",
          required = false) String bboxes,
      @ApiParam(hidden = true) @RequestParam(value = "bcircles", defaultValue = "",
          required = false) String bcircles,
      @ApiParam(hidden = true) @RequestParam(value = "bpolys", defaultValue = "",
          required = false) String bpolys,
      @ApiParam(hidden = true) @RequestParam(value = "types", defaultValue = "",
          required = false) String[] types,
      @ApiParam(hidden = true) @RequestParam(value = "keys", defaultValue = "",
          required = false) String[] keys,
      @ApiParam(hidden = true) @RequestParam(value = "values", defaultValue = "",
          required = false) String[] values,
      @ApiParam(hidden = true) @RequestParam(value = "userids", defaultValue = "",
          required = false) String[] userids,
      @ApiParam(hidden = true) @RequestParam(value = "time", defaultValue = "",
          required = false) String[] time,
      @ApiParam(hidden = true) @RequestParam(value = "format", defaultValue = "",
          required = false) String format,
      @ApiParam(hidden = true) @RequestParam(value = "showMetadata",
          defaultValue = "false") String showMetadata,
      @ApiParam(hidden = true) HttpServletRequest request)
      throws UnsupportedOperationException, Exception {

    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByBoundary(
        RequestResource.COUNT, RequestParameters.of(request.getMethod(), true, false, bboxes,
            bcircles, bpolys, types, keys, values, userids, time, format, showMetadata));
  }

  /**
   * Gives the count of OSM objects grouped by the key.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @param groupByKeys <code>String</code> array containing the key used to create the tags for the
   *        grouping. One or more keys can be provided.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Response
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
  public Response countGroupByKey(
      @ApiParam(hidden = true) @RequestParam(value = "bboxes", defaultValue = "",
          required = false) String bboxes,
      @ApiParam(hidden = true) @RequestParam(value = "bcircles", defaultValue = "",
          required = false) String bcircles,
      @ApiParam(hidden = true) @RequestParam(value = "bpolys", defaultValue = "",
          required = false) String bpolys,
      @ApiParam(hidden = true) @RequestParam(value = "types", defaultValue = "",
          required = false) String[] types,
      @ApiParam(hidden = true) @RequestParam(value = "keys", defaultValue = "",
          required = false) String[] keys,
      @ApiParam(hidden = true) @RequestParam(value = "values", defaultValue = "",
          required = false) String[] values,
      @ApiParam(hidden = true) @RequestParam(value = "userids", defaultValue = "",
          required = false) String[] userids,
      @ApiParam(hidden = true) @RequestParam(value = "time", defaultValue = "",
          required = false) String[] time,
      @ApiParam(hidden = true) @RequestParam(value = "showMetadata",
          defaultValue = "false") String showMetadata,
      @ApiParam(hidden = true) HttpServletRequest request, @RequestParam(value = "groupByKeys",
          defaultValue = "", required = false) String[] groupByKeys)
      throws UnsupportedOperationException, Exception {

    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByKey(RequestResource.COUNT,
        new RequestParameters(request.getMethod(), true, false, bboxes, bcircles, bpolys, types,
            keys, values, userids, time, showMetadata),
        groupByKeys);
  }

  /**
   * Gives the count of OSM objects grouped by the tag.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @param groupByKey <code>String</code> array containing the key used to create the tags for the
   *        grouping. At the current implementation, there must be one key given (not more and not
   *        less).
   * @param groupByValues <code>String</code> array containing the values used to create the tags
   *        for grouping. If a given value does not appear in the output, then there are no objects
   *        assigned to it (within the given filters).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Response
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
  public Response countGroupByTag(
      @ApiParam(hidden = true) @RequestParam(value = "bboxes", defaultValue = "",
          required = false) String bboxes,
      @ApiParam(hidden = true) @RequestParam(value = "bcircles", defaultValue = "",
          required = false) String bcircles,
      @ApiParam(hidden = true) @RequestParam(value = "bpolys", defaultValue = "",
          required = false) String bpolys,
      @ApiParam(hidden = true) @RequestParam(value = "types", defaultValue = "",
          required = false) String[] types,
      @ApiParam(hidden = true) @RequestParam(value = "keys", defaultValue = "",
          required = false) String[] keys,
      @ApiParam(hidden = true) @RequestParam(value = "values", defaultValue = "",
          required = false) String[] values,
      @ApiParam(hidden = true) @RequestParam(value = "userids", defaultValue = "",
          required = false) String[] userids,
      @ApiParam(hidden = true) @RequestParam(value = "time", defaultValue = "",
          required = false) String[] time,
      @ApiParam(hidden = true) @RequestParam(value = "showMetadata",
          defaultValue = "false") String showMetadata,
      @ApiParam(hidden = true) HttpServletRequest request,
      @RequestParam(value = "groupByKey", defaultValue = "", required = false) String[] groupByKey,
      @RequestParam(value = "groupByValues", defaultValue = "",
          required = false) String[] groupByValues)
      throws UnsupportedOperationException, Exception {

    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByTag(
        RequestResource.COUNT, new RequestParameters(request.getMethod(), true, false, bboxes,
            bcircles, bpolys, types, keys, values, userids, time, showMetadata),
        groupByKey, groupByValues);
  }

  /**
   * Gives the share of selected items satisfying keys2 and values2 within items selected by types,
   * keys and values.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @param keys2 <code>String</code> array having the same format as keys and used to define the
   *        subgroup(share).
   * @param values2 <code>String</code> array having the same format as values and used to define
   *        the subgroup(share).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Response
   *         Response}
   */
  @ApiOperation(
      value = "Share of count of elements satisfying keys2 and values2 within elements selected by types, keys and values",
      nickname = "elementsCountShare")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "keys2", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = "maxspeed", paramType = "query", dataType = "string", required = true),
      @ApiImplicitParam(name = "values2", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/share", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response countShare(
      @ApiParam(hidden = true) @RequestParam(value = "bboxes", defaultValue = "",
          required = false) String bboxes,
      @ApiParam(hidden = true) @RequestParam(value = "bcircles", defaultValue = "",
          required = false) String bcircles,
      @ApiParam(hidden = true) @RequestParam(value = "bpolys", defaultValue = "",
          required = false) String bpolys,
      @ApiParam(hidden = true) @RequestParam(value = "types", defaultValue = "",
          required = false) String[] types,
      @ApiParam(hidden = true) @RequestParam(value = "keys", defaultValue = "",
          required = false) String[] keys,
      @ApiParam(hidden = true) @RequestParam(value = "values", defaultValue = "",
          required = false) String[] values,
      @ApiParam(hidden = true) @RequestParam(value = "userids", defaultValue = "",
          required = false) String[] userids,
      @ApiParam(hidden = true) @RequestParam(value = "time", defaultValue = "",
          required = false) String[] time,
      @ApiParam(hidden = true) @RequestParam(value = "showMetadata",
          defaultValue = "false") String showMetadata,
      @ApiParam(hidden = true) HttpServletRequest request,
      @RequestParam(value = "keys2", defaultValue = "", required = false) String[] keys2,
      @RequestParam(value = "values2", defaultValue = "", required = false) String[] values2)
      throws UnsupportedOperationException, Exception {

    return ElementsRequestExecutor.executeCountLengthPerimeterAreaRatio(
        RequestResource.COUNT, new RequestParameters(request.getMethod(), true, false, bboxes,
            bcircles, bpolys, types, keys, values, userids, time, showMetadata),
        types, keys2, values2, true);
  }

  /**
   * Gives the share of selected items satisfying keys2 and values2 within items selected by types,
   * keys and values grouped by the boundary.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @param keys2 <code>String</code> array having the same format as keys and used to define the
   *        subgroup(share).
   * @param values2 <code>String</code> array having the same format as values and used to define
   *        the subgroup(share).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Response
   *         Response}
   */
  @ApiOperation(value = "Share results of OSM elements grouped by the boundary",
      nickname = "elementsCountShareGroupByBoundary")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "keys2", value = ParameterDescriptions.KEYS_DESCR, defaultValue = "",
          paramType = "query", dataType = "string", required = true),
      @ApiImplicitParam(name = "values2", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/share/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
  public Response countShareGroupByBoundary(
      @ApiParam(hidden = true) @RequestParam(value = "bboxes", defaultValue = "",
          required = false) String bboxes,
      @ApiParam(hidden = true) @RequestParam(value = "bcircles", defaultValue = "",
          required = false) String bcircles,
      @ApiParam(hidden = true) @RequestParam(value = "bpolys", defaultValue = "",
          required = false) String bpolys,
      @ApiParam(hidden = true) @RequestParam(value = "types", defaultValue = "",
          required = false) String[] types,
      @ApiParam(hidden = true) @RequestParam(value = "keys", defaultValue = "",
          required = false) String[] keys,
      @ApiParam(hidden = true) @RequestParam(value = "values", defaultValue = "",
          required = false) String[] values,
      @ApiParam(hidden = true) @RequestParam(value = "userids", defaultValue = "",
          required = false) String[] userids,
      @ApiParam(hidden = true) @RequestParam(value = "time", defaultValue = "",
          required = false) String[] time,
      @ApiParam(hidden = true) @RequestParam(value = "format", defaultValue = "",
          required = false) String format,
      @ApiParam(hidden = true) @RequestParam(value = "showMetadata",
          defaultValue = "false") String showMetadata,
      @ApiParam(hidden = true) HttpServletRequest request,
      @RequestParam(value = "keys2", defaultValue = "", required = false) String[] keys2,
      @RequestParam(value = "values2", defaultValue = "", required = false) String[] values2)
      throws UnsupportedOperationException, Exception {

    return ElementsRequestExecutor.executeCountLengthPerimeterAreaRatioGroupByBoundary(
        RequestResource.COUNT, RequestParameters.of(request.getMethod(), true, false, bboxes,
            bcircles, bpolys, types, keys, values, userids, time, format, showMetadata),
        types, keys2, values2, true);
  }

  /**
   * Gives the density of selected items (number of items per square-kilometers).
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Response
   *         Response}
   */
  @ApiOperation(value = "Density of OSM elements (number of elements per square-kilometers)",
      nickname = "elementsCountDensity")
  @RequestMapping(value = "/density", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response countDensity(
      @ApiParam(hidden = true) @RequestParam(value = "bboxes", defaultValue = "",
          required = false) String bboxes,
      @ApiParam(hidden = true) @RequestParam(value = "bcircles", defaultValue = "",
          required = false) String bcircles,
      @ApiParam(hidden = true) @RequestParam(value = "bpolys", defaultValue = "",
          required = false) String bpolys,
      @ApiParam(hidden = true) @RequestParam(value = "types", defaultValue = "",
          required = false) String[] types,
      @ApiParam(hidden = true) @RequestParam(value = "keys", defaultValue = "",
          required = false) String[] keys,
      @ApiParam(hidden = true) @RequestParam(value = "values", defaultValue = "",
          required = false) String[] values,
      @ApiParam(hidden = true) @RequestParam(value = "userids", defaultValue = "",
          required = false) String[] userids,
      @ApiParam(hidden = true) @RequestParam(value = "time", defaultValue = "",
          required = false) String[] time,
      @ApiParam(hidden = true) @RequestParam(value = "showMetadata",
          defaultValue = "false") String showMetadata,
      @ApiParam(hidden = true) HttpServletRequest request)
      throws UnsupportedOperationException, Exception {

    return ElementsRequestExecutor.executeCountLengthPerimeterArea(RequestResource.COUNT,
        new RequestParameters(request.getMethod(), true, true, bboxes, bcircles, bpolys, types,
            keys, values, userids, time, showMetadata));
  }

  /**
   * Gives the density of OSM objects grouped by the OSM type.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Response
   *         Response}
   */
  @ApiOperation(value = "Density of OSM elements grouped by the type",
      nickname = "elementsCountDensityGroupByType")
  @RequestMapping(value = "density/groupBy/type", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public Response countDensityGroupByType(
      @ApiParam(hidden = true) @RequestParam(value = "bboxes", defaultValue = "",
          required = false) String bboxes,
      @ApiParam(hidden = true) @RequestParam(value = "bcircles", defaultValue = "",
          required = false) String bcircles,
      @ApiParam(hidden = true) @RequestParam(value = "bpolys", defaultValue = "",
          required = false) String bpolys,
      @ApiParam(hidden = true) @RequestParam(value = "types", defaultValue = "",
          required = false) String[] types,
      @ApiParam(hidden = true) @RequestParam(value = "keys", defaultValue = "",
          required = false) String[] keys,
      @ApiParam(hidden = true) @RequestParam(value = "values", defaultValue = "",
          required = false) String[] values,
      @ApiParam(hidden = true) @RequestParam(value = "userids", defaultValue = "",
          required = false) String[] userids,
      @ApiParam(hidden = true) @RequestParam(value = "time", defaultValue = "",
          required = false) String[] time,
      @ApiParam(hidden = true) @RequestParam(value = "showMetadata",
          defaultValue = "false") String showMetadata,
      @ApiParam(hidden = true) HttpServletRequest request)
      throws UnsupportedOperationException, Exception {

    return ElementsRequestExecutor.executeCountPerimeterAreaGroupByType(RequestResource.COUNT,
        new RequestParameters(request.getMethod(), true, true, bboxes, bcircles, bpolys, types,
            keys, values, userids, time, showMetadata));
  }

  /**
   * Gives the density of OSM objects grouped by the boundary parameter (bounding
   * box/circle/polygon).
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Response
   *         Response}
   */
  @ApiOperation(
      value = "Density of OSM elements grouped by the boundary (bboxes, bcircles, or bpolys)",
      nickname = "elementsCountDensityGroupByBoundary")
  @RequestMapping(value = "/density/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
  public Response countDensityGroupByBoundary(
      @ApiParam(hidden = true) @RequestParam(value = "bboxes", defaultValue = "",
          required = false) String bboxes,
      @ApiParam(hidden = true) @RequestParam(value = "bcircles", defaultValue = "",
          required = false) String bcircles,
      @ApiParam(hidden = true) @RequestParam(value = "bpolys", defaultValue = "",
          required = false) String bpolys,
      @ApiParam(hidden = true) @RequestParam(value = "types", defaultValue = "",
          required = false) String[] types,
      @ApiParam(hidden = true) @RequestParam(value = "keys", defaultValue = "",
          required = false) String[] keys,
      @ApiParam(hidden = true) @RequestParam(value = "values", defaultValue = "",
          required = false) String[] values,
      @ApiParam(hidden = true) @RequestParam(value = "userids", defaultValue = "",
          required = false) String[] userids,
      @ApiParam(hidden = true) @RequestParam(value = "time", defaultValue = "",
          required = false) String[] time,
      @ApiParam(hidden = true) @RequestParam(value = "format", defaultValue = "",
          required = false) String format,
      @ApiParam(hidden = true) @RequestParam(value = "showMetadata",
          defaultValue = "false") String showMetadata,
      @ApiParam(hidden = true) HttpServletRequest request)
      throws UnsupportedOperationException, Exception {

    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByBoundary(
        RequestResource.COUNT, RequestParameters.of(request.getMethod(), true, true, bboxes,
            bcircles, bpolys, types, keys, values, userids, time, format, showMetadata));
  }

  /**
   * Gives the density of selected items (number of items per square-kilometers) grouped by the tag.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @param groupByKey <code>String</code> array containing the key used to create the tags for the
   *        grouping. At the current implementation, there must be one key given (not more and not
   *        less).
   * @param groupByValues <code>String</code> array containing the values used to create the tags
   *        for grouping. If a given value does not appear in the output, then there are no objects
   *        assigned to it (within the given filters).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Response
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
  public Response countDensityGroupByTag(
      @ApiParam(hidden = true) @RequestParam(value = "bboxes", defaultValue = "",
          required = false) String bboxes,
      @ApiParam(hidden = true) @RequestParam(value = "bcircles", defaultValue = "",
          required = false) String bcircles,
      @ApiParam(hidden = true) @RequestParam(value = "bpolys", defaultValue = "",
          required = false) String bpolys,
      @ApiParam(hidden = true) @RequestParam(value = "types", defaultValue = "",
          required = false) String[] types,
      @ApiParam(hidden = true) @RequestParam(value = "keys", defaultValue = "",
          required = false) String[] keys,
      @ApiParam(hidden = true) @RequestParam(value = "values", defaultValue = "",
          required = false) String[] values,
      @ApiParam(hidden = true) @RequestParam(value = "userids", defaultValue = "",
          required = false) String[] userids,
      @ApiParam(hidden = true) @RequestParam(value = "time", defaultValue = "",
          required = false) String[] time,
      @ApiParam(hidden = true) @RequestParam(value = "showMetadata",
          defaultValue = "false") String showMetadata,
      @ApiParam(hidden = true) HttpServletRequest request,
      @RequestParam(value = "groupByKey", defaultValue = "", required = false) String[] groupByKey,
      @RequestParam(value = "groupByValues", defaultValue = "",
          required = false) String[] groupByValues)
      throws UnsupportedOperationException, Exception {

    return ElementsRequestExecutor.executeCountLengthPerimeterAreaGroupByTag(
        RequestResource.COUNT, new RequestParameters(request.getMethod(), true, true, bboxes,
            bcircles, bpolys, types, keys, values, userids, time, showMetadata),
        groupByKey, groupByValues);
  }

  /**
   * Gives the ratio of selected items satisfying types2, keys2 and values2 within items selected by
   * types, keys and values.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @param types2 <code>String</code> array having the same format as types.
   * @param keys2 <code>String</code> array having the same format as keys.
   * @param values2 <code>String</code> array having the same format as values.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Response
   *         Response}
   */
  @ApiOperation(
      value = "Ratio of selected items satisfying types2, keys2 and values2 within items selected by types, keys and values",
      nickname = "elementsCountRatio")
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
  public Response countRatio(
      @ApiParam(hidden = true) @RequestParam(value = "bboxes", defaultValue = "",
          required = false) String bboxes,
      @ApiParam(hidden = true) @RequestParam(value = "bcircles", defaultValue = "",
          required = false) String bcircles,
      @ApiParam(hidden = true) @RequestParam(value = "bpolys", defaultValue = "",
          required = false) String bpolys,
      @ApiParam(hidden = true) @RequestParam(value = "types", defaultValue = "",
          required = false) String[] types,
      @ApiParam(hidden = true) @RequestParam(value = "keys", defaultValue = "",
          required = false) String[] keys,
      @ApiParam(hidden = true) @RequestParam(value = "values", defaultValue = "",
          required = false) String[] values,
      @ApiParam(hidden = true) @RequestParam(value = "userids", defaultValue = "",
          required = false) String[] userids,
      @ApiParam(hidden = true) @RequestParam(value = "time", defaultValue = "",
          required = false) String[] time,
      @ApiParam(hidden = true) @RequestParam(value = "showMetadata",
          defaultValue = "false") String showMetadata,
      @ApiParam(hidden = true) HttpServletRequest request,
      @RequestParam(value = "types2", defaultValue = "", required = false) String[] types2,
      @RequestParam(value = "keys2", defaultValue = "", required = false) String[] keys2,
      @RequestParam(value = "values2", defaultValue = "", required = false) String[] values2)
      throws UnsupportedOperationException, Exception {

    return ElementsRequestExecutor.executeCountLengthPerimeterAreaRatio(
        RequestResource.COUNT, new RequestParameters(request.getMethod(), true, false, bboxes,
            bcircles, bpolys, types, keys, values, userids, time, showMetadata),
        types2, keys2, values2, false);
  }

  /**
   * Gives the ratio of selected items satisfying types2, keys2 and values2 within items selected by
   * types, keys and values grouped by the boundary.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#countRatio(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest, String[], String[], String[])
   * countRatio} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Response
   *         Response}
   */
  @ApiOperation(value = "Ratio of selected items grouped by the boundary",
      nickname = "elementsCountRatioGroupByBoundary")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "types2", value = ParameterDescriptions.TYPES_DESCR,
          defaultValue = "node", paramType = "query", dataType = "string", required = false),
      @ApiImplicitParam(name = "keys2", value = ParameterDescriptions.KEYS_DESCR,
          defaultValue = DefaultSwaggerParameters.HOUSENUMBER_KEY, paramType = "query",
          dataType = "string", required = false),
      @ApiImplicitParam(name = "values2", value = ParameterDescriptions.VALUES_DESCR,
          defaultValue = "", paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/ratio/groupBy/boundary",
      method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
  public Response countRatioGroupByBoundary(
      @ApiParam(hidden = true) @RequestParam(value = "bboxes", defaultValue = "",
          required = false) String bboxes,
      @ApiParam(hidden = true) @RequestParam(value = "bcircles", defaultValue = "",
          required = false) String bcircles,
      @ApiParam(hidden = true) @RequestParam(value = "bpolys", defaultValue = "",
          required = false) String bpolys,
      @ApiParam(hidden = true) @RequestParam(value = "types", defaultValue = "",
          required = false) String[] types,
      @ApiParam(hidden = true) @RequestParam(value = "keys", defaultValue = "",
          required = false) String[] keys,
      @ApiParam(hidden = true) @RequestParam(value = "values", defaultValue = "",
          required = false) String[] values,
      @ApiParam(hidden = true) @RequestParam(value = "userids", defaultValue = "",
          required = false) String[] userids,
      @ApiParam(hidden = true) @RequestParam(value = "time", defaultValue = "",
          required = false) String[] time,
      @ApiParam(hidden = true) @RequestParam(value = "format", defaultValue = "",
          required = false) String format,
      @ApiParam(hidden = true) @RequestParam(value = "showMetadata",
          defaultValue = "false") String showMetadata,
      @ApiParam(hidden = true) HttpServletRequest request,
      @RequestParam(value = "types2", defaultValue = "", required = false) String[] types2,
      @RequestParam(value = "keys2", defaultValue = "", required = false) String[] keys2,
      @RequestParam(value = "values2", defaultValue = "", required = false) String[] values2)
      throws UnsupportedOperationException, Exception {

    return ElementsRequestExecutor.executeCountLengthPerimeterAreaRatioGroupByBoundary(
        RequestResource.COUNT, RequestParameters.of(request.getMethod(), true, false, bboxes,
            bcircles, bpolys, types, keys, values, userids, time, format, showMetadata),
        types2, keys2, values2, false);
  }

}
