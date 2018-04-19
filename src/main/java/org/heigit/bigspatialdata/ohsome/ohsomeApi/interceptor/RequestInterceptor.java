package org.heigit.bigspatialdata.ohsome.ohsomeApi.interceptor;

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
    
    // builds the initial url, which was sent as request
    requestUrl = request.getRequestURL() + "?" + request.getQueryString();
    // adding a 1 year cache option
    response.setHeader("Cache-Control", "no-transform, public, max-age=31556926");
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) {
    
  }
}
