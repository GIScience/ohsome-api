package org.heigit.ohsome.ohsomeapi.utilities;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.exception.NotFoundException;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.ohsome.oshdb.util.time.IsoDateTimeParser;
import org.heigit.ohsome.oshdb.util.time.OSHDBTimestamps;
import org.heigit.ohsome.oshdb.util.time.TimestampFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TimeUtility implements Serializable {

  @Autowired
  private ExtractMetadata extractMetadata;
  private String[] toTimestamps;

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
        toTimestamps = getToTimestampsFromTimestamplist(
            timeData);
      }
    } else {
      // list of timestamps
      toTimestamps = getToTimestampsFromTimestamplist(
          timeData);
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
   * <p>For clarification: the format YYYY-MM-DDThh:mm:ss can be applied to any format, where a
   * timestamp is used and # is a replacement holder for "no value". Note that the positioning and
   * using of the forward slash '/' is very important.
   *
   * @param time <code>String</code> holding the unparsed time information.
   * @return <code>String</code> array containing the startTime at [0], the endTime at [1] and the
   *     period at [2].
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
        timeVals[0] = extractMetadata.getFromTstamp();
        timeVals[1] = extractMetadata.getToTstamp();
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
        timeVals[1] = extractMetadata.getToTstamp();
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
      timeVals[0] = extractMetadata.getFromTstamp();
    } else if (time.endsWith("/")) {
      if (split.length != 1) {
        // invalid time parameter
        throw new BadRequestException(ExceptionMessages.TIME_FORMAT);
      }
      // YYYY-MM-DD/
      checkTimestampsOnIsoConformity(split[0]);
      checkTemporalExtend(split[0]);
      timeVals[0] = split[0];
      timeVals[1] = extractMetadata.getToTstamp();
    } else if (split.length == 3) {
      if (split[1].length() == 0) {
        // YYYY-MM-DD//PnYnMnD
        checkTimestampsOnIsoConformity(split[0]);
        checkTemporalExtend(split[0]);
        timeVals[1] = extractMetadata.getToTstamp();
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
    String[] sortedTimestamps = sortTimestamps(new String[]{timeVals[0], timeVals[1]});
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
    List<String> timeStringList = new ArrayList<String>();
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
   * Checks the provided time info on its temporal extent.
   *
   * @param timeInfo time information to check
   * @throws NotFoundException if the given time is not completely within the timerange of the
   *     underlying data
   * @throws BadRequestException if the timestamps are not ISO-8601 conform
   * @throws RuntimeException if the Date or DateTime Format are not supported
   */
  public void checkTemporalExtend(String... timeInfo) {
    long start = 0;
    long end = 0;
    long timestampLong = 0;
    try {
      start = IsoDateTimeParser.parseIsoDateTime(extractMetadata.getFromTstamp()).toEpochSecond();
      end = IsoDateTimeParser.parseIsoDateTime(extractMetadata.getToTstamp()).toEpochSecond();
    } catch (Exception e) {
      throw new RuntimeException("The ISO 8601 Date or the combined Date-Time String cannot be"
          + " converted into a UTC based ZonedDateTime Object");
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
                  + extractMetadata.getFromTstamp() + " to " + extractMetadata.getToTstamp()
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
  public void checkTimestampsOnIsoConformity(String... timeInfo) {
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
  public void checkPeriodOnIsoConformity(String period) {
    try {
      IsoDateTimeParser.parseIsoPeriod(period);
    } catch (Exception e) {
      throw new BadRequestException(
          "The interval (period) of the provided time parameter is not ISO-8601 conform.");
    }
  }

  /**
   * Internal helper method to get the toTimestamps from a timestampList.
   */
  public String[] getToTimestampsFromTimestamplist(String[] timeData) {
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

  public String[] getToTimestamps() {
    return toTimestamps;
  }

  public void setToTimestamps(String[] toTimestamps) {
    this.toTimestamps = toTimestamps;
  }
}