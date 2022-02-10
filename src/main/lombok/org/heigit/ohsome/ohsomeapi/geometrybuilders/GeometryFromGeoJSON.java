package org.heigit.ohsome.ohsomeapi.geometrybuilders;

import org.locationtech.jts.geom.Geometry;

public interface GeometryFromGeoJSON extends GeometryFrom {

  Geometry create(String geometry);
}
