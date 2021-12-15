package org.heigit.ohsome.ohsomeapi.controller.dataextraction.elements;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.ohsome.ohsomeapi.output.ExtractionResponse;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller containing the methods, which are mapped to "/elementsFullHistory" and used to
 * return the full history of the requested OSM data.
 */
@Api(tags = "Full History Elements Extraction")
@RestController
@RequestMapping("/elementsFullHistory")
public class ElementsFullHistoryController {

//  @Autowired
//  DataRequestExecutor dataRequestExecutor;
//  @Autowired
//  ElementsFullHistory elementsFullHistory;
  @Autowired
  Operator operator;

  /**
   * Gives the OSM objects as GeoJSON features, which have the geometry of the respective objects in
   * the geometry field.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.DataRequestExecutor
   *         #extract() extract}
   */
  @ApiOperation(
      value = "Full history OSM data having the raw geometry of each OSM object as geometry",
      nickname = "elementsFullHistory", response = ExtractionResponse.class)
  @ApiImplicitParam(name = "time",
      value = "Two ISO-8601 conform timestrings defining an interval; no default value",
      defaultValue = "2016-01-01,2017-01-01", paramType = "query", dataType = "string",
      required = true)
  @RequestMapping(value = "/geometry", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public void elementsFullHistory(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
//    DataRequestExecutor executor = new DataRequestExecutor(RequestResource.DATAEXTRACTION,
//        ElementsGeometry.RAW);
//    operator.setOperation(elementsFullHistory);
//    operator.compute();
    //dataRequestExecutor.extract(RequestResource.DATAEXTRACTION, ElementsGeometry.RAW);
  }

  /**
   * Gives the OSM objects as GeoJSON features, which have the bounding box of the respective
   * objects in the geometry field.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.DataRequestExecutor
   *         #extract() extract}
   */
  @ApiOperation(
      value = "Full history OSM data having the bounding box of each OSM object as geometry",
      nickname = "elementsBboxFullHistory", response = ExtractionResponse.class)
  @ApiImplicitParam(name = "time",
      value = "Two ISO-8601 conform timestrings defining an interval; no default value",
      defaultValue = "2016-01-01,2017-01-01", paramType = "query", dataType = "string",
      required = true)
  @RequestMapping(value = "/bbox", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public void elementsBboxFullHistory(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
//    DataRequestExecutor executor = new DataRequestExecutor(RequestResource.DATAEXTRACTION,
//        ElementsGeometry.BBOX);
//    operator.setOperation(elementsFullHistory);
//    operator.compute();
    //dataRequestExecutor.extract(RequestResource.DATAEXTRACTION, ElementsGeometry.BBOX);
  }

  /**
   * Gives the OSM objects as GeoJSON features, which have the centroid of the respective objects in
   * the geometry field.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.DataRequestExecutor
   *         #extract() extract}
   */
  @ApiOperation(value = "Full history OSM data having the centroid of each OSM object as geometry",
      nickname = "elementsCentroidFullHistory", response = ExtractionResponse.class)
  @ApiImplicitParam(name = "time",
      value = "Two ISO-8601 conform timestrings defining an interval; no default value",
      defaultValue = "2016-01-01,2017-01-01", paramType = "query", dataType = "string",
      required = true)
  @RequestMapping(value = "/centroid", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public void elementsCentroidFullHistory(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
//    DataRequestExecutor executor = new DataRequestExecutor(RequestResource.DATAEXTRACTION,
//        ElementsGeometry.CENTROID);
//    operator.setOperation(elementsFullHistory);
//    operator.compute();
    //dataRequestExecutor.extract(RequestResource.DATAEXTRACTION, ElementsGeometry.CENTROID);
  }
}
