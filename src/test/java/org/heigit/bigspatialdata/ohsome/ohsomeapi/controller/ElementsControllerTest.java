package org.heigit.bigspatialdata.ohsome.ohsomeapi.controller;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.Application;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import com.fasterxml.jackson.databind.JsonNode;

public class ElementsControllerTest {

  private static String port = TestProperties.PORT3;
  private String server = TestProperties.SERVER;

  /** Method to start this application context. */
  @BeforeClass
  public static void applicationMainStartup() {
    assumeTrue(TestProperties.PORT3 != null && (TestProperties.INTEGRATION == null
        || !TestProperties.INTEGRATION.equalsIgnoreCase("no")));
    // this instance gets reused by all of the following @Test methods
    Application.main(new String[] {TestProperties.DB_FILE_PATH_PROPERTY, "--port=" + port});
  }

  /*
   * /elements tests
   */

  // test result without setting any tags
  @Test
  public void postElementsUsingNoTagsTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.67452,49.40961,8.70392,49.41823");
    map.add("types", "node");
    map.add("time", "2016-02-05");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements", map, JsonNode.class);
    assertTrue(response.getBody().get("features").get(0).get("properties").get("osm-id").asText()
        .equalsIgnoreCase("node/135742850"));
  }

  // test result with e.g. building=residential
  @Test
  public void getElementsUsingOneTagTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements?bboxes=8.67452,49.40961,8.70392,49.41823&types=way&keys=building"
            + "&values=residential&time=2015-12-01",
        JsonNode.class);
    assertTrue(response.getBody().get("features").get(0).get("properties").get("osm-id").asText()
        .equals("way/140112811"));
  }
}
