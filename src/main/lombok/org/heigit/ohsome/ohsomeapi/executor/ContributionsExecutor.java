package org.heigit.ohsome.ohsomeapi.executor;

import java.util.SortedMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.object.OSMContribution;
import org.heigit.bigspatialdata.oshdb.util.OSHDBTimestamp;
import org.heigit.ohsome.ohsomeapi.Application;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.ohsome.ohsomeapi.output.Attribution;
import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
import org.heigit.ohsome.ohsomeapi.output.Description;
import org.heigit.ohsome.ohsomeapi.output.Metadata;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.users.ContributionsResult;
import org.locationtech.jts.geom.Geometry;

public class ContributionsExecutor extends RequestExecutor{
  
  private final InputProcessor inputProcessor;
  private final ProcessingData processingData;
  private final long startTime = System.currentTimeMillis();

  public ContributionsExecutor(HttpServletRequest servletRequest,
      HttpServletResponse servletResponse, boolean isDensity) {
    super(servletRequest, servletResponse);
    inputProcessor = new InputProcessor(servletRequest, false, isDensity);
    processingData = inputProcessor.getProcessingData();
  }
  
  /**
   * Performs a count calculation.
   * 
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response}
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters},
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#count() count}
   */
  public Response count() throws Exception {
    final SortedMap<OSHDBTimestamp, ? extends Number> result;
    MapReducer<OSMContribution> mapRed = null;
    mapRed = inputProcessor.processParameters();
    result = mapRed.aggregateByTimestamp().count();
    ExecutionUtils exeUtils = new ExecutionUtils(processingData);
    Geometry geom = inputProcessor.getGeometry();
    RequestParameters requestParameters = processingData.getRequestParameters();
    ContributionsResult[] results = exeUtils.fillContributionsResult(result,
        requestParameters.isDensity(), inputProcessor, df, geom);
    Metadata metadata = null;
    if (processingData.isShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata =
          new Metadata(duration, Description.countContributions(requestParameters.isDensity()),
              inputProcessor.getRequestUrlIfGetRequest(servletRequest));
    }
    if ("csv".equalsIgnoreCase(requestParameters.getFormat())) {
      exeUtils.writeCsvResponse(results, servletResponse,
          exeUtils.createCsvTopComments(URL, TEXT, Application.API_VERSION, metadata));
      return null;
    }
    return DefaultAggregationResponse.of(new Attribution(URL, TEXT), Application.API_VERSION,
        metadata, results);
  }

}
