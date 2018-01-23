package org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.elements;

import org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.executor.ElementsRequestExecutor;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.groupByResponse.GroupByResponseContent;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller containing the GET and POST request handling methods, which are mapped to
 * "/elements/area".
 *
 */
@RestController
@RequestMapping("/elements/area")
public class AreaController {

  /**
   * GET request giving the area of OSM objects.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping("")
  public ElementsResponseContent getArea(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time,
      @RequestParam(value = "showMetadata", defaultValue = "false") String showMetadata)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeLengthArea(true, false, bboxes, bpoints, bpolys, types, keys, values,
        userids, time, showMetadata);
  }

  /**
   * GET request giving the area of OSM objects grouped by the tag.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @param groupByKey <code>String</code> array containing the key used to create the tags for the
   *        grouping. At the current implementation, there must be one key given (not more and not
   *        less).
   * @param groupByValues <code>String</code> array containing the values used to create the tags
   *        for grouping. If a given value does not appear in the output, then there are no objects
   *        assigned to it (within the given filters).
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping("/groupBy/tag")
  public GroupByResponseContent getAreaGroupByTag(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time,
      @RequestParam(value = "showMetadata", defaultValue = "false") String showMetadata,
      @RequestParam(value = "groupByKey", defaultValue = "") String[] groupByKey,
      @RequestParam(value = "groupByValues", defaultValue = "") String[] groupByValues)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeLengthPerimeterAreaGroupByTag((byte) 3, false, bboxes, bpoints, bpolys,
        types, keys, values, userids, time, showMetadata, groupByKey, groupByValues);
  }

  /**
   * GET request giving the area of OSM objects grouped by the userId.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping("/groupBy/user")
  public GroupByResponseContent getAreaGroupByUser(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time,
      @RequestParam(value = "showMetadata", defaultValue = "false") String showMetadata)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeLengthPerimeterAreaGroupByUser((byte) 3, false, bboxes, bpoints, bpolys,
        types, keys, values, userids, time, showMetadata);
  }

  /**
   * GET request giving the area of OSM objects grouped by the OSM type.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.groupByResponse.GroupByResponseContent
   *         GroupByResponseContent}
   */
  @RequestMapping("/groupBy/type")
  public GroupByResponseContent getAreaGroupByType(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time,
      @RequestParam(value = "showMetadata", defaultValue = "false") String showMetadata)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeAreaPerimeterGroupByType(true, false, bboxes, bpoints, bpolys, types,
        keys, values, userids, time, showMetadata);
  }

  /**
   * GET request giving the area of items satisfying keys, values (+ other params) and part of items
   * satisfying keys2, values2.(+ other params).
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @param keys2 <code>String</code> array having the same format as keys and used to define the
   *        subgroup(share).
   * @param values2 <code>String</code> array having the same format as values and used to define
   *        the subgroup(share).
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping("/share")
  public ElementsResponseContent getAreaShare(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time,
      @RequestParam(value = "showMetadata", defaultValue = "false") String showMetadata,
      @RequestParam(value = "keys2", defaultValue = "") String[] keys2,
      @RequestParam(value = "values2", defaultValue = "") String[] values2)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeLengthPerimeterAreaShare((byte) 3, false, bboxes, bpoints, bpolys, types,
        keys, values, userids, time, showMetadata, keys2, values2);
  }

  /**
   * POST request giving the area of OSM objects. POST requests should only be used if the request
   * URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping(value = "", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ElementsResponseContent postArea(String[] bboxes, String[] bpoints, String[] bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata) throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeLengthArea(true, true, bboxes, bpoints, bpolys, types, keys, values,
        userids, time, showMetadata);
  }

  /**
   * POST request giving the area of OSM objects grouped by the OSM type. POST requests should only
   * be used if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.groupByResponse.GroupByResponseContent
   *         GroupByResponseContent}
   */
  @RequestMapping(value = "/groupBy/type", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByResponseContent postAreaGroupByType(String[] bboxes, String[] bpoints,
      String[] bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata)
      throws UnsupportedOperationException, Exception, BadRequestException {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeAreaPerimeterGroupByType(true, true, bboxes, bpoints, bpolys, types,
        keys, values, userids, time, showMetadata);
  }

  /**
   * POST request giving the area of OSM objects grouped by the tag. POST requests should only be
   * used if the request URL would be too long for a GET request.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @param groupByKey <code>String</code> array containing the key used to create the tags for the
   *        grouping. At the current implementation, there must be one key given (not more and not
   *        less).
   * @param groupByValues <code>String</code> array containing the values used to create the tags
   *        for grouping. If a given value does not appear in the output, then there are no objects
   *        assigned to it (within the given filters).
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.groupByResponse.GroupByResponseContent
   *         GroupByResponseContent}
   */
  @RequestMapping(value = "/groupBy/tag", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByResponseContent postAreaGroupByTag(String[] bboxes, String[] bpoints,
      String[] bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata, String[] groupByKey, String[] groupByValues)
      throws UnsupportedOperationException, Exception, BadRequestException {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeLengthPerimeterAreaGroupByTag((byte) 3, true, bboxes, bpoints, bpolys,
        types, keys, values, userids, time, showMetadata, groupByKey, groupByValues);
  }

  /**
   * POST request giving the area of OSM objects grouped by the userID. POST requests should only be
   * used if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.groupByResponse.GroupByResponseContent
   *         GroupByResponseContent}
   */
  @RequestMapping(value = "/groupBy/user", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByResponseContent postAreaGroupByUser(String[] bboxes, String[] bpoints,
      String[] bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata)
      throws UnsupportedOperationException, Exception, BadRequestException {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeLengthPerimeterAreaGroupByUser((byte) 3, true, bboxes, bpoints, bpolys,
        types, keys, values, userids, time, showMetadata);
  }

  /**
   * POST request giving the area of items satisfying keys, values (+ other params) and part of
   * items satisfying keys2, values2.(+ other params). POST requests should only be used if the
   * request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @param keys2 <code>String</code> array having the same format as keys and used to define the
   *        subgroup(share).
   * @param values2 <code>String</code> array having the same format as values and used to define
   *        the subgroup(share).
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping(value = "/share", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ElementsResponseContent postAreaShare(String[] bboxes, String[] bpoints, String[] bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata, String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception, BadRequestException {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeLengthPerimeterAreaShare((byte) 3, true, bboxes, bpoints, bpolys, types,
        keys, values, userids, time, showMetadata, keys2, values2);
  }


}
