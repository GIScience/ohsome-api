package org.heigit.bigspatialdata.ohsome.springBootWebAPI.config;

import org.heigit.bigspatialdata.ohsome.springBootWebAPI.interceptor.ElementsRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
/**
 * Config class, which adds the ElementsRequestInterceptor into the spring MVC life cycle.
 *
 */
public class ElementsMVCConfig extends WebMvcConfigurerAdapter {
 
    @Autowired
    private ElementsRequestInterceptor elementsRequestInterceptor;
 
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(elementsRequestInterceptor)
          .addPathPatterns("/**/elements/**/");
    }
}
