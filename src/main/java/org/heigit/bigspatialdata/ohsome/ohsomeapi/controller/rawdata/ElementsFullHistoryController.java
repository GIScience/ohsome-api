package org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.rawdata;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.ElementsRequestExecutor;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.output.rawdataresponse.DataResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;


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
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   */
  @ApiOperation(
      value = "Full-history OSM data having the raw geometry of each OSM object as geometry",
      nickname = "fullHistory", response = DataResponse.class)
  @RequestMapping(value = "/geometry", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public void elementsFullHistory(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    ElementsRequestExecutor.executeElementsFullHistory(ElementsGeometry.RAW, servletRequest,
        servletResponse);
  }

  /**
   * Gives the OSM objects as GeoJSON features, which have the bounding box of the respective
   * objects in the geometry field.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   */
  @ApiOperation(
      value = "Full-history OSM data, having the bounding box of each OSM object as geometry",
      nickname = "bboxFullHistory", response = DataResponse.class)
  @RequestMapping(value = "/bbox", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public void elementsBboxFullHistory(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    ElementsRequestExecutor.executeElementsFullHistory(ElementsGeometry.BBOX, servletRequest,
        servletResponse);
  }

  /**
   * Gives the OSM objects as GeoJSON features, which have the centroid of the respective objects in
   * the geometry field.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   */
  @ApiOperation(value = "Full-history OSM data, having the centroid of each OSM object as geometry",
      nickname = "dentroidFullHistory", response = DataResponse.class)
  @RequestMapping(value = "/centroid", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public void elementsCentroidFullHistory(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    ElementsRequestExecutor.executeElementsFullHistory(ElementsGeometry.CENTROID, servletRequest,
        servletResponse);
  }
}
