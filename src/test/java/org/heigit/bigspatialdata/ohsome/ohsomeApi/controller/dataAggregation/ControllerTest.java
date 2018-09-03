package org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation;

import static org.junit.Assert.assertTrue;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.Application;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Test class for all of the controller classes.
 */
@SpringBootTest(classes = Application.class)
public class ControllerTest {
  
  public static final String dbPropertyPathJenkins = "--database.db=heidelberg.oshdb";
  // for local testing
  public static final String dbPropertyPathLocal = "--database.db=C:\\Users\\kowatsch\\Desktop\\HeiGIT\\oshdb\\data\\withKeytables\\heidelberg.oshdb";

  /*
   * Simple GET requests
   */

  @Test
  public void getElementsLengthTest() {

    // this instance gets reused by all of the following @Test methods
    // and must be in the first length test (otherwise: connection refused error for /length)
    Application.main(new String[] {dbPropertyPathJenkins});
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/length?bboxes=8.67452,49.40961,8.70392,49.41823&types=way&time=2012-01-01&keys=highway&values=residential&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("result").get(0).get("value").asDouble() == 15171.81);
  }

  @Test
  public void getElementsCountTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/count?bboxes=8.67452,49.40961,8.70392,49.41823&types=way&time=2015-01-01&keys=building&values=residential&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("result").get(0).get("value").asInt() == 40);
  }

  @Test
  public void getElementsPerimeterTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/perimeter?bboxes=8.67452,49.40961,8.70392,49.41823&types=way&time=2015-01-01&keys=building&values=residential&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("result").get(0).get("value").asDouble() == 2552.21);
  }

  @Test
  public void getElementsAreaTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/area?bboxes=8.67452,49.40961,8.70392,49.41823&types=way&time=2015-01-01&keys=building&values=residential&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("result").get(0).get("value").asDouble() == 8923.4);
  }

  @Test
  public void getUsersCountTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/users/count?bboxes=8.67452,49.40961,8.70392,49.41823&types=way&time=2014-01-01,2015-01-01&keys=building&values=residential&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("result").get(0).get("value").asInt() == 5);
  }

  @Test
  public void getMetadataTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate.getForEntity("http://localhost:8080" + "/metadata", JsonNode.class);
    assertTrue(response.getBody().get("extractRegion").get("temporalExtent").get("toTimestamp")
        .asText().equals("2018-05-10T18:12:35"));
  }

}
