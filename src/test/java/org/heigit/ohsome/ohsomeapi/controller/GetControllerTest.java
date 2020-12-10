package org.heigit.ohsome.ohsomeapi.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

  private static String port = TestProperties.PORT1;
  private String server = TestProperties.SERVER;

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
    SpringApplication.exit(Application.getApplicationContext(), () -> 0);
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
    ResponseEntity<JsonNode> response =
        restTemplate.getForEntity(server + port + "/elements/area?type=way", JsonNode.class);
    assertEquals(400, response.getBody().get("status").asInt());
  }

  @Test
  public void getGeneralResourceWithSpecificParameterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate
        .getForEntity(server + port + "/elements/count/density?values2=highway", JsonNode.class);
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
        + "/elements/count?bboxes=8.67452,49.40961,8.70392,49.41823&types=way&time=2015-01-01"
        + "&keys=building&values=residential&showMetadata=true", JsonNode.class);
    assertEquals(40, response.getBody().get("result").get(0).get("value").asInt());
  }

  @Test
  public void getElementsCountGroupByBoundaryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/count/groupBy/boundary?bboxes=8.70538,49.40891,8.70832,49.41155|"
            + "8.68667,49.41353,8.68828,49.414&types=polygon&time=2017-01-01&keys=building"
            + "&values=church&showMetadata=true",
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
        + "types=way&time=2016-11-09&keys=building&groupByKey=building&groupByValues=yes",
        JsonNode.class);
    assertEquals(43, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(
            jsonNode -> "boundary1".equalsIgnoreCase(jsonNode.get("groupByObject").get(0).asText())
                && "remainder".equalsIgnoreCase(jsonNode.get("groupByObject").get(1).asText()))
        .findFirst().get().get("result").get(0).get("value").asInt(), 0);
  }

  @Test
  public void getElementsCountGroupByTypeTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/count/groupBy/type?bboxes=8.67038,49.40341,8.69197,49.40873"
            + "&types=way,relation&time=2017-01-01&keys=building&showMetadata=true",
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
        server + port + "/elements/count/groupBy/tag?bboxes=8.67859,49.41189,8.67964,49.41263"
            + "&types=way&time=2017-01-01&keys=building&groupByKey=building&showMetadata=true",
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
            server + port + "/elements/count/groupBy/key?bboxes=8.67859,49.41189,8.67964,49.41263"
                + "&types=way&time=2012-01-01&groupByKeys=building&showMetadata=true",
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
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/count/ratio?bboxes=8.66004,49.41184,8.68481,49.42094&types=way"
            + "&time=2015-01-01/2019-01-01/P1Y&keys=building&types2=node&keys2=addr:housenumber",
        JsonNode.class);
    assertEquals(0.153933, response.getBody().get("ratioResult").get(0).get("ratio").asDouble(),
        1e-6);
  }

  @Test
  public void getElementsCountRatioGroupByBoundaryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/count/ratio/groupBy/boundary?bcircles=8.66906,49.4167,100|"
            + "8.69013,49.40223,100&types=way&time=2017-09-20&keys=building"
            + "&types2=node&keys2=addr:housenumber",
        JsonNode.class);
    assertEquals(1.052632, StreamSupport
        .stream(
            Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByBoundaryResult").iterator(), Spliterator.ORDERED),
            false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("boundary1"))
        .findFirst().get().get("ratioResult").get(0).get("ratio").asDouble(), 1e-6);
  }

  @Test
  public void getElementsCountDensityTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/count/density?bboxes=8.68794,49.41434,8.69021,49.41585"
            + "&types=way&time=2017-08-11&keys=building&showMetadata=true",
        JsonNode.class);
    assertEquals(3880.74, response.getBody().get("result").get(0).get("value").asDouble(), 1e-6);
  }

  @Test
  public void getElementsCountDensityGroupByBoundaryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port
            + "/elements/count/density/groupBy/boundary?bboxes=8.68794,49.41434,8.69021,49.41585|"
            + "8.67933,49.40505,8.6824,49.40638&types=way&time=2017-08-19&keys=building",
        JsonNode.class);
    assertEquals(334.85, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("boundary2"))
        .findFirst().get().get("result").get(0).get("value").asDouble(), 0);
  }

  @Test
  public void getElementsCountDensityGroupByTypeTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/count/density/groupBy/type?bboxes=8.68086,49.39948,8.69401,49.40609"
        + "&types=way,node&time=2016-11-09&keys=addr:housenumber", JsonNode.class);
    assertEquals(893.67,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("way"))
            .findFirst().get().get("result").get(0).get("value").asDouble(),
        0);
  }

  @Test
  public void getElementsCountDensityGroupByTagTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/count/density/groupBy/tag?bboxes=8.68086,49.39948,8.69401,49.40609&types=way"
        + "&time=2016-11-09&keys=building&groupByKey=building&groupByValues=yes", JsonNode.class);
    assertEquals(61.48, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("remainder"))
        .findFirst().get().get("result").get(0).get("value").asDouble(), 1e-6);
  }

  @Test
  public void getElementsCountDensityGroupByBoundaryGroupByTagTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/count/density/groupBy/boundary/groupBy/tag?bboxes=b1:8.68086,49.39948,8.69401,"
        + "49.40609|b2:8.68081,49.39943,8.69408,49.40605&types=way&time=2016-11-09&keys=building&"
        + "groupByKey=building", JsonNode.class);
    assertEquals(2.83, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> "b2".equalsIgnoreCase(jsonNode.get("groupByObject").get(0).asText())
            && "building=church".equalsIgnoreCase(jsonNode.get("groupByObject").get(1).asText()))
        .findFirst().get().get("result").get(0).get("value").asDouble(), 1e-6);
  }

  /*
   * /elements/length tests
   */

  @Test
  public void getElementsLengthTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/length?bboxes=8.67452,49.40961,8.70392,49.41823&types=way"
            + "&time=2012-01-01&keys=highway&values=residential",
        JsonNode.class);
    assertEquals(15171.81, response.getBody().get("result").get(0).get("value").asDouble(), 1e-6);
  }

  @Test
  public void getElementsLengthGroupByBoundaryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port
            + "/elements/length/groupBy/boundary?bboxes=8.695443,49.408928,8.695636,49.409151|"
            + "8.699262,49.409451,8.701547,49.412205&types=way&time=2014-08-21&keys=highway",
        JsonNode.class);
    assertEquals(25.5, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("boundary1"))
        .findFirst().get().get("result").get(0).get("value").asDouble(), 0);
  }

  @Test
  public void getElementsLengthGroupByBoundaryGroupByTagTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/length/groupBy/boundary/groupBy/tag?bboxes=8.68086,49.39948,8.69401,49.40609"
        + "&types=way&time=2017-11-25&keys=highway&groupByKey=highway", JsonNode.class);
    assertEquals(670.61, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> "boundary1"
            .equalsIgnoreCase(jsonNode.get("groupByObject").get(0).asText())
            && "highway=secondary".equalsIgnoreCase(jsonNode.get("groupByObject").get(1).asText()))
        .findFirst().get().get("result").get(0).get("value").asDouble(), 1e-6);
  }

  @Test
  public void getElementsLengthGroupByTypeTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/length/groupBy/type?bboxes=8.701665,49.408802,8.703999,49.409553"
            + "&types=way,relation&time=2014-08-21&keys=highway",
        JsonNode.class);
    assertEquals(540.52,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("way"))
            .findFirst().get().get("result").get(0).get("value").asDouble(),
        0);
  }

  @Test
  public void getElementsLengthGroupByKeyTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/length/groupBy/key?bboxes=8.67181,49.40434,8.67846,49.40878"
            + "&types=way&time=2016-08-21&groupByKeys=highway,railway",
        JsonNode.class);
    assertEquals(3132.95, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("remainder"))
        .findFirst().get().get("result").get(0).get("value").asDouble(), 1e-6);
  }

  @Test
  public void getElementsLengthGroupByTagTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/length/groupBy/tag?bboxes=8.70773,49.40832,8.71413,49.41092&types=way"
        + "&time=2016-08-21&groupByKey=highway", JsonNode.class);
    assertEquals(372.78, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("highway=path"))
        .findFirst().get().get("result").get(0).get("value").asDouble(), 1e-6);
  }

  @Test
  public void getElementsLengthRatioTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/length/ratio?bboxes=8.67567,49.40695,8.69434,49.40882"
            + "&types=way&time=2011-12-13&keys=highway&keys2=railway",
        JsonNode.class);
    assertEquals(0.135225, response.getBody().get("ratioResult").get(0).get("ratio").asDouble(),
        1e-6);
  }

  @Test
  public void getElementsLengthRatioGroupByBoundaryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port + "/elements"
        + "/length/ratio/groupBy/boundary?bboxes=8.67829,49.39807,8.69061,49.40578|"
        + "8.68306,49.42407,8.68829,49.42711&types=way&time=2012-12-22&keys=highway&keys2=railway",
        JsonNode.class);
    assertEquals(0.47867, StreamSupport
        .stream(
            Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByBoundaryResult").iterator(), Spliterator.ORDERED),
            false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("boundary1"))
        .findFirst().get().get("ratioResult").get(0).get("ratio").asDouble(), 1e-6);
  }

  @Test
  public void getElementsLengthDensityTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/length/density?bboxes=8.70538,49.40464,8.71264,49.41042"
            + "&types=way&time=2013-01-04&keys=highway",
        JsonNode.class);
    assertEquals(29022.41, response.getBody().get("result").get(0).get("value").asDouble(), 1e-6);
  }

  @Test
  public void getElementsLengthDensityGroupByTypeTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/length/density/groupBy/type?bboxes=8.68242,49.40059,8.68732,49.4059"
        + "&types=way,node&time=2015-03-25", JsonNode.class);
    assertEquals(47849.51,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("way"))
            .findFirst().get().get("result").get(0).get("value").asDouble(),
        0);
  }

  @Test
  public void getElementsLengthDensityGroupByTagTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/length/density/groupBy/tag?bboxes=8.66972,49.40453,8.67564,49.4076"
        + "&types=way&time=2016-01-17&groupByKey=railway", JsonNode.class);
    assertEquals(20495.63, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("railway=tram"))
        .findFirst().get().get("result").get(0).get("value").asDouble(), 1e-6);
  }

  @Test
  public void getElementsLengthDensityGroupByBoundaryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port
            + "/elements/length/density/groupBy/boundary?bboxes=8.69079,49.40129,8.69238,49.40341|"
            + "8.67504,49.4119,8.67813,49.41668&types=way&time=2017-05-30&keys=highway",
        JsonNode.class);
    assertEquals(48739.54, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("boundary2"))
        .findFirst().get().get("result").get(0).get("value").asDouble(), 0);
  }

  @Test
  public void getElementsLengthDensityGroupByBoundaryGroupByTagTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/length/density/groupBy/boundary/groupBy/tag?bboxes=b1:8.68086,49.39948,8.69401"
        + ",49.40609|b2:8.68081,49.39943,8.69408,49.40605&types=way&time=2017-10-08&keys=highway&"
        + "groupByKey=highway", JsonNode.class);
    assertEquals(73.71,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> "b1".equalsIgnoreCase(jsonNode.get("groupByObject").get(0).asText())
                && "highway=steps".equalsIgnoreCase(jsonNode.get("groupByObject").get(1).asText()))
            .findFirst().get().get("result").get(0).get("value").asDouble(),
        1e-6);
  }

  /*
   * /users tests
   */

  @Test
  public void getUsersCountTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate
            .getForEntity(
                server + port + "/users/count?bboxes=8.67452,49.40961,8.70392,49.41823&types=way"
                    + "&time=2014-01-01,2015-01-01&keys=building&values=residential",
                JsonNode.class);
    assertEquals(5, response.getBody().get("result").get(0).get("value").asInt());
  }

  @Test
  public void getUsersCountGroupByTypeTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/users/count/groupBy/type?bboxes=8.67,49.39941,8.69545,49.4096&types=way,relation"
        + "&time=2014-01-01,2015-01-01&keys=building", JsonNode.class);
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
        server + port + "/users/count/groupBy/key?bboxes=8.67,49.39941,8.69545,49.4096&types=way"
            + "&time=2014-01-01,2015-01-01&groupByKeys=building",
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
        server + port + "/users/count/groupBy/tag?bboxes=8.67,49.39941,8.69545,49.4096&types=way"
            + "&time=2014-01-01,2015-01-01&groupByKey=building",
        JsonNode.class);
    assertEquals(29, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("building=yes"))
        .findFirst().get().get("result").get(0).get("value").asInt());
  }

  @Test
  public void getUsersCountDensityTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/users/count/density?bboxes=8.67,49.39941,8.69545,49.4096&types=way"
            + "&time=2014-01-01,2015-01-01&keys=building",
        JsonNode.class);
    assertEquals(14.38, response.getBody().get("result").get(0).get("value").asDouble(), 1e-6);
  }

  @Test
  public void getUsersCountDensityGroupByTypeTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/users/count/density/groupBy/type?bboxes=8.67,49.39941,8.69545,49.4096&types=way,"
        + "relation&time=2014-01-01,2015-01-01&keys=building", JsonNode.class);
    assertEquals(3.83,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("relation"))
            .findFirst().get().get("result").get(0).get("value").asDouble(),
        1e-6);
  }

  @Test
  public void getUsersCountDensityGroupByTagTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/users/count/density/groupBy/tag?bboxes=8.67,49.39941,8.69545,49.4096&types=way"
        + "&time=2014-01-01,2015-01-01&groupByKey=building&showMetadata=true", JsonNode.class);
    assertEquals(26.84, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("remainder"))
        .findFirst().get().get("result").get(0).get("value").asDouble(), 1e-6);
  }

  @Test
  public void getUsersCountGroupByBoundaryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/users/count/groupBy/boundary?bboxes=a:8.67452,49.40961,8.70392,49.41823|"
        + "b:8.67,49.39941,8.69545,49.4096&types=way&time=2014-01-01,2015-01-01&showMetadata=true"
        + "&keys=building", JsonNode.class);
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
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/users/count/density/groupBy/boundary?bboxes=a:8.67452,49.40961,8.70392,49.41823|"
        + "b:8.67,49.39941,8.69545,49.4096&types=way&time=2014-01-01,2015-01-01&showMetadata=true"
        + "&keys=building", JsonNode.class);
    assertEquals(14.38,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("b"))
            .findFirst().get().get("result").get(0).get("value").asDouble(),
        1e-6);
  }

  /*
   * csv output tests start here
   */

  @Test
  public void getElementsCountCsvTest() throws IOException {
    // expect result to have 1 entry row, with 2 columns
    String responseBody = getResponseBody("/elements/count?"
        + "bboxes=8.689086,49.40268,8.689606,49.402973&types=way&time=2019-01-01" + "&format=csv");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(2, headers.size());
    assertEquals(5.0, Double.parseDouble(records.get(0).get("value")), 0);
  }

  @Test
  public void getElementsCountDensityCsvTest() throws IOException {
    // expect result to have 1 entry row, with 2 columns
    // bbox contains 2 shops(bbox 1 ~ 0.01km²)
    String responseBody =
        getResponseBody("/elements/count/density?" + "bboxes=8.6889,49.39281,8.69025,49.39366&"
            + "types=node&time=2017-01-01&keys=shop&format=csv");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(2, headers.size());
    assertEquals(216.58, Double.parseDouble(records.get(0).get("value")), 0.01);
  }

  @Test
  public void getElementsCountDensityGroupByBoundaryCsvTest() throws IOException {
    // expect result to have 1 entry row, with 3 columns
    // bbox 1 contains 3, bbox 2 contains 0 residential buildings (bbox 1 ~ 1km²)
    String responseBody = getResponseBody("/elements/count/density/groupBy/boundary?"
        + "bboxes=8.678,49.41254,8.69074,49.4203|8.67959,49.41039,8.68092,49.41125&"
        + "types=way&time=2017-07-01&keys=building&values=residential&format=csv");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(3, headers.size());
    assertEquals(3.77, Double.parseDouble(records.get(0).get("boundary1")), 0.01);
  }

  @Test
  public void getElementsCountDensityGroupByBoundaryGroupByTagCsvTest() throws IOException {
    // expect result to have 1 entry row, with 5 columns
    // each bbox contains 2 garages and 2 residential buildings
    String responseBody = getResponseBody("/elements/count/density/groupBy/boundary/"
        + "groupBy/tag?bboxes=b1:8.692826,49.399133,8.693497,49.399388"
        + "|b2:8.69376,49.398376,8.69443,49.39863&types=way&time=2016-11-09&keys=building&"
        + "groupByKey=building&format=csv&groupByValues=garage,residential");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(5, headers.size());
    assertEquals(1460.52, Double.parseDouble(records.get(0).get("b2_building=garage")), 0.01);
  }

  @Test
  public void getElementsCountDensityGroupByTagCsvTest() throws IOException {
    // expect result to have 1 entry row, with 4 columns
    // bbox contains 1 church and 1 synagogue
    String responseBody = getResponseBody("/elements/count/density/groupBy/tag?"
        + "bboxes=8.687208,49.403608,8.690481,49.404687&format=csv&"
        + "groupByKey=building&groupByValues=church,synagogue&time=2019-01-01&types=way");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(35.19, Double.parseDouble(records.get(0).get("building=church")), 0.01);
  }

  @Test
  public void getElementsCountDensityGroupByTypeCsvTest() throws IOException {
    // expect result to have 1 entry row, with 3 columns
    // bbox contains 1 way and 1 relation with highway=pedestrian
    String responseBody = getResponseBody("/elements/count/density/groupBy/type?"
        + "bboxes=8.694322,49.409853,8.694584,49.410038&keys=highway&values=pedestrian"
        + "&types=way,relation&time=2015-01-01&format=csv");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(3, headers.size());
    assertEquals(2564.57, Double.parseDouble(records.get(0).get("RELATION")), 0.01);
  }

  @Test
  public void getElementsCountGroupByBoundaryCsvTest() throws IOException {
    // expect result to have 1 entry row, with 3 columns
    String responseBody = getResponseBody("/elements/count/groupBy/boundary?"
        + "bboxes=8.672445,49.418337,8.673196,49.419087|"
        + "8.670868,49.418892,8.672188,49.419216&types=node&time=2017-05-01&keys=bicycle_parking"
        + "&values=stands&format=csv");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(3, headers.size());
    assertEquals(2.0, Double.parseDouble(records.get(0).get("boundary1")), 0);
  }

  @Test
  public void getElementsCountGroupByBoundaryGroupByTagCsvTest() throws IOException {
    // expect result to have 1 entry row, with columns for: timestamp and
    // per boundary:
    // remainder , key=value 1 , ... , key=value N
    String responseBody = getResponseBody("/elements/count/groupBy/boundary/groupBy/tag?"
        + "bboxes=8.673025,49.41914,8.673931,49.419597|8.671206,49.419401,8.672215,49.41951&"
        + "types=way,node,relation&time=2016-11-09&&groupByKey=natural&groupByValues=tree,water"
        + "&format=csv");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(7, headers.size());
    assertEquals(5.0, Double.parseDouble(records.get(0).get("boundary2_natural=tree")), 0);
  }

  @Test
  public void getElementsCountGroupByKeyCsvTest() throws IOException {
    // expect result to have 1 entry row, with 4 columns
    String responseBody =
        getResponseBody("/elements/count/groupBy/key?" + "bboxes=8.66841,49.40129,8.6728,49.40282&"
            + "format=csv&groupByKeys=female,male&time=2019-01-01&types=node");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(1.0, Double.parseDouble(records.get(0).get("female")), 0);
  }

  @Test
  public void getElementsCountGroupByTagCsvTest() throws IOException {
    // expect result to have 1 entry row, with 4 columns
    String responseBody = getResponseBody(
        "/elements/count/groupBy/tag?" + "bboxes=8.685459,49.412258,8.689724,49.412868"
            + "&format=csv&groupByKey=amenity&groupByValues=bbq,cafe&time=2019-01-01&"
            + "types=node,way");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(2.0, Double.parseDouble(records.get(0).get("amenity=bbq")), 0);
  }

  @Test
  public void getElementsCountGroupByTypeCsvTest() throws IOException {
    // expect result to have 1 entry row, with one timestamp-column and one column per requested
    // type
    String responseBody =
        getResponseBody("/elements/count/groupBy/type?" + "bboxes=8.68748,49.41404,8.69094,49.41458"
            + "&format=csv&time=2016-01-01&types=way,node&keys=amenity&values=restaurant");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(3, headers.size());
    assertEquals(2.0, Double.parseDouble(records.get(0).get("WAY")), 0);
  }

  @Test
  public void getElementsCountRatioCsvTest() throws IOException {
    // expect result to have 1 entry row, with 4 columns
    String responseBody = getResponseBody("/elements/count/ratio?"
        + "bboxes=8.689317,49.395149,8.689799,49.395547&format=csv&keys=building&"
        + "keys2=addr:housenumber&time=2018-01-01&types=way&types2=node");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(0.2, Double.parseDouble(records.get(0).get("ratio")), 0);
  }

  @Test
  public void getElementsCountRatioGroupByBoundaryCsvTest() throws IOException {
    // expect result to have 1 entry row, with columns for: timestamp ,(per boundary:
    // key=value , key2=value2, ratio)
    String responseBody = getResponseBody(
        "/elements/count/ratio/groupBy/boundary?" + "bboxes=8.65917,49.39534,8.66428,49.40019|"
            + "8.65266,49.40178,8.65400,49.40237&format=csv&keys=highway&keys2=name&"
            + "time=2018-01-01&types=way&types2=way");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(7, headers.size());
    assertEquals(0.6, Double.parseDouble(records.get(0).get("boundary2_ratio")), 0.0001);
  }

  @Test
  public void getElementsLengthGroupByBoundaryGroupByTagCsvTest() throws IOException {
    // expect result to have 1 entry row, with columns for: timestamp ,(per boundary:
    // remainder , value 1 , ... , value N)
    String responseBody = getResponseBody("/elements/length/groupBy/boundary/groupBy/tag?"
        + "bboxes=bboxes=b1:8.68593,49.39461,8.68865,49.39529|b2:8.68885,49.39450,8.68994,49.39536"
        + "&types=way&time=2017-11-25&keys=highway&groupByKey=highway&format=csv&groupByValues="
        + "service,residential");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(7, headers.size());
    assertEquals(44.37, Double.parseDouble(records.get(0).get("b2_highway=service")), 0.01);
  }

  @Test
  public void getElementsLengthDensityGroupByBoundaryGroupByTagCsvTest() throws IOException {
    // expect result to have 1 entry row, with columns for: timestamp , (per boundary:
    // remainder , value 1 , ... , value N)
    String responseBody = getResponseBody("/elements/length/density/groupBy/boundary"
        + "/groupBy/tag?bboxes=b1:8.68086,49.39948,8.69401"
        + ",49.40609|b2:8.68081,49.39943,8.69408,49.40605&types=way&time=2017-10-08&keys=highway&"
        + "groupByKey=highway&format=csv&groupByValues=residential,primary");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(7, headers.size());
    assertEquals(3195.93, Double.parseDouble(records.get(0).get("b2_highway=primary")), 0.01);
  }

  @Test
  public void getElementsAreaDensityGroupByTypeCsvTest() throws IOException {
    // group by type: expect result to have one column per requested type
    String responseBody =
        getResponseBody("/elements/area/density/groupBy/type?" + "bcircles=8.68250,49.39384,300"
            + "&format=csv&keys=leisure&time=2018-01-01&types=way,relation");
    final List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertFalse(headers.containsKey("NODE"));
    assertEquals(3, headers.size());
    assertEquals(264812.45, Double.parseDouble(records.get(0).get("WAY")), 0.01);
    assertEquals(46838.97, Double.parseDouble(records.get(0).get("RELATION")), 0.01);
  }

  @Test
  public void getElementsAreaDensityGroupByTagCsvTest() throws IOException {
    // expect result to have 1 entry row, with 4 columns
    String responseBody = getResponseBody("/elements/area/density/groupBy/tag?"
        + "bboxes=8.68482,49.40167,8.68721,49.40267&format=csv&groupByKey=building&"
        + "groupByValues=retail,church&time=2018-10-01&types=way");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(14440.82, Double.parseDouble(records.get(0).get("building=retail")), 0.01);
  }

  @Test
  public void getElementsAreaGroupByTypeCsvTest() throws IOException {
    // expect result to have 1 entry row, with 3 columns
    String responseBody =
        getResponseBody("/elements/area/groupBy/type?" + "bcircles=8.689054,49.402481,80&"
            + "format=csv&keys=building&time=2018-01-01&types=way,relation");
    // way in geojson.io sq meters: 23.97
    // relation in geojson.io sq meters: 5399.27; in response:6448.93; in qgis 5393.5
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(3, headers.size());
    assertEquals(1978.12, Double.parseDouble(records.get(0).get("WAY")), 0.01);
  }

  @Test
  public void getElementsAreaRatioCsvTest() throws IOException {
    // expect result to have 1 entry row, with 4 columns
    String responseBody =
        getResponseBody("/elements/area/ratio?" + "bboxes=8.68934,49.39415,8.69654,49.39936"
            + "&format=csv&keys=landuse&keys2=building&time=2018-01-01&"
            + "types=way&types2=way&values=cemetery&values2=yes");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(1, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(0.041629, Double.parseDouble(records.get(0).get("ratio")), 0.01);
  }

  @Test
  public void getUsersCountCsvTest() throws IOException {
    // expect result to have 3 entry rows (1 row per time interval), with 3 columns
    String responseBody =
        getResponseBody("/users/count?" + "bboxes=8.69338,49.40772,8.71454,49.41251"
            + "&format=csv&keys=shop&time=2014-01-01/2017-01-01/P1Y&types=node&values=clothes");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(3, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(3, headers.size());
    assertEquals(7.0, Double.parseDouble(records.get(0).get("value")), 0);
  }

  @Test
  public void getUsersCountDensityCsvTest() throws IOException {
    // expect result to have 3 entry rows (1 row per time interval), with 3 columns
    String responseBody = getResponseBody(
        "users/count/density?" + "bcircles=8.68628,49.41117,200|8.68761,49.40819,200"
            + "&format=csv&keys=wheelchair&time=2014-01-01/2017-01-01/P1Y&types=way&values=yes");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(3, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(3, headers.size());
    assertEquals(28.94, Double.parseDouble(records.get(0).get("value")), 0.01);
  }

  @Test
  public void getUsersCountDensityGroupByTypeCsvTest() throws IOException {
    // expect result to have 3 entry rows (1 row per time interval)
    String responseBody = getResponseBody(
        "users/count/density/groupBy/type?" + "bboxes=8.691773,49.413804,8.692149,49.413975"
            + "&format=csv&keys=addr:housenumber&time=2014-01-01/2017-01-01/P1Y"
            + "&types=way,node&values=5");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(3, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(3866.95, Double.parseDouble(records.get(0).get("NODE")), 0.01);
  }

  @Test
  public void getUsersCountGroupByTagCsvTest() throws IOException {
    // expect result to have 3 entry rows (1 row per time interval)
    String responseBody =
        getResponseBody("users/count/groupBy/tag?" + "bboxes=8.691865,49.413835,8.692605,49.414756"
            + "&format=csv&groupByKey=shop&time=2015-01-01/2018-01-01/P1Y"
            + "&types=node&groupByValues=clothes,wine");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(3, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(6, headers.size());
    assertEquals(2.0, Double.parseDouble(records.get(1).get("shop=wine")), 0);
  }

  @Test
  public void getUsersCountGroupByTypeCsvTest() throws IOException {
    // expect result to have 3 entry rows (1 row per time interval)
    String responseBody =
        getResponseBody("users/count/groupBy/type?" + "bboxes=8.700609,49.409336,8.701488,49.409591"
            + "&format=csv&keys=addr:housenumber,addr:street&time=2010-01-01/2013-01-01/P1Y"
            + "&types=way,node&values=,Plöck");
    List<CSVRecord> records = Helper.getCsvRecords(responseBody);
    assertEquals(3, Helper.getCsvRecords(responseBody).size());
    Map<String, Integer> headers = Helper.getCsvHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(1.0, Double.parseDouble(records.get(2).get("WAY")), 0);
  }

  /*
   * filter tests
   */

  @Test
  public void getElementsCountFilterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/count?bboxes=8.67452,49.40961,"
            + "8.70392,49.41823&time=2015-01-01&filter=building=residential and type:way",
        JsonNode.class);
    assertEquals(40, response.getBody().get("result").get(0).get("value").asInt());
  }

  @Test
  public void areaRatioFilterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/area/ratio?bboxes=8.68081,49.39821,8.69528,49.40687&time="
            + "2018-01-01&filter=building=* and type:way&filter2=building=* and type:relation",
        JsonNode.class);
    assertEquals(0.060083, response.getBody().get("ratioResult").get(0).get("ratio").asDouble(),
        1e-6);
  }

  @Test
  public void ratioGroupByBoundaryFilterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/count/ratio/groupBy/boundary?bboxes=b1:8.66004,49.41184,8.68481,"
            + "49.42094|b2:8.66009,49.41180,8.68461,49.42079&time=2018-01-01&"
            + "filter=geometry:polygon and building=*&filter2=type:node and addr:housenumber=*",
        JsonNode.class);
    assertEquals(0.230435, StreamSupport
        .stream(
            Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByBoundaryResult").iterator(), Spliterator.ORDERED),
            false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("b2"))
        .findFirst().get().get("ratioResult").get(0).get("ratio").asDouble(), 1e-6);
  }

  @Test
  public void getElementsCountRatioEmptyFilterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/count/ratio?bboxes=8.685824,49.414756,8.686253,49.414955&"
            + "filter2=highway=*&time=2019-01-01",
        JsonNode.class);
    assertEquals(0.2, response.getBody().get("ratioResult").get(0).get("ratio").asDouble(), 1e-6);
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
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate.getForEntity(server + port + "/elements/length?bboxes=8.684692,49.407669,"
            + "8.688061,49.410310&time=2014-01-01&filter=highway=residential", JsonNode.class);
    assertEquals(584.42, response.getBody().get("result").get(0).get("value").asDouble(), 0.0);
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
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/perimeter?bboxes=8.684692,49.407669,8.688061,49.410310"
            + "&time=2014-01-01&filter=building!=flats",
        JsonNode.class);
    assertEquals(9239.88, response.getBody().get("result").get(0).get("value").asDouble(), 0.0);
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
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate.getForEntity(
            server + port + "/elements/area?bboxes=8.684692,49.407669,8.688061,49.410310,"
                + "&time=2014-01-01&filter=building=* and (name!=* or noname!=yes)",
            JsonNode.class);
    assertEquals(20834.12, response.getBody().get("result").get(0).get("value").asDouble(), 0.0);
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
  public void getRequestEndsByQuestionMark() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate.getForEntity(server + port + "/users/count?", JsonNode.class);
    assertEquals(null, response.getBody().get("error"));
  }
}
