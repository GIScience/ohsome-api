package org.heigit.ohsome.ohsomeapi.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.heigit.ohsome.ohsomeapi.controller.DefaultSwaggerParameters;
import org.heigit.ohsome.ohsomeapi.controller.ParameterDescriptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
import springfox.documentation.swagger.web.InMemorySwaggerResourcesProvider;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/** Swagger configuration class. */
@Configuration
@EnableSwagger2
@PropertySource("classpath:application.properties")
@Primary
public class SwaggerConfig implements SwaggerResourcesProvider {
  private enum OhsomeApiResourceSpec {
    DATA_AGGREGATION("Data Aggregation", 1),
    DATA_EXTRACTION("Data Extraction", 2),
    METADATA("Metadata", 9);

    private final String name;
    private final int sorting;
    OhsomeApiResourceSpec(String name, int sorting) {
      this.name = name;
      this.sorting = sorting;
    }
  }

  @Autowired
  @Qualifier("ohsomeAPIVersion")
  private String ohsomeAPIVersion;
  private final InMemorySwaggerResourcesProvider resourcesProvider;
  private final Map<String, Integer> resourcesSorting = new HashMap<>();

  /**
   * Creates swagger configuration object, initializes internal specs sorting table.
   */
  @Autowired
  public SwaggerConfig(InMemorySwaggerResourcesProvider resourcesProvider) {
    this.resourcesProvider = resourcesProvider;
    for (OhsomeApiResourceSpec spec : OhsomeApiResourceSpec.values()) {
      resourcesSorting.put(spec.name, spec.sorting);
    }
  }

  @Override
  public List<SwaggerResource> get() {
    return resourcesProvider.get().stream()
        .sorted(Comparator.comparing(r -> resourcesSorting.getOrDefault(r.getName(), 99)))
        .collect(Collectors.toList());
  }

  /** Creates the Swagger2 documentation for the data aggregation resources. */
  @Bean
  public Docket dataAggregationDocket() {
    ArrayList<ResponseMessage> responseMessages = defineResponseMessages();
    return new Docket(DocumentationType.SWAGGER_2)
        .groupName(OhsomeApiResourceSpec.DATA_AGGREGATION.name).select()
        .apis(RequestHandlerSelectors
            .basePackage("org.heigit.ohsome.ohsomeapi.controller.dataaggregation"))
        .paths(PathSelectors.any()).build().apiInfo(apiInfo()).useDefaultResponseMessages(false)
        .globalOperationParameters(defineGlobalOperationParams(false))
        .tags(
            new Tag("Elements Count",
                "Compute the count of point/linear/polygonal OSM elements", 1),
            new Tag("Elements Length", "Compute the length of linear OSM elements", 2),
            new Tag("Elements Area", "Compute the area of polygonal OSM elements", 3),
            new Tag("Elements Perimeter", "Compute the perimeter of polygonal OSM elements", 4),
            new Tag("Users Count", "Compute the count of OSM users", 5),
            new Tag("Contributions Count", "Compute the count of OSM contributions", 6))
        .forCodeGeneration(true).globalResponseMessage(RequestMethod.GET, responseMessages)
        .globalResponseMessage(RequestMethod.POST, responseMessages);
  }

  /** Creates the Swagger2 documentation for the metadata resources. */
  @Bean
  public Docket metadataDocket() {
    ArrayList<ResponseMessage> responseMessages = defineResponseMessages();
    return new Docket(DocumentationType.SWAGGER_2)
        .groupName(OhsomeApiResourceSpec.METADATA.name).select()
        .apis(
            RequestHandlerSelectors.basePackage("org.heigit.ohsome.ohsomeapi.controller.metadata"))
        .paths(PathSelectors.any()).build().apiInfo(apiInfo()).useDefaultResponseMessages(false)
        .tags(new Tag("Metadata", "Request metadata of the underlying OSHDB", 1))
        .forCodeGeneration(true).globalResponseMessage(RequestMethod.GET, responseMessages);
  }

  /** Creates the Swagger2 documentation for the data extraction resources. */
  @Bean
  public Docket dataExtractionDocket() {
    ArrayList<ResponseMessage> responseMessages = defineResponseMessages();
    return new Docket(DocumentationType.SWAGGER_2)
        .groupName(OhsomeApiResourceSpec.DATA_EXTRACTION.name).select()
        .apis(RequestHandlerSelectors
            .basePackage("org.heigit.ohsome.ohsomeapi.controller.dataextraction"))
        .paths(PathSelectors.any()).build().apiInfo(apiInfo()).useDefaultResponseMessages(false)
        .globalOperationParameters(defineGlobalOperationParams(true))
        .tags(
            new Tag("Elements Extraction", "Direct access to the OSM data", 1),
            new Tag("Full History Elements Extraction",
                "Direct access to the full history of the OSM data", 2),
            new Tag("Contributions Extraction",
                "Direct access to all contributions provided to the OSM data", 3))
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
    responseMessages
        .add(new ResponseMessageBuilder().code(503).message("Service Unavailable").build());
    return responseMessages;
  }

  /** Defines information about this API. */
  private ApiInfo apiInfo() {
    return new ApiInfo("ohsome API",
        "This REST-based API aims to leverage the tools of the "
            + "<a href=\"https://github.com/GIScience/oshdb\" target=\"_blank\">OSHDB</a> "
            + "through allowing to access some of its functionalities via HTTP requests. \n"
            + "The official documentation can be found "
            + "<a href=\"https://docs.ohsome.org/ohsome-api/stable\" target=\"_blank\">here</a>. ",
        ohsomeAPIVersion, "",
        new Contact("Heidelberg Institute for Geoinformation Technology", "https://www.heigit.org",
            "info@heigit.org"),
        "License of the used data", "https://ohsome.org/copyrights", Collections.emptyList());
  }

  /**
   * Defines the description of each parameter, which are used in all resources for the Swagger2
   * documentation.
   */
  private List<Parameter> defineGlobalOperationParams(boolean isDataExtraction) {
    final String string = "string";
    final String query = "query";
    List<Parameter> globalOperationParams = new ArrayList<>();
    globalOperationParams.add(new ParameterBuilder().name("bboxes")
        .description(ParameterDescriptions.BBOXES).modelRef(new ModelRef(string))
        .parameterType(query).defaultValue(DefaultSwaggerParameters.BBOX).required(false).build());
    globalOperationParams.add(new ParameterBuilder().name("bcircles")
        .description(ParameterDescriptions.BCIRCLES).modelRef(new ModelRef(string))
        .parameterType(query).defaultValue("").required(false).build());
    globalOperationParams.add(new ParameterBuilder().name("bpolys")
        .description(ParameterDescriptions.BPOLYS).modelRef(new ModelRef(string))
        .parameterType(query).defaultValue("").required(false).build());
    globalOperationParams
        .add(new ParameterBuilder().name("filter").description(ParameterDescriptions.FILTER)
            .modelRef(new ModelRef(string)).parameterType(query)
            .defaultValue(DefaultSwaggerParameters.GENERIC_FILTER).required(false).build());
    globalOperationParams.add(new ParameterBuilder().name("timeout")
        .description(ParameterDescriptions.TIMEOUT).modelRef(new ModelRef(string))
        .parameterType(query).defaultValue("").required(false).build());
    if (!isDataExtraction) {
      globalOperationParams
          .add(new ParameterBuilder().name("time").description(ParameterDescriptions.TIME)
              .modelRef(new ModelRef(string)).parameterType(query)
              .defaultValue(DefaultSwaggerParameters.TIME).required(false).build());
      globalOperationParams.add(new ParameterBuilder().name("format")
          .description(ParameterDescriptions.FORMAT).modelRef(new ModelRef(string))
          .parameterType(query).defaultValue("json").required(false).build());
    } else {
      globalOperationParams.add(new ParameterBuilder().name("time")
          .description(ParameterDescriptions.TIME_DATA_EXTRACTION).modelRef(new ModelRef(string))
          .parameterType(query).defaultValue("2016-01-01,2017-01-01").required(true).build());
      globalOperationParams.add(new ParameterBuilder().name("properties")
          .description(ParameterDescriptions.PROPERTIES).modelRef(new ModelRef(string))
          .parameterType(query).defaultValue("tags").required(false).build());
      globalOperationParams.add(new ParameterBuilder().name("clipGeometry")
          .description(ParameterDescriptions.CLIP_GEOMETRY).modelRef(new ModelRef(string))
          .parameterType(query).defaultValue("true").required(false).build());
    }
    globalOperationParams.add(new ParameterBuilder().name("showMetadata")
        .description(ParameterDescriptions.SHOW_METADATA).modelRef(new ModelRef(string))
        .parameterType(query).defaultValue("").required(false).build());
    return globalOperationParams;
  }
}