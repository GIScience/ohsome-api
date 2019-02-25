package org.heigit.bigspatialdata.ohsome.ohsomeapi.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/** Interceptor class, which is used to get the request URL. */
@Component
public class RequestInterceptor extends HandlerInterceptorAdapter {

  public static String requestUrl;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) {
    String queryString = request.getQueryString();
    if (queryString != null) {
      queryString = queryString.replaceAll("%20", "");
    }
    // builds the initial url, which was sent as request
    if (request.getHeader("X-REQUEST-URI") != null) {
      requestUrl = request.getHeader("X-REQUEST-URI") + "?" + queryString;
    } else {
      requestUrl = request.getRequestURL() + "?" + queryString;
    }
    return true;
  }
}
