package org.heigit.ohsome.ohsomeapi.geometrybuilders;

import java.util.ArrayList;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.exception.NotFoundException;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.ohsomeapi.utilities.SpatialUtility;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

public class BCircleBuilder extends GeometryBuilder implements GeometryFromCoordinates {
  org.heigit.ohsome.ohsomeapi.inputprocessing.GeometryBuilder geometryBuilder;
  /**
   * Creates a <code>Geometry</code> object around the coordinates of the given <code>String</code>
   * array.
   *
   * @param bpoints <code>String</code> array containing the lon/lat coordinates of the point at [0]
   *     and [1] and the size of the buffer at [2].
   * @return <code>Geometry</code> object representing (a) circular polygon(s) around the given
   *     bounding point(s).
   * @throws BadRequestException if bcircle coordinates or radius are invalid
   */
  public Geometry create(String[] bpoints, InputProcessor inputProcessor) {
    GeometryFactory geomFact = new GeometryFactory();
    Geometry buffer;
    Geometry geom;
    CoordinateReferenceSystem sourceCrs;
    CoordinateReferenceSystem targetCrs;
    MathTransform transform = null;
    ArrayList<Geometry> geometryList = new ArrayList<Geometry>();
    SpatialUtility utils = new SpatialUtility();
    try {
      for (int i = 0; i < bpoints.length; i += 3) {
        sourceCrs = CRS.decode("EPSG:4326", true);
        targetCrs = CRS.decode(
            utils.findEpsg(Double.parseDouble(bpoints[i]), Double.parseDouble(bpoints[i + 1])),
            true);
        transform = CRS.findMathTransform(sourceCrs, targetCrs, false);
        Point p = geomFact.createPoint(
            new Coordinate(Double.parseDouble(bpoints[i]), Double.parseDouble(bpoints[i + 1])));
        buffer = JTS.transform(p, transform).buffer(Double.parseDouble(bpoints[i + 2]));
        transform = CRS.findMathTransform(targetCrs, sourceCrs, false);
        geom = JTS.transform(buffer, transform);
        if (bpoints.length == 3) {
          if (!utils.isWithin(geom)) {
            throw new NotFoundException(ExceptionMessages.BOUNDARY_NOT_IN_DATA_EXTRACT);
          }
          geometryList.add(geom);
//          geometryBuilder = new org.heigit.ohsome.ohsomeapi.inputprocessing.GeometryBuilder(
//              inputProcessor.getProcessingData());
          inputProcessor.getProcessingData().setBoundaryList(geometryList);
          inputProcessor.getProcessingData().setRequestGeom(geom);
          return geom;
        }
        geometryList.add(geom);
      }
      Geometry result = unifyPolys(geometryList);
      inputProcessor.getProcessingData().setBoundaryList(geometryList);
      inputProcessor.getProcessingData().setRequestGeom(result);
      return result;
    } catch (NumberFormatException | FactoryException | MismatchedDimensionException
        | TransformException | ArrayIndexOutOfBoundsException e) {
      throw new BadRequestException(
          "Each bcircle must consist of a lon/lat coordinate pair plus a buffer in meters.");
    }
  }
}
