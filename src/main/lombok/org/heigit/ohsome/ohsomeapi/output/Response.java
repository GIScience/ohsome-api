package org.heigit.ohsome.ohsomeapi.output;

import javax.servlet.http.HttpServletRequest;
import org.geojson.Feature;
import org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResult;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.utilities.StartTimeOfRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

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
@Configurable
public abstract class Response {

  @Autowired
  private StartTimeOfRequest startTimeOfRequest;
  @Autowired
  private HttpServletRequest servletRequest;
  private Metadata metadata;
  private GroupByResult[] groupByResult;
  private String type;
  private Feature[] features;

//  public Response getResponse(Operation operation, List<Result> result) {
//    if ("geojson".equalsIgnoreCase(servletRequest.getParameter("format"))) {
//      getGeoJSONResponse("FeatureCollection",
//          createGeoJsonFeatures(groupByResult,
//              inputProcessor.getProcessingData().getGeoJsonGeoms()));
//    }
//  }

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
    this.metadata = generateMetadata(responseDescription, operation);
    //        if ("csv".equalsIgnoreCase(servletRequest.getParameter("format"))) {
    //          return writeCsv(createCsvTopComments(metadata), writeCsvResponse(resultSet));
    //        }
  }

  private String getResponseDescription(Operation operation){
    return Description.aggregate(operation.getInputProcessor().isDensity(),
        operation.getDescription(), operation.getUnit());
  }

  /**
   * Creates the metadata for the JSON response containing info like execution time, request URL and
   * a short description of the returned data.
   */
  public Metadata generateMetadata(String description, Operation operation) {
    Metadata metadata = null;
    if (operation.getInputProcessor().getProcessingData().isShowMetadata()) {
      long duration = System.currentTimeMillis() - startTimeOfRequest.getSTART_TIME();
      metadata = new Metadata(duration, description,
          operation.getInputProcessor().getRequestUrlIfGetRequest());
    }
    return metadata;
  }
}