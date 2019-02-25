package org.heigit.bigspatialdata.ohsome.ohsomeapi.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Adds a filter, which adds headers allowing Cross-Origin Resource Sharing (CORS) and to cache the
 * result if no exception is thrown.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    response.setHeader("Access-Control-Allow-Origin", "*");
    response.setHeader("Access-Control-Allow-Methods", "POST, GET");
    response.setHeader("Access-Control-Max-Age", "3600");
    response.setHeader("Access-Control-Allow-Credentials", "true");
    response.setHeader("Access-Control-Allow-Headers",
        "Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,"
            + "Access-Control-Request-Headers,Authorization");
    HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper(response) {
      int status = 200;

      @Override
      public void setStatus(int sc) {
        super.setStatus(sc);
        status = sc;
      }

      @Override
      public void setHeader(String name, String value) {
        if (status != 200) {
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
    filterChain.doFilter(request, wrapper);
  }
}
