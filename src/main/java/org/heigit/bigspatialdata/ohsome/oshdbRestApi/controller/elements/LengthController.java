package org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements;

import org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.executor.ElementsRequestExecutor;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.ElementsResponseContent;
import org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.groupByResponse.GroupByResponseContent;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller containing the GET and POST request handling methods, which are mapped to
 * "/elements/length".
 */
@RestController
@RequestMapping("/elements/length")
public class LengthController {

  /**
   * GET request giving the length of OSM objects.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
  public ElementsResponseContent getLength(
      @RequestParam(value = "bboxes", defaultValue = "", required = false) String bboxes,
      @RequestParam(value = "bpoints", defaultValue = "", required = false) String bpoints,
      @RequestParam(value = "bpolys", defaultValue = "", required = false) String bpolys,
      @RequestParam(value = "types", defaultValue = "", required = false) String[] types,
      @RequestParam(value = "keys", defaultValue = "", required = false) String[] keys,
      @RequestParam(value = "values", defaultValue = "", required = false) String[] values,
      @RequestParam(value = "userids", defaultValue = "", required = false) String[] userids,
      @RequestParam(value = "time", defaultValue = "", required = false) String[] time,
      @RequestParam(value = "showMetadata", defaultValue = "false") String showMetadata)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeLengthArea(false, false, bboxes, bpoints, bpolys, types, keys, values,
        userids, time, showMetadata);
  }

  /**
   * GET request giving the length of OSM objects grouped by the tag.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param groupByKey <code>String</code> array containing the key used to create the tags for the
   *        grouping. At the current implementation, there must be one key given (not more and not
   *        less).
   * @param groupByValues <code>String</code> array containing the values used to create the tags
   *        for grouping. If a given value does not appear in the output, then there are no objects
   *        assigned to it (within the given filters).
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.groupByResponse.GroupByResponseContent
   *         GroupByResponseContent}
   */
  @RequestMapping(value = "/groupBy/tag", method = RequestMethod.GET, produces = "application/json")
  public GroupByResponseContent getLengthGroupByTag(
      @RequestParam(value = "bboxes", defaultValue = "", required = false) String bboxes,
      @RequestParam(value = "bpoints", defaultValue = "", required = false) String bpoints,
      @RequestParam(value = "bpolys", defaultValue = "", required = false) String bpolys,
      @RequestParam(value = "types", defaultValue = "", required = false) String[] types,
      @RequestParam(value = "keys", defaultValue = "", required = false) String[] keys,
      @RequestParam(value = "values", defaultValue = "", required = false) String[] values,
      @RequestParam(value = "userids", defaultValue = "", required = false) String[] userids,
      @RequestParam(value = "time", defaultValue = "", required = false) String[] time,
      @RequestParam(value = "showMetadata", defaultValue = "false") String showMetadata,
      @RequestParam(value = "groupByKey", defaultValue = "", required = false) String[] groupByKey,
      @RequestParam(value = "groupByValues", defaultValue = "",
          required = false) String[] groupByValues)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeLengthPerimeterAreaGroupByTag((byte) 1, false, bboxes, bpoints, bpolys,
        types, keys, values, userids, time, showMetadata, groupByKey, groupByValues);
  }

  /**
   * GET request giving the length of OSM objects grouped by the userId.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.groupByResponse.GroupByResponseContent
   *         GroupByResponseContent}
   */
  @RequestMapping(value = "/groupBy/user", method = RequestMethod.GET,
      produces = "application/json")
  public GroupByResponseContent getLengthGroupByUser(
      @RequestParam(value = "bboxes", defaultValue = "", required = false) String bboxes,
      @RequestParam(value = "bpoints", defaultValue = "", required = false) String bpoints,
      @RequestParam(value = "bpolys", defaultValue = "", required = false) String bpolys,
      @RequestParam(value = "types", defaultValue = "", required = false) String[] types,
      @RequestParam(value = "keys", defaultValue = "", required = false) String[] keys,
      @RequestParam(value = "values", defaultValue = "", required = false) String[] values,
      @RequestParam(value = "userids", defaultValue = "", required = false) String[] userids,
      @RequestParam(value = "time", defaultValue = "", required = false) String[] time,
      @RequestParam(value = "showMetadata", defaultValue = "false") String showMetadata)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeLengthPerimeterAreaGroupByUser((byte) 1, false, bboxes, bpoints, bpolys,
        types, keys, values, userids, time, showMetadata);
  }

  /**
   * GET request giving the length of items satisfying keys, values (+ other params) and part of
   * items satisfying keys2, values2.(+ other params).
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param keys2 <code>String</code> array having the same format as keys and used to define the
   *        subgroup(share).
   * @param values2 <code>String</code> array having the same format as values and used to define
   *        the subgroup(share).
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping(value = "/share", method = RequestMethod.GET, produces = "application/json")
  public ElementsResponseContent getLengthShare(
      @RequestParam(value = "bboxes", defaultValue = "", required = false) String bboxes,
      @RequestParam(value = "bpoints", defaultValue = "", required = false) String bpoints,
      @RequestParam(value = "bpolys", defaultValue = "", required = false) String bpolys,
      @RequestParam(value = "types", defaultValue = "", required = false) String[] types,
      @RequestParam(value = "keys", defaultValue = "", required = false) String[] keys,
      @RequestParam(value = "values", defaultValue = "", required = false) String[] values,
      @RequestParam(value = "userids", defaultValue = "", required = false) String[] userids,
      @RequestParam(value = "time", defaultValue = "", required = false) String[] time,
      @RequestParam(value = "showMetadata", defaultValue = "false") String showMetadata,
      @RequestParam(value = "keys2", defaultValue = "", required = false) String[] keys2,
      @RequestParam(value = "values2", defaultValue = "", required = false) String[] values2)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeLengthPerimeterAreaShare((byte) 1, false, bboxes, bpoints, bpolys, types,
        keys, values, userids, time, showMetadata, keys2, values2);
  }

  /**
   * POST request giving the length of OSM objects. POST requests should only be used if the request
   * URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ElementsResponseContent postLength(String bboxes, String bpoints, String bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata) throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeLengthArea(false, true, bboxes, bpoints, bpolys, types, keys, values,
        userids, time, showMetadata);
  }

  /**
   * POST request giving the length of OSM objects grouped by the tag. POST requests should only be
   * used if the request URL would be too long for a GET request.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param groupByKey <code>String</code> array containing the key used to create the tags for the
   *        grouping. At the current implementation, there must be one key given (not more and not
   *        less).
   * @param groupByValues <code>String</code> array containing the values used to create the tags
   *        for grouping. If a given value does not appear in the output, then there are no objects
   *        assigned to it (within the given filters).
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.groupByResponse.GroupByResponseContent
   *         GroupByResponseContent}
   */
  @RequestMapping(value = "/groupBy/tag", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByResponseContent postLengthGroupByTag(String bboxes, String bpoints, String bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata, String[] groupByKey, String[] groupByValues)
      throws UnsupportedOperationException, Exception, BadRequestException {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeLengthPerimeterAreaGroupByTag((byte) 1, true, bboxes, bpoints, bpolys,
        types, keys, values, userids, time, showMetadata, groupByKey, groupByValues);
  }

  /**
   * POST request giving the length of OSM objects grouped by the userID. POST requests should only
   * be used if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.groupByResponse.GroupByResponseContent
   *         GroupByResponseContent}
   */
  @RequestMapping(value = "/groupBy/user", method = RequestMethod.POST,
      produces = "application/json", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByResponseContent postLengthGroupByUser(String bboxes, String bpoints, String bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata) throws UnsupportedOperationException, Exception, BadRequestException {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeLengthPerimeterAreaGroupByUser((byte) 1, true, bboxes, bpoints, bpolys,
        types, keys, values, userids, time, showMetadata);
  }

  /**
   * POST request giving the length of items satisfying keys, values (+ other params) and part of
   * items satisfying keys2, values2.(+ other params). POST requests should only be used if the
   * request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param keys2 <code>String</code> array having the same format as keys and used to define the
   *        subgroup(share).
   * @param values2 <code>String</code> array having the same format as values and used to define
   *        the subgroup(share).
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping(value = "/share", method = RequestMethod.POST, produces = "application/json",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ElementsResponseContent postLengthShare(String bboxes, String bpoints, String bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata, String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception, BadRequestException {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeLengthPerimeterAreaShare((byte) 1, true, bboxes, bpoints, bpolys, types,
        keys, values, userids, time, showMetadata, keys2, values2);
  }


}
