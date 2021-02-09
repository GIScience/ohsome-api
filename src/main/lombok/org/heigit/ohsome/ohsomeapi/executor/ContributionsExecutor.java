package org.heigit.ohsome.ohsomeapi.executor;

import java.util.Map.Entry;
import java.util.SortedMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.bigspatialdata.oshdb.api.mapreducer.MapReducer;
import org.heigit.bigspatialdata.oshdb.api.object.OSMContribution;
import org.heigit.bigspatialdata.oshdb.util.OSHDBTimestamp;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.ohsome.ohsomeapi.output.Response;

public class ContributionsExecutor extends RequestExecutor{
  
  private final InputProcessor inputProcessor;
  private final ProcessingData processingData;
  private final long startTime = System.currentTimeMillis();

  public ContributionsExecutor(
      HttpServletRequest servletRequest, HttpServletResponse servletResponse, boolean isDensity) {
    super(servletRequest, servletResponse);
    inputProcessor = new InputProcessor(servletRequest, false, isDensity);
    processingData = inputProcessor.getProcessingData();
  }
  
  /**
   * Performs a count calculation.
   * 
   * @return {@link org.heigit.ohsome.ohsomeapi.output.dataaggregationresponse.Response Response}
   * @throws RuntimeException if an unsupported RequestResource type is used. Only COUNT, LENGTH,
   *         PERIMETER, and AREA are permitted here
   * @throws Exception thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor#processParameters()
   *         processParameters},
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#count() count}, or
   *         {@link org.heigit.bigspatialdata.oshdb.api.mapreducer.MapAggregator#sum() sum}
   */
  public Response count() throws Exception {
    final SortedMap<OSHDBTimestamp, ? extends Number> result;
    MapReducer<OSMContribution> mapRed = null;
    mapRed = inputProcessor.processParameters();
    result = mapRed.aggregateByTimestamp().count();
    
    for (Entry<OSHDBTimestamp, ? extends Number> entry : result.entrySet()) {
        System.out.println(entry.getKey() + " " + entry.getValue());
    }
    
    
    throw new RuntimeException();

//    Geometry geom = inputProcessor.getGeometry();
//    RequestParameters requestParameters = processingData.getRequestParameters();
//    ElementsResult[] resultSet =
//        fillElementsResult(result, requestParameters.isDensity(), df, geom);
//    String description = Description.aggregate(requestParameters.isDensity(),
//        requestResource.getDescription(), requestResource.getUnit());
//    Metadata metadata = generateMetadata(description);
//    if ("csv".equalsIgnoreCase(requestParameters.getFormat())) {
//      return writeCsv(createCsvTopComments(metadata), writeCsvResponse(resultSet));
//    }
//    return DefaultAggregationResponse.of(ATTRIBUTION, Application.API_VERSION, metadata, resultSet);
  }

}
