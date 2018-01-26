package org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements;

import org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.executor.ElementsRequestExecutor;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.ElementsResponseContent;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller containing the GET and POST request handling methods, which are mapped to
 * "/elements/density".
 *
 */
@RestController
@RequestMapping("/elements/density")
public class DensityController {
  
  /**
   * GET request giving the density of selected items (number of items per area).
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping("")
  public ElementsResponseContent getDensity(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time, @RequestParam(value = "showMetadata", defaultValue = "false") String showMetadata)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeDensity(false, bboxes, bpoints, bpolys, types, keys, values, userids, time, showMetadata);
  }
  
  /**
   * POST request giving the density of OSM objects. POST requests should only be used if the
   * request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping(value = "", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ElementsResponseContent postDensity(String[] bboxes, String[] bpoints, String[] bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time, String showMetadata)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeDensity(true, bboxes, bpoints, bpolys, types, keys, values, userids, time, showMetadata);
  }

}
