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
 * REST controller containing the method, which is mapped to "/elements" and used to return OSM
 * data.
 */
@Api(tags = "dataExtraction")
@RestController
@RequestMapping("/elements")
public class ElementsController {

  /**
   * Gives the OSM objects as GeoJSON features. Depending on the used resource, the geometry of the
   * response consists either of their pure geometry, their bounding box, or their centroid.
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
  @ApiOperation(value = "OSM Data", nickname = "rawData")
  @ApiImplicitParam(name = "properties", value = ParameterDescriptions.PROPERTIES_DESCR,
      defaultValue = "tags", paramType = "query", dataType = "string", required = false)
  @RequestMapping(value = {"/geometry", "/bbox", "/centroid"},
      method = {RequestMethod.GET, RequestMethod.POST})
  public void retrieveOSMData(
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] types,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] keys,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] values,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] userids,
      @ApiParam(hidden = true) @RequestParam(defaultValue = "") String[] time,
      @ApiParam(hidden = true) HttpServletRequest request,
      @ApiParam(hidden = true) HttpServletResponse response)
      throws UnsupportedOperationException, Exception {
    ElementsGeometry elemGeomType;
    switch (request.getRequestURI()) {
      case "/elements/geometry":
        elemGeomType = ElementsGeometry.RAW;
        break;
      case "/elements/bbox":
        elemGeomType = ElementsGeometry.BBOX;
        break;
      case "/elements/centroid":
        elemGeomType = ElementsGeometry.CENTROID;
        break;
      default:
        elemGeomType = ElementsGeometry.RAW;
        break;
    }
    ElementsRequestExecutor.executeElements(
        new RequestParameters(request.getMethod(), true, false, request.getParameter("bboxes"),
            request.getParameter("bcircles"), request.getParameter("bpolys"), types, keys, values,
            userids, time, request.getParameter("showMetadata")),
        elemGeomType, request.getParameterValues("properties"), response);
  }
}
