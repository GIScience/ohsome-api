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

  public RequestParameters(String requestMethod, boolean isSnapshot, boolean isDensity,
      String bboxes, String bcircles, String bpolys, String[] types, String[] keys, String[] values,
      String[] userids, String[] time, String format, String showMetadata) {
    this.requestMethod = requestMethod;
    this.isDensity = isDensity;
    this.isSnapshot = isSnapshot;
    this.bboxes = bboxes;
    this.bcircles = bcircles;
    this.bpolys = bpolys;
    this.types = types;
    this.keys = keys;
    this.values = values;
    this.userids = userids;
    this.time = time;
    this.format = format;
    this.showMetadata = showMetadata;
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
