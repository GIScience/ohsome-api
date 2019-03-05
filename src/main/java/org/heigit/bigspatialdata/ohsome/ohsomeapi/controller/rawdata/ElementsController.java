package org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.rawdata;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.ElementsRequestExecutor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST controller containing the methods, which are mapped to "/elements" and used to return OSM
 * data.
 */
@Api(tags = "dataExtraction")
@RestController
@RequestMapping("/elements")
public class ElementsController {

  /**
   * Gives the OSM objects as GeoJSON features, which have the geometry of the respective objects in
   * the geometry field.
   */
  @ApiOperation(value = "OSM Data having the raw geometry of each OSM object as geometry",
      nickname = "rawData")
  @RequestMapping(value = "/geometry", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public void retrieveOSMDataRaw(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    ElementsRequestExecutor.executeElements(ElementsGeometry.RAW, servletRequest, servletResponse);
  }

  /**
   * Gives the OSM objects as GeoJSON features, which have the bounding box of the respective
   * objects in the geometry field.
   */
  @ApiOperation(value = "OSM Data, having the bounding box of each OSM object as geometry",
      nickname = "rawDataBbox")
  @RequestMapping(value = "/bbox", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public void retrieveOSMDataBbox(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    ElementsRequestExecutor.executeElements(ElementsGeometry.BBOX, servletRequest, servletResponse);
  }

  /**
   * Gives the OSM objects as GeoJSON features, which have the centroid of the respective objects in
   * the geometry field.
   */
  @ApiOperation(value = "OSM Data, having the centroid of each OSM object as geometry",
      nickname = "rawDataCentroid")
  @RequestMapping(value = "/centroid", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public void retrieveOSMDataCentroid(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    ElementsRequestExecutor.executeElements(ElementsGeometry.CENTROID, servletRequest,
        servletResponse);
  }
}
