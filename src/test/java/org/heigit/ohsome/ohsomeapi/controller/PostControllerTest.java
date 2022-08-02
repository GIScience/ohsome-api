package org.heigit.ohsome.ohsomeapi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/** Test class for all of the controller classes sending POST requests. */
public class PostControllerTest {

  private static final String port = TestProperties.PORT2;
  private final String server = TestProperties.SERVER;
  private final double deltaPercentage = TestProperties.DELTA_PERCENTAGE;

  /** Starts this application context. */
  @BeforeAll
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
  @AfterAll
  public static void applicationMainShutdown() {
    if (Application.getApplicationContext()  != null) {
      SpringApplication.exit(Application.getApplicationContext(), () -> 0);
    }
  }

  /*
   * test geometries lying outside the underlying data-extract polygon
   */

  @Test
  public void providedBcirclesOutsideUnderlyingDataExtractPolygonTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    String message = "The provided boundary parameter "
        + "does not lie completely within the underlying data-extract polygon.";
    map.add("bcircles", "8.457261,49.488483,100");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/users/count", map, JsonNode.class);
    assertEquals(404, response.getBody().get("status").asInt());
    assertEquals(message, response.getBody().get("message").asText());
  }

  @Test
  public void providedBpolysOutsideUnderlyingDataExtractPolygonTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    String message = "The provided boundary parameter "
        + "does not lie completely within the underlying data-extract polygon.";
    map.add("bpolys", "8.422684,49.471910,8.422694,49.471980|8.426363,49.473583,8.426373,49.473593"
        + "|8.422684,49.471910,8.422694,49.471980");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/perimeter", map, JsonNode.class);
    assertEquals(404, response.getBody().get("status").asInt());
    assertEquals(message, response.getBody().get("message").asText());
  }

  /*
   * test request with invalid bpolys boundary
   */

  @Test
  public void oneCoordinatesPairTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bpolys", "8.65821,49.41129");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/count", map, JsonNode.class);
    assertEquals(400, response.getBody().get("status").asInt());
    assertEquals(ExceptionMessages.BPOLYS_FORMAT, response.getBody().get("message").asText());
  }

  @Test
  public void nonNodedLinestringsIntersectionTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bpolys", "8.695483,49.400794,8.696384,49.401269|8.674739,49.401869,8.681818,49.404774"
        + "|8.695483,49.400794,8.696384,49.401269");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/count", map, JsonNode.class);
    assertEquals(400, response.getBody().get("status").asInt());
    assertTrue(response.getBody().get("message").asText()
        .contains(ExceptionMessages.BPOLYS_PARAM_GEOMETRY));
  }

  /*
   * false parameter and no parameters tests
   */

  @Test
  public void queryWithoutParametersTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/perimeter", null, JsonNode.class);
    assertEquals(ExceptionMessages.NO_DEFINED_PARAMS, response.getBody().get("message").asText());
  }

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
    map.add("time", "2013-01-01/2016-01-01/P1Y");
    map.add("filter", "type:way and building=residential");
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
    map.add("time", "2016-01-01");
    map.add("filter", "type:way and building=*");
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
    map.add("time", "2013-01-01/2016-01-01/P1Y");
    map.add("filter", "geometry:point and building=*");
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
    map.add("time", "2018-01-01");
    map.add("filter", "(geometry:point or geometry:line) and highway=*");
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
    map.add("time", "2013-01-01/2016-01-01/P1Y");
    map.add("filter", "geometry:line and building=*");
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
    map.add("time", "2015-01-01/2019-01-01/P1Y");
    map.add("filter", "geometry:polygon and leisure=track");
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
    map.add("time", "2015-01-01/2019-01-01/P1Y");
    map.add("filter", "geometry:other and type=restriction");
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
    map.add("time", "2017-03-01");
    map.add("filter", "building=* and (geometry:polygon or geometry:point)");
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
    final double expectedValue = 572.95;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.69416,49.40969,8.71154,49.41161");
    map.add("time", "2013-01-01/2016-01-01/P1Y");
    map.add("filter", "type:way and building=residential");
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
    final double expectedValue = 2480.68;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "Weststadt:8.68081,49.39821,8.69528,49.40687|Neuenheim:8.67691,"
        + "49.41256,8.69304,49.42331");
    map.add("time", "2016-01-01");
    map.add("filter", "type:way and building=residential");
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
    final double expectedValue = 3057.18;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "Weststadt:8.68081,49.39821,8.69528,49.40687|Neuenheim:8.67691,"
        + "49.41256,8.69304,49.42331");
    map.add("time", "2016-07-01");
    map.add("filter", "type:way and building=*");
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
    final double expectedValue = 65402.39;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.68081,49.39821,8.69528,49.40687");
    map.add("time", "2016-01-01");
    map.add("filter", "(type:way or type:relation) and building=*");
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
    final double expectedValue = 65402.39;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.68081,49.39821,8.69528,49.40687");
    map.add("time", "2016-01-01");
    map.add("groupByKeys", "building,highway");
    map.add("filter", "type:way");
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
    final double expectedValue = 20555.77;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.68081,49.39821,8.69528,49.40687");
    map.add("time", "2015-01-01");
    map.add("groupByKey", "building");
    map.add("filter", "type:way");
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
    final double expectedValue = 0.015582;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.68081,49.39821,8.69528,49.40687");
    map.add("filter", "type:way and building=*");
    map.add("filter2", "type:relation and building=*");
    map.add("time", "2015-01-01");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elements/perimeter/ratio", map, JsonNode.class);
    assertEquals(expectedValue,
        response.getBody().get("ratioResult").get(0).get("ratio").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsPerimeterRatioGroupByBoundaryTest() {
    final double expectedValue = 0.008612;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "Weststadt:8.68081,49.39821,8.69528,49.40687|Neuenheim:8.67691,49.41256,"
        + "8.69304,49.42331");
    map.add("time", "2015-01-01");
    map.add("filter", "type:way and building=yes");
    map.add("filter2", "type:relation and building=yes");
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
    final double expectedValue = 2127.38;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.69416,49.40969,8.71154,49.41161");
    map.add("time", "2015-01-01");
    map.add("filter", "type:way and building=residential");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elements/perimeter/density", map, JsonNode.class);
    assertEquals(expectedValue, response.getBody().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsPerimeterDensityGroupByTypeTest() {
    final double expectedValue = 989.64;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.68081,49.39821,8.69528,49.40687");
    map.add("time", "2015-01-01");
    map.add("filter", "(type:way or type:relation) and building=*");
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
    final double expectedValue = 5066.28;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.68081,49.39821,8.69528,49.40687");
    map.add("time", "2015-01-01");
    map.add("filter", "type:way and building=*");
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
    final double expectedValue = 454.41;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "Weststadt:8.68081,49.39821,8.69528,49.40687|Neuenheim:8.67691,49.41256,"
        + "8.69304,49.42331");
    map.add("time", "2015-01-01");
    map.add("filter", "type:way and building=residential");
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
    final double expectedValue = 93.61;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "Weststadt:8.68081,49.39821,8.69528,49.40687");
    map.add("time", "2016-07-09");
    map.add("filter", "type:way and building=*");
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
    final double expectedValue1 = 77.9;
    final double expectedValue2 = 58.85;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.662714,49.413594,8.663337,49.414324");
    map.add("time", "2018-03-24");
    map.add("filter", "geometry:polygon");
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
    final double expectedValue = 1851.88;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.69416,49.40969,8.71154,49.41161");
    map.add("time", "2015-01-01");
    map.add("filter", "type:way and building=residential");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/area", map, JsonNode.class);
    assertEquals(expectedValue, response.getBody().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsAreaGroupByBoundaryTest() {
    final double expectedValue = 1867.8;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "Weststadt:8.68081,49.39821,8.69528,49.40687|Neuenheim:8.67691,49.41256,"
        + "8.69304,49.42331");
    map.add("time", "2015-01-01");
    map.add("filter", "type:way and building=residential");
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
    final double expectedValue = 641.72;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "b1:8.68081,49.39821,8.69528,49.40687");
    map.add("time", "2014-07-09");
    map.add("filter", "type:way and building=*");
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
    final double expectedValue = 16021.55;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.68081,49.39821,8.69528,49.40687");
    map.add("time", "2017-01-01");
    map.add("filter", "(type:way or type:relation) and building=*");
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
    final double expectedValue = 264762.38;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.68081,49.39821,8.69528,49.40687");
    map.add("filter", "type:way");
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
    final double expectedValue = 244873.26;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.68081,49.39821,8.69528,49.40687");
    map.add("filter", "type:way");
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
    final double expectedValue = 0.060513;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.68081,49.39821,8.69528,49.40687");
    map.add("time", "2017-01-01");
    map.add("filter", "type:way and building=*");
    map.add("filter2", "type:relation and building=*");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/area/ratio", map, JsonNode.class);
    assertEquals(expectedValue,
        response.getBody().get("ratioResult").get(0).get("ratio").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsAreaRatioGroupByBoundaryTest() {
    final double expectedValue = 0.060513;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "Neuenheim:8.67691,49.41256,8.69304,49.42331|"
        + "Weststadt:8.68081,49.39821,8.69528,49.40687");
    map.add("time", "2017-01-01");
    map.add("filter", "type:way and building=*");
    map.add("filter2", "type:relation and building=*");
    ResponseEntity<JsonNode> response = restTemplate.postForEntity(
        server + port + "/elements/area/ratio/groupBy/boundary", map, JsonNode.class);
    assertEquals(expectedValue, response.getBody().get("groupByBoundaryResult").get(1)
        .get("ratioResult").get(0).get("ratio").asDouble(), expectedValue * deltaPercentage);
  }

  @Test
  public void elementsAreaDensityTest() {
    final double expectedValue = 404281.85;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.69416,49.40969,8.71154,49.41161");
    map.add("time", "2017-01-01");
    map.add("filter", "type:way and building=yes");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/area/density", map, JsonNode.class);
    assertEquals(expectedValue, response.getBody().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsAreaDensityGroupByTypeTest() {
    final double expectedValue = 22225.47;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.69416,49.40969,8.71154,49.41161");
    map.add("time", "2017-01-01");
    map.add("filter", "building=* and (type:way or type:relation)");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elements/area/density/groupBy/type", map, JsonNode.class);
    assertEquals(expectedValue,
        response.getBody().get("groupByResult").get(2).get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsAreaDensityGroupByTagTest() {
    final double expectedValue = 404281.85;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.69416,49.40969,8.71154,49.41161");
    map.add("time", "2017-01-01");
    map.add("groupByKey", "building");
    map.add("filter", "type:way");
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
    final double expectedValue = 261743.56;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "Weststadt:8.68081,49.39821,8.69528,49.40687|Neuenheim:8.67691,49.41256,"
        + "8.69304,49.42331");
    map.add("time", "2017-01-01");
    map.add("filter", "type:way and building=*");
    ResponseEntity<JsonNode> response = restTemplate.postForEntity(
        server + port + "/elements/area/density/groupBy/boundary", map, JsonNode.class);
    assertEquals(expectedValue,
        response.getBody().get("groupByResult").get(0).get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsAreaDensityGroupByBoundaryGroupByTagTest() {
    final double expectedValue = 7568.03;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "b1:8.68081,49.39821,8.69528,49.40687");
    map.add("filter", "type:way");
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
    final double expectedValue = 1238.37;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.68815,49.41964,8.68983,49.42045");
    map.add("time", "2019-01-01");
    map.add("filter", "geometry:polygon and highway=pedestrian");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/area", map, JsonNode.class);
    assertEquals(expectedValue, response.getBody().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsAreaRatioSimpleFeaturePolygonTest() {
    final double expectedValue = 0.558477;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.679789,49.409088,8.680535,49.40943");
    map.add("time", "2018-12-01");
    map.add("filter", "geometry:polygon and leisure=swimming_pool");
    map.add("filter2", "geometry:polygon and name=Schwimmerbecken");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/area/ratio", map, JsonNode.class);
    assertEquals(expectedValue,
        response.getBody().get("ratioResult").get(0).get("ratio").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsAreaGroupByTagSimpleFeaturePolygonTest() {
    final double expectedValue = 4065.86;
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "b1:8.68287,49.36967,8.68465,49.37135");
    map.add("time", "2019-01-01");
    map.add("filter", "geometry:polygon");
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

  /*
   * csv output tests
   */

  @Test
  public void elementsLengthCsvTest() throws IOException {
    final double expectedValue = 378.39;
    // expect result to have 1 entry rows with 2 columns
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.67508,49.37834,8.67565,49.38026");
    map.add("time", "2019-01-11");
    map.add("format", "csv");
    map.add("filter", "type:way and railway=platform");
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
    final double expectedValue = 103070.01;
    // expect result to have 1 entry rows with 4 columns
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.687782,49.412861,8.687986,49.412945");
    map.add("time", "2017-08-04");
    map.add("groupByKey", "highway");
    map.add("groupByValues", "path,footway");
    map.add("format", "csv");
    map.add("filter", "type:way");
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
    final double expectedValue = 1.019428;
    // expect result to have 1 entry rows with 7 columns
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes",
        "8.672343,49.413675,8.673797,49.41395|" + "8.674157,49.413455,8.67465,49.413741");
    map.add("time", "2018-01-01");
    map.add("format", "csv");
    map.add("filter", "type:way and highway=unclassified");
    map.add("filter2", "type:way and highway=service");
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
    // expect result to have 1 entry rows with 4 columns
    final double expectedValue = 106.16;
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.675873,49.412488,8.676082,49.412701");
    map.add("time", "2018-01-01");
    map.add("format", "csv");
    map.add("filter", "(type:way or type:relation) and name=*");
    String responseBody = Helper.getPostResponseBody("/elements/length/groupBy/type", map);
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("relation")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void elementsLengthGroupByBoundaryGroupByTagSimpleFeatureCsvTest() throws IOException {
    // expect result to have 1 entry rows with 9 columns
    final double expectedValue = 226.58;
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "b1:8.69205,49.41164,8.69319,49.41287|b2:8.66785,49.40973,8.66868,49.41176");
    map.add("filter", "geometry:line");
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
    final double expectedValue = 662.23;
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.68855,49.40193,8.68979,49.40316");
    map.add("time", "2017-01-01");
    map.add("format", "csv");
    map.add("filter", "type:relation and building=hospital");
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
    final double expectedValue = 94.69;
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "Weststadt:8.68081,49.39821,8.69528,49.40687|Neuenheim:8.676699,"
        + "49.414781,8.678003,49.415371");
    map.add("time", "2016-07-01");
    map.add("groupByKey", "building");
    map.add("groupByValues", "house");
    map.add("format", "csv");
    map.add("filter", "type:way and building=*");
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
    final double expectedValue = 62501.18;
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bcircles", "8.67512, 49.40023,60|8.675659,49.39841,50");
    map.add("time", "2017-03-01");
    map.add("format", "csv");
    map.add("filter", "type:way and building=yes");
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
    final double expectedValue = 366.12;
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.685642,49.395621,8.687128,49.396528");
    map.add("filter", "type:way");
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
    final double expectedValue = 0.500029;
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes",
        "8.685642,49.396078,8.687192,49.396528|8.685744,49.395621,8.687294,49.396078");
    map.add("time", "2018-01-01");
    map.add("format", "csv");
    map.add("filter", "type:way and leisure=*");
    map.add("filter2", "leisure=pitch");
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
    final double expectedValue = 0.257533;
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bcircles", "b1:8.70167,49.38686,60|b2: 8.70231,49.38952,60");
    map.add("time", "2018-08-09");
    map.add("format", "csv");
    map.add("filter", "geometry:polygon and landuse=*");
    map.add("filter2", "geometry:polygon and landuse=meadow");
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
    final double expectedValue = 48.52;
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes",
        "b1:8.695003,49.399594,8.695421,49.399789|" + "b2:8.687788,49.402997,8.68856,49.403441");
    map.add("time", "2014-07-09");
    map.add("groupByKey", "building");
    map.add("groupByValues", "garage");
    map.add("format", "csv");
    map.add("filter", "type:way and building=*");
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
    // expect result to have 1 entry rows with 4 columns
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.688517,49.401936,8.68981,49.403168");
    map.add("time", "2019-01-01");
    map.add("format", "csv");
    map.add("filter", "(geometry:line or geometry:polygon) and wheelchair=*");
    String responseBody = Helper.getPostResponseBody("/elements/count/groupBy/type", map);
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(1.0, Double.parseDouble(records.get(0).get("relation")), 0.0);
    assertEquals(1.0, Double.parseDouble(records.get(0).get("way")), 0.0);
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
    final double expectedValue = 17514.13;
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
  public void postQueryRequestEndsByQuestionMarkTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/count?", map, JsonNode.class);
    assertEquals(null, response.getBody().get("error"));
  }

  /*
   * /contributions/count tests
   */

  @Test
  public void countContributionsToHeidelbergCastleTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.7137,49.40916,8.71694,49.41198");
    map.add("time", "2015-01-01,2019-01-01");
    map.add("filter", "id:way/254154168");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/contributions/count", map, JsonNode.class);
    assertEquals(16, response.getBody().get("result").get(0).get("value").asInt());
  }

  @Test
  public void countDensityOfContributionsToShopsInOldtownHeidelbergTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.69282,49.40766,8.71673,49.4133");
    map.add("time", "2018-01-01,2019-01-01");
    map.add("filter", "shop=* and type:node");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/contributions/count/density", map, JsonNode.class);
    assertEquals(85.45, response.getBody().get("result").get(0).get("value").asDouble(),
        deltaPercentage);
  }

  @Test
  public void postRequestNonUniqueParam() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.69282,49.40766,8.71673,49.4133");
    map.add("time", "2018-01-01,2019-01-01");
    map.add("filter", "type:node");
    map.add("filter", "type:way");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/contributions/count/density", map, JsonNode.class);
    assertEquals(400, response.getStatusCode().value());
  }
}
