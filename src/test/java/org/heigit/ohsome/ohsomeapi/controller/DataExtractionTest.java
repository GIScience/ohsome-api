package org.heigit.ohsome.ohsomeapi.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.heigit.ohsome.ohsomeapi.Application;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/** Test class for the data extraction requests. */
public class DataExtractionTest {

  private static String port = TestProperties.PORT3;
  private String server = TestProperties.SERVER;

  /** Starts this application context. */
  @BeforeClass
  public static void applicationMainStartup() {
    assumeTrue(TestProperties.PORT3 != null && (TestProperties.INTEGRATION == null
        || !TestProperties.INTEGRATION.equalsIgnoreCase("no")));
    List<String> params = new LinkedList<>();
    params.add("--port=" + port);
    params.addAll(Arrays.asList(TestProperties.DB_FILE_PATH_PROPERTY.split(" ")));
    // this instance gets reused by all of the following @Test methods
    Application.main(params.toArray(new String[0]));
  }

  /** Stops this application context. */
  @AfterClass
  public static void applicationMainShutdown() {
    if (null != Application.getApplicationContext()) {
      SpringApplication.exit(Application.getApplicationContext(), () -> 0);
    }
  }

  /*
   * ./elements/geometry tests
   */

  @Test
  public void elementsGeometryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/geometry?bboxes=8.67452,49.40961,8.70392,49.41823&types=way"
            + "&keys=building&values=residential&time=2015-01-01&properties=metadata",
        JsonNode.class);
    JsonNode feature = Helper.getFeatureByIdentifier(response, "@osmId", "way/140112811");
    assertEquals(7, feature.get("properties").size());
  }

  @Test
  public void elementsGeomUsingOneTagTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/geometry?bboxes=8.67452,49.40961,8.70392,49.41823&types=way&keys=building"
        + "&values=residential&time=2015-12-01&properties=metadata", JsonNode.class);
    assertTrue(Helper.getFeatureByIdentifier(response, "@osmId", "way/140112811") != null);
  }

  @Test
  public void elementsGeomUsingMultipleTagsTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port
            + "/elements/geometry?bboxes=8.67559,49.40853,8.69379,49.4231&types=way&keys=highway,"
            + "name,maxspeed&values=residential&time=2015-10-01&properties=metadata",
        JsonNode.class);
    assertTrue(Helper.getFeatureByIdentifier(response, "@osmId", "way/4084860") != null);
  }

  @Test
  public void elementsGeomUnclippedSimpleFeaturesTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port
            + "/elements/geometry?bboxes=8.700582,49.4143039,8.701247,49.414994&types=other,line&"
            + "keys=building&showMetadata=true&properties=unclipped&time=2019-01-02",
        JsonNode.class);
    assertTrue(response.getBody().get("features").size() == 0);
  }

  @Test
  public void elementsGeomSimpleFeaturesOtherLineTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/geometry?bboxes=8.700582,49.4143039,8.701247,49.414994&types=other,line&"
        + "keys=building&showMetadata=true&time=2019-01-02", JsonNode.class);
    assertTrue("GeometryCollection"
        .equals(response.getBody().get("features").get(0).get("geometry").get("type").asText()));
  }

  @Test
  public void elementsGeomUsingNoTagsTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.67452,49.40961,8.70392,49.41823");
    map.add("types", "node");
    map.add("time", "2016-02-05");
    map.add("properties", "metadata");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/geometry", map, JsonNode.class);
    assertTrue(Helper.getFeatureByIdentifier(response, "@osmId", "node/135742850") != null);
  }

  @Test
  public void elementsBboxTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/bbox?bboxes=8.67452,49.40961,8.70392,49.41823&types=way"
            + "&keys=building&values=residential&time=2015-01-01&properties=metadata",
        JsonNode.class);
    JsonNode featureGeom =
        Helper.getFeatureByIdentifier(response, "@osmId", "way/294644468").get("geometry");
    assertEquals("Polygon", featureGeom.get("type").asText());
    assertEquals(5, featureGeom.get("coordinates").get(0).size());
  }

  @Test
  public void elementsCentroidTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/centroid?bboxes=8.67452,49.40961,8.70392,49.41823&types=way"
            + "&keys=building&values=residential&time=2015-01-01&properties=metadata",
        JsonNode.class);
    assertEquals(2, Helper.getFeatureByIdentifier(response, "@osmId", "way/294644468")
        .get("geometry").get("coordinates").size());
  }

  @Test
  public void elementsClipGeometryParamTrueFalseTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    String uri = "/elements/geometry?bboxes=8.700582,49.4143039,8.701247,49.414994&types=other,"
        + "line&keys=building&showMetadata=true&time=2018-01-02";
    ResponseEntity<JsonNode> emptyFeatureResponse =
        restTemplate.getForEntity(server + port + uri + "&clipGeometry=false", JsonNode.class);
    ResponseEntity<JsonNode> featureResponse =
        restTemplate.getForEntity(server + port + uri + "&clipGeometry=true", JsonNode.class);
    assertTrue(emptyFeatureResponse.getBody().get("features").size() == 0);
    assertTrue(featureResponse.getBody().get("features").size() == 1);
  }

  /*
   * ./elementsFullHistory/geometry|bbox|centroid tests
   */

  @Test
  public void elementsFullHistoryGeometryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elementsFullHistory/geometry?bboxes=8.67452,49.40961,8.70392,49.41823&"
            + "types=way&keys=building&values=residential&properties=metadata&time=2015-01-01,"
            + "2015-07-01&showMetadata=true",
        JsonNode.class);
    assertTrue(Helper.getFeatureByIdentifier(response, "@osmId", "way/295135436") != null);
    assertEquals(7, Helper.getFeatureByIdentifier(response, "@validTo", "2015-05-05T06:59:35Z")
        .get("properties").size());
  }

  @Test
  public void elementsFullHistoryGeometryWithTagsTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elementsFullHistory/geometry?bboxes=8.67494,49.417032,8.676136,49.419576&"
            + "types=way&keys=brand&values=Aldi Süd&properties=tags&time=2017-01-01,2018-01-01&"
            + "showMetadata=true",
        JsonNode.class);
    assertTrue(
        Helper.getFeatureByIdentifier(response, "@validFrom", "2017-01-18T17:38:06Z") != null);
    assertEquals(13, Helper.getFeatureByIdentifier(response, "@validTo", "2017-03-03T18:51:20Z")
        .get("properties").size());
  }

  @Test
  public void elementsFullHistoryBboxTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.67494,49.417032,8.676136,49.419576");
    map.add("types", "way");
    map.add("keys", "brand");
    map.add("values", "Aldi Süd");
    map.add("time", "2017-01-01,2018-01-01");
    map.add("properties", "tags,metadata");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elementsFullHistory/bbox", map, JsonNode.class);
    assertTrue(
        Helper.getFeatureByIdentifier(response, "@validFrom", "2017-01-01T00:00:00Z") != null);
    assertEquals(16, Helper.getFeatureByIdentifier(response, "@changesetId", "43971880")
        .get("properties").size());
  }

  @Test
  public void elementsFullHistoryCentroidTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.67452,49.40961,8.70392,49.41823");
    map.add("types", "way");
    map.add("time", "2017-01-01,2017-07-01");
    map.add("keys", "building");
    map.add("values", "residential");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elementsFullHistory/centroid", map, JsonNode.class);
    assertEquals(4, Helper.getFeatureByIdentifier(response, "@osmId", "way/295135455")
        .get("properties").size());
  }

  /*
   * filter tests
   */

  @Test
  public void getElementsFilterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/bbox?bboxes=8.684692,49.407669,8.688061,49.410310&time=2016-01-01,2017-01-01"
        + "&filter=service=* and name!=*&properties=tags", JsonNode.class);
    assertEquals(6, Helper.getFeatureByIdentifier(response, "@osmId", "way/225890568")
        .get("properties").size());
  }

  @Test
  public void postElementsFilterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.684692,49.407669,8.688061,49.410310");
    map.add("time", "2012-01-01");
    map.add("filter", "oneway=yes");
    map.add("properties", "tags");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/bbox", map, JsonNode.class);
    assertTrue(Helper.getFeatureByIdentifier(response, "@osmId", "way/4403824").get("properties")
        .get("highway").asText().equalsIgnoreCase("tertiary"));
  }

  /*
   * false parameter tests
   */

  @Test
  public void getDataExtractionWithSpecificParameterOfOtherSpecificResourceTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate
        .getForEntity(server + port + "/elements/bbox?groupByKeys=building", JsonNode.class);
    assertEquals(400, response.getBody().get("status").asInt());
  }

  @Test
  public void postDataExtractionWithSpecificParameterOfOtherSpecificResourceTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("values2", "primary");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/geometry", map, JsonNode.class);
    assertEquals(400, response.getBody().get("status").asInt());
  }

  @Test
  public void fullHistoryDataExtractionWithFalseSpecificParameterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("propertie", "tags");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elementsFullHistory/geometry", map, JsonNode.class);
    assertEquals(400, response.getBody().get("status").asInt());
  }

  /*
   * ./contributions tests
   */

  @Test
  public void contributionsContributionTypesTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/contributions/geometry?bboxes=8.686017,49.406453,8.686983,49.406966&filter=building=*&"
        + "time=2008-01-01,2009-09-01&properties=metadata,tags&clipGeometry=false", JsonNode.class);
    JsonNode featuresArray = response.getBody().get("features");
    assertTrue(featuresArray.get(0).get("properties").get("@creation").asText().equals("true"));
    assertTrue(
        featuresArray.get(1).get("properties").get("@geometryChange").asText().equals("true"));
    assertTrue(featuresArray.get(2).get("properties").get("@tagChange").asText().equals("true"));
  }

  @Test
  public void contributionsTwoContributionTypesTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/contributions/geometry?bboxes=8.70328,49.411926,8.70564,49.413343&filter=building=*&"
        + "time=2010-01-01,2012-01-01&properties=metadata&clipGeometry=false", JsonNode.class);
    JsonNode featureProperties =
        Helper.getFeatureByIdentifier(response, "@changesetId", "10082609").get("properties");
    assertTrue(featureProperties.get("@geometryChange").asText().equals("true")
        && featureProperties.get("@tagChange").asText().equals("true"));
  }

  /*
   * ./contributions/latest tests
   */

  @Test
  public void contributionsLatestTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/contributions/latest/geometry?bboxes=8.687337,49.415067,8.687493,49.415172&filter="
        + "building=*&time=2010-01-01,2016-06-01&clipGeometry=false", JsonNode.class);
    assertTrue(response.getBody().get("features").get(0).get("properties").get("@timestamp")
        .asText().equals("2015-06-04T19:23:19Z"));
  }

}
