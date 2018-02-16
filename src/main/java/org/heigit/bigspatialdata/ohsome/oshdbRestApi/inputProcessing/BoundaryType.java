package org.heigit.bigspatialdata.ohsome.oshdbRestApi.inputProcessing;

/**
 * Enumeration defining the type of boundary parameter (BBOXES, BPOINTS, BPOLYS, or NOBOUNDARY in
 * case of no boundary parameter is given)
 *
 */
public enum BoundaryType {

  NOBOUNDARY, BBOXES, BCIRCLES, BPOLYS
}
