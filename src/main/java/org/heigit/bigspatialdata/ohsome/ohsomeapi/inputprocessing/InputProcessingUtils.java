package org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.NotFoundException;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.bigspatialdata.oshdb.util.time.ISODateTimeParser;
import org.heigit.bigspatialdata.oshdb.util.time.OSHDBTimestamps;
import org.heigit.bigspatialdata.oshdb.util.time.TimestampFormatter;
import com.vividsolutions.jts.geom.Geometry;

/** Holds utility methods that are used by the input processing and executor classes. */
public class InputProcessingUtils {

  private String[] boundaryIds;
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
    } else {
      return new String[] {boundaryParam};
    }
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
            throw new BadRequestException("Error in processing the boundary parameter. Please "
                + "remember to follow the format, where you separate each boundary object with a "
                + "pipe-sign '|' and add optional custom ids to every first coordinate with a "
                + "colon ':'.");
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
            throw new BadRequestException(
                "One or more boundary object(s) have a custom id (or at least a colon), "
                    + "whereas other(s) don't. You can either set custom ids for all your "
                    + "boundary objects, or for none.");
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
      } else {
        throw new BadRequestException("The processing of the boundary parameter gave an error. "
            + "Please use the predefined format where you delimit different objects "
            + "with the pipe-sign '|' and optionally add custom ids with the colon ':' "
            + "at the first coordinate of each boundary object.");
      }
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
            throw new BadRequestException("Error in processing the boundary parameter. Please "
                + "remember to follow the format, where you separate each boundary object with a "
                + "pipe-sign '|' and add optional custom ids to every first coordinate with a "
                + "colon ':'.");
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
            throw new BadRequestException(
                "One or more boundary object(s) have a custom id (or at least a colon), "
                    + "whereas other(s) don't. You can either set custom ids for all your "
                    + "boundary objects, or for none.");
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
      } else {
        throw new BadRequestException("The processing of the boundary parameter gave an error. "
            + "Please use the predefined format where you delimit different objects "
            + "with the pipe-sign '|' and optionally add custom ids with the colon ':' "
            + "at the first coordinate of each boundary object.");
      }
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
                throw new BadRequestException("Error in processing the boundary parameter. Please "
                    + "remember to follow the format, where you separate each boundary object "
                    + "with a pipe-sign '|' and add optional custom ids to every first coordinate "
                    + "with a colon ':'.");
              }
              boundaryParamValues.add(coords[i]);
            }
            idCount++;
          } else {
            throw new BadRequestException(
                "One or more boundary object(s) have a custom id (or at least a colon), "
                    + "whereas other(s) don't. You can either set custom ids for all your "
                    + "boundary objects, or for none.");
          }
        }
      } else {
        idCount = 1;
        for (String boundaryObject : bpolysArray) {
          coords = boundaryObject.split("\\,");
          for (String coord : coords) {
            boundaryParamValues.add(coord);
          }
          boundaryIds[idCount - 1] = "bpoly" + String.valueOf(idCount);
          idCount++;
        }
      }
    } catch (Exception e) {
      if (e.getClass() == BadRequestException.class) {
        throw e;
      } else {
        throw new BadRequestException("The processing of the boundary parameter gave an error. "
            + "Please use the predefined format where you delimit different objects "
            + "with the pipe-sign '|' and optionally add custom ids with the colon ':' "
            + "at the first coordinate of each boundary object.");
      }
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
      // nasty nested 'if' needed to check for interval
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
    if (time.contains("/")) {
      if (time.length() == 1) {
        // only "/" is given
        timeVals[0] = ExtractMetadata.fromTstamp;
        timeVals[1] = ExtractMetadata.toTstamp;
        return timeVals;
      }
      String[] timeSplit = time.split("/");
      if (timeSplit[0].length() > 0) {
        // start timestamp
        ZonedDateTime zdt = ISODateTimeParser.parseISODateTime(timeSplit[0]);
        checkTemporalExtend(zdt.format(DateTimeFormatter.ISO_DATE_TIME));
        timeVals[0] = timeSplit[0];
        if (time.endsWith("/") && (timeSplit.length < 2 || timeSplit[1].length() == 0)) {
          // latest timestamp
          timeVals[1] = ExtractMetadata.toTstamp;
          return timeVals;
        }
      } else {
        // earliest timestamp
        timeVals[0] = ExtractMetadata.fromTstamp;
      }
      if (timeSplit[1].length() > 0) {
        // end timestamp
        ZonedDateTime zdt = ISODateTimeParser.parseISODateTime(timeSplit[1]);
        checkTemporalExtend(zdt.format(DateTimeFormatter.ISO_DATE_TIME));
        timeVals[1] = timeSplit[1];
      } else {
        // latest timestamp
        timeVals[1] = ExtractMetadata.toTstamp;
      }
      if (timeSplit.length == 3 && timeSplit[2].length() > 0) {
        // interval
        try {
          ISODateTimeParser.parseISOPeriod(timeSplit[2]);
          timeVals[2] = timeSplit[2];
        } catch (Exception e) {
          throw new BadRequestException(
              "The interval (period) of the provided time parameter is not ISO-8601 conform.");
        }
      }
    } else {
      // just one timestamp
      try {
        ZonedDateTime zdt = ISODateTimeParser.parseISODateTime(time);
        checkTemporalExtend(zdt.format(DateTimeFormatter.ISO_DATE_TIME));
        timeVals[0] = time;
      } catch (DateTimeParseException e) {
        throw new BadRequestException("The provided time parameter is not ISO-8601 conform.");
      }
    }
    return timeVals;
  }

  /**
   * Checks the provided time info on its temporal extent. Throws a 404 exception if it is not
   * completely within the timerange of the underlying data.
   * 
   */
  public void checkTemporalExtend(String... timeInfo) throws NotFoundException {
    long start = 0;
    long end = 0;
    long timestampLong = 0;
    start = DateTimeFormatter.ISO_DATE_TIME.parse(ExtractMetadata.fromTstamp + "T00:00:00Z")
        .getLong(ChronoField.INSTANT_SECONDS);
    end = DateTimeFormatter.ISO_DATE_TIME.parse(ExtractMetadata.toTstamp + "Z")
        .getLong(ChronoField.INSTANT_SECONDS);
    for (String timestamp : timeInfo) {
      timestampLong =
          DateTimeFormatter.ISO_DATE_TIME.parse(timestamp).getLong(ChronoField.INSTANT_SECONDS);
      if (timestampLong < start || timestampLong > end) {
        throw new NotFoundException(
            "The given time parameter is not completely within the timeframe ("
                + ExtractMetadata.fromTstamp + " to " + ExtractMetadata.toTstamp
                + ") of the underlying osh-data.");
      }
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

  public String[] getBoundaryIds() {
    return boundaryIds;
  }

  public String[] getToTimestamps() {
    return toTimestamps;
  }

  public void setBoundaryIds(String[] boundaryIds) {
    this.boundaryIds = boundaryIds;
  }

  public void setToTimestamps(String[] toTimestamps) {
    this.toTimestamps = toTimestamps;
  }
}
