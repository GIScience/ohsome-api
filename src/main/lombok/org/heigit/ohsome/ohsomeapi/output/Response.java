package org.heigit.ohsome.ohsomeapi.output;

import static org.heigit.ohsome.ohsomeapi.utils.GroupByBoundaryGeoJsonGenerator.createGeoJsonFeatures;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.geojson.Feature;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResult;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.utilities.ProcessingRequestTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Interface for all Response classes.
 * <ul>
 * <li>{@link org.heigit.ohsome.ohsomeapi.output.DefaultAggregationResponse
 * DefaultAggregationResponse}</li>
 * <li>{@link org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResponse GroupByResponse}</li>
 * <li>{@link org.heigit.ohsome.ohsomeapi.output.ratio.RatioGroupByBoundaryResponse
 * RatioGroupByBoundaryResponse}</li>
 * <li>{@link org.heigit.ohsome.ohsomeapi.output.ratio.RatioResponse RatioResponse}</li>
 * <li>{@link org.heigit.ohsome.ohsomeapi.output.ExtractionResponse DataResponse}</li>
 * </ul>
 */
@Component
@RequestScope
public abstract class Response {

  @Autowired
  InputProcessor inputProcessor;
  @Autowired
  ProcessingRequestTime processingRequestTime;
  @Autowired
  HttpServletRequest servletRequest;
  private Metadata metadata;
  private GroupByResult[] groupByResult;
  private String type;
  private Feature[] features;

  abstract Attribution getAttribution();

  abstract String getApiVersion();

  abstract Metadata getMetadata();

  public Response getResponse(Operation operation, List<Result> result) {
    if ("geojson".equalsIgnoreCase(servletRequest.getParameter("format"))) {
      getGeoJSONResponse("FeatureCollection",
          createGeoJsonFeatures(groupByResult,
              inputProcessor.getProcessingData().getGeoJsonGeoms()));
    }
  }

  private Response getJSONResponse(Operation operation, GroupByResult[] groupByUserResult) {
    fillResponse(operation);
    this.groupByResult = groupByUserResult;
    return this;
  }

  /** Static factory method returning the whole GeoJSON response.*/
  private Response getGeoJSONResponse(String type, Feature[] features) {
    this.type = type;
    this.features = features;
    return this;
  }

  private void fillResponse(Operation operation){
    String responseDescription = getResponseDescription(operation);
    this.metadata = generateMetadata(responseDescription);
    //        if ("csv".equalsIgnoreCase(servletRequest.getParameter("format"))) {
    //          return writeCsv(createCsvTopComments(metadata), writeCsvResponse(resultSet));
    //        }
  }

  private String getResponseDescription(Operation operation){
    return Description.aggregate(inputProcessor.isDensity(),
        operation.getDescription(), operation.getUnit());
  }

  /**
   * Creates the metadata for the JSON response containing info like execution time, request URL and
   * a short description of the returned data.
   */
  private Metadata generateMetadata(String description) {
    Metadata metadata = null;
    if (inputProcessor.getProcessingData().isShowMetadata()) {
      long duration = System.currentTimeMillis() - processingRequestTime.getSTART_TIME();
      metadata = new Metadata(duration, description,
          inputProcessor.getRequestUrlIfGetRequest());
    }
    return metadata;
  }

}