package org.heigit.ohsome.ohsomeapi.inputprocessing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.heigit.ohsome.ohsomeapi.controller.TestProperties;
import org.heigit.ohsome.ohsomeapi.exception.BadRequestException;
import org.heigit.ohsome.ohsomeapi.geometrybuilders.BBoxBuilder;
import org.heigit.ohsome.ohsomeapi.geometrybuilders.BCircleBuilder;
import org.heigit.ohsome.ohsomeapi.geometrybuilders.BPolygonBuilder;
import org.heigit.ohsome.ohsomeapi.geometrybuilders.BPolygonFromGeoJSON;
import org.heigit.ohsome.ohsomeapi.geometrybuilders.GeometryOfOSHDBExtent;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test class for the {@link GeometryBuilder GeometryBuilderUtility} class.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class GeometryBuilderUtilityTest {

  @Autowired
  private BBoxBuilder bBoxBuilder;
  @Autowired
  private BCircleBuilder bCircleBuilder;
  @Autowired
  private BPolygonBuilder bPolygonBuilder;
  @Autowired
  private BPolygonFromGeoJSON bPolygonFromGeoJSON;
  private final GeometryOfOSHDBExtent geometryOfOSHDBExtent = new GeometryOfOSHDBExtent();

  private final boolean isSnapshot = true;
  private final String bboxes = "8.67,49.39,8.71,49.42";
  private final String bcircles = "8.6528,49.3683,1000|8.7294,49.4376,1000";
  private final String bpolys = "8.65821,49.41129,8.65821,49.41825,8.70053,49.41825,8.70053,"
      + "49.41129,8.65821,49.41129|8.67817,49.42147,8.67817,49.4342,8.70053,49.4342,8.70053,"
      + "49.42147,8.67817,49.42147";
  private final String[] time = {"2014-01-01,2017-01-01"};
  private final String format = "json";
  private final String showMetadata = "true";
  private final double timeout = 4000;
  private final String filter = "type:way and natural=*";

  /** Checks the value of the junit property. */
  @BeforeClass
  public static void checkJunitProperty() {
    assumeTrue(TestProperties.JUNIT == null || !TestProperties.JUNIT.equalsIgnoreCase("no"));
  }

  @Test(expected = BadRequestException.class)
  public void createPolygonWithWrongCoordinatesFromBboxes() {
    String[] coords = new String[] {"8.67452", "49.40961", "8.70392", "[invalid input]", "8.68302",
        "49.41044", "8.69722", "49.41639"};
    bBoxBuilder.create(coords);
  }

  @Test
  public void createOverlappingPolygonFromBboxes() {
    String[] coords = new String[] {"8.678", "49.4148", "8.68864", "49.41971", "8.68302",
        "49.41044", "8.69722", "49.41639"};
    Geometry geom = bBoxBuilder.create(coords);
    assertTrue(geom instanceof Polygon);
  }

  @Test
  public void createNonOverlappingMultiPolygonFromBboxes() {
    String[] coords = new String[] {"8.67976", "49.41767", "8.68817", "49.42214", "8.6974",
        "49.40882", "8.70722", "49.41276"};
    Geometry geom = bBoxBuilder.create(coords);
    assertTrue(geom instanceof MultiPolygon);
  }

  // bcircles tests

  @Test(expected = BadRequestException.class)
  public void createPolygonWithWrongCoordinatesFromBcircles() {
    String[] bcircles =
        new String[] {"8.68452", "49.41781", "[invalid input]", "8.68491", "49.4179", "117"};
    bCircleBuilder.create(bcircles);
  }

  @Test
  public void createOverlappingPolygonFromBcircles() {
    String[] bcircles = new String[] {"8.68452", "49.41781", "100", "8.68491", "49.4179", "100"};
    Geometry geom = bCircleBuilder.create(bcircles);
    assertTrue(geom instanceof Polygon);
  }

  @Test
  public void createNonOverlappingMultiPolygonFromBcircles() {
    String[] bcircles = new String[] {"8.68242", "49.42482", "100", "8.68692", "49.40449", "100"};
    Geometry geom = bCircleBuilder.create(bcircles);
    assertTrue(geom instanceof MultiPolygon);
    assertEquals(geom.getNumGeometries(), bcircles.length / 3);
  }

  // bpolys tests

  @Test(expected = BadRequestException.class)
  public void createPolygonWithWrongCoordinatesFromBpolys() {
    String[] bpolys = new String[] {"8.6974", "49.40882", "8.6974", "49.41276", "8.70722",
        "49.41276", "8.70722", "[invalid input]"};
    bCircleBuilder.create(bpolys);
  }

  @Test
  public void createOverlappingPolygonFromBpolys() {
    String[] bpolys = new String[] {"8.678", "49.4148", "8.678", "49.41971", "8.68864", "49.41971",
        "8.68864", "49.4148", "8.678", "49.4148", "8.678", "49.4147", "8.678", "49.41972",
        "8.68864", "49.41972", "8.68864", "49.4147", "8.678", "49.4147"};
    Geometry geom = bPolygonBuilder.create(bpolys);
    assertTrue(geom instanceof Polygon);
    assertTrue(((Polygon) geom).getExteriorRing().getNumPoints() < bpolys.length / 2);
  }

  @Test
  public void createNonOverlappingMultiPolygonFromBpolys() {
    String[] bpolys = new String[] {"8.678", "49.4148", "8.678", "49.41971", "8.68864", "49.41971",
        "8.678", "49.4148", "8.67229", "49.40343", "8.67229", "49.40804", "8.67941", "49.40804",
        "8.67229", "49.40343"};
    Geometry geom = bPolygonBuilder.create(bpolys);
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
        "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":"
            + "{\"id\":\"Neuenheim\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[[[8.68465,"
            + "49.41769]]]}}]}";
    bPolygonFromGeoJSON.create(geoJson);
  }

  @Test(expected = BadRequestException.class)
  public void createPolygonFromGeoJsonWithWrongFormat() {
    String geoJson =
        "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"MultiPolygon\",\"coordinates\":"
            + "[[[[8.2944706,49.4313443],[8.2943343,49.4311946],[8.2942287,49.4313051],"
            + "[8.2942929,49.4313993],[8.2940648,49.4314931],[8.2931602,49.4328216],"
            + "[8.2933214,49.4329125],[8.2936734,49.4330121],[8.2940745,49.4331354],"
            + "[8.2950478,49.4317345],[8.2944706,49.4313443]]]]}]}";
    bPolygonFromGeoJSON.create(geoJson);
  }

  @Test(expected = BadRequestException.class)
  public void createGeometryFromInvalidInputGeoJson() {
    String geoJson = "{\"type\": \"FeatureCollection\"}";
    bPolygonFromGeoJSON.create(geoJson);
  }

  @Test
  public void createPolygonFromGeoJson() {
    String geoJson =
        "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":"
            + "{\"id\":\"Neuenheim\"},\"geometry\":{\"type\":\"Polygon\","
            + "\"coordinates\":[[[8.65821,49.41318],[8.65821,49.42225],[8.70053,49.42225],"
            + "[8.70053,49.42225],[8.65821,49.41318]]]}},"
            + "{\"type\":\"Feature\",\"properties\":{\"id\":\"Weststadt\"},\"geometry\":{\"type\":"
            + "\"Polygon\",\"coordinates\":[[[8.6801,49.39874],[8.6801,49.40586],[8.69615,"
            + "49.40586],[8.69615,49.39874],[8.6801,49.39874]]]}}]}";
    Geometry geom = bPolygonFromGeoJSON.create(geoJson);
    assertTrue(geom instanceof MultiPolygon);
  }

  @Test
  public void createOverlappingPolygonFromGeoJson() {
    String geoJson =
        "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":"
            + "{\"id\":\"Neuenheim\"},\"geometry\":{\"type\":\"Polygon\","
            + "\"coordinates\":[[[8.65821,49.41318],[8.65821,49.42225],[8.70053,49.42225],"
            + "[8.70053,49.42225],[8.65821,49.41318]]]}},"
            + "{\"type\":\"Feature\",\"properties\":{\"id\":\"Handschuhsheim\"},\"geometry\":"
            + "{\"type\":\"Polygon\",\"coordinates\":[[[8.67817,49.42147],[8.67817,49.4342],"
            + "[8.70053,49.4342],[8.70053,49.42147],[8.67817,49.42147]]]}}]}";
    Geometry geom = bPolygonFromGeoJSON.create(geoJson);
    assertTrue(geom instanceof Polygon);
  }

  @Test
  public void createGeometryFromMetadataGeoJson() {
    String geoJson = "{\"type\":\"Polygon\",\"coordinates\":[[[87.9784,26.34136],[87.87828,26.425],"
        + "[84.60404,27.29723],[84.29493,27.35496],[84.11996,27.48018],[84.04862,27.41662],"
        + "[80.22741,28.71684],[80.03542,28.81103],[80.02361,28.92357],[80.11695,29.1246],"
        + "[80.2165,29.1634],[80.2152,29.23861],[80.26026,29.26786],[80.21384,29.45208],"
        + "[81.21925,30.05499],[81.23257,30.16875],[81.35785,30.22718],[81.39828,30.44943],"
        + "[82.23489,30.1641],[82.21737,30.10248],[82.57134,29.98702],[82.85296,29.7277],"
        + "[83.9004,29.35718],[84.10834,29.32062],[84.23132,29.24686],[84.20122,29.16883],"
        + "[88.03636,26.33809],[87.9784,26.34136]]]}";
    geometryOfOSHDBExtent.create(geoJson);
  }

  @Test(expected = RuntimeException.class)
  public void createGeometryFromWrongMetadataGeoJson() {
    String geoJson = "{\"type\":\"Polygon\",\"coordinates\":[Invalid-Input]}";
    geometryOfOSHDBExtent.create(geoJson);
  }
}
