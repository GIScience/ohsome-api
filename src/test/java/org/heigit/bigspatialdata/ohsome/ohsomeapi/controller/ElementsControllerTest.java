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
    List<String> params = new LinkedList<>();
    params.add("--port=" + port);
    params.addAll(Arrays.asList(TestProperties.DB_FILE_PATH_PROPERTY.split(" ")));
    // this instance gets reused by all of the following @Test methods
    Application.main(params.toArray(new String[0]));
  }

  /*
   * /elements tests
   */

  @Test
  public void postElementsUsingNoTagsTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.67452,49.40961,8.70392,49.41823");
    map.add("types", "node");
    map.add("time", "2016-02-05");
    map.add("osmMetadata", "yes");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements", map, JsonNode.class);
    assertTrue(StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("features").iterator(),
            Spliterator.ORDERED), false)
        .anyMatch(jsonNode -> jsonNode.get("properties").get("osmId").asText()
            .equalsIgnoreCase("node/135742850")));
  }

  @Test
  public void getElementsUsingOneTagTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements?bboxes=8.67452,49.40961,8.70392,49.41823&types=way&keys=building"
            + "&values=residential&time=2015-12-01&osmMetadata=true",
        JsonNode.class);
    assertTrue(StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("features").iterator(),
            Spliterator.ORDERED), false)
        .anyMatch(jsonNode -> jsonNode.get("properties").get("osmId").asText()
            .equalsIgnoreCase("way/140112811")));
  }

  @Test
  public void getElementsUsingMultipleTagsTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elements?bboxes=8.67559,49.40853,8.69379,49.4231&types=way&keys=highway,"
            + "name,maxspeed&values=residential&time=2015-10-01&osmMetadata=true",
        JsonNode.class);
    assertTrue(StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("features").iterator(),
            Spliterator.ORDERED), false)
        .anyMatch(jsonNode -> jsonNode.get("properties").get("osmId").asText()
            .equalsIgnoreCase("way/4084860")));
  }

  /*
   * /elements/geom tests
   */

  @Test
  public void getElementsGeomTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response =
        restTemplate.getForEntity(
            server + port + "/elements/geom?bboxes=8.67452,49.40961,8.70392,49.41823&types=way"
                + "&keys=building&values=residential&time=2015-01-01&osmMetadata=yes",
            JsonNode.class);
    assertTrue(StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("features").iterator(),
            Spliterator.ORDERED), false)
        .anyMatch(jsonNode -> jsonNode.get("properties").get("osmId").asText()
            .equalsIgnoreCase("way/140112811")));
    assertEquals(3, StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("features").iterator(),
            Spliterator.ORDERED), false)
        .filter(jsonNode -> jsonNode.get("properties").get("osmId").asText()
            .equalsIgnoreCase("way/140112811"))
        .findFirst().get().get("properties").size());
  }
}
