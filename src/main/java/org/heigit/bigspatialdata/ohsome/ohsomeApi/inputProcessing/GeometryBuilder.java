package org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import org.apache.commons.lang3.ArrayUtils;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.exception.BadRequestException;
import org.heigit.bigspatialdata.oshdb.util.OSHDBBoundingBox;
import org.heigit.bigspatialdata.oshdb.util.geometry.OSHDBGeometryBuilder;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Includes methods to create and manipulate geometries derived from the boundary input parameters.
 */
public class GeometryBuilder {

  // default bbox coordinates defining the whole area (here: BW)
  private final double defMinLon = 7.3949;
  private final double defMaxLon = 10.6139;
  private final double defMinLat = 47.3937;
  private final double defMaxLat = 49.9079;
  private OSHDBBoundingBox bbox;
  private Geometry bcircleGeom;
  private Polygon bpoly;
  private Collection<Geometry> bboxColl;
  private Collection<Geometry> bcircleColl;
  private Collection<Geometry> bpolyColl;

  /**
   * Creates a <code>BoundingBox</code> object out of the content of the given <code>String</code>
   * array. Only used if one or no bounding box is given.
   * 
   * @param bbox <code>String</code> array containing the lon/lat coordinates of the bounding box.
   *        It must consist of 2 lon/lat coordinate pairs (bottom-left and top-right).
   * @return <code>BoundingBox</code> object.
   * @throws BadRequestException if coordinates are invalid
   */
  public OSHDBBoundingBox createBbox(String[] bbox) throws BadRequestException {
    if (bbox.length == 0) {
      // no bboxes given -> global request
      this.bbox = new OSHDBBoundingBox(defMinLon, defMinLat, defMaxLon, defMaxLat);
      return this.bbox;
    } else if (bbox.length == 4) {
      try {
        double minLon = Double.parseDouble(bbox[0]);
        double minLat = Double.parseDouble(bbox[1]);
        double maxLon = Double.parseDouble(bbox[2]);
        double maxLat = Double.parseDouble(bbox[3]);
        this.bbox = new OSHDBBoundingBox(minLon, minLat, maxLon, maxLat);
        bboxColl = new LinkedHashSet<Geometry>();;
        bboxColl.add(OSHDBGeometryBuilder.getGeometry(this.bbox));
        return this.bbox;
      } catch (NumberFormatException e) {
        throw new BadRequestException(
            "Apart from the custom id, the bounding box must contain double-parseable values in the following order: minLon, minLat, maxLon, maxLat.");
      }
    } else {
      throw new BadRequestException(
          "Apart from the custom id, the bounding box must contain double-parseable values in the following order: minLon, minLat, maxLon, maxLat.");
    }
  }

  /**
   * Creates a unified <code>Geometry</code> object out of the content of the given
   * <code>String</code> array. Only used if more than one bounding box is given in the input array.
   * 
   * @param bboxes <code>String</code> array containing the lon/lat coordinates of the bounding
   *        boxes. Each bounding box must consist of 2 lon/lat coordinate pairs (bottom-left and
   *        top-right).
   * @return <code>Geometry</code> object representing the unified bounding boxes.
   * @throws BadRequestException if coordinates are invalid, or boundary does not intersect with
   *         underlying data polygon
   */
  public Geometry createBboxes(String[] bboxes) throws BadRequestException {

    Utils utils = new Utils();
    try {
      Geometry unifiedBbox;
      GeometryFactory gf = new GeometryFactory();
      double minLon = Double.parseDouble(bboxes[0]);
      double minLat = Double.parseDouble(bboxes[1]);
      double maxLon = Double.parseDouble(bboxes[2]);
      double maxLat = Double.parseDouble(bboxes[3]);
      this.bbox = new OSHDBBoundingBox(minLon, minLat, maxLon, maxLat);
      unifiedBbox = gf.createGeometry(OSHDBGeometryBuilder.getGeometry(this.bbox));
      bboxColl = new LinkedHashSet<Geometry>();;
      bboxColl.add(OSHDBGeometryBuilder.getGeometry(this.bbox));

      for (int i = 4; i < bboxes.length; i += 4) {
        minLon = Double.parseDouble(bboxes[i]);
        minLat = Double.parseDouble(bboxes[i + 1]);
        maxLon = Double.parseDouble(bboxes[i + 2]);
        maxLat = Double.parseDouble(bboxes[i + 3]);
        this.bbox = new OSHDBBoundingBox(minLon, minLat, maxLon, maxLat);
        bboxColl.add(OSHDBGeometryBuilder.getGeometry(this.bbox));
        unifiedBbox = unifiedBbox.union(OSHDBGeometryBuilder.getGeometry(this.bbox));
      }
      if (utils.isWithin(unifiedBbox) == false)
        throw new BadRequestException(
            "The provided boundary parameter does not lie completely within the underlying data-extract polygon.");
      return unifiedBbox;
    } catch (NumberFormatException e) {
      throw new BadRequestException(
          "Apart from the custom ids, the bboxeses array must contain double-parseable values in the following order: minLon, minLat, maxLon, maxLat.");
    }
  }

  /**
   * Creates a <code>Geometry</code> object around the coordinates of the given <code>String</code>
   * array.
   * 
   * @param bcircles <code>String</code> array containing the lon/lat coordinates of the point at
   *        [0] and [1] and the size of the buffer at [2].
   * @return <code>Geometry</code> object representing a circular polygon around the bounding point.
   * @throws BadRequestException if coordinates or radius are invalid
   */
  public Geometry createCircularPolygons(String[] bcircles) throws BadRequestException {
    GeometryFactory geomFact = new GeometryFactory();
    Geometry buffer;
    Geometry geom;
    CoordinateReferenceSystem sourceCRS;
    CoordinateReferenceSystem targetCRS;
    MathTransform transform = null;
    Collection<Geometry> geometryCollection = new LinkedHashSet<Geometry>();
    Utils utils = new Utils();
    try {
      for (int i = 0; i < bcircles.length; i += 3) {
        sourceCRS = CRS.decode("EPSG:4326", true);
        targetCRS = CRS.decode(
            utils.findEPSG(Double.parseDouble(bcircles[i]), Double.parseDouble(bcircles[i + 1])),
            true);
        transform = CRS.findMathTransform(sourceCRS, targetCRS, false);
        Point p = geomFact.createPoint(
            new Coordinate(Double.parseDouble(bcircles[i]), Double.parseDouble(bcircles[i + 1])));
        buffer = JTS.transform(p, transform).buffer(Double.parseDouble(bcircles[i + 2]));
        // transform back again
        transform = CRS.findMathTransform(targetCRS, sourceCRS, false);
        geom = JTS.transform(buffer, transform);
        bcircleGeom = geom;
        // returns this geometry if there was only one bcircle given
        if (bcircles.length == 3) {
          geometryCollection.add(geom);
          bcircleColl = geometryCollection;
          return geom;
        }
        geometryCollection.add(geom);
      }
      // set the geometryCollection to be accessible for /groupBy/boundary
      bcircleColl = geometryCollection;
      geometryCollection = unifyIntersectedPolys(geometryCollection);
      MultiPolygon combined = createMultiPolygon(geometryCollection);
      bcircleGeom = combined;
      return combined;
    } catch (FactoryException | MismatchedDimensionException | TransformException e) {
      throw new BadRequestException(
          "Each bcircle must consist of a lon/lat coordinate pair plus a buffer in meters.");
    }
  }

  /**
   * Creates a <code>Polygon</code> out of the coordinates in the given array. If more polygons are
   * given, a union of the polygons is applied and a <code>MultiPolygon</code> is created.
   * 
   * @param bpolys <code>String</code> array containing the lon/lat coordinates of the bounding
   *        polygon(s).
   * @return <code>Geometry</code> object representing a <code>Polygon</code> object, if only one
   *         polygon was given or a <code>MultiPolygon</code> object, if more than one were given.
   * @throws BadRequestException if coordinates are invalid
   */
  public Geometry createBpolys(String[] bpolys) throws BadRequestException {
    GeometryFactory geomFact = new GeometryFactory();
    ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
    // checks if the first and last coordinate pairs are the same (= only 1 polygon)
    if (bpolys[0].equals(bpolys[bpolys.length - 2])
        && bpolys[1].equals(bpolys[bpolys.length - 1])) {
      try {
        for (int i = 0; i < bpolys.length; i += 2) {
          coords.add(
              new Coordinate(Double.parseDouble(bpolys[i]), Double.parseDouble(bpolys[i + 1])));
        }
      } catch (NumberFormatException e) {
        throw new BadRequestException(
            "The bpolys parameter must contain double-parseable values in form of lon/lat coordinate pairs.");
      }
      // creates a polygon from the coordinates
      this.bpoly = geomFact.createPolygon((Coordinate[]) coords.toArray(new Coordinate[] {}));
      return this.bpoly;
    } else {
      Collection<Geometry> geometryCollection = new LinkedHashSet<Geometry>();
      Coordinate firstPoint;
      try {
        firstPoint = new Coordinate(Double.parseDouble(bpolys[0]), Double.parseDouble(bpolys[1]));
        coords.add(firstPoint);
        for (int i = 2; i < bpolys.length; i += 2) {
          if (firstPoint.x == Double.parseDouble(bpolys[i])
              && firstPoint.y == Double.parseDouble(bpolys[i + 1])) {
            Polygon poly;
            coords.add(
                new Coordinate(Double.parseDouble(bpolys[i]), Double.parseDouble(bpolys[i + 1])));
            poly = geomFact.createPolygon((Coordinate[]) coords.toArray(new Coordinate[] {}));
            geometryCollection.add(poly);
            coords.removeAll(coords);
            // if the end is reached
            if (i + 2 >= bpolys.length)
              break;
            firstPoint = new Coordinate(Double.parseDouble(bpolys[i + 2]),
                Double.parseDouble(bpolys[i + 3]));
            coords.add(firstPoint);
            i += 2;
          } else
            coords.add(
                new Coordinate(Double.parseDouble(bpolys[i]), Double.parseDouble(bpolys[i + 1])));
        }
        bpolyColl = geometryCollection;

        return geomFact
            .createGeometryCollection(geometryCollection.toArray(new Geometry[] {})).union();
      } catch (NumberFormatException e) {
        throw new BadRequestException(
            "The bpolys parameter must contain double-parseable values in form of lon/lat coordinate pairs.");
      }
    }
  }

  /**
   * Unifies polygons, which intersect with each other and adds the unified polygons to the
   * collection.
   * 
   * @param collection <code>Collection</code> that includes all polygons.
   * @return Collection that includes unified polygons created from intersected polygons and other
   *         polygons, which do not intersect with any other polygon.
   */
  private Collection<Geometry> unifyIntersectedPolys(Collection<Geometry> collection) {
    Geometry[] polys = collection.toArray(new Geometry[collection.size()]);
    for (int i = 0; i < polys.length - 1; i++) {
      for (int j = i + 1; j < polys.length; j++) {
        if (polys[i].intersects(polys[j])) {
          Geometry unionedPoly = polys[i].union(polys[j]);
          polys = ArrayUtils.remove(polys, i);
          polys = ArrayUtils.remove(polys, j - 1);
          polys = ArrayUtils.add(polys, unionedPoly);
        }
      }
    }
    collection = new LinkedHashSet<Geometry>(Arrays.asList(polys));
    return collection;
  }

  /**
   * Creates a <code>MultiPolygon</code> out of the polygons in the given <code>Collection</code>.
   * 
   * @param collection <code>Collection</code> that holds the polygons.
   * @return <code>MultiPolygon</code> object consisting of the given polygons.
   */
  public MultiPolygon createMultiPolygon(Collection<Geometry> collection) {
    Polygon p = null;
    MultiPolygon combined = null;
    for (Geometry g : collection) {
      if (p == null)
        p = (Polygon) g;
      else {
        if (combined == null)
          combined = (MultiPolygon) p.union((Polygon) g);
        else
          combined = (MultiPolygon) combined.union((Polygon) g);
      }
    }
    return combined;
  }

  /**
   * Gets the <code>Geometry</code> for each boundary object in the given <code>String</code> array.
   * 
   * @param type <code>String</code> defining the boundary type (bbox, bcircle, bpoly)
   * @return <code>ArrayList</code> containing the <code>Geometry</code> objects for each input
   *         boundary object sorted by the given order of the array.
   */
  public ArrayList<Geometry> getGeometry(BoundaryType type) {

    ArrayList<Geometry> geoms = new ArrayList<>();
    switch (type) {
      case BBOXES:
        geoms.addAll(bboxColl);
        break;
      case BCIRCLES:
        geoms.addAll(bcircleColl);
        break;
      case BPOLYS:
        geoms.addAll(bpolyColl);
        break;
      default:
        geoms = null;
        break;
    }
    return geoms;
  }

  public OSHDBBoundingBox getBbox() {
    return bbox;
  }

  public Geometry getbcircleGeom() {
    return bcircleGeom;
  }

  public Polygon getBpoly() {
    return bpoly;
  }

}
