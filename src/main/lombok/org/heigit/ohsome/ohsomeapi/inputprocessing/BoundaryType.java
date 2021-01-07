package org.heigit.ohsome.ohsomeapi.inputprocessing;

/**
 * Enumeration defining the type of boundary parameter (BBOXES, BPOINTS, BPOLYS, or NOBOUNDARY in
 * case that no boundary parameter is given).
 */
public enum BoundaryType {
  NOBOUNDARY, BBOXES, BCIRCLES, BPOLYS
}
