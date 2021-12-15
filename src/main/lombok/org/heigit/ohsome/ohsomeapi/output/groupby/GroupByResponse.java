package org.heigit.ohsome.ohsomeapi.output.groupby;

import static org.heigit.ohsome.ohsomeapi.utils.GroupByBoundaryGeoJsonGenerator.createGeoJsonFeatures;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.geojson.Feature;
import org.heigit.ohsome.ohsomeapi.Application;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.output.Attribution;
import org.heigit.ohsome.ohsomeapi.output.Metadata;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.utilities.ProcessingRequestTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Represents the whole JSON response object for the data aggregation response using the
 * count|length|area|perimeter/groupBy resource. It contains an optional {@link
 * org.heigit.ohsome.ohsomeapi.output.Metadata Metadata}, the requested {@link
 * org.heigit.ohsome.ohsomeapi.output.groupby.GroupByResult GroupByResult} for a JSON response and
 * an identifier of the object. If the output format is GeoJSON, the response includes a {@link
 * org.geojson.Feature Feature} array, which holds the respective objects with their
 * timestamp-value pairs.
 */
@Getter
//@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(Include.NON_NULL)
@Component
public class GroupByResponse implements Response {

  @ApiModelProperty(notes = "License and copyright info", required = true)
  @Autowired
  private Attribution attribution;
  @ApiModelProperty(notes = "Version of this api", required = true)
  final private String apiVersion =Application.API_VERSION;
  @ApiModelProperty(notes = "Metadata describing the output")
  private Metadata metadata;
  @ApiModelProperty(notes = "Type of the GeoJSON", required = true)
  private String type;
  @ApiModelProperty(notes = "GeoJSON Features", required = true)
  private Feature[] features;
  @ApiModelProperty(notes = "GroupByResult array holding the respective objects "
      + "with their timestamp-value pairs", required = true)
  private List<GroupByResult> result;
  @Autowired
  InputProcessor inputProcessor;
  @Autowired
  HttpServletRequest servletRequest;
  @Autowired
  ProcessingRequestTime processingRequestTime;


  private void fillResponse(Operation operation){
    if ("geojson".equalsIgnoreCase(servletRequest.getParameter("format"))) {
      getGeoJSONResponse("FeatureCollection",
          createGeoJsonFeatures(groupByResult,
              inputProcessor.getProcessingData().getGeoJsonGeoms()));
    }//    } else if ("csv".equalsIgnoreCase(servletRequest.getParameter("format"))) {
//      return writeCsv(createCsvTopComments(metadata), writeCsvResponse(resultSet));
//    }
  }
}
