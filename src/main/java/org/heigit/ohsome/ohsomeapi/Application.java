package org.heigit.ohsome.ohsomeapi;

import com.zaxxer.hikari.HikariConfig;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBDatabase;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBH2;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBIgnite;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBJdbc;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;
import org.heigit.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.ohsome.ohsomeapi.oshdb.RemoteTagTranslator;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.context.annotation.RequestScope;

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
  private static OSHDBDatabase oshdb;

  public static ApplicationContext getApplicationContext() {
    return context;
  }

  /** Main method to run this SpringBootApplication. */
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
  
  @Bean
  @RequestScope
  public ExtractMetadata extractMetadataInstance() throws IOException {
    return ExtractMetadata.extractOSHDBMetadata();
  }
  
  @Bean(name = "oshdb")
  public OSHDBDatabase oshdbInstance() {
    return oshdb;
  }
  
  

  /**
   * Reads and sets the given application arguments and makes a connection to the OSHDB.
   *
   * @param args Application arguments given over the commandline on startup
   * @throws Exception if the connection to the db cannot be established
   */
  public static void preRun(ApplicationArguments args) throws Exception {
    final String dbProperty = "database.db";
    boolean multithreading = true;
    boolean caching = false;
    String dbPrefix = null;
    long timeoutInMilliseconds = DEFAULT_TIMEOUT_IN_MILLISECONDS;
    int numberOfClusterNodes = DEFAULT_NUMBER_OF_CLUSTER_NODES;
    int numberOfDataExtractionThreads = DEFAULT_NUMBER_OF_DATA_EXTRACTION_THREADS;
    // only used when tests are executed directly in Eclipse
    if (System.getProperty(dbProperty) != null) {
      oshdb = new OSHDBH2(System.getProperty(dbProperty));
    }
    try {
      for (String paramName : args.getOptionNames()) {
        switch (paramName) {
          case dbProperty:
            oshdb = new OSHDBH2(args.getOptionValues(paramName).get(0));
            break;
          case "database.jdbc":
            String[] jdbcParam = args.getOptionValues(paramName).get(0).split(";");
            oshdb = new OSHDBJdbc(jdbcParam[0], jdbcParam[1], jdbcParam[2], jdbcParam[3]);
            break;
          case "database.ignite":
            if (oshdb != null) {
              break;
            }
            oshdb = new OSHDBIgnite(args.getOptionValues(paramName).get(0));
            break;
          case "database.keytables":
            DbConnData.keytables = new OSHDBH2(args.getOptionValues(paramName).get(0));
            break;
          case "database.keytables.jdbc":
            jdbcParam = args.getOptionValues(paramName).get(0).split(";");
            DbConnData.keytables =
                new OSHDBJdbc(jdbcParam[0], jdbcParam[1], jdbcParam[2], jdbcParam[3]);
            DbConnData.mapTagTranslator = new RemoteTagTranslator(() -> {
              try {
                Class.forName(jdbcParam[0]);
                return new TagTranslator(
                    DriverManager.getConnection(jdbcParam[1], jdbcParam[2], jdbcParam[3]));
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(jdbcParam[1]);
            hikariConfig.setUsername(jdbcParam[2]);
            hikariConfig.setPassword(jdbcParam[3]);
            hikariConfig.setMaximumPoolSize(numberOfDataExtractionThreads);
            DbConnData.keytablesDbPoolConfig = hikariConfig;
            break;
          case "database.multithreading":
            if (args.getOptionValues(paramName).get(0).equalsIgnoreCase("false")) {
              multithreading = false;
            }
            break;
          case "database.caching":
            if (args.getOptionValues(paramName).get(0).equalsIgnoreCase("true")) {
              caching = true;
            }
            break;
          case "database.prefix":
            dbPrefix = args.getOptionValues(paramName).get(0);
            break;
          case "database.timeout":
            timeoutInMilliseconds = Long.parseLong(args.getOptionValues(paramName).get(0));
            break;
          case "cluster.servernodes.count":
            numberOfClusterNodes = Integer.parseInt(args.getOptionValues(paramName).get(0));
            break;
          case "cluster.dataextraction.threadcount":
            numberOfDataExtractionThreads =
                Integer.parseInt(args.getOptionValues(paramName).get(0));
            break;
          default:
            break;
        }
      }
      if (oshdb == null) {
        throw new RuntimeException(
            "You have to define one of the following three database parameters: '--database.db', "
                + "'--database.ignite', or '--database.jdbc'.");
      }
      ProcessingData.setTimeout(timeoutInMilliseconds / 1000.0);
      oshdb.timeoutInMilliseconds(timeoutInMilliseconds);
      ProcessingData.setNumberOfClusterNodes(numberOfClusterNodes);
      ProcessingData.setNumberOfDataExtractionThreads(numberOfDataExtractionThreads);
      if (oshdb instanceof OSHDBJdbc) {
        oshdb = ((OSHDBJdbc) oshdb).multithreading(multithreading);
      }
      if (oshdb instanceof OSHDBH2) {
        oshdb = ((OSHDBH2) oshdb).inMemory(caching);
      }
      if (DbConnData.keytables != null) {
        DbConnData.tagTranslator = new TagTranslator(DbConnData.keytables.getConnection());
      } else {
        if (!(oshdb instanceof OSHDBJdbc)) {
          throw new RuntimeException("Missing keytables.");
        }
        DbConnData.tagTranslator = new TagTranslator(((OSHDBJdbc) oshdb).getConnection());
      }
      
      if (DbConnData.mapTagTranslator == null) {
        DbConnData.mapTagTranslator = new RemoteTagTranslator(DbConnData.tagTranslator);
      }
      if (oshdb instanceof OSHDBIgnite) {
        RemoteTagTranslator mtt = DbConnData.mapTagTranslator;
        ((OSHDBIgnite) oshdb).onClose(() -> {
          try {
            if (mtt.wasEvaluated()) {
              mtt.get().getConnection().close();
            }
          } catch (SQLException e) {
            throw new RuntimeException(e);
          }
        });
      }
      if (dbPrefix != null) {
        oshdb = oshdb.prefix(dbPrefix);
      }
    } catch (ClassNotFoundException | SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void run(ApplicationArguments args) {
    // empty body on purpose. main function is used instead.
  }

  /**
   * Get the API version. It throws a RuntimeException if the API version is null.
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
