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
  
  public static final String dbProperty = "--database.db=C:\\Users\\kowatsch\\Desktop\\HeiGIT\\oshdb\\data\\withKeytables\\nepal-z15.oshdb";

  /*
   * Simple GET requests
   */

  @Test
  public void getElementsLengthTest() {

    // this instance gets reused by all of the following @Test methods
    // and must be in the first length test (otherwise: connection refused error for /length)
    Application.main(new String[] {dbProperty});
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/length?bboxes=85.2,27.61,85.45,27.81&types=way&time=2012-01-01&keys=highway&values=residential&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("result").get(0).get("value").asDouble() == 336083.1);
  }

  @Test
  public void getElementsCountTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/count?bboxes=85.2,27.61,85.45,27.81&types=way&time=2015-01-01&keys=building&values=residential&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("result").get(0).get("value").asInt() == 839);
  }

  @Test
  public void getElementsPerimeterTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/perimeter?bboxes=85.2,27.61,85.45,27.81&types=way&time=2015-01-01&keys=building&values=residential&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("result").get(0).get("value").asDouble() == 33858.61);
  }

  @Test
  public void getElementsAreaTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/elements/area?bboxes=85.2,27.61,85.45,27.81&types=way&time=2015-01-01&keys=building&values=residential&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("result").get(0).get("value").asDouble() == 86253.43);
  }

  @Test
  public void getUsersCountTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity("http://localhost:8080"
        + "/users/count?bboxes=85.2,27.61,85.45,27.81&types=way&time=2014-01-01,2015-01-01&keys=building&values=residential&showMetadata=true",
        JsonNode.class);
    assertTrue(response.getBody().get("result").get(0).get("value").asInt() == 29);
  }

  @Test
  public void getMetadataTest() {

    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate.getForEntity("http://localhost:8080" + "/metadata", JsonNode.class);
    assertTrue(response.getBody().get("extractRegion").get("temporalExtent").get("toTimestamp")
        .asText().equals("2018-04-24T19:44:44"));
  }

}
