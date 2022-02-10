package org.heigit.ohsome.ohsomeapi.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.heigit.ohsome.ohsomeapi.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Adds a filter, which adds headers allowing Cross-Origin Resource Sharing (CORS), sets the
 * character encoding to utf-8, if it's undefined, as well as a header to enable caching, if
 * appropriate.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestFilter extends OncePerRequestFilter {

  @Autowired
  private RequestUtils requestUtils;

  /**
   * @throws IOException thrown by {@link org.heigit.ohsome.ohsomeapi.utils.RequestUtils
   *         #extractOSHDBMetadata() extractOSHDBMetadata}, and
   *         {@link javax.servlet.ServletRequest#setCharacterEncoding(String) setCharacterEncoding}
   * @throws ServletException thrown by {@link javax.servlet.FilterChain
   *         #doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse) doFilter}
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {
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
    String requestUrl = request.getRequestURL().toString();
    if (RequestUtils.isDataExtraction(requestUrl)
        || RequestUtils.isContributionsExtraction(requestUrl)) {
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
    requestUtils.extractOSHDBMetadata();
    filterChain.doFilter(request, wrapper);
  }
}
