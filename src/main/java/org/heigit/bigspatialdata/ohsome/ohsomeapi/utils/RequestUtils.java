package org.heigit.bigspatialdata.ohsome.ohsomeapi.utils;

import javax.servlet.http.HttpServletRequest;

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
   * Checks, if caching will be allowed for the given <code>HttpServletRequest</code> object, or
   * not.
   * 
   * @param request <code>HttpServletRequest</code> object used to check
   * @return whether caching is allowed, or not
   */
  public static boolean cacheNotAllowed(HttpServletRequest request) {
    return isMetadata(request) || usesDefaultToTimestamp(request) || isDataExtraction(request);
  }
  
  /**
   * Checks if the given request is requesting a data-extraction.
   * 
   * @param request <code>HttpServletRequest</code> object used to check
   * @return whether it is a data-extraction request, or not
   */
  public static boolean isDataExtraction(HttpServletRequest request) {
    String url = request.getRequestURL().toString();
    return (url.contains("elementsFullHistory") || url.contains("elements/geometry")
        || url.contains("elements/centroid") || url.contains("elements/bbox"));
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
   * @param request <code>HttpServletRequest</code> object used to check
   * @return whether it uses the default toTimestamp, or not
   */
  private static boolean usesDefaultToTimestamp(HttpServletRequest request) {
    String[] time = request.getParameterValues("time");
    if (time == null || time[0].replaceAll("\\s", "").length() == 0) {
      return true;
    }
    int length = time.length;
    if (length != 1) {
      return false;
    }
    return (time[0].contains("//") || time[0].endsWith("/"));
  }

  /**
   * Checks if the given request is requesting metadata.
   * 
   * @param request <code>HttpServletRequest</code> object used to check
   * @return whether it is a metadata request, or not
   */
  private static boolean isMetadata(HttpServletRequest request) {
    return request.getRequestURL().toString().contains("/metadata");
  }
}
