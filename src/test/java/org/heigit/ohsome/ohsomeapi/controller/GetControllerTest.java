package org.heigit.ohsome.ohsomeapi.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
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
import org.heigit.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

/** Test class for all of the controller classes sending GET requests. */
public class GetControllerTest {

  private static final String port = TestProperties.PORT1;
  private final String server = TestProperties.SERVER;
  private final double deltaPercentage = TestProperties.DELTA_PERCENTAGE;

  /** Starts this application context. */
  @BeforeClass
  public static void applicationMainStartup() {
    assumeTrue(TestProperties.PORT1 != null && (TestProperties.INTEGRATION == null
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
    if (Application.getApplicationContext() != null) {
      SpringApplication.exit(Application.getApplicationContext(), () -> 0);
    }
  }

  /** Method to get response body as String. */
  private String getResponseBody(String urlParams) {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response =
        restTemplate.getForEntity(server + port + urlParams, String.class);
    String responseBody = response.getBody();
    return responseBody;
  }

  /*
   * /metadata test
   */
  @Test
  public void getMetadataTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate.getForEntity(server + port + "/metadata", JsonNode.class);
    assertEquals("https://ohsome.org/copyrights",
        response.getBody().get("attribution").get("url").asText());
    assertEquals(ProcessingData.getTimeout(), response.getBody().get("timeout").asDouble(), 1e-3);
    assertEquals(JsonNodeType.OBJECT,
        response.getBody().get("extractRegion").get("spatialExtent").getNodeType());
    assertTrue(response.getBody().get("extractRegion").get("temporalExtent").isContainerNode());
    assertTrue(response.getBody().get("extractRegion").get("replicationSequenceNumber").isInt());
  }

  /*
   * False parameters tests
   */

  @Test
  public void getGeneralResourceWithFalseParameterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate
        .getForEntity(server + port + "/elements/area?filterr=type:way", JsonNode.class);
    assertEquals(400, response.getBody().get("status").asInt());
  }

  @Test
  public void getGeneralResourceWithSpecificParameterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate
        .getForEntity(server + port + "/elements/count/density?filter2=highway", JsonNode.class);
    assertEquals(400, response.getBody().get("status").asInt());
  }

  @Test
  public void getSpecificResourceWithFalseSpecificParameterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/count/groupBy/tag?groupByKe=building", JsonNode.class);
    assertEquals(400, response.getBody().get("status").asInt());
  }

  @Test
  public void getSpecificResourceWithFalseGeneralParameterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate
        .getForEntity(server + port + "/elements/area/groupBy/key?forma=json", JsonNode.class);
    assertEquals(400, response.getBody().get("status").asInt());
  }

  @Test
  public void getSpecificResourceWithSpecificParameterOfOtherSpecificResourceTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate
        .getForEntity(server + port + "/elements/count/ratio?properties=tags", JsonNode.class);
    assertEquals(400, response.getBody().get("status").asInt());
  }

  /*
   * false parameter /metadata test
   */

  @Test
  public void getMetadataParameterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate.getForEntity(server + port + "/metadata?groupByKey=highway", JsonNode.class);
    assertEquals(400, response.getBody().get("status").asInt());
  }

  /*
   * /elements/count tests
   */

  @Test
  public void getElementsCountTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/count?bboxes=8.67452,49.40961,8.70392,49.41823&time=2015-01-01&"
        + "filter=type:way and building=residential", JsonNode.class);
    assertEquals(40, response.getBody().get("result").get(0).get("value").asInt());
  }

  @Test
  public void getElementsCountGroupByBoundaryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/count/groupBy/boundary?bboxes=8.70538,49.40891,8.70832,49.41155|"
            + "8.68667,49.41353,8.68828,49.414&time=2017-01-01&showMetadata=true&"
            + "filter=geometry:polygon and building=church",
        JsonNode.class);
    assertEquals(2, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("boundary1"))
        .findFirst().get().get("result").get(0).get("value").asInt());
  }

  @Test
  public void getElementsCountGroupByBoundaryGroupByTagTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/count/groupBy/boundary/groupBy/tag?bboxes=8.68086,49.39948,8.69401,49.40609&"
        + "time=2016-11-09&groupByKey=building&groupByValues=yes&filter=type:way and building=*",
        JsonNode.class);
    assertEquals(43, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(
            jsonNode -> "boundary1".equalsIgnoreCase(jsonNode.get("groupByObject").get(0).asText())
                && "remainder".equalsIgnoreCase(jsonNode.get("groupByObject").get(1).asText()))
        .findFirst().get().get("result").get(0).get("value").asInt());
  }

  @Test
  public void getElementsCountGroupByTypeTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/count/groupBy/type?bboxes=8.67038,49.40341,8.69197,49.40873&"
            + "time=2017-01-01&filter=building=* and (type:way or type:relation)",
        JsonNode.class);
    assertEquals(967,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("way"))
            .findFirst().get().get("result").get(0).get("value").asInt());
  }

  @Test
  public void getElementsCountGroupByTagTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/count/groupBy/tag?bboxes=8.67859,49.41189,8.67964,49.41263&"
            + "time=2017-01-01&groupByKey=building&filter=building=* and type:way",
        JsonNode.class);
    assertEquals(8, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("building=yes"))
        .findFirst().get().get("result").get(0).get("value").asInt());
  }

  @Test
  public void getElementsCountGroupByKeyTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate.getForEntity(
            server + port + "/elements/count/groupBy/key?bboxes=8.67859,49.41189,8.67964,49.41263&"
                + "time=2012-01-01&groupByKeys=building&filter=type:way",
            JsonNode.class);
    assertEquals(7,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("building"))
            .findFirst().get().get("result").get(0).get("value").asInt());
  }

  @Test
  public void getElementsCountRatioTest() {
    final double expectedValue = 0.153933;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/count/ratio?bboxes=8.66004,49.41184,8.68481,49.42094&time="
            + "2015-01-01/2019-01-01/P1Y&filter=type:way and building=*&filter2=type:node and "
            + "addr:housenumber=*",
        JsonNode.class);
    assertEquals(expectedValue,
        response.getBody().get("ratioResult").get(0).get("ratio").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsCountRatioGroupByBoundaryTest() {
    final double expectedValue = 1.052632;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/count/ratio/groupBy/boundary?bcircles=8.66906,49.4167,100|"
            + "8.69013,49.40223,100&time=2017-09-20&filter=type:way and building=*&"
            + "filter2=type:node and addr:housenumber=*",
        JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(
            Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByBoundaryResult").iterator(), Spliterator.ORDERED),
            false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("boundary1"))
        .findFirst().get().get("ratioResult").get(0).get("ratio").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsCountDensityTest() {
    final double expectedValue = 3868.09;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/count/density?bboxes=8.68794,49.41434,8.69021,49.41585&"
            + "time=2017-08-11&filter=type:way and building=*",
        JsonNode.class);
    assertEquals(expectedValue, response.getBody().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsCountDensityGroupByBoundaryTest() {
    final double expectedValue = 333.76;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port
            + "/elements/count/density/groupBy/boundary?bboxes=8.68794,49.41434,8.69021,49.41585|"
            + "8.67933,49.40505,8.6824,49.40638&time=2017-08-19&filter=type:way and building=*",
        JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("boundary2"))
        .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsCountDensityGroupByTypeTest() {
    final double expectedValue = 890.76;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/count/density/groupBy/type?bboxes=8.68086,49.39948,8.69401,49.40609&"
        + "time=2016-11-09&filter=addr:housenumber=* and (type:way or type:node)", JsonNode.class);
    assertEquals(expectedValue,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("way"))
            .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsCountDensityGroupByTagTest() {
    final double expectedValue = 61.28;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/count/density/groupBy/tag?bboxes=8.68086,49.39948,8.69401,49.40609&"
        + "time=2016-11-09&groupByKey=building&groupByValues=yes&filter=type:way and building=*",
        JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("remainder"))
        .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsCountDensityGroupByBoundaryGroupByTagTest() {
    final double expectedValue = 2.82;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/count/density/groupBy/boundary/groupBy/tag?bboxes=b1:8.68086,49.39948,8.69401,"
        + "49.40609|b2:8.68081,49.39943,8.69408,49.40605&time=2016-11-09&groupByKey=building&"
        + "filter=type:way and building=*", JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> "b2".equalsIgnoreCase(jsonNode.get("groupByObject").get(0).asText())
            && "building=church".equalsIgnoreCase(jsonNode.get("groupByObject").get(1).asText()))
        .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  /*
   * /elements/length tests
   */

  @Test
  public void getElementsLengthTest() {
    final double expectedValue = 15198.89;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/length?bboxes=8.67452,49.40961,8.70392,49.41823"
            + "&time=2012-01-01&filter=type:way and highway=residential",
        JsonNode.class);
    assertEquals(expectedValue, response.getBody().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsLengthGroupByBoundaryTest() {
    final double expectedValue = 25.52;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port
            + "/elements/length/groupBy/boundary?bboxes=8.695443,49.408928,8.695636,49.409151|"
            + "8.699262,49.409451,8.701547,49.412205&time=2014-08-21&filter=type:way and highway=*",
        JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("boundary1"))
        .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsLengthGroupByBoundaryGroupByTagTest() {
    final double expectedValue = 672.24;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/length/groupBy/boundary/groupBy/tag?bboxes=8.68086,49.39948,8.69401,49.40609"
        + "&time=2017-11-25&groupByKey=highway&filter=type:way and highway=*", JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> "boundary1"
            .equalsIgnoreCase(jsonNode.get("groupByObject").get(0).asText())
            && "highway=secondary".equalsIgnoreCase(jsonNode.get("groupByObject").get(1).asText()))
        .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsLengthGroupByTypeTest() {
    final double expectedValue = 541.85;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/length/groupBy/type?bboxes=8.701665,49.408802,8.703999,49.409553"
            + "&time=2014-08-21&filter=highway=* and (type:way or type:relation)",
        JsonNode.class);
    assertEquals(expectedValue,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("way"))
            .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsLengthGroupByKeyTest() {
    final double expectedValue = 3139.77;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/length/groupBy/key?bboxes=8.67181,49.40434,8.67846,49.40878"
            + "&time=2016-08-21&groupByKeys=highway,railway&filter=type:way",
        JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("remainder"))
        .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsLengthGroupByTagTest() {
    final double expectedValue = 373.51;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/length/groupBy/tag?bboxes=8.70773,49.40832,8.71413,49.41092&time=2016-08-21"
        + "&groupByKey=highway&filter=type:way", JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("highway=path"))
        .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsLengthRatioTest() {
    final double expectedValue = 0.135294;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/length/ratio?bboxes=8.67567,49.40695,8.69434,49.40882"
            + "&time=2011-12-13&&filter=type:way and highway=*&filter2=railway=*",
        JsonNode.class);
    assertEquals(expectedValue,
        response.getBody().get("ratioResult").get(0).get("ratio").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsLengthRatioGroupByBoundaryTest() {
    final double expectedValue = 0.478598;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port + "/elements"
        + "/length/ratio/groupBy/boundary?bboxes=8.67829,49.39807,8.69061,49.40578|"
        + "8.68306,49.42407,8.68829,49.42711&time=2012-12-22&filter=type:way and highway=*"
        + "&filter2=railway=*",
        JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(
            Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByBoundaryResult").iterator(), Spliterator.ORDERED),
            false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("boundary1"))
        .findFirst().get().get("ratioResult").get(0).get("ratio").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsLengthDensityTest() {
    final double expectedValue = 28990.48;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/length/density?bboxes=8.70538,49.40464,8.71264,49.41042"
            + "&time=2013-01-04&filter=type:way and highway=*",
        JsonNode.class);
    assertEquals(expectedValue, response.getBody().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsLengthDensityGroupByTypeTest() {
    final double expectedValue = 47782.05;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/length/density/groupBy/type?bboxes=8.68242,49.40059,8.68732,49.4059"
        + "&time=2015-03-25&filter=type:way or type:node", JsonNode.class);
    assertEquals(expectedValue,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("way"))
            .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsLengthDensityGroupByTagTest() {
    final double expectedValue = 20460.96;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/length/density/groupBy/tag?bboxes=8.66972,49.40453,8.67564,49.4076"
        + "&time=2016-01-17&groupByKey=railway&filter=type:way", JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("railway=tram"))
        .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsLengthDensityGroupByBoundaryTest() {
    final double expectedValue = 48648.22;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port
            + "/elements/length/density/groupBy/boundary?bboxes=8.69079,49.40129,8.69238,49.40341|"
            + "8.67504,49.4119,8.67813,49.41668&time=2017-05-30&filter=type:way and highway=*",
        JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("boundary2"))
        .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsLengthDensityGroupByBoundaryGroupByTagTest() {
    final double expectedValue = 73.67;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/length/density/groupBy/boundary/groupBy/tag?bboxes=b1:8.68086,49.39948,8.69401"
        + ",49.40609|b2:8.68081,49.39943,8.69408,49.40605&time=2017-10-08&groupByKey=highway"
        + "&filter=type:way and highway=*", JsonNode.class);
    assertEquals(expectedValue,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> "b1".equalsIgnoreCase(jsonNode.get("groupByObject").get(0).asText())
                && "highway=steps".equalsIgnoreCase(jsonNode.get("groupByObject").get(1).asText()))
            .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  /*
   * /users tests
   */

  @Test
  public void getUsersCountTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/users/count?bboxes=8.67452,49.40961,8.70392,49.41823&"
            + "&time=2014-01-01,2015-01-01&filter=type:way and building=residential",
        JsonNode.class);
    assertEquals(5, response.getBody().get("result").get(0).get("value").asInt());
  }

  @Test
  public void getUsersCountGroupByTypeTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/users/count/groupBy/type?bboxes=8.67,49.39941,8.69545,49.4096"
            + "&time=2014-01-01,2015-01-01&filter=(type:way or type:relation) and building=*",
        JsonNode.class);
    assertEquals(30,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("way"))
            .findFirst().get().get("result").get(0).get("value").asInt());
  }

  @Test
  public void getUsersCountGroupByKeyTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/users/count/groupBy/key?bboxes=8.67,49.39941,8.69545,49.4096"
            + "&time=2014-01-01,2015-01-01&groupByKeys=building&filter=type:way",
        JsonNode.class);
    assertEquals(30,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("building"))
            .findFirst().get().get("result").get(0).get("value").asInt());
  }

  @Test
  public void getUsersCountGroupByTagTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/users/count/groupBy/tag?bboxes=8.67,49.39941,8.69545,49.4096"
            + "&time=2014-01-01,2015-01-01&groupByKey=building&filter=type:way",
        JsonNode.class);
    assertEquals(29, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("building=yes"))
        .findFirst().get().get("result").get(0).get("value").asInt());
  }

  @Test
  public void getUsersCountDensityTest() {
    final double expectedValue = 14.33;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/users/count/density?bboxes=8.67,49.39941,8.69545,49.4096"
            + "&time=2014-01-01,2015-01-01&filter=type:way and building=*",
        JsonNode.class);
    assertEquals(expectedValue, response.getBody().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getUsersCountDensityGroupByTypeTest() {
    final double expectedValue = 3.82;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/users/count/density/groupBy/type?bboxes=8.67,49.39941,8.69545,49.4096"
            + "&time=2014-01-01,2015-01-01&filter=(type:way or type:relation) and building=*",
        JsonNode.class);
    assertEquals(expectedValue,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("relation"))
            .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getUsersCountDensityGroupByTagTest() {
    final double expectedValue = 26.75;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/users/count/density/groupBy/tag?bboxes=8.67,49.39941,8.69545,49.4096"
        + "&time=2014-01-01,2015-01-01&groupByKey=building&filter=type:way", JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("remainder"))
        .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getUsersCountGroupByBoundaryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/users/count/groupBy/boundary?bboxes=a:8.67452,49.40961,8.70392,49.41823|"
        + "b:8.67,49.39941,8.69545,49.4096&time=2014-01-01,2015-01-01"
        + "&filter=building=* and type:way", JsonNode.class);
    assertEquals(29,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("a"))
            .findFirst().get().get("result").get(0).get("value").asInt());
    assertEquals(30,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("b"))
            .findFirst().get().get("result").get(0).get("value").asInt());
  }

  @Test
  public void getUsersCountDensityGroupByBoundaryTest() {
    final double expectedValue = 14.33;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/users/count/density/groupBy/boundary?bboxes=a:8.67452,49.40961,8.70392,49.41823|"
        + "b:8.67,49.39941,8.69545,49.4096&time=2014-01-01,2015-01-01"
        + "&filter=type:way and building=*", JsonNode.class);
    assertEquals(expectedValue,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("b"))
            .findFirst().get().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  /*
   * /contributions tests
   */

  @Test
  public void contributionsLatestCountTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> responseAggregation =
        restTemplate.getForEntity(server + port
            + "/contributions/latest/count?bboxes=8.67,49.39,8.71,49.42"
            + "&filter=type:way and natural=*&format=json&time=2014-01-01/2017-01-01/P1Y",
        JsonNode.class);
    ResponseEntity<JsonNode> responseExtraction = restTemplate.getForEntity(server + port
        + "/contributions/latest/bbox?bboxes=8.67,49.39,8.71,49.42&filter=type:way and natural=*"
        + "&properties=tags&time=2014-01-01,2017-01-01",
        JsonNode.class);
    int sumAggregation = StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(responseAggregation.getBody().get("result").iterator(),
            Spliterator.ORDERED),
        false).mapToInt(node -> node.get("value").asInt()).sum();
    long sumExtraction =
        StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(
                responseExtraction.getBody().get("features").iterator(), Spliterator.ORDERED),
            false).count();
    assertEquals(sumExtraction, sumAggregation);
  }

  @Test
  public void contributionsLatestCountFilteredByGeometryChange() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/contributions/count?bboxes=8.673088,49.401834,8.692051,49.407979&filter=type:way and "
        + "building=residential&format=json&time=2016-01-01/2020-01-01/P1Y&"
        + "contributionType=geometryChange", JsonNode.class);
    int sum = StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("result").iterator(),
            Spliterator.ORDERED), false)
        .mapToInt(node -> node.get("value").asInt()).sum();
    assertEquals(7, sum);
  }

  @Test
  public void contributionsLatestCountFilteredByTagChange() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/contributions/count?bboxes=8.673088,49.401834,8.692051,49.407979&filter=type:way and "
        + "building=residential&format=json&time=2016-01-01/2020-01-01/P1Y&"
        + "contributionType=tagChange", JsonNode.class);
    int sum = StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("result").iterator(),
            Spliterator.ORDERED), false)
        .mapToInt(node -> node.get("value").asInt()).sum();
    assertEquals(4, sum);
  }
  
  /*
   * csv output tests start here
   */
  @Test
  public void getElementsCountCsvTest() throws IOException {
    // expect result to have 1 entry row, with 2 columns
    final double expectedValue = 5.0;
    String responseBody = getResponseBody("/elements/count?"
        + "bboxes=8.689086,49.40268,8.689606,49.402973&time=2019-01-01&format=csv&filter=type:way");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(2, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("value")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsCountDensityCsvTest() throws IOException {
    // expect result to have 1 entry row, with 2 columns
    // bbox contains 2 shops(bbox 1 ~ 0.01km²)
    final double expectedValue = 215.87;
    String responseBody = getResponseBody("/elements/count/density?bboxes=8.6889,49.39281,8.69025,"
        + "49.39366&time=2017-01-01&format=csv&filter=type:node and shop=*");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(2, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("value")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsCountDensityGroupByBoundaryCsvTest() throws IOException {
    // expect result to have 1 entry row, with 3 columns
    // bbox 1 contains 3, bbox 2 contains 0 residential buildings (bbox 1 ~ 1km²)
    final double expectedValue = 3.76;
    String responseBody = getResponseBody("/elements/count/density/groupBy/boundary?"
        + "bboxes=8.678,49.41254,8.69074,49.4203|8.67959,49.41039,8.68092,49.41125"
        + "&time=2017-07-01&format=csv&filter=type:way and building=residential");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(3, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("boundary1")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsCountDensityGroupByBoundaryGroupByTagCsvTest() throws IOException {
    // expect result to have 1 entry row, with 5 columns
    // each bbox contains 2 garages and 2 residential buildings
    final double expectedValue = 1455.77;
    String responseBody = getResponseBody("/elements/count/density/groupBy/boundary/"
        + "groupBy/tag?bboxes=b1:8.692826,49.399133,8.693497,49.399388"
        + "|b2:8.69376,49.398376,8.69443,49.39863&time=2016-11-09&groupByKey=building&format=csv"
        + "&groupByValues=garage,residential&filter=type:way and building=*");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(5, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("b2_building=garage")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsCountDensityGroupByTagCsvTest() throws IOException {
    // expect result to have 1 entry row, with 4 columns
    // bbox contains 1 church and 1 synagogue
    final double expectedValue = 35.08;
    String responseBody = getResponseBody("/elements/count/density/groupBy/tag?"
        + "bboxes=8.687208,49.403608,8.690481,49.404687&format=csv&"
        + "groupByKey=building&groupByValues=church,synagogue&time=2019-01-01&filter=type:way");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("building=church")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsCountDensityGroupByTypeCsvTest() throws IOException {
    // expect result to have 1 entry row, with 3 columns
    // bbox contains 1 way and 1 relation with highway=pedestrian
    final double expectedValue = 2556.22;
    String responseBody = getResponseBody("/elements/count/density/groupBy/type?"
        + "bboxes=8.694322,49.409853,8.694584,49.410038&time=2015-01-01&format=csv"
        + "&filter=(type:way or type:relation) and highway=pedestrian");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("RELATION")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsCountGroupByBoundaryCsvTest() throws IOException {
    // expect result to have 1 entry row, with 3 columns
    final double expectedValue = 2.0;
    String responseBody = getResponseBody("/elements/count/groupBy/boundary?"
        + "bboxes=8.672445,49.418337,8.673196,49.419087|"
        + "8.670868,49.418892,8.672188,49.419216&time=2017-05-01&format=csv"
        + "&filter=type:node and bicycle_parking=stands");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(3, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("boundary1")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsCountGroupByBoundaryGroupByTagCsvTest() throws IOException {
    // expect result to have 1 entry row, with columns for: timestamp and
    // per boundary:
    // remainder , key=value 1 , ... , key=value N
    final double expectedValue = 5.0;
    String responseBody = getResponseBody("/elements/count/groupBy/boundary/groupBy/tag?"
        + "bboxes=8.673025,49.41914,8.673931,49.419597|8.671206,49.419401,8.672215,49.41951"
        + "&time=2016-11-09&&groupByKey=natural&groupByValues=tree,water&format=csv");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(7, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("boundary2_natural=tree")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsCountGroupByKeyCsvTest() throws IOException {
    // expect result to have 1 entry row, with 4 columns
    final double expectedValue = 1.0;
    String responseBody =
        getResponseBody("/elements/count/groupBy/key?bboxes=8.66841,49.40129,8.6728,49.40282&"
            + "format=csv&groupByKeys=female,male&time=2019-01-01&filter=type:node");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("female")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsCountGroupByTagCsvTest() throws IOException {
    // expect result to have 1 entry row, with 4 columns
    final double expectedValue = 2.0;
    String responseBody = getResponseBody(
        "/elements/count/groupBy/tag?bboxes=8.685459,49.412258,8.689724,49.412868"
            + "&format=csv&groupByKey=amenity&groupByValues=bbq,cafe&time=2019-01-01&"
            + "filter=type:node or type:way");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("amenity=bbq")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsCountGroupByTypeCsvTest() throws IOException {
    // expect result to have 1 entry row, with one timestamp-column and one column per requested
    // type
    final double expectedValue = 2.0;
    String responseBody =
        getResponseBody("/elements/count/groupBy/type?bboxes=8.68748,49.41404,8.69094,49.41458"
            + "&format=csv&time=2016-01-01&filter=(type:way or type:node) and amenity=restaurant");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("WAY")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsCountRatioCsvTest() throws IOException {
    // expect result to have 1 entry row, with 4 columns
    final double expectedValue = 0.2;
    String responseBody = getResponseBody("/elements/count/ratio?"
        + "bboxes=8.689317,49.395149,8.689799,49.395547&format=csv&time=2018-01-01"
        + "&filter=building=* and type:way&filter2=type:node and addr:housenumber=*");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("ratio")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsCountRatioGroupByBoundaryCsvTest() throws IOException {
    // expect result to have 1 entry row, with columns for: timestamp ,(per boundary:
    // key=value , key2=value2, ratio)
    final double expectedValue = 0.6;
    String responseBody = getResponseBody(
        "/elements/count/ratio/groupBy/boundary?bboxes=8.65917,49.39534,8.66428,49.40019|"
            + "8.65266,49.40178,8.65400,49.40237&format=csv&time=2018-01-01"
            + "&filter=highway=* and type:way&filter2=type:way and name=*");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(7, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("boundary2_ratio")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsLengthGroupByBoundaryGroupByTagCsvTest() throws IOException {
    // expect result to have 1 entry row, with columns for: timestamp ,(per boundary:
    // remainder , value 1 , ... , value N)
    final double expectedValue = 44.5;
    String responseBody = getResponseBody("/elements/length/groupBy/boundary/groupBy/tag?"
        + "bboxes=bboxes=b1:8.68593,49.39461,8.68865,49.39529|b2:8.68885,49.39450,8.68994,49.39536"
        + "&time=2017-11-25&groupByKey=highway&format=csv&groupByValues=service,residential"
        + "&filter=type:way and highway=*");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(7, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("b2_highway=service")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsLengthDensityGroupByBoundaryGroupByTagCsvTest() throws IOException {
    // expect result to have 1 entry row, with columns for: timestamp , (per boundary:
    // remainder , value 1 , ... , value N)
    final double expectedValue = 3190.17;
    String responseBody = getResponseBody("/elements/length/density/groupBy/boundary"
        + "/groupBy/tag?bboxes=b1:8.68086,49.39948,8.69401"
        + ",49.40609|b2:8.68081,49.39943,8.69408,49.40605&types=way&time=2017-10-08&keys=highway&"
        + "groupByKey=highway&format=csv&groupByValues=residential,primary");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(7, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("b2_highway=primary")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsAreaDensityGroupByTypeCsvTest() throws IOException {
    // group by type: expect result to have one column per requested type
    final double expectedValue1 = 264812.41;
    final double expectedValue2 = 46838.97;
    String responseBody =
        getResponseBody("/elements/area/density/groupBy/type?bcircles=8.68250,49.39384,300"
            + "&format=csv&time=2018-01-01&filter=leisure=* and (type:way or type:relation)");
    final List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(expectedValue1, Double.parseDouble(records.get(0).get("WAY")),
        expectedValue1 * deltaPercentage);
    assertEquals(expectedValue2, Double.parseDouble(records.get(0).get("RELATION")),
        expectedValue2 * deltaPercentage);
  }

  @Test
  public void getElementsAreaDensityGroupByTagCsvTest() throws IOException {
    // expect result to have 1 entry row, with 4 columns
    final double expectedValue = 14440.82;
    String responseBody = getResponseBody("/elements/area/density/groupBy/tag?"
        + "bboxes=8.68482,49.40167,8.68721,49.40267&format=csv&groupByKey=building&"
        + "groupByValues=retail,church&time=2018-10-01&filter=type:way");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("building=retail")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsAreaGroupByTypeCsvTest() throws IOException {
    // expect result to have 1 entry row, with 4 columns
    final double expectedValue = 1984.58;
    String responseBody =
        getResponseBody("/elements/area/groupBy/type?bcircles=8.689054,49.402481,80&"
            + "format=csv&time=2018-01-01&filter=building=* and (type:way or type:relation)");
    // way in geojson.io sq meters: 23.97
    // relation in geojson.io sq meters: 5399.27; in response:6448.93; in qgis 5393.5
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("WAY")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsAreaRatioCsvTest() throws IOException {
    // expect result to have 1 entry row, with 4 columns
    final double expectedValue = 0.041629;
    String responseBody = getResponseBody("/elements/area/ratio?"
        + "bboxes=8.68934,49.39415,8.69654,49.39936&format=csv&time=2018-01-01"
        + "&filter=type:way and landuse=cemetery&filter2=type:way and building=yes");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("ratio")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getUsersCountCsvTest() throws IOException {
    // expect result to have 3 entry rows (1 row per time interval), with 3 columns
    final double expectedValue = 7.0;
    String responseBody =
        getResponseBody("/users/count?bboxes=8.69338,49.40772,8.71454,49.41251&format=csv"
            + "&time=2014-01-01/2017-01-01/P1Y&filter=type:node and shop=clothes");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(3, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(3, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("value")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getUsersCountDensityCsvTest() throws IOException {
    // expect result to have 3 entry rows (1 row per time interval), with 3 columns
    final double expectedValue = 28.85;
    String responseBody = getResponseBody(
        "users/count/density?bcircles=8.68628,49.41117,200|8.68761,49.40819,200"
            + "&format=csv&time=2014-01-01/2017-01-01/P1Y&filter=type:way and wheelchair=yes");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(3, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(3, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("value")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getUsersCountDensityGroupByTypeCsvTest() throws IOException {
    // expect result to have 3 entry rows (1 row per time interval)
    final double expectedValue = 3854.35;
    String responseBody = getResponseBody("users/count/density/groupBy/type?"
        + "bboxes=8.691773,49.413804,8.692149,49.413975&format=csv&time=2014-01-01/2017-01-01/P1Y"
        + "&filter=addr:housenumber=5 and (type:way or type:node)");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(3, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(5, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(0).get("NODE")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getUsersCountGroupByTagCsvTest() throws IOException {
    // expect result to have 3 entry rows (1 row per time interval)
    final double expectedValue = 2.0;
    String responseBody =
        getResponseBody("users/count/groupBy/tag?bboxes=8.691865,49.413835,8.692605,49.414756"
            + "&format=csv&groupByKey=shop&time=2015-01-01/2018-01-01/P1Y"
            + "&groupByValues=clothes,wine&filter=type:node");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(3, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(6, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(1).get("shop=wine")),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getUsersCountGroupByTypeCsvTest() throws IOException {
    // expect result to have 3 entry rows (1 row per time interval)
    final double expectedValue = 1.0;
    String responseBody = getResponseBody("users/count/groupBy/type?"
        + "bboxes=8.700609,49.409336,8.701488,49.409591&format=csv&time=2010-01-01/2013-01-01/P1Y"
        + "&filter=(type:way or type:node) and addr:housenumber=* and addr:street=\"Plöck\"");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(3, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(5, headers.size());
    assertEquals(expectedValue, Double.parseDouble(records.get(2).get("WAY")),
        expectedValue * deltaPercentage);
  }

  /*
   * filter tests
   */

  @Test
  public void getElementsCountFilterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/count?bboxes=8.67452,49.40961,8.70392,49.41823"
            + "&time=2015-01-01&filter=building=residential and type:way",
        JsonNode.class);
    assertEquals(40, response.getBody().get("result").get(0).get("value").asInt());
  }

  @Test
  public void areaRatioFilterTest() {
    final double expectedValue = 0.060083;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/area/ratio?bboxes=8.68081,49.39821,8.69528,49.40687&time="
            + "2018-01-01&filter=building=* and type:way&filter2=building=* and type:relation",
        JsonNode.class);
    assertEquals(expectedValue,
        response.getBody().get("ratioResult").get(0).get("ratio").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void ratioGroupByBoundaryFilterTest() {
    final double expectedValue = 0.230435;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/count/ratio/groupBy/boundary?bboxes=b1:8.66004,49.41184,8.68481,"
            + "49.42094|b2:8.66009,49.41180,8.68461,49.42079&time=2018-01-01&"
            + "filter=geometry:polygon and building=*&filter2=type:node and addr:housenumber=*",
        JsonNode.class);
    assertEquals(expectedValue, StreamSupport
        .stream(
            Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByBoundaryResult").iterator(), Spliterator.ORDERED),
            false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("b2"))
        .findFirst().get().get("ratioResult").get(0).get("ratio").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getElementsCountRatioEmptyFilterTest() {
    final double expectedValue = 0.2;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/count/ratio?bboxes=8.685824,49.414756,8.686253,49.414955&"
            + "filter2=highway=*&time=2019-01-01",
        JsonNode.class);
    assertEquals(expectedValue,
        response.getBody().get("ratioResult").get(0).get("ratio").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void ratioEmptyFilter2Test() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/count/ratio?bboxes=8.687337,49.415067,8.687493,49.415172&"
            + "time=2010-01-01&filter=building=*",
        JsonNode.class);
    assertEquals(400, response.getBody().get("status").asInt());
  }

  @Test
  public void getElementsCountWrongFilterTypesCombinationTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate.getForEntity(server + port + "/elements/count?bboxes=8.67452,49.40961,"
            + "8.70392,49.41823&time=2015-01-01&filter=building=*&types=way", JsonNode.class);
    assertEquals(400, response.getBody().get("status").asInt());
  }

  @Test
  public void getElementsCountWrongFilterKeysCombinationTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate.getForEntity(
            server + port + "/elements/count?bboxes=8.67452,49.40961,"
                + "8.70392,49.41823&time=2015-01-01&filter=building=*&keys=building",
            JsonNode.class);
    assertEquals(400, response.getBody().get("status").asInt());
  }

  @Test
  public void getFilterTest() {
    final double expectedValue = 585.48;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate.getForEntity(server + port + "/elements/length?bboxes=8.684692,49.407669,"
            + "8.688061,49.410310&time=2014-01-01&filter=highway=residential", JsonNode.class);
    assertEquals(expectedValue, response.getBody().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getAllValuesFilterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate.getForEntity(server + port + "/elements/count?bboxes=8.67452,49.40961,"
            + "8.70392,49.41823&time=2015-01-01&filter=building=*", JsonNode.class);
    assertEquals(2010, response.getBody().get("result").get(0).get("value").asInt());
  }

  @Test
  public void getNotEqualsFilterTest() {
    final double expectedValue = 9257.4;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/perimeter?bboxes=8.684692,49.407669,8.688061,49.410310"
            + "&time=2014-01-01&filter=building!=flats",
        JsonNode.class);
    assertEquals(expectedValue, response.getBody().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getNotEqualsAllValuesFilterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate.getForEntity(server + port + "/elements/count?bboxes=8.67452,49.40961,"
            + "8.70392,49.41823&time=2015-01-01&filter=building!=*", JsonNode.class);
    assertEquals(3893, response.getBody().get("result").get(0).get("value").asInt());
  }

  @Test
  public void getBracketsFilterTest() {
    final double expectedValue = 20902.2;
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate.getForEntity(
            server + port + "/elements/area?bboxes=8.684692,49.407669,8.688061,49.410310,"
                + "&time=2014-01-01&filter=building=* and (name!=* or noname!=yes)",
            JsonNode.class);
    assertEquals(expectedValue, response.getBody().get("result").get(0).get("value").asDouble(),
        expectedValue * deltaPercentage);
  }

  @Test
  public void getNotFilterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate.getForEntity(
            server + port + "/elements/count?bboxes=8.67452,49.40961,"
                + "8.70392,49.41823&time=2015-01-01&filter=not building=residential",
            JsonNode.class);
    assertEquals(5863, response.getBody().get("result").get(0).get("value").asInt());
  }

  @Test
  public void getAndFilterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/users/count?bboxes=8.67452,49.40961,8.70392,49.41823&"
            + "time=2014-01-01/2015-01-01&filter=highway=residential and maxspeed=*",
        JsonNode.class);
    assertEquals(7, response.getBody().get("result").get(0).get("value").asInt());
  }

  @Test
  public void getOrFilterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/users/count?bboxes=8.67452,49.40961,8.70392,49.41823&"
            + "time=2014-01-01/2015-01-01&filter=highway=residential or maxspeed=*",
        JsonNode.class);
    assertEquals(19, response.getBody().get("result").get(0).get("value").asInt());
  }

  @Test
  public void getRequestEndsByQuestionMarkTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate.getForEntity(server + port + "/users/count?", JsonNode.class);
    assertEquals(null, response.getBody().get("error"));
  }
}
