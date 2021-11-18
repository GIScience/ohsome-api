package org.heigit.ohsome.ohsomeapi.geometrybuilders;

import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.locationtech.jts.geom.Geometry;

public interface GeometryFromGeoJSON {
  Geometry create(String geometry, InputProcessor inputProcessor);
}
