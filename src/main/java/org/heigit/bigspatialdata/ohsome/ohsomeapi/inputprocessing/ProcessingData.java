package org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import org.geojson.GeoJsonObject;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.RequestParameters;
import org.heigit.bigspatialdata.oshdb.osm.OSMType;
import com.vividsolutions.jts.geom.Geometry;

/** Holds the relevant objects for processing the request and creating the response. */
public class ProcessingData {

  private static Geometry dataPolyGeom;
  private static double timeout;
  private RequestParameters requestParameters;
  private String requestUrl;
  private BoundaryType boundaryType;
  private String[] boundaryValues;
  private String boundaryValuesGeoJson;
  private EnumSet<OSMType> osmTypes;
  private boolean showMetadata;
  private String format;
  private Geometry requestGeom;
  private Collection<Geometry> boundaryColl;
  private GeoJsonObject[] geoJsonGeoms;

  public ProcessingData(RequestParameters requestParameters) {
    this.requestParameters = requestParameters;
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

  public Collection<Geometry> getBoundaryColl() {
    return boundaryColl;
  }

  public void setBoundaryColl(Collection<Geometry> boundaryColl) {
    this.boundaryColl = boundaryColl;
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
}
