package org.heigit.ohsome.ohsomeapi.geometrybuilders;

import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Component;

@Component
public interface GeometryFromCoordinates {
  Geometry create(String[] geometries);
}
