package org.heigit.bigspatialdata.ohsome.ohsomeapi.executor;

/** Holds those parameters, which are relevant for every request. */
public class RequestParameters {

  private String requestMethod;
  private boolean isSnapshot;
  private boolean isDensity;
  private String bboxes;
  private String bcircles;
  private String bpolys;
  private String[] types;
  private String[] keys;
  private String[] values;
  private String[] userids;
  private String[] time;
  private String format;
  private String showMetadata;

  private RequestParameters() {}

  /** Overloaded constructor, which gets used by all but the /groupBy/boundary resources. */
  public RequestParameters(String requestMethod, boolean isSnapshot, boolean isDensity,
      String bboxes, String bcircles, String bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time, String showMetadata) {
    this.requestMethod = requestMethod;
    this.isSnapshot = isSnapshot;
    this.isDensity = isDensity;
    this.bboxes = bboxes;
    this.bcircles = bcircles;
    this.bpolys = bpolys;
    this.types = types;
    this.keys = keys;
    this.values = values;
    this.userids = userids;
    this.time = time;
    this.showMetadata = showMetadata;
  }

  /** Static factory method, which includes the <code>format</code> parameter. */
  public static RequestParameters of(String requestMethod, boolean isSnapshot, boolean isDensity,
      String bboxes, String bcircles, String bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time, String format, String showMetadata) {
    RequestParameters requestParameters = new RequestParameters();
    requestParameters.requestMethod = requestMethod;
    requestParameters.isSnapshot = isSnapshot;
    requestParameters.isDensity = isDensity;
    requestParameters.bboxes = bboxes;
    requestParameters.bcircles = bcircles;
    requestParameters.bpolys = bpolys;
    requestParameters.types = types;
    requestParameters.keys = keys;
    requestParameters.values = values;
    requestParameters.userids = userids;
    requestParameters.time = time;
    requestParameters.format = format;
    requestParameters.showMetadata = showMetadata;

    return requestParameters;
  }

  public String getRequestMethod() {
    return requestMethod;
  }

  public boolean isSnapshot() {
    return isSnapshot;
  }

  public boolean isDensity() {
    return isDensity;
  }

  public String getBboxes() {
    return bboxes;
  }

  public String getBcircles() {
    return bcircles;
  }

  public String getBpolys() {
    return bpolys;
  }

  public String[] getTypes() {
    return types;
  }

  public String[] getKeys() {
    return keys;
  }

  public String[] getValues() {
    return values;
  }

  public String[] getUserids() {
    return userids;
  }

  public String[] getTime() {
    return time;
  }

  public String getFormat() {
    return format;
  }

  public String getShowMetadata() {
    return showMetadata;
  }
}
