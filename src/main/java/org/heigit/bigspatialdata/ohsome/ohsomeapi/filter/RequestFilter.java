package org.heigit.bigspatialdata.ohsome.ohsomeapi.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.exception.ServiceUnavailableException;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.utils.RequestUtils;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBIgnite;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Adds a filter, which adds headers allowing Cross-Origin Resource Sharing (CORS), sets the
 * character encoding to utf-8, if it's undefined, as well as a header to enable caching, if
 * appropriate. Also checks for each request, if all cluster nodes are still active (only for 
 * OSHDBIgnite backends).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    if (DbConnData.db instanceof OSHDBIgnite) {
      checkClusterAvailability();
    }
    if (request.getCharacterEncoding() == null) {
      request.setCharacterEncoding("UTF-8");
    }
    // TODO move header definition into separate method defineResponseHeaders
    response.setHeader("Access-Control-Allow-Origin", "*");
    response.setHeader("Access-Control-Allow-Methods", "POST, GET");
    response.setHeader("Access-Control-Max-Age", "3600");
    response.setHeader("Access-Control-Allow-Credentials", "true");
    response.setHeader("Access-Control-Allow-Headers",
        "Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,"
            + "Access-Control-Request-Headers,Authorization");
    if (RequestUtils.isDataExtraction(request.getRequestURL().toString())) {
      response.setHeader("Content-disposition", "attachment;filename=ohsome.geojson");
    }
    if (RequestUtils.usesCsvFormat(request)) {
      response.setHeader("Content-disposition", "attachment;filename=ohsome.csv");
    }
    boolean cacheNotAllowed = RequestUtils.cacheNotAllowed(request.getRequestURL().toString(),
        request.getParameterValues("time"));
    HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper(response) {
      int status = 200;

      @Override
      public void setStatus(int sc) {
        super.setStatus(sc);
        status = sc;
      }

      @Override
      public void setHeader(String name, String value) {
        if (status != 200 || cacheNotAllowed) {
          super.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        } else {
          super.setHeader("Cache-Control", "no-transform, public, max-age=31556926");
        }
      }

      @Override
      public void addHeader(String name, String value) {
        setHeader(name, value);
      }
    };
    RequestUtils.extractOSHDBMetadata();
    filterChain.doFilter(request, wrapper);
  }

  /**
   * Checks, if the cluster still has the same amount of active server nodes, as defined on startup.
   * Throws a 503 Service Unavailable exception, in case one or more nodes are inactive.
   */
  private void checkClusterAvailability() {
    OSHDBIgnite igniteDb = (OSHDBIgnite) DbConnData.db;
    int definedNumberOfNodes = ProcessingData.getNumberOfClusterNodes();
    int currentNumberOfNodes =
        igniteDb.getIgnite().services().clusterGroup().metrics().getTotalNodes();
    if (definedNumberOfNodes != currentNumberOfNodes) {
      throw new ServiceUnavailableException("The cluster backend is currently not able to process "
          + "your request. Please try again later.");
    }
  }

}
