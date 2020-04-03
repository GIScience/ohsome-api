package org.heigit.bigspatialdata.ohsome.ohsomeapi.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.Application;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/** Test class for the data extraction requests. */
public class ElementsControllerTest {

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
    SpringApplication.exit(Application.getApplicationContext(), () -> 0);
  }

  /*
   * ./elements/geometry tests
   */

  @Test
  public void getElementsGeometryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/geometry?bboxes=8.67452,49.40961,8.70392,49.41823&types=way"
            + "&keys=building&values=residential&time=2015-01-01&properties=metadata",
        JsonNode.class);
    assertTrue(StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("features").iterator(),
            Spliterator.ORDERED), false)
        .anyMatch(jsonNode -> jsonNode.get("properties").get("@osmId").asText()
            .equalsIgnoreCase("way/140112811")));
    assertEquals(7, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("features").iterator(),
            Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("properties").get("@osmId").asText()
            .equalsIgnoreCase("way/140112811"))
        .findFirst().get().get("properties").size());
  }

  @Test
  public void getElementsGeomUsingOneTagTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/geometry?bboxes=8.67452,49.40961,8.70392,49.41823&types=way&keys=building"
        + "&values=residential&time=2015-12-01&properties=metadata", JsonNode.class);
    assertTrue(StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("features").iterator(),
            Spliterator.ORDERED), false)
        .anyMatch(jsonNode -> jsonNode.get("properties").get("@osmId").asText()
            .equalsIgnoreCase("way/140112811")));
  }

  @Test
  public void getElementsGeomUsingMultipleTagsTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port
            + "/elements/geometry?bboxes=8.67559,49.40853,8.69379,49.4231&types=way&keys=highway,"
            + "name,maxspeed&values=residential&time=2015-10-01&properties=metadata",
        JsonNode.class);
    assertTrue(StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("features").iterator(),
            Spliterator.ORDERED), false)
        .anyMatch(jsonNode -> jsonNode.get("properties").get("@osmId").asText()
            .equalsIgnoreCase("way/4084860")));
  }

  @Test
  public void getElementsGeomUnclippedSimpleFeaturesTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port
            + "/elements/geometry?bboxes=8.700582,49.4143039,8.701247,49.414994&types=other,line&"
            + "keys=building&showMetadata=true&properties=unclipped&time=2019-01-02",
        JsonNode.class);
    assertTrue(response.getBody().get("features").size() == 0);
  }

  @Test
  public void getElementsGeomSimpleFeaturesOtherLineTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/geometry?bboxes=8.700582,49.4143039,8.701247,49.414994&types=other,line&"
        + "keys=building&showMetadata=true&time=2019-01-02", JsonNode.class);
    assertTrue("GeometryCollection"
        .equals(response.getBody().get("features").get(0).get("geometry").get("type").asText()));
  }

  @Test
  public void postElementsGeomUsingNoTagsTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.67452,49.40961,8.70392,49.41823");
    map.add("types", "node");
    map.add("time", "2016-02-05");
    map.add("properties", "metadata");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/geometry", map, JsonNode.class);
    assertTrue(StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("features").iterator(),
            Spliterator.ORDERED), false)
        .anyMatch(jsonNode -> jsonNode.get("properties").get("@osmId").asText()
            .equalsIgnoreCase("node/135742850")));
  }

  @Test
  public void getElementsBboxTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/bbox?bboxes=8.67452,49.40961,8.70392,49.41823&types=way"
            + "&keys=building&values=residential&time=2015-01-01&properties=metadata",
        JsonNode.class);
    assertEquals("Polygon", StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("features").iterator(),
            Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("properties").get("@osmId").asText()
            .equalsIgnoreCase("way/294644468"))
        .findFirst().get().get("geometry").get("type").asText());
    assertEquals(5, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("features").iterator(),
            Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("properties").get("@osmId").asText()
            .equalsIgnoreCase("way/294644468"))
        .findFirst().get().get("geometry").get("coordinates").get(0).size());
  }

  @Test
  public void getElementsCentroidTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/centroid?bboxes=8.67452,49.40961,8.70392,49.41823&types=way"
            + "&keys=building&values=residential&time=2015-01-01&properties=metadata",
        JsonNode.class);
    assertEquals(2, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("features").iterator(),
            Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("properties").get("@osmId").asText()
            .equalsIgnoreCase("way/294644468"))
        .findFirst().get().get("geometry").get("coordinates").size());
  }

  /*
   * ./elementsFullHistory/geometry|bbox|centroid tests
   */

  @Test
  public void getElementsFullHistoryGeometryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elementsFullHistory/geometry?bboxes=8.67452,49.40961,8.70392,49.41823&"
            + "types=way&keys=building&values=residential&properties=metadata&time=2015-01-01,"
            + "2015-07-01&showMetadata=true",
        JsonNode.class);
    assertTrue(StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("features").iterator(),
            Spliterator.ORDERED), false)
        .anyMatch(jsonNode -> jsonNode.get("properties").get("@osmId").asText()
            .equalsIgnoreCase("way/295135436")));
    assertEquals(7, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("features").iterator(),
            Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("properties").get("@validTo").asText()
            .equalsIgnoreCase("2015-05-05T06:59:35Z"))
        .findFirst().get().get("properties").size());
  }

  @Test
  public void getElementsFullHistoryGeometryWithTagsTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elementsFullHistory/geometry?bboxes=8.67494,49.417032,8.676136,49.419576&"
            + "types=way&keys=brand&values=Aldi Süd&properties=tags&time=2017-01-01,2018-01-01&"
            + "showMetadata=true",
        JsonNode.class);
    assertTrue(StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("features").iterator(),
            Spliterator.ORDERED), false)
        .anyMatch(jsonNode -> jsonNode.get("properties").get("@validFrom").asText()
            .equalsIgnoreCase("2017-01-18T17:38:06Z")));
    assertEquals(13, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("features").iterator(),
            Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("properties").get("@validTo").asText()
            .equalsIgnoreCase("2017-03-03T18:51:20Z"))
        .findFirst().get().get("properties").size());
  }

  @Test
  public void postElementsFullHistoryBboxTest() {
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
    assertTrue(StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("features").iterator(),
            Spliterator.ORDERED), false)
        .anyMatch(jsonNode -> jsonNode.get("properties").get("@changesetId").asText()
            .equalsIgnoreCase("43971880"))
        && StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("features").iterator(), Spliterator.ORDERED), false)
            .anyMatch(jsonNode -> jsonNode.get("properties").get("@validFrom").asText()
                .equalsIgnoreCase("2017-01-01T00:00:00Z")));
    assertEquals(16, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("features").iterator(),
            Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("properties").get("@changesetId").asText()
            .equalsIgnoreCase("43971880"))
        .findFirst().get().get("properties").size());
  }

  @Test
  public void postElementsFullHistoryCentroidTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.67452,49.40961,8.70392,49.41823");
    map.add("types", "way");
    map.add("time", "2017-01-01,2017-07-01");
    map.add("keys", "building");
    map.add("values", "residential");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elementsFullHistory/centroid", map, JsonNode.class);
    assertTrue(StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("features").iterator(),
            Spliterator.ORDERED), false)
        .anyMatch(jsonNode -> jsonNode.get("properties").get("@osmId").asText()
            .equalsIgnoreCase("way/295135455")));
    assertEquals(4, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("features").iterator(),
            Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("properties").get("@osmId").asText()
            .equalsIgnoreCase("way/295135455"))
        .findFirst().get().get("properties").size());
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
  public void postFullHistoryDataExtractionWithFalseSpecificParameterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("propertie", "tags");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elementsFullHistory/geometry", map, JsonNode.class);
    assertEquals(400, response.getBody().get("status").asInt());
  }
}
