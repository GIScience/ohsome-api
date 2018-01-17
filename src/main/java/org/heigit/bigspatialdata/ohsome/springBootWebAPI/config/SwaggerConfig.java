package org.heigit.bigspatialdata.ohsome.springBootWebAPI.config;

import static springfox.documentation.builders.PathSelectors.regex;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Swagger configuration class.
 * At the moment it is disabled.
 *
 */
//@Configuration
//@EnableSwagger2
public class SwaggerConfig {
  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.basePackage("org.heigit.bigspatialdata.ohsome.springBootWebAPI"))
        .paths(regex("/elements.*")).build();
  }
}
