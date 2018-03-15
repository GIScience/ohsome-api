package org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements;

import org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.executor.ElementsRequestExecutor;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.executor.RequestResource;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByBoundaryResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByKeyResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByTagResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByTypeResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByUserResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.RatioResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.ShareGroupByBoundaryResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.ShareResponse;
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
 * "/elements/perimeter".
 */
@Api(tags = "perimeter")
@RestController
@RequestMapping("/elements/perimeter")
public class PerimeterController {

  final String bboxesDescr = "WGS84 coordinates in the following format: "
      + "id1:lon1,lat1,lon2,lat2|id2:lon1,lat1,lon2,lat2|... OR lon1,lat1,lon2,lat2|lon1,lat1,lon2,lat2|...; default: whole dataset (if all three boundary parameters are empty)";
  final String bcirclesDescr = "WGS84 coordinates + radius in meter in the following format: "
      + "id1:lon,lat,r|id2:lon,lat,r|... OR lon,lat,r|lon,lat,r|...; default: whole dataset (if all three boundary parameters are empty)";
  final String bpolysDescr =
      "WGS84 coordinates given as a list of coordinate pairs (as for bboxes) or GeoJSON FeatureCollection. The first point has to be the same as "
          + "the last point and MultiPolygons are only supported in GeoJSON; default: whole dataset (if all three boundary parameters are empty)";
  final String typesDescr =
      "OSM type(s) 'node' and/or 'way' and/or 'relation'; default: all three types";
  final String keysDescr = "OSM key(s) e.g.: 'highway', 'building'; default: no key";
  final String valuesDescr = "OSM value(s) e.g.: 'primary', 'residential'; default: no value";
  final String useridsDescr = "OSM userids; default: no userid";
  final String timeDescr = "ISO-8601 conform timestring(s); default: today";
  final String showMetadataDescr = "'Boolean' operator 'true' or 'false'; default: 'false'";

  /**
   * GET request giving the perimeter of polygonal OSM objects.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   */
  @ApiOperation(value = "Perimeter of OSM elements")
  @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
  public DefaultAggregationResponse getPerimeter(
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


    return ElementsRequestExecutor.executeLengthPerimeterArea(RequestResource.PERIMETER, false,
        false, bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata);
  }

  /**
   * GET request giving the perimeter of polygonal OSM objects grouped by the OSM type.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByTypeResponse
   *         GroupByTypeResponseContent}
   */
  @ApiOperation(value = "Perimeter of OSM elements grouped by the type")
  @RequestMapping(value = "/groupBy/type", method = RequestMethod.GET,
      produces = "application/json")
  public GroupByTypeResponse getPerimeterGroupByType(
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

    return ElementsRequestExecutor.executePerimeterAreaGroupByType(RequestResource.PERIMETER, false,
        false, bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata);
  }

  /**
   * GET request giving the perimeter of polygonal OSM objects grouped by the userId.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByUserResponse
   *         GroupByUserResponseContent}
   */
  @ApiOperation(value = "Perimeter of OSM elements grouped by the user")
  @RequestMapping(value = "/groupBy/user", method = RequestMethod.GET,
      produces = "application/json")
  public GroupByUserResponse getPerimeterGroupByUser(
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

    return ElementsRequestExecutor.executeLengthPerimeterAreaGroupByUser(RequestResource.PERIMETER,
        false, bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata);
  }

  /**
   * GET request giving the perimeter of polygonal OSM objects grouped by the boundary parameter
   * (bounding box/circle/polygon).
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByBoundaryResponse
   *         GroupByBoundaryResponseContent}
   */
  @ApiOperation(
      value = "Perimeter of OSM elements in meter grouped by the boundary (bboxes, bcircles, or bpolys)")
  @RequestMapping(value = "/groupBy/boundary", method = RequestMethod.GET,
      produces = "application/json")
  public GroupByBoundaryResponse getPerimeterGroupByBoundary(
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

    return ElementsRequestExecutor.executeLengthPerimeterAreaGroupByBoundary(
        RequestResource.PERIMETER, false, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata);
  }

  /**
   * GET request giving the perimeter of polygonal OSM objects grouped by the key.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param groupByKeys <code>String</code> array containing the key used to create the tags for the
   *        grouping. One or more keys can be provided.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByKeyResponse
   *         GroupByKeyResponseContent}
   */
  @ApiOperation(value = "Count of OSM elements grouped by the key")
  @ApiImplicitParams({@ApiImplicitParam(name = "groupByKeys", value = keysDescr,
      defaultValue = "building", paramType = "query", dataType = "string", required = true)})
  @RequestMapping(value = "/groupBy/key", method = RequestMethod.GET, produces = "application/json")
  public GroupByKeyResponse getPerimeterGroupByKey(
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
      @RequestParam(value = "groupByKeys", defaultValue = "",
          required = false) String[] groupByKeys)
      throws UnsupportedOperationException, Exception {

    return ElementsRequestExecutor.executeLengthPerimeterAreaGroupByKey(RequestResource.PERIMETER,
        false, bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata,
        groupByKeys);
  }

  /**
   * GET request giving the perimeter of polygonal OSM objects grouped by the tag.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param groupByKey <code>String</code> array containing the key used to create the tags for the
   *        grouping. At the current implementation, there must be one key given (not more and not
   *        less).
   * @param groupByValues <code>String</code> array containing the values used to create the tags
   *        for grouping. If a given value does not appear in the output, then there are no objects
   *        assigned to it (within the given filters).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByTagResponse
   *         GroupByTagResponseContent}
   */
  @ApiOperation(value = "Perimeter of OSM elements grouped by the tag")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = keysDescr, defaultValue = "building",
          paramType = "query", dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = valuesDescr, defaultValue = "",
          paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/groupBy/tag", method = RequestMethod.GET, produces = "application/json")
  public GroupByTagResponse getPerimeterGroupByTag(
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
      @RequestParam(value = "groupByKey", defaultValue = "", required = false) String[] groupByKey,
      @RequestParam(value = "groupByValues", defaultValue = "",
          required = false) String[] groupByValues)
      throws UnsupportedOperationException, Exception {

    return ElementsRequestExecutor.executeLengthPerimeterAreaGroupByTag(RequestResource.PERIMETER,
        false, false, bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata,
        groupByKey, groupByValues);
  }

  /**
   * GET request giving the perimeter of items satisfying keys, values (+ other params) and part of
   * items satisfying keys2, values2.(+ other params).
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param keys2 <code>String</code> array having the same format as keys and used to define the
   *        subgroup(share).
   * @param values2 <code>String</code> array having the same format as values and used to define
   *        the subgroup(share).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.ShareResponse
   *         ShareResponse}
   */
  @ApiOperation(
      value = "Share of perimeter of elements satisfying keys2 and values2 within elements selected by types, keys and values")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "keys2", value = keysDescr, defaultValue = "addr:street",
          paramType = "query", dataType = "string", required = true),
      @ApiImplicitParam(name = "values2", value = valuesDescr, defaultValue = "",
          paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/share", method = RequestMethod.GET, produces = "application/json")
  public ShareResponse getPerimeterShare(
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
      @RequestParam(value = "keys2", defaultValue = "", required = false) String[] keys2,
      @RequestParam(value = "values2", defaultValue = "", required = false) String[] values2)
      throws UnsupportedOperationException, Exception {

    return ElementsRequestExecutor.executeLengthPerimeterAreaShare(RequestResource.PERIMETER, false,
        bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata, keys2, values2);
  }

  /**
   * GET request giving the perimeter of items satisfying keys, values (+ other params) and part of
   * items satisfying keys2, values2 (plus other parameters), grouped by the boundary.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param keys2 <code>String</code> array having the same format as keys and used to define the
   *        subgroup(share).
   * @param values2 <code>String</code> array having the same format as values and used to define
   *        the subgroup(share).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.ShareGroupByBoundaryResponse
   *         ShareGroupByBoundaryResponse}
   */
  @ApiOperation(value = "Share results of OSM elements grouped by the boundary")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "keys2", value = keysDescr, defaultValue = "addr:street",
          paramType = "query", dataType = "string", required = true),
      @ApiImplicitParam(name = "values2", value = valuesDescr, defaultValue = "",
          paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/share/groupBy/boundary", method = RequestMethod.GET,
      produces = "application/json")
  public ShareGroupByBoundaryResponse getPerimeterShareGroupByBoundary(
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
      @RequestParam(value = "keys2", defaultValue = "", required = false) String[] keys2,
      @RequestParam(value = "values2", defaultValue = "", required = false) String[] values2)
      throws UnsupportedOperationException, Exception {

    return ElementsRequestExecutor.executeLengthPerimeterAreaShareGroupByBoundary(
        RequestResource.PERIMETER, false, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata, keys2, values2);
  }

  /**
   * GET request giving the density of selected items (perimeter of items per square-kilometers).
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   */
  @ApiOperation(value = "Density of OSM elements (perimeter of elements per square-kilometers)")
  @RequestMapping(value = "/density", method = RequestMethod.GET, produces = "application/json")
  public DefaultAggregationResponse getPerimeterDensity(
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

    return ElementsRequestExecutor.executeLengthPerimeterArea(RequestResource.PERIMETER, false,
        true, bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata);
  }

  /**
   * GET request giving the density of selected items (perimeter of items per square-kilometers)
   * grouped by the OSM type.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByTypeResponse
   *         GroupByTypeResponseContent}
   */
  @ApiOperation(
      value = "Density of OSM elements (perimeter of items per square-kilometers) grouped by the type")
  @RequestMapping(value = "density/groupBy/type", method = RequestMethod.GET,
      produces = "application/json")
  public GroupByTypeResponse getPerimeterDensityGroupByType(
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

    return ElementsRequestExecutor.executePerimeterAreaGroupByType(RequestResource.PERIMETER, false,
        true, bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata);
  }

  /**
   * GET request giving the density of selected items (perimeter of items per square-kilometers)
   * grouped by the tag.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param groupByKey <code>String</code> array containing the key used to create the tags for the
   *        grouping. At the current implementation, there must be one key given (not more and not
   *        less).
   * @param groupByValues <code>String</code> array containing the values used to create the tags
   *        for grouping. If a given value does not appear in the output, then there are no objects
   *        assigned to it (within the given filters).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByTagResponse
   *         GroupByTagResponseContent}
   */
  @ApiOperation(
      value = "Density of selected items (perimeter of items per square-kilometers) grouped by the tag")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "groupByKey", value = keysDescr, defaultValue = "building",
          paramType = "query", dataType = "string", required = true),
      @ApiImplicitParam(name = "groupByValues", value = valuesDescr, defaultValue = "",
          paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/density/groupBy/tag", method = RequestMethod.GET,
      produces = "application/json")
  public GroupByTagResponse getPerimeterDensityGroupByTag(
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
      @RequestParam(value = "groupByKey", defaultValue = "", required = false) String[] groupByKey,
      @RequestParam(value = "groupByValues", defaultValue = "",
          required = false) String[] groupByValues)
      throws UnsupportedOperationException, Exception {

    return ElementsRequestExecutor.executeLengthPerimeterAreaGroupByTag(RequestResource.PERIMETER,
        false, true, bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata,
        groupByKey, groupByValues);
  }

  /**
   * GET request giving the ratio of selected items satisfying types2, keys2 and values2 within
   * items selected by types, keys and values.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param types2 <code>String</code> array having the same format as types.
   * @param keys2 <code>String</code> array having the same format as keys.
   * @param values2 <code>String</code> array having the same format as values.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.RatioResponse
   *         RatioResponse}
   */
  @ApiOperation(
      value = "Ratio of selected items satisfying types2, keys2 and values2 within items selected by types, keys and values")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "types2", value = typesDescr, defaultValue = "way",
          paramType = "query", dataType = "string", required = false),
      @ApiImplicitParam(name = "keys2", value = keysDescr, defaultValue = "building",
          paramType = "query", dataType = "string", required = false),
      @ApiImplicitParam(name = "values2", value = valuesDescr, defaultValue = "residential",
          paramType = "query", dataType = "string", required = false)})
  @RequestMapping(value = "/ratio", method = RequestMethod.GET, produces = "application/json")
  public RatioResponse getAreaRatio(
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
      @RequestParam(value = "types2", defaultValue = "", required = false) String[] types2,
      @RequestParam(value = "keys2", defaultValue = "", required = false) String[] keys2,
      @RequestParam(value = "values2", defaultValue = "", required = false) String[] values2)
      throws UnsupportedOperationException, Exception {

    return ElementsRequestExecutor.executeLengthPerimeterAreaRatio(RequestResource.PERIMETER, false,
        bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata, types2, keys2,
        values2);
  }

  /**
   * POST request giving the perimeter of polygonal OSM objects. POST requests should only be used
   * if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   */
  @ApiOperation(value = "Perimeter of OSM elements")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
          defaultValue = "8.6128,49.3183,8.7294,49.4376", required = false, value = bboxesDescr),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false, value = bcirclesDescr),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = bpolysDescr),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way", required = false, value = typesDescr),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = "building", required = false, value = keysDescr),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string",
          defaultValue = "residential", required = false, value = valuesDescr),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = useridsDescr),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = "2010-01-01/2017-01-01/P1Y", required = false, value = timeDescr),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = "true", required = false, value = showMetadataDescr)})
  @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public DefaultAggregationResponse postPerimeter(String bboxes, String bcircles, String bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata) throws UnsupportedOperationException, Exception {

    return ElementsRequestExecutor.executeLengthPerimeterArea(RequestResource.PERIMETER, true,
        false, bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata);
  }

  /**
   * POST request giving the perimeter of polygonal OSM objects grouped by the OSM type. POST
   * requests should only be used if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByTypeResponse
   *         GroupByTypeResponseContent}
   */
  @ApiOperation(value = "Perimeter of OSM elements grouped by the type")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
          defaultValue = "8.6128,49.3183,8.7294,49.4376", required = false, value = bboxesDescr),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false, value = bcirclesDescr),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = bpolysDescr),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way, relation", required = false, value = typesDescr),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = "building", required = false, value = keysDescr),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string", defaultValue = "",
          required = false, value = valuesDescr),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = useridsDescr),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = "2010-01-01/2017-01-01/P1Y", required = false, value = timeDescr),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = "true", required = false, value = showMetadataDescr)})
  @RequestMapping(value = "/groupBy/type", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByTypeResponse postPerimeterGroupByType(String bboxes, String bcircles, String bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata) throws UnsupportedOperationException, Exception, BadRequestException {

    return ElementsRequestExecutor.executePerimeterAreaGroupByType(RequestResource.PERIMETER, true,
        false, bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata);
  }

  /**
   * POST request giving the perimeter of polygonal OSM objects grouped by the userID. POST requests
   * should only be used if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByUserResponse
   *         GroupByUserResponseContent}
   */
  @ApiOperation(value = "Perimeter of OSM elements grouped by the user")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
          defaultValue = "8.6128,49.3183,8.7294,49.4376", required = false, value = bboxesDescr),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false, value = bcirclesDescr),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = bpolysDescr),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way", required = false, value = typesDescr),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = "building", required = false, value = keysDescr),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string",
          defaultValue = "residential", required = false, value = valuesDescr),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = useridsDescr),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = "2010-01-01/2017-01-01/P1Y", required = false, value = timeDescr),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = "true", required = false, value = showMetadataDescr)})
  @RequestMapping(value = "/groupBy/user", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByUserResponse postPerimeterGroupByUser(String bboxes, String bcircles, String bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata) throws UnsupportedOperationException, Exception, BadRequestException {

    return ElementsRequestExecutor.executeLengthPerimeterAreaGroupByUser(RequestResource.PERIMETER,
        true, bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata);
  }

  /**
   * POST request giving the perimeter of polygonal OSM objects grouped by the boundary parameter
   * (bounding box/circle/polygon). POST requests should only be used if the request URL would be
   * too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByBoundaryResponse
   *         GroupByBoundaryResponseContent}
   */
  @ApiOperation(
      value = "Perimeter of OSM elements grouped by the boundary (bboxes, bcircles, or bpolys)")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
          defaultValue = "8.6128,49.3183,8.7294,49.4376", required = false, value = bboxesDescr),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false, value = bcirclesDescr),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = bpolysDescr),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way", required = false, value = typesDescr),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = "building", required = false, value = keysDescr),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string",
          defaultValue = "residential", required = false, value = valuesDescr),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = useridsDescr),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = "2010-01-01/2017-01-01/P1Y", required = false, value = timeDescr),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = "true", required = false, value = showMetadataDescr)})
  @RequestMapping(value = "/groupBy/boundary", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByBoundaryResponse postPerimeterGroupByBoundary(String bboxes, String bcircles,
      String bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata)
      throws UnsupportedOperationException, Exception, BadRequestException {

    return ElementsRequestExecutor.executeLengthPerimeterAreaGroupByBoundary(
        RequestResource.PERIMETER, true, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata);
  }

  /**
   * POST request giving the perimeter of polygonal OSM objects grouped by the key. POST requests
   * should only be used if the request URL would be too long for a GET request.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param groupByKeys <code>String</code> array containing the key used to create the tags for the
   *        grouping. At the current implementation, there must be one key given (not more and not
   *        less).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByTagResponse
   *         GroupByTagResponseContent}
   */
  @ApiOperation(value = "Perimeter of OSM elements grouped by the key")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
          defaultValue = "8.6128,49.3183,8.7294,49.4376", required = false, value = bboxesDescr),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false, value = bcirclesDescr),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = bpolysDescr),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way", required = false, value = typesDescr),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string", defaultValue = "",
          required = false, value = keysDescr),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string", defaultValue = "",
          required = false, value = valuesDescr),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = useridsDescr),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = "2010-01-01/2017-01-01/P1Y", required = false, value = timeDescr),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = "true", required = false, value = showMetadataDescr),
      @ApiImplicitParam(name = "groupByKeys", paramType = "form", dataType = "string",
          defaultValue = "addr:street", required = true, value = keysDescr)})
  @RequestMapping(value = "/groupBy/key", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByKeyResponse postPerimeterGroupByKey(String bboxes, String bcircles, String bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata, String[] groupByKeys)
      throws UnsupportedOperationException, Exception, BadRequestException {

    return ElementsRequestExecutor.executeLengthPerimeterAreaGroupByKey(RequestResource.PERIMETER,
        true, bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata,
        groupByKeys);
  }

  /**
   * POST request giving the perimeter of OSM objects grouped by the tag. POST requests should only
   * be used if the request URL would be too long for a GET request.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param groupByKey <code>String</code> array containing the key used to create the tags for the
   *        grouping. At the current implementation, there must be one key given (not more and not
   *        less).
   * @param groupByValues <code>String</code> array containing the values used to create the tags
   *        for grouping. If a given value does not appear in the output, then there are no objects
   *        assigned to it (within the given filters).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByTagResponse
   *         GroupByTagResponseContent}
   */
  @ApiOperation(value = "Perimeter of OSM elements grouped by the tag")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
          defaultValue = "8.6128,49.3183,8.7294,49.4376", required = false, value = bboxesDescr),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false, value = bcirclesDescr),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = bpolysDescr),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way", required = false, value = typesDescr),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = "building", required = false, value = keysDescr),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string", defaultValue = "",
          required = false, value = valuesDescr),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = useridsDescr),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = "2010-01-01/2017-01-01/P1Y", required = false, value = timeDescr),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = "true", required = false, value = showMetadataDescr),
      @ApiImplicitParam(name = "groupByKey", paramType = "form", dataType = "string",
          defaultValue = "addr:street", required = true, value = keysDescr),
      @ApiImplicitParam(name = "groupByValues", paramType = "form", dataType = "string",
          defaultValue = "", required = false, value = valuesDescr)})
  @RequestMapping(value = "/groupBy/tag", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByTagResponse postPerimeterGroupByTag(String bboxes, String bcircles, String bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata, String[] groupByKey, String[] groupByValues)
      throws UnsupportedOperationException, Exception, BadRequestException {

    return ElementsRequestExecutor.executeLengthPerimeterAreaGroupByTag(RequestResource.PERIMETER,
        true, false, bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata,
        groupByKey, groupByValues);
  }

  /**
   * POST request giving the share of perimeter of items satisfying keys, values (+ other params)
   * and part of items satisfying keys2, values2.(+ other params). POST requests should only be used
   * if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param keys2 <code>String</code> array having the same format as keys and used to define the
   *        subgroup(share).
   * @param values2 <code>String</code> array having the same format as values and used to define
   *        the subgroup(share).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.ShareResponse
   *         ShareResponse}
   */
  @ApiOperation(
      value = "Share of perimeter of elements satisfying keys2 and values2 within elements selected by types, keys and values")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
          defaultValue = "8.6128,49.3183,8.7294,49.4376", required = false, value = bboxesDescr),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false, value = bcirclesDescr),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = bpolysDescr),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way", required = false, value = typesDescr),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = "building", required = false, value = keysDescr),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string", defaultValue = "",
          required = false, value = valuesDescr),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = useridsDescr),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = "2010-01-01/2017-01-01/P1Y", required = false, value = timeDescr),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = "true", required = false, value = showMetadataDescr),
      @ApiImplicitParam(name = "keys2", paramType = "form", dataType = "string",
          defaultValue = "building", required = true, value = keysDescr),
      @ApiImplicitParam(name = "values2", paramType = "form", dataType = "string",
          defaultValue = "residential", required = false, value = valuesDescr)})
  @RequestMapping(value = "/share", method = RequestMethod.POST, produces = "application/json",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ShareResponse postPerimeterShare(String bboxes, String bcircles, String bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata, String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception, BadRequestException {

    return ElementsRequestExecutor.executeLengthPerimeterAreaShare(RequestResource.PERIMETER, true,
        bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata, keys2, values2);
  }

  /**
   * POST request giving the perimeter of items satisfying keys, values and part of items satisfying
   * keys2, values2, grouped by the boundary. POST requests should only be used if the request URL
   * would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param keys2 <code>String</code> array having the same format as keys and used to define the
   *        subgroup(share).
   * @param values2 <code>String</code> array having the same format as values and used to define
   *        the subgroup(share).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.ShareGroupByBoundaryResponse
   *         ShareGroupByBoundaryResponse}
   */
  @ApiOperation(value = "Share results of OSM elements grouped by the boundary")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
          defaultValue = "8.6128,49.3183,8.7294,49.4376", required = false, value = bboxesDescr),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false, value = bcirclesDescr),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = bpolysDescr),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way", required = false, value = typesDescr),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = "building", required = false, value = keysDescr),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string", defaultValue = "",
          required = false, value = valuesDescr),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = useridsDescr),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = "2010-01-01/2017-01-01/P1Y", required = false, value = timeDescr),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = "true", required = false, value = showMetadataDescr),
      @ApiImplicitParam(name = "keys2", paramType = "form", dataType = "string",
          defaultValue = "building", required = true, value = keysDescr),
      @ApiImplicitParam(name = "values2", paramType = "form", dataType = "string",
          defaultValue = "residential", required = false, value = valuesDescr)})
  @RequestMapping(value = "/share/groupBy/boundary", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ShareGroupByBoundaryResponse postPerimeterShareGroupByBoundary(String bboxes,
      String bcircles, String bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time, String showMetadata, String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception, BadRequestException {

    return ElementsRequestExecutor.executeLengthPerimeterAreaShareGroupByBoundary(
        RequestResource.PERIMETER, true, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata, keys2, values2);
  }

  /**
   * POST request giving the density of OSM elements (perimeter of elements per square-kilometers).
   * POST requests should only be used if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   */
  @ApiOperation(value = "Density of OSM elements (perimeter of elements per square-kilometers)")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
          defaultValue = "8.6128,49.3183,8.7294,49.4376", required = false, value = bboxesDescr),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false, value = bcirclesDescr),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = bpolysDescr),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way", required = false, value = typesDescr),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = "building", required = false, value = keysDescr),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string", defaultValue = "",
          required = false, value = valuesDescr),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = useridsDescr),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = "2010-01-01/2017-01-01/P1Y", required = false, value = timeDescr),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = "true", required = false, value = showMetadataDescr)})
  @RequestMapping(value = "/density", method = RequestMethod.POST, produces = "application/json",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public DefaultAggregationResponse postPerimeterDensity(String bboxes, String bcircles,
      String bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata)
      throws UnsupportedOperationException, Exception, BadRequestException {

    return ElementsRequestExecutor.executeLengthPerimeterArea(RequestResource.PERIMETER, true, true,
        bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata);
  }

  /**
   * POST request giving the density of OSM elements (perimeter of items per square-kilometers)
   * grouped by the type. POST requests should only be used if the request URL would be too long for
   * a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByTypeResponse
   *         GroupByTypeResponseContent}
   */
  @ApiOperation(
      value = "Density of OSM elements (perimeter of items per square-kilometers) grouped by the type")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
          defaultValue = "8.6128,49.3183,8.7294,49.4376", required = false, value = bboxesDescr),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false, value = bcirclesDescr),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = bpolysDescr),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way, relation", required = false, value = typesDescr),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = "building", required = false, value = keysDescr),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string", defaultValue = "",
          required = false, value = valuesDescr),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = useridsDescr),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = "2010-01-01/2017-01-01/P1Y", required = false, value = timeDescr),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = "true", required = false, value = showMetadataDescr)})
  @RequestMapping(value = "/density/groupBy/type", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByTypeResponse postPerimeterDensityGroupByType(String bboxes, String bcircles,
      String bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata)
      throws UnsupportedOperationException, Exception, BadRequestException {

    return ElementsRequestExecutor.executePerimeterAreaGroupByType(RequestResource.PERIMETER, true,
        true, bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata);
  }

  /**
   * POST request giving the density of selected items (perimeter of items per square-kilometers)
   * grouped by the tag. POST requests should only be used if the request URL would be too long for
   * a GET request.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param groupByKey <code>String</code> array containing the key used to create the tags for the
   *        grouping. At the current implementation, there must be one key given (not more and not
   *        less).
   * @param groupByValues <code>String</code> array containing the values used to create the tags
   *        for grouping. If a given value does not appear in the output, then there are no objects
   *        assigned to it (within the given filters).
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.GroupByTagResponse
   *         GroupByTagResponseContent}
   */
  @ApiOperation(
      value = "Density of selected items (perimeter of items per square-kilometers) grouped by the tag")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
          defaultValue = "8.6128,49.3183,8.7294,49.4376", required = false, value = bboxesDescr),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false, value = bcirclesDescr),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = bpolysDescr),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way", required = false, value = typesDescr),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = "building", required = false, value = keysDescr),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string", defaultValue = "",
          required = false, value = valuesDescr),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = useridsDescr),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = "2010-01-01/2017-01-01/P1Y", required = false, value = timeDescr),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = "true", required = false, value = showMetadataDescr),
      @ApiImplicitParam(name = "groupByKey", paramType = "form", dataType = "string",
          defaultValue = "building", required = true, value = keysDescr),
      @ApiImplicitParam(name = "groupByValues", paramType = "form", dataType = "string",
          defaultValue = "", required = false, value = valuesDescr)})
  @RequestMapping(value = "/density/groupBy/tag", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByTagResponse postPerimeterDensityGroupByTag(String bboxes, String bcircles,
      String bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata, String[] groupByKey, String[] groupByValues)
      throws UnsupportedOperationException, Exception, BadRequestException {

    return ElementsRequestExecutor.executeLengthPerimeterAreaGroupByTag(RequestResource.PERIMETER,
        true, true, bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata,
        groupByKey, groupByValues);
  }

  /**
   * POST request giving the ratio of selected items satisfying types2, keys2 and values2 within
   * items selected by types, keys and values. POST requests should only be used if the request URL
   * would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param types2 <code>String</code> array having the same format as types.
   * @param keys2 <code>String</code> array having the same format as keys.
   * @param values2 <code>String</code> array having the same format as values.
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.RatioResponse
   *         RatioResponse}
   */
  @ApiOperation(
      value = "Ratio of selected items satisfying types2, keys2 and values2 within items selected by types, keys and values")
  @ApiImplicitParams({
      @ApiImplicitParam(name = "bboxes", paramType = "form", dataType = "string",
          defaultValue = "8.6128,49.3183,8.7294,49.4376", required = false, value = bboxesDescr),
      @ApiImplicitParam(name = "bcircles", paramType = "form", dataType = "string",
          required = false, value = bcirclesDescr),
      @ApiImplicitParam(name = "bpolys", paramType = "form", dataType = "string", required = false,
          value = bpolysDescr),
      @ApiImplicitParam(name = "types", paramType = "form", dataType = "string",
          defaultValue = "way", required = false, value = typesDescr),
      @ApiImplicitParam(name = "keys", paramType = "form", dataType = "string",
          defaultValue = "building", required = false, value = keysDescr),
      @ApiImplicitParam(name = "values", paramType = "form", dataType = "string", defaultValue = "",
          required = false, value = valuesDescr),
      @ApiImplicitParam(name = "userids", paramType = "form", dataType = "string", required = false,
          value = useridsDescr),
      @ApiImplicitParam(name = "time", paramType = "form", dataType = "string",
          defaultValue = "2010-01-01/2017-01-01/P1Y", required = false, value = timeDescr),
      @ApiImplicitParam(name = "showMetadata", paramType = "form", dataType = "string",
          defaultValue = "true", required = false, value = showMetadataDescr),
      @ApiImplicitParam(name = "types2", value = typesDescr, defaultValue = "way",
          paramType = "query", dataType = "string", required = false),
      @ApiImplicitParam(name = "keys2", paramType = "form", dataType = "string",
          defaultValue = "building", required = false, value = keysDescr),
      @ApiImplicitParam(name = "values2", paramType = "form", dataType = "string",
          defaultValue = "yes", required = false, value = valuesDescr)})
  @RequestMapping(value = "/ratio", method = RequestMethod.POST, produces = "application/json",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public RatioResponse postPerimeterRatio(String bboxes, String bcircles, String bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata, String[] types2, String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception, BadRequestException {

    return ElementsRequestExecutor.executeLengthPerimeterAreaRatio(RequestResource.PERIMETER, true,
        bboxes, bcircles, bpolys, types, keys, values, userids, time, showMetadata, types2, keys2,
        values2);
  }
}
