package org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.NotFoundException;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.bigspatialdata.oshdb.util.time.ISODateTimeParser;
import org.heigit.bigspatialdata.oshdb.util.time.OSHDBTimestamps;
import org.heigit.bigspatialdata.oshdb.util.time.TimestampFormatter;
import com.vividsolutions.jts.geom.Geometry;

/** Holds utility methods that are used by the input processing and executor classes. */
public class InputProcessingUtils {

  private Object[] boundaryIds;
  private String[] toTimestamps = null;

  /**
   * Finds and returns the EPSG code of the given point, which is needed for
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.GeometryBuilder#createCircularPolygons(String[] bcircles)
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
    String isNorth = (lat > 0) ? "6" : "7";
    String zone = (zoneNumber < 10) ? "0" + zoneNumber : "" + zoneNumber;
    return "EPSG:32" + isNorth + zone;
  }

  /**
   * Splits the given boundary parameter (bboxes, bcircles, or bpolys) on '|' to seperate the
   * different bounding objects.
   */
  public String[] splitOnHyphen(String boundaryParam) {
    if (boundaryParam.contains("|")) {
      return boundaryParam.split("\\|");
    }
    return new String[] {boundaryParam};
  }

  /** Splits the given bboxes String and returns its content as a String array. */
  public ArrayList<String> splitBboxes(String bboxes) {
    String[] bboxesArray = splitOnHyphen(bboxes);
    ArrayList<String> boundaryParamValues = new ArrayList<String>();
    String[] boundaryIds = new String[bboxesArray.length];
    String[] coords;
    int idCount = 0;
    try {
      if (bboxesArray[0].contains(":")) {
        // custom ids are given
        for (String boundaryObject : bboxesArray) {
          coords = boundaryObject.split("\\,");
          if (coords.length != 4) {
            throw new BadRequestException(ExceptionMessages.boundaryParamFormat);
          }
          if (coords[0].contains(":")) {
            String[] idAndCoordinate = coords[0].split(":");
            // extract the id
            boundaryIds[idCount] = idAndCoordinate[0];
            // extract the coordinates
            boundaryParamValues.add(idAndCoordinate[1]);
            boundaryParamValues.add(coords[1]);
            boundaryParamValues.add(coords[2]);
            boundaryParamValues.add(coords[3]);
            idCount++;
          } else {
            throw new BadRequestException(ExceptionMessages.boundaryIdsFormat);
          }
        }
      } else {
        // no custom ids are given
        idCount = 1;
        for (String boundaryObject : bboxesArray) {
          coords = boundaryObject.split("\\,");
          for (String coord : coords) {
            boundaryParamValues.add(coord);
          }
          // adding of ids
          boundaryIds[idCount - 1] = "bbox" + String.valueOf(idCount);
          idCount++;
        }
      }
    } catch (Exception e) {
      if (e.getClass() == BadRequestException.class) {
        throw e;
      }
      throw new BadRequestException(ExceptionMessages.boundaryParamFormat);
    }
    this.boundaryIds = boundaryIds;
    boundaryParamValues.removeAll(Collections.singleton(null));
    return boundaryParamValues;
  }

  /** Splits the given bcircles String and returns its content as a String array. */
  public ArrayList<String> splitBcircles(String bcircles) {
    String[] bcirclesArray = splitOnHyphen(bcircles);
    ArrayList<String> boundaryParamValues = new ArrayList<String>();
    String[] boundaryIds = new String[bcirclesArray.length];
    String[] coords;
    int idCount = 0;
    try {
      if (bcirclesArray[0].contains(":")) {
        for (String boundaryObject : bcirclesArray) {
          coords = boundaryObject.split("\\,");
          if (coords.length != 3) {
            throw new BadRequestException(ExceptionMessages.boundaryParamFormat);
          }
          if (coords[0].contains(":")) {
            String[] idAndCoordinate = coords[0].split(":");
            boundaryIds[idCount] = idAndCoordinate[0];
            // extract the coordinate
            boundaryParamValues.add(idAndCoordinate[1]);
            boundaryParamValues.add(coords[1]);
            // extract the radius
            boundaryParamValues.add(coords[2]);
            idCount++;
          } else {
            throw new BadRequestException(ExceptionMessages.boundaryIdsFormat);
          }
        }
      } else {
        idCount = 1;
        for (String boundaryObject : bcirclesArray) {
          coords = boundaryObject.split("\\,");
          for (String coord : coords) {
            boundaryParamValues.add(coord);
          }
          boundaryIds[idCount - 1] = "bcircle" + String.valueOf(idCount);
          idCount++;
        }
      }
    } catch (Exception e) {
      if (e.getClass() == BadRequestException.class) {
        throw e;
      }
      throw new BadRequestException(ExceptionMessages.boundaryParamFormat);
    }
    this.boundaryIds = boundaryIds;
    boundaryParamValues.removeAll(Collections.singleton(null));
    return boundaryParamValues;
  }

  /** Splits the given bpolys String and returns its content as an ArrayList. */
  public ArrayList<String> splitBpolys(String bpolys) {
    String[] bpolysArray = splitOnHyphen(bpolys);
    ArrayList<String> boundaryParamValues = new ArrayList<String>();
    String[] boundaryIds = new String[bpolysArray.length];
    String[] coords;
    int idCount = 0;
    try {
      if (bpolysArray[0].contains(":")) {
        for (String boundaryObject : bpolysArray) {
          coords = boundaryObject.split("\\,");
          if (coords[0].contains(":")) {
            String[] idAndCoordinate = coords[0].split(":");
            // extract the id and the first coordinate
            boundaryIds[idCount] = idAndCoordinate[0];
            boundaryParamValues.add(idAndCoordinate[1]);
            // extract the other coordinates
            for (int i = 1; i < coords.length; i++) {
              if (coords[i].contains(":")) {
                throw new BadRequestException(ExceptionMessages.boundaryParamFormat);
              }
              boundaryParamValues.add(coords[i]);
            }
            idCount++;
          } else {
            throw new BadRequestException(ExceptionMessages.boundaryIdsFormat);
          }
        }
      } else if (bpolysArray[0].contains(",")) {
        idCount = 1;
        for (String boundaryObject : bpolysArray) {
          coords = boundaryObject.split("\\,");
          for (String coord : coords) {
            boundaryParamValues.add(coord);
          }
          boundaryIds[idCount - 1] = "bpoly" + String.valueOf(idCount);
          idCount++;
        }
      } else {
        throw new BadRequestException(ExceptionMessages.boundaryParamFormat);
      }
    } catch (Exception e) {
      if (e.getClass() == BadRequestException.class) {
        throw e;
      }
      throw new BadRequestException(ExceptionMessages.boundaryParamFormat);
    }
    this.boundaryIds = boundaryIds;
    boundaryParamValues.removeAll(Collections.singleton(null));
    return boundaryParamValues;
  }

  /**
   * Defines the toTimestamps for the result json object for /users responses.
   */
  public String[] defineToTimestamps(String[] timeData) {
    String[] toTimestamps;
    OSHDBTimestamps timestamps;
    if (timeData.length == 3 && timeData[2] != null) {
      // needed to check for interval
      if (timeData[2].startsWith("P")) {
        timestamps = new OSHDBTimestamps(timeData[0], timeData[1], timeData[2]);
        toTimestamps = timestamps.get().stream().map(oshdbTimestamp -> {
          return TimestampFormatter.getInstance().isoDateTime(oshdbTimestamp);
        }).toArray(String[]::new);
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
   * @return <code>String</code> array containing the startTime at at [0], the endTime at [1] and
   *         the period at [2].
   */
  public String[] extractIsoTime(String time) throws Exception {
    String[] timeVals = new String[3];
    String[] split = time.split("/");
    if (split.length == 0 && !"/".equals(time)) {
      // invalid time parameter
      throw new BadRequestException(ExceptionMessages.timeFormat);
    }
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
        throw new BadRequestException(ExceptionMessages.timeFormat);
      }
      timeVals[0] = ExtractMetadata.fromTstamp;
    } else if (time.endsWith("/")) {
      if (split.length != 1) {
        // invalid time parameter
        throw new BadRequestException(ExceptionMessages.timeFormat);
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
      throw new BadRequestException(ExceptionMessages.timeFormat);
    }
    String[] sortedTimestamps = sortTimestamps(new String[] {timeVals[0], timeVals[1]});
    timeVals[0] = sortedTimestamps[0];
    timeVals[1] = sortedTimestamps[1];
    return timeVals;
  }

  /**
   * Checks the provided time info on its temporal extent. Throws a 404 NotFoundException if it is
   * not completely within the timerange of the underlying data, or a 400 BadRequestException if the
   * timestamps are not ISO conform.
   */
  protected void checkTemporalExtend(String... timeInfo) throws Exception {
    long start = 0;
    long end = 0;
    long timestampLong = 0;
    start = DateTimeFormatter.ISO_DATE_TIME.parse(ExtractMetadata.fromTstamp + "T00:00:00Z")
        .getLong(ChronoField.INSTANT_SECONDS);
    end = DateTimeFormatter.ISO_DATE_TIME.parse(ExtractMetadata.toTstamp + "Z")
        .getLong(ChronoField.INSTANT_SECONDS);
    for (String timestamp : timeInfo) {
      try {
        ZonedDateTime zdt = ISODateTimeParser.parseISODateTime(timestamp);
        timestampLong =
            DateTimeFormatter.ISO_DATE_TIME.parse(zdt.format(DateTimeFormatter.ISO_DATE_TIME))
                .getLong(ChronoField.INSTANT_SECONDS);
        if (timestampLong < start || timestampLong > end) {
          throw new NotFoundException(
              "The given time parameter is not completely within the timeframe ("
                  + ExtractMetadata.fromTstamp + " to " + ExtractMetadata.toTstamp
                  + ") of the underlying osh-data.");
        }
      } catch (Exception e) {
        if (e instanceof NotFoundException) {
          throw e;
        }
        throw new BadRequestException(ExceptionMessages.timeFormat);
      }
    }
  }

  /**
   * Checks the provided time info on its ISO conformity. Throws a 400 BadRequestException if the
   * timestamps are not ISO conform.
   */
  protected void checkTimestampsOnIsoConformity(String... timeInfo) throws BadRequestException {
    for (String timestamp : timeInfo) {
      try {
        ISODateTimeParser.parseISODateTime(timestamp);
      } catch (Exception e) {
        throw new BadRequestException(ExceptionMessages.timeFormat);
      }
    }
  }

  /**
   * Checks the provided period on its ISO conformity. Throws a 400 BadRequestException if it is not
   * ISO conform.
   */
  protected void checkPeriodOnIsoConformity(String period) throws BadRequestException {
    try {
      ISODateTimeParser.parseISOPeriod(period);
    } catch (Exception e) {
      throw new BadRequestException(
          "The interval (period) of the provided time parameter is not ISO-8601 conform.");
    }
  }

  /** Sorts the given timestamps from oldest to newest. */
  public String[] sortTimestamps(String[] timestamps) throws BadRequestException {
    List<String> timeStringList = new ArrayList<String>();
    for (String timestamp : timestamps) {
      try {
        ZonedDateTime zdt = ISODateTimeParser.parseISODateTime(timestamp);
        checkTemporalExtend(zdt.format(DateTimeFormatter.ISO_DATE_TIME));
        timeStringList.add(zdt.format(DateTimeFormatter.ISO_DATE_TIME));
      } catch (Exception e) {
        throw new BadRequestException(ExceptionMessages.timeFormat);
      }
    }
    Collections.sort(timeStringList);
    return timeStringList.toArray(timestamps);
  }

  /** Checks the given custom boundary id. At the moment only used if output format = csv. */
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

  /** Internal helper method to get the toTimestamps from a timestampList. */
  private String[] getToTimestampsFromTimestamplist(String[] timeData) {
    String[] toTimestamps = new String[timeData.length];
    for (int i = 0; i < timeData.length; i++) {
      try {
        toTimestamps[i] =
            ISODateTimeParser.parseISODateTime(timeData[i]).format(DateTimeFormatter.ISO_DATE_TIME);
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
