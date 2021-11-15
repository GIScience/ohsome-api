package org.heigit.ohsome.ohsomeapi.geometries;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import org.geojson.GeoJsonObject;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.utilities.SpatialUtility;
import org.locationtech.jts.geom.Geometry;
import org.wololo.jts2geojson.GeoJSONReader;

public class FromGeoJSONGeometryBuilder extends GeometryBuilder implements GeometryFromGeoJSONBuilder {
org.heigit.ohsome.ohsomeapi.inputprocessing.GeometryBuilder geometryBuilder;
  /**
   * Creates a Geometry object from the given GeoJSON String. It must be of type 'FeatureCollection'
   * and its features must be of type 'Polygon' or 'Multipolygon'.
   *
   * @throws BadRequestException if the given GeoJSON String cannot be converted to a Geometry, it
   *     is not of the type 'FeatureCollection', or if the provided custom id(s) cannot be
   *     parsed
   */
  public Geometry create(String geoJson) {
    ArrayList<Geometry> geometryList = new ArrayList<Geometry>();
    JsonObject root = null;
    JsonArray features;
    Serializable[] boundaryIds;
    GeoJsonObject[] geoJsonGeoms;
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
          boundaryIds[count] = createBoundaryIdFromJsonObjectId(feature);
        } else if (properties != null && properties.containsKey("id")) {
          boundaryIds[count] = createBoundaryIdFromJsonObjectId(properties);
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
        this.checkGeometryTypeOfFeature(geomObj);
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
    Geometry result = unifyPolys(geometryList);
    geometryBuilder.getProcessingData().setGeoJsonGeoms(geoJsonGeoms);
    geometryBuilder.getProcessingData().setBoundaryList(geometryList);
    geometryBuilder.getProcessingData().setRequestGeom(result);
    SpatialUtility util = new SpatialUtility();
    util.setBoundaryIds(boundaryIds);
    return result;
  }
}
