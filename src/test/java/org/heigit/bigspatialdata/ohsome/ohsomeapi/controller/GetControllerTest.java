package org.heigit.bigspatialdata.ohsome.ohsomeapi.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.Application;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;

/** Test class for all of the controller classes sending GET requests. */
public class GetControllerTest {

  private static String port = TestProperties.PORT1;
  private String server = TestProperties.SERVER;

  /** Method to start this application context. */
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

  /*
   * /metadata test
   */

  @Test
  public void getMetadataTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate.getForEntity(server + port + "/metadata", JsonNode.class);
    assertTrue(!response.getBody().get("extractRegion").get("temporalExtent").get("toTimestamp")
        .asText().equals("2018-01-01T00:00:00"));
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
            + "8.68667,49.41353,8.68828,49.414&types=way&time=2017-01-01&keys=building"
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
  public void getElementsCountShareTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate.getForEntity(server + port + "/elements/count/share?bboxes=8.67859,"
            + "49.41189,8.67964,49.41263&types=way&time=2015-01-01&keys=building&keys2="
            + "building&values2=yes", JsonNode.class);
    assertEquals(9, response.getBody().get("shareResult").get(0).get("whole").asInt());
  }

  @Test
  public void getElementsCountShareGroupByBoundaryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/count/share/groupBy/boundary?bboxes=8.68242,49.4127,8.68702,49.41566|"
        + "8.69716,49.41071,8.70534,49.41277&types=way&time=2016-08-11&keys=building"
        + "&keys2=building&values2=residential", JsonNode.class);
    assertEquals(11, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("shareGroupByBoundaryResult").iterator(), Spliterator.ORDERED),
            false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("boundary2"))
        .findFirst().get().get("shareResult").get(0).get("part").asInt());
  }

  @Test
  public void getElementsCountRatioTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/count/ratio?bboxes=8.66004,49.41184,8.68481,49.42094&types=way"
            + "&time=2017-09-20&keys=building&types2=node&keys2=addr:housenumber",
        JsonNode.class);
    assertEquals(0.236186, response.getBody().get("ratioResult").get(0).get("ratio").asDouble(),
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
  public void getElementsLengthShareTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements/length/share?bboxes=8.68297,49.40863,8.69121,49.41016&types=way"
            + "&time=2016-07-25&keys2=highway",
        JsonNode.class);
    assertEquals(4233.37, response.getBody().get("shareResult").get(0).get("part").asDouble(),
        1e-6);
  }

  @Test
  public void getElementsLengthShareGroupByBoundaryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port + "/elements"
        + "/length/share/groupBy/boundary?bboxes=8.68297,49.40863,8.69121,49.41016|8.68477,"
        + "49.39871,8.68949,49.40232&types=way&time=2010-02-03&keys2=highway", JsonNode.class);
    assertTrue(response.getBody().get("shareGroupByBoundaryResult").get(1).get("shareResult").get(0)
        .get("part").asDouble() == 3074.8);
    assertEquals(3074.8, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("shareGroupByBoundaryResult").iterator(), Spliterator.ORDERED),
            false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("boundary2"))
        .findFirst().get().get("shareResult").get(0).get("part").asDouble(), 1e-6);
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
            + "8.67504,49.4119,8.67813,49.41668&types=way&time=2017-05-30&key=highway",
        JsonNode.class);
    assertEquals(74037.2, StreamSupport
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
    assertEquals(5, response.getBody().get("result").get(0).get("value").asInt(), 1e-6);
  }

  @Test
  public void getUsersCountGroupByTypeTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/users/count/groupBy/type?bboxes=8.67,49.39941,8.69545,49.4096&types=way,relation"
        + "&time=2014-01-01,2015-01-01&keys=building", JsonNode.class);
    assertEquals(31,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("way"))
            .findFirst().get().get("result").get(0).get("value").asDouble(),
        0);
  }

  @Test
  public void getUsersCountGroupByKeyTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/users/count/groupBy/key?bboxes=8.67,49.39941,8.69545,49.4096&types=way"
            + "&time=2014-01-01,2015-01-01&groupByKeys=building",
        JsonNode.class);
    assertEquals(31,
        StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(
                response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
            .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("building"))
            .findFirst().get().get("result").get(0).get("value").asDouble(),
        0);
  }

  @Test
  public void getUsersCountGroupByTagTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/users/count/groupBy/tag?bboxes=8.67,49.39941,8.69545,49.4096&types=way"
            + "&time=2014-01-01,2015-01-01&groupByKey=building",
        JsonNode.class);
    assertEquals(30, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("building=yes"))
        .findFirst().get().get("result").get(0).get("value").asDouble(), 1e-6);
  }

  @Test
  public void getUsersCountDensityTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/users/count/density?bboxes=8.67,49.39941,8.69545,49.4096&types=way"
            + "&time=2014-01-01,2015-01-01&keys=building",
        JsonNode.class);
    assertEquals(14.86, response.getBody().get("result").get(0).get("value").asDouble(), 1e-6);
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
        0);
  }

  @Test
  public void getUsersCountDensityGroupByTagTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/users/count/density/groupBy/tag?bboxes=8.67,49.39941,8.69545,49.4096&types=way"
        + "&time=2014-01-01,2015-01-01&groupByKey=building&showMetadata=true", JsonNode.class);
    assertEquals(25.88, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(
            response.getBody().get("groupByResult").iterator(), Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("groupByObject").asText().equalsIgnoreCase("remainder"))
        .findFirst().get().get("result").get(0).get("value").asDouble(), 1e-6);
  }

  /*
   * csv output tests start here
   */

  @Test
  public void getElementsCountCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(server + port
        + "/elements/count?bboxes=8.67452,49.40961,8.70392,49.41823&types=way&time=2015-01-01"
        + "&keys=building&values=residential&format=csv", String.class);
    int length = response.getBody().length();
    assertEquals("40.0", response.getBody().substring(length - 5, length - 1));
  }

  @Test
  public void getElementsCountDensityCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(
        server + port + "/elements/count/density?bboxes=8.66709,49.41237,8.69649,49.42099&"
            + "types=way&time=2015-01-01&keys=building&values=residential&format=csv",
        String.class);
    int length = response.getBody().length();
    assertEquals("8.34", response.getBody().substring(length - 5, length - 1));
  }

  @Test
  public void getElementsCountDensityGroupByBoundaryCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(server + port
        + "/elements/count/density/groupBy/boundary?bboxes=8.6544,49.4085,8.6979,49.4349|"
        + "8.6551,49.3818,8.6986,49.4082&types=way&time=2017-01-01"
        + "&keys=building&values=residential&format=csv", String.class);
    int length = response.getBody().length();
    assertEquals("48.83;30.19", response.getBody().substring(length - 12, length - 1));
  }

  @Test
  public void getElementsCountDensityGroupByBoundaryGroupByTagCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(server + port
        + "/elements/count/density/groupBy/boundary/groupBy/tag?bboxes=b1:8.68086,49.39948,8.69401,"
        + "49.40609|b2:8.68081,49.39943,8.69408,49.40605&types=way&time=2016-11-09&keys=building&"
        + "groupByKey=building&format=csv&groupByValues=garage,residential", String.class);
    String responseBody = response.getBody();
    String[] splittedResponseBody = responseBody.split("\\r?\\n");
    assertEquals(5, splittedResponseBody.length);
    // check on length of header line and data line of csv response
    assertEquals(121, splittedResponseBody[3].length());
    assertEquals(59, splittedResponseBody[4].length());
  }

  @Test
  public void getElementsCountDensityGroupByTagCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(
        server + port + "/elements/count/density/groupBy/tag?bboxes=8.6737,49.413,8.694,49.4258|"
            + "8.6743,49.3958,8.6946,49.4086&format=csv&groupByKey=building&groupByValues="
            + "residential&time=2016-01-01&types=way",
        String.class);
    int length = response.getBody().length();
    assertEquals("41.62", response.getBody().substring(length - 6, length - 1));
  }

  @Test
  public void getElementsCountDensityGroupByTypeCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(
        server + port + "/elements/count/density/groupBy/type?bboxes=8.6544,49.4085,8.6979,49.4349"
            + "&types=way&time=2015-01-01&keys=highway&values=primary&format=csv",
        String.class);
    int length = response.getBody().length();
    assertEquals("13.97", response.getBody().substring(length - 6, length - 1));
  }

  @Test
  public void getElementsCountGroupByBoundaryCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(server + port
        + "/elements/count/groupBy/boundary?bboxes=8.70538,49.40891,8.70832,49.41155|8.68667,"
        + "49.41353,8.68828,49.414&types=way&time=2017-01-01&keys=building"
        + "&values=church&format=csv", String.class);
    int length = response.getBody().length();
    assertEquals("2.0;1.0", response.getBody().substring(length - 8, length - 1));
  }

  @Test
  public void getElementsCountGroupByBoundaryGroupByTagCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(server + port
        + "/elements/count/groupBy/boundary/groupBy/tag?bboxes=8.68086,49.39948,8.69401,49.40609&"
        + "types=way&time=2016-11-09&keys=building&groupByKey=building&groupByValues=yes"
        + "&format=csv", String.class);
    int length = response.getBody().length();
    assertEquals("43.0;931.0", response.getBody().substring(length - 11, length - 1));
  }

  @Test
  public void getElementsCountGroupByKeyCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response =
        restTemplate.getForEntity(
            server + port + "/elements/count/groupBy/key?bboxes=8.6562,49.41243,8.69946,49.42384&"
                + "format=csv&groupByKeys=building,highway&time=2014-01-01&types=way",
            String.class);
    int length = response.getBody().length();
    assertEquals("2292.0;1429.0", response.getBody().substring(length - 14, length - 1));
  }

  @Test
  public void getElementsCountGroupByTagCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(
        server + port + "/elements/count/groupBy/tag?bboxes=8.6562,49.41243,8.69946,49.42384"
            + "&format=csv&groupByKey=highway&groupByValues=tertiary,path&time=2015-01-01&"
            + "types=way",
        String.class);
    int length = response.getBody().length();
    assertEquals("127.0;13.0", response.getBody().substring(length - 11, length - 1));
  }

  @Test
  public void getElementsCountGroupByTypeCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(
        server + port + "/elements/count/groupBy/type?bboxes=8.6562,49.41243,8.69946,49.42384"
            + "&format=csv&time=2016-01-01&types=way,node&keys=amenity&values=restaurant",
        String.class);
    int length = response.getBody().length();
    assertEquals("18.0;7.0", response.getBody().substring(length - 9, length - 1));
  }

  @Test
  public void getElementsCountRatioCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(
        server + port + "/elements/count/ratio?bboxes=8.6773,49.4124,8.6977,49.4351&"
            + "format=csv&keys2=addr:housenumber&time=2014-01-01&types=way&types2=node",
        String.class);
    int length = response.getBody().length();
    assertEquals("4622.0;827.0;0.178927", response.getBody().substring(length - 22, length - 1));
  }

  @Test
  public void getElementsCountRatioGroupByBoundaryCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(server + port
        + "/elements/count/ratio/groupBy/boundary?bboxes=8.6753,49.3857,8.6957,49.4083|"
        + "8.6773,49.4124,8.6977,49.4351&format=csv&keys=building&keys2=addr:housenumber&"
        + "time=2016-01-01&types=way&types2=node&values=residential&values2=2", String.class);
    int length = response.getBody().length();
    assertEquals("122.0;6.0;0.04918;343.0;25.0;0.072886",
        response.getBody().substring(length - 38, length - 1));
  }

  @Test
  public void getElementsCountShareCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate
        .getForEntity(server + port + "/elements/count/share?bboxes=8.6773,49.4124,8.6977,49.4351&"
            + "format=csv&keys=building&keys2=maxspeed&"
            + "time=2017-01-01&types=way&values=residential", String.class);
    int length = response.getBody().length();
    assertEquals("390.0;0.0", response.getBody().substring(length - 10, length - 1));
  }

  @Test
  public void getElementsCountShareGroupByBoundaryCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(server + port
        + "/elements/count/share/groupBy/boundary?bboxes=8.6562,49.41243,8.69946,49.42384|"
        + "8.65053,49.39757,8.69379,49.40899&format=csv&keys=highway&keys2=highway&"
        + "time=2014-01-01&types=way&values2=secondary", String.class);
    int length = response.getBody().length();
    assertEquals("1429.0;7.0;1428.0;136.0", response.getBody().substring(length - 24, length - 1));
  }

  @Test
  public void getElementsLengthGroupByBoundaryGroupByTagCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(server + port
        + "/elements/length/groupBy/boundary/groupBy/tag?bboxes=8.68086,49.39948,8.69401,49.40609"
        + "&types=way&time=2017-11-25&keys=highway&groupByKey=highway&format=csv&groupByValues="
        + "primary,secondary", String.class);
    int length = response.getBody().length();
    assertEquals("2017-11-25T00:00:00Z;15861.34;670.61;2258.59",
        response.getBody().substring(length - 45, length - 1));
  }

  @Test
  public void getElementsLengthDensityGroupByBoundaryGroupByTagCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(server + port
        + "/elements/length/density/groupBy/boundary/groupBy/tag?bboxes=b1:8.68086,49.39948,8.69401"
        + ",49.40609|b2:8.68081,49.39943,8.69408,49.40605&types=way&time=2017-10-08&keys=highway&"
        + "groupByKey=highway&format=csv&groupByValues=residential,primary", String.class);
    int length = response.getBody().length();
    assertEquals("3195.93", response.getBody().substring(length - 8, length - 1));
  }

  @Test
  public void getElementsAreaDensityGroupByTypeCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(
        server + port + "/elements/area/density/groupBy/type?bcircles=8.68136,49.39115,1500"
            + "&format=csv&keys=leisure&time=2018-01-01&types=way,relation",
        String.class);
    int length = response.getBody().length();
    assertEquals("97989.41;12329.71", response.getBody().substring(length - 18, length - 1));
  }

  @Test
  public void getElementsAreaDensityGroupByTagCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response =
        restTemplate.getForEntity(server + port + "/elements/area/density/groupBy/tag?"
            + "bboxes=8.68482,49.40167,8.68721,49.40267&format=csv&groupByKey=building&"
            + "groupByValues=retail,church&time=2018-10-01&types=way", String.class);
    int length = response.getBody().length();
    assertEquals("49279.72;14440.82", response.getBody().substring(length - 18, length - 1));
  }

  @Test
  public void getElementsAreaTypeCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate
        .getForEntity(server + port + "/elements/area/groupBy/type?bcircles=8.689054,49.402481,500&"
            + "format=csv&keys=building&time=2018-01-01&types=way,relation", String.class);
    int length = response.getBody().length();
    assertEquals("209696.95;22111.69", response.getBody().substring(length - 19, length - 1));
  }

  @Test
  public void getElementsAreaRatioCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(
        server + port + "/elements/area/ratio?bboxes=8.68934,49.39415,8.69654,49.39936"
            + "&format=csv&keys=landuse&keys2=building&time=2018-01-01&"
            + "types=way&types2=way&values=cemetery&values2=yes",
        String.class);
    int length = response.getBody().length();
    assertEquals("0.041629", response.getBody().substring(length - 9, length - 1));
  }

  @Test
  public void getElementsAreaShareGroupByBoundaryCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(
        server + port + "/elements/area/share/groupBy/boundary?bboxes=8.68275,49.39993,8.68722,"
            + "49.40517|8.6874,49.39996,8.69188,49.40521&format=csv&keys=leisure&"
            + "keys2=leisure&time=2018-01-01&types=way&types2=way&values2=playground",
        String.class);
    int length = response.getBody().length();
    assertEquals("3892.4199999999996;3651.24;4846.01;612.76",
        response.getBody().substring(length - 42, length - 1));
  }

  @Test
  public void getUsersCountCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(
        server + port + "/users/count?bboxes=8.69338,49.40772,8.71454,49.41251"
            + "&format=csv&keys=shop&time=2014-01-01/2017-01-01/P1Y&types=node&values=clothes",
        String.class);
    int length = response.getBody().length();
    assertEquals(
        "7.0\n" + "2015-01-01T00:00:00Z;2016-01-01T00:00:00Z;7.0\n"
            + "2016-01-01T00:00:00Z;2017-01-01T00:00:00Z;14.0",
        response.getBody().substring(length - 97, length - 1));
  }

  @Test
  public void getUsersCountDensityCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(
        server + port + "users/count/density?bcircles=8.68628,49.41117,200|8.68761,49.40819,200"
            + "&format=csv&keys=wheelchair&time=2014-01-01/2017-01-01/P1Y&types=way&values=yes",
        String.class);
    int length = response.getBody().length();
    assertEquals(
        "28.94\n" + "2015-01-01T00:00:00Z;2016-01-01T00:00:00Z;24.81\n"
            + "2016-01-01T00:00:00Z;2017-01-01T00:00:00Z;28.94",
        response.getBody().substring(length - 102, length - 1));
  }

  @Test
  public void getUsersCountDensityGroupByTypeCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(server + port
        + "users/count/density/groupBy/type?bboxes=8.691773,49.413804,8.692149,49.413975"
        + "&format=csv&keys=addr:housenumber&time=2014-01-01/2017-01-01/P1Y"
        + "&types=way,node&values=5", String.class);
    int length = response.getBody().length();
    assertEquals(
        "2014-01-01T00:00:00Z;2015-01-01T00:00:00Z;3866.95;0.0\n"
            + "2015-01-01T00:00:00Z;2016-01-01T00:00:00Z;0.0;0.0\n"
            + "2016-01-01T00:00:00Z;2017-01-01T00:00:00Z;3866.95;1933.48",
        response.getBody().substring(length - 162, length - 1));
  }

  @Test
  public void getUsersCountGroupByTagCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(
        server + port + "users/count/groupBy/tag?bboxes=8.691865,49.413835,8.692605,49.414756"
            + "&format=csv&groupByKey=shop&time=2015-01-01/2018-01-01/P1Y"
            + "&types=node&groupByValues=clothes,wine",
        String.class);
    String responseBody = response.getBody();
    String[] splittedResponseBody = responseBody.split("\\r?\\n");
    assertEquals(7, splittedResponseBody.length);
    assertEquals(57, splittedResponseBody[4].length());
  }

  @Test
  public void getUsersCountGroupByTypeCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(
        server + port + "users/count/groupBy/type?bboxes=8.700609,49.409336,8.701488,49.409591"
            + "&format=csv&keys=addr:housenumber,addr:street&time=2010-01-01/2013-01-01/P1Y"
            + "&types=way,node&values=,Plöck",
        String.class);
    int length = response.getBody().length();
    assertEquals(
        "2010-01-01T00:00:00Z;2011-01-01T00:00:00Z;3.0;0.0\n"
            + "2011-01-01T00:00:00Z;2012-01-01T00:00:00Z;3.0;2.0\n"
            + "2012-01-01T00:00:00Z;2013-01-01T00:00:00Z;0.0;1.0",
        response.getBody().substring(length - 150, length - 1));
  }
}
