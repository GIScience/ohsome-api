package org.heigit.ohsome.ohsomeapi.geometrybuilders;

import org.locationtech.jts.geom.Geometry;

public interface GeometryFromCoordinates extends GeometryFrom {

  Geometry create(String[] geometries);

}
