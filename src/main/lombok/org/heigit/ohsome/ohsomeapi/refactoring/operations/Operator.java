package org.heigit.ohsome.ohsomeapi.refactoring.operations;

import lombok.Getter;
import lombok.Setter;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.output.Attribution;
import org.heigit.ohsome.ohsomeapi.output.Metadata;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class Operator {
  @Autowired
  InputProcessor inputProcessor;
  @Getter
  @Setter
  private Operation operation;
  private final long startTime = System.currentTimeMillis();

  public Response compute() throws Exception {
//    Object ob;
//    for (Operation op : listOperations) {
//      ob = op.compute();
//    }
    return new Response() {
      @Override
      public Attribution getAttribution() {
        return null;
      }

      @Override
      public String getApiVersion() {
        return null;
      }

      @Override
      public Metadata getMetadata() {
        return null;
      }
    };
  }

  public void setOperation(Operation operation) {
    this.operation = operation;
  }

  //  public Response getResponse(Operation operation) throws Exception {
//    this.operation = operation;
//    Response response = fillResponse(operation.compute());
//    return response;
//  }

//  private Response fillResponse(Result[] resultSet) {
//    String description = Description.aggregate(inputProcessor.isDensity(),
//        operation.getDescription(), operation.getUnit());
//    Metadata metadata = generateMetadata(description);
////    if ("csv".equalsIgnoreCase(servletRequest.getParameter("format"))) {
////      return writeCsv(createCsvTopComments(metadata), writeCsvResponse(resultSet));
////    }
//    return DefaultAggregationResponse.getGeoJSONResponse(metadata, resultSet);
//  }

  /**
   * Creates the metadata for the JSON response containing info like execution time, request URL and
   * a short description of the returned data.
   */
  private Metadata generateMetadata(String description) {
    Metadata metadata = null;
    if (inputProcessor.getProcessingData().isShowMetadata()) {
      long duration = System.currentTimeMillis() - startTime;
      metadata = new Metadata(duration, description,
          inputProcessor.getRequestUrlIfGetRequest());
    }
    return metadata;
  }
}
