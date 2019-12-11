package org.heigit.bigspatialdata.ohsome.ohsomeapi.config;

import org.heigit.bigspatialdata.ohsome.ohsomeapi.interceptor.LoggingRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Logger config, used for logging response code, accessed URI and execution time. */
@Configuration
public class LoggingConfig implements WebMvcConfigurer {

  @Autowired
  private LoggingRequestInterceptor loggingRequestInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(loggingRequestInterceptor).addPathPatterns("/elements/**/",
        "/elementsFullHistory/**/", "/metadata/**/", "/users/**/");
  }

}
