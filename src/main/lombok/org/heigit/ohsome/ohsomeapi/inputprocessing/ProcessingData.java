package org.heigit.ohsome.ohsomeapi.inputprocessing;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.geojson.GeoJsonObject;
import org.heigit.ohsome.ohsomeapi.executor.RequestParameters;
import org.heigit.ohsome.oshdb.filter.FilterExpression;
import org.locationtech.jts.geom.Geometry;

/** Holds the relevant objects for processing the request and creating the response.*/
@Getter
@Setter
public class ProcessingData implements Serializable {
  @Getter
  @Setter
  private static Geometry dataPolyGeom;
  @Getter
  @Setter
  private static double timeout;
  @Getter
  @Setter
  private static int numberOfDataExtractionThreads = 1;
  private RequestParameters requestParameters;
  private String requestUrl;
  private BoundaryType boundaryType;
  private String[] boundaryValues;
  private String boundaryValuesGeoJson;
  private boolean showMetadata;
  private String format;
  private Geometry requestGeom;
  private List<Geometry> boundaryList;
  private GeoJsonObject[] geoJsonGeoms;
  @Getter
  @Setter
  private static int numberOfClusterNodes;
  private boolean isRatio;
  private boolean isGroupByBoundary;
  private boolean isFullHistory;
  private FilterExpression filterExpression;

  public ProcessingData(RequestParameters requestParameters, String requestUrl) {
    this.requestParameters = requestParameters;
    this.requestUrl = requestUrl;
    this.isRatio = false;
    this.isGroupByBoundary = false;
    this.isFullHistory = false;
  }

  // Override @Getter of Lombok
  public Optional<FilterExpression> getFilterExpression() {
    return Optional.ofNullable(this.filterExpression);
  }

}
