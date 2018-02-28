package org.heigit.bigspatialdata.ohsome.ohsomeApi.config;

import org.heigit.bigspatialdata.ohsome.ohsomeApi.interceptor.ElementsRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Configuration class, which adds the
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.interceptor.ElementsRequestInterceptor
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

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addRedirectViewController("/", "/swagger-ui.html");
  }
}
