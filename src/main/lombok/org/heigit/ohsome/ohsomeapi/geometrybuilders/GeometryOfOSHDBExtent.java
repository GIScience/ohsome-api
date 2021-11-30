package org.heigit.ohsome.ohsomeapi.geometrybuilders;

import org.heigit.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Component;
import org.wololo.jts2geojson.GeoJSONReader;

@Component
public class GeometryOfOSHDBExtent extends GeometryBuilder {
//TODO this class should implement GeometryFrom GeoJSON
  /**
   * Creates a Geometry object from the given GeoJSON String, which is derived from the metadata.
   *
   * @throws RuntimeException if the derived GeoJSON cannot be converted to a Geometry
   */
  public Geometry create(String geoJson) {
    GeoJSONReader reader = new GeoJSONReader();
    try {
      Geometry geometry = reader.read(geoJson);
      ProcessingData.setDataPolyGeom(geometry);
      return geometry;
    } catch (Exception e) {
      throw new RuntimeException("The GeoJSON that is derived out of the metadata, cannot be "
          + "converted. Please use a different data file and contact an admin about this issue.");
    }
  }
}
