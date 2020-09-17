package org.heigit.ohsome.ohsomeapi.inputprocessing;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.geojson.GeoJsonObject;
import org.heigit.bigspatialdata.oshdb.osm.OSMType;
import org.heigit.ohsome.filter.BinaryOperator;
import org.heigit.ohsome.filter.FilterExpression;
import org.heigit.ohsome.filter.GeometryTypeFilter;
import org.heigit.ohsome.ohsomeapi.executor.RequestParameters;
import org.locationtech.jts.geom.Geometry;

/** Holds the relevant objects for processing the request and creating the response. */
public class ProcessingData {

  private static Geometry dataPolyGeom;
  private static double timeout;
  private static int numberOfDataExtractionThreads = 1;
  private RequestParameters requestParameters;
  private String requestUrl;
  private BoundaryType boundaryType;
  private String[] boundaryValues;
  private String boundaryValuesGeoJson;
  private EnumSet<OSMType> osmTypes;
  private boolean showMetadata;
  private String format;
  private Geometry requestGeom;
  private List<Geometry> boundaryList;
  private GeoJsonObject[] geoJsonGeoms;
  private boolean containsSimpleFeatureTypes;
  private EnumSet<SimpleFeatureType> simpleFeatureTypes;
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

  public static Geometry getDataPolyGeom() {
    return dataPolyGeom;
  }

  public static void setDataPolyGeom(Geometry dataPolyGeom) {
    ProcessingData.dataPolyGeom = dataPolyGeom;
  }

  public static double getTimeout() {
    return timeout;
  }

  public static void setTimeout(double timeout) {
    ProcessingData.timeout = timeout;
  }

  public RequestParameters getRequestParameters() {
    return requestParameters;
  }

  public void setRequestParameters(RequestParameters requestParameters) {
    this.requestParameters = requestParameters;
  }

  public String getRequestUrl() {
    return requestUrl;
  }

  public void setRequestUrl(String requestUrl) {
    this.requestUrl = requestUrl;
  }

  public BoundaryType getBoundaryType() {
    return boundaryType;
  }

  public void setBoundaryType(BoundaryType boundaryType) {
    this.boundaryType = boundaryType;
  }

  public String[] getBoundaryValues() {
    return boundaryValues;
  }

  public void setBoundaryValues(String[] boundaryValues) {
    this.boundaryValues = boundaryValues;
  }

  public String getBoundaryValuesGeoJson() {
    return boundaryValuesGeoJson;
  }

  public void setBoundaryValuesGeoJson(String boundaryValuesGeoJson) {
    this.boundaryValuesGeoJson = boundaryValuesGeoJson;
  }

  public Set<OSMType> getOsmTypes() {
    return osmTypes;
  }

  public void setOsmTypes(Set<OSMType> osmTypes) {
    this.osmTypes = (EnumSet<OSMType>) osmTypes;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public Geometry getRequestGeom() {
    return requestGeom;
  }

  public void setRequestGeom(Geometry requestGeom) {
    this.requestGeom = requestGeom;
  }

  public List<Geometry> getBoundaryList() {
    return boundaryList;
  }

  public void setBoundaryList(List<Geometry> boundaryList) {
    this.boundaryList = boundaryList;
  }

  public GeoJsonObject[] getGeoJsonGeoms() {
    return geoJsonGeoms;
  }

  public void setGeoJsonGeoms(GeoJsonObject[] geoJsonGeoms) {
    this.geoJsonGeoms = geoJsonGeoms;
  }

  public boolean isShowMetadata() {
    return showMetadata;
  }

  public void setShowMetadata(boolean showMetadata) {
    this.showMetadata = showMetadata;
  }

  public boolean containsSimpleFeatureTypes() {
    return containsSimpleFeatureTypes;
  }

  public void setContainsSimpleFeatureTypes(boolean containsSimpleFeatureTypes) {
    this.containsSimpleFeatureTypes = containsSimpleFeatureTypes;
  }

  public Set<SimpleFeatureType> getSimpleFeatureTypes() {
    return simpleFeatureTypes;
  }

  public void setSimpleFeatureTypes(Set<SimpleFeatureType> simpleFeatureTypes) {
    this.simpleFeatureTypes = (EnumSet<SimpleFeatureType>) simpleFeatureTypes;
  }

  public static int getNumberOfClusterNodes() {
    return numberOfClusterNodes;
  }

  public static void setNumberOfClusterNodes(int numberOfClusterNodes) {
    ProcessingData.numberOfClusterNodes = numberOfClusterNodes;
  }

  public static int getNumberOfDataExtractionThreads() {
    return numberOfDataExtractionThreads;
  }

  public static void setNumberOfDataExtractionThreads(int numberOfDataExtractionsThreads) {
    numberOfDataExtractionThreads = numberOfDataExtractionsThreads;
  }

  public boolean isRatio() {
    return isRatio;
  }

  public void setIsRatio(boolean isRatio) {
    this.isRatio = isRatio;
  }

  public boolean isGroupByBoundary() {
    return isGroupByBoundary;
  }

  public void setIsGroupByBoundary(boolean isGroupByBoundary) {
    this.isGroupByBoundary = isGroupByBoundary;
  }

  public boolean isFullHistory() {
    return isFullHistory;
  }

  public void setIsFullHistory(boolean isFullHistory) {
    this.isFullHistory = isFullHistory;
  }

  public void setFilterExpression(FilterExpression filterExpression) {
    this.filterExpression = filterExpression;
  }

  public Optional<FilterExpression> getFilterExpression() {
    return Optional.ofNullable(this.filterExpression);
  }

  /**
   * Checks if a given filter expression contains a geometry type check or not.
   *
   * @param expr the filter expression to check
   * @return true if the given filter expression contains at least one geometry type check
   */
  public static boolean filterContainsGeometryTypeCheck(FilterExpression expr) {
    if (expr instanceof GeometryTypeFilter) {
      return true;
    } else if (expr instanceof BinaryOperator) {
      return filterContainsGeometryTypeCheck(((BinaryOperator) expr).getLeftOperand())
          || filterContainsGeometryTypeCheck(((BinaryOperator) expr).getRightOperand());
    } else {
      return false;
    }
  }
}
