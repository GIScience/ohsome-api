package org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing;

/**
 * Enumeration defining the type of boundary parameter (BBOXES, BPOINTS, BPOLYS, or NOBOUNDARY in
 * case that no boundary parameter is given)
 *
 */
public enum BoundaryType {

  NOBOUNDARY, BBOXES, BCIRCLES, BPOLYS
}
