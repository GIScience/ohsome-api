package org.heigit.bigspatialdata.ohsome.ohsomeApi.executor;

/** Holds those parameters, which are relevant for every request. */
public class RequestParameters {

  private boolean isPost;
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
  private String showMetadata;

  public RequestParameters(boolean isPost, boolean isSnapshot, boolean isDensity, String bboxes, String bcircles,
      String bpolys, String[] types, String[] keys, String[] values, String[] userids,
      String[] time, String showMetadata) {

    this.isPost = isPost;
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

  public boolean isPost() {
    return isPost;
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

  public String getShowMetadata() {
    return showMetadata;
  }

}
