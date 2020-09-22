package org.heigit.ohsome.ohsomeapi.controller;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

/** Holds helper methods that are used within the various test classes. */
public class Helper {
  private static String portPost = TestProperties.PORT2;
  private static String server = TestProperties.SERVER;

  /** Gets the post response body as String. */
  static String getPostResponseBody(String urlParams, MultiValueMap<String, String> map) {
    TestRestTemplate restTemplate = new TestRestTemplate();
    ResponseEntity<String> response =
        restTemplate.postForEntity(server + portPost + urlParams, map, String.class);
    String responseBody = response.getBody();
    return responseBody;
  }

  /** Creates a CSV parser using ';' as delimiter and '#' as comment marker. */
  public static CSVParser csvParser(String responseBody) throws IOException {
    CSVFormat csvFormat =
        CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(';').withCommentMarker('#');
    CSVParser csvParser = CSVParser.parse(responseBody, csvFormat);
    return csvParser;
  }

  /** Gets the CSV entries from the given response. */
  public static List<CSVRecord> getCsvRecords(String responseBody) throws IOException {
    CSVParser csvParser = csvParser(responseBody);
    List<CSVRecord> records = csvParser.getRecords();
    return records;
  }

  /** Gets the CSV headers from the given response. */
  public static Map<String, Integer> getCsvHeaders(String responseBody) throws IOException {
    CSVParser csvParser = csvParser(responseBody);
    Map<String, Integer> headers = csvParser.getHeaderMap();
    return headers;
  }

  /**
   * Gets the feature from a data-extraction endpoint via the given identifier and corresponding
   * value.
   */
  public static JsonNode getFeatureByIdentifier(ResponseEntity<JsonNode> response, String identifier,
      String value) {
    return StreamSupport
        .stream(Spliterators.spliteratorUnknownSize(response.getBody().get("features").iterator(),
            Spliterator.ORDERED), false)
        .filter(
            jsonNode -> jsonNode.get("properties").get(identifier).asText().equalsIgnoreCase(value))
        .findFirst().get();
  }

}
