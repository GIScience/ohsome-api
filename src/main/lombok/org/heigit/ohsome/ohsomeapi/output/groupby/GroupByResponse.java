package org.heigit.ohsome.ohsomeapi.output.groupby;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.geojson.Feature;
import org.heigit.ohsome.ohsomeapi.geometrybuilders.BPolygonFromGeoJSON;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.ohsome.ohsomeapi.output.Attribution;
import org.heigit.ohsome.ohsomeapi.output.Metadata;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.GroupByBoundary;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.aggregation.GroupByBoundaryGroupByTag;
import org.heigit.ohsome.ohsomeapi.utils.GroupByBoundaryGeoJsonGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

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
@Configurable
public class GroupByResponse extends Response {

  @Autowired
  private ExtractMetadata extractMetadata;
  @ApiModelProperty(notes = "License and copyright info", required = true)
  @Autowired
  private Attribution attribution;
  @ApiModelProperty(notes = "Version of this api", required = true)
  final private String apiVersion = extractMetadata.getApiVersion();
  @ApiModelProperty(notes = "Metadata describing the output")
  private final Metadata metadata;
  @ApiModelProperty(notes = "Type of the GeoJSON", required = true)
  private String type;
  @ApiModelProperty(notes = "GeoJSON Features", required = true)
  private Feature[] features;
  @ApiModelProperty(notes = "GroupByResult array holding the respective objects "
      + "with their timestamp-value pairs", required = true)
  private final List<GroupByResult> result;
  @Autowired
  private HttpServletRequest servletRequest;
  @Autowired
  private BPolygonFromGeoJSON fromGeoJSONbuilder;

  public GroupByResponse(List result, Operation operation) {
    this.result = result;
    this.metadata = this.generateMetadata(operation.getMetadataDescription(), operation);
    if ("geojson".equalsIgnoreCase(servletRequest.getParameter("format"))
        && ((operation instanceof GroupByBoundary)
        || operation instanceof GroupByBoundaryGroupByTag)) {
      this.type = "FeatureCollection";
      this.features = GroupByBoundaryGeoJsonGenerator.createGeoJsonFeatures(result,
        fromGeoJSONbuilder.getGeoJsonGeoms());
  }
  //    } else if ("csv".equalsIgnoreCase(servletRequest.getParameter("format"))) {
//      return writeCsv(createCsvTopComments(metadata), writeCsvResponse(resultSet));
//    }
  }
}
