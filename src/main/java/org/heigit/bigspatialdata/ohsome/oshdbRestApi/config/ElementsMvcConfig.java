package org.heigit.bigspatialdata.ohsome.oshdbRestApi.config;

import org.heigit.bigspatialdata.ohsome.oshdbRestApi.interceptor.ElementsRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Configuration class, which adds the
 * {@link org.heigit.bigspatialdata.ohsome.oshdbRestApi.interceptor.ElementsRequestInterceptor
 * ElementsRequestInterceptor} class into the spring MVC life cycle.
 */
@Configuration
public class ElementsMvcConfig extends WebMvcConfigurerAdapter {

  @Autowired
  private ElementsRequestInterceptor elementsRequestInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(elementsRequestInterceptor).addPathPatterns("/**/elements/**/");
  }

}
