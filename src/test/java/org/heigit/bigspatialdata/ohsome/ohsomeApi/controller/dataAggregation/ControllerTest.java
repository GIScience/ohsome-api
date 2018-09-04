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

  public static final String dbPropertyPathJenkins = "--database.db=heidelberg.oshdb";
  // for local testing
  public static final String dbPropertyPathLocal =
      "--database.db=C:\\Users\\kowatsch\\Desktop\\HeiGIT\\oshdb\\data\\withKeytables\\heidelberg.oshdb";

  /*
   * GET /metadata test
   */

  /** Written with an "a" at the beginning to be the first test to get executed. */
  @Test
  public void aGetMetadataTest() {

    // this instance gets reused by all of the following @Test methods
    Application.main(new String[] {dbPropertyPathJenkins});
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate.getForEntity("http://localhost:8080" + "/metadata", JsonNode.class);
    assertTrue(response.getBody().get("extractRegion").get("temporalExtent").get("toTimestamp")
        .asText().equals("2018-05-10T18:12:35"));
  }

  /*
   * GET /elements/count tests
   */

  @Test
  public void getElementsCountTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/count?bboxes=8.67452,49.40961,8.70392,49.41823&types=way&time=2015-01-01&keys=building&values=residential&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("result").get(0).get("value").asInt() == 40);
  }

  @Test
  public void getElementsCountGroupByBoundaryTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/count/groupBy/boundary?bboxes=8.70538,49.40891,8.70832,49.41155|8.68667,49.41353,8.68828,49.414&types=way&time=2017-01-01&keys=building&values=church&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("groupByResult").get(0).get("result").get(0).get("value")
        .asInt() == 2);
    assertTrue(response.getBody().get("groupByResult").get(1).get("result").get(0).get("value")
        .asInt() == 1);
  }

  @Test
  public void getElementsCountGroupByTypeTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/count/groupBy/type?bboxes=8.67038,49.40341,8.69197,49.40873&types=way,relation&time=2017-01-01&keys=building&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("groupByResult").get(0).get("result").get(0).get("value")
        .asInt() == 967);
    assertTrue(response.getBody().get("groupByResult").get(1).get("result").get(0).get("value")
        .asInt() == 9);
  }

  @Test
  public void getElementsCountGroupByTagTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/count/groupBy/tag?bboxes=8.67859,49.41189,8.67964,49.41263&types=way&time=2017-01-01&keys=building&groupByKey=building&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("groupByResult").get(0).get("result").get(0).get("value")
        .asInt() == 8);
  }

  @Test
  public void getElementsCountGroupByKeyTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/count/groupBy/key?bboxes=8.67859,49.41189,8.67964,49.41263&types=way&time=2012-01-01&groupByKeys=building&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("groupByResult").get(1).get("result").get(0).get("value")
        .asInt() == 7);
  }

  @Test
  public void getElementsCountGroupByUserTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/count/groupBy/user?bboxes=8.67859,49.41189,8.67964,49.41263&types=way&time=2015-01-01&keys=building&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("groupByResult").get(1).get("result").get(0).get("value")
        .asInt() == 4);
  }

  @Test
  public void getElementsCountShareTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/count/share?bboxes=8.67859,49.41189,8.67964,49.41263&types=way&time=2015-01-01&keys2=building&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("shareResult").get(0).get("whole").asInt() == 13);
  }

  @Test
  public void getElementsCountShareGroupByBoundaryTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/count/share/groupBy/boundary?bboxes=8.68242,49.4127,8.68702,49.41566|8.69716,49.41071,8.70534,49.41277&types=way&time=2016-08-11&key=building&keys2=building&values2=residential&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("shareGroupByBoundaryResult").get(1).get("shareResult").get(0)
        .get("part").asInt() == 11);
  }

  @Test
  public void getElementsCountDensityTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/count/density?bboxes=8.68794,49.41434,8.69021,49.41585&types=way&time=2017-08-11&key=building&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("result").get(0).get("value").asDouble() == 4279.69);
  }

  @Test
  public void getElementsCountDensityGroupByBoundaryTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/count/density/groupBy/boundary?bboxes=8.68794,49.41434,8.69021,49.41585|8.67933,49.40505,8.6824,49.40638&types=way&time=2017-08-19&key=building&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("groupByResult").get(1).get("result").get(0).get("value")
        .asDouble() == 1065.44);
  }
  
  @Test
  public void getElementsCountDensityGroupByTypeTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/count/density/groupBy/type?bboxes=8.68086,49.39948,8.69401,49.40609&types=way,node&time=2016-11-09&key=addr:housenumber&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("groupByResult").get(1).get("result").get(0).get("value")
        .asDouble() == 1990.34);
  }
  
  @Test
  public void getElementsCountDensityGroupByTagTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/count/density/groupBy/tag?bboxes=8.68086,49.39948,8.69401,49.40609&types=way&time=2016-11-09&groupByKey=building&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("groupByResult").get(0).get("result").get(0).get("value")
        .asDouble() == 597.67);
  }
  
  @Test
  public void getElementsCountRatioTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/count/ratio?bboxes=8.66004,49.41184,8.68481,49.42094&types=way&time=2017-09-20&key=building&showMetadata=true&types2=node&keys2=addr:housenumber",
        JsonNode.class);
    assertTrue(response.getBody().get("ratioResult").get(0).get("ratio").asDouble() == 0.062339);
  }

  @Test
  public void getElementsCountRatioGroupByBoundaryTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/count/ratio/groupBy/boundary?bcircles=8.66906,49.4167,100|8.69013,49.40223,100&types=way&time=2017-09-20&key=building&showMetadata=true&types2=node&keys2=addr:housenumber",
        JsonNode.class);
    assertTrue(response.getBody().get("groupByBoundaryResult").get(0).get("ratioResult").get(0)
        .get("ratio").asDouble() == 0.210526);
  }

  /*
   * GET /elements/length tests
   */

  @Test
  public void getElementsLengthTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/length?bboxes=8.67452,49.40961,8.70392,49.41823&types=way&time=2012-01-01&keys=highway&values=residential&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("result").get(0).get("value").asDouble() == 15171.81);
  }

  /*
   * GET /elements/perimeter tests
   */

  @Test
  public void getElementsPerimeterTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/perimeter?bboxes=8.67452,49.40961,8.70392,49.41823&types=way&time=2015-01-01&keys=building&values=residential&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("result").get(0).get("value").asDouble() == 2552.21);
  }

  /*
   * GET /elements/area tests
   */

  @Test
  public void getElementsAreaTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/area?bboxes=8.67452,49.40961,8.70392,49.41823&types=way&time=2015-01-01&keys=building&values=residential&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("result").get(0).get("value").asDouble() == 8923.4);
  }

  /*
   * GET /users tests
   */

  @Test
  public void getUsersCountTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/users/count?bboxes=8.67452,49.40961,8.70392,49.41823&types=way&time=2014-01-01,2015-01-01&keys=building&values=residential&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("result").get(0).get("value").asInt() == 5);
  }

}
