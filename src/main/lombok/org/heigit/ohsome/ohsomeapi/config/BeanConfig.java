package org.heigit.ohsome.ohsomeapi.config;

import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import org.heigit.ohsome.ohsomeapi.Application;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.ContributionView;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.SnapshotView;
import org.heigit.ohsome.ohsomeapi.refactoring.operations.ViewOnData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

@Configuration
public class BeanConfig {

  @Autowired
  HttpServletRequest servletRequest;

  @Bean
  @RequestScope
  ViewOnData getViewOnData() {
    String url = servletRequest.getRequestURL().toString();
    if (url.contains("/contributions") || url.contains("/users") || url.contains(
        "elementsfullhistory")) {
      return new ContributionView();
    }
    return new SnapshotView();
  }

  /**
   * Get the API version. It throws a RuntimeException if the API version is null.
   *
   * @throws RuntimeException if API version from the application.properties file cannot be loaded
   */
  @Bean
  @Qualifier("ohsomeAPIVersion")
  String ohsomeAPIVersion() {
    String apiVersion;
    try {
      Properties properties = new Properties();
      properties
          .load(Application.class.getClassLoader().getResourceAsStream("application.properties"));
      apiVersion = properties.getProperty("project.version");
    } catch (Exception e) {
      return "The application.properties file could not be found";
    }
    if (apiVersion == null) {
      throw new RuntimeException(
          "The API version from the application.properties file could not be loaded.");
    } else {
      return apiVersion;
    }
  }
}
