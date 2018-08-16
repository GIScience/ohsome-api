package org.heigit.bigspatialdata.ohsome.ohsomeApi.config;

import org.heigit.bigspatialdata.ohsome.ohsomeApi.interceptor.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Adds the {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.interceptor.RequestInterceptor
 * RequestInterceptor} class into the spring MVC life cycle.
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

  @Autowired
  private RequestInterceptor requestInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(requestInterceptor).addPathPatterns("/**/elements/**/");
    registry.addInterceptor(requestInterceptor).addPathPatterns("/**/users/**/");
  }
}
