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

    String[] coords = new String[] {"8.67452", "49.40961", "8.70392", "[invalid input]", "8.68302", "49.41044", "8.69722", "49.41639"};
    geomBuilder.createBboxes(coords);
  }

  @Test
  public void createOverlappingPolygonFromBboxes() {

    String[] coords = new String[] {"8.678", "49.4148", "8.68864", "49.41971", "8.68302", "49.41044", "8.69722", "49.41639"};
    Geometry geom = geomBuilder.createBboxes(coords);
    assertTrue(geom instanceof Polygon);
  }

  @Test
  public void createNonOverlappingMultiPolygonFromBboxes() {

    String[] coords = new String[] {"8.67976", "49.41767", "8.68817", "49.42214", "8.6974", "49.40882", "8.70722", "49.41276"};
    Geometry geom = geomBuilder.createBboxes(coords);
    assertTrue(geom instanceof MultiPolygon);
  }

  // bcircles tests

  @Test(expected = BadRequestException.class)
  public void createPolygonWithWrongCoordinatesFromBcircles() {

    String[] bcircles =
        new String[] {"8.68452", "49.41781", "[invalid input]", "8.68491", "49.4179", "117"};
    geomBuilder.createCircularPolygons(bcircles);
  }

  @Test
  public void createOverlappingPolygonFromBcircles() {

    String[] bcircles = new String[] {"8.68452", "49.41781", "100", "8.68491", "49.4179", "100"};
    Geometry geom = geomBuilder.createCircularPolygons(bcircles);
    assertTrue(geom instanceof Polygon);
  }

  @Test
  public void createNonOverlappingMultiPolygonFromBcircles() {

    String[] bcircles = new String[] {"8.68242", "49.42482", "100", "8.68692", "49.40449", "100"};
    Geometry geom = geomBuilder.createCircularPolygons(bcircles);
    assertTrue(geom instanceof MultiPolygon);
    assertEquals(geom.getNumGeometries(), bcircles.length / 3);
  }

  // bpolys tests

  @Test(expected = BadRequestException.class)
  public void createPolygonWithWrongCoordinatesFromBpolys() {

    String[] bpolys =
        new String[] {"8.6974", "49.40882", "8.6974", "49.41276", "8.70722", "49.41276", "8.70722", "[invalid input]"};
    geomBuilder.createBpolys(bpolys);
  }

  @Test
  public void createOverlappingPolygonFromBpolys() {

    String[] bpolys =
        new String[] {"8.678", "49.4148", "8.678", "49.41971", "8.68864", "49.41971", "8.68864", "49.4148", "8.678", "49.4148", 
            "8.678", "49.4147", "8.678", "49.41972", "8.68864", "49.41972", "8.68864", "49.4147", "8.678", "49.4147"};
    Geometry geom = geomBuilder.createBpolys(bpolys);
    assertTrue(geom instanceof Polygon);
    assertTrue(((Polygon) geom).getExteriorRing().getNumPoints() < bpolys.length / 2);
  }

  @Test
  public void createNonOverlappingMultiPolygonFromBpolys() {

    String[] bpolys = new String[] {"8.678", "49.4148", "8.678", "49.41971", "8.68864", "49.41971", "8.678", "49.4148", 
        "8.67229", "49.40343", "8.67229", "49.40804", "8.67941", "49.40804", "8.67229", "49.40343"};
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
        "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{\"id\":\"Neuenheim\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[[[8.68465,49.41769]]]}}]}";
    InputProcessor iP = new InputProcessor();
    geomBuilder.createGeometryFromGeoJson(geoJson, iP);
  }

  @Test
  public void createPolygonFromGeoJson() {

    String geoJson =
        "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{\"id\":\"Neuenheim\"},\"geometry\":{\"type\":\"Polygon\","
            + "\"coordinates\":[[[8.65821,49.41318],[8.65821,49.42225],[8.70053,49.42225],[8.70053,49.42225],[8.65821,49.41318]]]}},"
            + "{\"type\":\"Feature\",\"properties\":{\"id\":\"Weststadt\"},\"geometry\":{\"type\":\"Polygon\","
            + "\"coordinates\":[[[8.6801,49.39874],[8.6801,49.40586],[8.69615,49.40586],[8.69615,49.39874],[8.6801,49.39874]]]}}]}";
    InputProcessor iP = new InputProcessor();
    iP.setUtils(new InputProcessingUtils());
    Geometry geom = geomBuilder.createGeometryFromGeoJson(geoJson, iP);
    assertTrue(geom instanceof MultiPolygon);
  }

  @Test
  public void createOverlappingPolygonFromGeoJson() {

    String geoJson =
        "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{\"id\":\"Neuenheim\"},\"geometry\":{\"type\":\"Polygon\","
            + "\"coordinates\":[[[8.65821,49.41318],[8.65821,49.42225],[8.70053,49.42225],[8.70053,49.42225],[8.65821,49.41318]]]}},"
            + "{\"type\":\"Feature\",\"properties\":{\"id\":\"Handschuhsheim\"},\"geometry\":{\"type\":\"Polygon\","
            + "\"coordinates\":[[[8.67817,49.42147],[8.67817,49.4342],[8.70053,49.4342],[8.70053,49.42147],[8.67817,49.42147]]]}}]}";
    InputProcessor iP = new InputProcessor();
    iP.setUtils(new InputProcessingUtils());
    Geometry geom = geomBuilder.createGeometryFromGeoJson(geoJson, iP);
    assertTrue(geom instanceof Polygon);
  }

}
