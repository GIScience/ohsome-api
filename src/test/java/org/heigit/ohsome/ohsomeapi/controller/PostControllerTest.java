package org.heigit.ohsome.ohsomeapi.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import org.apache.commons.csv.CSVRecord;
import org.heigit.ohsome.ohsomeapi.Application;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/** Test class for all of the controller classes sending POST requests. */
public class PostControllerTest {

  private static String port = TestProperties.PORT2;
  private String server = TestProperties.SERVER;
  private double deltaPercentage = TestProperties.DELTA_PERCENTAGE;

  /** Starts this application context. */
  @BeforeClass
  public static void applicationMainStartup() {
    assumeTrue(TestProperties.PORT2 != null && (TestProperties.INTEGRATION == null
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
   * false parameter tests
   */

  @Test
  public void postGeneralResourceWithFalseParameterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("forma", "json");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/count/density", map, JsonNode.class);
    assertEquals(400, response.getBody().get("status").asInt());
  }

  @Test
  public void postGeneralResourceWithSpecificParameterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("properties", "tags");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/perimeter", map, JsonNode.class);
    assertEquals(400, response.getBody().get("status").asInt());
  }

  @Test
  public void postSpecificResourceWithFalseSpecificParameterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("sgroupByKeys", "building");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/users/count/groupBy/key", map, JsonNode.class);
    assertEquals(400, response.getBody().get("status").asInt());
  }

  @Test
  public void postSpecificResourceWithFalseGeneralParameterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("tim", "2014-01-01%2F2017-01-01%2FP1Y");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elements/perimeter/groupBy/key", map, JsonNode.class);
    assertEquals(400, response.getBody().get("status").asInt());
  }

  @Test
  public void postSpecificResourceWithSpecificParameterOfOtherSpecificResourceTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("groupByKeys", "building");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/users/count/groupBy/tag", map, JsonNode.class);
    assertEquals(400, response.getBody().get("status").asInt());
  }

  /*
   * /elements/count tests
   */

  @Test
  public void elementsCountTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.67452,49.40961,8.70392,49.41823");
    map.add("types", "way");
    map.add("time", "2013-01-01/2016-01-01/P1Y");
    map.add("keys", "building");
    map.add("values", "residential");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/count", map, JsonNode.class);
    assertEquals(40, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("result").iterator(),
            Spliterator.ORDERED), false)
        .filter(
            jsonNode -> jsonNode.get("timestamp").asText().equalsIgnoreCase("2015-01-01T00:00:00Z"))
        .findFirst().get().get("value").asInt());
  }

  @Test
  public void elementsCountGroupByBoundaryGeoJsonTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bpolys",
        "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{},"
            + "\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[8.68494,49.41951],"
            + "[8.67902,49.41460],[8.69009,49.41527],[8.68494,49.41951]]]}},{\"type\":\"Feature\","
            + "\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[8.68812,"
            + "49.40466],[8.68091,49.40058],[8.69121,49.40069],[8.68812,49.40466]]]}}]}");
    map.add("types", "way");
    map.add("time", "2016-01-01");
    map.add("keys", "building");
    map.add("format", "geojson");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elements/count/groupBy/boundary", map, JsonNode.class);
    assertEquals(203, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("features").iterator(),
            Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("properties").get("groupByBoundaryId").asText()
            .equalsIgnoreCase("feature2"))
        .findFirst().get().get("properties").get("value").asInt());
  }

  @Test
  public void elementsCountSimpleFeaturePointTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.6475,49.4002,8.7057,49.4268");
    map.add("types", "point");
    map.add("time", "2013-01-01/2016-01-01/P1Y");
    map.add("keys", "building");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/count", map, JsonNode.class);
    assertEquals(64, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("result").iterator(),
            Spliterator.ORDERED), false)
        .filter(
            jsonNode -> jsonNode.get("timestamp").asText().equalsIgnoreCase("2015-01-01T00:00:00Z"))
        .findFirst().get().get("value").asInt());
  }

  @Test
  public void elementsCountSimpleFeaturePointLineTest() {
    // count SF types point and line in bbox, 2 bus_stops with key highway, 3 lines with key highway
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.663031,49.41513,8.663616,49.415451");
    map.add("types", "point,line");
    map.add("time", "2018-01-01");
    map.add("keys", "highway");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/count", map, JsonNode.class);
    assertEquals(5, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("result").iterator(),
            Spliterator.ORDERED), false)
        .filter(
            jsonNode -> jsonNode.get("timestamp").asText().equalsIgnoreCase("2018-01-01T00:00:00Z"))
        .findFirst().get().get("value").asInt());
  }

  @Test
  public void elementsCountSimpleFeatureLineTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.6519,49.3758,8.721,49.4301");
    map.add("types", "line");
    map.add("time", "2013-01-01/2016-01-01/P1Y");
    map.add("keys", "building");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/count", map, JsonNode.class);
    assertEquals(2, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("result").iterator(),
            Spliterator.ORDERED), false)
        .filter(
            jsonNode -> jsonNode.get("timestamp").asText().equalsIgnoreCase("2014-01-01T00:00:00Z"))
        .findFirst().get().get("value").asInt());
  }

  @Test
  public void elementsCountSimpleFeaturePolygonTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.6519,49.3758,8.721,49.4301");
    map.add("types", "polygon");
    map.add("time", "2015-01-01/2019-01-01/P1Y");
    map.add("keys", "leisure");
    map.add("values", "track");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/count", map, JsonNode.class);
    assertEquals(11, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("result").iterator(),
            Spliterator.ORDERED), false)
        .filter(
            jsonNode -> jsonNode.get("timestamp").asText().equalsIgnoreCase("2019-01-01T00:00:00Z"))
        .findFirst().get().get("value").asInt());
  }

  @Test
  public void elementsCountSimpleFeatureOtherTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.6519,49.3758,8.721,49.4301");
    map.add("types", "other");
    map.add("time", "2015-01-01/2019-01-01/P1Y");
    map.add("keys", "type");
    map.add("values", "restriction");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/count", map, JsonNode.class);
    assertEquals(246, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("result").iterator(),
            Spliterator.ORDERED), false)
        .filter(
            jsonNode -> jsonNode.get("timestamp").asText().equalsIgnoreCase("2018-01-01T00:00:00Z"))
        .findFirst().get().get("value").asInt());
  }

  @Test
  public void elementsCountGroupByTypeSimpleFeaturePointPolygonTest() {
    // check count of SF types, in bbox 2 points and 1 polygon (OSM type WAY)
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.690314,49.409546,8.690861,49.409752");
    map.add("types", "point,polygon");
    map.add("time", "2017-03-01");
    map.add("keys", "building");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elements/count/groupBy/type", map, JsonNode.class);
    assertEquals(2,
        response.getBody().get("groupByResult").get(0).get("result").get(0).get("value").asInt());
    assertEquals(1,
        response.getBody().get("groupByResult").get(1).get("result").get(0).get("value").asInt());
  }

  /*
   * /elements/perimeter tests
   */

  @Test
  public void elementsPerimeterTest() {
    double expectedValue = 572.95;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.69416,49.40969,8.71154,49.41161");
    map.add("types", "way");
    map.add("time", "2013-01-01/2016-01-01/P1Y");
    map.add("keys", "building");
    map.add("values", "residential");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/perimeter", map, JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("result").iterator(),
            Spliterator.ORDERED), false)
        .filter(
            jsonNode -> jsonNode.get("timestamp").asText().equalsIgnoreCase("2015-01-01T00:00:00Z"))
        .findFirst().get().get("value").asDouble(), expectedValue * deltaPercentage);
  }

  @Test
  public void elementsPerimeterGroupByBoundaryTest() {
    double expectedValue = 2480.68;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "Weststadt:8.68081,49.39821,8.69528,49.40687|Neuenheim:8.67691,"
        + "49.41256,8.69304,49.42331");
    map.add("types", "way");
    map.add("time", "2016-01-01");
    map.add("keys", "building");
    map.add("values", "residential");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elements/perimeter/groupBy/boundary", map, JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("Weststadt"))
        .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsPerimeterGroupByBoundaryGroupByTagTest() {
    double expectedValue = 3057.18;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "Weststadt:8.68081,49.39821,8.69528,49.40687|Neuenheim:8.67691,"
        + "49.41256,8.69304,49.42331");
    map.add("types", "way");
    map.add("time", "2016-07-01");
    map.add("keys", "building");
    map.add("groupByKey", "building");
    map.add("groupByValues", "residential,garage");
    ResponseEntity<JsonNode> response = restTemplate.postForEntity(
        server + port + "/elements/perimeter/groupBy/boundary/groupBy/tag", map, JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(
            jsonNode -> "Weststadt".equalsIgnoreCase(jsonNode.get("groupByObject").get(0).asText())
                && "building=residential"
                    .equalsIgnoreCase(jsonNode.get("groupByObject").get(1).asText()))
        .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsPerimeterGroupByTypeTest() {
    double expectedValue = 65402.39;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.68081,49.39821,8.69528,49.40687");
    map.add("types", "way,relation");
    map.add("time", "2016-01-01");
    map.add("keys", "building");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elements/perimeter/groupBy/type", map, JsonNode.class);
    assertEquals(expectedValue,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("way"))
            .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsPerimeterGroupByKeyTest() {
    double expectedValue = 65402.39;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.68081,49.39821,8.69528,49.40687");
    map.add("types", "way");
    map.add("time", "2016-01-01");
    map.add("groupByKeys", "building,highway");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elements/perimeter/groupBy/key", map, JsonNode.class);
    assertEquals(expectedValue,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("building"))
            .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsPerimeterGroupByTagTest() {
    double expectedValue = 20555.77;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.68081,49.39821,8.69528,49.40687");
    map.add("types", "way");
    map.add("time", "2015-01-01");
    map.add("groupByKey", "building");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elements/perimeter/groupBy/tag", map, JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("remainder"))
        .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsPerimeterRatioTest() {
    double expectedValue = 0.015582;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.68081,49.39821,8.69528,49.40687");
    map.add("types", "way");
    map.add("keys", "building");
    map.add("time", "2015-01-01");
    map.add("types2", "relation");
    map.add("keys2", "building");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elements/perimeter/ratio", map, JsonNode.class);
    assertEquals(expectedValue,
        response.getBody().get("ratioResult").get(0).get("ratio").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsPerimeterRatioGroupByBoundaryTest() {
    double expectedValue = 0.008612;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "Weststadt:8.68081,49.39821,8.69528,49.40687|Neuenheim:8.67691,49.41256,"
        + "8.69304,49.42331");
    map.add("types", "way");
    map.add("keys", "building");
    map.add("time", "2015-01-01");
    map.add("values", "yes");
    map.add("types2", "relation");
    map.add("keys2", "building");
    map.add("values2", "yes");
    ResponseEntity<JsonNode> response = restTemplate.postForEntity(
        server + port + "/elements/perimeter/ratio/groupBy/boundary", map, JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(
            Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByBoundaryResult").iterator(), Spliterator.ORDERED),
            false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("Neuenheim"))
        .findFirst().get().get("ratioResult").get(0).get("ratio").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsPerimeterDensityTest() {
    double expectedValue = 2127.38;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.69416,49.40969,8.71154,49.41161");
    map.add("types", "way");
    map.add("time", "2015-01-01");
    map.add("keys", "building");
    map.add("values", "residential");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elements/perimeter/density", map, JsonNode.class);
    assertEquals(expectedValue, response.getBody().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsPerimeterDensityGroupByTypeTest() {
    double expectedValue = 989.64;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.68081,49.39821,8.69528,49.40687");
    map.add("types", "way,relation");
    map.add("time", "2015-01-01");
    map.add("keys", "building");
    ResponseEntity<JsonNode> response = restTemplate.postForEntity(
        server + port + "/elements/perimeter/density/groupBy/type", map, JsonNode.class);
    assertEquals(expectedValue,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("relation"))
            .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsPerimeterDensityGroupByTagTest() {
    double expectedValue = 5066.28;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.68081,49.39821,8.69528,49.40687");
    map.add("types", "way");
    map.add("time", "2015-01-01");
    map.add("keys", "building");
    map.add("groupByKey", "building");
    map.add("groupByValues", "yes");
    ResponseEntity<JsonNode> response = restTemplate.postForEntity(
        server + port + "/elements/perimeter/density/groupBy/tag", map, JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("remainder"))
        .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsPerimeterDensityGroupByBoundaryTest() {
    double expectedValue = 454.41;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "Weststadt:8.68081,49.39821,8.69528,49.40687|Neuenheim:8.67691,49.41256,"
        + "8.69304,49.42331");
    map.add("types", "way");
    map.add("time", "2015-01-01");
    map.add("keys", "building");
    map.add("values", "residential");
    ResponseEntity<JsonNode> response = restTemplate.postForEntity(
        server + port + "/elements/perimeter/density/groupBy/boundary", map, JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("Neuenheim"))
        .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsPerimeterDensityGroupByBoundaryGroupByTagTest() {
    double expectedValue = 93.61;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "Weststadt:8.68081,49.39821,8.69528,49.40687");
    map.add("types", "way");
    map.add("time", "2016-07-09");
    map.add("keys", "building");
    map.add("groupByKey", "building");
    ResponseEntity<JsonNode> response = restTemplate.postForEntity(
        server + port + "/elements/perimeter/density/groupBy/boundary/groupBy/tag", map,
        JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(
            jsonNode -> "Weststadt".equalsIgnoreCase(jsonNode.get("groupByObject").get(0).asText())
                && "building=house".equalsIgnoreCase(jsonNode.get("groupByObject").get(1).asText()))
        .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
    assertEquals(10, response.getBody().get("groupByResult").size());
  }

  @Test
  public void elementsPerimeterGroupByKeySimpleFeaturePolygonTest() {
    double expectedValue1 = 77.9;
    double expectedValue2 = 58.85;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.662714,49.413594,8.663337,49.414324");
    map.add("types", "polygon");
    map.add("time", "2018-03-24");
    map.add("groupByKeys", "building,landuse");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elements/perimeter/groupBy/key", map, JsonNode.class);
    assertEquals(expectedValue1,
        response.getBody().get("groupByResult").get(1).get("result").get(0).get("value").asDouble(),
        expectedValue1 * deltaPercentage);
    assertEquals(expectedValue2,
        response.getBody().get("groupByResult").get(2).get("result").get(0).get("value").asDouble(),
        expectedValue2 * deltaPercentage);
  }

  /*
   * /elements/area tests
   */

  @Test
  public void elementsAreaTest() {
    double expectedValue = 1851.88;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.69416,49.40969,8.71154,49.41161");
    map.add("types", "way");
    map.add("time", "2015-01-01");
    map.add("keys", "building");
    map.add("values", "residential");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/area", map, JsonNode.class);
    assertEquals(expectedValue, response.getBody().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsAreaGroupByBoundaryTest() {
    double expectedValue = 1867.8;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "Weststadt:8.68081,49.39821,8.69528,49.40687|Neuenheim:8.67691,49.41256,"
        + "8.69304,49.42331");
    map.add("types", "way");
    map.add("time", "2015-01-01");
    map.add("keys", "building");
    map.add("values", "residential");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elements/area/groupBy/boundary", map, JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("Neuenheim"))
        .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsAreaGroupByBoundaryGroupByTagTest() {
    double expectedValue = 641.72;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "b1:8.68081,49.39821,8.69528,49.40687");
    map.add("types", "way");
    map.add("time", "2014-07-09");
    map.add("keys", "building");
    map.add("groupByKey", "building");
    map.add("groupByValues", "residential,garage");
    ResponseEntity<JsonNode> response = restTemplate.postForEntity(
        server + port + "/elements/area/groupBy/boundary/groupBy/tag", map, JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> "b1".equalsIgnoreCase(jsonNode.get("groupByObject").get(0).asText())
            && "building=garage".equalsIgnoreCase(jsonNode.get("groupByObject").get(1).asText()))
        .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
    assertEquals(3, response.getBody().get("groupByResult").size());
  }

  @Test
  public void elementsAreaGroupByTypeTest() {
    double expectedValue = 16021.55;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.68081,49.39821,8.69528,49.40687");
    map.add("types", "way,relation");
    map.add("time", "2017-01-01");
    map.add("keys", "building");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elements/area/groupBy/type", map, JsonNode.class);
    assertEquals(expectedValue,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("relation"))
            .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsAreaGroupByKeyTest() {
    double expectedValue = 264762.38;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.68081,49.39821,8.69528,49.40687");
    map.add("types", "way");
    map.add("time", "2017-01-01");
    map.add("groupByKeys", "building");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elements/area/groupBy/key", map, JsonNode.class);
    assertEquals(expectedValue,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("building"))
            .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsAreaGroupByTagTest() {
    double expectedValue = 244873.26;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.68081,49.39821,8.69528,49.40687");
    map.add("types", "way");
    map.add("time", "2017-01-01");
    map.add("groupByKey", "building");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elements/area/groupBy/tag", map, JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("building=yes"))
        .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsAreaRatioTest() {
    double expectedValue = 0.060513;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.68081,49.39821,8.69528,49.40687");
    map.add("types", "way");
    map.add("keys", "building");
    map.add("time", "2017-01-01");
    map.add("types2", "relation");
    map.add("keys2", "building");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/area/ratio", map, JsonNode.class);
    assertEquals(expectedValue,
        response.getBody().get("ratioResult").get(0).get("ratio").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsAreaRatioGroupByBoundaryTest() {
    double expectedValue = 0.060513;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "Neuenheim:8.67691,49.41256,8.69304,49.42331|"
        + "Weststadt:8.68081,49.39821,8.69528,49.40687");
    map.add("types", "way");
    map.add("keys", "building");
    map.add("time", "2017-01-01");
    map.add("types2", "relation");
    map.add("keys2", "building");
    ResponseEntity<JsonNode> response = restTemplate.postForEntity(
        server + port + "/elements/area/ratio/groupBy/boundary", map, JsonNode.class);
    assertEquals(expectedValue, response.getBody().get("groupByBoundaryResult").get(1)
        .get("ratioResult").get(0).get("ratio").asDouble(), expectedValue * deltaPercentage);
  }

  @Test
  public void elementsAreaDensityTest() {
    double expectedValue = 404281.85;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.69416,49.40969,8.71154,49.41161");
    map.add("types", "way");
    map.add("time", "2017-01-01");
    map.add("keys", "building");
    map.add("values", "yes");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/area/density", map, JsonNode.class);
    assertEquals(expectedValue, response.getBody().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsAreaDensityGroupByTypeTest() {
    double expectedValue = 22225.47;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.69416,49.40969,8.71154,49.41161");
    map.add("types", "way,relation");
    map.add("time", "2017-01-01");
    map.add("keys", "building");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elements/area/density/groupBy/type", map, JsonNode.class);
    assertEquals(expectedValue,
        response.getBody().get("groupByResult").get(1).get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsAreaDensityGroupByTagTest() {
    double expectedValue = 404281.85;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.69416,49.40969,8.71154,49.41161");
    map.add("types", "way");
    map.add("time", "2017-01-01");
    map.add("groupByKey", "building");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elements/area/density/groupBy/tag", map, JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("building=yes"))
        .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsAreaDensityGroupByBoundaryTest() {
    double expectedValue = 261743.56;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "Weststadt:8.68081,49.39821,8.69528,49.40687|Neuenheim:8.67691,49.41256,"
        + "8.69304,49.42331");
    map.add("types", "way");
    map.add("time", "2017-01-01");
    map.add("keys", "building");
    ResponseEntity<JsonNode> response = restTemplate.postForEntity(
        server + port + "/elements/area/density/groupBy/boundary", map, JsonNode.class);
    assertEquals(expectedValue,
        response.getBody().get("groupByResult").get(0).get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsAreaDensityGroupByBoundaryGroupByTagTest() {
    double expectedValue = 7568.03;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "b1:8.68081,49.39821,8.69528,49.40687");
    map.add("types", "way");
    map.add("time", "2014-07-09");
    map.add("groupByKey", "building");
    map.add("groupByValues", "residential,garage");
    ResponseEntity<JsonNode> response = restTemplate.postForEntity(
        server + port + "/elements/area/density/groupBy/boundary/groupBy/tag", map, JsonNode.class);
    assertEquals(expectedValue,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> "b1".equalsIgnoreCase(jsonNode.get("groupByObject").get(0).asText())
                && "building=residential"
                    .equalsIgnoreCase(jsonNode.get("groupByObject").get(1).asText()))
            .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
    assertEquals(3, response.getBody().get("groupByResult").size());
  }

  @Test
  public void elementsAreaSimpleFeaturePolygonTest() {
    double expectedValue = 1238.37;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.68815,49.41964,8.68983,49.42045");
    map.add("time", "2019-01-01");
    map.add("types", "polygon");
    map.add("keys", "highway");
    map.add("values", "pedestrian");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/area", map, JsonNode.class);
    assertEquals(expectedValue, response.getBody().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsAreaRatioSimpleFeaturePolygonTest() {
    double expectedValue = 0.558477;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.679789,49.409088,8.680535,49.40943");
    map.add("time", "2018-12-01");
    map.add("types", "polygon");
    map.add("types2", "polygon");
    map.add("keys", "leisure");
    map.add("keys2", "name");
    map.add("values", "swimming_pool");
    map.add("values2", "Schwimmerbecken");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/area/ratio", map, JsonNode.class);
    assertEquals(expectedValue,
        response.getBody().get("ratioResult").get(0).get("ratio").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsAreaGroupByTagSimpleFeaturePolygonTest() {
    double expectedValue = 4065.86;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "b1:8.68287,49.36967,8.68465,49.37135");
    map.add("time", "2019-01-01");
    map.add("types", "polygon");
    map.add("groupByKey", "leisure");
    map.add("groupByValues", "pitch,sports_centre");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elements/area/groupBy/tag", map, JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(
            jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("leisure=pitch"))
        .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  // csv output tests

  @Test
  public void elementsLengthCsvTest() throws IOException {
    double expectedValue = 378.39;
    // expect result to have 1 entry rows with 2 columns
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.67508,49.37834,8.67565,49.38026");
    map.add("types", "way");
    map.add("time", "2019-01-11");
    map.add("keys", "railway");
    map.add("values", "platform");
    map.add("format", "csv");
    String responseBody = Helper.getPostResponseBody("/elements/length", map);
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(2, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("value")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsLengthDensityGroupByTagCsvTest() throws IOException {
    double expectedValue = 103070.01;
    // expect result to have 1 entry rows with 4 columns
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.687782,49.412861,8.687986,49.412945");
    map.add("types", "way");
    map.add("time", "2017-08-04");
    map.add("groupByKey", "highway");
    map.add("groupByValues", "path,footway");
    map.add("format", "csv");
    String responseBody = Helper.getPostResponseBody("/elements/length/density/groupBy/tag", map);
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("highway=footway")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsLengthRatioGroupByBoundaryCsvTest() throws IOException {
    double expectedValue = 1.019428;
    // expect result to have 1 entry rows with 7 columns
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes",
        "8.672343,49.413675,8.673797,49.41395|" + "8.674157,49.413455,8.67465,49.413741");
    map.add("types", "way");
    map.add("types2", "way");
    map.add("time", "2018-01-01");
    map.add("keys", "highway");
    map.add("keys2", "highway");
    map.add("values", "unclassified");
    map.add("values2", "service");
    map.add("format", "csv");
    String responseBody =
        Helper.getPostResponseBody("/elements/length/ratio/" + "groupBy/boundary", map);
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(7, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("boundary1_ratio")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsLengthGroupByTypeCsvTest() throws IOException {
    // expect result to have 1 entry rows with 3 columns
    double expectedValue = 106.16;
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.675873,49.412488,8.676082,49.412701");
    map.add("types", "way,relation");
    map.add("time", "2018-01-01");
    map.add("keys", "name");
    map.add("format", "csv");
    String responseBody = Helper.getPostResponseBody("/elements/length/groupBy/type", map);
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(3, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("RELATION")),
        expectedValue * deltaPercentage);
  }


  @Test
  public void elementsLengthGroupByBoundaryGroupByTagSimpleFeatureCsvTest() throws IOException {
    // expect result to have 1 entry rows with 9 columns
    double expectedValue = 226.58;
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "b1:8.69205,49.41164,8.69319,49.41287|b2:8.66785,49.40973,8.66868,49.41176");
    map.add("types", "line");
    map.add("time", "2017-09-02");
    map.add("groupByKey", "highway");
    map.add("groupByValues", "path,primary,footway");
    map.add("format", "csv");
    String responseBody =
        Helper.getPostResponseBody("/elements/length/groupBy/boundary/" + "groupBy/tag", map);
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(9, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("b2_highway=footway")),
        expectedValue * deltaPercentage);
  }


  @Test
  public void elementsPerimeterCsvTest() throws IOException {
    // expect result to have 1 entry rows with 2 columns
    // testing perimeter of building with a hole
    double expectedValue = 662.23;
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.68855,49.40193,8.68979,49.40316");
    map.add("types", "relation");
    map.add("time", "2017-01-01");
    map.add("keys", "building");
    map.add("values", "hospital");
    map.add("format", "csv");
    String responseBody = Helper.getPostResponseBody("/elements/perimeter", map);
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(2, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("value")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsPerimeterGroupByBoundaryGroupByTagCsvTest() throws IOException {
    // expect result to have 1 entry rows with 5 columns
    double expectedValue = 94.69;
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "Weststadt:8.68081,49.39821,8.69528,49.40687|Neuenheim:8.676699,"
        + "49.414781,8.678003,49.415371");
    map.add("types", "way");
    map.add("time", "2016-07-01");
    map.add("keys", "building");
    map.add("groupByKey", "building");
    map.add("groupByValues", "house");
    map.add("format", "csv");
    String responseBody =
        Helper.getPostResponseBody("/elements/perimeter/" + "groupBy/boundary/groupBy/tag", map);
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(5, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("Weststadt_building=house")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsPerimeterDensityGroupByBoundaryCsvTest() throws IOException {
    // expect result to have 1 entry rows with 3 columns
    double expectedValue = 62501.18;
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bcircles", "8.67512, 49.40023,60|8.675659,49.39841,50");
    map.add("types", "way");
    map.add("time", "2017-03-01");
    map.add("keys", "building");
    map.add("values", "yes");
    map.add("format", "csv");
    String responseBody =
        Helper.getPostResponseBody("/elements/perimeter/density/" + "groupBy/boundary", map);
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(3, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("boundary2")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsPerimeterGroupByKeyCsvTest() throws IOException {
    // expect result to have 1 entry rows with 4 columns
    double expectedValue = 366.12;
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.685642,49.395621,8.687128,49.396528");
    map.add("types", "way");
    map.add("time", "2018-01-01");
    map.add("groupByKeys", "building,leisure");
    map.add("format", "csv");
    String responseBody = Helper.getPostResponseBody("/elements/perimeter/groupBy/key", map);
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("building")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsPerimeterRatioGroupByBoundaryCsvTest() throws IOException {
    // expect result to have 1 entry rows with 7 columns
    double expectedValue = 0.500029;
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes",
        "8.685642,49.396078,8.687192,49.396528|8.685744,49.395621,8.687294,49.396078");
    map.add("types", "way");
    map.add("time", "2018-01-01");
    map.add("keys", "leisure");
    map.add("keys2", "leisure");
    map.add("values2", "pitch");
    map.add("format", "csv");
    String responseBody =
        Helper.getPostResponseBody("/elements/perimeter/ratio/" + "groupBy/boundary", map);
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(7, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("boundary2_ratio")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsAreaRatioGroupByBoundarySimpleFeatureCsvTest() throws IOException {
    // expect result to have 1 entry rows with 7 columns
    double expectedValue = 0.257533;
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bcircles", "b1:8.70167,49.38686,60|b2: 8.70231,49.38952,60");
    map.add("types", "polygon");
    map.add("types2", "polygon");
    map.add("time", "2018-08-09");
    map.add("keys", "landuse");
    map.add("keys2", "landuse");
    map.add("values2", "meadow");
    map.add("format", "csv");
    String responseBody = Helper.getPostResponseBody("/elements/area/ratio/groupBy/boundary", map);
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(7, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("b1_ratio")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsAreaGroupByBoundaryGroupByTagCsvTest() throws IOException {
    // expect result to have 1 entry rows with 5 columns
    double expectedValue = 48.52;
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes",
        "b1:8.695003,49.399594,8.695421,49.399789|" + "b2:8.687788,49.402997,8.68856,49.403441");
    map.add("types", "way");
    map.add("time", "2014-07-09");
    map.add("keys", "building");
    map.add("groupByKey", "building");
    map.add("groupByValues", "garage");
    map.add("format", "csv");
    String responseBody =
        Helper.getPostResponseBody("/elements/area/" + "groupBy/boundary/groupBy/tag", map);
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(5, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("b1_building=garage")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsCountGroupByTypeSimpleFeatureCsvTest() throws IOException {
    // expect result to have 1 entry rows with 3 columns
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.688517,49.401936,8.68981,49.403168");
    map.add("types", "line,polygon");
    map.add("time", "2019-01-01");
    map.add("keys", "wheelchair");
    map.add("format", "csv");
    String responseBody = Helper.getPostResponseBody("/elements/count/groupBy/type", map);
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(3, headers.size());
    assertEquals(1.0, Double.parseDouble(records.get(0).get("RELATION")), 0.0);
    assertEquals(1.0, Double.parseDouble(records.get(0).get("WAY")), 0.0);
  }

  /*
   * filter tests
   */
  @Test
  public void postFilterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.684692,49.407669,8.688061,49.410310");
    map.add("time", "2014-01-01");
    map.add("filter", "highway=residential");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/count", map, JsonNode.class);
    assertEquals(8, response.getBody().get("result").get(0).get("value").asInt());
  }

  @Test
  public void postOrFilterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.684692,49.407669,8.688061,49.410310");
    map.add("time", "2014-01-01,2015-01-01");
    map.add("filter", "highway=residential or highway=service");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/users/count", map, JsonNode.class);
    assertEquals(4, response.getBody().get("result").get(0).get("value").asInt());
  }

  @Test
  public void postAndAllNotEqualsFilterTest() {
    double expectedValue = 17514.13;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.684692,49.407669,8.688061,49.410310");
    map.add("time", "2014-01-01");
    map.add("filter", "building=* and name!=*");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/area", map, JsonNode.class);
    assertEquals(expectedValue, response.getBody().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void postUmlautFilterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.690455,49.410615,8.691002,49.410416");
    map.add("filter", "name=\"Institut für Rechtsmedizin\"");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/count", map, JsonNode.class);
    assertEquals(1, response.getBody().get("result").get(0).get("value").asInt());
  }

  @Test
  public void postQueryRequestEndsByQuestionMark() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/count?", map, JsonNode.class);
    assertEquals(null, response.getBody().get("error"));
  }
}
