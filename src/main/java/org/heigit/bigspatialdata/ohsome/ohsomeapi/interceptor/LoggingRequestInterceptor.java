package org.heigit.bigspatialdata.ohsome.ohsomeapi.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/** Request interceptor, used for logging response code, accessed URI and execution time. */
@Component
public class LoggingRequestInterceptor extends HandlerInterceptorAdapter {

  final Logger logger = LoggerFactory.getLogger(Application.class);
  private long startTime;

  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    startTime = System.currentTimeMillis();
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) {
    String requestURI;
    if (request.getHeader("X-REQUEST-URI") != null) {
      requestURI = request.getHeader("X-REQUEST-URI");
    } else {
      requestURI = request.getRequestURL().toString();
    }
    logger.info("accessed URI: " + requestURI);
    logger.info("processing time: " + (System.currentTimeMillis() - startTime));
    logger.info("response code: " + response.getStatus());
  }
}
