package org.heigit.bigspatialdata.ohsome.oshdbRestApi.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Component
/**
 * Interceptor class, which is used to get the input request URL.
 */
public class ElementsRequestInterceptor extends HandlerInterceptorAdapter {

  public static String requestUrl;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) {
    // builds the initial url, which was sent as request
    requestUrl = request.getRequestURL() + "?" +  request.getQueryString();
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) {
    //
  }
}
