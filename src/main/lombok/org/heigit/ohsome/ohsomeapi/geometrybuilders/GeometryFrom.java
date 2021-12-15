package org.heigit.ohsome.ohsomeapi.geometrybuilders;

import java.util.List;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Component;

@Component
public interface GeometryFrom {

  List<Geometry> getGeometryList();
}
