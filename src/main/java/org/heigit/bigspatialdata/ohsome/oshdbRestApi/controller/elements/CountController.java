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
 * "/elements/count".
 *
 */
@RestController
@RequestMapping("/elements/count")
public class CountController {

  /**
   * GET request giving the count of OSM objects.
   * <p>
   * 
   * @param bboxes <code>String</code> array containing lon1, lat1, lon2, lat2 values, which have to
   *        be <code>double</code> parse-able. The coordinates refer to the bottom-left and
   *        top-right corner points of a bounding box. If bboxes is given, bpoints and bpolys must
   *        be <code>null</code> or <code>empty</code>. If neither of these parameters is given, a
   *        global request is computed.
   * @param bpoints <code>String</code> array containing lon, lat and radius values, which have to
   *        be <code>double</code> parse-able. If bpoints is given, bboxes and bpolys must be
   *        <code>null</code> or <code>empty</code>.
   * @param bpolys <code>String</code> array containing lon1, lat1, ..., lonN, latN values, which
   *        have to be <code>double</code> parse-able. The first and the last coordinate pair of
   *        each polygon have to be the same. If bpolys is given, bboxes and bpoints must be
   *        <code>null</code> or <code>empty</code>.
   * @param types <code>String</code> array containing one or more OSMTypes. It can contain "node"
   *        and/or "way" and/or "relation". If types is <code>null</code> or <code>empty</code>, all
   *        three are used.
   * @param keys <code>String</code> array containing one or more keys.
   * @param values <code>String</code> array containing one or more values. Must be less or equal
   *        than <code>keys.length()</code> and values[n] must pair with keys[n].
   * @param userids <code>String</code> array containing one or more user-IDs.
   * @param time <code>String</code> array that holds a list of timestamps or a datetimestring,
   *        which fits to one of the formats used by the method
   *        {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.inputValidation.InputValidator#extractIsoTime(String)
   *        extractIsoTime(String time)}.
   * @param showMetadata <code>String</code> containing the values "true" or "false".
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   * @throws UnsupportedOperationException thrown by
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer#aggregateByTimestamp()
   *         aggregateByTimestamp()}
   * @throws Exception thrown by
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#count() count()}
   */
  @RequestMapping("")
  public ElementsResponseContent getCount(
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
    return executor.executeCount(false, bboxes, bpoints, bpolys, types, keys, values, userids, time,
        showMetadata);
  }

  /**
   * GET request giving the count of OSM objects grouped by the OSM type.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.groupByResponse.GroupByResponseContent
   *         GroupByResponseContent}
   */
  @RequestMapping("/groupBy/type")
  public GroupByResponseContent getCountGroupByType(
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
    return executor.executeCountGroupByType(false, bboxes, bpoints, bpolys, types, keys, values,
        userids, time, showMetadata);
  }

  /**
   * GET request giving the count of OSM objects grouped by the userId.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.groupByResponse.GroupByResponseContent
   *         GroupByResponseContent}
   */
  @RequestMapping("/groupBy/user")
  public GroupByResponseContent getCountGroupByUser(
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
    return executor.executeCountGroupByUser(false, bboxes, bpoints, bpolys, types, keys, values,
        userids, time, showMetadata);
  }

  /**
   * GET request giving the count of OSM objects grouped by the boundary parameter (bounding
   * box/point/polygon).
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.groupByResponse.GroupByResponseContent
   *         GroupByResponseContent}
   */
  @RequestMapping("/groupBy/boundary")
  public GroupByResponseContent getCountGroupByBoundary(
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
    return executor.executeCountGroupByBoundary(false, bboxes, bpoints, bpolys, types, keys, values,
        userids, time, showMetadata);
  }

  /**
   * GET request giving the count of OSM objects grouped by the tag.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param groupByKey <code>String</code> array containing the key used to create the tags for the
   *        grouping. At the current implementation, there must be one key given (not more and not
   *        less).
   * @param groupByValues <code>String</code> array containing the values used to create the tags
   *        for grouping. If a given value does not appear in the output, then there are no objects
   *        assigned to it (within the given filters).
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.groupByResponse.GroupByResponseContent
   *         GroupByResponseContent}
   */
  @RequestMapping("/groupBy/tag")
  public GroupByResponseContent getCountGroupByTag(
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
    return executor.executeCountGroupByTag(false, bboxes, bpoints, bpolys, types, keys, values,
        userids, time, showMetadata, groupByKey, groupByValues);
  }

  /**
   * GET request giving the count of OSM objects grouped by the key.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param groupByKeys <code>String</code> array containing the key used to create the tags for the
   *        grouping. One or more keys can be provided.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.groupByResponse.GroupByResponseContent
   *         GroupByResponseContent}
   */
  @RequestMapping("/groupBy/key")
  public GroupByResponseContent getCountGroupByKey(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time,
      @RequestParam(value = "showMetadata", defaultValue = "false") String showMetadata,
      @RequestParam(value = "groupByKeys", defaultValue = "") String[] groupByKeys,
      @RequestParam(value = "groupByValues", defaultValue = "") String[] groupByValues)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCountGroupByKey(false, bboxes, bpoints, bpolys, types, keys, values,
        userids, time, showMetadata, groupByKeys);
  }

  /**
   * GET request giving the share of selected items satisfying keys2 and values2 within items
   * selected by types, keys and values.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param keys2 <code>String</code> array having the same format as keys and used to define the
   *        subgroup(share).
   * @param values2 <code>String</code> array having the same format as values and used to define
   *        the subgroup(share).
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping("/share")
  public ElementsResponseContent getCountShare(
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
    return executor.executeCountShare(false, bboxes, bpoints, bpolys, types, keys, values, userids,
        time, showMetadata, keys2, values2);
  }

  /**
   * GET request giving the ratio of selected items satisfying types2, keys2 and values2 within
   * items selected by types, keys and values.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param types2 <code>String</code> array having the same format as types.
   * @param keys2 <code>String</code> array having the same format as keys.
   * @param values2 <code>String</code> array having the same format as values.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping("/ratio")
  public ElementsResponseContent getCountRatio(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time,
      @RequestParam(value = "showMetadata", defaultValue = "false") String showMetadata,
      @RequestParam(value = "types2", defaultValue = "") String[] types2,
      @RequestParam(value = "keys2", defaultValue = "") String[] keys2,
      @RequestParam(value = "values2", defaultValue = "") String[] values2)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeRatio(false, bboxes, bpoints, bpolys, types, keys, values, userids, time,
        showMetadata, types2, keys2, values2);
  }

  /**
   * POST request giving the count of OSM objects. POST requests should only be used if the request
   * URL would be too long for a GET request.
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
  public ElementsResponseContent postCount(String[] bboxes, String[] bpoints, String[] bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata) throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCount(true, bboxes, bpoints, bpolys, types, keys, values, userids, time,
        showMetadata);
  }

  /**
   * POST request giving the count of OSM objects grouped by the OSM type. POST requests should only
   * be used if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.groupByResponse.GroupByResponseContent
   *         GroupByResponseContent}
   */
  @RequestMapping(value = "/groupBy/type", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByResponseContent postCountGroupByType(String[] bboxes, String[] bpoints,
      String[] bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata) throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCountGroupByType(true, bboxes, bpoints, bpolys, types, keys, values,
        userids, time, showMetadata);
  }

  /**
   * POST request giving the count of OSM objects grouped by the userID. POST requests should only
   * be used if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.groupByResponse.GroupByResponseContent
   *         GroupByResponseContent}
   */
  @RequestMapping(value = "/groupBy/user", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByResponseContent postCountGroupByUser(String[] bboxes, String[] bpoints,
      String[] bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata)
      throws UnsupportedOperationException, Exception, BadRequestException {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCountGroupByUser(true, bboxes, bpoints, bpolys, types, keys, values,
        userids, time, showMetadata);
  }

  /**
   * POST request giving the count of OSM objects grouped by the boundary parameter (bounding
   * box/point/polygon). POST requests should only be used if the request URL would be too long for
   * a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.groupByResponse.GroupByResponseContent
   *         GroupByResponseContent}
   */
  @RequestMapping(value = "/groupBy/boundary", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByResponseContent postCountGroupByBoundary(String[] bboxes, String[] bpoints,
      String[] bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata)
      throws UnsupportedOperationException, Exception, BadRequestException {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCountGroupByBoundary(true, bboxes, bpoints, bpolys, types, keys, values,
        userids, time, showMetadata);
  }

  /**
   * POST request giving the count of OSM objects grouped by the tag. POST requests should only be
   * used if the request URL would be too long for a GET request.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param groupByKey <code>String</code> array containing the key used to create the tags for the
   *        grouping. At the current implementation, there must be one key given (not more and not
   *        less).
   * @param groupByValues <code>String</code> array containing the values used to create the tags
   *        for grouping. If a given value does not appear in the output, then there are no objects
   *        assigned to it (within the given filters).
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.groupByResponse.GroupByResponseContent
   *         GroupByResponseContent}
   */
  @RequestMapping(value = "/groupBy/tag", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByResponseContent postCountGroupByTag(String[] bboxes, String[] bpoints,
      String[] bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata, String[] groupByKey, String[] groupByValues)
      throws UnsupportedOperationException, Exception, BadRequestException {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCountGroupByTag(true, bboxes, bpoints, bpolys, types, keys, values,
        userids, time, showMetadata, groupByKey, groupByValues);
  }

  /**
   * POST request giving the count of OSM objects grouped by the key. POST requests should only be
   * used if the request URL would be too long for a GET request.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param groupByKeys <code>String</code> array containing the key used to create the tags for the
   *        grouping. One or more keys can be provided.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.groupByResponse.GroupByResponseContent
   *         GroupByResponseContent}
   */
  @RequestMapping(value = "/groupBy/key", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public GroupByResponseContent postCountGroupByKey(String[] bboxes, String[] bpoints,
      String[] bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata, String[] groupByKeys)
      throws UnsupportedOperationException, Exception, BadRequestException {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCountGroupByKey(true, bboxes, bpoints, bpolys, types, keys, values,
        userids, time, showMetadata, groupByKeys);
  }

  /**
   * POST request giving the share of selected items satisfying keys2 and values2 within items
   * selected by types, keys and values. POST requests should only be used if the request URL would
   * be too long for a GET request.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param keys2 <code>String</code> array having the same format as keys and used to define the
   *        subgroup(share).
   * @param values2 <code>String</code> array having the same format as values and used to define
   *        the subgroup(share).
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping(value = "/share", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ElementsResponseContent postCountShare(String[] bboxes, String[] bpoints, String[] bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata, String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception, BadRequestException {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeCountShare(true, bboxes, bpoints, bpolys, types, keys, values, userids,
        time, showMetadata, keys2, values2);
  }

  /**
   * POST request giving the ratio of selected items satisfying types2, keys2 and values2 within
   * items selected by types, keys and values. POST requests should only be used if the request URL
   * would be too long for a GET request.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.controller.elements.CountController#getCount(String[], String[], String[], String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @param types2 <code>String</code> array having the same format as types.
   * @param keys2 <code>String</code> array having the same format as keys.
   * @param values2 <code>String</code> array having the same format as values.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping(value = "/ratio", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ElementsResponseContent postCountRatio(String[] bboxes, String[] bpoints, String[] bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String showMetadata, String[] types2, String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception {

    ElementsRequestExecutor executor = new ElementsRequestExecutor();
    return executor.executeRatio(true, bboxes, bpoints, bpolys, types, keys, values, userids, time,
        showMetadata, types2, keys2, values2);
  }

}
