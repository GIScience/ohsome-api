package org.heigit.ohsome.ohsomeapi.geometries;

import org.locationtech.jts.geom.Geometry;

public interface OhsomePolygon {
  Geometry create(String[] geometries);
}
