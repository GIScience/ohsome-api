package org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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

  // bboxes tests

  @Test(expected = BadRequestException.class)
  public void createPolygonWithWrongCoordinatesFromBboxes() {

    String[] bcircles = new String[] {"85.21987", "27.7679", "85.4079", "27.8649", "85.3022",
        "27.7084", "[invalid input]", "27.8115"};
    geomBuilder.createBboxes(bcircles);
  }

  @Test
  public void createOverlappingPolygonFromBboxes() {

    String[] bcircles = new String[] {"85.21987", "27.7679", "85.4079", "27.8649", "85.3022",
        "27.7084", "85.4422", "27.8115"};
    Geometry geom = geomBuilder.createBboxes(bcircles);
    assertTrue(geom instanceof Polygon);
  }

  @Test
  public void createNonOverlappingMultiPolygonFromBboxes() {

    String[] bcircles = new String[] {"85.3118", "27.7472", "85.4312", "27.8334", "85.1855",
        "27.6538", "85.2841", "27.7771"};
    Geometry geom = geomBuilder.createBboxes(bcircles);
    assertTrue(geom instanceof MultiPolygon);
  }

  // bcircles tests

  @Test(expected = BadRequestException.class)
  public void createPolygonWithWrongCoordinatesFromBcircles() {

    String[] bcircles =
        new String[] {"85.3653", "27.8154", "[invalid input]", "85.1704", "27.7706", "8056"};
    geomBuilder.createCircularPolygons(bcircles);
  }

  @Test
  public void createOverlappingPolygonFromBcircles() {

    String[] bcircles = new String[] {"85.3431", "27.7582", "6762", "85.2361", "27.6684", "11104"};
    Geometry geom = geomBuilder.createCircularPolygons(bcircles);
    assertTrue(geom instanceof Polygon);
  }

  @Test
  public void createNonOverlappingMultiPolygonFromBcircles() {

    String[] bcircles = new String[] {"85.3653", "27.8154", "5239", "85.1704", "27.7706", "8056"};
    Geometry geom = geomBuilder.createCircularPolygons(bcircles);
    assertTrue(geom instanceof MultiPolygon);
    assertEquals(geom.getNumGeometries(), bcircles.length / 3);
  }

  // bpolys tests

  @Test(expected = BadRequestException.class)
  public void createPolygonWithWrongCoordinatesFromBpolys() {

    String[] bpolys =
        new String[] {"85.2712", "27.6122", "85.5374", "27.6219", "85.5100", "27.6899", "85.3151",
            "27.7117", "85.2712", "[invalid input]", "85.2163", "27.6753", "85.3865", "27.6559",
            "85.4798", "27.7433", "85.4263", "27.8197", "85.2492", "27.7906", "85.2163", "27.6753"};
    geomBuilder.createBpolys(bpolys);
  }

  @Test
  public void createOverlappingPolygonFromBpolys() {

    String[] bpolys =
        new String[] {"85.2712", "27.6122", "85.5374", "27.6219", "85.5100", "27.6899", "85.3151",
            "27.7117", "85.2712", "27.6122", "85.2163", "27.6753", "85.3865", "27.6559", "85.4798",
            "27.7433", "85.4263", "27.8197", "85.2492", "27.7906", "85.2163", "27.6753"};
    Geometry geom = geomBuilder.createBpolys(bpolys);
    assertTrue(geom instanceof Polygon);
    assertTrue(((Polygon) geom).getExteriorRing().getNumPoints() < bpolys.length / 2);
  }

  @Test
  public void createNonOverlappingMultiPolygonFromBpolys() {

    String[] bpolys = new String[] {"85.2575", "27.5502", "85.3892", "27.5708", "85.3302",
        "27.6097", "85.2575", "27.5502", "85.2575", "27.7190", "85.3906", "27.66201", "85.2547",
        "27.6656", "85.2575", "27.7190"};
    Geometry geom = geomBuilder.createBpolys(bpolys);
    assertTrue(geom instanceof MultiPolygon);
    MultiPolygon mp = (MultiPolygon) geom;
    assertEquals(mp.getNumGeometries(), 2);
    assertEquals(mp.getGeometryN(0).getNumPoints() + mp.getGeometryN(1).getNumPoints(),
        bpolys.length / 2);
  }

  // geojson tests

  @Test(expected = BadRequestException.class)
  public void createPolygonFromGeoJsonWithWrongGeomType() {

    String geoJson =
        "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{\"id\":\"Kathmandu\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[[[85.2044677734375,27.907058371121995]]]}}]}";
    InputProcessor iP = new InputProcessor();
    geomBuilder.createGeometryFromGeoJson(geoJson, iP);
  }

  @Test
  public void createPolygonFromGeoJson() {

    String geoJson =
        "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{\"id\":\"Kathmandu\"},\"geometry\":{\"type\":\"Polygon\","
            + "\"coordinates\":[[[85.20447,27.907059],[84.94629,27.63974],[85.39673,27.420538],[85.6604,27.766191],[85.20447,27.907059]]]}},"
            + "{\"type\":\"Feature\",\"properties\":{\"id\":\"Pokhara\"},\"geometry\":{\"type\":\"Polygon\","
            + "\"coordinates\":[[[83.913574,28.343065],[83.78723,28.17856],[84.0564,27.974998],[84.28162,28.246328],[83.913574,28.343065]]]}}]}";
    InputProcessor iP = new InputProcessor();
    iP.setUtils(new Utils());
    Geometry geom = geomBuilder.createGeometryFromGeoJson(geoJson, iP);
    assertTrue(geom instanceof MultiPolygon);
  }

  @Test
  public void createOverlappingPolygonFromGeoJson() {

    String geoJson =
        "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\","
            + "\"coordinates\":[[[85.44617,27.999252],[84.418945,27.552113],[86.53931,27.449791],[85.44617,27.999252]]]}},"
            + "{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\","
            + "\"coordinates\":[[[84.89685,28.130127],[85.02869,27.152033],[86.484375,27.01509],[84.89685,28.130127]]]}}]}";
    InputProcessor iP = new InputProcessor();
    iP.setUtils(new Utils());
    Geometry geom = geomBuilder.createGeometryFromGeoJson(geoJson, iP);
    assertTrue(geom instanceof Polygon);
  }

}
