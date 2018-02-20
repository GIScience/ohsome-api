package org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements;

import org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.executor.ElementsRequestExecutor;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.executor.RequestResource;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponse;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByKeyResponse;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByTagResponse;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByTypeResponse;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByUserResponse;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.ShareResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * REST controller containing the GET and POST request handling methods, which are mapped to
 * "/elements/area".
 */
@Api(tags = "area")
@RestController
@RequestMapping("/elements/area")
public class AreaController {

  /**
   * GET request giving the area of OSM objects.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         ElementsResponseContent}
   */
  @ApiOperation(value = "Area of OSM elements")
  @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
  public DefaultAggregationResponse getArea(
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
    return executor.executeLengthArea(RequestResource.AREA, false, bboxes, bcircles, bpolys, types,
        keys, values, userids, time, showMetadata);
  }

  /**
   * GET request giving the area of OSM objects grouped by the OSM type.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByTypeResponse
   *         GroupByTypeResponseContent}
   */
  @ApiOperation(value = "Area of OSM elements grouped by the type")
  @RequestMapping(value = "/groupBy/type", method = RequestMethod.GET,
      produces = "application/json")
  public GroupByTypeResponse getAreaGroupByType(
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
    return executor.executeAreaPerimeterGroupByType(RequestResource.AREA, false, bboxes, bcircles,
        bpolys, types, keys, values, userids, time, showMetadata);
  }

  /**
   * GET request giving the area of OSM objects grouped by the userId.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         ElementsResponseContent}
   */
  @ApiOperation(value = "Area of OSM elements grouped by the user")
  @RequestMapping(value = "/groupBy/user", method = RequestMethod.GET,
      produces = "application/json")
  public GroupByUserResponse getAreaGroupByUser(
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
    return executor.executeLengthPerimeterAreaGroupByUser(RequestResource.AREA, false, bboxes,
        bcircles, bpolys, types, keys, values, userids, time, showMetadata);
  }

  /**
   * GET request giving the area of OSM objects grouped by the key.
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
  public GroupByKeyResponse getAreaGroupByKey(
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
      @ApiParam(value = "OSM key(s) e.g.: 'highway', 'building'; default: null", defaultValue = "",
          required = false) @RequestParam(value = "groupByKeys", defaultValue = "",
              required = false) String[] groupByKeys)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeLengthPerimeterAreaGroupByKey(RequestResource.AREA, false, bboxes,
        bcircles, bpolys, types, keys, values, userids, time, showMetadata, groupByKeys);
  }

  /**
   * GET request giving the area of OSM objects grouped by the tag.
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
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         ElementsResponseContent}
   */
  @ApiOperation(value = "Area of OSM elements grouped by the tag")
  @RequestMapping(value = "/groupBy/tag", method = RequestMethod.GET, produces = "application/json")
  public GroupByTagResponse getAreaGroupByTag(
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
      @ApiParam(value = "OSM key e.g.: 'highway', 'building'; default: null", defaultValue = "",
          required = false) @RequestParam(value = "groupByKey", defaultValue = "",
              required = false) String[] groupByKey,
      @ApiParam(value = "OSM value(s) e.g.: 'primary', 'residential'; default: null",
          defaultValue = "", required = false) @RequestParam(value = "groupByValues",
              defaultValue = "", required = false) String[] groupByValues)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeLengthPerimeterAreaGroupByTag(RequestResource.AREA, false, bboxes,
        bcircles, bpolys, types, keys, values, userids, time, showMetadata, groupByKey,
        groupByValues);
  }

  /**
   * GET request giving the area of items satisfying keys, values (plus other parameters) and part
   * of items satisfying keys2, values2 (plus other parameters).
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
      value = "Share of area of elements satisfying keys2 and values2 within elements selected by types, keys and values")
  @RequestMapping(value = "/share", method = RequestMethod.GET, produces = "application/json")
  public ShareResponse getAreaShare(
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
      @ApiParam(value = "OSM key(s) e.g.: 'highway', 'building'; default: null", defaultValue = "",
          required = false) @RequestParam(value = "keys2", defaultValue = "",
              required = false) String[] keys2,
      @ApiParam(value = "OSM value(s) e.g.: 'primary', 'residential'; default: null",
          defaultValue = "", required = false) @RequestParam(value = "values2", defaultValue = "",
              required = false) String[] values2)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeLengthPerimeterAreaShare(RequestResource.AREA, false, bboxes, bcircles,
        bpolys, types, keys, values, userids, time, showMetadata, keys2, values2);
  }

  /**
   * POST request giving the area of OSM objects. POST requests should only be used if the request
   * URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         ElementsResponseContent}
   */
  @ApiOperation(value = "Area of OSM elements")
  @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public DefaultAggregationResponse postArea(
      @ApiParam(value = "WGS84 coordinates in the following format: "
          + "id1:lon1,lat1,lon2,lat2|id2:lon1,lat1,lon2,lat2|... OR lon1,lat1,lon2,lat2|lon1,lat1,lon2,lat2|...; default: null",
          defaultValue = "", required = false) String bboxes,
      @ApiParam(
          value = "WGS84 coordinates + radius in meters in the following format: "
              + "id1:lon,lat,r|id2:lon,lat,r|... OR lon,lat,r|lon,lat,r|...; default: null",
          defaultValue = "", required = false) String bcircles,
      @ApiParam(value = "WGS84 coordinates in the following format: "
          + "id1:lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|id2:lon1,lat1,lon2,lat2,... lonm,latm,lon1,lat1|... OR "
          + "lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|lon1,lat1,lon2,lat2... lonm,latm,lon1,lat1|...; default: null",
          defaultValue = "", required = false) String bpolys,
      @ApiParam(value = "OSM type(s) 'node' and/or 'way' and/or 'relation'; default: null",
          defaultValue = "", required = false) String[] types,
      @ApiParam(value = "OSM key(s) e.g.: 'highway', 'building'; default: null", defaultValue = "",
          required = false) String[] keys,
      @ApiParam(value = "OSM value(s) e.g.: 'primary', 'residential'; default: null",
          defaultValue = "", required = false) String[] values,
      @ApiParam(value = "OSM userids; default: null", defaultValue = "",
          required = false) String[] userids,
      @ApiParam(value = "ISO-8601 conform timestring(s); default: today", defaultValue = "",
          required = false) String[] time,
      @ApiParam(value = "'Boolean' operator 'true' or 'false'; default: 'false'", defaultValue = "",
          required = false) String showMetadata)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeLengthArea(RequestResource.AREA, true, bboxes, bcircles, bpolys, types,
        keys, values, userids, time, showMetadata);
  }

  /**
   * POST request giving the area of OSM objects grouped by the OSM type. POST requests should only
   * be used if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByTypeResponse
   *         GroupByTypeResponseContent}
   */
  @ApiOperation(value = "Area of OSM elements grouped by the type")
  @RequestMapping(value = "/groupBy/type", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByTypeResponse postAreaGroupByType(
      @ApiParam(value = "WGS84 coordinates in the following format: "
          + "id1:lon1,lat1,lon2,lat2|id2:lon1,lat1,lon2,lat2|... OR lon1,lat1,lon2,lat2|lon1,lat1,lon2,lat2|...; default: null",
          defaultValue = "", required = false) String bboxes,
      @ApiParam(
          value = "WGS84 coordinates + radius in meters in the following format: "
              + "id1:lon,lat,r|id2:lon,lat,r|... OR lon,lat,r|lon,lat,r|...; default: null",
          defaultValue = "", required = false) String bcircles,
      @ApiParam(value = "WGS84 coordinates in the following format: "
          + "id1:lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|id2:lon1,lat1,lon2,lat2,... lonm,latm,lon1,lat1|... OR "
          + "lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|lon1,lat1,lon2,lat2... lonm,latm,lon1,lat1|...; default: null",
          defaultValue = "", required = false) String bpolys,
      @ApiParam(value = "OSM type(s) 'node' and/or 'way' and/or 'relation'; default: null",
          defaultValue = "", required = false) String[] types,
      @ApiParam(value = "OSM key(s) e.g.: 'highway', 'building'; default: null", defaultValue = "",
          required = false) String[] keys,
      @ApiParam(value = "OSM value(s) e.g.: 'primary', 'residential'; default: null",
          defaultValue = "", required = false) String[] values,
      @ApiParam(value = "OSM userids; default: null", defaultValue = "",
          required = false) String[] userids,
      @ApiParam(value = "ISO-8601 conform timestring(s); default: today", defaultValue = "",
          required = false) String[] time,
      @ApiParam(value = "'Boolean' operator 'true' or 'false'; default: 'false'", defaultValue = "",
          required = false) String showMetadata)
      throws UnsupportedOperationException, Exception, BadRequestException {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeAreaPerimeterGroupByType(RequestResource.AREA, true, bboxes, bcircles,
        bpolys, types, keys, values, userids, time, showMetadata);
  }

  /**
   * POST request giving the area of OSM objects grouped by the userID. POST requests should only be
   * used if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByUserResponse
   *         GroupByUserResponseContent}
   */
  @ApiOperation(value = "Area of OSM elements grouped by the user")
  @RequestMapping(value = "/groupBy/user", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByUserResponse postAreaGroupByUser(
      @ApiParam(value = "WGS84 coordinates in the following format: "
          + "id1:lon1,lat1,lon2,lat2|id2:lon1,lat1,lon2,lat2|... OR lon1,lat1,lon2,lat2|lon1,lat1,lon2,lat2|...; default: null",
          defaultValue = "", required = false) String bboxes,
      @ApiParam(
          value = "WGS84 coordinates + radius in meters in the following format: "
              + "id1:lon,lat,r|id2:lon,lat,r|... OR lon,lat,r|lon,lat,r|...; default: null",
          defaultValue = "", required = false) String bcircles,
      @ApiParam(value = "WGS84 coordinates in the following format: "
          + "id1:lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|id2:lon1,lat1,lon2,lat2,... lonm,latm,lon1,lat1|... OR "
          + "lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|lon1,lat1,lon2,lat2... lonm,latm,lon1,lat1|...; default: null",
          defaultValue = "", required = false) String bpolys,
      @ApiParam(value = "OSM type(s) 'node' and/or 'way' and/or 'relation'; default: null",
          defaultValue = "", required = false) String[] types,
      @ApiParam(value = "OSM key(s) e.g.: 'highway', 'building'; default: null", defaultValue = "",
          required = false) String[] keys,
      @ApiParam(value = "OSM value(s) e.g.: 'primary', 'residential'; default: null",
          defaultValue = "", required = false) String[] values,
      @ApiParam(value = "OSM userids; default: null", defaultValue = "",
          required = false) String[] userids,
      @ApiParam(value = "ISO-8601 conform timestring(s); default: today", defaultValue = "",
          required = false) String[] time,
      @ApiParam(value = "'Boolean' operator 'true' or 'false'; default: 'false'", defaultValue = "",
          required = false) String showMetadata)
      throws UnsupportedOperationException, Exception, BadRequestException {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeLengthPerimeterAreaGroupByUser(RequestResource.AREA, true, bboxes,
        bcircles, bpolys, types, keys, values, userids, time, showMetadata);
  }

  /**
   * POST request giving the area of OSM objects grouped by the key. POST requests should only be
   * used if the request URL would be too long for a GET request.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param groupByKeys <code>String</code> array containing the key used to create the tags for the
   *        grouping. At the current implementation, there must be one key given (not more and not
   *        less).
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.GroupByTagResponse
   *         GroupByTagResponseContent}
   */
  @ApiOperation(value = "Length of OSM elements grouped by the tag")
  @RequestMapping(value = "/groupBy/key", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByKeyResponse postAreaGroupByKey(
      @ApiParam(value = "WGS84 coordinates in the following format: "
          + "id1:lon1,lat1,lon2,lat2|id2:lon1,lat1,lon2,lat2|... OR lon1,lat1,lon2,lat2|lon1,lat1,lon2,lat2|...; default: null",
          defaultValue = "", required = false) String bboxes,
      @ApiParam(
          value = "WGS84 coordinates + radius in meters in the following format: "
              + "id1:lon,lat,r|id2:lon,lat,r|... OR lon,lat,r|lon,lat,r|...; default: null",
          defaultValue = "", required = false) String bcircles,
      @ApiParam(value = "WGS84 coordinates in the following format: "
          + "id1:lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|id2:lon1,lat1,lon2,lat2,... lonm,latm,lon1,lat1|... OR "
          + "lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|lon1,lat1,lon2,lat2... lonm,latm,lon1,lat1|...; default: null",
          defaultValue = "", required = false) String bpolys,
      @ApiParam(value = "OSM type(s) 'node' and/or 'way' and/or 'relation'; default: null",
          defaultValue = "", required = false) String[] types,
      @ApiParam(value = "OSM key(s) e.g.: 'highway', 'building'; default: null", defaultValue = "",
          required = false) String[] keys,
      @ApiParam(value = "OSM value(s) e.g.: 'primary', 'residential'; default: null",
          defaultValue = "", required = false) String[] values,
      @ApiParam(value = "OSM userids; default: null", defaultValue = "",
          required = false) String[] userids,
      @ApiParam(value = "ISO-8601 conform timestring(s); default: today", defaultValue = "",
          required = false) String[] time,
      @ApiParam(value = "'Boolean' operator 'true' or 'false'; default: 'false'", defaultValue = "",
          required = false) String showMetadata,
      @ApiParam(value = "OSM key(s) e.g.: 'highway', 'building'; default: null", defaultValue = "",
          required = false) String[] groupByKeys)
      throws UnsupportedOperationException, Exception, BadRequestException {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeLengthPerimeterAreaGroupByKey(RequestResource.AREA, true, bboxes,
        bcircles, bpolys, types, keys, values, userids, time, showMetadata, groupByKeys);
  }

  /**
   * POST request giving the area of OSM objects grouped by the tag. POST requests should only be
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
  @ApiOperation(value = "Area of OSM elements grouped by the tag")
  @RequestMapping(value = "/groupBy/tag", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByTagResponse postAreaGroupByTag(
      @ApiParam(value = "WGS84 coordinates in the following format: "
          + "id1:lon1,lat1,lon2,lat2|id2:lon1,lat1,lon2,lat2|... OR lon1,lat1,lon2,lat2|lon1,lat1,lon2,lat2|...; default: null",
          defaultValue = "", required = false) String bboxes,
      @ApiParam(
          value = "WGS84 coordinates + radius in meters in the following format: "
              + "id1:lon,lat,r|id2:lon,lat,r|... OR lon,lat,r|lon,lat,r|...; default: null",
          defaultValue = "", required = false) String bcircles,
      @ApiParam(value = "WGS84 coordinates in the following format: "
          + "id1:lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|id2:lon1,lat1,lon2,lat2,... lonm,latm,lon1,lat1|... OR "
          + "lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|lon1,lat1,lon2,lat2... lonm,latm,lon1,lat1|...; default: null",
          defaultValue = "", required = false) String bpolys,
      @ApiParam(value = "OSM type(s) 'node' and/or 'way' and/or 'relation'; default: null",
          defaultValue = "", required = false) String[] types,
      @ApiParam(value = "OSM key(s) e.g.: 'highway', 'building'; default: null", defaultValue = "",
          required = false) String[] keys,
      @ApiParam(value = "OSM value(s) e.g.: 'primary', 'residential'; default: null",
          defaultValue = "", required = false) String[] values,
      @ApiParam(value = "OSM userids; default: null", defaultValue = "",
          required = false) String[] userids,
      @ApiParam(value = "ISO-8601 conform timestring(s); default: today", defaultValue = "",
          required = false) String[] time,
      @ApiParam(value = "'Boolean' operator 'true' or 'false'; default: 'false'", defaultValue = "",
          required = false) String showMetadata,
      @ApiParam(value = "OSM key e.g.: 'highway', 'building'; default: null", defaultValue = "",
          required = false) String[] groupByKey,
      @ApiParam(value = "OSM value(s) e.g.: 'primary', 'residential'; default: null",
          defaultValue = "", required = false) String[] groupByValues)
      throws UnsupportedOperationException, Exception, BadRequestException {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeLengthPerimeterAreaGroupByTag(RequestResource.AREA, true, bboxes,
        bcircles, bpolys, types, keys, values, userids, time, showMetadata, groupByKey,
        groupByValues);
  }

  /**
   * POST request giving the share of selected items satisfying keys2 and values2 within items
   * selected by types, keys and values. POST requests should only be used if the request URL would
   * be too long for a GET request.
   * <p>
   * The parameters are described in the
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
      value = "Share of area of elements satisfying keys2 and values2 within elements selected by types, keys and values")
  @RequestMapping(value = "/share", method = RequestMethod.POST, produces = "application/json",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ShareResponse postAreaShare(
      @ApiParam(value = "WGS84 coordinates in the following format: "
          + "id1:lon1,lat1,lon2,lat2|id2:lon1,lat1,lon2,lat2|... OR lon1,lat1,lon2,lat2|lon1,lat1,lon2,lat2|...; default: null",
          defaultValue = "", required = false) String bboxes,
      @ApiParam(
          value = "WGS84 coordinates + radius in meters in the following format: "
              + "id1:lon,lat,r|id2:lon,lat,r|... OR lon,lat,r|lon,lat,r|...; default: null",
          defaultValue = "", required = false) String bcircles,
      @ApiParam(value = "WGS84 coordinates in the following format: "
          + "id1:lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|id2:lon1,lat1,lon2,lat2,... lonm,latm,lon1,lat1|... OR "
          + "lon1,lat1,lon2,lat2,... lonn,latn,lon1,lat1|lon1,lat1,lon2,lat2... lonm,latm,lon1,lat1|...; default: null",
          defaultValue = "", required = false) String bpolys,
      @ApiParam(value = "OSM type(s) 'node' and/or 'way' and/or 'relation'; default: null",
          defaultValue = "", required = false) String[] types,
      @ApiParam(value = "OSM key(s) e.g.: 'highway', 'building'; default: null", defaultValue = "",
          required = false) String[] keys,
      @ApiParam(value = "OSM value(s) e.g.: 'primary', 'residential'; default: null",
          defaultValue = "", required = false) String[] values,
      @ApiParam(value = "OSM userids; default: null", defaultValue = "",
          required = false) String[] userids,
      @ApiParam(value = "ISO-8601 conform timestring(s); default: today", defaultValue = "",
          required = false) String[] time,
      @ApiParam(value = "'Boolean' operator 'true' or 'false'; default: 'false'", defaultValue = "",
          required = false) String showMetadata,
      @ApiParam(value = "OSM key(s) e.g.: 'highway', 'building'; default: null", defaultValue = "",
          required = false) String[] keys2,
      @ApiParam(value = "OSM value(s) e.g.: 'primary', 'residential'; default: null",
          defaultValue = "", required = false) String[] values2)
      throws UnsupportedOperationException, Exception, BadRequestException {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeLengthPerimeterAreaShare(RequestResource.AREA, true, bboxes, bcircles,
        bpolys, types, keys, values, userids, time, showMetadata, keys2, values2);
  }

}
