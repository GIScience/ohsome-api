package org.heigit.bigspatialdata.ohsome.ohsomeApi.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.DefaultSwaggerParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

/** Swagger configuration class */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
  @Bean
  public Docket api() {

    ArrayList<ResponseMessage> responseMessages = defineResponseMessages();
    return new Docket(DocumentationType.SWAGGER_2).groupName("data aggregation").select()
        .apis(RequestHandlerSelectors.basePackage("org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.dataAggregation"))
        .paths(PathSelectors.any()).build().apiInfo(apiInfo()).useDefaultResponseMessages(false)
        .globalOperationParameters(defineGOPs())
        .tags(new Tag("/users", "Data Aggregation functions on users"),
            new Tag("/elements/area", "Area resources for polygonal objects"),
            new Tag("/elements/length", "Length resources for line objects"),
            new Tag("/elements/count", "Count resources for point/line/polygonal objects"),
            new Tag("/elements/perimeter", "Perimeter resources for polygonal objects"))
        .forCodeGeneration(true).globalResponseMessage(RequestMethod.GET, responseMessages)
        .globalResponseMessage(RequestMethod.POST, responseMessages);
  }

  @Bean
  public Docket metadataDocket() {

    ArrayList<ResponseMessage> responseMessages = defineResponseMessages();
    return new Docket(DocumentationType.SWAGGER_2).groupName("metadata").select()
        .apis(RequestHandlerSelectors.basePackage("org.heigit.bigspatialdata.ohsome.ohsomeApi.controller.metadata"))
        .paths(PathSelectors.any()).build().apiInfo(apiInfo()).useDefaultResponseMessages(false)
        .tags(new Tag("/metadata", "Metadata of the underlying data-extract"))
        .forCodeGeneration(true).globalResponseMessage(RequestMethod.GET, responseMessages)
        .globalResponseMessage(RequestMethod.POST, responseMessages);
  }

  /**
   * Defines custom response messages to define the possible response codes.
   * 
   * @return
   */
  private ArrayList<ResponseMessage> defineResponseMessages() {

    ArrayList<ResponseMessage> responseMessages = new ArrayList<ResponseMessage>();
    responseMessages.add(new ResponseMessageBuilder().code(200).message("OK").build());
    responseMessages.add(new ResponseMessageBuilder().code(400).message("Bad request").build());
    responseMessages.add(new ResponseMessageBuilder().code(401).message("Unauthorized").build());
    responseMessages.add(new ResponseMessageBuilder().code(404).message("Not found").build());
    responseMessages
        .add(new ResponseMessageBuilder().code(405).message("Method not allowed").build());
    responseMessages
        .add(new ResponseMessageBuilder().code(500).message("Internal server error").build());
    responseMessages.add(new ResponseMessageBuilder().code(501).message("Not implemented").build());
    return responseMessages;
  }

  /**
   * Defines information about this API displayed on the Swagger2 documentation.
   * 
   * @return {@link springfox.documentation.service.ApiInfo ApiInfo} defining information about this
   *         API.
   */
  private ApiInfo apiInfo() {
    return new ApiInfo("OHSOME API",
        "This REST-API aims to leverage the tools of the OSHDB-API through allowing to access some of its functionalities via HTTP requests.",
        "v_0.1", "Terms of service",
        new Contact("Heidelberg Institute for Geoinformation Technology", "http://www.heigit.org",
            "info@heigit.org"),
        "License of API and used data", "https://ohsome.org/copyrights", Collections.emptyList());
  }

  /**
   * Defines the description of each GET parameter, which is used in all resources for the Swagger2
   * documentation.
   * 
   * @return <code>ArrayList</code> holding <code>Parameter</code> objects that describe those GET
   *         parameters that are used in all resources.
   */
  private List<Parameter> defineGOPs() {

    List<Parameter> gOPs = new ArrayList<Parameter>();
    gOPs.add(new ParameterBuilder().name("bboxes")
        .description("WGS84 coordinates in the following formats: "
            + "id1:lon1,lat1,lon2,lat2|id2:lon1,lat1,lon2,lat2|... OR lon1,lat1,lon2,lat2|lon1,lat1,lon2,lat2|...; default: whole dataset (if all three boundary parameters are empty)")
        .modelRef(new ModelRef("string")).parameterType("query")
        .defaultValue(DefaultSwaggerParameters.BBOX).required(false).build());
    gOPs.add(new ParameterBuilder().name("bcircles")
        .description("WGS84 coordinates + radius in meters in the following formats: "
            + "id1:lon,lat,r|id2:lon,lat,r|... OR lon,lat,r|lon,lat,r|...; default: whole dataset (if all three boundary parameters are empty)")
        .modelRef(new ModelRef("string")).parameterType("query").defaultValue("").required(false)
        .build());
    gOPs.add(new ParameterBuilder().name("bpolys").description(
        "WGS84 coordinates given as a list of coordinate pairs (as for bboxes) or GeoJSON FeatureCollection. The first point has to be the same as "
            + "the last point and MultiPolygons are only supported in GeoJSON; default: whole dataset (if all three boundary parameters are empty)")
        .modelRef(new ModelRef("string")).parameterType("query").defaultValue("").required(false)
        .build());
    gOPs.add(new ParameterBuilder().name("types")
        .description("OSM type(s) 'node' and/or 'way' and/or 'relation'; default: all three types")
        .modelRef(new ModelRef("string")).allowMultiple(true).parameterType("query")
        .defaultValue(DefaultSwaggerParameters.TYPE).required(false).build());
    gOPs.add(new ParameterBuilder().name("keys")
        .description("OSM key(s) e.g.: 'highway', 'building'; default: no key")
        .modelRef(new ModelRef("string")).parameterType("query").defaultValue("").required(false)
        .build());
    gOPs.add(new ParameterBuilder().name("values")
        .description("OSM value(s) e.g.: 'primary', 'residential'; default: no value")
        .modelRef(new ModelRef("string")).parameterType("query").defaultValue("").required(false)
        .build());
    gOPs.add(new ParameterBuilder().name("userids").description("OSM userids; default: no userid")
        .modelRef(new ModelRef("string")).parameterType("query").defaultValue("").required(false)
        .build());
    gOPs.add(new ParameterBuilder().name("time")
        .description("ISO-8601 conform timestring(s); default: today")
        .modelRef(new ModelRef("string")).parameterType("query")
        .defaultValue(DefaultSwaggerParameters.TIME).required(false).build());
    gOPs.add(new ParameterBuilder().name("showMetadata")
        .description("Boolean operator 'true' or 'false'; default: 'false'")
        .modelRef(new ModelRef("string")).parameterType("query").defaultValue(DefaultSwaggerParameters.SHOW_METADATA)
        .required(false).build());

    return gOPs;
  }
}
