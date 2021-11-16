package org.heigit.ohsome.ohsomeapi.geometrybuilders;

import org.locationtech.jts.geom.Geometry;

public interface GeometryFromGeoJSON {
  Geometry create(String geometry);
}
