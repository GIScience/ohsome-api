package org.heigit.ohsome.ohsomeapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;


/** Used for applying auto-indenting onto the JSON response. */
@Configuration
public class JacksonConfig {

  @Autowired
  private ObjectMapper objectMapper;

  @PostConstruct
  public void setup() {
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }
}
