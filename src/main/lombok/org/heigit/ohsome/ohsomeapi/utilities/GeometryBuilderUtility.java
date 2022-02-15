package org.heigit.ohsome.ohsomeapi.utilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.exception.NotFoundException;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GeometryBuilderUtility {

  private final SpatialUtility spatialUtility;

  @Autowired
  protected GeometryBuilderUtility(SpatialUtility spatialUtility) {
    this.spatialUtility = spatialUtility;
  }

  /**
   * Computes the union of the given geometries and checks if it is completely within the underlying
   * data extract.
   *
   * @param geometries <code>Collection</code> containing the geometries to unify
   * @return unified geometries
   * @throws NotFoundException if the unified Geometry does not lie completely within the underlying
   *         data extract
   */
  public Geometry unifyPolys(Collection<Geometry> geometries) {
    GeometryFactory geometryFactory = new GeometryFactory();
    Polygon[] polys = geometries.stream().flatMap(geo -> {
      if (geo instanceof MultiPolygon) {
        int num = geo.getNumGeometries();
        ArrayList<Polygon> parts = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
          parts.add((Polygon) geo.getGeometryN(i));
        }
        return parts.stream();
      } else {
        return Stream.of(geo);
      }
    }).toArray(Polygon[]::new);
    MultiPolygon mp = geometryFactory.createMultiPolygon(polys);
    // merge all input geometries to single (multi) polygon
    Geometry result = mp.union();
    if (!spatialUtility.isWithin(result)) {
      throw new NotFoundException(ExceptionMessages.BOUNDARY_NOT_IN_DATA_EXTRACT);
    }
    return result;
  }

  /**
   * Creates a boundary ID value from the 'id' field in the given <code>JsonObject</code>.
   *
   * @param jsonObject <code>JsonObject</code> where the 'id' value is extracted from
   * @return <code>Object</code> having the custom id of type <code>String</code> or
   *         <code>Integer</code>
   */
  public Serializable createBoundaryIdFromJsonObjectId(JsonObject jsonObject,
      HttpServletRequest servletRequest) {
    if (jsonObject.get("id").getValueType().compareTo(JsonValue.ValueType.STRING) == 0) {
      String id = jsonObject.getString("id");
      if ("csv".equalsIgnoreCase(InputProcessor.createEmptyStringIfNull(servletRequest.getParameter("format")))) {
        spatialUtility.checkCustomBoundaryId(id);
      }
      return id;
    } else {
      return jsonObject.getInt("id");
    }
  }

  /**
   * Checks the geometry of the given <code>JsonObject</code> on its type. If it's not of type
   * Polygon or Multipolygon, an exception is thrown.
   *
   * @param geomObj <code>JsonObject</code> to check
   * @throws BadRequestException if the given <code>JsonObject</code> is not of type Polygon or
   *         Multipolygon
   */
  public void checkGeometryTypeOfFeature(JsonObject geomObj) {
    if (!geomObj.getString("type").equals("Polygon")
        && !geomObj.getString("type").equals("MultiPolygon")) {
      throw new BadRequestException(
          "The geometry of each feature in the GeoJSON has to be of type 'Polygon' "
              + "or 'MultiPolygon'.");
    }
  }
}
