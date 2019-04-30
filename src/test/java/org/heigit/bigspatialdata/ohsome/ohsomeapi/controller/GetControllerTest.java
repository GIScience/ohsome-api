package org.heigit.bigspatialdata.ohsome.ohsomeapi.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.Application;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.Assert;


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

  /** Method to get response body as String */
  private String getResponseBody(String urlParams) {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(
        server + port + urlParams,
        String.class);
    String responseBody = response.getBody();
    return responseBody;
  }

  /** Method to create CSV parser, skip comment headers */
  private CSVParser csvParser(String responseBody) throws IOException {
    CSVFormat csvFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(';')
        .withCommentMarker('#');
    CSVParser csvParser = CSVParser.parse(responseBody, csvFormat);
    return csvParser;
  }

  /** Method to get CSV entries */
  private List<CSVRecord> getCSVRecords(String responseBody) throws IOException {
    CSVParser csvParser = csvParser(responseBody);
    List<CSVRecord> records = csvParser.getRecords();
    return  records;
  }

  /** Method to get CSV headers */
  private Map<String, Integer> getCSVHeaders(String responseBody) throws IOException {
    CSVParser csvParser = csvParser(responseBody);
    Map<String, Integer> headers = csvParser.getHeaderMap();
    return  headers;
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
            response.getBody()
                .get("groupByResult")
                .iterator(),
            Spliterator
                .ORDERED), false)
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
            response
                .getBody()
                .get("shareGroupByBoundaryResult")
                .iterator(),
            Spliterator.ORDERED),
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
            response.getBody()
                .get("groupByResult")
                .iterator(), Spliterator
                .ORDERED), false)
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
                response.getBody().get("groupByResult")
                    .iterator(),
                Spliterator.ORDERED), false)
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
            response.getBody()
                .get("groupByResult")
                .iterator(), Spliterator
                .ORDERED), false)
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
   // assertEquals("40.0", response.getBody().substring(length - 5, length - 1));
  }

  @Test
  public void getElementsCountDensityCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(
        server + port + "/elements/count/density?bboxes=8.66709,49.41237,8.69649,49.42099&"
            + "types=way&time=2015-01-01&keys=building&values=residential&format=csv",
        String.class);
    int length = response.getBody().length();
    //assertEquals("8.34", response.getBody().substring(length - 5, length - 1));
  }

  @Test
  public void getElementsCountDensityGroupByBoundaryCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(server + port
        + "/elements/count/density/groupBy/boundary?bboxes=8.6544,49.4085,8.6979,49.4349|"
        + "8.6551,49.3818,8.6986,49.4082&types=way&time=2017-01-01"
        + "&keys=building&values=residential&format=csv", String.class);
    int length = response.getBody().length();
   // assertEquals("48.83;30.19", response.getBody().substring(length - 12, length - 1));
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
   // assertEquals(5, splittedResponseBody.length);
    // check on length of header line and data line of csv response
    //assertEquals(121, splittedResponseBody[3].length());
    //assertEquals(59, splittedResponseBody[4].length());
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
   // assertEquals("41.62", response.getBody().substring(length - 6, length - 1));
  }

  @Test
  public void getElementsCountDensityGroupByTypeCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(
        server + port + "/elements/count/density/groupBy/type?bboxes=8.6544,49.4085,8.6979,49.4349"
            + "&types=way&time=2015-01-01&keys=highway&values=primary&format=csv",
        String.class);
    int length = response.getBody().length();
   // assertEquals("13.97", response.getBody().substring(length - 6, length - 1));
  }

  @Test
  public void getElementsCountGroupByBoundaryCsvTest() throws IOException {
    // expect result to have 1 entry row, with 3 columns and check results against known values
    String responseBody = getResponseBody("/elements/count/groupBy/boundary?"
        + "bboxes=8.672445,49.418337,8.673196,49.419087|"
        + "8.670868,49.418892,8.672188,49.419216&types=node&time=2017-05-01&keys=bicycle_parking"
        + "&values=stands&format=csv");
    List<CSVRecord> records = getCSVRecords(responseBody);
    assertEquals(1, getCSVRecords(responseBody).size());
    Map<String, Integer> headers = getCSVHeaders(responseBody);
    assertEquals(3, headers.size());
    assertEquals(2.0, Double.parseDouble(records.get(0).get("boundary1")),
        0);
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
    List<CSVRecord> records = getCSVRecords(responseBody);
    assertEquals(1, getCSVRecords(responseBody).size());
    Map<String, Integer> headers = getCSVHeaders(responseBody);
    assertEquals(7, headers.size());
    assertEquals(5.0, Double.parseDouble(records.get(0).get("boundary2_natural=tree")),
        0);
  }

  @Test
  public void getElementsCountGroupByKeyCsvTest() throws IOException {
    // expect result to have 1 entry row, with 4 columns and check results against known values
    String responseBody = getResponseBody("/elements/count/groupBy/key?"
        + "bboxes=8.66841,49.40129,8.6728,49.40282&"
        + "format=csv&groupByKeys=female,male&time=2019-01-01&types=node");
    List<CSVRecord> records = getCSVRecords(responseBody);
    assertEquals(1, getCSVRecords(responseBody).size());
    Map<String, Integer> headers = getCSVHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(1.0, Double.parseDouble(records.get(0).get("female")),
        0);
  }

  @Test
  public void getElementsCountGroupByTagCsvTest() throws IOException {
    // expect result to have 1 entry row, with 4 columns and check results against known values
    String responseBody = getResponseBody("/elements/count/groupBy/tag?"
        + "bboxes=8.685459,49.412258,8.689724,49.412868"
        + "&format=csv&groupByKey=amenity&groupByValues=bbq,cafe&time=2019-01-01&"
        + "types=node,way");
    List<CSVRecord> records = getCSVRecords(responseBody);
    assertEquals(1, getCSVRecords(responseBody).size());
    Map<String, Integer> headers = getCSVHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(2.0, Double.parseDouble(records.get(0).get("amenity=bbq")),
        0);
  }


  @Test
  public void getElementsCountGroupByTypeCsvTest() throws IOException {
    // expect result to have 1 entry row, with one timestamp-column and one column per requested type
    // and check results against known values
    String responseBody = getResponseBody("/elements/count/groupBy/type?"
        + "bboxes=8.68748,49.41404,8.69094,49.41458"
        + "&format=csv&time=2016-01-01&types=way,node&keys=amenity&values=restaurant");
    List<CSVRecord> records = getCSVRecords(responseBody);
    assertEquals(1, getCSVRecords(responseBody).size());
    Map<String, Integer> headers = getCSVHeaders(responseBody);
    assertEquals(3, headers.size());
    assertEquals(2.0, Double.parseDouble(records.get(0).get("WAY")),
        0);
  }

  @Test
  public void getElementsCountRatioCsvTest() throws IOException {
    // expect result to have 1 entry row, with 4 columns and check results against known values
    String responseBody = getResponseBody("/elements/count/ratio?"
        + "bboxes=8.689317,49.395149,8.689799,49.395547&format=csv&keys=building&"
        + "keys2=addr:housenumber&time=2018-01-01&types=way&types2=node");
    List<CSVRecord> records = getCSVRecords(responseBody);
    assertEquals(1, getCSVRecords(responseBody).size());
    Map<String, Integer> headers = getCSVHeaders(responseBody);
    assertEquals(4, headers.size());
    assertEquals(0.2, Double.parseDouble(records.get(0).get("ratio")),
        0);
  }

  @Test
  public void getElementsCountRatioGroupByBoundaryCsvTest() throws IOException {
    // expect result to have 1 entry row, with columns for: timestamp ,(
    // per boundary:
    // key=value , key2=value2, ratio)
    String responseBody = getResponseBody("/elements/count/ratio/groupBy/boundary?"
        + "bboxes=8.65917,49.39534,8.66428,49.40019|"
        + "8.65266,49.40178,8.65400,49.40237&format=csv&keys=highway&keys2=name&"
        + "time=2018-01-01&types=way&types2=way");
    List<CSVRecord> records = getCSVRecords(responseBody);
    assertEquals(1, getCSVRecords(responseBody).size());
    Map<String, Integer> headers = getCSVHeaders(responseBody);
    assertEquals(7, headers.size());
    assertEquals(0.6, Double.parseDouble(records.get(0).get("boundary2_ratio")),
        0.0001);
  }

  @Test
  public void getElementsCountShareCsvTest() throws IOException {
    // expect result to have 1 entry row, with 3 columns and check results against known values
    String responseBody = getResponseBody("/elements/count/share?"
        + "bboxes=8.68517,49.39356,8.68588,49.39516&"
        + "format=csv&keys=highway&keys2=maxspeed&"
        + "time=2017-01-01&types=way");
    List<CSVRecord> records = getCSVRecords(responseBody);
    assertEquals(1, getCSVRecords(responseBody).size());
    Map<String, Integer> headers = getCSVHeaders(responseBody);
    assertEquals(3, headers.size());
    assertEquals(19.0, Double.parseDouble(records.get(0).get("whole")),
        0);
    assertEquals(4.0, Double.parseDouble(records.get(0).get("part")),
        0);
  }

  @Test
  public void getElementsCountShareGroupByBoundaryCsvTest() throws IOException {
    // expect result to have 1 entry row, with columns for: timestamp + (
    // per boundary:
    // count of elements with key=value,  count of of elements with key=value AND key2=value2)
    String responseBody = getResponseBody("/elements/count/share/groupBy/boundary?"
        + "bboxes=b1:8.68593,49.39461,8.68865,49.39529|"
        + "b2:8.68885,49.39450,8.68994,49.39536&format=csv&keys=highway&keys2=highway&"
        + "time=2017-12-01&types=way&values2=service");
    List<CSVRecord> records = getCSVRecords(responseBody);
    assertEquals(1, getCSVRecords(responseBody).size());
    Map<String, Integer> headers = getCSVHeaders(responseBody);
    assertEquals(5, headers.size());
  }

  @Test
  public void getElementsLengthGroupByBoundaryGroupByTagCsvTest() throws IOException {
    // expect result to have 1 entry row, with columns for: timestamp ,(
    // per boundary:
    // remainder , value 1 , ... , value N)
    String responseBody = getResponseBody("/elements/length/groupBy/boundary/groupBy/tag?"
        + "bboxes=bboxes=b1:8.68593,49.39461,8.68865,49.39529|b2:8.68885,49.39450,8.68994,49.39536"
        + "&types=way&time=2017-11-25&keys=highway&groupByKey=highway&format=csv&groupByValues="
        + "service,residential");
    List<CSVRecord> records = getCSVRecords(responseBody);
    assertEquals(1, getCSVRecords(responseBody).size());
    Map<String, Integer> headers = getCSVHeaders(responseBody);
    assertEquals(7, headers.size());
    assertEquals(44.37, Double.parseDouble(records.get(0).get("b2_highway=service")),
        0.01);
  }

  @Test
  public void getElementsLengthDensityGroupByBoundaryGroupByTagCsvTest() throws IOException {
    // expect result to have 1 entry row, with columns for: timestamp , (
    // per boundary:
    // remainder , value 1 , ... , value N)
    String responseBody = getResponseBody("/elements/length/density/groupBy/boundary"
        + "/groupBy/tag?bboxes=b1:8.68086,49.39948,8.69401"
        + ",49.40609|b2:8.68081,49.39943,8.69408,49.40605&types=way&time=2017-10-08&keys=highway&"
        + "groupByKey=highway&format=csv&groupByValues=residential,primary");
    List<CSVRecord> records = getCSVRecords(responseBody);
    assertEquals(1, getCSVRecords(responseBody).size());
    Map<String, Integer> headers = getCSVHeaders(responseBody);
    assertEquals(7, headers.size());
    assertEquals(3195.93, Double.parseDouble(records.get(0).get("b2_highway=primary")),
        0.01);
  }

  @Test
  public void getElementsAreaDensityGroupByTypeCsvTest() throws IOException {
    // group by type: expect result to have one column per requested type and check results
    // against known values
    String responseBody = getResponseBody("/elements/area/density/groupBy/type?"
        + "bcircles=8.68250,49.39384,300"
        + "&format=csv&keys=leisure&time=2018-01-01&types=way,relation");
    List<CSVRecord> records = getCSVRecords(responseBody);
    assertEquals(1, getCSVRecords(responseBody).size());
    Map<String, Integer> headers = getCSVHeaders(responseBody);
    assertTrue(!headers.containsKey("NODE"));
    assertEquals(3, headers.size());
    assertEquals(264812.45, Double.parseDouble(records.get(0).get("WAY")), 0.01);
    assertEquals(120015.73, Double.parseDouble(records.get(0).get("RELATION")), 0.01);
  }

  @Test
  public void getElementsAreaDensityGroupByTagCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response =
        restTemplate.getForEntity(server + port + "/elements/area/density/groupBy/tag?"
            + "bboxes=8.68482,49.40167,8.68721,49.40267&format=csv&groupByKey=building&"
            + "groupByValues=retail,church&time=2018-10-01&types=way", String.class);
    String responseBody = response.getBody();
    String[] splittedResponseBody = responseBody.split("\\r?\\n");
   // assertEquals(5, splittedResponseBody.length);
   // assertEquals(51, splittedResponseBody[3].length());
    //assertEquals(49, splittedResponseBody[4].length());
  }

  @Test
  public void getElementsAreaTypeCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate
        .getForEntity(server + port + "/elements/area/groupBy/type?bcircles=8.689054,49.402481,500&"
            + "format=csv&keys=building&time=2018-01-01&types=way,relation", String.class);
    String responseBody = response.getBody();
    String[] splittedResponseBody = responseBody.split("\\r?\\n");
   // assertEquals(5, splittedResponseBody.length);
    //assertEquals(39, splittedResponseBody[4].length());
  }

  @Test
  public void getElementsAreaRatioCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(
        server + port + "/elements/area/ratio?bboxes=8.68934,49.39415,8.69654,49.39936"
            + "&format=csv&keys=landuse&keys2=building&time=2018-01-01&"
            + "types=way&types2=way&values=cemetery&values2=yes",
        String.class);
    // /elements/area/ratio?bboxes=8.68934,49.39415,8.69654,49.39936&
    // format=csv&keys=landuse&keys2=building&time=2018-01-01&types=way&types2=way&values=cemetery&values2=yes
    String responseBody = response.getBody();
    String[] splittedResponseBody = responseBody.split("\\r?\\n");
   // assertEquals(5, splittedResponseBody.length);
   // assertEquals(41, splittedResponseBody[4].length());
  }

  @Test
  public void getElementsAreaShareGroupByBoundaryCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(
        server + port + "/elements/area/share/groupBy/boundary?bboxes=8.68275,49.39993,8.68722,"
            + "49.40517|8.6874,49.39996,8.69188,49.40521&format=csv&keys=leisure&"
            + "keys2=leisure&time=2018-01-01&types=way&types2=way&values2=playground",
        String.class);
    String responseBody = response.getBody();
    String[] splittedResponseBody = responseBody.split("\\r?\\n");
    //assertEquals(5, splittedResponseBody.length);
    //assertEquals(61, splittedResponseBody[4].length());
  }

  @Test
  public void getUsersCountCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(
        server + port + "/users/count?bboxes=8.69338,49.40772,8.71454,49.41251"
            + "&format=csv&keys=shop&time=2014-01-01/2017-01-01/P1Y&types=node&values=clothes",
        String.class);
    String responseBody = response.getBody();
    String[] splittedResponseBody = responseBody.split("\\r?\\n");
    //assertEquals(7, splittedResponseBody.length);
    //assertEquals(45, splittedResponseBody[4].length());
  }

  @Test
  public void getUsersCountDensityCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(
        server + port + "users/count/density?bcircles=8.68628,49.41117,200|8.68761,49.40819,200"
            + "&format=csv&keys=wheelchair&time=2014-01-01/2017-01-01/P1Y&types=way&values=yes",
        String.class);
    String responseBody = response.getBody();
    String[] splittedResponseBody = responseBody.split("\\r?\\n");
   // assertEquals(7, splittedResponseBody.length);
    //assertEquals(48, splittedResponseBody[4].length());
  }

  @Test
  public void getUsersCountDensityGroupByTypeCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(server + port
        + "users/count/density/groupBy/type?bboxes=8.691773,49.413804,8.692149,49.413975"
        + "&format=csv&keys=addr:housenumber&time=2014-01-01/2017-01-01/P1Y"
        + "&types=way,node&values=5", String.class);
    String responseBody = response.getBody();
    String[] splittedResponseBody = responseBody.split("\\r?\\n");
   // assertEquals(7, splittedResponseBody.length);
   // assertEquals(53, splittedResponseBody[4].length());
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
    //assertEquals(7, splittedResponseBody.length);
    //assertEquals(57, splittedResponseBody[4].length());
  }

  @Test
  public void getUsersCountGroupByTypeCsvTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response = restTemplate.getForEntity(
        server + port + "users/count/groupBy/type?bboxes=8.700609,49.409336,8.701488,49.409591"
            + "&format=csv&keys=addr:housenumber,addr:street&time=2010-01-01/2013-01-01/P1Y"
            + "&types=way,node&values=,Plöck",
        String.class);
    String responseBody = response.getBody();
    String[] splittedResponseBody = responseBody.split("\\r?\\n");
    //assertEquals(7, splittedResponseBody.length);
    //assertEquals(50, splittedResponseBody[4].length());
  }
}
