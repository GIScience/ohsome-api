package org.heigit.ohsome.ohsomeapi.executor;

import lombok.Getter;
import lombok.Setter;

/** Holds those parameters, which are relevant for every request. */
@Getter
@Setter
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
  private String[] time;
  private String format;
  private String showMetadata;
  private double timeout;
  private String filter;

  public RequestParameters(String requestMethod, boolean isSnapshot, boolean isDensity,
      String bboxes, String bcircles, String bpolys, String[] types, String[] keys, String[] values,
      String[] time, String format, String showMetadata, double timeout, String filter) {

    this.requestMethod = requestMethod;
    this.isDensity = isDensity;
    this.isSnapshot = isSnapshot;
    this.bboxes = bboxes;
    this.bcircles = bcircles;
    this.bpolys = bpolys;
    this.types = types;
    this.keys = keys;
    this.values = values;
    this.time = time;
    this.format = format;
    this.showMetadata = showMetadata;
    this.timeout = timeout;
    this.filter = filter;
  }
}
