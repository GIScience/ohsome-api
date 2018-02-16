package org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements;

import org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.executor.ElementsRequestExecutor;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponseContent;
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
 * "/elements/density".
 */
@Api(tags = "density")
@RestController
@RequestMapping("/elements/density")
public class DensityController {

  /**
   * GET request giving the density of selected items (number of items per area).
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponseContent
   *         ElementsResponseContent}
   */
  @ApiOperation(value = "Density of OSM elements (number of elements per area)")
  @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
  public DefaultAggregationResponseContent getDensity(
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
    return executor.executeDensity(false, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata);
  }

  /**
   * POST request giving the density of OSM objects. POST requests should only be used if the
   * request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.DefaultAggregationResponseContent
   *         ElementsResponseContent}
   */
  @ApiOperation(value = "Density of OSM elements (number of elements per area)")
  @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public DefaultAggregationResponseContent postDensity(
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
    return executor.executeDensity(true, bboxes, bcircles, bpolys, types, keys, values, userids,
        time, showMetadata);
  }

}
