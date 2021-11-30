//package org.heigit.ohsome.ohsomeapi.refactoring;
//
//import java.util.List;
//import org.heigit.ohsome.ohsomeapi.Application;
//import org.heigit.ohsome.ohsomeapi.executor.ExecutionUtils;
//import org.heigit.ohsome.ohsomeapi.executor.RequestParameters;
//import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
//import org.heigit.ohsome.ohsomeapi.output.Attribution;
//import org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse;
//import org.heigit.ohsome.ohsomeapi.output.Metadata;
//import org.heigit.ohsome.ohsomeapi.output.Response;
//import org.heigit.ohsome.ohsomeapi.output.contributions.ContributionsResult;
//import org.locationtech.jts.geom.Geometry;
//
//public class Contributions {
//  private List<Operation> operationList;
//  private Operator operator;
//  private final long startTime = System.currentTimeMillis();
//
//  public void addOperation(Operation operation){
//    this.operationList.add(operation);
//  }
//
//  public Response aggregate(Mapper mapper) throws Exception {
//    operator.setListOperations(operationList);
//   this.operator.compute();
//    InputProcessor inputProcessor = new InputProcessor();
//    Geometry geom = inputProcessor.getGeometry();
//    RequestParameters requestParameters = processingData.getRequestParameters();
//    ContributionsResult[] results = ExecutionUtils.fillContributionsResult(result,
//        requestParameters.isDensity(), inputProcessor, df, geom);
//    ParameterUtility parameterUtility = new ParameterUtility();
//    Metadata metadata = parameterUtility.getMetadata(startTime);
//    if ("csv".equalsIgnoreCase(requestParameters.getFormat())) {
//      var exeUtils = new ExecutionUtils(processingData);
//      exeUtils.writeCsvResponse(results, servletResponse,
//          ExecutionUtils.createCsvTopComments(URL, TEXT, Application.API_VERSION, metadata));
//      return null;
//    }
//    return DefaultAggregationResponse.of(new Attribution(URL, TEXT), Application.API_VERSION,
//        metadata, results);
//  }
//
//
//}
