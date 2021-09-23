package org.heigit.ohsome.ohsomeapi.inputprocessing;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.heigit.ohsome.ohsomeapi.executor.MetadataRequestExecutor;

public class ResourceParameters {
  private ResourceParameters() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Checks the resource of the request and gives back a list of available parameters for this
   * resource.
   *
   * <p>Note that some resources don't use this method, but implement their own checks. One example
   * for this is the metadata request in {@link MetadataRequestExecutor
   * #executeGetMetadata(HttpServletRequest) executeGetMetadata}.
   *
   * @param servletRequest represents the HttpServlet request.
   * @return a list of available parameters for the given resource.
   */
  public static List<String> getResourceSpecificParams(HttpServletRequest servletRequest) {
    String uri = servletRequest.getRequestURI();
    List<String> possibleParams = new LinkedList<>(Arrays.asList("bboxes", "bcircles", "bpolys",
        "types", "keys", "values", "timeout", "time", "showMetadata", "filter"));
    if (uri.contains("/count") || uri.contains("/length") || uri.contains("/area")
        || uri.contains("/perimeter")) {
      possibleParams.add("format");
    }
    if (uri.contains("/groupBy/tag")) {
      possibleParams.add("groupByKey");
      possibleParams.add("groupByValues");
    } else if (uri.contains("/groupBy/key")) {
      possibleParams.add("groupByKeys");
    } else if (uri.contains("/ratio")) {
      possibleParams.add("filter2");
      possibleParams.add("keys2");
      possibleParams.add("types2");
      possibleParams.add("values2");
    } else if (uri.contains("/bbox") || uri.contains("/centroid") || uri.contains("/geometry")) {
      possibleParams.add("properties");
      possibleParams.add("clipGeometry");
    }
    if (uri.contains("/contributions")) {
      // removing deprecated params from newer endpoint
      possibleParams.remove("types");
      possibleParams.remove("keys");
      possibleParams.remove("values");
      if (uri.contains("/count")) {
        possibleParams.add("contributionType");
      }
    }
    if (uri.contains("/users")) {
      possibleParams.add("contributionType");
    }
    return possibleParams;
  }

  /**
   * Checks, if the request contains unexpected parameters for this resource.
   *
   * @param servletRequest represents the HttpServlet request.
   * @param resourceParams represents a list of all possible parameters for the requested resource.
   * @return a list of unexpected parameters
   */
  public static List<String> checkUnexpectedParams(HttpServletRequest servletRequest,
      List<String> resourceParams) {
    List<String> unexpectedParams = new LinkedList<>();
    for (String param : servletRequest.getParameterMap().keySet()) {
      if (!resourceParams.contains(param)) {
        unexpectedParams.add(param);
      }
    }
    return unexpectedParams;
  }
}
