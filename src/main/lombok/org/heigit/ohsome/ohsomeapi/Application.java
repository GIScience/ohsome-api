package org.heigit.ohsome.ohsomeapi;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import org.heigit.ohsome.ohsomeapi.exception.DatabaseAccessException;
import org.heigit.ohsome.ohsomeapi.utils.ConfigureApplication;
import org.heigit.ohsome.oshdb.util.exceptions.OSHDBKeytablesNotFoundException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main class, which is used to run this Spring boot application. Establishes a connection to the
 * database on startup in the {@link #run(ApplicationArguments) run()} method using parameters
 * provided via the console.
 */
@SpringBootApplication
@ComponentScan({"org.heigit.ohsome.ohsomeapi"})
public class Application implements ApplicationRunner {
  public static final String API_VERSION = ohsomeApiVersion();
  public static final int DEFAULT_TIMEOUT_IN_MILLISECONDS = 100000;
  public static final int DEFAULT_NUMBER_OF_CLUSTER_NODES = 0;
  public static final int DEFAULT_NUMBER_OF_DATA_EXTRACTION_THREADS = 40;

  private static ApplicationContext context;

  public static ApplicationContext getApplicationContext() {
    return context;
  }

  /**
   * Main method to run this SpringBootApplication.
   *
   * @throws RuntimeException if database and keytables are not defined in the
   *         '-DdbFilePathProperty=' parameter of 'mvn test'.
   */
  public static void main(String[] args) {
    if (args == null || args.length == 0) {
      throw new RuntimeException(
          "For tests, define at least the '--database.db' or the '--database.ignite'"
              + " and '--database.keytables' parameter(s) inside the (nested) "
              + " '-DdbFilePathProperty=' parameter of 'mvn test'.");
    }
    try {
      preRun(new DefaultApplicationArguments(args));
      context = SpringApplication.run(Application.class, args);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Reads and sets the given application arguments and makes a connection to the OSHDB.
   *
   * @param args Application arguments given over the commandline on startup
   * @throws RuntimeException if a class with a specific name could not be found, or if the database
   *         parameter is not defined
   * @throws DatabaseAccessException if the access to keytables or database is not possible
   * @throws SQLException thrown by {@link org.heigit.ohsome.oshdb.api.db.OSHDBH2
   *         #OSHDBH2(String) OSHDBH2}
   * @throws ClassNotFoundException thrown by {@link org.heigit.ohsome.oshdb.api.db.OSHDBH2
   *         #OSHDBH2(String) OSHDBH2}
   * @throws OSHDBKeytablesNotFoundException thrown by {@link
   *         org.heigit.ohsome.oshdb.util.tagtranslator.TagTranslator
   *         #TagTranslator(java.sql.Connection) TagTranslator}
   * @throws IOException thrown by {@link org.heigit.ohsome.ohsomeapi.utils.RequestUtils
   *         #extractOSHDBMetadata() extractOSHDBMetadata}
   */
  public static void preRun(ApplicationArguments args)
      throws ClassNotFoundException, SQLException, OSHDBKeytablesNotFoundException, IOException {
    ConfigureApplication.parseArguments(args);
  }

  @Override
  public void run(ApplicationArguments args) {
    // empty body on purpose. main function is used instead.
  }

  /**
   * Get the API version. It throws a RuntimeException if the API version is null.
   *
   * @throws RuntimeException if API version from the application.properties file cannot be loaded
   */
  private static String ohsomeApiVersion() {
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
