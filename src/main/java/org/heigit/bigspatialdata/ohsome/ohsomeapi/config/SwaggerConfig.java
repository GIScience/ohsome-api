package org.heigit.bigspatialdata.ohsome.ohsomeapi.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.DefaultSwaggerParameters;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.ParameterDescriptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/** Swagger configuration class. */
@Configuration
@EnableSwagger2
@PropertySource("classpath:application.properties")
public class SwaggerConfig {

  /** Creates the Swagger2 documentation for the dataAggregation resources. */
  @Bean
  public Docket dataAggregationDocket() {
    ArrayList<ResponseMessage> responseMessages = defineResponseMessages();
    return new Docket(DocumentationType.SWAGGER_2).groupName("dataAggregation").select()
        .apis(RequestHandlerSelectors
            .basePackage("org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.dataaggregation"))
        .paths(PathSelectors.any()).build().apiInfo(apiInfo()).useDefaultResponseMessages(false)
        .globalOperationParameters(defineGlobalOperationParams(false))
        .tags(new Tag("users", "Data Aggregation functions on users"),
            new Tag("elementsArea", "Area resources for polygonal objects"),
            new Tag("elementsLength", "Length resources for line objects"),
            new Tag("elementsCount", "Count resources for point/line/polygonal objects"),
            new Tag("elementsPerimeter", "Perimeter resources for polygonal objects"))
        .forCodeGeneration(true).globalResponseMessage(RequestMethod.GET, responseMessages)
        .globalResponseMessage(RequestMethod.POST, responseMessages);
  }

  /** Creates the Swagger2 documentation for the metadata resources. */
  @Bean
  public Docket metadataDocket() {
    ArrayList<ResponseMessage> responseMessages = defineResponseMessages();
    return new Docket(DocumentationType.SWAGGER_2).groupName("metadata").select()
        .apis(RequestHandlerSelectors
            .basePackage("org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.metadata"))
        .paths(PathSelectors.any()).build().apiInfo(apiInfo()).useDefaultResponseMessages(false)
        .tags(new Tag("metadata", "")).forCodeGeneration(true)
        .globalResponseMessage(RequestMethod.GET, responseMessages);
  }

  /** Creates the Swagger2 documentation for the data extraction resources. */
  @Bean
  public Docket rawDataDocket() {
    ArrayList<ResponseMessage> responseMessages = defineResponseMessages();
    return new Docket(DocumentationType.SWAGGER_2).groupName("dataExtraction").select()
        .apis(RequestHandlerSelectors
            .basePackage("org.heigit.bigspatialdata.ohsome.ohsomeapi.controller.rawdata"))
        .paths(PathSelectors.any()).build().apiInfo(apiInfo()).useDefaultResponseMessages(false)
        .globalOperationParameters(defineGlobalOperationParams(true))
        .tags(new Tag("dataExtraction", "Direct access to OSM data"),
            new Tag("dataExtractionFullHistory",
                "Direct access to the full-history of each OSM object"))
        .forCodeGeneration(true).globalResponseMessage(RequestMethod.GET, responseMessages);
  }

  /** Defines custom response messages for the used response codes. */
  private ArrayList<ResponseMessage> defineResponseMessages() {
    ArrayList<ResponseMessage> responseMessages = new ArrayList<>();
    responseMessages.add(new ResponseMessageBuilder().code(200).message("OK").build());
    responseMessages.add(new ResponseMessageBuilder().code(400).message("Bad request").build());
    responseMessages.add(new ResponseMessageBuilder().code(401).message("Unauthorized").build());
    responseMessages.add(new ResponseMessageBuilder().code(404).message("Not found").build());
    responseMessages
        .add(new ResponseMessageBuilder().code(413).message("Payload too large").build());
    responseMessages
        .add(new ResponseMessageBuilder().code(405).message("Method not allowed").build());
    responseMessages
        .add(new ResponseMessageBuilder().code(500).message("Internal server error").build());
    responseMessages.add(new ResponseMessageBuilder().code(501).message("Not implemented").build());
    return responseMessages;
  }

  /** Defines information about this API. */
  private ApiInfo apiInfo() {
    return new ApiInfo("ohsome API",
        "This REST-based API aims to leverage the tools of the "
            + "<a href=\"https://github.com/GIScience/oshdb\" target=\"_blank\">OSHDB</a> "
            + "through allowing to access some of its functionalities via HTTP requests.",
        "0.9", "",
        new Contact("Heidelberg Institute for Geoinformation Technology", "https://www.heigit.org",
            "info@heigit.org"),
        "License of the used data", "https://ohsome.org/copyrights", Collections.emptyList());
  }

  /**
   * Defines the description of each parameter, which are used in all resources for the Swagger2
   * documentation.
   */
  private List<Parameter> defineGlobalOperationParams(boolean isFullHistory) {
    final String STRING = "string";
    final String QUERY = "query";
    List<Parameter> globalOperationParams = new ArrayList<>();
    globalOperationParams.add(new ParameterBuilder().name("bboxes")
        .description(ParameterDescriptions.BBOXES_DESCR).modelRef(new ModelRef(STRING))
        .parameterType(QUERY).defaultValue(DefaultSwaggerParameters.BBOX).required(false).build());
    globalOperationParams.add(new ParameterBuilder().name("bcircles")
        .description(ParameterDescriptions.BCIRCLES_DESCR).modelRef(new ModelRef(STRING))
        .parameterType(QUERY).defaultValue("").required(false).build());
    globalOperationParams.add(new ParameterBuilder().name("bpolys")
        .description(ParameterDescriptions.BPOLYS_DESCR).modelRef(new ModelRef(STRING))
        .parameterType(QUERY).defaultValue("").required(false).build());
    globalOperationParams
        .add(new ParameterBuilder().name("types").description(ParameterDescriptions.TYPES_DESCR)
            .modelRef(new ModelRef(STRING)).allowMultiple(true).parameterType(QUERY)
            .defaultValue(DefaultSwaggerParameters.TYPE).required(false).build());
    globalOperationParams.add(new ParameterBuilder().name("keys")
        .description(ParameterDescriptions.KEYS_DESCR).modelRef(new ModelRef(STRING))
        .parameterType(QUERY).defaultValue("").required(false).build());
    globalOperationParams.add(new ParameterBuilder().name("values")
        .description(ParameterDescriptions.VALUES_DESCR).modelRef(new ModelRef(STRING))
        .parameterType(QUERY).defaultValue("").required(false).build());
    globalOperationParams.add(new ParameterBuilder().name("userids")
        .description(ParameterDescriptions.USERIDS_DESCR).modelRef(new ModelRef(STRING))
        .parameterType(QUERY).defaultValue("").required(false).build());
    globalOperationParams.add(new ParameterBuilder().name("format")
        .description(ParameterDescriptions.FORMAT_DESCR).modelRef(new ModelRef(STRING))
        .parameterType(QUERY).defaultValue("json").required(false).build());
    if (!isFullHistory) {
      globalOperationParams
          .add(new ParameterBuilder().name("time").description(ParameterDescriptions.TIME_DESCR)
              .modelRef(new ModelRef(STRING)).parameterType(QUERY)
              .defaultValue(DefaultSwaggerParameters.TIME).required(false).build());
    } else {
      globalOperationParams.add(new ParameterBuilder().name("time")
          .description(ParameterDescriptions.TIME_DESCR).modelRef(new ModelRef(STRING))
          .parameterType(QUERY).defaultValue("2014-01-01,2017-01-01").required(false).build());
    }
    globalOperationParams.add(new ParameterBuilder().name("showMetadata")
        .description(ParameterDescriptions.SHOW_METADATA_DESCR).modelRef(new ModelRef(STRING))
        .parameterType(QUERY).defaultValue(DefaultSwaggerParameters.SHOW_METADATA).required(false)
        .build());
    return globalOperationParams;
  }
}
