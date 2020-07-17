package org.heigit.ohsome.ohsomeapi.utils;


import javax.servlet.http.HttpServletRequest;


/** Utils class containing request-specific static utility methods. */
public class RequestUtils {

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
