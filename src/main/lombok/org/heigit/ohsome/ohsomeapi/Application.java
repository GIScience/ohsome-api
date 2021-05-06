package org.heigit.ohsome.ohsomeapi;

import com.zaxxer.hikari.HikariConfig;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBH2;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBIgnite;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBJdbc;
import org.heigit.bigspatialdata.oshdb.util.exceptions.OSHDBKeytablesNotFoundException;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;
import org.heigit.ohsome.ohsomeapi.exception.DatabaseAccessException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.oshdb.RemoteTagTranslator;
import org.heigit.ohsome.ohsomeapi.utils.RequestUtils;
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
   * @throws SQLException thrown by {@link org.heigit.bigspatialdata.oshdb.api.db.OSHDBH2
   *         #OSHDBH2(String) OSHDBH2}
   * @throws ClassNotFoundException thrown by {@link org.heigit.bigspatialdata.oshdb.api.db.OSHDBH2
   *         #OSHDBH2(String) OSHDBH2}
   * @throws OSHDBKeytablesNotFoundException thrown by {@link
   *         org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator
   *         #TagTranslator(java.sql.Connection) TagTranslator}
   * @throws IOException thrown by {@link org.heigit.ohsome.ohsomeapi.utils.RequestUtils
   *         #extractOSHDBMetadata() extractOSHDBMetadata}
   */
  public static void preRun(ApplicationArguments args)
      throws ClassNotFoundException, SQLException, OSHDBKeytablesNotFoundException, IOException {
    final String dbProperty = "database.db";
    boolean multithreading = true;
    boolean caching = false;
    String dbPrefix = null;
    long timeoutInMilliseconds = DEFAULT_TIMEOUT_IN_MILLISECONDS;
    int numberOfClusterNodes = DEFAULT_NUMBER_OF_CLUSTER_NODES;
    int numberOfDataExtractionThreads = DEFAULT_NUMBER_OF_DATA_EXTRACTION_THREADS;
    // only used when tests are executed directly in Eclipse
    if (System.getProperty(dbProperty) != null) {
      DbConnData.db = new OSHDBH2(System.getProperty(dbProperty));
    }
    for (String paramName : args.getOptionNames()) {
      switch (paramName) {
        case dbProperty:
          DbConnData.db = new OSHDBH2(args.getOptionValues(paramName).get(0));
          break;
        case "database.jdbc":
          String[] jdbcParam = args.getOptionValues(paramName).get(0).split(";");
          DbConnData.db = new OSHDBJdbc(jdbcParam[0], jdbcParam[1], jdbcParam[2], jdbcParam[3]);
          break;
        case "database.ignite":
          if (DbConnData.db != null) {
            break;
          }
          DbConnData.db = new OSHDBIgnite(args.getOptionValues(paramName).get(0));
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
            } catch (ClassNotFoundException e) {
              throw new RuntimeException("A class with this specific name could not be found");
            } catch (OSHDBKeytablesNotFoundException | SQLException e) {
              throw new DatabaseAccessException(ExceptionMessages.DATABASE_ACCESS);
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
          numberOfDataExtractionThreads = Integer.parseInt(args.getOptionValues(paramName).get(0));
          break;
        default:
          break;
      }
    }
    if (DbConnData.db == null) {
      throw new RuntimeException(
          "You have to define one of the following three database parameters: '--database.db', "
              + "'--database.ignite', or '--database.jdbc'.");
    }
    ProcessingData.setTimeout(timeoutInMilliseconds / 1000.0);
    DbConnData.db.timeoutInMilliseconds(timeoutInMilliseconds);
    ProcessingData.setNumberOfClusterNodes(numberOfClusterNodes);
    ProcessingData.setNumberOfDataExtractionThreads(numberOfDataExtractionThreads);
    if (DbConnData.db instanceof OSHDBJdbc) {
      DbConnData.db = ((OSHDBJdbc) DbConnData.db).multithreading(multithreading);
    }
    if (DbConnData.db instanceof OSHDBH2) {
      DbConnData.db = ((OSHDBH2) DbConnData.db).inMemory(caching);
    }
    if (DbConnData.keytables != null) {
      DbConnData.tagTranslator = new TagTranslator(DbConnData.keytables.getConnection());
    } else {
      if (!(DbConnData.db instanceof OSHDBJdbc)) {
        throw new DatabaseAccessException("Missing keytables.");
      }
      DbConnData.tagTranslator = new TagTranslator(((OSHDBJdbc) DbConnData.db).getConnection());
    }
    RequestUtils.extractOSHDBMetadata();
    if (DbConnData.mapTagTranslator == null) {
      DbConnData.mapTagTranslator = new RemoteTagTranslator(DbConnData.tagTranslator);
    }
    if (DbConnData.db instanceof OSHDBIgnite) {
      RemoteTagTranslator mtt = DbConnData.mapTagTranslator;
      ((OSHDBIgnite) DbConnData.db).onClose(() -> {
        try {
          if (mtt.wasEvaluated()) {
            mtt.get().getConnection().close();
          }
        } catch (SQLException e) {
          throw new DatabaseAccessException(ExceptionMessages.DATABASE_ACCESS);
        }
      });
    }
    if (dbPrefix != null) {
      DbConnData.db = DbConnData.db.prefix(dbPrefix);
    }
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
