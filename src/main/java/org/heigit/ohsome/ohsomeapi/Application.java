package org.heigit.ohsome.ohsomeapi;

import com.google.common.base.Throwables;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBDatabase;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBIgnite;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBJdbc;
import org.heigit.bigspatialdata.oshdb.util.exceptions.OSHDBKeytablesNotFoundException;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;
import org.heigit.ohsome.ohsomeapi.config.OSHDBConfig;
import org.heigit.ohsome.ohsomeapi.exception.ConfigurationException;
import org.heigit.ohsome.ohsomeapi.exception.DatabaseAccessException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
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
  public static final String APPLICATION_NAME = "ohsome_api." + API_VERSION + "";
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
   *       
   * @throws RuntimeException if a class with a specific name could not be found, or if the database
   *         parameter is not defined
   * @throws DatabaseAccessException if the access to keytables or database is not possible
   * @throws SQLException thrown by
   *         {@link org.heigit.bigspatialdata.oshdb.api.db.OSHDBH2#OSHDBH2(String) OSHDBH2}
   * @throws ClassNotFoundException thrown by
   *         {@link org.heigit.bigspatialdata.oshdb.api.db.OSHDBH2#OSHDBH2(String) OSHDBH2}
   * @throws OSHDBKeytablesNotFoundException thrown by
   *         {@link org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator#TagTranslator(java.sql.Connection)
   *         TagTranslator}
   * @throws IOException thrown by
   *         {@link org.heigit.ohsome.ohsomeapi.utils.RequestUtils#extractOSHDBMetadata()
   *         extractOSHDBMetadata}
   */
  public static void main(String[] args) {
    try {
      context = SpringApplication.run(Application.class, args);
    } catch (Exception e) {
      Throwable rootCause = Throwables.getRootCause(e);
      if (rootCause instanceof ConfigurationException) {
        System.err.println("Configurations Problem. We need a better text here!");
        System.out.println(rootCause.getMessage());
      } else {
        e.printStackTrace();
      }
      System.exit(1);
    }
  }

  @Bean(name = "oshdb")
  public OSHDBDatabase oshdbInstance(OSHDBConfig config) {
    final OSHDBDatabase oshdb;
    if (config.hasIgnite()) {
      oshdb = new OSHDBIgnite(config.getIgnitePath().toFile());
    } else {
      config.hasH2Db();
      config.hasJdbcDb();
      // TODO: add parts for other DBs
      throw new ConfigurationException();
    }
    oshdb.prefix(config.getPrefix());
    oshdb.timeoutInMilliseconds(config.getTimeout());
    return oshdb;
  }

  @Bean("keytablesPool")
  public DataSource keytablesDataSourceInstance(OSHDBConfig config) {
    if (config.hasKeytables()) {
      HikariDataSource hikari = new HikariDataSource();
      if (config.getKeytablesDataSourceClassName() != null) {
        hikari.setDataSourceClassName(config.getKeytablesDataSourceClassName());
        hikari.addDataSourceProperty("serverName", config.getKeytablesServerName());
        hikari.addDataSourceProperty("portNumber", config.getKeytablesPortNumber());
        hikari.addDataSourceProperty("databaseName", config.getKeytablesDatabaseName());
        hikari.addDataSourceProperty("applicationName", APPLICATION_NAME);
      } else if (config.getKeytablesJdbcUrl() != null) {
        String url = config.getKeytablesJdbcUrl();

        if (!url.toLowerCase().contains("applicationName")) {
          if (url.indexOf("?") != -1) {
            url += "?ApplicationName=" + APPLICATION_NAME;
          } else {
            url += "&ApplicationName=" + APPLICATION_NAME;
          }
        }

        hikari.setJdbcUrl(url);
      } else {
        throw new ConfigurationException();
      }

      hikari.setUsername(config.getKeytablesUsername());
      hikari.setPassword(config.getKeytablesPassword());

      if (config.getKeytablesPoolSize() > 0) {
        hikari.setMaximumPoolSize(config.getKeytablesPoolSize());
      }
      return hikari;
    }
    return null;
  }

  @Bean(name = "tagTranslator")
  public TagTranslator tagTranslatorInstance(OSHDBDatabase oshdb, DataSource keytablesPool)
      throws OSHDBKeytablesNotFoundException, SQLException {
    if (keytablesPool != null) {
      return new TagTranslator(keytablesPool.getConnection());
    }
    if (oshdb instanceof OSHDBJdbc) {
      return new TagTranslator(((OSHDBJdbc) oshdb).getConnection());
    }
    throw new ConfigurationException();
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
