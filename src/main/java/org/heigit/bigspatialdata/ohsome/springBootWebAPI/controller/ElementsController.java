package org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.Application;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.eventHolder.EventHolderBean;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.exception.NotImplementedException;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.inputValidation.InputValidator;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.interceptor.ElementsRequestInterceptor;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.MetaData;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.GroupByResult;
import org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.Result;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDB_H2;
import org.heigit.bigspatialdata.oshdb.api.generic.OSHDBTimestampAndOtherIndex;
import org.heigit.bigspatialdata.oshdb.api.generic.function.SerializableFunction;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapBiAggregatorByTimestamps;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.object.OSMEntitySnapshot;
import org.heigit.bigspatialdata.oshdb.api.utils.OSHDBTimestamp;
import org.heigit.bigspatialdata.oshdb.osm.OSMType;
import org.heigit.bigspatialdata.oshdb.util.Geo;
import org.heigit.bigspatialdata.oshdb.util.TagTranslator;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygonal;
import net.bytebuddy.jar.asm.commons.TryCatchBlockSorter;

/**
 * REST controller containing the GET and POST request handling methods, which are mapped to
 * "/elements".
 *
 */
@RestController
@RequestMapping("/elements")
public class ElementsController {

  /*
   * GET requests start here
   */

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
   *        {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.inputValidation.InputValidator#extractIsoTime(String)
   *        extractIsoTime(String time)}.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   * @throws UnsupportedOperationException thrown by
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer#aggregateByTimestamp()
   *         aggregateByTimestamp()}
   * @throws Exception thrown by
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#count() count()}
   */
  @RequestMapping("/count")
  public ElementsResponseContent getCount(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time)
      throws UnsupportedOperationException, Exception {

    return executeCount(false, bboxes, bpoints, bpolys, types, keys, values, userids, time);
  }

  /**
   * GET request giving the length of OSM objects.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping("/length")
  public ElementsResponseContent getLength(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time)
      throws UnsupportedOperationException, Exception {

    return executeLengthArea(true, false, bboxes, bpoints, bpolys, types, keys, values, userids,
        time);
  }

  /**
   * GET request giving the perimeter of polygonal OSM objects.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping("/perimeter")
  public ElementsResponseContent getPerimeter(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time)
      throws UnsupportedOperationException, Exception {

    return executePerimeter(false, bboxes, bpoints, bpolys, types, keys, values, userids, time);
  }

  /**
   * GET request giving the area of OSM objects.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping("/area")
  public ElementsResponseContent getArea(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time)
      throws UnsupportedOperationException, Exception {

    return executeLengthArea(false, false, bboxes, bpoints, bpolys, types, keys, values, userids,
        time);
  }

  /**
   * GET request giving the mean minimum distance of items to a given bounding point. This method is
   * not implemented yet.
   * <p>
   * For description of the parameters, <code>return</code> object and exceptions, look at the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   */
  @RequestMapping("/mean-minimal-distance")
  public ElementsResponseContent getMeanMinimalDistance(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time)
      throws UnsupportedOperationException, Exception {

    throw new NotImplementedException("This method is not implemented yet.");
  }

  /**
   * GET request giving the density of selected items (number of items per area).
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping("/density")
  public ElementsResponseContent getDensity(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time)
      throws UnsupportedOperationException, Exception {

    return executeDensity(false, bboxes, bpoints, bpolys, types, keys, values, userids, time);
  }

  /**
   * GET request giving the ratio of selected items satisfying types2, keys2 and values2 within
   * items selected by types, keys and values.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @param types2 <code>String</code> array having the same format as types.
   * @param keys2 <code>String</code> array having the same format as keys.
   * @param values2 <code>String</code> array having the same format as values.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping("/ratio")
  public ElementsResponseContent getRatio(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time,
      @RequestParam(value = "types2", defaultValue = "") String[] types2,
      @RequestParam(value = "keys2", defaultValue = "") String[] keys2,
      @RequestParam(value = "values2", defaultValue = "") String[] values2)
      throws UnsupportedOperationException, Exception {

    return executeRatio(false, bboxes, bpoints, bpolys, types, keys, values, userids, time, types2,
        keys2, values2);
  }

  /*
   * GET groupBy Requests start here
   */

  /**
   * GET request giving the count of OSM objects grouped by the OSM type.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping("/count/groupBy/type")
  public ElementsResponseContent getCountGroupByType(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time)
      throws UnsupportedOperationException, Exception {

    return executeCountGroupByType(false, bboxes, bpoints, bpolys, types, keys, values, userids,
        time);
  }

  /**
   * GET request giving the count of OSM objects grouped by the userId.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping("/count/groupBy/user")
  public ElementsResponseContent getCountGroupByUser(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time)
      throws UnsupportedOperationException, Exception {

    return executeCountGroupByUser(false, bboxes, bpoints, bpolys, types, keys, values, userids,
        time);
  }

  /**
   * GET request giving the count of OSM objects grouped by the boundary parameter (bounding
   * box/point/polygon).
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping("/count/groupBy/boundary")
  public ElementsResponseContent getCountGroupByBoundary(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time)
      throws UnsupportedOperationException, Exception {

    return executeCountGroupByBoundary(false, bboxes, bpoints, bpolys, types, keys, values, userids,
        time);
  }

  /**
   * GET request giving the count of OSM objects grouped by the tag.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
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
  @RequestMapping("/count/groupBy/tag")
  public ElementsResponseContent getCountGroupByTag(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time,
      @RequestParam(value = "groupByKey", defaultValue = "") String[] groupByKey,
      @RequestParam(value = "groupByValues", defaultValue = "") String[] groupByValues)
      throws UnsupportedOperationException, Exception {

    return executeCountGroupByTag(false, bboxes, bpoints, bpolys, types, keys, values, userids,
        time, groupByKey, groupByValues);
  }

  /**
   * GET request giving the length of OSM objects grouped by the userId.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping("/length/groupBy/user")
  public ElementsResponseContent getLengthGroupByUser(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time)
      throws UnsupportedOperationException, Exception {

    return executeLengthPerimeterAreaGroupByUser((byte) 1, false, bboxes, bpoints, bpolys, types,
        keys, values, userids, time);
  }

  /**
   * GET request giving the perimeter of polygonal OSM objects grouped by the OSM type.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping("/perimeter/groupBy/type")
  public ElementsResponseContent getPerimeterGroupByType(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time)
      throws UnsupportedOperationException, Exception {

    return executeAreaPerimeterGroupByType(false, false, bboxes, bpoints, bpolys, types, keys,
        values, userids, time);
  }

  /**
   * GET request giving the area of OSM objects grouped by the userId.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping("/area/groupBy/user")
  public ElementsResponseContent getAreaGroupByUser(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time)
      throws UnsupportedOperationException, Exception {

    return executeLengthPerimeterAreaGroupByUser((byte) 3, false, bboxes, bpoints, bpolys, types,
        keys, values, userids, time);
  }

  /**
   * GET request giving the area of OSM objects grouped by the OSM type.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping("/area/groupBy/type")
  public ElementsResponseContent getAreaGroupByType(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time)
      throws UnsupportedOperationException, Exception {

    return executeAreaPerimeterGroupByType(true, false, bboxes, bpoints, bpolys, types, keys,
        values, userids, time);
  }

  /**
   * GET request giving the perimeter of polygonal OSM objects grouped by the userId.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping("/perimeter/groupBy/user")
  public ElementsResponseContent getPerimeterGroupByUser(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time)
      throws UnsupportedOperationException, Exception {

    return executeLengthPerimeterAreaGroupByUser((byte) 2, false, bboxes, bpoints, bpolys, types,
        keys, values, userids, time);
  }

  /*
   * POST Requests start here
   */

  /**
   * POST request giving the count of OSM objects. POST requests should only be used if the request
   * URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping(value = "/count", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ElementsResponseContent postCount(String[] bboxes, String[] bpoints, String[] bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time)
      throws UnsupportedOperationException, Exception {

    return executeCount(true, bboxes, bpoints, bpolys, types, keys, values, userids, time);
  }

  /**
   * POST request giving the length of OSM objects. POST requests should only be used if the request
   * URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping(value = "/length", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ElementsResponseContent postLength(String[] bboxes, String[] bpoints, String[] bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time)
      throws UnsupportedOperationException, Exception {

    return executeLengthArea(true, true, bboxes, bpoints, bpolys, types, keys, values, userids,
        time);
  }

  /**
   * POST request giving the perimeter of polygonal OSM objects. POST requests should only be used
   * if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping(value = "/perimeter", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ElementsResponseContent postPerimeter(String[] bboxes, String[] bpoints, String[] bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time)
      throws UnsupportedOperationException, Exception {

    return executePerimeter(true, bboxes, bpoints, bpolys, types, keys, values, userids, time);
  }

  /**
   * POST request giving the area of OSM objects. POST requests should only be used if the request
   * URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping(value = "/area", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ElementsResponseContent postArea(String[] bboxes, String[] bpoints, String[] bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time)
      throws UnsupportedOperationException, Exception {

    return executeLengthArea(false, true, bboxes, bpoints, bpolys, types, keys, values, userids,
        time);
  }

  /**
   * POST request giving the density of OSM objects. POST requests should only be used if the
   * request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping(value = "/density", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ElementsResponseContent postDensity(String[] bboxes, String[] bpoints, String[] bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time)
      throws UnsupportedOperationException, Exception {

    return executeDensity(true, bboxes, bpoints, bpolys, types, keys, values, userids, time);
  }

  /**
   * POST request giving the ratio of selected items satisfying types2, keys2 and values2 within
   * items selected by types, keys and values. POST requests should only be used if the request URL
   * would be too long for a GET request.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @param types2 <code>String</code> array having the same format as types.
   * @param keys2 <code>String</code> array having the same format as keys.
   * @param values2 <code>String</code> array having the same format as values.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping(value = "/ratio", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ElementsResponseContent postRatio(String[] bboxes, String[] bpoints, String[] bpolys,
      String[] types, String[] keys, String[] values, String[] userids, String[] time,
      String[] types2, String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception {

    return executeRatio(true, bboxes, bpoints, bpolys, types, keys, values, userids, time, types2,
        keys2, values2);
  }

  /*
   * POST groupBy requests start here
   */

  /**
   * POST request giving the count of OSM objects grouped by the OSM type. POST requests should only
   * be used if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping(value = "/count/groupBy/type", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ElementsResponseContent postCountGroupByType(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time)
      throws UnsupportedOperationException, Exception {

    return executeCountGroupByType(true, bboxes, bpoints, bpolys, types, keys, values, userids,
        time);
  }

  /**
   * POST request giving the count of OSM objects grouped by the userID. POST requests should only
   * be used if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping(value = "/count/groupBy/user", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ElementsResponseContent postCountGroupByUser(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time)
      throws UnsupportedOperationException, Exception, BadRequestException {

    return executeCountGroupByUser(true, bboxes, bpoints, bpolys, types, keys, values, userids,
        time);
  }

  /**
   * POST request giving the count of OSM objects grouped by the boundary parameter (bounding
   * box/point/polygon). POST requests should only be used if the request URL would be too long for
   * a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping(value = "/count/groupBy/boundary", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ElementsResponseContent postCountGroupByBoundary(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time)
      throws UnsupportedOperationException, Exception, BadRequestException {

    return executeCountGroupByBoundary(true, bboxes, bpoints, bpolys, types, keys, values, userids,
        time);
  }

  /**
   * POST request giving the count of OSM objects grouped by the tag. POST requests should only be
   * used if the request URL would be too long for a GET request.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
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
  @RequestMapping(value = "/count/groupBy/tag", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ElementsResponseContent postCountGroupByTag(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time,
      @RequestParam(value = "groupByKey", defaultValue = "") String[] groupByKey,
      @RequestParam(value = "groupByValues", defaultValue = "") String[] groupByValues)
      throws UnsupportedOperationException, Exception, BadRequestException {

    return executeCountGroupByTag(true, bboxes, bpoints, bpolys, types, keys, values, userids, time,
        groupByKey, groupByValues);
  }

  /**
   * POST request giving the length of OSM objects grouped by the userID. POST requests should only
   * be used if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping(value = "/length/groupBy/user", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ElementsResponseContent postLengthGroupByUser(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time)
      throws UnsupportedOperationException, Exception, BadRequestException {

    return executeLengthPerimeterAreaGroupByUser((byte) 1, true, bboxes, bpoints, bpolys, types,
        keys, values, userids, time);
  }

  /**
   * POST request giving the area of OSM objects grouped by the OSM type. POST requests should only
   * be used if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping(value = "/area/groupBy/type", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ElementsResponseContent postAreaGroupByType(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time)
      throws UnsupportedOperationException, Exception, BadRequestException {

    return executeAreaPerimeterGroupByType(true, true, bboxes, bpoints, bpolys, types, keys, values,
        userids, time);
  }

  /**
   * POST request giving the area of OSM objects grouped by the userID. POST requests should only be
   * used if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping(value = "/area/groupBy/user", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ElementsResponseContent postAreaGroupByUser(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time)
      throws UnsupportedOperationException, Exception, BadRequestException {

    return executeLengthPerimeterAreaGroupByUser((byte) 3, true, bboxes, bpoints, bpolys, types,
        keys, values, userids, time);
  }

  /**
   * POST request giving the perimeter of polygonal OSM objects grouped by the OSM type. POST
   * requests should only be used if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping(value = "/perimeter/groupBy/type", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ElementsResponseContent postPerimeterGroupByType(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time)
      throws UnsupportedOperationException, Exception, BadRequestException {

    return executeAreaPerimeterGroupByType(false, true, bboxes, bpoints, bpolys, types, keys,
        values, userids, time);
  }

  /**
   * POST request giving the perimeter of polygonal OSM objects grouped by the userID. POST requests
   * should only be used if the request URL would be too long for a GET request.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.controller.ElementsController#getCount(String[], String[], String[], String[], String[], String[], String[], String[])
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.springBootWebAPI.output.dataAggregationResponse.ElementsResponseContent
   *         ElementsResponseContent}
   */
  @RequestMapping(value = "/perimeter/groupBy/user", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ElementsResponseContent postPerimeterGroupByUser(
      @RequestParam(value = "bboxes", defaultValue = "") String[] bboxes,
      @RequestParam(value = "bpoints", defaultValue = "") String[] bpoints,
      @RequestParam(value = "bpolys", defaultValue = "") String[] bpolys,
      @RequestParam(value = "types", defaultValue = "") String[] types,
      @RequestParam(value = "keys", defaultValue = "") String[] keys,
      @RequestParam(value = "values", defaultValue = "") String[] values,
      @RequestParam(value = "userids", defaultValue = "") String[] userids,
      @RequestParam(value = "time", defaultValue = "") String[] time)
      throws UnsupportedOperationException, Exception, BadRequestException {

    return executeLengthPerimeterAreaGroupByUser((byte) 2, true, bboxes, bpoints, bpolys, types,
        keys, values, userids, time);
  }

  /*
   * EXECUTING methods of all requests start here
   */

  /**
   * Gets the input parameters of the request and performs a count calculation.
   */
  private ElementsResponseContent executeCount(boolean isPost, String[] bboxes, String[] bpoints,
      String[] bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Integer> result;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputValidator iV = new InputValidator();
    String requestURL = null;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    // input parameter processing
    mapRed =
        iV.processParameters(isPost, bboxes, bpoints, bpolys, types, keys, values, userids, time);
    // db result
    result = mapRed.aggregateByTimestamp().count();
    // output
    Result[] resultSet = new Result[result.size()];
    int count = 0;
    for (Entry<OSHDBTimestamp, Integer> entry : result.entrySet()) {
      resultSet[count] = new Result(entry.getKey().formatIsoDateTime(),
          String.valueOf(entry.getValue().intValue()));
      count++;
    }
    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response = new ElementsResponseContent(
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
        new MetaData(duration, "amount",
            "Total number of elements, which are selected by the parameters.", requestURL),
        null, resultSet);

    return response;
  }

  /**
   * Gets the input parameters of the request and performs a length or area calculation.
   * 
   * @param isArea <code>Boolean</code> defining an area (true) or a length (false) request.
   */
  private ElementsResponseContent executeLengthArea(boolean isArea, boolean isPost, String[] bboxes,
      String[] bpoints, String[] bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Number> result;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputValidator iV = new InputValidator();
    String unit;
    String description;
    String requestURL = null;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;

    // input parameter processing
    mapRed =
        iV.processParameters(isPost, bboxes, bpoints, bpolys, types, keys, values, userids, time);
    // db result
    result = mapRed.aggregateByTimestamp()
        .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          if (isArea) {
            return Geo.areaOf(snapshot.getGeometry());
          } else {
            return Geo.lengthOf(snapshot.getGeometry());
          }
        });
    // output
    Result[] resultSet = new Result[result.size()];
    int count = 0;
    for (Map.Entry<OSHDBTimestamp, Number> entry : result.entrySet()) {
      resultSet[count] = new Result(entry.getKey().formatIsoDateTime(),
          String.valueOf(entry.getValue().floatValue()));
      count++;
    }
    if (isArea) {
      unit = "square-meter";
      description = "Total area of polygons.";
    } else {
      unit = "meter";
      description = "Total length of lines.";
    }
    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response = new ElementsResponseContent(
        "-Hier könnte Ihre Lizenz stehen.-", "-Hier könnte Ihr Copyright stehen.-",
        new MetaData(duration, unit, description, requestURL), null, resultSet);
    return response;
  }

  /**
   * Gets the input parameters of the request and performs a perimeter calculation.
   */
  private ElementsResponseContent executePerimeter(boolean isPost, String[] bboxes,
      String[] bpoints, String[] bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Number> result;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputValidator iV = new InputValidator();
    String requestURL = null;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;

    // input parameter processing
    mapRed =
        iV.processParameters(isPost, bboxes, bpoints, bpolys, types, keys, values, userids, time);
    // db result
    result = mapRed.aggregateByTimestamp()
        .sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          // checks if the geometry is polygonal (needed for OSM relations, which are not polygonal)
          if (snapshot.getGeometry() instanceof Polygonal)
            return Geo.lengthOf(snapshot.getGeometry().getBoundary());
          else
            return 0.0;
        });
    // output
    Result[] resultSet = new Result[result.size()];
    int count = 0;
    for (Map.Entry<OSHDBTimestamp, Number> entry : result.entrySet()) {
      resultSet[count] = new Result(entry.getKey().formatIsoDateTime(),
          String.valueOf(entry.getValue().floatValue()));
      count++;
    }
    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response =
        new ElementsResponseContent("-Hier könnte Ihre Lizenz stehen.-",
            "-Hier könnte Ihr Copyright stehen.-", new MetaData(duration, "meters",
                "Total length of the perimeter (polygon boundaries)", requestURL),
            null, resultSet);
    return response;
  }

  /**
   * Gets the input parameters of the request and performs a density calculation.
   */
  private ElementsResponseContent executeDensity(boolean isPost, String[] bboxes, String[] bpoints,
      String[] bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Integer> countResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputValidator iV = new InputValidator();
    String requestURL = null;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    // input parameter processing
    mapRed =
        iV.processParameters(isPost, bboxes, bpoints, bpolys, types, keys, values, userids, time);
    // count result
    countResult = mapRed.aggregateByTimestamp().count();
    int count = 0;
    Result[] countResultSet = new Result[countResult.size()];
    for (Entry<OSHDBTimestamp, Integer> entry : countResult.entrySet()) {
      countResultSet[count] = new Result(entry.getKey().formatIsoDateTime(),
          String.valueOf(entry.getValue().intValue()));
      count++;
    }
    // geometry
    Geometry geom = null;
    switch (iV.getBoundary()) {
      case 0:
        geom = iV.getBbox().getGeometry();
        break;
      case 1:
        geom = iV.getBbox().getGeometry();
        break;
      case 2:
        geom = iV.getBpoint();
        break;
      case 3:
        geom = iV.getBpoly();
        break;
    }
    // output
    Result[] resultSet = new Result[countResult.size()];
    for (int i = 0; i < resultSet.length; i++) {
      // gets the timestamp and the results from count and divides it through the area
      String date = countResultSet[i].getTimestamp();
      String value = String
          .valueOf((Float.parseFloat(countResultSet[i].getValue()) / (Geo.areaOf(geom) / 1000000)));
      resultSet[i] = new Result(date, value);
    }
    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response =
        new ElementsResponseContent("-Hier könnte Ihre Lizenz stehen.-",
            "-Hier könnte Ihr Copyright stehen.-",
            new MetaData(duration, "items per square-kilometer",
                "Density of selected items (number of items per area).", requestURL),
            null, resultSet);
    return response;
  }

  /**
   * Gets the input parameters of the request and performs a ratio calculation.
   */
  private ElementsResponseContent executeRatio(boolean isPost, String[] bboxes, String[] bpoints,
      String[] bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String[] types2, String[] keys2, String[] values2)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Integer> result1;
    SortedMap<OSHDBTimestamp, Integer> result2;
    MapReducer<OSMEntitySnapshot> mapRed1;
    MapReducer<OSMEntitySnapshot> mapRed2;
    InputValidator iV = new InputValidator();
    String requestURL = null;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    // input parameter processing 1 and result 1
    mapRed1 =
        iV.processParameters(isPost, bboxes, bpoints, bpolys, types, keys, values, userids, time);
    result1 = mapRed1.aggregateByTimestamp().count();
    // input parameter processing 2 and result 2
    mapRed2 = iV.processParameters(isPost, bboxes, bpoints, bpolys, types2, keys2, values2, userids,
        time);
    result2 = mapRed2.aggregateByTimestamp().count();
    // resultSet 1
    Result[] resultSet1 = new Result[result1.size()];
    int count = 0;
    for (Entry<OSHDBTimestamp, Integer> entry : result1.entrySet()) {
      resultSet1[count] = new Result(entry.getKey().formatIsoDateTime(),
          String.valueOf(entry.getValue().intValue()));
      count++;
    }
    // output
    Result[] resultSet = new Result[result1.size()];
    count = 0;
    for (Entry<OSHDBTimestamp, Integer> entry : result2.entrySet()) {
      // gets the timestamp and the results from both counts and divides 2 through 1
      String date = resultSet1[count].getTimestamp();
      String value = String
          .valueOf(entry.getValue().floatValue() / Float.parseFloat(resultSet1[count].getValue()));
      resultSet[count] = new Result(date, value);
      count++;
    }
    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response = new ElementsResponseContent(
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
        new MetaData(duration, "ratio",
            "Ratio of items satisfying types2, keys2, values2 within items are selected by types, keys, values.",
            requestURL),
        null, resultSet);
    return response;
  }

  /**
   * Gets the input parameters of the request and performs a count grouped by types.
   */
  private ElementsResponseContent executeCountGroupByType(boolean isPost, String[] bboxes,
      String[] bpoints, String[] bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestampAndOtherIndex<OSMType>, Integer> result;
    SortedMap<OSMType, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputValidator iV = new InputValidator();
    String requestURL = null;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    // input parameter processing
    mapRed =
        iV.processParameters(isPost, bboxes, bpoints, bpolys, types, keys, values, userids, time);
    // db result
    result = mapRed.aggregateByTimestamp()
        .aggregateBy((SerializableFunction<OSMEntitySnapshot, OSMType>) f -> {
          return f.getEntity().getType();
        }).count();
    groupByResult = MapBiAggregatorByTimestamps.nest_IndexThenTime(result);
    // output
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    int count = 0;
    int innerCount = 0;
    // iterate over the entry objects aggregated by user
    for (Entry<OSMType, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult.entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      innerCount = 0;
      // iterate over the inner entry objects containing timestamp-value pairs
      for (Entry<OSHDBTimestamp, Integer> innerEntry : entry.getValue().entrySet()) {
        results[innerCount] = new Result(innerEntry.getKey().formatIsoDateTime(),
            String.valueOf(innerEntry.getValue()));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response = new ElementsResponseContent(
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
        new MetaData(duration, "amount", "Total number of items aggregated on the userids.",
            requestURL),
        resultSet, null);
    return response;
  }

  /**
   * Gets the input parameters of the request and performs a count grouped by boundary.
   */
  private ElementsResponseContent executeCountGroupByBoundary(boolean isPost, String[] bboxes,
      String[] bpoints, String[] bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    ArrayList<SortedMap<OSHDBTimestamp, Integer>> reqResults =
        new ArrayList<SortedMap<OSHDBTimestamp, Integer>>();
    String requestURL = null;
    String boundaryDescr;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    if (bboxes != null) {
      boundaryDescr = "bounding box ";
      for (int i = 0; i < bboxes.length; i += 4) {
        InputValidator iV = new InputValidator();
        SortedMap<OSHDBTimestamp, Integer> result;
        MapReducer<OSMEntitySnapshot> mapRed;
        // extraction of the bbox
        String[] bbox = new String[4];
        bbox[0] = bboxes[i];
        bbox[1] = bboxes[i + 1];
        bbox[2] = bboxes[i + 2];
        bbox[3] = bboxes[i + 3];
        // input parameter processing
        mapRed =
            iV.processParameters(isPost, bbox, bpoints, bpolys, types, keys, values, userids, time);
        // db result
        result = mapRed.aggregateByTimestamp().count();
        // add it to the array and increase counter
        reqResults.add(result);
      }
    } else if (bpoints != null) {
      boundaryDescr = "bounding point ";
      // bpoints given
    } else if (bpolys != null) {
      boundaryDescr = "bounding polygon ";
      // bpolys given
    } else {
      boundaryDescr = "whole dataset ";
      // no boundary --> default bbox == whole data
    }
    // output
    GroupByResult[] resultSet = new GroupByResult[reqResults.size()];
    int count = 1;
    int innerCount = 0;
    // iterate over each result
    for (SortedMap<OSHDBTimestamp, Integer> map : reqResults) {
      Result[] results = new Result[map.size()];
      innerCount = 0;
      // iterate over each entry in the map containing timestamp-value pairs
      for (Entry<OSHDBTimestamp, Integer> entry : map.entrySet()) {
        results[innerCount] =
            new Result(entry.getKey().formatIsoDateTime(), String.valueOf(entry.getValue()));
        innerCount++;
      }
      resultSet[count - 1] = new GroupByResult(boundaryDescr + String.valueOf(count), results);
      count++;
    }
    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response = new ElementsResponseContent(
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
        new MetaData(duration, "amount",
            "Total number of items aggregated on the bounding objects.", requestURL),
        resultSet, null);
    return response;
  }

  private ElementsResponseContent executeCountGroupByBoundaryyy(boolean isPost, String[] bboxes,
      String[] bpoints, String[] bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Integer> result;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputValidator iV = new InputValidator();
    String requestURL = null;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    // input parameter processing
    mapRed =
        iV.processParameters(isPost, bboxes, bpoints, bpolys, types, keys, values, userids, time);
    // db result
    result = mapRed.aggregateByTimestamp().count();


    // output
    Result[] resultSet = new Result[result.size()];
    int count = 0;
    for (Entry<OSHDBTimestamp, Integer> entry : result.entrySet()) {
      resultSet[count] = new Result(entry.getKey().formatIsoDateTime(),
          String.valueOf(entry.getValue().intValue()));
      count++;
    }



    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response = new ElementsResponseContent(
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
        new MetaData(duration, "amount",
            "Total number of elements, which are selected by the parameters.", requestURL),
        null, resultSet);
    return response;
  }

  /**
   * Gets the input parameters of the request and performs a count grouped by users.
   */
  private ElementsResponseContent executeCountGroupByUser(boolean isPost, String[] bboxes,
      String[] bpoints, String[] bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time) throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestampAndOtherIndex<Integer>, Integer> result;
    SortedMap<Integer, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputValidator iV = new InputValidator();
    String requestURL = null;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    // input parameter processing
    mapRed =
        iV.processParameters(isPost, bboxes, bpoints, bpolys, types, keys, values, userids, time);
    // db result
    result = mapRed.aggregateByTimestamp()
        .aggregateBy((SerializableFunction<OSMEntitySnapshot, Integer>) f -> {
          return f.getEntity().getUserId();
        }).count();
    groupByResult = MapBiAggregatorByTimestamps.nest_IndexThenTime(result);
    // output
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    int count = 0;
    int innerCount = 0;
    // iterate over the entry objects aggregated by user
    for (Entry<Integer, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult.entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      innerCount = 0;
      // iterate over the inner entry objects containing timestamp-value pairs
      for (Entry<OSHDBTimestamp, Integer> innerEntry : entry.getValue().entrySet()) {
        results[innerCount] = new Result(innerEntry.getKey().formatIsoDateTime(),
            String.valueOf(innerEntry.getValue()));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response = new ElementsResponseContent(
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
        new MetaData(duration, "amount", "Total number of items aggregated on the userids.",
            requestURL),
        resultSet, null);
    return response;
  }

  /**
   * Gets the input parameters of the request and performs a count grouped by tags.
   */
  private ElementsResponseContent executeCountGroupByTag(boolean isPost, String[] bboxes,
      String[] bpoints, String[] bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time, String[] groupByKey, String[] groupByValues)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestampAndOtherIndex<ImmutablePair<Integer, Integer>>, Integer> result;
    SortedMap<ImmutablePair<Integer, Integer>, SortedMap<OSHDBTimestamp, Integer>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputValidator iV = new InputValidator();
    String requestURL = null;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    // needed to get access to the keytables
    EventHolderBean bean = Application.getEventHolderBean();
    OSHDB_H2[] dbConnObjects = bean.getDbConnObjects();
    TagTranslator tt = new TagTranslator(dbConnObjects[1].getConnection());
    Integer[] keysInt = new Integer[groupByKey.length];
    Integer[] valuesInt = new Integer[groupByValues.length];
    if (groupByKey == null || groupByKey.length == 0) {
      throw new BadRequestException(
          "You need to give one groupByKey parameters, if you want to use groupBy/tag");
    }
    // input parameter processing
    mapRed =
        iV.processParameters(isPost, bboxes, bpoints, bpolys, types, keys, values, userids, time);
    // get the integer values for the given keys
    for (int i = 0; i < groupByKey.length; i++) {
      keysInt[i] = tt.key2Int(groupByKey[i]);
      if (groupByValues != null) {
        // get the integer values for the given values
        for (int j = 0; j < groupByValues.length; j++) {
          valuesInt[j] = tt.tag2Int(groupByKey[i], groupByValues[j]).getValue();
        }
      }
    }
    // group by tag logic
    result = mapRed.map(f -> {
      int[] tags = f.getEntity().getTags();
      for (int i = 0; i < tags.length; i += 2) {
        int tagKeyId = tags[i];
        int tagValueId = tags[i + 1];
        for (int key : keysInt) {
          // if key in input key list
          if (tagKeyId == key) {
            if (valuesInt.length == 0) {
              return new ImmutablePair<>(new ImmutablePair<Integer, Integer>(tagKeyId, tagValueId),
                  f);
            }
            for (int value : valuesInt) {
              // if value in input value list
              if (tagValueId == value)
                return new ImmutablePair<>(
                    new ImmutablePair<Integer, Integer>(tagKeyId, tagValueId), f);
            }
          }
        }
      }
      return new ImmutablePair<>(new ImmutablePair<Integer, Integer>(-1, -1), f);
    }).aggregateByTimestamp().aggregateBy(Pair::getKey).map(Pair::getValue).count();

    groupByResult = MapBiAggregatorByTimestamps.nest_IndexThenTime(result);

    // output
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    String groupByName = "";
    int count = 0;
    int innerCount = 0;
    // iterate over the entry objects aggregated by tags
    for (Entry<ImmutablePair<Integer, Integer>, SortedMap<OSHDBTimestamp, Integer>> entry : groupByResult
        .entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      innerCount = 0;
      // check for non-remainder objects (which do have the defined key/tag)
      if (entry.getKey().getKey() != -1 && entry.getKey().getValue() != -1) {
        groupByName = tt.tag2String(entry.getKey()).getValue();
      } else {
        groupByName = "remainder";
      }
      // iterate over the inner entry objects containing timestamp-value pairs
      for (Entry<OSHDBTimestamp, Integer> innerEntry : entry.getValue().entrySet()) {
        results[innerCount] = new Result(innerEntry.getKey().formatIsoDateTime(),
            String.valueOf(innerEntry.getValue()));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(groupByName, results);
      count++;
    }
    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response = new ElementsResponseContent(
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
        new MetaData(duration, "amount", "Total number of items aggregated on the tags.",
            requestURL),
        resultSet, null);
    return response;
  }

  /**
   * Gets the input parameters of the request and computes the length, perimeter, or area results
   * grouped by the user.
   * 
   * @param requestType <code>Byte</code> defining a length (1), perimeter (2), or area (3) request.
   */
  private ElementsResponseContent executeLengthPerimeterAreaGroupByUser(byte requestType,
      boolean isPost, String[] bboxes, String[] bpoints, String[] bpolys, String[] types,
      String[] keys, String[] values, String[] userids, String[] time)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestampAndOtherIndex<Integer>, Number> result;
    SortedMap<Integer, SortedMap<OSHDBTimestamp, Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputValidator iV = new InputValidator();
    String unit = "";
    String description = "";
    String requestURL = null;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    // input parameter processing
    mapRed =
        iV.processParameters(isPost, bboxes, bpoints, bpolys, types, keys, values, userids, time);
    // db result
    result = mapRed.aggregateByTimestamp()
        .aggregateBy((SerializableFunction<OSMEntitySnapshot, Integer>) f -> {
          return f.getEntity().getUserId();
        }).sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          switch (requestType) {
            case 1:
              return Geo.lengthOf(snapshot.getGeometry());
            case 2:
              if (snapshot.getGeometry() instanceof Polygonal)
                return Geo.lengthOf(snapshot.getGeometry().getBoundary());
              else
                return 0.0;
            case 3:
              return Geo.areaOf(snapshot.getGeometry());
            default:
              return 0.0;
          }
        });
    groupByResult = MapBiAggregatorByTimestamps.nest_IndexThenTime(result);
    // output
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    int count = 0;
    int innerCount = 0;
    // iterate over the entry objects aggregated by type
    for (Entry<Integer, SortedMap<OSHDBTimestamp, Number>> entry : groupByResult.entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      innerCount = 0;
      // iterate over the inner entry objects containing timestamp-value pairs
      for (Entry<OSHDBTimestamp, Number> innerEntry : entry.getValue().entrySet()) {
        results[innerCount] = new Result(innerEntry.getKey().formatIsoDateTime(),
            String.valueOf(innerEntry.getValue().floatValue()));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    // setting of the unit and description output parameters
    switch (requestType) {
      case 1:
        unit = "meter";
        description = "Total length of items aggregated on the userid.";
        break;
      case 2:
        unit = "meter";
        description = "Total perimeter of polygonal items aggregated on the userid.";
        break;
      case 3:
        unit = "square-meter";
        description = "Total area of items aggregated on the userid.";
        break;
    }
    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response = new ElementsResponseContent(
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
        new MetaData(duration, unit, description, requestURL), resultSet, null);
    return response;
  }

  /**
   * Gets the input parameters of the request and computes the area, or the perimeter grouped by the
   * OSM type.
   * 
   * @param isArea <code>Boolean</code> defining an area (true) or a length (false) request.
   */
  private ElementsResponseContent executeAreaPerimeterGroupByType(boolean isArea, boolean isPost,
      String[] bboxes, String[] bpoints, String[] bpolys, String[] types, String[] keys,
      String[] values, String[] userids, String[] time)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestampAndOtherIndex<OSMType>, Number> result;
    SortedMap<OSMType, SortedMap<OSHDBTimestamp, Number>> groupByResult;
    MapReducer<OSMEntitySnapshot> mapRed;
    InputValidator iV = new InputValidator();
    String unit;
    String description;
    String requestURL = null;
    // request url is only returned in output for GET requests
    if (!isPost)
      requestURL = ElementsRequestInterceptor.requestUrl;
    // input parameter processing
    mapRed =
        iV.processParameters(isPost, bboxes, bpoints, bpolys, types, keys, values, userids, time);
    // db result
    result = mapRed.aggregateByTimestamp()
        .aggregateBy((SerializableFunction<OSMEntitySnapshot, OSMType>) f -> {
          return f.getEntity().getType();
        }).sum((SerializableFunction<OSMEntitySnapshot, Number>) snapshot -> {
          if (isArea) {
            return Geo.areaOf(snapshot.getGeometry());
          } else {
            if (snapshot.getGeometry() instanceof Polygonal)
              return Geo.lengthOf(snapshot.getGeometry().getBoundary());
            else
              return 0.0;
          }
        });
    groupByResult = MapBiAggregatorByTimestamps.nest_IndexThenTime(result);
    // output
    GroupByResult[] resultSet = new GroupByResult[groupByResult.size()];
    int count = 0;
    int innerCount = 0;
    // iterate over the entry objects aggregated by type
    for (Entry<OSMType, SortedMap<OSHDBTimestamp, Number>> entry : groupByResult.entrySet()) {
      Result[] results = new Result[entry.getValue().entrySet().size()];
      innerCount = 0;
      // iterate over the inner entry objects containing timestamp-value pairs
      for (Entry<OSHDBTimestamp, Number> innerEntry : entry.getValue().entrySet()) {
        results[innerCount] = new Result(innerEntry.getKey().formatIsoDateTime(),
            String.valueOf(innerEntry.getValue().floatValue()));
        innerCount++;
      }
      resultSet[count] = new GroupByResult(entry.getKey().toString(), results);
      count++;
    }
    // setting of the unit and description output parameters
    if (isArea) {
      unit = "square-meter";
      description = "Total area of items aggregated on the OSM type.";
    } else {
      unit = "meter";
      description = "Total perimeter of items aggregated on the OSM type.";
    }
    long duration = System.currentTimeMillis() - startTime;
    // response
    ElementsResponseContent response = new ElementsResponseContent(
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,",
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.",
        new MetaData(duration, unit, description, requestURL), resultSet, null);
    return response;
  }

}
