package org.heigit.ohsome.ohsomeapi.geometrybuilders;

import javax.servlet.http.HttpServletRequest;
import org.locationtech.jts.geom.Geometry;

public interface GeometryFromGeoJSON extends GeometryFrom {

  Geometry create(String geometry, HttpServletRequest servletRequest);
}
