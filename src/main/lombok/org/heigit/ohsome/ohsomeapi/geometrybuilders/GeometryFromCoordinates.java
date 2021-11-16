package org.heigit.ohsome.ohsomeapi.geometrybuilders;

import org.locationtech.jts.geom.Geometry;

public interface GeometryFromCoordinates {
  Geometry create(String[] geometries);
}
