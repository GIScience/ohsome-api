package org.heigit.bigspatialdata.ohsome.ohsomeApi.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.interceptor.ElementsRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Configuration class, which adds the
 * {@link org.heigit.bigspatialdata.ohsome.ohsomeApi.interceptor.ElementsRequestInterceptor
 * ElementsRequestInterceptor} class into the spring MVC life cycle and modifies the error-JSON
 * response.
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

  @Bean
  /**
   * Modifies the error response to have a more meaningful content.
   * 
   * @return <code>Map</code> containing the error-attributes as key-value pairs.
   */
  public ErrorAttributes modifyExceptionResponse() {
    return new DefaultErrorAttributes() {
      @Override
      public Map<String, Object> getErrorAttributes(RequestAttributes requestAttributes,
          boolean includeStackTrace) {

        Map<String, Object> errorAttributes =
            super.getErrorAttributes(requestAttributes, includeStackTrace);
        errorAttributes.remove("path");
        errorAttributes.remove("exception");
        errorAttributes.put("timestamp",
            LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        if (errorAttributes.get("message").toString().equals("No message available"))
          errorAttributes.remove("requestUrl");
        else
          errorAttributes.put("requestUrl", ElementsRequestInterceptor.requestUrl);
        return errorAttributes;
      }

    };
  }
}
