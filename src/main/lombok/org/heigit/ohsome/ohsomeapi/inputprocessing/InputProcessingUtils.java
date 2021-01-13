package org.heigit.ohsome.ohsomeapi.inputprocessing;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.object.OSHDBMapReducible;
import org.heigit.bigspatialdata.oshdb.osm.OSMType;
import org.heigit.bigspatialdata.oshdb.util.OSHDBTag;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;
import org.heigit.bigspatialdata.oshdb.util.time.IsoDateTimeParser;
import org.heigit.bigspatialdata.oshdb.util.time.OSHDBTimestamps;
import org.heigit.bigspatialdata.oshdb.util.time.TimestampFormatter;
import org.heigit.ohsome.filter.FilterExpression;
import org.heigit.ohsome.filter.FilterParser;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.exception.NotFoundException;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.jparsec.error.ParserException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Lineal;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.geom.Puntal;

/** Holds utility methods that are used by the input processing and executor classes. */
public class InputProcessingUtils {

  private static final String GEOMCOLLTYPE = "GeometryCollection";
  private Object[] boundaryIds;
  private String[] toTimestamps = null;

  /**
   * Finds and returns the EPSG code of the given point, which is needed for
   * {@link org.heigit.ohsome.ohsomeapi.inputprocessing.GeometryBuilder#createCircularPolygons(String[] bcircles)
   * createCircularPolygons}.
   * 
   * <p>
   * Adapted code from UTMCodeFromLonLat.java class in the osmatrix project (© by Michael Auer)
   * 
   * @param lon Longitude coordinate of the point.
   * @param lat Latitude coordinate of the point.
   * @return <code>String</code> representing the corresponding EPSG code.
   */
  public String findEpsg(double lon, double lat) {

    if (lat >= 84) {
      return "EPSG:32661"; // UPS North
    }
    if (lat < -80) {
      return "EPSG:32761"; // UPS South
    }
    int zoneNumber = (int) (Math.floor((lon + 180) / 6) + 1);
    if (lat >= 56.0 && lat < 64.0 && lon >= 3.0 && lon < 12.0) {
      zoneNumber = 32;
    }
    // Special zones for Svalbard
    if (lat >= 72.0 && lat < 84.0) {
      if (lon >= 0.0 && lon < 9.0) {
        zoneNumber = 31;
      } else if (lon >= 9.0 && lon < 21.0) {
        zoneNumber = 33;
      } else if (lon >= 21.0 && lon < 33.0) {
        zoneNumber = 35;
      } else if (lon >= 33.0 && lon < 42.0) {
        zoneNumber = 37;
      }
    }
    String isNorth = lat > 0 ? "6" : "7";
    String zone = zoneNumber < 10 ? "0" + zoneNumber : "" + zoneNumber;
    return "EPSG:32" + isNorth + zone;
  }

  /**
   * Splits the given bounding boxes and returns them in a <code>List</code>.
   * 
   * @param bboxes contains the given bounding boxes
   * @return <code>List</code> containing the splitted bounding boxes
   * @throws BadRequestException if the bboxes parameter has an invalid format
   */
  public List<String> splitBboxes(String bboxes) {
    String[] bboxesArray = splitOnHyphen(bboxes);
    List<String> boundaryParamValues = new ArrayList<>();
    boundaryIds = new Object[bboxesArray.length];
    try {
      if (bboxesArray[0].contains(":")) {
        boundaryParamValues = splitBboxesWithIds(bboxesArray);
      } else {
        boundaryParamValues = splitBoundariesWithoutIds(bboxesArray, BoundaryType.BBOXES);
      }
    } catch (Exception e) {
      if (e.getClass() == BadRequestException.class) {
        throw e;
      }
      throw new BadRequestException(ExceptionMessages.BOUNDARY_PARAM_FORMAT);
    }
    boundaryParamValues.removeAll(Collections.singleton(null));
    return boundaryParamValues;
  }

  /**
   * Splits the given bounding circles and returns them in a <code>List</code>.
   * 
   * @param bcircles contains the given bounding circles
   * @return <code>List</code> containing the splitted bounding circles
   * @throws BadRequestException if the bcircles parameter has an invalid format
   */
  public List<String> splitBcircles(String bcircles) {
    String[] bcirclesArray = splitOnHyphen(bcircles);
    List<String> boundaryParamValues = new ArrayList<>();
    boundaryIds = new Object[bcirclesArray.length];
    try {
      if (bcirclesArray[0].contains(":")) {
        boundaryParamValues = splitBcirclesWithIds(bcirclesArray);
      } else {
        boundaryParamValues = splitBoundariesWithoutIds(bcirclesArray, BoundaryType.BCIRCLES);
      }
    } catch (Exception e) {
      if (e.getClass() == BadRequestException.class) {
        throw e;
      }
      throw new BadRequestException(ExceptionMessages.BOUNDARY_PARAM_FORMAT);
    }
    boundaryParamValues.removeAll(Collections.singleton(null));
    return boundaryParamValues;
  }

  /**
   * Splits the given bounding polygons and returns them in a <code>List</code>.
   * 
   * @param bpolys contains the given bounding polygons
   * @return <code>List</code> containing the splitted bounding polygons
   * @throws BadRequestException if the bpolys parameter has an invalid format
   */
  public List<String> splitBpolys(String bpolys) {
    String[] bpolysArray = splitOnHyphen(bpolys);
    List<String> boundaryParamValues = new ArrayList<>();
    boundaryIds = new Object[bpolysArray.length];
    try {
      if (bpolysArray[0].contains(":")) {
        boundaryParamValues = splitBpolysWithIds(bpolysArray);

      } else if (bpolysArray[0].contains(",")) {
        boundaryParamValues = splitBoundariesWithoutIds(bpolysArray, BoundaryType.BPOLYS);
      } else {
        throw new BadRequestException(ExceptionMessages.BOUNDARY_PARAM_FORMAT);
      }
    } catch (Exception e) {
      if (e.getClass() == BadRequestException.class) {
        throw e;
      }
      throw new BadRequestException(ExceptionMessages.BOUNDARY_PARAM_FORMAT);
    }
    boundaryParamValues.removeAll(Collections.singleton(null));
    return boundaryParamValues;
  }

  /**
   * Defines the toTimestamps for the result json object for /users responses.
   * 
   * @param timeData contains the requested time
   * @return array having only the toTimestamps
   */
  public String[] defineToTimestamps(String[] timeData) {
    OSHDBTimestamps timestamps;
    if (timeData.length == 3 && timeData[2] != null) {
      // needed to check for interval
      if (timeData[2].startsWith("P")) {
        timestamps = new OSHDBTimestamps(timeData[0], timeData[1], timeData[2]);
        toTimestamps = timestamps.get().stream()
            .map(oshdbTimestamp -> TimestampFormatter.getInstance().isoDateTime(oshdbTimestamp))
            .toArray(String[]::new);
      } else {
        // list of timestamps
        toTimestamps = getToTimestampsFromTimestamplist(timeData);
      }
    } else {
      // list of timestamps
      toTimestamps = getToTimestampsFromTimestamplist(timeData);
    }
    return toTimestamps;
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
   * <p>
   * For clarification: the format YYYY-MM-DDThh:mm:ss can be applied to any format, where a
   * timestamp is used and # is a replacement holder for "no value". Note that the positioning and
   * using of the forward slash '/' is very important.
   * 
   * @param time <code>String</code> holding the unparsed time information.
   * @return <code>String</code> array containing the startTime at [0], the endTime at [1] and the
   *         period at [2].
   * @throws BadRequestException if the given time parameter is not ISO-8601 conform
   */
  public String[] extractIsoTime(String time) {
    String[] split = time.split("/");
    if (split.length == 0 && !"/".equals(time)) {
      // invalid time parameter
      throw new BadRequestException(ExceptionMessages.TIME_FORMAT);
    }
    String[] timeVals = new String[3];
    if (time.startsWith("/")) {
      if (time.length() == 1) {
        // only /
        timeVals[0] = ExtractMetadata.fromTstamp;
        timeVals[1] = ExtractMetadata.toTstamp;
        return timeVals;
      }
      if (split[0].length() == 0 && split.length == 2) {
        // /YYYY-MM-DD
        checkTimestampsOnIsoConformity(split[1]);
        checkTemporalExtend(split[1]);
        timeVals[1] = split[1];
      } else if (split.length == 3 && split[0].length() == 0 && split[1].length() == 0) {
        // //PnYnMnD
        checkPeriodOnIsoConformity(split[2]);
        timeVals[1] = ExtractMetadata.toTstamp;
        timeVals[2] = split[2];
      } else if (split.length == 3 && split[1].length() != 0) {
        // /YYYY-MM-DD/PnYnMnD
        checkTimestampsOnIsoConformity(split[1]);
        checkTemporalExtend(split[1]);
        checkPeriodOnIsoConformity(split[2]);
        timeVals[1] = split[1];
        timeVals[2] = split[2];
      } else {
        // invalid time parameter
        throw new BadRequestException(ExceptionMessages.TIME_FORMAT);
      }
      timeVals[0] = ExtractMetadata.fromTstamp;
    } else if (time.endsWith("/")) {
      if (split.length != 1) {
        // invalid time parameter
        throw new BadRequestException(ExceptionMessages.TIME_FORMAT);
      }
      // YYYY-MM-DD/
      checkTimestampsOnIsoConformity(split[0]);
      checkTemporalExtend(split[0]);
      timeVals[0] = split[0];
      timeVals[1] = ExtractMetadata.toTstamp;
    } else if (split.length == 3) {
      if (split[1].length() == 0) {
        // YYYY-MM-DD//PnYnMnD
        checkTimestampsOnIsoConformity(split[0]);
        checkTemporalExtend(split[0]);
        timeVals[1] = ExtractMetadata.toTstamp;
        timeVals[2] = split[2];
      } else {
        // YYYY-MM-DD/YYYY-MM-DD/PnYnMnD
        checkTimestampsOnIsoConformity(split[0], split[1]);
        checkTemporalExtend(split[0], split[1]);
        timeVals[1] = split[1];
      }
      checkPeriodOnIsoConformity(split[2]);
      timeVals[0] = split[0];
      timeVals[2] = split[2];
    } else if (split.length == 2) {
      // YYYY-MM-DD/YYYY-MM-DD
      checkTimestampsOnIsoConformity(split[0], split[1]);
      checkTemporalExtend(split[0], split[1]);
      timeVals[0] = split[0];
      timeVals[1] = split[1];
    } else if (split.length == 1) {
      // YYYY-MM-DD
      checkTimestampsOnIsoConformity(split[0]);
      checkTemporalExtend(split[0]);
      timeVals[0] = split[0];
      return timeVals;
    } else {
      // invalid time parameter
      throw new BadRequestException(ExceptionMessages.TIME_FORMAT);
    }
    String[] sortedTimestamps = sortTimestamps(new String[] {timeVals[0], timeVals[1]});
    timeVals[0] = sortedTimestamps[0];
    timeVals[1] = sortedTimestamps[1];
    return timeVals;
  }

  /**
   * Sorts the given timestamps from oldest to newest.
   * 
   * @throws BadRequestException if the given time parameter is not ISO-8601 conform
   */
  public String[] sortTimestamps(String[] timestamps) {
    List<String> timeStringList = new ArrayList<>();
    for (String timestamp : timestamps) {
      try {
        ZonedDateTime zdt = IsoDateTimeParser.parseIsoDateTime(timestamp);
        checkTemporalExtend(zdt.format(DateTimeFormatter.ISO_DATE_TIME));
        timeStringList.add(zdt.format(DateTimeFormatter.ISO_DATE_TIME));
      } catch (Exception e) {
        throw new BadRequestException(ExceptionMessages.TIME_FORMAT);
      }
    }
    Collections.sort(timeStringList);
    return timeStringList.toArray(timestamps);
  }

  /**
   * Checks the given custom boundary id. At the moment only used if output format = csv.
   * 
   * @throws BadRequestException if the custom ids contain semicolons
   */
  public void checkCustomBoundaryId(String id) {
    if (id.contains(";")) {
      throw new BadRequestException("The given custom ids cannot contain semicolons, "
          + "if you want to use csv as output format.");
    }
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
    if (ExtractMetadata.dataPoly != null) {
      return geom.within(ExtractMetadata.dataPoly);
    }
    return true;
  }

  /** Checks if the given String is one of the simple feature types (point, line, polygon). */
  public boolean isSimpleFeatureType(String type) {
    return "point".equalsIgnoreCase(type) || "line".equalsIgnoreCase(type)
        || "polygon".equalsIgnoreCase(type) || "other".equalsIgnoreCase(type);
  }

  /**
   * Applies an entity filter using only planar relations (relations with an area) on the given
   * MapReducer object. It uses the tags "type=multipolygon" and "type=boundary".
   */
  public <T extends OSHDBMapReducible> MapReducer<T> filterOnPlanarRelations(MapReducer<T> mapRed) {
    // further filtering to not look at all relations
    TagTranslator tt = DbConnData.tagTranslator;
    OSHDBTag typeMultipolygon = tt.getOSHDBTagOf("type", "multipolygon");
    OSHDBTag typeBoundary = tt.getOSHDBTagOf("type", "boundary");
    mapRed.osmEntityFilter(entity -> !entity.getType().equals(OSMType.RELATION)
        || entity.hasTagValue(typeMultipolygon.getKey(), typeMultipolygon.getValue())
        || entity.hasTagValue(typeBoundary.getKey(), typeBoundary.getValue()));
    return mapRed;
  }

  /**
   * Checks whether a geometry is of given feature type (Puntal|Lineal|Polygonal).
   *
   * @param simpleFeatureTypes a set of feature types
   * @return true if the geometry matches the given simpleFeatureTypes, otherwise false
   */
  public boolean checkGeometryOnSimpleFeatures(Geometry geom,
      Set<SimpleFeatureType> simpleFeatureTypes) {
    return simpleFeatureTypes.contains(SimpleFeatureType.POLYGON) && geom instanceof Polygonal
        || simpleFeatureTypes.contains(SimpleFeatureType.POINT) && geom instanceof Puntal
        || simpleFeatureTypes.contains(SimpleFeatureType.LINE) && geom instanceof Lineal
        || simpleFeatureTypes.contains(SimpleFeatureType.OTHER)
            && GEOMCOLLTYPE.equalsIgnoreCase(geom.getGeometryType());
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
    long start = 0;
    long end = 0;
    long timestampLong = 0;
    try {
      start = IsoDateTimeParser.parseIsoDateTime(ExtractMetadata.fromTstamp).toEpochSecond();
      end = IsoDateTimeParser.parseIsoDateTime(ExtractMetadata.toTstamp).toEpochSecond();
    } catch (Exception e) {
      throw new RuntimeException(
          "The ISO 8601 Date or the combined Date-Time String cannot be converted into a UTC based ZonedDateTime Object");
    }
    for (String timestamp : timeInfo) {
      try {
        ZonedDateTime zdt = IsoDateTimeParser.parseIsoDateTime(timestamp);
        timestampLong =
            DateTimeFormatter.ISO_DATE_TIME.parse(zdt.format(DateTimeFormatter.ISO_DATE_TIME))
                .getLong(ChronoField.INSTANT_SECONDS);
        if (timestampLong < start || timestampLong > end) {
          throw new NotFoundException(
              "The given time parameter is not completely within the timeframe ("
                  + ExtractMetadata.fromTstamp + " to " + ExtractMetadata.toTstamp
                  + ") of the underlying osh-data.");
        }
      } catch (NotFoundException e) {
        throw e;
      } catch (Exception e) {
        throw new BadRequestException(ExceptionMessages.TIME_FORMAT);
      }
    }
  }

  /**
   * Checks the provided time info on its ISO conformity.
   * 
   * @param timeInfo time information to check
   * @throws BadRequestException if the timestamps are not ISO-8601 conform.
   */
  protected void checkTimestampsOnIsoConformity(String... timeInfo) {
    for (String timestamp : timeInfo) {
      try {
        IsoDateTimeParser.parseIsoDateTime(timestamp);
      } catch (Exception e) {
        throw new BadRequestException(ExceptionMessages.TIME_FORMAT);
      }
    }
  }

  /**
   * Checks the provided period on its ISO conformity.
   * 
   * @throws BadRequestException if the interval is not ISO-8601 conform.
   */
  protected void checkPeriodOnIsoConformity(String period) {
    try {
      IsoDateTimeParser.parseIsoPeriod(period);
    } catch (Exception e) {
      throw new BadRequestException(
          "The interval (period) of the provided time parameter is not ISO-8601 conform.");
    }
  }

  /**
   * Splits the given boundary parameter (bboxes, bcircles, or bpolys) on '|' to seperate the
   * different bounding objects.
   * 
   * @param boundaryParam <code>String</code> that contains the boundary parameter(s)
   * @return splitted boundaries
   */
  private String[] splitOnHyphen(String boundaryParam) {
    if (boundaryParam.contains("|")) {
      return boundaryParam.split("\\|");
    }
    return new String[] {boundaryParam};
  }

  /**
   * Splits the coordinates from the given boundaries array.
   * 
   * @param boundariesArray contains the boundaries without a custom id
   * @return <code>List</code> containing the splitted boundaries
   */
  private List<String> splitBoundariesWithoutIds(String[] boundariesArray,
      BoundaryType boundaryType) {
    List<String> boundaryParamValues = new ArrayList<>();
    for (int i = 0; i < boundariesArray.length; i++) {
      String[] coords = boundariesArray[i].split("\\,");
      for (String coord : coords) {
        boundaryParamValues.add(coord);
      }
      boundaryIds[i] = "boundary" + (i + 1);
    }
    checkBoundaryParamLength(boundaryParamValues, boundaryType);
    return boundaryParamValues;
  }

  /**
   * Splits the ids and the coordinates from the given bounding boxes array.
   * 
   * @param bboxesArray contains the bounding boxes having a custom id
   * @return <code>List</code> containing the splitted bounding boxes
   * @throws BadRequestException if the bboxes have invalid format
   */
  private List<String> splitBboxesWithIds(String[] bboxesArray) {
    List<String> boundaryParamValues = new ArrayList<>();
    for (int i = 0; i < bboxesArray.length; i++) {
      String[] coords = bboxesArray[i].split("\\,");
      if (coords.length != 4) {
        throw new BadRequestException(ExceptionMessages.BOUNDARY_PARAM_FORMAT);
      }
      if (coords[0].contains(":")) {
        String[] idAndCoordinate = coords[0].split(":");
        // extract the id
        boundaryIds[i] = idAndCoordinate[0];
        // extract the coordinates
        boundaryParamValues.add(idAndCoordinate[1]);
        boundaryParamValues.add(coords[1]);
        boundaryParamValues.add(coords[2]);
        boundaryParamValues.add(coords[3]);
      } else {
        throw new BadRequestException(ExceptionMessages.BOUNDARY_IDS_FORMAT);
      }
    }
    checkBoundaryParamLength(boundaryParamValues, BoundaryType.BBOXES);
    return boundaryParamValues;
  }

  /**
   * Splits the ids and the coordinates from the given bounding circles array.
   * 
   * @param bcirclesArray contains the bounding circles having a custom id
   * @return <code>List</code> containing the splitted bounding circles
   * @throws BadRequestException if the bcircles have invalid format
   */
  private List<String> splitBcirclesWithIds(String[] bcirclesArray) {
    List<String> boundaryParamValues = new ArrayList<>();
    for (int i = 0; i < bcirclesArray.length; i++) {
      String[] coords = bcirclesArray[i].split("\\,");
      if (coords.length != 3) {
        throw new BadRequestException(ExceptionMessages.BOUNDARY_PARAM_FORMAT);
      }
      String[] idAndCoordinate = coords[0].split(":");
      boundaryIds[i] = idAndCoordinate[0];
      // extract the coordinate
      boundaryParamValues.add(idAndCoordinate[1]);
      boundaryParamValues.add(coords[1]);
      // extract the radius
      boundaryParamValues.add(coords[2]);
    }
    checkBoundaryParamLength(boundaryParamValues, BoundaryType.BCIRCLES);
    return boundaryParamValues;
  }

  /**
   * Splits the ids and the coordinates from the given bounding polygons array.
   * 
   * @param bpolysArray contains the bounding polygons having a custom id
   * @return <code>List</code> containing the splitted bounding polygons
   * @throws BadRequestException if the bpolys have invalid format
   */
  private List<String> splitBpolysWithIds(String[] bpolysArray) {
    List<String> boundaryParamValues = new ArrayList<>();
    for (int i = 0; i < bpolysArray.length; i++) {
      String[] coords = bpolysArray[i].split("\\,");
      String[] idAndCoordinate = coords[0].split(":");
      // extract the id and the first coordinate
      boundaryIds[i] = idAndCoordinate[0];
      boundaryParamValues.add(idAndCoordinate[1]);
      // extract the other coordinates
      for (int j = 1; j < coords.length; j++) {
        if (coords[j].contains(":")) {
          throw new BadRequestException(ExceptionMessages.BOUNDARY_PARAM_FORMAT);
        }
        boundaryParamValues.add(coords[j]);
      }
    }
    checkBoundaryParamLength(boundaryParamValues, BoundaryType.BPOLYS);
    return boundaryParamValues;
  }

  /**
   * Checks the given boundaries list on their length. Bounding box and polygon list must be even,
   * bounding circle list must be divisable by three.
   * 
   * @param boundaries parameter to check the length
   * @throws BadRequestException if the length is not even or divisible by three
   */
  private void checkBoundaryParamLength(List<String> boundaries, BoundaryType boundaryType) {
    if ((boundaryType.equals(BoundaryType.BBOXES) || boundaryType.equals(BoundaryType.BPOLYS))
        && boundaries.size() % 2 != 0) {
      throw new BadRequestException(ExceptionMessages.BOUNDARY_PARAM_FORMAT);
    }
    if (boundaryType.equals(BoundaryType.BCIRCLES) && boundaries.size() % 3 != 0) {
      throw new BadRequestException(ExceptionMessages.BOUNDARY_PARAM_FORMAT);
    }
  }

  /** Internal helper method to get the toTimestamps from a timestampList. */
  private String[] getToTimestampsFromTimestamplist(String[] timeData) {
    toTimestamps = new String[timeData.length];
    for (int i = 0; i < timeData.length; i++) {
      try {
        toTimestamps[i] =
            IsoDateTimeParser.parseIsoDateTime(timeData[i]).format(DateTimeFormatter.ISO_DATE_TIME);
      } catch (Exception e) {
        // time gets checked earlier already, so no exception should appear here
      }
    }
    return toTimestamps;
  }

  public Object[] getBoundaryIds() {
    return boundaryIds;
  }

  public String[] getToTimestamps() {
    return toTimestamps;
  }

  public void setBoundaryIds(Object[] boundaryIds) {
    this.boundaryIds = boundaryIds;
  }

  public void setToTimestamps(String[] toTimestamps) {
    this.toTimestamps = toTimestamps;
  }
}
