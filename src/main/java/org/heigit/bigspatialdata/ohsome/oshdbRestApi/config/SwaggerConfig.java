package org.heigit.bigspatialdata.ohsome.oshdbRestApi.config;

import static springfox.documentation.builders.PathSelectors.regex;
import java.util.ArrayList;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/** Swagger configuration class */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
  @Bean
  public Docket api() {

    // custom response messages to define the used error codes and their exception class
    ArrayList<ResponseMessage> responseMessages = new ArrayList<ResponseMessage>();
    responseMessages
        .add(new ResponseMessageBuilder().code(400).message("BadRequestException").build());
    responseMessages
        .add(new ResponseMessageBuilder().code(401).message("UnauthorizedException").build());
    responseMessages
        .add(new ResponseMessageBuilder().code(405).message("NotAllowedException").build());
    responseMessages
        .add(new ResponseMessageBuilder().code(501).message("NotImplementedException").build());

    return new Docket(DocumentationType.SWAGGER_2).select()
        .apis(RequestHandlerSelectors.basePackage("org.heigit.bigspatialdata.ohsome.oshdbRestApi"))
        .paths(regex("/elements.*")).build().apiInfo(apiInfo()).useDefaultResponseMessages(false)
        .tags(new Tag("area-controller", "Processes /elements/area resources"),
            new Tag("count-controller", "Processes /elements/count resources"),
            new Tag("density-controller", "Processes /elements/density resources"),
            new Tag("length-controller", "Processes /elements/length resources"),
            new Tag("perimeter-controller", "Processes /elements/perimeter resources"))
        .globalResponseMessage(RequestMethod.GET, responseMessages)
        .globalResponseMessage(RequestMethod.POST, responseMessages);
  }

  /**
   * Defines information about this API displayed on the Swagger2 documentation.
   * 
   * @return {@link springfox.documentation.service.ApiInfo ApiInfo} defining information about this
   *         API.
   */
  private ApiInfo apiInfo() {
    return new ApiInfo("OHSOME API",
        "This RESTful API aims to leverage the tools of the OSHDB Java API through allowing to access some of its functionalities via HTTP requests.",
        "v_0.1", "Terms of service",
        new Contact("Heidelberg Institute for Geoinformation Technology", "http://www.heigit.org",
            "fabian.kowatsch@uni-heidelberg.de"),
        "License of API", "API license URL", Collections.emptyList());
  }
}
