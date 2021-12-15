package org.heigit.ohsome.ohsomeapi.controller.dataextraction.contributions;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.ohsome.ohsomeapi.executor.DataRequestExecutor;
import org.heigit.ohsome.ohsomeapi.output.ExtractionResponse;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Latest;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operator;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.extraction.ContributionsExtraction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller containing the methods, which are mapped to "/contributions" and used to return
 * each contribution (creation, modification, deletion) of the OSM data.
 */
@Api(tags = "Contributions Extraction")
@RestController
@RequestMapping("/contributions")
public class ContributionsController {

  @Autowired
  DataRequestExecutor dataRequestExecutor;
  @Autowired
  ContributionsExtraction contributionsExtraction;
  @Autowired
  Operator operator;
  @Autowired
  Latest latest;

  /**
   * Gives the contributions as GeoJSON features, which have the geometry of the respective objects
   * in the geometry field.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.DataRequestExecutor
   *         #extract() extract}
   */
  @ApiOperation(value = "OSM contributions having the raw geometry of each OSM object as geometry",
      nickname = "contributionsGeometry", response = ExtractionResponse.class)
  @ApiImplicitParam(name = "time",
      value = "Two ISO-8601 conform timestrings defining an interval; no default value",
      defaultValue = "2016-01-01,2017-01-01", paramType = "query", dataType = "string",
      required = true)
  @RequestMapping(value = "/geometry", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public void contributions(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
      throws Exception {
    //DataRequestExecutor executor = new DataRequestExecutor(RequestResource.CONTRIBUTIONS,
      //  ElementsGeometry.RAW);
    operator.setOperation(contributionsExtraction);
    operator.compute();
    //dataRequestExecutor.extract(RequestResource.CONTRIBUTIONS, ElementsGeometry.RAW);
  }

  /**
   * Gives the contributions as GeoJSON features, which have the bounding box of the respective
   * objects in the geometry field.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.DataRequestExecutor
   *         #extract() extract}
   */
  @ApiOperation(value = "OSM contributions having the bounding box of each OSM object as geometry",
      nickname = "contributionsBbox", response = ExtractionResponse.class)
  @ApiImplicitParam(name = "time",
      value = "Two ISO-8601 conform timestrings defining an interval; no default value",
      defaultValue = "2016-01-01,2017-01-01", paramType = "query", dataType = "string",
      required = true)
  @RequestMapping(value = "/bbox", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public void contributionsBbox(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
//    DataRequestExecutor executor = new DataRequestExecutor(RequestResource.CONTRIBUTIONS,
//        ElementsGeometry.BBOX);
    operator.setOperation(contributionsExtraction);
    operator.compute();
    //dataRequestExecutor.extract(RequestResource.CONTRIBUTIONS, ElementsGeometry.BBOX);
  }

  /**
   * Gives the contributions as GeoJSON features, which have the centroid of the respective objects
   * in the geometry field.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @throws Exception thrown by {@link
   *         org.heigit.ohsome.ohsomeapi.executor.DataRequestExecutor#extract() extract}
   */
  @ApiOperation(value = "OSM contributions having the centroid of each OSM object as geometry",
      nickname = "contributionsCentroid", response = ExtractionResponse.class)
  @ApiImplicitParam(name = "time",
      value = "Two ISO-8601 conform timestrings defining an interval; no default value",
      defaultValue = "2016-01-01,2017-01-01", paramType = "query", dataType = "string",
      required = true)
  @RequestMapping(value = "/centroid", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public void contributionsCentroid(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
//    DataRequestExecutor executor = new DataRequestExecutor(RequestResource.CONTRIBUTIONS,
//        ElementsGeometry.CENTROID);
    operator.setOperation(contributionsExtraction);
    operator.compute();
    //dataRequestExecutor.extract(RequestResource.CONTRIBUTIONS, ElementsGeometry.CENTROID);
  }

  /**
   * Gives the latest contributions as GeoJSON features, which have the geometry of the respective
   * objects in the geometry field.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.DataRequestExecutor
   *         #extract() extract}
   */
  @ApiOperation(
      value = "Latest OSM contributions having the raw geometry of each OSM object as geometry",
      nickname = "contributionsLatestGeometry", response = ExtractionResponse.class)
  @ApiImplicitParam(name = "time",
      value = "Two ISO-8601 conform timestrings defining an interval; no default value",
      defaultValue = "2016-01-01,2017-01-01", paramType = "query", dataType = "string",
      required = true)
  @RequestMapping(value = "/latest/geometry", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public void contributionsLatest(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
//    DataRequestExecutor executor = new DataRequestExecutor(RequestResource.CONTRIBUTIONSLATEST,
//        ElementsGeometry.RAW);
    operator.setOperation(contributionsExtraction);
    operator.compute();
    //dataRequestExecutor.extract(RequestResource.CONTRIBUTIONSLATEST, ElementsGeometry.RAW);
  }

  /**
   * Gives the latest contributions as GeoJSON features, which have the bounding box of the
   * respective objects in the geometry field.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.DataRequestExecutor
   *         #extract() extract}
   */
  @ApiOperation(
      value = "Latest OSM contributions having the bounding box of each OSM object as geometry",
      nickname = "contributionsLatestBbox", response = ExtractionResponse.class)
  @ApiImplicitParam(name = "time",
      value = "Two ISO-8601 conform timestrings defining an interval; no default value",
      defaultValue = "2016-01-01,2017-01-01", paramType = "query", dataType = "string",
      required = true)
  @RequestMapping(value = "/latest/bbox", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public void contributionsBboxLatest() throws Exception {
//    DataRequestExecutor executor = new DataRequestExecutor(RequestResource.CONTRIBUTIONSLATEST,
//        ElementsGeometry.BBOX);
    operator.setOperation(contributionsExtraction);
    operator.setOperation(latest);
    operator.compute();
    //dataRequestExecutor.extract(RequestResource.CONTRIBUTIONSLATEST, ElementsGeometry.BBOX);
  }

  /**
   * Gives the latest contributions as GeoJSON features, which have the centroid of the respective
   * objects in the geometry field.
   *
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @throws Exception thrown by {@link org.heigit.ohsome.ohsomeapi.executor.DataRequestExecutor
   *         #extract() extract}
   */
  @ApiOperation(
      value = "Latest OSM contributions having the centroid of each OSM object as geometry",
      nickname = "contributionsLatestCentroid", response = ExtractionResponse.class)
  @ApiImplicitParam(name = "time",
      value = "Two ISO-8601 conform timestrings defining an interval; no default value",
      defaultValue = "2016-01-01,2017-01-01", paramType = "query", dataType = "string",
      required = true)
  @RequestMapping(value = "/latest/centroid", method = {RequestMethod.GET, RequestMethod.POST},
      produces = "application/json")
  public void contributionsCentroidLatest(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
//    DataRequestExecutor executor = new DataRequestExecutor(RequestResource.CONTRIBUTIONSLATEST,
//        ElementsGeometry.CENTROID);
    operator.setOperation(contributionsExtraction);
    operator.setOperation(latest);
    operator.compute();
    //dataRequestExecutor.extract(RequestResource.CONTRIBUTIONSLATEST, ElementsGeometry.CENTROID);
  }

}
