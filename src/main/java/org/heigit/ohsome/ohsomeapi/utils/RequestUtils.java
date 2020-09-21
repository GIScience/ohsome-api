package org.heigit.ohsome.ohsomeapi.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBDatabase;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBJdbc;
import org.heigit.ohsome.ohsomeapi.exception.DatabaseAccessException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.inputprocessing.GeometryBuilder;
import org.heigit.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;

/** Utils class containing request-specific static utility methods. */
public class RequestUtils {

  private RequestUtils() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Extracts the request URL from the given <code>HttpServletRequest</code> object.
   * 
   * @param request sent <code>HttpServletRequest</code> object
   * @return <code>String</code> that contains the received request URL
   */
  public static String extractRequestUrl(HttpServletRequest request) {
    String queryString = request.getQueryString();
    if (request.getHeader("X-REQUEST-URI") != null) {
      return request.getHeader("X-REQUEST-URI") + "?" + queryString;
    } else {
      return request.getRequestURL() + "?" + queryString;
    }
  }

  /**
   * Checks, if caching will be allowed for the given query, or not.
   *
   * @param url the URL of the request to check
   * @param timeParameter the "time" parameter of the request to check
   * @return whether caching is allowed, or not
   */
  public static boolean cacheNotAllowed(String url, String[] timeParameter) {
    return isMetadata(url) || usesDefaultToTimestamp(timeParameter) || isDataExtraction(url);
  }

  /**
   * Checks if the given request is requesting a data-extraction.
   * 
   * @param url the url of the request to check
   * @return whether it is a data-extraction request, or not
   */
  public static boolean isDataExtraction(String url) {
    return url.contains("elementsFullHistory") || url.contains("elements/geometry")
        || url.contains("elements/centroid") || url.contains("elements/bbox");
  }

  /**
   * Checks if the given request uses the csv format.
   * 
   * @param request <code>HttpServletRequest</code> object used to check
   * @return whether it uses the csv format, or not
   */
  public static boolean usesCsvFormat(HttpServletRequest request) {
    return "csv".equalsIgnoreCase(request.getParameter("format"));
  }

  /**
   * Extracts some metadata from the OSHDB keytables or db and adds it to the corresponding objects.
   * 
   * @throws DatabaseAccessException if keytables are missing
   * @throws RuntimeException the timerange metadata cannot be retrieved from the db
   * @throws IOException thrown by
   *         {@link com.fasterxml.jackson.databind.ObjectMapper#readTree(String) readTree}
   */
  public static void extractOSHDBMetadata() throws IOException {
    OSHDBDatabase db;
    if (DbConnData.getKeytables() != null) {
      db = DbConnData.getKeytables();
    } else {
      if (!(DbConnData.getDb() instanceof OSHDBJdbc)) {
        throw new DatabaseAccessException(ExceptionMessages.DATABASE_ACCESS);
      }
      db = DbConnData.getDb();
    }
    if (db.metadata("extract.region") != null) {
      String dataPolyString = db.metadata("extract.region");
      ObjectMapper mapper = new ObjectMapper();
      ExtractMetadata.setDataPolyJson(mapper.readTree(dataPolyString));
      GeometryBuilder geomBuilder = new GeometryBuilder();
      geomBuilder.createGeometryFromMetadataGeoJson(dataPolyString);
      ExtractMetadata.setDataPoly(ProcessingData.getDataPolyGeom());
    }
    if (db.metadata("extract.timerange") != null) {
      String[] timeranges = db.metadata("extract.timerange").split(",");
      ExtractMetadata.setFromTstamp(timeranges[0]);
      ExtractMetadata.setToTstamp(timeranges[1]);
    } else {
      throw new RuntimeException("The timerange metadata could not be retrieved from the db.");
    }
    if (db.metadata("attribution.short") != null) {
      ExtractMetadata.setAttributionShort(db.metadata("attribution.short"));
    } else {
      ExtractMetadata.setAttributionShort("© OpenStreetMap contributors");
    }
    if (db.metadata("attribution.url") != null) {
      ExtractMetadata.setAttributionUrl(db.metadata("attribution.url"));
    } else {
      ExtractMetadata.setAttributionUrl("https://ohsome.org/copyrights");
    }
    if (db.metadata("header.osmosis_replication_sequence_number") != null) {
      ExtractMetadata.setReplicationSequenceNumber(
          Integer.parseInt(db.metadata("header.osmosis_replication_sequence_number")));
    }
  }

  /**
   * Checks if the given request uses the default toTimestamp.
   * 
   * @param timeParameter the "time" parameter of the request to check
   * @return whether it uses the default toTimestamp, or not
   */
  private static boolean usesDefaultToTimestamp(String[] timeParameter) {
    if (timeParameter == null || timeParameter.length == 0) {
      return true;
    }
    int length = timeParameter.length;
    if (length != 1) {
      return false;
    }
    return timeParameter[0].contains("//") || timeParameter[0].endsWith("/");
  }

  /**
   * Checks if the given request is requesting metadata.
   * 
   * @param url the url of the request to check
   * @return whether it is a metadata request, or not
   */
  private static boolean isMetadata(String url) {
    return url.contains("/metadata");
  }
}
