package org.heigit.ohsome.ohsomeapi.geometries;

import java.util.ArrayList;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.exception.NotFoundException;
import org.heigit.ohsome.ohsomeapi.utilities.SpatialUtility;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.opengis.geometry.MismatchedDimensionException;

public class BPolyBuilder extends GeometryBuilder implements OhsomePolygon{

  org.heigit.ohsome.ohsomeapi.inputprocessing.GeometryBuilder geometryBuilder;
  /**
   * Creates a <code>Polygon</code> out of the coordinates in the given array. If more polygons are
   * given, a union of the polygons is applied and a <code>MultiPolygon</code> is created.
   *
   * @param bpolys <code>String</code> array containing the lon/lat coordinates of the bounding
   *     polygon(s).
   * @return <code>Geometry</code> object representing a <code>Polygon</code> object, if only one
   *     polygon was given or a <code>MultiPolygon</code> object, if more than one were given.
   * @throws BadRequestException if bpolys coordinates are invalid
   */
  public Geometry create(String[] bpolys) {
    GeometryFactory geomFact = new GeometryFactory();
    Geometry bpoly;
    ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
    ArrayList<Geometry> geometryList = new ArrayList<Geometry>();
    SpatialUtility utils = new SpatialUtility();
    if (bpolys[0].equals(bpolys[bpolys.length - 2])
        && bpolys[1].equals(bpolys[bpolys.length - 1])) {
      try {
        for (int i = 0; i < bpolys.length; i += 2) {
          coords.add(
              new Coordinate(Double.parseDouble(bpolys[i]), Double.parseDouble(bpolys[i + 1])));
        }
        bpoly = geomFact.createPolygon(coords.toArray(new Coordinate[]{}));
      } catch (IllegalArgumentException e) {
        throw new BadRequestException(ExceptionMessages.BPOLYS_FORMAT);
      }
      if (!utils.isWithin(bpoly)) {
        throw new NotFoundException(ExceptionMessages.BOUNDARY_NOT_IN_DATA_EXTRACT);
      }
      geometryList.add(bpoly);
      geometryBuilder.getProcessingData().setBoundaryList(geometryList);
      geometryBuilder.getProcessingData().setRequestGeom(bpoly);
      return bpoly;
    }
    Coordinate firstPoint = null;
    try {
      for (int i = 0; i < bpolys.length; i += 2) {
        if (firstPoint != null && firstPoint.x == Double.parseDouble(bpolys[i])
            && firstPoint.y == Double.parseDouble(bpolys[i + 1])) {
          Polygon poly;
          coords.add(
              new Coordinate(Double.parseDouble(bpolys[i]), Double.parseDouble(bpolys[i + 1])));
          poly = geomFact.createPolygon(coords.toArray(new Coordinate[]{}));
          geometryList.add(poly);
          coords.clear();
          firstPoint = null;
        } else {
          Coordinate coord =
              new Coordinate(Double.parseDouble(bpolys[i]), Double.parseDouble(bpolys[i + 1]));
          coords.add(coord);
          if (firstPoint == null) {
            firstPoint = coord;
          }
        }
      }
      Geometry result = unifyPolys(geometryList);
      geometryBuilder.getProcessingData().setBoundaryList(geometryList);
      geometryBuilder.getProcessingData().setRequestGeom(result);
      return result;
    } catch (NumberFormatException | MismatchedDimensionException e) {
      throw new BadRequestException(ExceptionMessages.BPOLYS_FORMAT);
    }
  }
}
