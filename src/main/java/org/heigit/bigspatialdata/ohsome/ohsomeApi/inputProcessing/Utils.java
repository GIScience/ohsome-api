package org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Objects;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.exception.NotFoundException;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.oshdb.ExtractMetadata;
import org.heigit.bigspatialdata.oshdb.util.time.ISODateTimeParser;
import org.heigit.bigspatialdata.oshdb.util.time.OSHDBTimestamps;
import org.heigit.bigspatialdata.oshdb.util.time.TimestampFormatter;
import com.vividsolutions.jts.geom.Geometry;

/** Holds additional utility methods. */
public class Utils {

  private String[] boundaryIds;
  private String[] toTimestamps = null;

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
  public String[] splitBoundaryParam(String boundaryParam, BoundaryType boundaryType) {

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
      switch (boundaryType) {
        case BBOXES:
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
          break;
        case BCIRCLES:
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
          break;
        case BPOLYS:
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
          break;
        default: // do nothing as it should never be reached
          break;
      }

    } catch (Exception e) {
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
   * Defines the toTimestamps for the result json object for /users responses.
   */
  public String[] defineToTimestamps(String[] timeData) {

    String[] toTimestamps;
    OSHDBTimestamps timestamps;
    if (timeData.length == 3 && timeData[2] != null) {
      // nasty nested if needed to check for interval
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
        checkIsoConformity(timeSplit[0], "start-timestamp");
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
        checkIsoConformity(timeSplit[1], "end-timestamp");
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
        checkIsoConformity(time, "given timestamp");
        timeVals[0] = time;
      } catch (DateTimeParseException e) {
        throw new BadRequestException("The provided time parameter is not ISO-8601 conform.");
      }
    }
    return timeVals;
  }

  /**
   * Checks the given time-<code>String</code> on its content and if it is ISO-8601 conform.
   * 
   * @param time <code>String</code> containing a start-, end-, or single timestamp from the given
   *        time parameter.
   * @param startEndTstamp <code>String</code> containing either "start", "end", or "timestamp x",
   *        where x refers to the number of the timestamp.
   * @throws Exception
   */
  public void checkIsoConformity(String time, String startEndTstamp) throws Exception {

    try {
      ZonedDateTime zdt = ISODateTimeParser.parseISODateTime(time);
      checkTemporalExtend(zdt.format(DateTimeFormatter.ISO_DATE_TIME));
    } catch (Exception e) {
      throw e;
    }
  }

  /**
   * Checks the provided time info on its temporal extent. Throws a 404 exception if it is not
   * completely within the timerange of the underlying data.
   * 
   * @param timeInfo
   */
  private void checkTemporalExtend(String... timeInfo) throws NotFoundException {

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
      if (timestampLong < start || timestampLong > end)
        throw new NotFoundException(
            "The given time parameter is not completely within the timeframe ("
                + ExtractMetadata.fromTstamp + " to " + ExtractMetadata.toTstamp
                + ") of the underlying osh-data.");
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

    if (ExtractMetadata.dataPoly != null)
      return geom.within(ExtractMetadata.dataPoly);

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
        // time gets checked earlier, so exception should never be thrown
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
