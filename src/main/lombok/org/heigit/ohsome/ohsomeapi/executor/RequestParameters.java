package org.heigit.ohsome.ohsomeapi.executor;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** Holds those parameters, which are relevant for every request. */
@Getter
@Setter
@AllArgsConstructor
public class RequestParameters implements Serializable {

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
}
