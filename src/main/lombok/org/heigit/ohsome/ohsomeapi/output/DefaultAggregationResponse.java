package org.heigit.ohsome.ohsomeapi.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.geojson.Feature;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.utilities.MetadataUtility;
import org.heigit.ohsome.ohsomeapi.utilities.StartTimeOfRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Represents the outer JSON response object for the data aggregation requests that do not use the
 * /groupBy resource. It contains attribution info, the version of the api, optional {@link
 * org.heigit.ohsome.ohsomeapi.output.Metadata Metadata} and the {@link
 * org.heigit.ohsome.ohsomeapi.output.elements.ElementsResult ElementsResult} objects.
 */
@Getter
@Setter
@JsonInclude(Include.NON_NULL)
@Configurable
public class DefaultAggregationResponse extends Response {

  @Autowired
  private ExtractMetadata extractMetadata;
  @Autowired
  private MetadataUtility metadataUtility;
  @ApiModelProperty(notes = "License and copyright info", required = true)
  @Autowired
  private Attribution attribution;
  @ApiModelProperty(notes = "Version of this api", required = true)
  @Autowired
  @Qualifier("ohsomeAPIVersion")
  private String apiVersion;
  @ApiModelProperty(notes = "Metadata describing the output")
  private Metadata metadata;
  @ApiModelProperty(notes = "Type of the GeoJSON", required = true)
  private String type;
  @ApiModelProperty(notes = "GeoJSON Features", required = true)
  private Feature[] features;
  @ApiModelProperty(notes = "ElementsResult holding timestamp-value pairs", required = true)
  private List<Result> result;
  @Autowired
  private StartTimeOfRequest startTimeOfRequest;
  @Autowired
  private InputProcessor inputProcessor;

  public DefaultAggregationResponse(List<Result> result, Operation operation) {
    this.result = result;
    this.metadata = this.generateMetadata(operation.getMetadataDescription(), operation);
  }
}

//    if ("csv".equalsIgnoreCase(servletRequest.getParameter("format"))) {
//      ExecutionUtils exeUtils = new ExecutionUtils(processingData);
//      exeUtils.writeCsvResponse(resultSet, servletResponse,
//          ExecutionUtils.createCsvTopComments(attribution.getUrl(), attribution.getText(), Application.API_VERSION, metadata));
//      return null;
//    }
//    return new GroupByResponse(attribution, Application.API_VERSION, metadata,
//        resultSet);