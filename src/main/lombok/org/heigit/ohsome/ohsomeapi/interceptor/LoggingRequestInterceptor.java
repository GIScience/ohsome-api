package org.heigit.ohsome.ohsomeapi.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.heigit.ohsome.ohsomeapi.Application;
import org.heigit.ohsome.ohsomeapi.inputprocessing.InputProcessor;
import org.heigit.ohsome.oshdb.util.geometry.Geo;
import org.heigit.ohsome.oshdb.util.geometry.OSHDBGeometryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/** Request interceptor, used for logging response code, accessed URI and execution time. */
@Component
public class LoggingRequestInterceptor extends HandlerInterceptorAdapter {

  final Logger logger = LoggerFactory.getLogger(Application.class);
  private long startTime;

  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) {
    startTime = System.currentTimeMillis();
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) {
    String requestUri;
    if (request.getHeader("X-REQUEST-URI") != null) {
      requestUri = request.getHeader("X-REQUEST-URI");
    } else {
      requestUri = request.getRequestURL().toString();
    }
    logger.info("accessed URI: " + requestUri);
    logger.info("query filter: "
        + request.getParameterMap().getOrDefault("filter", new String[] { "<none>" })[0]);
    logger.info("query time: "
        + request.getParameterMap().getOrDefault("time", new String[] { "<none>" })[0]);
    try {
      var inputProcessor = new InputProcessor(request, false, false);
      inputProcessor.processParameters();
      var boundary = inputProcessor.getGeometry();
      var envelope = boundary.getEnvelopeInternal();
      var bbox = OSHDBGeometryBuilder.boundingBoxOf(envelope);
      logger.info("query boundary type: "
          + inputProcessor.getProcessingData().getBoundaryType().toString());
      logger.info("query boundary bbox: " + bbox.toString());
      logger.info("query boundary area: " + String.format("%.2f", Geo.areaOf(boundary) / 1E6));
    } catch (Exception e) {
      logger.info("query boundary: <error while processing request params>");
    }
    logger.info("processing time: " + (System.currentTimeMillis() - startTime));
    logger.info("response code: " + response.getStatus());
  }
}
