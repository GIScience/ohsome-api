package org.heigit.ohsome.ohsomeapi.controller.dataextraction.elements;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor;
import org.heigit.ohsome.ohsomeapi.output.ExtractionResponse;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operator;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.extraction.ElementsExtraction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller containing the methods, which are mapped to "/elements" and used to return OSM
 * data.
 */
@Api(tags = "Elements Extraction")
@RestController
@RequestMapping("/elements")
public class ElementsController {

  @Autowired
  ElementsRequestExecutor elementsRequestExecutor;
  @Autowired
  ElementsExtraction elementsExtraction;
  @Autowired
  Operator operator;

  /**
   * Gives the OSM objects as GeoJSON features, which have the geometry of the respective objects in
   * the geometry field.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #extract(RequestResource, ElementsGeometry, HttpServletRequest, HttpServletResponse)
   *         extract}
   */
  @ApiOperation(value = "OSM DataProcessingType having the raw geometry of each OSM object as geometry",
      nickname = "elementsGeometry", response = ExtractionResponse.class)
  @RequestMapping(value = "/geometry", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public void elementsGeometry(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    //ElementsRequestExecutor executor = new ElementsRequestExecutor();
    operator.setOperation(elementsExtraction);
    operator.compute();
//    elementsRequestExecutor.extract(RequestResource.DATAEXTRACTION, ElementsGeometry.RAW,
//        servletRequest, servletResponse);
  }

  /**
   * Gives the OSM objects as GeoJSON features, which have the bounding box of the respective
   * objects in the geometry field.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #extract(RequestResource, ElementsGeometry, HttpServletRequest, HttpServletResponse)
   *         extract}
   */
  @ApiOperation(value = "OSM DataProcessingType, having the bounding box of each OSM object as geometry",
      nickname = "elementsBbox", response = ExtractionResponse.class)
  @RequestMapping(value = "/bbox", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public void elementsBbox(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
      throws Exception {
    //ElementsRequestExecutor executor = new ElementsRequestExecutor();
    operator.setOperation(elementsExtraction);
    operator.compute();
//    elementsRequestExecutor.extract(RequestResource.DATAEXTRACTION, ElementsGeometry.BBOX,
//        servletRequest, servletResponse);
  }

  /**
   * Gives the OSM objects as GeoJSON features, which have the centroid of the respective objects in
   * the geometry field.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.ElementsRequestExecutor
   *         #extract(RequestResource, ElementsGeometry, HttpServletRequest, HttpServletResponse)
   *         extract}
   */
  @ApiOperation(value = "OSM DataProcessingType, having the centroid of each OSM object as geometry",
      nickname = "elementsCentroid", response = ExtractionResponse.class)
  @RequestMapping(value = "/centroid", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public void elementsCentroid(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    //ElementsRequestExecutor executor = new ElementsRequestExecutor();
    operator.setOperation(elementsExtraction);
    operator.compute();
//    elementsRequestExecutor.extract(RequestResource.DATAEXTRACTION, ElementsGeometry.CENTROID,
//        servletRequest, servletResponse);
  }
}
