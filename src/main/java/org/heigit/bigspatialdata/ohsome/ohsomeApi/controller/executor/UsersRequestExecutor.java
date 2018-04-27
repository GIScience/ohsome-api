package org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.executor;

import java.text.DecimalFormat;
import java.util.Map.Entry;
import java.util.SortedMap;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.Application;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.GeometryBuilder;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.InputProcessor;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.interceptor.RequestInterceptor;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Attribution;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Metadata;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.users.UsersResult;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.object.OSMContribution;
import org.heigit.bigspatialdata.oshdb.util.OSHDBTimestamp;
import org.heigit.bigspatialdata.oshdb.util.geometry.Geo;
import org.heigit.bigspatialdata.oshdb.util.time.TimestampFormatter;
import com.vividsolutions.jts.geom.Geometry;

/** Includes the execute methods for requests mapped to /users. */
public class UsersRequestExecutor {

  private static final String url = Application.getAttributionUrl();
  private static final String text = Application.getAttributionShort();

  /**
   * Performs a count calculation.
   * <p>
   * The other parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   */
  public static DefaultAggregationResponse executeCount(RequestParameters rPs)
      throws UnsupportedOperationException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Integer> result;
    MapReducer<OSMContribution> mapRed = null;
    InputProcessor iP = new InputProcessor();
    ExecutionUtils exeUtils = new ExecutionUtils();
    String description = null;
    String requestURL = null;
    DecimalFormat df = exeUtils.defineDecimalFormat("#.##");
    if (!rPs.isPost())
      requestURL = RequestInterceptor.requestUrl;
    mapRed = iP.processParameters(mapRed, rPs);
    // db result
    result = mapRed.aggregateByTimestamp().map(contrib -> {
      return contrib.getContributorUserId();
    }).countUniq();
    UsersResult[] resultSet = new UsersResult[result.size()];
    String[] toTimestamps = iP.getUtils().getToTimestamps();
    GeometryBuilder geomBuilder = iP.getGeomBuilder();
    Geometry geom = exeUtils.getGeometry(iP.getBoundaryType(), geomBuilder);
    int count = 0;
    for (Entry<OSHDBTimestamp, Integer> entry : result.entrySet()) {
      if (rPs.isDensity()) {
        resultSet[count] =
            new UsersResult(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
                toTimestamps[count + 1], Double.parseDouble(
                    df.format((entry.getValue().doubleValue() / (Geo.areaOf(geom) * 0.000001)))));
      } else {
        resultSet[count] =
            new UsersResult(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
                toTimestamps[count + 1], entry.getValue().intValue());
      }
      count++;
    }
    if (rPs.isDensity()) {
      description =
          "Density of distinct users per time interval (number of users per square-kilometer).";
    } else {
      description = "Number of distinct users per time interval.";
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      metadata = new Metadata(duration, description, requestURL);
    }
    DefaultAggregationResponse response = new DefaultAggregationResponse(new Attribution(url, text),
        Application.apiVersion, metadata, resultSet);
    return response;
  }

}
