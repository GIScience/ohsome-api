package org.heigit.ohsome.ohsomeapi.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.heigit.ohsome.ohsomeapi.Application;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/** Test class for the data extraction requests. */
public class DataExtractionTest {

  private static String port = TestProperties.PORT3;
  private String server = TestProperties.SERVER;

  /** Starts this application context. */
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

  /** Stops this application context. */
  @AfterClass
  public static void applicationMainShutdown() {
    if (Application.getApplicationContext() != null) {
      SpringApplication.exit(Application.getApplicationContext(), () -> 0);
    }
  }

  /*
   * ./elements/geometry tests
   */

  @Test
  public void elementsGeometryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/geometry?bboxes=8.67452,49.40961,8.70392,49.41823"
        + "&time=2015-01-01&properties=metadata&filter=type:way and building=residential",
        JsonNode.class);
    JsonNode feature = Helper.getFeatureByIdentifier(response, "@osmId", "way/140112811");
    assertEquals(6, feature.get("properties").size());
  }

  @Test
  public void elementsGeomUsingOneTagTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/geometry?bboxes=8.67452,49.40961,8.70392,49.41823&time=2015-12-01"
        + "&properties=metadata&filter=type:way and building=residential", JsonNode.class);
    assertTrue(Helper.getFeatureByIdentifier(response, "@osmId", "way/140112811") != null);
  }

  @Test
  public void elementsGeomUsingMultipleTagsTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/geometry?bboxes=8.67559,49.40853,8.69379,49.4231&time=2015-10-01"
        + "&properties=metadata&filter=type:way and highway=residential and maxspeed=* and name=*",
        JsonNode.class);
    assertTrue(Helper.getFeatureByIdentifier(response, "@osmId", "way/4084860") != null);
  }

  @Test
  public void elementsGeomSimpleFeaturesOtherLineTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
            + "/elements/geometry?bboxes=8.700582,49.4143039,8.701247,49.414994"
            + "&time=2019-01-02&filter=building=* and (geometry:other or geometry:line)",
        JsonNode.class);
    assertTrue("GeometryCollection"
        .equals(response.getBody().get("features").get(0).get("geometry").get("type").asText()));
  }

  @Test
  public void elementsGeomUsingNoTagsTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.67452,49.40961,8.70392,49.41823");
    map.add("filter", "type:node");
    map.add("time", "2016-02-05");
    map.add("properties", "metadata");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/geometry", map, JsonNode.class);
    assertTrue(Helper.getFeatureByIdentifier(response, "@osmId", "node/135742850") != null);
  }

  @Test
  public void elementsBboxTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/bbox?bboxes=8.67452,49.40961,8.70392,49.41823&time=2015-01-01"
        + "&properties=metadata&filter=type:way and building=residential", JsonNode.class);
    JsonNode featureGeom =
        Helper.getFeatureByIdentifier(response, "@osmId", "way/294644468").get("geometry");
    assertEquals("Polygon", featureGeom.get("type").asText());
    assertEquals(5, featureGeom.get("coordinates").get(0).size());
  }

  @Test
  public void checkResponseMessageForWrongPropertiesParam() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/bbox?bboxes=8.67,49.39,8.71,49.42&clipGeometry=true&"
        + "filter=type:way and natural=*&properties=contributionTypes&time=2016-04-20,2016-04-21",
        JsonNode.class);
    assertEquals("\"The properties parameter of this resource can only contain the values 'tags' "
        + "and/or 'metadata' and/or 'unclipped'.\"", response.getBody().get("message").toString());
  }

  @Test
  public void elementsCentroidTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/centroid?bboxes=8.67452,49.40961,8.70392,49.41823&time=2015-01-01"
        + "&properties=metadata&filter=type:way and building=residential", JsonNode.class);
    assertEquals(2, Helper.getFeatureByIdentifier(response, "@osmId", "way/294644468")
        .get("geometry").get("coordinates").size());
  }

  @Test
  public void elementsClipGeometryParamTrueFalseTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    String uri = "/elements/geometry?bboxes=8.700582,49.4143039,8.701247,49.414994&time=2018-01-02"
        + "&filter=building=* and (geometry:other or geometry:line)";
    ResponseEntity<JsonNode> clipGeometryFalseResponse =
        restTemplate.getForEntity(server + port + uri + "&clipGeometry=false", JsonNode.class);
    ResponseEntity<JsonNode> clipGeometryTrueResponse =
        restTemplate.getForEntity(server + port + uri + "&clipGeometry=true", JsonNode.class);
    assertEquals("Polygon", clipGeometryFalseResponse.getBody().get("features").get(0)
        .get("geometry").get("type").asText());
    assertEquals("GeometryCollection", clipGeometryTrueResponse.getBody().get("features").get(0)
        .get("geometry").get("type").asText());
  }

  /*
   * ./elementsFullHistory/geometry|bbox|centroid tests
   */

  @Test
  public void elementsGeometryCoordinateTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/geometry?bboxes=8.68641,49.41642,8.69499,49.42112&filter=id:node/3429511451&"
        + "time=2019-01-01", JsonNode.class);
    JsonNode feature = Helper.getFeatureByIdentifier(response, "@osmId", "node/3429511451");
    assertEquals(49.418466, feature.get("geometry").get("coordinates").get(1).asDouble(), 0);
  }

  @Test
  public void elementsFullHistoryGeometryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elementsFullHistory/geometry?bboxes=8.67452,49.40961,8.70392,49.41823"
            + "&properties=metadata&time=2015-01-01,2015-07-01"
            + "&filter=type:way and building=residential",
        JsonNode.class);
    assertTrue(Helper.getFeatureByIdentifier(response, "@osmId", "way/295135436") != null);
    assertEquals(6, Helper.getFeatureByIdentifier(response, "@validTo", "2015-05-05T06:59:35Z")
        .get("properties").size());
  }

  @Test
  public void elementsFullHistoryGeometryWithTagsTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/elementsFullHistory/geometry?bboxes=8.67494,49.417032,8.676136,49.419576"
            + "&properties=tags&time=2017-01-01,2018-01-01&filter=type:way and brand=\"Aldi Süd\"",
        JsonNode.class);
    assertTrue(
        Helper.getFeatureByIdentifier(response, "@validFrom", "2017-01-18T17:38:06Z") != null);
    assertEquals(13, Helper.getFeatureByIdentifier(response, "@validTo", "2017-03-03T18:51:20Z")
        .get("properties").size());
  }

  @Test
  public void elementsFullHistoryBboxTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.67494,49.417032,8.676136,49.419576");
    map.add("filter", "type:way and brand=\"Aldi Süd\"");
    map.add("time", "2017-01-01,2018-01-01");
    map.add("properties", "tags,metadata");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elementsFullHistory/bbox", map, JsonNode.class);
    assertTrue(
        Helper.getFeatureByIdentifier(response, "@validFrom", "2017-01-01T00:00:00Z") != null);
    assertEquals(16, Helper.getFeatureByIdentifier(response, "@changesetId", "43971880")
        .get("properties").size());
  }

  @Test
  public void elementsFullHistoryCentroidTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.67452,49.40961,8.70392,49.41823");
    map.add("filter", "type:way and building=residential");
    map.add("time", "2017-01-01,2017-07-01");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elementsFullHistory/centroid", map, JsonNode.class);
    assertEquals(3, Helper.getFeatureByIdentifier(response, "@osmId", "way/295135455")
        .get("properties").size());
  }

  /*
   * filter tests
   */

  @Test
  public void getElementsFilterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/elements/bbox?bboxes=8.684692,49.407669,8.688061,49.410310&time=2016-01-01,2017-01-01"
        + "&filter=service=* and name!=*&properties=tags", JsonNode.class);
    assertEquals(6, Helper.getFeatureByIdentifier(response, "@osmId", "way/225890568")
        .get("properties").size());
  }

  @Test
  public void postElementsFilterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.684692,49.407669,8.688061,49.410310");
    map.add("time", "2012-01-01");
    map.add("filter", "oneway=yes");
    map.add("properties", "tags");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/bbox", map, JsonNode.class);
    assertTrue(Helper.getFeatureByIdentifier(response, "@osmId", "way/4403824").get("properties")
        .get("highway").asText().equalsIgnoreCase("tertiary"));
  }

  /*
   * false parameter tests
   */

  @Test
  public void getDataExtractionWithSpecificParameterOfOtherSpecificResourceTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate
        .getForEntity(server + port + "/elements/bbox?groupByKeys=building", JsonNode.class);
    assertEquals(400, response.getBody().get("status").asInt());
  }

  @Test
  public void postDataExtractionWithSpecificParameterOfOtherSpecificResourceTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("filter2", "highway=primary");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/elements/geometry", map, JsonNode.class);
    assertEquals(400, response.getBody().get("status").asInt());
  }

  @Test
  public void fullHistoryDataExtractionWithFalseSpecificParameterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("propertie", "tags");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/elementsFullHistory/geometry", map, JsonNode.class);
    assertEquals(400, response.getBody().get("status").asInt());
  }

  /*
   * ./contributions tests
   */

  @Test
  public void contributionsContributionTypesTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/contributions/geometry?bboxes=8.686017,49.406453,8.686983,49.406966&filter=building=*&"
        + "time=2008-01-01,2009-09-01&properties=metadata,tags&clipGeometry=false", JsonNode.class);
    JsonNode featuresArray = response.getBody().get("features");
    assertTrue(featuresArray.get(0).get("properties").get("@creation").asBoolean());
    assertTrue(featuresArray.get(1).get("properties").get("@geometryChange").asBoolean());
    assertTrue(featuresArray.get(2).get("properties").get("@tagChange").asBoolean());
  }

  @Test
  public void contributionsGeometryChangePostTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.70606,49.412150,8.70766,49.413686");
    map.add("time", "2011-06-01,2012-01-01");
    map.add("filter", "building=*");
    map.add("properties", "metadata");
    map.add("clipGeometry", "false");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/contributions/bbox", map, JsonNode.class);
    JsonNode featureProperties =
        Helper.getFeatureByIdentifier(response, "@osmId", "relation/1385511").get("properties");
    assertTrue(featureProperties.get("@geometryChange").asBoolean());
  }

  @Test
  public void contributionsCreationPostTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.70606,49.412150,8.70766,49.413686");
    map.add("time", "2011-06-01,2012-01-01");
    map.add("filter", "building=*");
    map.add("properties", "metadata");
    map.add("clipGeometry", "false");
    ResponseEntity<JsonNode> response =
        restTemplate.postForEntity(server + port + "/contributions/bbox", map, JsonNode.class);
    JsonNode featureProperties =
        Helper.getFeatureByIdentifier(response, "@osmId", "relation/1387943").get("properties");
    assertTrue(featureProperties.get("@creation").asBoolean());
  }

  @Test
  public void contributionsTwoContributionTypesTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/contributions/geometry?bboxes=8.70328,49.411926,8.70564,49.413343&filter=building=*&"
        + "time=2010-01-01,2012-01-01&properties=metadata&clipGeometry=false", JsonNode.class);
    JsonNode featureProperties =
        Helper.getFeatureByIdentifier(response, "@changesetId", "10082609").get("properties");
    assertTrue(featureProperties.get("@geometryChange").asBoolean()
        && featureProperties.get("@tagChange").asBoolean());
  }

  @Test
  public void contributionsCreationTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/contributions/geometry?bboxes=8.70606,49.412150,8.70766,49.413686"
        + "&filter=building=*&time=2011-06-01,2012-01-01&properties=metadata&clipGeometry=false",
        JsonNode.class);
    JsonNode featureProperties =
        Helper.getFeatureByIdentifier(response, "@changesetId", "8371765").get("properties");
    assertTrue(featureProperties.get("@creation").asBoolean());
  }

  @Test
  public void contributionsIsNotCreationTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/contributions/centroid?bboxes=8.70500,49.412004,8.70666,49.413445"
        + "&filter=building=*&time=2015-01-01,2017-01-01&properties=metadata&clipGeometry=false",
        JsonNode.class);
    JsonNode featureProperties =
        Helper.getFeatureByIdentifier(response, "@changesetId", "36337061").get("properties");
    assertNull(featureProperties.get("@creation"));
  }

  @Test
  public void contributionsWithoutGeometryChangeTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/contributions/bbox?bboxes=8.70400,49.411004,8.70566,49.413345"
        + "&filter=building=*&time=2012-01-01,2014-01-01&properties=metadata&clipGeometry=false",
        JsonNode.class);
    JsonNode featureProperties =
        Helper.getFeatureByIdentifier(response, "@changesetId", "10696832").get("properties");
    assertNull(featureProperties.get("@geometryChange"));
  }

  @Test
  public void contributionsWithoutTagChangeTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/contributions/geometry?bboxes=8.70600,49.412104,8.70766,49.413666"
        + "&filter=building=*&time=2015-01-01,2017-01-01&properties=metadata&clipGeometry=false",
        JsonNode.class);
    JsonNode featureProperties =
        Helper.getFeatureByIdentifier(response, "@changesetId", "36337061").get("properties");
    assertNull(featureProperties.get("@tagChange"));
  }

  @Test
  public void contributionsDeletionTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/contributions/geometry?bboxes=8.699552,49.411985,8.700909,49.412648&filter=building=* "
        + "and type:way and id:14195519&time=2008-01-28,2012-01-01&properties=metadata"
        + "&clipGeometry=false", JsonNode.class);
    assertEquals(Helper.getFeatureByIdentifier(response, "@changesetId", "9218673").get("geometry")
        .getNodeType(), JsonNodeType.NULL);
  }

  @Test
  public void contributionsGeometryCollectionDeletionTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port + "/contributions/geometry?bboxes=8.66589,49.37737,8.6688,49.37861&"
            + "filter=id:relation/3326519&properties=tags,metadata&time=2018-01-01,2019-01-01",
        JsonNode.class);
    assertEquals(Helper.getFeatureByIdentifier(response, "@changesetId", "61636634").get("geometry")
        .getNodeType(), JsonNodeType.NULL);
  }

  @Test
  public void contributionsVersionTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/contributions/centroid?bboxes=8.70785,49.412222,8.70766,49.413759&filter=building=*"
        + "&time=2010-01-01,2014-01-01&properties=metadata&clipGeometry=false", JsonNode.class);
    JsonNode featureProperties =
        Helper.getFeatureByIdentifier(response, "@osmId", "way/248975559").get("properties");
    assertEquals(1, featureProperties.get("@version").asInt());
  }

  @Test
  public void contributionsAssociationChangeSetIdWithOsmIdAndVersionTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/contributions/bbox?bboxes=8.70606,49.412150,8.70766,49.413686"
        + "&filter=building=*&time=2011-06-01,2012-01-01&properties=metadata&clipGeometry=false",
        JsonNode.class);
    JsonNode featureProperties =
        Helper.getFeatureByIdentifier(response, "@changesetId", "7042867").get("properties");
    assertTrue(featureProperties.get("@version").asInt() == 2
        && featureProperties.get("@osmId").asText().equals("way/96054443"));
  }

  @Test
  public void contributionsGeometryCoordinateTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/contributions/geometry?bboxes=8.68641,49.41642,8.69499,49.42112"
        + "&filter=id:node/3429511451&time=2017-01-01,2019-01-01", JsonNode.class);
    JsonNode feature = Helper.getFeatureByIdentifier(response, "@osmId", "node/3429511451");
    assertEquals(49.418466, feature.get("geometry").get("coordinates").get(1).asDouble(), 0);
  }

  @Test
  public void contributionTypesPropertiesParameterTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/contributions/bbox?bboxes=8.67,49.39,8.71,49.42&clipGeometry=true&"
        + "filter=id:way/25316163&properties=metadata,contributionTypes&time=2012-12-10,2012-12-11",
        JsonNode.class);
    JsonNode feature = response.getBody().get("features").get(0);
    assertTrue(feature.get("properties").has("@geometryChange"));
    assertEquals("14184500", feature.get("properties").get("@changesetId").asText());
    assertEquals("14227603", feature.get("properties").get("@contributionChangesetId").asText());
  }

  /*
   * ./contributions/latest tests
   */

  @Test
  public void contributionsLatestTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/contributions/latest/geometry?bboxes=8.687337,49.415067,8.687493,49.415172&filter="
        + "building=*&time=2010-01-01,2016-06-01&clipGeometry=false", JsonNode.class);
    assertTrue(response.getBody().get("features").get(0).get("properties").get("@timestamp")
        .asText().equals("2015-06-04T19:23:19Z"));
  }

  @Test
  public void contributionsLatestPostTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("bboxes", "8.70606,49.412150,8.70766,49.413686");
    map.add("time", "2011-06-01,2012-01-01");
    map.add("filter", "building=*");
    map.add("properties", "metadata");
    map.add("clipGeometry", "false");
    ResponseEntity<JsonNode> response = restTemplate
        .postForEntity(server + port + "/contributions/latest/centroid", map, JsonNode.class);
    JsonNode featureProperties =
        Helper.getFeatureByIdentifier(response, "@osmId", "relation/1387943").get("properties");
    assertTrue(featureProperties.get("@timestamp").asText().equals("2011-06-07T16:45:11Z")
        && featureProperties.get("@changesetId").asInt() == 7052829);
  }

  @Test
  public void contributionsLatestOnlyOneEntryTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/contributions/latest/geometry?bboxes=8.70606,49.412150,8.70766,49.413686&filter="
        + "building=*&time=2011-06-01,2012-01-01&clipGeometry=false", JsonNode.class);
    JsonNode featuresArray = response.getBody().get("features");
    List<String> osmIds = new ArrayList<String>();
    for (JsonNode feature : featuresArray) {
      osmIds.add(feature.get("properties").get("@osmId").asText());
    }
    assertEquals(1, Collections.frequency(osmIds, "relation/1387943"));
  }

  @Test
  public void contributionsLatestDeletionTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(server + port
        + "/contributions/latest/geometry?bboxes=8.699552,49.411985,8.700909,49.412648&filter="
        + "building=* and type:way and id:14195519&time=2008-01-28,2012-01-01&properties=metadata",
        JsonNode.class);
    assertEquals(Helper.getFeatureByIdentifier(response, "@changesetId", "9218673").get("geometry")
        .getNodeType(), JsonNodeType.NULL);
  }

  @Test
  public void contributionsLatestCreationTest() {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port
            + "/contributions/latest/geometry?bboxes=8.679253,49.424025,8.679623,49.424185&filter="
            + "building=*&time=2010-01-01,2011-01-17&properties=metadata,tags&clipGeometry=false",
        JsonNode.class);
    assertTrue(
        response.getBody().get("features").get(0).get("properties").get("@creation").asBoolean());
  }

  @Test
  public void issue109Test() {
    // see https://github.com/GIScience/ohsome-api/issues/109
    TestRestTemplate restTemplate = new TestRestTemplate();
    // this uses the centroid endpoint to make sure that geometry filters are even applied to
    // the geometries before being transformed to, e.g., centroid points
    ResponseEntity<JsonNode> response = restTemplate.getForEntity(
        server + port
            + "/elementsFullHistory/centroid?bboxes=8.69525,49.40938,8.70461,49.41203&"
            + "time=2011-09-05,2011-09-06&filter=geometry:polygon and id:relation/1391838",
        JsonNode.class);
    assertEquals(1, response.getBody().get("features").size());
  }
}
