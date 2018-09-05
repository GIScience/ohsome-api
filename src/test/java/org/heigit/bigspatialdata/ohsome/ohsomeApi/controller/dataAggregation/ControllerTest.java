package org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation;

import static org.junit.Assert.assertTrue;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.Application;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Test class for all of the controller classes.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest(classes = Application.class)
public class ControllerTest {
  // for local testing
  public static String dbPropertyPathLocal =
      "--database.db=C:\\Users\\kowatsch\\Desktop\\HeiGIT\\oshdb\\data"
          + "\\withKeytables\\heidelberg.oshdb";
  public static String dbPropertyPathJenkins = "--database.db=/opt/data/heidelberg.oshdb";
  public static String localhost = "http://localhost:";
  public static String port = "8081";

  /*
   * GET /metadata test
   */

  /** Written with an "a" at the beginning to be the first test to get executed. */
  @Test
  public void aGetMetadataTest() {
    // this instance gets reused by all of the following @Test methods
    Application.main(new String[] {dbPropertyPathJenkins, "--port=" + port});
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate.getForEntity(localhost + port + "/metadata", JsonNode.class);
    assertTrue(response.getBody().get("extractRegion").get("temporalExtent").get("toTimestamp")
        .asText().equals("2018-05-10T18:12:35"));
  }

  /*
   * GET /elements/count tests
   */

  @Test
  public void getElementsCountTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(localhost + port
        + "/elements/count?bboxes=8.67452,49.40961,8.70392,49.41823&types=way&time=2015-01-01"
        + "&keys=building&values=residential&showMetadata=true", JsonNode.class);
    assertTrue(response.getBody().get("result").get(0).get("value").asInt() == 40);
  }

  @Test
  public void getElementsCountGroupByBoundaryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(localhost + port
        + "/elements/count/groupBy/boundary?bboxes=8.70538,49.40891,8.70832,49.41155|"
        + "8.68667,49.41353,8.68828,49.414&types=way&time=2017-01-01&keys=building"
        + "&values=church&showMetadata=true", JsonNode.class);
    assertTrue(response.getBody().get("groupByResult").get(0).get("result").get(0).get("value")
        .asInt() == 2);
    assertTrue(response.getBody().get("groupByResult").get(1).get("result").get(0).get("value")
        .asInt() == 1);
  }

  @Test
  public void getElementsCountGroupByTypeTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        localhost + port + "/elements/count/groupBy/type?bboxes=8.67038,49.40341,8.69197,49.40873"
            + "&types=way,relation&time=2017-01-01&keys=building&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("groupByResult").get(0).get("result").get(0).get("value")
        .asInt() == 967);
    assertTrue(response.getBody().get("groupByResult").get(1).get("result").get(0).get("value")
        .asInt() == 9);
  }

  @Test
  public void getElementsCountGroupByTagTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        localhost + port + "/elements/count/groupBy/tag?bboxes=8.67859,49.41189,8.67964,49.41263"
            + "&types=way&time=2017-01-01&keys=building&groupByKey=building&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("groupByResult").get(0).get("result").get(0).get("value")
        .asInt() == 8);
  }

  @Test
  public void getElementsCountGroupByKeyTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        localhost + port + "/elements/count/groupBy/key?bboxes=8.67859,49.41189,8.67964,49.41263"
            + "&types=way&time=2012-01-01&groupByKeys=building&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("groupByResult").get(1).get("result").get(0).get("value")
        .asInt() == 7);
  }

  @Test
  public void getElementsCountGroupByUserTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        localhost + port + "/elements/count/groupBy/user?bboxes=8.67859,49.41189,8.67964,49.41263"
            + "&types=way&time=2015-01-01&keys=building&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("groupByResult").get(1).get("result").get(0).get("value")
        .asInt() == 4);
  }

  @Test
  public void getElementsCountShareTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate
            .getForEntity(
                localhost + port + "/elements/count/share?bboxes=8.67859,49.41189,8.67964,49.41263"
                    + "&types=way&time=2015-01-01&keys2=building&showMetadata=true",
                JsonNode.class);
    assertTrue(response.getBody().get("shareResult").get(0).get("whole").asInt() == 13);
  }

  @Test
  public void getElementsCountShareGroupByBoundaryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(localhost + port
        + "/elements/count/share/groupBy/boundary?bboxes=8.68242,49.4127,8.68702,49.41566|"
        + "8.69716,49.41071,8.70534,49.41277&types=way&time=2016-08-11&key=building"
        + "&keys2=building&values2=residential&showMetadata=true", JsonNode.class);
    assertTrue(response.getBody().get("shareGroupByBoundaryResult").get(1).get("shareResult").get(0)
        .get("part").asInt() == 11);
  }

  @Test
  public void getElementsCountRatioTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        localhost + port
            + "/elements/count/ratio?bboxes=8.66004,49.41184,8.68481,49.42094&types=way"
            + "&time=2017-09-20&key=building&showMetadata=true&types2=node&keys2=addr:housenumber",
        JsonNode.class);
    assertTrue(response.getBody().get("ratioResult").get(0).get("ratio").asDouble() == 0.062339);
  }

  @Test
  public void getElementsCountRatioGroupByBoundaryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        localhost + port + "/elements/count/ratio/groupBy/boundary?bcircles=8.66906,49.4167,100|"
            + "8.69013,49.40223,100&types=way&time=2017-09-20&key=building&showMetadata=true"
            + "&types2=node&keys2=addr:housenumber",
        JsonNode.class);
    assertTrue(response.getBody().get("groupByBoundaryResult").get(0).get("ratioResult").get(0)
        .get("ratio").asDouble() == 0.210526);
  }

  @Test
  public void getElementsCountDensityTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        localhost + port + "/elements/count/density?bboxes=8.68794,49.41434,8.69021,49.41585"
            + "&types=way&time=2017-08-11&key=building&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("result").get(0).get("value").asDouble() == 4279.69);
  }

  @Test
  public void getElementsCountDensityGroupByBoundaryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(localhost + port
        + "/elements/count/density/groupBy/boundary?bboxes=8.68794,49.41434,8.69021,49.41585|"
        + "8.67933,49.40505,8.6824,49.40638&types=way&time=2017-08-19"
        + "&key=building&showMetadata=true", JsonNode.class);
    assertTrue(response.getBody().get("groupByResult").get(1).get("result").get(0).get("value")
        .asDouble() == 1065.44);
  }

  @Test
  public void getElementsCountDensityGroupByTypeTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        localhost + port
            + "/elements/count/density/groupBy/type?bboxes=8.68086,49.39948,8.69401,49.40609"
            + "&types=way,node&time=2016-11-09&key=addr:housenumber&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("groupByResult").get(1).get("result").get(0).get("value")
        .asDouble() == 1990.34);
  }

  @Test
  public void getElementsCountDensityGroupByTagTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(localhost + port
        + "/elements/count/density/groupBy/tag?bboxes=8.68086,49.39948,8.69401,49.40609"
        + "&types=way&time=2016-11-09&groupByKey=building&showMetadata=true", JsonNode.class);
    assertTrue(response.getBody().get("groupByResult").get(0).get("result").get(0).get("value")
        .asDouble() == 597.67);
  }

  /*
   * GET /elements/length tests
   */

  @Test
  public void getElementsLengthTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate.getForEntity(
            localhost + port + "/elements/length?bboxes=8.67452,49.40961,8.70392,49.41823&types=way"
                + "&time=2012-01-01&keys=highway&values=residential&showMetadata=true",
            JsonNode.class);
    assertTrue(response.getBody().get("result").get(0).get("value").asDouble() == 15171.81);
  }

  @Test
  public void getElementsLengthGroupByBoundaryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        localhost + port
            + "/elements/length/groupBy/boundary?bboxes=8.695443,49.408928,8.695636,49.409151|"
            + "8.699262,49.409451,8.701547,49.412205&types=way&time=2014-08-21&keys=highway",
        JsonNode.class);
    assertTrue(response.getBody().get("groupByResult").get(0).get("result").get(0).get("value")
        .asDouble() == 25.5);
  }

  @Test
  public void getElementsLengthGroupByTypeTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(localhost + port
        + "/elements/length/groupBy/type?bboxes=8.701665,49.408802,8.703999,49.409553"
        + "&types=way,node&time=2014-08-21&keys=highway", JsonNode.class);
    assertTrue(response.getBody().get("groupByResult").get(0).get("result").get(0).get("value")
        .asInt() == 0);
  }

  @Test
  public void getElementsLengthGroupByKeyTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        localhost + port + "/elements/length/groupBy/key?bboxes=8.67181,49.40434,8.67846,49.40878"
            + "&types=way&time=2016-08-21&groupByKeys=highway,railway",
        JsonNode.class);
    assertTrue(response.getBody().get("groupByResult").get(0).get("result").get(0).get("value")
        .asDouble() == 3132.95);
  }

  @Test
  public void getElementsLengthGroupByTagTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(localhost + port
        + "/elements/length/groupBy/tag?bboxes=8.70773,49.40832,8.71413,49.41092&types=way"
        + "&time=2016-08-21&groupByKey=highway", JsonNode.class);
    assertTrue(response.getBody().get("groupByResult").get(0).get("result").get(0).get("value")
        .asDouble() == 2779.09);
  }

  @Test
  public void getElementsLengthGroupByUserTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        localhost + port + "/elements/length/groupBy/user?bboxes=8.68658,49.39316,8.69881,49.40511"
            + "&types=way&time=2014-04-03&keys=highway",
        JsonNode.class);
    assertTrue(response.getBody().get("groupByResult").get(0).get("result").get(0).get("value")
        .asDouble() == 134.65);
  }

  @Test
  public void getElementsLengthShareTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(localhost + port
        + "/elements/length/share?bboxes=8.68297,49.40863,8.69121,49.41016&types=way"
        + "&time=2016-07-25&keys2=highway", JsonNode.class);
    assertTrue(response.getBody().get("shareResult").get(0).get("part").asDouble() == 4233.42);
  }

  @Test
  public void getElementsLengthShareGroupByBoundaryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(localhost + port + "/elements"
        + "/length/share/groupBy/boundary?bboxes=8.68297,49.40863,8.69121,49.41016|8.68477,"
        + "49.39871,8.68949,49.40232&types=way&time=2010-02-03&keys2=highway", JsonNode.class);
    assertTrue(response.getBody().get("shareGroupByBoundaryResult").get(1).get("shareResult").get(0)
        .get("part").asDouble() == 3074.8);
  }

  @Test
  public void getElementsLengthRatioTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        localhost + port + "/elements" + "/length/ratio?bboxes=8.67567,49.40695,8.69434,49.40882"
            + "&types=way&time=2011-12-13&keys=highway&keys2=railway",
        JsonNode.class);
    assertTrue(response.getBody().get("ratioResult").get(0).get("ratio").asDouble() == 0.135225);
  }

  @Test
  public void getElementsLengthRatioGroupByBoundaryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(localhost + port + "/elements"
        + "/length/ratio/groupBy/boundary?bboxes=8.67829,49.39807,8.69061,49.40578|"
        + "8.68306,49.42407,8.68829,49.42711&types=way&time=2012-12-22&keys=highway&keys2=railway",
        JsonNode.class);
    assertTrue(response.getBody().get("groupByBoundaryResult").get(0).get("ratioResult").get(0)
        .get("ratio").asDouble() == 0.47867);
  }

  @Test
  public void getElementsLengthDensityTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        localhost + port + "/elements/length/density?bboxes=8.70538,49.40464,8.71264,49.41042"
            + "&types=way&time=2013-01-04&keys=highway",
        JsonNode.class);
    assertTrue(response.getBody().get("result").get(0).get("value").asDouble() == 29022.41);
  }

  @Test
  public void getElementsLengthDensityGroupByTypeTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(localhost + port
        + "/elements/length/density/groupBy/type?bboxes=8.68242,49.40059,8.68732,49.4059"
        + "&types=way,node&time=2015-03-25", JsonNode.class);
    assertTrue(response.getBody().get("groupByResult").get(1).get("result").get(0).get("value")
        .asDouble() == 47849.51);
  }

  @Test
  public void getElementsLengthDensityGroupByTagTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(localhost + port
        + "/elements/length/density/groupBy/tag?bboxes=8.66972,49.40453,8.67564,49.4076"
        + "&types=way&time=2016-01-17&groupByKey=railway", JsonNode.class);
    assertTrue(response.getBody().get("groupByResult").get(0).get("result").get(0).get("value")
        .asDouble() == 60235.78);
  }

  @Test
  public void getElementsLengthDensityGroupByBoundaryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        localhost + port
            + "/elements/length/density/groupBy/boundary?bboxes=8.69079,49.40129,8.69238,49.40341|"
            + "8.67504,49.4119,8.67813,49.41668&types=way&time=2017-05-30&key=highway",
        JsonNode.class);
    assertTrue(response.getBody().get("groupByResult").get(1).get("result").get(0).get("value")
        .asDouble() == 74036.22);
  }

  /*
   * GET /elements/perimeter tests
   */

  @Test
  public void getElementsPerimeterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        localhost + port + "/elements/perimeter?bboxes=8.67452,49.40961,8.70392,49.41823"
            + "&types=way&time=2015-01-01&keys=building&values=residential&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("result").get(0).get("value").asDouble() == 2552.21);
  }

  /*
   * GET /elements/area tests
   */

  @Test
  public void getElementsAreaTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        localhost + port + "/elements/area?bboxes=8.67452,49.40961,8.70392,49.41823&types=way"
            + "&time=2015-01-01&keys=building&values=residential&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("result").get(0).get("value").asDouble() == 8923.4);
  }

  /*
   * GET /users tests
   */

  @Test
  public void getUsersCountTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        localhost + port + "/users/count?bboxes=8.67452,49.40961,8.70392,49.41823&types=way"
            + "&time=2014-01-01,2015-01-01&keys=building&values=residential&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("result").get(0).get("value").asInt() == 5);
  }
}
