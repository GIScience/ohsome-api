package org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements;

import org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.executor.ElementsRequestExecutor;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponse;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByBoundaryResponse;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByKeyResponse;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByTagResponse;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByTypeResponse;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByUserResponse;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.RatioResponse;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.ShareGroupByBoundaryResponse;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.ShareResponse;
import org.springframework.http.MediaType;
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
@Api(tags = "count")
@RestController
@RequestMapping("/elements/count")
public class CountController {

  /**
   * GET request giving the count of OSM objects.
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
   *        {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.inputProcessing.Utils#extractIsoTime(String)
   *        extractIsoTime(String time)}.
   * @param showMetadata <code>String</code> containing the values "true" or "false".
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         ElementsResponseContent}
   * @throws UnsupportedOperationException thrown by
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer#aggregateByTimestamp()
   *         aggregateByTimestamp()}
   * @throws Exception thrown by
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#count() count()}
   */
  @ApiOperation(value = "Count of OSM elements")
  @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
  public DefaultAggregationResponse getCount(
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
          required = false) String showMetadata)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCount(false, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata);
  }

  /**
   * GET request giving the count of OSM objects grouped by the OSM type.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByTypeResponse
   *         GroupByTypeResponseContent}
   */
  @ApiOperation(value = "Count of OSM elements grouped by the type")
  @RequestMapping(value = "/groupBy/type", method = RequestMethod.GET,
      produces = "application/json")
  public GroupByTypeResponse getCountGroupByType(
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
          defaultValue = "false") String showMetadata)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCountGroupByType(false, bboxes, bcircles, bpolys, types, keys, values,
        userids, time, showMetadata);
  }

  /**
   * GET request giving the count of OSM objects grouped by the userId.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByUserResponse
   *         GroupByUserResponseContent}
   */
  @ApiOperation(value = "Count of OSM elements grouped by the user")
  @RequestMapping(value = "/groupBy/user", method = RequestMethod.GET,
      produces = "application/json")
  public GroupByUserResponse getCountGroupByUser(
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
          defaultValue = "false") String showMetadata)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCountGroupByUser(false, bboxes, bcircles, bpolys, types, keys, values,
        userids, time, showMetadata);
  }

  /**
   * GET request giving the count of OSM objects grouped by the boundary parameter (bounding
   * box/point/polygon).
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByBoundaryResponse
   *         GroupByBoundaryResponseContent}
   */
  @ApiOperation(
      value = "Count of OSM elements grouped by the boundary (bboxes, bcircles, or bpolys)")
  @RequestMapping(value = "/groupBy/boundary", method = RequestMethod.GET,
      produces = "application/json")
  public GroupByBoundaryResponse getCountGroupByBoundary(
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
          defaultValue = "false") String showMetadata)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCountGroupByBoundary(false, bboxes, bcircles, bpolys, types, keys,
        values, userids, time, showMetadata);
  }

  /**
   * GET request giving the count of OSM objects grouped by the key.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param groupByKeys <code>String</code> array containing the key used to create the tags for the
   *        grouping. One or more keys can be provided.
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByKeyResponse
   *         GroupByKeyResponseContent}
   */
  @ApiOperation(value = "Count of OSM elements grouped by the key")
  @RequestMapping(value = "/groupBy/key", method = RequestMethod.GET, produces = "application/json")
  public GroupByKeyResponse getCountGroupByKey(
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
      @ApiParam(value = "OSM key(s) e.g.: 'highway', 'building'; default: no key",
          defaultValue = "", required = false) @RequestParam(value = "groupByKeys",
              defaultValue = "", required = false) String[] groupByKeys)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCountGroupByKey(false, bboxes, bcircles, bpolys, types, keys, values,
        userids, time, showMetadata, groupByKeys);
  }

  /**
   * GET request giving the count of OSM objects grouped by the tag.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param groupByKey <code>String</code> array containing the key used to create the tags for the
   *        grouping. At the current implementation, there must be one key given (not more and not
   *        less).
   * @param groupByValues <code>String</code> array containing the values used to create the tags
   *        for grouping. If a given value does not appear in the output, then there are no objects
   *        assigned to it (within the given filters).
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByTagResponse
   *         GroupByTagResponseContent}
   */
  @ApiOperation(value = "Count of OSM elements grouped by the tag")
  @RequestMapping(value = "/groupBy/tag", method = RequestMethod.GET, produces = "application/json")
  public GroupByTagResponse getCountGroupByTag(
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
      @ApiParam(value = "OSM key e.g.: 'highway', 'building'; default: no key", defaultValue = "",
          required = false) @RequestParam(value = "groupByKey", defaultValue = "",
              required = false) String[] groupByKey,
      @ApiParam(value = "OSM value(s) e.g.: 'primary', 'residential'; default: no value",
          defaultValue = "", required = false) @RequestParam(value = "groupByValues",
              defaultValue = "", required = false) String[] groupByValues)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCountGroupByTag(false, bboxes, bcircles, bpolys, types, keys, values,
        userids, time, showMetadata, groupByKey, groupByValues);
  }

  /**
   * GET request giving the share of selected items satisfying keys2 and values2 within items
   * selected by types, keys and values.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param keys2 <code>String</code> array having the same format as keys and used to define the
   *        subgroup(share).
   * @param values2 <code>String</code> array having the same format as values and used to define
   *        the subgroup(share).
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         ElementsResponseContent}
   */
  @ApiOperation(
      value = "Share of count of elements satisfying keys2 and values2 within elements selected by types, keys and values")
  @RequestMapping(value = "/share", method = RequestMethod.GET, produces = "application/json")
  public ShareResponse getCountShare(
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
      @ApiParam(value = "OSM key(s) e.g.: 'highway', 'building'; default: no key",
          defaultValue = "", required = false) @RequestParam(value = "keys2", defaultValue = "",
              required = false) String[] keys2,
      @ApiParam(value = "OSM value(s) e.g.: 'primary', 'residential'; default: no value",
          defaultValue = "", required = false) @RequestParam(value = "values2", defaultValue = "",
              required = false) String[] values2)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCountShare(false, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata, keys2, values2);
  }

  /**
   * GET request giving the share of selected items satisfying keys2 and values2 within items
   * selected by types, keys and values grouped by the boundary.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param keys2 <code>String</code> array having the same format as keys and used to define the
   *        subgroup(share).
   * @param values2 <code>String</code> array having the same format as values and used to define
   *        the subgroup(share).
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.ShareGroupByBoundaryResponse
   *         ShareGroupByBoundaryResponse}
   */
  @ApiOperation(value = "Share results of OSM elements grouped by the boundary")
  @RequestMapping(value = "/share/groupBy/boundary", method = RequestMethod.GET,
      produces = "application/json")
  public ShareGroupByBoundaryResponse getCountShareGroupByBoundary(
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
      @ApiParam(value = "OSM key(s) e.g.: 'highway', 'building'; default: no key",
          defaultValue = "", required = false) @RequestParam(value = "keys2", defaultValue = "",
              required = false) String[] keys2,
      @ApiParam(value = "OSM value(s) e.g.: 'primary', 'residential'; default: no value",
          defaultValue = "", required = false) @RequestParam(value = "values2", defaultValue = "",
              required = false) String[] values2)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCountShareGroupByBoundary(false, bboxes, bcircles, bpolys, types, keys,
        values, userids, time, showMetadata, keys2, values2);
  }

  /**
   * GET request giving the ratio of selected items satisfying types2, keys2 and values2 within
   * items selected by types, keys and values.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param types2 <code>String</code> array having the same format as types.
   * @param keys2 <code>String</code> array having the same format as keys.
   * @param values2 <code>String</code> array having the same format as values.
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         ElementsResponseContent}
   */
  @ApiOperation(
      value = "Ratio of selected items satisfying types2, keys2 and values2 within items selected by types, keys and values")
  @RequestMapping(value = "ratio", method = RequestMethod.GET, produces = "application/json")
  public RatioResponse getCountRatio(
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
      @ApiParam(
          value = "OSM type(s) 'node' and/or 'way' and/or 'relation'; default: all three types",
          defaultValue = "", required = false) @RequestParam(value = "types2", defaultValue = "",
              required = false) String[] types2,
      @ApiParam(value = "OSM key(s) e.g.: 'highway', 'building'; default: no key",
          defaultValue = "", required = false) @RequestParam(value = "keys2", defaultValue = "",
              required = false) String[] keys2,
      @ApiParam(value = "OSM value(s) e.g.: 'primary', 'residential'; default: no value",
          defaultValue = "", required = false) @RequestParam(value = "values2", defaultValue = "",
              required = false) String[] values2)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCountRatio(false, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata, types2, keys2, values2);
  }

  /**
   * POST request giving the count of OSM objects. POST requests should only be used if the request
   * URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         ElementsResponseContent}
   */
  @ApiOperation(value = "Count of OSM elements")
  @ApiImplicitParams({@ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
      defaultValue = "8.6128,49.3183,8.7294,49.4376", required = false,
      value = "WGS84 coordinates in the following format: "
          + "id1:lon1,lat1,lon2,lat2|id2:lon1,lat1,lon2,lat2|... OR lon1,lat1,lon2,lat2|lon1,lat1,lon2,lat2|...; default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false,
          value = "WGS84 coordinates + radius in meters in the following format: "
              + "id1:lon,lat,r|id2:lon,lat,r|... OR lon,lat,r|lon,lat,r|...; default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = "WGS84 coordinates in the following format: "
              + "id1:lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|id2:lon1,lat1,lon2,lat2,... lonm,latm,lon1,lat1|... OR "
              + "lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|lon1,lat1,lon2,lat2... lonm,latm,lon1,lat1|...; default: default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way", required = false,
          value = "OSM type(s) 'node' and/or 'way' and/or 'relation'; default: all three types"),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = "highway", required = false,
          value = "OSM key(s) e.g.: 'highway', 'building'; default: no key"),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string",
          defaultValue = "residential", required = false,
          value = "OSM value(s) e.g.: 'primary', 'residential'; default: no value"),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = "OSM userids; default: no userid"),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = "2015-01-01/2017-01-01/P1Y", required = false,
          value = "ISO-8601 conform timestring(s); default: today"),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = "true", required = false,
          value = "'Boolean' operator 'true' or 'false'; default: 'false'")})
  @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public DefaultAggregationResponse postCount(String bboxes, String bcircles, String bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata) throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCount(true, bboxes, bcircles, bpolys, types, keys, values, userids, time,
        showMetadata);
  }

  /**
   * POST request giving the count of OSM objects grouped by the OSM type. POST requests should only
   * be used if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByTypeResponse
   *         GroupByTypeResponseContent}
   */
  @ApiOperation(value = "Count of OSM elements grouped by the type")
  @ApiImplicitParams({@ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
      defaultValue = "8.6128,49.3183,8.7294,49.4376", required = false,
      value = "WGS84 coordinates in the following format: "
          + "id1:lon1,lat1,lon2,lat2|id2:lon1,lat1,lon2,lat2|... OR lon1,lat1,lon2,lat2|lon1,lat1,lon2,lat2|...; default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false,
          value = "WGS84 coordinates + radius in meters in the following format: "
              + "id1:lon,lat,r|id2:lon,lat,r|... OR lon,lat,r|lon,lat,r|...; default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = "WGS84 coordinates in the following format: "
              + "id1:lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|id2:lon1,lat1,lon2,lat2,... lonm,latm,lon1,lat1|... OR "
              + "lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|lon1,lat1,lon2,lat2... lonm,latm,lon1,lat1|...; default: default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way", required = false,
          value = "OSM type(s) 'node' and/or 'way' and/or 'relation'; default: all three types"),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = "highway", required = false,
          value = "OSM key(s) e.g.: 'highway', 'building'; default: no key"),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string",
          defaultValue = "residential", required = false,
          value = "OSM value(s) e.g.: 'primary', 'residential'; default: no value"),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = "OSM userids; default: no userid"),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = "2015-01-01/2017-01-01/P1Y", required = false,
          value = "ISO-8601 conform timestring(s); default: today"),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = "true", required = false,
          value = "'Boolean' operator 'true' or 'false'; default: 'false'")})
  @RequestMapping(value = "/groupBy/type", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByTypeResponse postCountGroupByType(String bboxes, String bcircles, String bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata) throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCountGroupByType(true, bboxes, bcircles, bpolys, types, keys, values,
        userids, time, showMetadata);
  }

  /**
   * POST request giving the count of OSM objects grouped by the userID. POST requests should only
   * be used if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByUserResponse
   *         GroupByUserResponseContent}
   */
  @ApiOperation(value = "Count of OSM elements grouped by the user")
  @ApiImplicitParams({@ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
      defaultValue = "8.6128,49.3183,8.7294,49.4376", required = false,
      value = "WGS84 coordinates in the following format: "
          + "id1:lon1,lat1,lon2,lat2|id2:lon1,lat1,lon2,lat2|... OR lon1,lat1,lon2,lat2|lon1,lat1,lon2,lat2|...; default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false,
          value = "WGS84 coordinates + radius in meters in the following format: "
              + "id1:lon,lat,r|id2:lon,lat,r|... OR lon,lat,r|lon,lat,r|...; default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = "WGS84 coordinates in the following format: "
              + "id1:lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|id2:lon1,lat1,lon2,lat2,... lonm,latm,lon1,lat1|... OR "
              + "lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|lon1,lat1,lon2,lat2... lonm,latm,lon1,lat1|...; default: default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way", required = false,
          value = "OSM type(s) 'node' and/or 'way' and/or 'relation'; default: all three types"),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = "highway", required = false,
          value = "OSM key(s) e.g.: 'highway', 'building'; default: no key"),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string",
          defaultValue = "residential", required = false,
          value = "OSM value(s) e.g.: 'primary', 'residential'; default: no value"),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = "OSM userids; default: no userid"),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = "2015-01-01/2017-01-01/P1Y", required = false,
          value = "ISO-8601 conform timestring(s); default: today"),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = "true", required = false,
          value = "'Boolean' operator 'true' or 'false'; default: 'false'")})
  @RequestMapping(value = "/groupBy/user", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByUserResponse postCountGroupByUser(String bboxes, String bcircles, String bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata) throws UnsupportedOperationException, Exception, BadRequestException {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCountGroupByUser(true, bboxes, bcircles, bpolys, types, keys, values,
        userids, time, showMetadata);
  }

  /**
   * POST request giving the count of OSM objects grouped by the boundary parameter (bounding
   * box/point/polygon). POST requests should only be used if the request URL would be too long for
   * a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByBoundaryResponse
   *         GroupByBoundaryResponseContent}
   */
  @ApiOperation(
      value = "Count of OSM elements grouped by the boundary (bboxes, bcircles, or bpolys)")
  @ApiImplicitParams({@ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
      defaultValue = "8.6128,49.3183,8.7294,49.4376", required = false,
      value = "WGS84 coordinates in the following format: "
          + "id1:lon1,lat1,lon2,lat2|id2:lon1,lat1,lon2,lat2|... OR lon1,lat1,lon2,lat2|lon1,lat1,lon2,lat2|...; default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false,
          value = "WGS84 coordinates + radius in meters in the following format: "
              + "id1:lon,lat,r|id2:lon,lat,r|... OR lon,lat,r|lon,lat,r|...; default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = "WGS84 coordinates in the following format: "
              + "id1:lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|id2:lon1,lat1,lon2,lat2,... lonm,latm,lon1,lat1|... OR "
              + "lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|lon1,lat1,lon2,lat2... lonm,latm,lon1,lat1|...; default: default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way", required = false,
          value = "OSM type(s) 'node' and/or 'way' and/or 'relation'; default: all three types"),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = "highway", required = false,
          value = "OSM key(s) e.g.: 'highway', 'building'; default: no key"),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string",
          defaultValue = "residential", required = false,
          value = "OSM value(s) e.g.: 'primary', 'residential'; default: no value"),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = "OSM userids; default: no userid"),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = "2015-01-01/2017-01-01/P1Y", required = false,
          value = "ISO-8601 conform timestring(s); default: today"),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = "true", required = false,
          value = "'Boolean' operator 'true' or 'false'; default: 'false'")})
  @RequestMapping(value = "/groupBy/boundary", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByBoundaryResponse postCountGroupByBoundary(String bboxes, String bcircles,
      String bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata)
      throws UnsupportedOperationException, Exception, BadRequestException {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCountGroupByBoundary(true, bboxes, bcircles, bpolys, types, keys, values,
        userids, time, showMetadata);
  }

  /**
   * POST request giving the count of OSM objects grouped by the key. POST requests should only be
   * used if the request URL would be too long for a GET request.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param groupByKeys <code>String</code> array containing the key used to create the tags for the
   *        grouping. One or more keys can be provided.
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByKeyResponse
   *         GroupByKeyResponseContent}
   */
  @ApiOperation(value = "Count of OSM elements grouped by the key")
  @ApiImplicitParams({@ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
      defaultValue = "8.6128,49.3183,8.7294,49.4376", required = false,
      value = "WGS84 coordinates in the following format: "
          + "id1:lon1,lat1,lon2,lat2|id2:lon1,lat1,lon2,lat2|... OR lon1,lat1,lon2,lat2|lon1,lat1,lon2,lat2|...; default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false,
          value = "WGS84 coordinates + radius in meters in the following format: "
              + "id1:lon,lat,r|id2:lon,lat,r|... OR lon,lat,r|lon,lat,r|...; default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = "WGS84 coordinates in the following format: "
              + "id1:lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|id2:lon1,lat1,lon2,lat2,... lonm,latm,lon1,lat1|... OR "
              + "lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|lon1,lat1,lon2,lat2... lonm,latm,lon1,lat1|...; default: default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way", required = false,
          value = "OSM type(s) 'node' and/or 'way' and/or 'relation'; default: all three types"),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = "highway", required = false,
          value = "OSM key(s) e.g.: 'highway', 'building'; default: no key"),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string",
          defaultValue = "residential", required = false,
          value = "OSM value(s) e.g.: 'primary', 'residential'; default: no value"),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = "OSM userids; default: no userid"),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = "2015-01-01/2017-01-01/P1Y", required = false,
          value = "ISO-8601 conform timestring(s); default: today"),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = "true", required = false,
          value = "'Boolean' operator 'true' or 'false'; default: 'false'"),
      @ApiImplicitParam(name = "groupByKeys", paramType = "form", dataType = "string",
          defaultValue = "highway", required = true,
          value = "OSM key(s) e.g.: 'highway', 'building'; default: no key")})
  @RequestMapping(value = "/groupBy/key", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByKeyResponse postCountGroupByKey(String bboxes, String bcircles, String bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata, String[] groupByKeys)
      throws UnsupportedOperationException, Exception, BadRequestException {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCountGroupByKey(true, bboxes, bcircles, bpolys, types, keys, values,
        userids, time, showMetadata, groupByKeys);
  }

  /**
   * POST request giving the count of OSM objects grouped by the tag. POST requests should only be
   * used if the request URL would be too long for a GET request.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param groupByKey <code>String</code> array containing the key used to create the tags for the
   *        grouping. At the current implementation, there must be one key given (not more and not
   *        less).
   * @param groupByValues <code>String</code> array containing the values used to create the tags
   *        for grouping. If a given value does not appear in the output, then there are no objects
   *        assigned to it (within the given filters).
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByTagResponse
   *         GroupByTagResponseContent}
   */
  @ApiOperation(value = "Count of OSM elements grouped by the tag")
  @ApiImplicitParams({@ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
      defaultValue = "8.6128,49.3183,8.7294,49.4376", required = false,
      value = "WGS84 coordinates in the following format: "
          + "id1:lon1,lat1,lon2,lat2|id2:lon1,lat1,lon2,lat2|... OR lon1,lat1,lon2,lat2|lon1,lat1,lon2,lat2|...; default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false,
          value = "WGS84 coordinates + radius in meters in the following format: "
              + "id1:lon,lat,r|id2:lon,lat,r|... OR lon,lat,r|lon,lat,r|...; default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = "WGS84 coordinates in the following format: "
              + "id1:lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|id2:lon1,lat1,lon2,lat2,... lonm,latm,lon1,lat1|... OR "
              + "lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|lon1,lat1,lon2,lat2... lonm,latm,lon1,lat1|...; default: default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way", required = false,
          value = "OSM type(s) 'node' and/or 'way' and/or 'relation'; default: all three types"),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = "highway", required = false,
          value = "OSM key(s) e.g.: 'highway', 'building'; default: no key"),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string", defaultValue = "",
          required = false,
          value = "OSM value(s) e.g.: 'primary', 'residential'; default: no value"),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = "OSM userids; default: no userid"),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = "2015-01-01/2017-01-01/P1Y", required = false,
          value = "ISO-8601 conform timestring(s); default: today"),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = "true", required = false,
          value = "'Boolean' operator 'true' or 'false'; default: 'false'"),
      @ApiImplicitParam(name = "groupByKey", paramType = "form", dataType = "string",
          defaultValue = "maxspeed", required = true,
          value = "OSM key e.g.: 'highway', 'building'; default: no key"),
      @ApiImplicitParam(name = "groupByValues", paramType = "form", dataType = "string",
          defaultValue = "", required = false,
          value = "OSM value(s) e.g.: 'primary', 'residential'; default: no value")})
  @RequestMapping(value = "/groupBy/tag", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByTagResponse postCountGroupByTag(String bboxes, String bcircles, String bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata, String[] groupByKey, String[] groupByValues)
      throws UnsupportedOperationException, Exception, BadRequestException {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCountGroupByTag(true, bboxes, bcircles, bpolys, types, keys, values,
        userids, time, showMetadata, groupByKey, groupByValues);
  }

  /**
   * POST request giving the share of selected items satisfying keys2 and values2 within items
   * selected by types, keys and values. POST requests should only be used if the request URL would
   * be too long for a GET request.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param keys2 <code>String</code> array having the same format as keys and used to define the
   *        subgroup(share).
   * @param values2 <code>String</code> array having the same format as values and used to define
   *        the subgroup(share).
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         ElementsResponseContent}
   */
  @ApiOperation(
      value = "Share of count of elements satisfying keys2 and values2 within elements selected by types, keys and values")
  @ApiImplicitParams({@ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
      defaultValue = "8.6128,49.3183,8.7294,49.4376", required = false,
      value = "WGS84 coordinates in the following format: "
          + "id1:lon1,lat1,lon2,lat2|id2:lon1,lat1,lon2,lat2|... OR lon1,lat1,lon2,lat2|lon1,lat1,lon2,lat2|...; default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false,
          value = "WGS84 coordinates + radius in meters in the following format: "
              + "id1:lon,lat,r|id2:lon,lat,r|... OR lon,lat,r|lon,lat,r|...; default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = "WGS84 coordinates in the following format: "
              + "id1:lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|id2:lon1,lat1,lon2,lat2,... lonm,latm,lon1,lat1|... OR "
              + "lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|lon1,lat1,lon2,lat2... lonm,latm,lon1,lat1|...; default: default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way", required = false,
          value = "OSM type(s) 'node' and/or 'way' and/or 'relation'; default: all three types"),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = "highway", required = false,
          value = "OSM key(s) e.g.: 'highway', 'building'; default: no key"),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string",
          defaultValue = "residential", required = false,
          value = "OSM value(s) e.g.: 'primary', 'residential'; default: no value"),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = "OSM userids; default: no userid"),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = "2015-01-01/2017-01-01/P1Y", required = false,
          value = "ISO-8601 conform timestring(s); default: today"),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = "true", required = false,
          value = "'Boolean' operator 'true' or 'false'; default: 'false'"),
      @ApiImplicitParam(name = "keys2", paramType = "form", dataType = "string",
          defaultValue = "highway", required = true,
          value = "OSM key(s) e.g.: 'highway', 'building'; default: no key"),
      @ApiImplicitParam(name = "values2", paramType = "form", dataType = "string",
          defaultValue = "", required = false,
          value = "OSM value(s) e.g.: 'primary', 'residential'; default: no value")})
  @RequestMapping(value = "/share", method = RequestMethod.POST, produces = "application/json",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ShareResponse postCountShare(String bboxes, String bcircles, String bpolys, String[] types,
      String[] keys, String[] values, String[] userids, String[] time, String showMetadata,
      String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception, BadRequestException {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCountShare(true, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata, keys2, values2);
  }

  /**
   * POST request giving the share of selected items satisfying keys2 and values2 within items
   * selected by types, keys and values, grouped by the boundary. POST requests should only be used
   * if the request URL would be too long for a GET request.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param keys2 <code>String</code> array having the same format as keys and used to define the
   *        subgroup(share).
   * @param values2 <code>String</code> array having the same format as values and used to define
   *        the subgroup(share).
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         ElementsResponseContent}
   */
  @ApiOperation(value = "Share results of OSM elements grouped by the boundary")
  @ApiImplicitParams({@ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
      defaultValue = "8.6128,49.3183,8.7294,49.4376", required = false,
      value = "WGS84 coordinates in the following format: "
          + "id1:lon1,lat1,lon2,lat2|id2:lon1,lat1,lon2,lat2|... OR lon1,lat1,lon2,lat2|lon1,lat1,lon2,lat2|...; default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false,
          value = "WGS84 coordinates + radius in meters in the following format: "
              + "id1:lon,lat,r|id2:lon,lat,r|... OR lon,lat,r|lon,lat,r|...; default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = "WGS84 coordinates in the following format: "
              + "id1:lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|id2:lon1,lat1,lon2,lat2,... lonm,latm,lon1,lat1|... OR "
              + "lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|lon1,lat1,lon2,lat2... lonm,latm,lon1,lat1|...; default: default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way", required = false,
          value = "OSM type(s) 'node' and/or 'way' and/or 'relation'; default: all three types"),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = "highway", required = false,
          value = "OSM key(s) e.g.: 'highway', 'building'; default: no key"),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string",
          defaultValue = "residential", required = false,
          value = "OSM value(s) e.g.: 'primary', 'residential'; default: no value"),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = "OSM userids; default: no userid"),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = "2015-01-01/2017-01-01/P1Y", required = false,
          value = "ISO-8601 conform timestring(s); default: today"),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = "true", required = false,
          value = "'Boolean' operator 'true' or 'false'; default: 'false'"),
      @ApiImplicitParam(name = "keys2", paramType = "form", dataType = "string",
          defaultValue = "highway", required = true,
          value = "OSM key(s) e.g.: 'highway', 'building'; default: no key"),
      @ApiImplicitParam(name = "values2", paramType = "form", dataType = "string",
          defaultValue = "", required = false,
          value = "OSM value(s) e.g.: 'primary', 'residential'; default: no value")})
  @RequestMapping(value = "/share/groupBy/boundary", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ShareGroupByBoundaryResponse postCountShareGroupByBoundary(String bboxes, String bcircles,
      String bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata, String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception, BadRequestException {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCountShareGroupByBoundary(true, bboxes, bcircles, bpolys, types, keys,
        values, userids, time, showMetadata, keys2, values2);
  }

  /**
   * POST request giving the ratio of selected items satisfying types2, keys2 and values2 within
   * items selected by types, keys and values. POST requests should only be used if the request URL
   * would be too long for a GET request.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param types2 <code>String</code> array having the same format as types.
   * @param keys2 <code>String</code> array having the same format as keys.
   * @param values2 <code>String</code> array having the same format as values.
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         ElementsResponseContent}
   */
  @ApiOperation(
      value = "Ratio of selected items satisfying types2, keys2 and values2 within items selected by types, keys and values")
  @ApiImplicitParams({@ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
      defaultValue = "8.6128,49.3183,8.7294,49.4376", required = false,
      value = "WGS84 coordinates in the following format: "
          + "id1:lon1,lat1,lon2,lat2|id2:lon1,lat1,lon2,lat2|... OR lon1,lat1,lon2,lat2|lon1,lat1,lon2,lat2|...; default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false,
          value = "WGS84 coordinates + radius in meters in the following format: "
              + "id1:lon,lat,r|id2:lon,lat,r|... OR lon,lat,r|lon,lat,r|...; default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = "WGS84 coordinates in the following format: "
              + "id1:lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|id2:lon1,lat1,lon2,lat2,... lonm,latm,lon1,lat1|... OR "
              + "lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|lon1,lat1,lon2,lat2... lonm,latm,lon1,lat1|...; default: default: whole dataset (if all three boundary parameters are empty)"),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way", required = false,
          value = "OSM type(s) 'node' and/or 'way' and/or 'relation'; default: all three types"),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = "building", required = false,
          value = "OSM key(s) e.g.: 'highway', 'building'; default: no key"),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string", defaultValue = "",
          required = false,
          value = "OSM value(s) e.g.: 'primary', 'residential'; default: no value"),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = "OSM userids; default: no userid"),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = "2015-01-01/2017-01-01/P1Y", required = false,
          value = "ISO-8601 conform timestring(s); default: today"),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = "true", required = false,
          value = "'Boolean' operator 'true' or 'false'; default: 'false'"),
      @ApiImplicitParam(name = "types2", paramType = "form", dataType = "string",
          defaultValue = "way", required = true,
          value = "OSM type(s) 'node' and/or 'way' and/or 'relation'; default: all three types"),
      @ApiImplicitParam(name = "keys2", paramType = "form", dataType = "string",
          defaultValue = "addr:housenumber", required = true,
          value = "OSM key(s) e.g.: 'highway', 'building'; default: no key"),
      @ApiImplicitParam(name = "values2", paramType = "form", dataType = "string",
          defaultValue = "", required = false,
          value = "OSM value(s) e.g.: 'primary', 'residential'; default: no value")})
  @RequestMapping(value = "/ratio", method = RequestMethod.POST, produces = "application/json",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public RatioResponse postCountRatio(String bboxes, String bcircles, String bpolys, String[] types,
      String[] keys, String[] values, String[] userids, String[] time, String showMetadata,
      String[] types2, String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCountRatio(true, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata, types2, keys2, values2);
  }

}
