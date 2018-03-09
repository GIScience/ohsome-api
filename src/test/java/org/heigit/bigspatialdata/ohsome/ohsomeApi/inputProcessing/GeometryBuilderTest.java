package org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing;

import static org.junit.Assert.*;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.exception.BadRequestException;
import org.junit.Before;
import org.junit.Test;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Test class for the
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.GeometryBuilder
 * GeometryBuilder} class.
 */
public class GeometryBuilderTest {

  private GeometryBuilder geomBuilder;

  @Before
  public void setup() {
    geomBuilder = new GeometryBuilder();
  }

  @Test(expected = BadRequestException.class)
  public void createBpolyWithWrongCoordinates() {
    String[] bpolys =
        new String[] {"85.2712", "27.6122", "85.5374", "27.6219", "85.5100", "27.6899", "85.3151",
            "27.7117", "85.2712", "asdfadfasdfasdf", "85.2163", "27.6753", "85.3865", "27.6559",
            "85.4798", "27.7433", "85.4263", "27.8197", "85.2492", "27.7906", "85.2163", "27.6753"};
    geomBuilder.createBpolys(bpolys);
  }

  @Test
  public void createOverlappingBpoly() {
    String[] bpolys =
        new String[] {"85.2712", "27.6122", "85.5374", "27.6219", "85.5100", "27.6899", "85.3151",
            "27.7117", "85.2712", "27.6122", "85.2163", "27.6753", "85.3865", "27.6559", "85.4798",
            "27.7433", "85.4263", "27.8197", "85.2492", "27.7906", "85.2163", "27.6753"};
    Geometry geom = geomBuilder.createBpolys(bpolys);
    geom = (Polygon) geom;
    assertTrue(geom.isSimple());
  }

  @Test
  public void createNonOverlappingBpoly() {
    String[] bpolys = new String[] {"85.2575", "27.5502", "85.3892", "27.5708", "85.3302",
        "27.6097", "85.2575", "27.5502", "85.2575", "27.7190", "85.3906", "27.66201", "85.2547",
        "27.6656", "85.2575", "27.7190"};
    Geometry geom = geomBuilder.createBpolys(bpolys);
    geom = (MultiPolygon) geom;
    assertTrue(geom.isSimple());
  }

}
