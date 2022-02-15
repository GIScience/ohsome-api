package org.heigit.ohsome.ohsomeapi.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.heigit.ohsome.ohsomeapi.exception.DatabaseAccessException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.geometrybuilders.GeometryOfOSHDBExtent;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.ohsome.oshdb.api.db.OSHDBDatabase;
import org.heigit.ohsome.oshdb.api.db.OSHDBJdbc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/** Utils class containing request-specific static utility methods. */
@Component
@RequestScope
public class RequestUtils {

  private final HttpServletRequest servletRequest;
  private final ExtractMetadata extractMetadata;
  private final GeometryOfOSHDBExtent geometryOfOSHDBExtent;

  @Autowired
  public RequestUtils(HttpServletRequest servletRequest, ExtractMetadata extractMetadata,
      GeometryOfOSHDBExtent geometryOfOSHDBExtent) {
    this.servletRequest = servletRequest;
    this.extractMetadata = extractMetadata;
    this.geometryOfOSHDBExtent = geometryOfOSHDBExtent;
  }

  /**
   * Extracts the request URL from the given <code>HttpServletRequest</code> object.
   *
   * @param request sent <code>HttpServletRequest</code> object
   * @return <code>String</code> that contains the received request URL
   */
  public String extractRequestUrl() {
    String queryString = servletRequest.getQueryString();
    if (servletRequest.getHeader("X-REQUEST-URI") != null) {
      return servletRequest.getHeader("X-REQUEST-URI") + "?" + queryString;
    } else {
      return servletRequest.getRequestURL() + "?" + queryString;
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
   * Checks if the given request is requesting a data extraction that can potentially return a
   * bigger amount of GeoJSON data. This can either be through using the
   * /elements/geometry|bbox|centroid, or the /elementsFullHistory endpoint.
   *
   * @param url the url of the request to check
   * @return whether it is a data-extraction request, or not
   */
  public static boolean isDataExtraction(String url) {
    return url.contains("elementsFullHistory") || url.contains("elements/geometry")
        || url.contains("elements/centroid") || url.contains("elements/bbox");
  }

  /**
   * Checks if the given request is requesting a contributions extraction that can potentially
   * return a bigger amount of GeoJSON data. This can be through using the
   * /contributions/(latest)/geometry|bbox|centroid endpoint.
   *
   * @param url the url of the request to check
   * @return whether it is a contributions-extraction request, or not
   */
  public static boolean isContributionsExtraction(String url) {
    return url.contains("contributions/geometry") || url.contains("contributions/centroid")
        || url.contains("contributions/bbox") || url.contains("contributions/latest/geometry")
        || url.contains("contributions/latest/centroid")
        || url.contains("contributions/latest/bbox");
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
   * @throws DatabaseAccessException if the keytables are missing
   * @throws RuntimeException if the timerange metadata cannot be retrieved from the db
   * @throws IOException thrown by {@link com.fasterxml.jackson.databind.ObjectMapper
   *         #readTree(String) readTree}
   */
  public void extractOSHDBMetadata() throws IOException {
    OSHDBDatabase db;
    if (DbConnData.keytables != null) {
      db = DbConnData.keytables;
    } else {
      if (!(DbConnData.db instanceof OSHDBJdbc)) {
        throw new DatabaseAccessException(ExceptionMessages.DATABASE_ACCESS);
      }
      db = DbConnData.db;
    }
    if (db.metadata("extract.region") != null) {
      String dataPolyString = db.metadata("extract.region");
      ObjectMapper mapper = new ObjectMapper();
      extractMetadata.setDataPolyJson(mapper.readTree(dataPolyString));
      GeometryOfOSHDBExtent builder = new GeometryOfOSHDBExtent();
      builder.create(dataPolyString);
      extractMetadata.setDataPoly(geometryOfOSHDBExtent.getGeometry());
    }
    if (db.metadata("extract.timerange") != null) {
      String[] timeranges = db.metadata("extract.timerange").split(",");
      extractMetadata.setFromTstamp(timeranges[0]);
      extractMetadata.setToTstamp(timeranges[1]);
    } else {
      throw new RuntimeException("The timerange metadata could not be retrieved from the db.");
    }
    if (db.metadata("attribution.short") != null) {
      extractMetadata.setAttributionShort(db.metadata("attribution.short"));
    } else {
      extractMetadata.setAttributionShort("© OpenStreetMap contributors");
    }
    if (db.metadata("attribution.url") != null) {
      extractMetadata.setAttributionUrl(db.metadata("attribution.url"));
    } else {
      extractMetadata.setAttributionUrl("https://ohsome.org/copyrights");
    }
    if (db.metadata("header.osmosis_replication_sequence_number") != null) {
      extractMetadata.setReplicationSequenceNumber(Integer.parseInt(db.metadata("header.osmosis_replication_sequence_number")));
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
