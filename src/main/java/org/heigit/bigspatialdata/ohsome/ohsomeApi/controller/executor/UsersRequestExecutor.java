package org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.executor;

import java.util.Map.Entry;
import java.util.SortedMap;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.Application;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.exception.BadRequestException;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.InputProcessor;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.interceptor.RequestInterceptor;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.Attribution;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultUsersResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.metadata.Metadata;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.result.UsersResult;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.object.OSMContribution;
import org.heigit.bigspatialdata.oshdb.util.OSHDBTimestamp;
import org.heigit.bigspatialdata.oshdb.util.time.TimestampFormatter;

public class UsersRequestExecutor {

  private static final String url = Application.getAttributionUrl();
  private static final String text = Application.getAttributionShort();

  /**
   * Performs a count calculation.
   * <p>
   * The parameters are described in the
   * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation.CountController#getCount(String, String, String, String[], String[], String[], String[], String[], String)
   * getCount} method.
   * 
   * @return {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.output.dataAggregationResponse.DefaultAggregationResponse
   *         DefaultAggregationResponse}
   * @throws UnsupportedOperationException by
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer#aggregateByTimestamp()
   *         aggregateByTimestamp()}
   * @throws BadRequestException by
   *         {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.InputProcessor#processParameters(boolean, String, String, String, String[], String[], String[], String[], String[], String)
   *         processParameters()}
   * @throws Exception by
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#count() count()}
   */
  public static DefaultUsersResponse executeCount(RequestParameters rPs)
      throws UnsupportedOperationException, BadRequestException, Exception {

    long startTime = System.currentTimeMillis();
    SortedMap<OSHDBTimestamp, Integer> result;
    MapReducer<OSMContribution> mapRed = null;
    InputProcessor iP = new InputProcessor();
    String requestURL = null;
    if (!rPs.isPost())
      requestURL = RequestInterceptor.requestUrl;
    mapRed = iP.processParameters(mapRed, rPs);
    // db result
    result = mapRed.aggregateByTimestamp().map(contrib -> {
      return contrib.getContributorUserId();
    }).countUniq();
    UsersResult[] resultSet = new UsersResult[result.size()];
    String[] toTimestamps = iP.getUtils().getToTimestamps();
    int count = 0;
    for (Entry<OSHDBTimestamp, Integer> entry : result.entrySet()) {
      resultSet[count] =
          new UsersResult(TimestampFormatter.getInstance().isoDateTime(entry.getKey()),
              toTimestamps[count + 1], entry.getValue().intValue());
      count++;
    }
    Metadata metadata = null;
    long duration = System.currentTimeMillis() - startTime;
    if (iP.getShowMetadata()) {
      metadata = new Metadata(duration, "Number of distinct users per time interval.", requestURL);
    }
    DefaultUsersResponse response = new DefaultUsersResponse(new Attribution(url, text),
        Application.apiVersion, metadata, resultSet);

    return response;
  }

}
