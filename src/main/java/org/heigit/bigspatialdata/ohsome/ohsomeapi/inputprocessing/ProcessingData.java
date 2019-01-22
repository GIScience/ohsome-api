package org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing;

import java.util.Collection;
import java.util.EnumSet;
import javax.servlet.http.HttpServletRequest;
import org.geojson.GeoJsonObject;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.executor.RequestParameters;
import org.heigit.bigspatialdata.oshdb.osm.OSMType;
import com.vividsolutions.jts.geom.Geometry;

/** Holds the relevant objects for processing the request and creating the response. */
public class ProcessingData {

  public static Geometry dataPolyGeom;
  private final RequestParameters requestParameters;
  public String requestUrl;
  public BoundaryType boundary;
  public String[] boundaryValues;
  public String boundaryValuesGeoJson;
  public EnumSet<OSMType> osmTypes;
  public boolean showMetadata;
  public String format;
  public Geometry bboxesGeom;
  public Geometry bcirclesGeom;
  public Geometry bpolysGeom;
  public Collection<Geometry> boundaryColl;
  public GeoJsonObject[] geoJsonGeoms;

  public ProcessingData(HttpServletRequest request, boolean isSnapshot, boolean isDensity) {
    this.requestParameters = new RequestParameters(request.getMethod(), isSnapshot, isDensity,
        request.getParameter("bboxes"), request.getParameter("bcircles"),
        request.getParameter("bpolys"), request.getParameter("types").split(","),
        request.getParameter("keys").split(","), request.getParameter("values").split(","),
        request.getParameter("userids").split(","), request.getParameter("time").split(","),
        request.getParameter("format"), request.getParameter("showMetadata"));
  }
  
  public ProcessingData(RequestParameters requestParameters) {
    this.requestParameters = requestParameters;
  }

  public RequestParameters getRequestParameters() {
    return requestParameters;
  }

  public String getRequestUrl() {
    return requestUrl;
  }

  public void setRequestUrl(String requestUrl) {
    this.requestUrl = requestUrl;
  }

  public BoundaryType getBoundary() {
    return boundary;
  }

  public void setBoundary(BoundaryType boundary) {
    this.boundary = boundary;
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

  public EnumSet<OSMType> getOsmTypes() {
    return osmTypes;
  }

  public void setOsmTypes(EnumSet<OSMType> osmTypes) {
    this.osmTypes = osmTypes;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public Geometry getBboxesGeom() {
    return bboxesGeom;
  }

  public void setBboxesGeom(Geometry bboxesGeom) {
    this.bboxesGeom = bboxesGeom;
  }

  public Geometry getBcirclesGeom() {
    return bcirclesGeom;
  }

  public void setBcirclesGeom(Geometry bcirclesGeom) {
    this.bcirclesGeom = bcirclesGeom;
  }

  public Geometry getBpolysGeom() {
    return bpolysGeom;
  }

  public void setBpolysGeom(Geometry bpolysGeom) {
    this.bpolysGeom = bpolysGeom;
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

}
