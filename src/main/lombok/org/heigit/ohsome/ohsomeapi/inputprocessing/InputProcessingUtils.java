package org.heigit.ohsome.ohsomeapi.inputprocessing;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.exception.NotFoundException;
import org.heigit.ohsome.ohsomeapi.utilities.SpatialUtility;
import org.heigit.ohsome.ohsomeapi.utilities.TimeUtility;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;
import org.heigit.ohsome.oshdb.filter.FilterExpression;
import org.heigit.ohsome.oshdb.filter.FilterParser;
import org.heigit.ohsome.oshdb.util.mappable.OSHDBMapReducible;
import org.jparsec.error.ParserException;
import org.locationtech.jts.geom.Geometry;

/** Holds utility methods that are used by the input processing and executor classes. */
public class InputProcessingUtils implements Serializable {

  private final SpatialUtility spatialUtility = new SpatialUtility();
  private final TimeUtility timeUtility = new TimeUtility();

  /**
   * Finds and returns the EPSG code of the given point, which is needed for {@link
   * org.heigit.ohsome.ohsomeapi.inputprocessing.GeometryBuilder#createCircularPolygons(String[]
   * bcircles) createCircularPolygons}.
   *
   * <p>Adapted code from UTMCodeFromLonLat.java class in the osmatrix project (© by Michael Auer)
   *
   * @param lon Longitude coordinate of the point.
   * @param lat Latitude coordinate of the point.
   * @return <code>String</code> representing the corresponding EPSG code.
   */
  public String findEpsg(double lon, double lat) {

    // Special zones for Svalbard
    return spatialUtility.findEpsg(lon, lat);
  }

  /**
   * Splits the given bounding boxes and returns them in a <code>List</code>.
   *
   * @param bboxes contains the given bounding boxes
   * @return <code>List</code> containing the splitted bounding boxes
   * @throws BadRequestException if the bboxes parameter has an invalid format
   */
  public List<String> splitBboxes(String bboxes) {
    return spatialUtility.splitBboxes(bboxes);
  }

  /**
   * Splits the given bounding circles and returns them in a <code>List</code>.
   *
   * @param bcircles contains the given bounding circles
   * @return <code>List</code> containing the splitted bounding circles
   * @throws BadRequestException if the bcircles parameter has an invalid format
   */
  public List<String> splitBcircles(String bcircles) {
    return spatialUtility.splitBcircles(bcircles);
  }

  /**
   * Splits the given bounding polygons and returns them in a <code>List</code>.
   *
   * @param bpolys contains the given bounding polygons
   * @return <code>List</code> containing the splitted bounding polygons
   * @throws BadRequestException if the bpolys parameter has an invalid format
   */
  public List<String> splitBpolys(String bpolys) {
    return spatialUtility.splitBpolys(bpolys);
  }

  /**
   * Defines the toTimestamps for the result json object for /users responses.
   *
   * @param timeData contains the requested time
   * @return array having only the toTimestamps
   */
  public String[] defineToTimestamps(String[] timeData) {
    return timeUtility.defineToTimestamps(timeData);
  }

  /**
   * Extracts the time information out of the time parameter and checks the content on its format,
   * as well as <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO-8601</a> conformity. This
   * method is used if one datetimestring is given. Following time formats are allowed:
   * <ul>
   * <li><strong>YYYY-MM-DD</strong> or <strong>YYYY-MM-DDThh:mm:ss</strong>: When a timestamp
   * includes 'T', hh:mm must also be given. This applies for all time formats, which use
   * timestamps. If -MM-DD or only -DD is missing, '01' is used as default for month and day.</li>
   * <li><strong>YYYY-MM-DD/YYYY-MM-DD</strong>: start/end timestamps</li>
   * <li><strong>YYYY-MM-DD/YYYY-MM-DD/PnYnMnD</strong>: start/end/period where n refers to the size
   * of the respective period</li>
   * <li><strong>/YYYY-MM-DD</strong>: #/end where # equals the earliest timestamp</li>
   * <li><strong>/YYYY-MM-DD/PnYnMnD</strong>: #/end/period</li>
   * <li><strong>YYYY-MM-DD/</strong>: start/# where # equals the latest timestamp</li>
   * <li><strong>YYYY-MM-DD//PnYnMnD</strong>: start/#/period</li>
   * <li><strong>/</strong>: #/# where # equals the earliest and latest timestamp</li>
   * <li><strong>//PnYnMnD</strong>: #/#/period</li>
   * <li><strong>invalid</strong>: throws BadRequestException</li>
   * </ul>
   *
   * <p>For clarification: the format YYYY-MM-DDThh:mm:ss can be applied to any format, where a
   * timestamp is used and # is a replacement holder for "no value". Note that the positioning and
   * using of the forward slash '/' is very important.
   *
   * @param time <code>String</code> holding the unparsed time information.
   * @return <code>String</code> array containing the startTime at [0], the endTime at [1] and the
   *         period at [2].
   * @throws BadRequestException if the given time parameter is not ISO-8601 conform
   */
  public String[] extractIsoTime(String time) {
    return timeUtility.extractIsoTime(time);
  }

  /**
   * Sorts the given timestamps from oldest to newest.
   *
   * @throws BadRequestException if the given time parameter is not ISO-8601 conform
   */
  public String[] sortTimestamps(String[] timestamps) {
    return timeUtility.sortTimestamps(timestamps);
  }

  /**
   * Checks the given custom boundary id. At the moment only used if output format = csv.
   *
   * @throws BadRequestException if the custom ids contain semicolons
   */
  public void checkCustomBoundaryId(String id) {
    spatialUtility.checkCustomBoundaryId(id);
  }

  /**
   * Checks if the given geometry is within the underlying data-polygon. Returns also true if no
   * data-polygon is given.
   *
   * @param geom <code>Geometry</code>, which is tested against the data-polygon
   * @return <code>true</code> - if inside <br>
   *         <code>false</code> - if not inside
   */
  public boolean isWithin(Geometry geom) {
    return spatialUtility.isWithin(geom);
  }

  /** Checks if the given String is one of the simple feature types (point, line, polygon). */
  public boolean isSimpleFeatureType(String type) {
    return spatialUtility.isSimpleFeatureType(type);
  }

  /**
   * Applies an entity filter using only planar relations (relations with an area) on the given
   * MapReducer object. It uses the tags "type=multipolygon" and "type=boundary".
   */
  public <T extends OSHDBMapReducible> MapReducer<T> filterOnPlanarRelations(MapReducer<T> mapRed) {
    // further filtering to not look at all relations
    return spatialUtility.filterOnPlanarRelations(mapRed);
  }

  /**
   * Checks whether a geometry is of given feature type (Puntal|Lineal|Polygonal).
   *
   * @param simpleFeatureTypes a set of feature types
   * @return true if the geometry matches the given simpleFeatureTypes, otherwise false
   */
  public boolean checkGeometryOnSimpleFeatures(Geometry geom,
      Set<SimpleFeatureType> simpleFeatureTypes) {
    return spatialUtility.checkGeometryOnSimpleFeatures(geom, simpleFeatureTypes);
  }

  /**
   * Tries to parse the given filter using the given parser.
   *
   * @throws BadRequestException if the filter contains wrong syntax.
   */
  public FilterExpression parseFilter(FilterParser fp, String filter) {
    try {
      return fp.parse(filter);
    } catch (ParserException ex) {
      throw new BadRequestException(ExceptionMessages.FILTER_SYNTAX + " Detailed error message: "
          + ex.getMessage().replace("\n", " "));
    }
  }

  /**
   * Checks the provided time info on its temporal extent.
   *
   * @param timeInfo time information to check
   * @throws NotFoundException if the given time is not completely within the timerange of the
   *         underlying data
   * @throws BadRequestException if the timestamps are not ISO-8601 conform
   * @throws RuntimeException if the Date or DateTime Format are not supported
   */
  protected void checkTemporalExtend(String... timeInfo) {
    timeUtility.checkTemporalExtend(timeInfo);
  }

  /**
   * Checks the provided time info on its ISO conformity.
   *
   * @param timeInfo time information to check
   * @throws BadRequestException if the timestamps are not ISO-8601 conform.
   */
  protected void checkTimestampsOnIsoConformity(String... timeInfo) {
    timeUtility.checkTimestampsOnIsoConformity(timeInfo);
  }

  /**
   * Checks the provided period on its ISO conformity.
   *
   * @throws BadRequestException if the interval is not ISO-8601 conform.
   */
  protected void checkPeriodOnIsoConformity(String period) {
    timeUtility.checkPeriodOnIsoConformity(period);
  }

  /**
   * Splits the given boundary parameter (bboxes, bcircles, or bpolys) on '|' to seperate the
   * different bounding objects.
   *
   * @param boundaryParam <code>String</code> that contains the boundary parameter(s)
   * @return splitted boundaries
   */
  private String[] splitOnHyphen(String boundaryParam) {
    return spatialUtility.splitOnHyphen(boundaryParam);
  }

  /**
   * Splits the coordinates from the given boundaries array.
   *
   * @param boundariesArray contains the boundaries without a custom id
   * @return <code>List</code> containing the splitted boundaries
   */
  private List<String> splitBoundariesWithoutIds(String[] boundariesArray,
      BoundaryType boundaryType) {
    return spatialUtility.splitBoundariesWithoutIds(boundariesArray, boundaryType);
  }

  /**
   * Splits the ids and the coordinates from the given bounding boxes array.
   *
   * @param bboxesArray contains the bounding boxes having a custom id
   * @return <code>List</code> containing the splitted bounding boxes
   * @throws BadRequestException if the bboxes have invalid format
   */
  private List<String> splitBboxesWithIds(String[] bboxesArray) {
    return spatialUtility.splitBboxesWithIds(bboxesArray);
  }

  /**
   * Splits the ids and the coordinates from the given bounding circles array.
   *
   * @param bcirclesArray contains the bounding circles having a custom id
   * @return <code>List</code> containing the splitted bounding circles
   * @throws BadRequestException if the bcircles have invalid format
   */
  private List<String> splitBcirclesWithIds(String[] bcirclesArray) {
    return spatialUtility.splitBcirclesWithIds(bcirclesArray);
  }

  /**
   * Splits the ids and the coordinates from the given bounding polygons array.
   *
   * @param bpolysArray contains the bounding polygons having a custom id
   * @return <code>List</code> containing the splitted bounding polygons
   * @throws BadRequestException if the bpolys have invalid format
   */
  private List<String> splitBpolysWithIds(String[] bpolysArray) {
    return spatialUtility.splitBpolysWithIds(bpolysArray);
  }

  /**
   * Checks the given boundaries list on their length. Bounding box and polygon list must be even,
   * bounding circle list must be divisable by three.
   *
   * @param boundaries parameter to check the length
   * @throws BadRequestException if the length is not even or divisible by three
   */
  private void checkBoundaryParamLength(List<String> boundaries, BoundaryType boundaryType) {
    spatialUtility.checkBoundaryParamLength(boundaries, boundaryType);
  }

  /** Internal helper method to get the toTimestamps from a timestampList. */
  private String[] getToTimestampsFromTimestamplist(String[] timeData) {
    return timeUtility.getToTimestampsFromTimestamplist(timeData);
  }

  public Object[] getBoundaryIds() {
    return spatialUtility.getBoundaryIds();
  }

  public String[] getToTimestamps() {
    return timeUtility.getToTimestamps();
  }

  public void setBoundaryIds(Serializable[] boundaryIds) {
    spatialUtility.setBoundaryIds(boundaryIds);
  }

  public void setToTimestamps(String[] toTimestamps) {
    timeUtility.setToTimestamps(toTimestamps);
  }

  public SpatialUtility getSpatialUtility() {
    return spatialUtility;
  }

  public TimeUtility getTimeUtility() {
    return timeUtility;
  }
}

