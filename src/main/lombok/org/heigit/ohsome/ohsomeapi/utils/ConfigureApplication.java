package org.heigit.ohsome.ohsomeapi.utils;

import com.zaxxer.hikari.HikariConfig;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.heigit.ohsome.ohsomeapi.Application;
import org.heigit.ohsome.ohsomeapi.exception.DatabaseAccessException;
import org.heigit.ohsome.ohsomeapi.exception.ExceptionMessages;
import org.heigit.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.ohsomeapi.oshdb.RemoteTagTranslator;
import org.heigit.ohsome.oshdb.api.db.OSHDBH2;
import org.heigit.ohsome.oshdb.api.db.OSHDBIgnite;
import org.heigit.ohsome.oshdb.api.db.OSHDBJdbc;
import org.heigit.ohsome.oshdb.util.exceptions.OSHDBKeytablesNotFoundException;
import org.heigit.ohsome.oshdb.util.tagtranslator.TagTranslator;
import org.springframework.boot.ApplicationArguments;

public class ConfigureApplication {

  private boolean multithreading = true;
  private boolean caching = false;
  private String dbPrefix = null;
  private long timeoutInMilliseconds = Application.DEFAULT_TIMEOUT_IN_MILLISECONDS;
  private int numberOfClusterNodes = Application.DEFAULT_NUMBER_OF_CLUSTER_NODES;
  private int numberOfDataExtractionThreads = Application.DEFAULT_NUMBER_OF_DATA_EXTRACTION_THREADS;
  private String databaseClassName;
  private String databaseUser;
  private String databasePassword;
  private String databaseType;
  private String databaseUrl;
  private String keytablesClassName;
  private String keytablesUser;
  private String keytablesPassword;
  private String keytablesType;
  private String keytablesUrl;

  public ConfigureApplication(ApplicationArguments args) {
    for (String paramName : args.getOptionNames()) {
      switch (paramName) {
        // TODO change to "database.h2" for a future stable version
        case "database.db":
          databaseType = "h2";
          databaseUrl = args.getOptionValues(paramName).get(0);
          break;
        case "database.jdbc":
          databaseType = "jdbc";
          String[] jdbcParam = args.getOptionValues(paramName).get(0).split(";");
          databaseClassName = jdbcParam[0];
          databaseUrl = jdbcParam[1];
          databaseUser = jdbcParam[2];
          databasePassword = jdbcParam[3];
          break;
        case "database.ignite":
          databaseType = "ignite";
          databaseUrl = args.getOptionValues(paramName).get(0);
          break;
        case "database.keytables":
          keytablesType = "h2";
          keytablesUrl = args.getOptionValues(paramName).get(0);
          break;
        case "database.keytables.jdbc":
          keytablesType = "jdbc";
          String[] keytablesJdbcParam = args.getOptionValues(paramName).get(0).split(";");
          keytablesClassName = keytablesJdbcParam[0];
          keytablesUrl = keytablesJdbcParam[1];
          keytablesUser = keytablesJdbcParam[2];
          keytablesPassword = keytablesJdbcParam[3];
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
  }

  // refactor remainder back to Application.java
  public static void preRun(ApplicationArguments args)
      throws ClassNotFoundException, SQLException, OSHDBKeytablesNotFoundException, IOException {
    var config = new ConfigureApplication(args);
    switch (config.databaseType) {
      case "h2":
        DbConnData.db = new OSHDBH2(config.databaseUrl);
        break;
      case "jdbc":
        DbConnData.db = new OSHDBJdbc(config.databaseClassName, config.databaseUrl,
            config.databaseUser, config.databasePassword);
        break;
      case "ignite":
        DbConnData.db = new OSHDBIgnite(config.databaseUrl);
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + config.databaseType);
    }
    switch (config.keytablesType) {
      case "h2":
        DbConnData.db = new OSHDBH2(config.keytablesUrl);
        break;
      case "jdbc":
        DbConnData.db = new OSHDBJdbc(config.keytablesClassName, config.keytablesUrl,
            config.keytablesUser, config.keytablesPassword);
        DbConnData.mapTagTranslator = new RemoteTagTranslator(() -> {
          try {
            Class.forName(config.keytablesClassName);
            return new TagTranslator(
                DriverManager.getConnection(config.keytablesUrl, config.keytablesUser,
                    config.keytablesPassword));
          } catch (ClassNotFoundException e) {
            throw new RuntimeException("A class with this specific name could not be found");
          } catch (OSHDBKeytablesNotFoundException | SQLException e) {
            throw new DatabaseAccessException(ExceptionMessages.DATABASE_ACCESS);
          }
        });
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.keytablesUrl);
        hikariConfig.setUsername(config.keytablesUser);
        hikariConfig.setPassword(config.keytablesPassword);
        hikariConfig.setMaximumPoolSize(config.numberOfDataExtractionThreads);
        DbConnData.keytablesDbPoolConfig = hikariConfig;
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + config.keytablesType);
    }
    if (DbConnData.db == null) {
      throw new RuntimeException(
          "You have to define one of the following three database parameters: '--database.db', "
              + "'--database.ignite', or '--database.jdbc'.");
    }
    ProcessingData.setTimeout(config.timeoutInMilliseconds / 1000.0);
    DbConnData.db.timeoutInMilliseconds(config.timeoutInMilliseconds);
    ProcessingData.setNumberOfClusterNodes(config.numberOfClusterNodes);
    ProcessingData.setNumberOfDataExtractionThreads(config.numberOfDataExtractionThreads);
    if (DbConnData.db instanceof OSHDBJdbc) {
      DbConnData.db = ((OSHDBJdbc) DbConnData.db).multithreading(config.multithreading);
    }
    if (DbConnData.db instanceof OSHDBH2) {
      DbConnData.db = ((OSHDBH2) DbConnData.db).inMemory(config.caching);
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
    if (config.dbPrefix != null) {
      DbConnData.db = DbConnData.db.prefix(config.dbPrefix);
    }
  }
}