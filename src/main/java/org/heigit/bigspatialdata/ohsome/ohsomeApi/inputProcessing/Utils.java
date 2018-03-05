package org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Objects;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.Application;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.exception.NotFoundException;
import org.heigit.bigspatialdata.oshdb.util.geometry.fip.FastPolygonOperations;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Holds additional utility methods needed for classes
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.GeometryBuilder
 * GeometryBuilder} and
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.InputProcessor InputProcessor}.
 */
public class Utils {

  public static String defStartTime = "2007-01-01";
  public static String defEndTime = "2018-03-01";
  private String[] boundaryIds;
  private FastPolygonOperations dataPolyOps = new FastPolygonOperations(Application.getDataPoly());

  /**
   * Finds and returns the EPSG code of the given point, which is needed for
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.GeometryBuilder#createCircularPolygons(String[] bcircles)
   * createCircularPolygons}.
   * <p>
   * Adapted code from UTMCodeFromLonLat.java class in the osmatrix project (© by Michael Auer)
   * 
   * @param lon Longitude coordinate of the point.
   * @param lat Latitude coordinate of the point.
   * @return <code>String</code> representing the corresponding EPSG code.
   */
  public String findEPSG(double lon, double lat) {

    if (lat >= 84)
      return "EPSG:32661"; // UPS North
    if (lat < -80)
      return "EPSG:32761"; // UPS South

    int zoneNumber = (int) (Math.floor((lon + 180) / 6) + 1);
    if (lat >= 56.0 && lat < 64.0 && lon >= 3.0 && lon < 12.0)
      zoneNumber = 32;
    // Special zones for Svalbard
    if (lat >= 72.0 && lat < 84.0) {
      if (lon >= 0.0 && lon < 9.0)
        zoneNumber = 31;
      else if (lon >= 9.0 && lon < 21.0)
        zoneNumber = 33;
      else if (lon >= 21.0 && lon < 33.0)
        zoneNumber = 35;
      else if (lon >= 33.0 && lon < 42.0)
        zoneNumber = 37;
    }
    String isNorth = (lat > 0) ? "6" : "7";
    String zone = (zoneNumber < 10) ? "0" + zoneNumber : "" + zoneNumber;
    return "EPSG:32" + isNorth + zone;
  }

  /**
   * Splits the given boundary parameter (bboxes, bcircles, or bpolys) two times. The first split is
   * on '|' and to seperate the bounding objects; The second is on ':' to seperate the custom ids
   * from each first coordinate; Returns the coordinates after the second split (and the radius in
   * case of bounding points).
   * 
   * @param boundaryParam <code>String</code> containing the given boundary parameter.
   * @param boundaryType <code>Byte</code> containing the value 1 (bboxes), 2 (bcircles) or 3
   *        (bpolys).
   * @return <code>String</code> array holding only coordinates (plus the radius in case of bounding
   *         points).
   */
  public String[] splitBoundaryParam(String boundaryParam, byte boundaryType) {

    String[] boundaryObjects;
    String[] boundaryParamValues = null;
    String[] boundaryIds = null;
    String[] coords;
    // to check if there is more than one boundary object given
    if (boundaryParam.contains("|"))
      boundaryObjects = boundaryParam.split("\\|");
    else
      boundaryObjects = new String[] {boundaryParam};

    boundaryIds = new String[boundaryObjects.length];
    int idCount = 0;
    int paramCount = 0;

    try {
      if (boundaryType == 1) {
        if (boundaryObjects[0].contains(":")) {
          // custom ids are given
          boundaryParamValues = new String[boundaryObjects.length * 4];
          for (String bObject : boundaryObjects) {
            coords = bObject.split("\\,");
            if (coords[0].contains(":")) {
              String[] idAndCoordinate = coords[0].split(":");
              // extract the id
              boundaryIds[idCount] = idAndCoordinate[0];
              // extract the coordinates
              boundaryParamValues[paramCount] = idAndCoordinate[1];
              boundaryParamValues[paramCount + 1] = coords[1];
              boundaryParamValues[paramCount + 2] = coords[2];
              boundaryParamValues[paramCount + 3] = coords[3];
              idCount++;
              paramCount += 4;
            } else {
              throw new BadRequestException(
                  "One or more boundary object(s) have a custom id (or at least a colon), whereas other(s) don't. "
                      + "You can either set custom ids for all your boundary objects, or for none.");
            }
          }
        } else {
          // no custom ids are given
          boundaryParamValues = new String[boundaryObjects.length * 4];
          idCount = 1;
          for (String bObject : boundaryObjects) {
            coords = bObject.split("\\,");
            for (String coord : coords) {
              boundaryParamValues[paramCount] = coord;
              paramCount++;
            }
            // adding of ids
            boundaryIds[idCount - 1] = "bbox" + String.valueOf(idCount);
            idCount++;
          }
        }
      } else if (boundaryType == 2) {
        // bcircles given
        if (boundaryObjects[0].contains(":")) {
          // custom ids are given
          boundaryParamValues = new String[boundaryObjects.length * 3];
          for (String bObject : boundaryObjects) {
            coords = bObject.split("\\,");
            if (coords[0].contains(":")) {
              String[] idAndCoordinate = coords[0].split(":");
              // extract the id
              boundaryIds[idCount] = idAndCoordinate[0];
              // extract the coordinate
              boundaryParamValues[paramCount] = idAndCoordinate[1];
              boundaryParamValues[paramCount + 1] = coords[1];
              // extract the radius
              boundaryParamValues[paramCount + 2] = coords[2];
              idCount++;
              paramCount += 3;
            } else {
              throw new BadRequestException(
                  "One or more boundary object(s) have a custom id (or at least a colon), whereas other(s) don't. "
                      + "You can either set custom ids for all your boundary objects, or for none.");
            }
          }
        } else {
          // no custom ids are given
          boundaryParamValues = new String[boundaryObjects.length * 3];
          idCount = 1;
          for (String bObject : boundaryObjects) {
            coords = bObject.split("\\,");
            // walks through the coordinates + radius
            for (String coord : coords) {
              boundaryParamValues[paramCount] = coord;
              paramCount++;
            }
            // adding of ids
            boundaryIds[idCount - 1] = "bcircle" + String.valueOf(idCount);
            idCount++;
          }
        }
      } else {
        // bpolys given
        if (boundaryObjects[0].contains(":")) {
          // custom ids are given
          boundaryParamValues = new String[boundaryParam.length()];
          for (String bObject : boundaryObjects) {
            coords = bObject.split("\\,");
            if (coords[0].contains(":")) {
              String[] idAndCoordinate = coords[0].split(":");
              // extract the id and the first coordinate
              boundaryIds[idCount] = idAndCoordinate[0];
              boundaryParamValues[paramCount] = idAndCoordinate[1];
              paramCount++;
              // extract the other coordinates
              for (int i = 1; i < coords.length; i++) {
                boundaryParamValues[paramCount] = coords[i];
                paramCount++;
              }
              idCount++;
            } else {
              throw new BadRequestException(
                  "One or more boundary object(s) have a custom id (or at least a colon), whereas other(s) don't. "
                      + "You can either set custom ids for all your boundary objects, or for none.");
            }
          }
        } else {
          // no custom ids are given
          boundaryParamValues = new String[boundaryParam.length()];
          idCount = 1;
          for (String bObject : boundaryObjects) {
            coords = bObject.split("\\,");
            // walks through the coordinates
            for (String coord : coords) {
              boundaryParamValues[paramCount] = coord;
              paramCount++;
            }
            // adding of ids
            boundaryIds[idCount - 1] = "bpoly" + String.valueOf(idCount);
            idCount++;
          }
        }
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new BadRequestException(
          "The processing of the boundary parameter gave an error. Please use the predefined format "
              + "where you delimit different objects with the pipe-sign '|' "
              + "and optionally add custom ids with the colon ':' at the first coordinate of each object.");
    }
    this.boundaryIds = boundaryIds;
    boundaryParamValues =
        Arrays.stream(boundaryParamValues).filter(Objects::nonNull).toArray(String[]::new);
    return boundaryParamValues;
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
   * <p>
   * For clarification: the format YYYY-MM-DDThh:mm:ss can be applied to any format, where a
   * timestamp is used and # is a replacement holder for "no value". Note that the positioning and
   * using of the forward slash '/' is very important.
   * 
   * @param time <code>String</code> holding the unparsed time information.
   * @return <code>String</code> array containing the startTime at at [0], the endTime at [1] and
   *         the period at [2].
   * @throws BadRequestException if the provided time parameter does not fit to any specified format
   */
  public String[] extractIsoTime(String time) throws BadRequestException {
    // retrieve earliest/latest timestamp from metadata (if available)
    if (Application.getMetadata() != null) {
      defStartTime = Application.getMetadata().get(0);
      defEndTime = Application.getMetadata().get(1);
    }
    String[] timeVals = new String[3];
    if (time.contains("/")) {
      if (time.length() == 1) {
        // only "/" is given
        timeVals[0] = defStartTime;
        timeVals[1] = defEndTime;
        return timeVals;
      }
      String[] timeSplit = time.split("/");
      if (timeSplit[0].length() > 0) {
        // start timestamp
        checkIsoConformity(timeSplit[0], "start-timestamp");
        timeVals[0] = timeSplit[0];
        if (time.endsWith("/") && (timeSplit.length < 2 || timeSplit[1].length() == 0)) {
          // latest timestamp
          timeVals[1] = defEndTime;
          return timeVals;
        }
      } else {
        // earliest timestamp
        timeVals[0] = defStartTime;
      }
      if (timeSplit[1].length() > 0) {
        // end timestamp
        checkIsoConformity(timeSplit[1], "end-timestamp");
        timeVals[1] = timeSplit[1];
      } else {
        // latest timestamp
        timeVals[1] = defEndTime;
      }
      if (timeSplit.length == 3 && timeSplit[2].length() > 0) {
        // interval
        try {
          Period.parse(timeSplit[2]);
          timeVals[2] = timeSplit[2];
        } catch (DateTimeParseException e) {
          throw new BadRequestException(
              "The interval (period) of the provided time parameter is not ISO-8601 conform.");
        }
      }
    } else {
      // just one timestamp
      try {
        checkIsoConformity(time, "given timestamp");
        timeVals[0] = time;
        timeVals[1] = time;
        timeVals[2] = "P1Y";
      } catch (DateTimeParseException e) {
        throw new BadRequestException("The provided time parameter is not ISO-8601 conform.");
      }
    }
    return timeVals;
  }

  /**
   * Checks the given time-<code>String</code> on its content and if it is ISO-8601 conform.
   * 
   * @param time <code>String</code> containing the start or end time from the given time parameter.
   * @param startEnd <code>String</code> containing either "start" or "end" depending on the given
   *        timestamp.
   * @throws BadRequestException if the given time-String is not ISO-8601 conform
   */
  public void checkIsoConformity(String time, String startEndTstamp) {

    try {
      // YYYY
      if (time.length() == 4) {
        time = time + "-01-01";
        LocalDate.parse(time);
      }
      // YYYY-MM
      else if (time.length() == 7) {
        time = time + "-01";
        LocalDate.parse(time);
      }
      // YYYY-MM-DD
      else if (time.length() == 10) {
        LocalDate.parse(time);
      }
      // YYYY-MM-DDThh
      else if (time.length() == 13) {
        time = time + ":00:00";
        LocalDateTime.parse(time);
      }
      // YYYY-MM-DDThh:mm
      else if (time.length() == 16) {
        time = time + ":00";
        LocalDateTime.parse(time);
      }
      // YYYY-MM-DDThh:mm:ss
      else if (time.length() == 19) {
        LocalDateTime.parse(time);
      } else {
        throw new BadRequestException(
            "The " + startEndTstamp + " of the provided time parameter is not ISO-8601 conform.");
      }

      checkTemporalExtend(time);
    } catch (DateTimeParseException e) {
      throw new BadRequestException(
          "The " + startEndTstamp + " of the provided time parameter is not ISO-8601 conform.");
    }
  }

  /**
   * Checks the provided time info on its temporal extent. Throws a 404 exception if it is not
   * completely within the timerange of the underlying data.
   * 
   * @param timeInfo
   */
  private void checkTemporalExtend(String... timeInfo) throws NotFoundException {

    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    long start = 0;
    long end = 0;
    long timestampLong;
    if (Application.getMetadata() == null) {
      start = parseTimestamp(sdf1, defStartTime);
      end = parseTimestamp(sdf1, defEndTime);
    } else {
      start = parseTimestamp(sdf2, defStartTime);
      end = parseTimestamp(sdf2, defEndTime);
    }

    for (String timestamp : timeInfo) {
      timestampLong = parseTimestamp(sdf2, timestamp);
      if (timestampLong == 0)
        timestampLong = parseTimestamp(sdf1, timestamp);
      if (timestampLong < start || timestampLong > end)
        throw new NotFoundException(
            "The given time parameter is not completely within the timeframe (" + defStartTime
                + " to " + defEndTime + ") of the underlying osh-data.");
    }
  }

  /**
   * Parses the given timestamp-<code>String</code> into a <code>long</code> using the given
   * <code>SimpleDateFormat</code>.
   * 
   * @param format
   * @param timestamp
   * @return parsed <code>long</code> value (0 in case of not able to parse).
   */
  private long parseTimestamp(SimpleDateFormat format, String timestamp) {

    try {
      return format.parse(timestamp).getTime();
    } catch (ParseException e) {
      return 0;
    }
  }

  /**
   * Clips the given boundary parameter through the underlying polygon of the data-file.
   * 
   * @param geom
   * @return <code>Geometry</code> which represents the intersection between the provided boundary
   *         and the data-boundary.
   */
  public Geometry clipBoundary(Geometry geom) {

    if (!geom.intersects(Application.getDataPoly()))
      return null;

    Geometry intersection = dataPolyOps.intersection(geom);
    return intersection;
  }


  public String[] getBoundaryIds() {
    return boundaryIds;
  }

}
