package org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation;

import static org.junit.Assert.assertTrue;
import com.fasterxml.jackson.databind.JsonNode;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.Application;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


/** Test class for all of the controller classes sending POST requests. */
@SpringBootTest(classes = Application.class)
public class PostControllerTest {

  /*
   * /elements/count tests
   */

  @Test
  public void elementsCountTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.67452,49.40961,8.70392,49.41823");
    map.add("types", "way");
    map.add("time", "2015-01-01");
    map.add("keys", "building");
    map.add("values", "residential");
    ResponseEntity<JsonNode> response = restTemplate.postForEntity(
        GetControllerTest.localhost + GetControllerTest.port + "/elements/count", map,
        JsonNode.class);
    assertTrue(response.getBody().get("result").get(0).get("value").asInt() == 40);
  }

  @Test
  public void elementsCountGroupByBoundaryGeoJsonTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bpolys",
        "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{},"
            + "\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[8.68494,49.41951],"
            + "[8.67902,49.41460],[8.69009,49.41527],[8.68494,49.41951]]]}},{\"type\":\"Feature\","
            + "\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[8.68812,"
            + "49.40466],[8.68091,49.40058],[8.69121,49.40069],[8.68812,49.40466]]]}}]}");
    map.add("types", "way");
    map.add("time", "2016-01-01");
    map.add("keys", "building");
    map.add("format", "geojson");
    ResponseEntity<JsonNode> response = restTemplate.postForEntity(
        GetControllerTest.localhost + GetControllerTest.port + "/elements/count/groupBy/boundary",
        map, JsonNode.class);
    assertTrue(
        response.getBody().get("features").get(0).get("properties").get("value").asInt() == 367);
  }
}
