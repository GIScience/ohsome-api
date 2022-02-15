package org.heigit.ohsome.ohsomeapi.geometrybuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.geojson.GeoJsonObject;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.utilities.GeometryBuilderUtility;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.wololo.jts2geojson.GeoJSONReader;

@Component
@Getter
public class BPolygonFromGeoJSON implements GeometryFromGeoJSON {

  private List<Geometry> geometryList;
  private GeoJsonObject[] geoJsonGeoms;
  private Geometry geometry;
  private Serializable[] boundaryIds;
  private final GeometryBuilderUtility geometryBuilderUtility;

  @Autowired
  public BPolygonFromGeoJSON(GeometryBuilderUtility geometryBuilderUtility) {
    this.geometryBuilderUtility = geometryBuilderUtility;
  }

  /**
   * Creates a Geometry object from the given GeoJSON String. It must be of type 'FeatureCollection'
   * and its features must be of type 'Polygon' or 'Multipolygon'.
   *
   * @throws BadRequestException if the given GeoJSON String cannot be converted to a Geometry, it
   *     is not of the type 'FeatureCollection', or if the provided custom id(s) cannot be
   *     parsed
   */
  public Geometry create(String geoJson, HttpServletRequest servletRequest) {
    geometryList = new ArrayList<>();
    JsonObject root = null;
    JsonArray features;
    try (JsonReader jsonReader = Json.createReader(new StringReader(geoJson))) {
      root = jsonReader.readObject();
      features = root.getJsonArray("features");
      boundaryIds = new Serializable[features.size()];
      geoJsonGeoms = new GeoJsonObject[features.size()];
    } catch (Exception e) {
      throw new BadRequestException("Error in reading the provided GeoJSON. The given GeoJSON has "
          + "to be of the type 'FeatureCollection'. Please take a look at the documentation page "
          + "for the bpolys parameter to see an example of a fitting GeoJSON input file.");
    }
    int count = 0;
    for (JsonValue featureVal : features) {
      JsonObject feature = featureVal.asJsonObject();
      JsonObject properties = feature.getJsonObject("properties");
      try {
        if (feature.containsKey("id")) {
          boundaryIds[count] = geometryBuilderUtility.createBoundaryIdFromJsonObjectId(feature,
              servletRequest);
        } else if (properties != null && properties.containsKey("id")) {
          boundaryIds[count] = geometryBuilderUtility.createBoundaryIdFromJsonObjectId(properties,
              servletRequest);
        } else {
          boundaryIds[count] = "feature" + (count + 1);
        }
        count++;
      } catch (BadRequestException e) {
        throw e;
      } catch (Exception e) {
        throw new BadRequestException("The provided custom id(s) could not be parsed.");
      }
      try {
        JsonObject geomObj = feature.getJsonObject("geometry");
        geometryBuilderUtility.checkGeometryTypeOfFeature(geomObj);
        GeoJSONReader reader = new GeoJSONReader();
        Geometry currentResult = reader.read(geomObj.toString());
        geometryList.add(currentResult);
        geoJsonGeoms[count - 1] =
            new ObjectMapper().readValue(geomObj.toString(), GeoJsonObject.class);
      } catch (Exception e) {
        throw new BadRequestException("The provided GeoJSON cannot be converted. Please take a "
            + "look at the documentation page for the bpolys parameter to see an example of a "
            + "fitting GeoJSON input file.");
      }
    }
    geometry = geometryBuilderUtility.unifyPolys(geometryList);
    return geometry;
  }
}
