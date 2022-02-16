package org.heigit.ohsome.ohsomeapi.output.ratio;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.heigit.ohsome.ohsomeapi.geometrybuilders.BPolygonFromGeoJSON;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.ohsome.ohsomeapi.output.Attribution;
import org.heigit.ohsome.ohsomeapi.output.Metadata;
import org.heigit.ohsome.ohsomeapi.output.Response;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.Operation;
import org.heigit.ohsome.ohsomeapi.utilities.GroupByBoundaryGeoJsonGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Represents the whole JSON response object for the data aggregation response using the
 * /ratio/groupBy/boundary resource. It contains an optional {@link
 * org.heigit.ohsome.ohsomeapi.output.Metadata Metadata} object, the requested {@link
 * org.heigit.ohsome.ohsomeapi.output.ratio.RatioGroupByResult RatioGroupByResult} for a JSON
 * response and an identifier of the object. If the output format is GeoJSON, the response includes
 * a {@link org.geojson.Feature Feature} array, which holds the respective objects with their
 * timestamp-value pairs.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(Include.NON_NULL)
@Configurable
public class RatioGroupByBoundaryResponse extends Response {

  @Autowired
  private ExtractMetadata extractMetadata;
  @ApiModelProperty(notes = "License and copyright info", required = true)
  @Autowired
  private Attribution attribution;
  @ApiModelProperty(notes = "Version of this api", required = true)
  private final String apiVersion = extractMetadata.getApiVersion();
  @ApiModelProperty(notes = "Metadata describing the output")
  private Metadata metadata;
  @ApiModelProperty(notes = "Type of the GeoJSON", required = true)
  private String type;
  @ApiModelProperty(notes = "GeoJSON Features", required = true)
  private Feature[] features;
  @ApiModelProperty(notes = "RatioGroupByResult array holding the respective objects "
      + "with their timestamp-value-value2-ratio values", required = true)
  private List<RatioGroupByResult> groupByBoundaryResult;
  @Autowired
  private HttpServletRequest servletRequest;
  @Autowired
  private BPolygonFromGeoJSON fromGeoJSONbuilder;

  public RatioGroupByBoundaryResponse(List<RatioGroupByResult> groupByBoundaryResult, Operation operation) {
    this.metadata = this.generateMetadata(operation.getMetadataDescription(), operation);
    this.groupByBoundaryResult = groupByBoundaryResult;
    if ("geojson".equalsIgnoreCase(servletRequest.getParameter("format"))) {
      GeoJsonObject[] geoJsonGeoms = fromGeoJSONbuilder.getGeoJsonGeoms();
      this.type = "FeatureCollection";
      this.features = GroupByBoundaryGeoJsonGenerator.createGeoJsonFeatures(groupByBoundaryResult, geoJsonGeoms);
    }
  }

//  /** Static factory method returning the whole GeoJSON response. */
//  public static RatioGroupByBoundaryResponse of(Metadata metadata, String type, Feature[] features) {
//    RatioGroupByBoundaryResponse response = new RatioGroupByBoundaryResponse();
//    response.metadata = metadata;
//    response.type = type;
//    response.features = features;
//    return response;
//  }
}
