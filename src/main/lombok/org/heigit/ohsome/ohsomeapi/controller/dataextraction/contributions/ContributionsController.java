package org.heigit.ohsome.ohsomeapi.controller.dataextraction.contributions;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.ohsome.ohsomeapi.controller.dataextraction.features.ElementsGeometry;
import org.heigit.ohsome.ohsomeapi.executor.DataRequestExecutor;
import org.heigit.ohsome.ohsomeapi.executor.RequestResource;
import org.heigit.ohsome.ohsomeapi.output.ExtractionResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller containing the methods, which are mapped to "/contributions" and used to return
 * each contribution (creation, modification, deletion) of the OSM data.
 */
@Api(tags = "Contributions")
@RestController
@RequestMapping("/contributions")
public class ContributionsController {

  /**
   * Gives the contributions as GeoJSON features, which have the geometry of the respective objects
   * in the geometry field.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.executor.DataRequestExecutor#extract() extract}
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
    DataRequestExecutor executor = new DataRequestExecutor(RequestResource.CONTRIBUTIONS,
        ElementsGeometry.RAW, servletRequest, servletResponse);
    executor.extract();
  }

  /**
   * Gives the contributions as GeoJSON features, which have the bounding box of the respective
   * objects in the geometry field.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.executor.DataRequestExecutor#extract() extract}
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
    DataRequestExecutor executor = new DataRequestExecutor(RequestResource.CONTRIBUTIONS,
        ElementsGeometry.BBOX, servletRequest, servletResponse);
    executor.extract();
  }

  /**
   * Gives the contributions as GeoJSON features, which have the centroid of the respective objects
   * in the geometry field.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.executor.DataRequestExecutor#extract() extract}
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
    DataRequestExecutor executor = new DataRequestExecutor(RequestResource.CONTRIBUTIONS,
        ElementsGeometry.CENTROID, servletRequest, servletResponse);
    executor.extract();
  }

  /**
   * Gives the latest contributions as GeoJSON features, which have the geometry of the respective
   * objects in the geometry field.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.executor.DataRequestExecutor#extract() extract}
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
    DataRequestExecutor executor = new DataRequestExecutor(RequestResource.CONTRIBUTIONSLATEST,
        ElementsGeometry.RAW, servletRequest, servletResponse);
    executor.extract();
  }

  /**
   * Gives the latest contributions as GeoJSON features, which have the bounding box of the
   * respective objects in the geometry field.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.executor.DataRequestExecutor#extract() extract}
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
  public void contributionsBboxLatest(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) throws Exception {
    DataRequestExecutor executor = new DataRequestExecutor(RequestResource.CONTRIBUTIONSLATEST,
        ElementsGeometry.BBOX, servletRequest, servletResponse);
    executor.extract();
  }

  /**
   * Gives the latest contributions as GeoJSON features, which have the centroid of the respective
   * objects in the geometry field.
   * 
   * @param servletRequest <code>HttpServletRequest</code> of the incoming request
   * @param servletResponse <code>HttpServletResponse</code> of the outgoing response
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.executor.DataRequestExecutor#extract() extract}
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
    DataRequestExecutor executor = new DataRequestExecutor(RequestResource.CONTRIBUTIONSLATEST,
        ElementsGeometry.CENTROID, servletRequest, servletResponse);
    executor.extract();
  }

}
