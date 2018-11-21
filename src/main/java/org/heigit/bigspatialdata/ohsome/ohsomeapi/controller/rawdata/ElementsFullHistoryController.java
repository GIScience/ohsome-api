package org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.rawdata;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.ParameterDescriptions;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.ElementsRequestExecutor;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.RequestParameters;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;


/**
 * REST controller containing the methods, which are mapped to "/elementsFullHistory" and used to
 * return the full history of the requested OSM data.
 */
@Api(tags = "dataExtractionFullHistory")
@RestController
@RequestMapping("/elementsFullHistory")
public class ElementsFullHistoryController {

  /**
   * Gives the OSM objects as GeoJSON features, which have the geometry of the respective objects in
   * the geometry field.
   * 
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @param properties <code>String</code> array defining what types of properties should be
   *        included within the properties response field. It can contain "tags" and/or "metadata",
   *        meaning that it would add the OSM-tags or metadata of the respective OSM object to the
   *        properties.
   */
  @ApiOperation(value = "OSM Data having the raw geometry of each OSM object as geometry",
      nickname = "rawDataFullHistory")
  @ApiImplicitParam(name = "properties", value = ParameterDescriptions.PROPERTIES_DESCR,
      defaultValue = "tags", paramType = "query", dataType = "string", required = false)
  @RequestMapping(value = "/geometry", method = {RequestMethod.GET, RequestMethod.POST})
  public void retrieveOSMDataRawFullHistory(
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] properties,
      @ApiParam(hidden = true) HttpServletRequest request,
      @ApiParam(hidden = true) HttpServletResponse response)
      throws UnsupportedOperationException, Exception {
    ElementsRequestExecutor.executeElements(
        new RequestParameters(request.getMethod(), false, false, request.getParameter("bboxes"),
            request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys, values,
            userids, time, request.getParameter("showMetadata")),
        ElementsGeometry.RAW, properties, response);
  }

  /**
   * Gives the OSM objects as GeoJSON features, which have the bounding box of the respective
   * objects in the geometry field.
   * 
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @param properties <code>String</code> array defining what types of properties should be
   *        included within the properties response field. It can contain "tags" and/or "metadata",
   *        meaning that it would add the OSM-tags or metadata of the respective OSM object to the
   *        properties.
   */
  @ApiOperation(value = "OSM Data, having the bounding box of each OSM object as geometry",
      nickname = "rawDataBboxFullHistory")
  @ApiImplicitParam(name = "properties", value = ParameterDescriptions.PROPERTIES_DESCR,
      defaultValue = "tags", paramType = "query", dataType = "string", required = false)
  @RequestMapping(value = "/bbox", method = {RequestMethod.GET, RequestMethod.POST})
  public void retrieveOSMDataBbox(
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] properties,
      @ApiParam(hidden = true) HttpServletRequest request,
      @ApiParam(hidden = true) HttpServletResponse response)
      throws UnsupportedOperationException, Exception {
    ElementsRequestExecutor.executeElements(
        new RequestParameters(request.getMethod(), false, false, request.getParameter("bboxes"),
            request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys, values,
            userids, time, request.getParameter("showMetadata")),
        ElementsGeometry.BBOX, properties, response);
  }

  /**
   * Gives the OSM objects as GeoJSON features, which have the centroid of the respective objects in
   * the geometry field.
   * 
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation.CountController#count(String, String, String, String[], String[], String[], String[], String[], String, HttpServletRequest)
   * count} method.
   * 
   * @param properties <code>String</code> array defining what types of properties should be
   *        included within the properties response field. It can contain "tags" and/or "metadata",
   *        meaning that it would add the OSM-tags or metadata of the respective OSM object to the
   *        properties.
   */
  @ApiOperation(value = "OSM Data, having the centroid of each OSM object as geometry",
      nickname = "rawDataCentroidFullHistory")
  @ApiImplicitParam(name = "properties", value = ParameterDescriptions.PROPERTIES_DESCR,
      defaultValue = "tags", paramType = "query", dataType = "string", required = false)
  @RequestMapping(value = "/centroid", method = {RequestMethod.GET, RequestMethod.POST})
  public void retrieveOSMDataCentroid(
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] properties,
      @ApiParam(hidden = true) HttpServletRequest request,
      @ApiParam(hidden = true) HttpServletResponse response)
      throws UnsupportedOperationException, Exception {
    ElementsRequestExecutor.executeElements(
        new RequestParameters(request.getMethod(), false, false, request.getParameter("bboxes"),
            request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys, values,
            userids, time, request.getParameter("showMetadata")),
        ElementsGeometry.CENTROID, properties, response);
  }
}
