package org.heigit.ohsome.ohsomeapi.utilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.inputprocessing.BoundaryType;
import org.heigit.ohsome.ohsomeapi.inputprocessing.GeometryBuilder;
import org.heigit.ohsome.ohsomeapi.inputprocessing.SimpleFeatureType;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.ohsome.oshdb.OSHDBTag;
import org.heigit.ohsome.oshdb.api.mapreducer.MapReducer;
import org.heigit.ohsome.oshdb.osm.OSMType;
import org.heigit.ohsome.oshdb.util.mappable.OSHDBMapReducible;
import org.heigit.ohsome.oshdb.util.tagtranslator.TagTranslator;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Lineal;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.geom.Puntal;

public class SpatialUtility implements Serializable {
  public static final String GEOMCOLLTYPE = "GeometryCollection";
  private Serializable[] boundaryIds;

  /**
   * Finds and returns the EPSG code of the given point, which is needed for {@link
   * GeometryBuilder#createCircularPolygons(String[]
   * bcircles) createCircularPolygons}.
   *
   * <p>Adapted code from UTMCodeFromLonLat.java class in the osmatrix project (© by Michael Auer)
   *
   * @param lon Longitude coordinate of the point.
   * @param lat Latitude coordinate of the point.
   * @return <code>String</code> representing the corresponding EPSG code.
   */
  public String findEpsg(double lon, double lat) {

    if (lat >= 84) {
      return "EPSG:32661"; // UPS North
    }
    if (lat < -80) {
      return "EPSG:32761"; // UPS South
    }
    int zoneNumber = (int) (Math.floor((lon + 180) / 6) + 1);
    if (lat >= 56.0 && lat < 64.0 && lon >= 3.0 && lon < 12.0) {
      zoneNumber = 32;
    }
    // Special zones for Svalbard
    if (lat >= 72.0 && lat < 84.0) {
      if (lon >= 0.0 && lon < 9.0) {
        zoneNumber = 31;
      } else if (lon >= 9.0 && lon < 21.0) {
        zoneNumber = 33;
      } else if (lon >= 21.0 && lon < 33.0) {
        zoneNumber = 35;
      } else if (lon >= 33.0 && lon < 42.0) {
        zoneNumber = 37;
      }
    }
    String isNorth = lat > 0 ? "6":"7";
    String zone = zoneNumber < 10 ? "0" + zoneNumber:"" + zoneNumber;
    return "EPSG:32" + isNorth + zone;
  }

  /**
   * Splits the given bounding boxes and returns them in a <code>List</code>.
   *
   * @param bboxes contains the given bounding boxes
   * @return <code>List</code> containing the splitted bounding boxes
   * @throws BadRequestException if the bboxes parameter has an invalid format
   */
  public List<String> splitBboxes(String bboxes) {
    String[] bboxesArray = splitOnHyphen(bboxes);
    List<String> boundaryParamValues = new ArrayList<>();
    boundaryIds = new Serializable[bboxesArray.length];
    try {
      if (bboxesArray[0].contains(":")) {
        boundaryParamValues = splitBboxesWithIds(bboxesArray);
      } else {
        boundaryParamValues = splitBoundariesWithoutIds(bboxesArray, BoundaryType.BBOXES);
      }
    } catch (Exception e) {
      if (e.getClass() == BadRequestException.class) {
        throw e;
      }
      throw new BadRequestException(ExceptionMessages.BOUNDARY_PARAM_FORMAT);
    }
    boundaryParamValues.removeAll(Collections.singleton(null));
    return boundaryParamValues;
  }

  /**
   * Splits the given bounding circles and returns them in a <code>List</code>.
   *
   * @param bcircles contains the given bounding circles
   * @return <code>List</code> containing the splitted bounding circles
   * @throws BadRequestException if the bcircles parameter has an invalid format
   */
  public List<String> splitBcircles(String bcircles) {
    String[] bcirclesArray = splitOnHyphen(bcircles);
    List<String> boundaryParamValues = new ArrayList<String>();
    boundaryIds = new Serializable[bcirclesArray.length];
    try {
      if (bcirclesArray[0].contains(":")) {
        boundaryParamValues = splitBcirclesWithIds(bcirclesArray);
      } else {
        boundaryParamValues = splitBoundariesWithoutIds(bcirclesArray, BoundaryType.BCIRCLES);
      }
    } catch (Exception e) {
      if (e.getClass() == BadRequestException.class) {
        throw e;
      }
      throw new BadRequestException(ExceptionMessages.BOUNDARY_PARAM_FORMAT);
    }
    boundaryParamValues.removeAll(Collections.singleton(null));
    return boundaryParamValues;
  }

  /**
   * Splits the given bounding polygons and returns them in a <code>List</code>.
   *
   * @param bpolys contains the given bounding polygons
   * @return <code>List</code> containing the splitted bounding polygons
   * @throws BadRequestException if the bpolys parameter has an invalid format
   */
  public List<String> splitBpolys(String bpolys) {
    String[] bpolysArray = splitOnHyphen(bpolys);
    List<String> boundaryParamValues = new ArrayList<String>();
    boundaryIds = new Serializable[bpolysArray.length];
    try {
      if (bpolysArray[0].contains(":")) {
        boundaryParamValues = splitBpolysWithIds(bpolysArray);
      } else if (bpolysArray[0].contains(",")) {
        boundaryParamValues = splitBoundariesWithoutIds(bpolysArray, BoundaryType.BPOLYS);
      } else {
        throw new BadRequestException(ExceptionMessages.BOUNDARY_PARAM_FORMAT);
      }
    } catch (Exception e) {
      if (e.getClass() == BadRequestException.class) {
        throw e;
      }
      throw new BadRequestException(ExceptionMessages.BOUNDARY_PARAM_FORMAT);
    }
    boundaryParamValues.removeAll(Collections.singleton(null));
    return boundaryParamValues;
  }

  /**
   * Checks the given custom boundary id. At the moment only used if output format = csv.
   *
   * @throws BadRequestException if the custom ids contain semicolons
   */
  public void checkCustomBoundaryId(String id) {
    if (id.contains(";")) {
      throw new BadRequestException("The given custom ids cannot contain semicolons, "
          + "if you want to use csv as output format.");
    }
  }

  /**
   * Checks if the given geometry is within the underlying data-polygon. Returns also true if no
   * data-polygon is given.
   *
   * @param geom <code>Geometry</code>, which is tested against the data-polygon
   * @return <code>true</code> - if inside <br>
   *     <code>false</code> - if not inside
   */
  public boolean isWithin(Geometry geom) {
    if (ExtractMetadata.dataPoly != null) {
      return geom.within(ExtractMetadata.dataPoly);
    }
    return true;
  }

  /**
   * Checks if the given String is one of the simple feature types (point, line, polygon).
   */
  public boolean isSimpleFeatureType(String type) {
    return "point".equalsIgnoreCase(type) || "line".equalsIgnoreCase(type)
        || "polygon".equalsIgnoreCase(type) || "other".equalsIgnoreCase(type);
  }

  /**
   * Applies an entity filter using only planar relations (relations with an area) on the given
   * MapReducer object. It uses the tags "type=multipolygon" and "type=boundary".
   */
  public <T extends OSHDBMapReducible> MapReducer<T> filterOnPlanarRelations(MapReducer<T> mapRed) {
    // further filtering to not look at all relations
    TagTranslator tt = DbConnData.tagTranslator;
    OSHDBTag typeMultipolygon = tt.getOSHDBTagOf("type", "multipolygon");
    OSHDBTag typeBoundary = tt.getOSHDBTagOf("type", "boundary");
    return mapRed.osmEntityFilter(entity -> !entity.getType().equals(OSMType.RELATION)
        || entity.hasTagValue(typeMultipolygon.getKey(), typeMultipolygon.getValue())
        || entity.hasTagValue(typeBoundary.getKey(), typeBoundary.getValue()));
  }

  /**
   * Checks whether a geometry is of given feature type (Puntal|Lineal|Polygonal).
   *
   * @param simpleFeatureTypes a set of feature types
   * @return true if the geometry matches the given simpleFeatureTypes, otherwise false
   */
  public boolean checkGeometryOnSimpleFeatures(Geometry geom,
      Set<SimpleFeatureType> simpleFeatureTypes) {
    return simpleFeatureTypes.contains(SimpleFeatureType.POLYGON) && geom instanceof Polygonal
        || simpleFeatureTypes.contains(SimpleFeatureType.POINT) && geom instanceof Puntal
        || simpleFeatureTypes.contains(SimpleFeatureType.LINE) && geom instanceof Lineal
        || simpleFeatureTypes.contains(SimpleFeatureType.OTHER)
        && GEOMCOLLTYPE.equalsIgnoreCase(geom.getGeometryType());
  }

  /**
   * Splits the given boundary parameter (bboxes, bcircles, or bpolys) on '|' to seperate the
   * different bounding objects.
   *
   * @param boundaryParam <code>String</code> that contains the boundary parameter(s)
   * @return splitted boundaries
   */
  public String[] splitOnHyphen(String boundaryParam) {
    if (boundaryParam.contains("|")) {
      return boundaryParam.split("\\|");
    }
    return new String[]{boundaryParam};
  }

  /**
   * Splits the coordinates from the given boundaries array.
   *
   * @param boundariesArray contains the boundaries without a custom id
   * @return <code>List</code> containing the splitted boundaries
   */
  public List<String> splitBoundariesWithoutIds(String[] boundariesArray,
      BoundaryType boundaryType) {
    List<String> boundaryParamValues = new ArrayList<String>();
    for (int i = 0; i < boundariesArray.length; i++) {
      String[] coords = boundariesArray[i].split(",");
      Collections.addAll(boundaryParamValues, coords);
      boundaryIds[i] = "boundary" + (i + 1);
    }
    checkBoundaryParamLength(boundaryParamValues, boundaryType);
    return boundaryParamValues;
  }

  /**
   * Splits the ids and the coordinates from the given bounding boxes array.
   *
   * @param bboxesArray contains the bounding boxes having a custom id
   * @return <code>List</code> containing the splitted bounding boxes
   * @throws BadRequestException if the bboxes have invalid format
   */
  public List<String> splitBboxesWithIds(String[] bboxesArray) {
    List<String> boundaryParamValues = new ArrayList<String>();
    for (int i = 0; i < bboxesArray.length; i++) {
      String[] coords = bboxesArray[i].split(",");
      if (coords.length != 4) {
        throw new BadRequestException(ExceptionMessages.BOUNDARY_PARAM_FORMAT);
      }
      if (coords[0].contains(":")) {
        String[] idAndCoordinate = coords[0].split(":");
        // extract the id
        boundaryIds[i] = idAndCoordinate[0];
        // extract the coordinates
        boundaryParamValues.add(idAndCoordinate[1]);
        boundaryParamValues.add(coords[1]);
        boundaryParamValues.add(coords[2]);
        boundaryParamValues.add(coords[3]);
      } else {
        throw new BadRequestException(ExceptionMessages.BOUNDARY_IDS_FORMAT);
      }
    }
    checkBoundaryParamLength(boundaryParamValues, BoundaryType.BBOXES);
    return boundaryParamValues;
  }

  /**
   * Splits the ids and the coordinates from the given bounding circles array.
   *
   * @param bcirclesArray contains the bounding circles having a custom id
   * @return <code>List</code> containing the splitted bounding circles
   * @throws BadRequestException if the bcircles have invalid format
   */
  public List<String> splitBcirclesWithIds(String[] bcirclesArray) {
    List<String> boundaryParamValues = new ArrayList<String>();
    for (int i = 0; i < bcirclesArray.length; i++) {
      String[] coords = bcirclesArray[i].split(",");
      if (coords.length != 3) {
        throw new BadRequestException(ExceptionMessages.BOUNDARY_PARAM_FORMAT);
      }
      String[] idAndCoordinate = coords[0].split(":");
      boundaryIds[i] = idAndCoordinate[0];
      // extract the coordinate
      boundaryParamValues.add(idAndCoordinate[1]);
      boundaryParamValues.add(coords[1]);
      // extract the radius
      boundaryParamValues.add(coords[2]);
    }
    checkBoundaryParamLength(boundaryParamValues, BoundaryType.BCIRCLES);
    return boundaryParamValues;
  }

  /**
   * Splits the ids and the coordinates from the given bounding polygons array.
   *
   * @param bpolysArray contains the bounding polygons having a custom id
   * @return <code>List</code> containing the splitted bounding polygons
   * @throws BadRequestException if the bpolys have invalid format
   */
  public List<String> splitBpolysWithIds(String[] bpolysArray) {
    List<String> boundaryParamValues = new ArrayList<String>();
    for (int i = 0; i < bpolysArray.length; i++) {
      String[] coords = bpolysArray[i].split(",");
      String[] idAndCoordinate = coords[0].split(":");
      // extract the id and the first coordinate
      boundaryIds[i] = idAndCoordinate[0];
      boundaryParamValues.add(idAndCoordinate[1]);
      // extract the other coordinates
      for (int j = 1; j < coords.length; j++) {
        if (coords[j].contains(":")) {
          throw new BadRequestException(ExceptionMessages.BOUNDARY_PARAM_FORMAT);
        }
        boundaryParamValues.add(coords[j]);
      }
    }
    checkBoundaryParamLength(boundaryParamValues, BoundaryType.BPOLYS);
    return boundaryParamValues;
  }

  /**
   * Checks the given boundaries list on their length. Bounding box and polygon list must be even,
   * bounding circle list must be divisable by three.
   *
   * @param boundaries parameter to check the length
   * @throws BadRequestException if the length is not even or divisible by three
   */
  public void checkBoundaryParamLength(List<String> boundaries, BoundaryType boundaryType) {
    if ((boundaryType.equals(BoundaryType.BBOXES) || boundaryType.equals(BoundaryType.BPOLYS))
        && boundaries.size() % 2 != 0) {
      throw new BadRequestException(ExceptionMessages.BOUNDARY_PARAM_FORMAT);
    }
    if (boundaryType.equals(BoundaryType.BCIRCLES) && boundaries.size() % 3 != 0) {
      throw new BadRequestException(ExceptionMessages.BOUNDARY_PARAM_FORMAT);
    }
  }

  public Object[] getBoundaryIds() {
    return boundaryIds;
  }

  public void setBoundaryIds(Serializable[] boundaryIds) {
    this.boundaryIds = boundaryIds;
  }
}