package org.heigit.ohsome.ohsomeapi.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Used to allow the not encoded pipe sign (|) in request URLs. */
@Configuration
public class TomcatConfig {
  
  @Bean
  public ConfigurableServletWebServerFactory webServerFactory() {
      TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
      factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
          @Override
          public void customize(Connector connector) {
              connector.setProperty("relaxedQueryChars", "|");
          }
      });
      return factory;
  }
}
