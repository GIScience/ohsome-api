package org.heigit.ohsome.ohsomeapi.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import org.heigit.ohsome.ohsomeapi.Application;
import org.heigit.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.ohsome.oshdb.api.db.H2Support;
import org.heigit.ohsome.oshdb.api.db.OSHDBH2;
import org.heigit.ohsome.oshdb.api.db.OSHDBIgnite;
import org.heigit.ohsome.oshdb.api.db.OSHDBJdbc;
import org.heigit.ohsome.oshdb.util.exceptions.OSHDBKeytablesNotFoundException;
import org.heigit.ohsome.oshdb.util.tagtranslator.CachedTagTranslator;
import org.springframework.boot.ApplicationArguments;

/**
 * Utility Class for parsing and configuration the ohsome API application.
 */
public class ConfigureApplication {

  private enum DatabaseType {
    H2, JDBC, IGNITE, NONE
  }

  private boolean multithreading = true;
  private String dbPrefix = "";
  private long timeoutInMilliseconds = Application.DEFAULT_TIMEOUT_IN_MILLISECONDS;
  private int numberOfClusterNodes = Application.DEFAULT_NUMBER_OF_CLUSTER_NODES;
  private int numberOfDataExtractionThreads = Application.DEFAULT_NUMBER_OF_DATA_EXTRACTION_THREADS;
  private String databaseUser;
  private String databasePassword;
  private DatabaseType databaseType = DatabaseType.NONE;
  private String databaseUrl;
  private String keytablesUser;
  private String keytablesPassword;
  private DatabaseType keytablesType = DatabaseType.NONE;
  private String keytablesUrl;
  private long ttMaxBytesValue = 512L * 1024L * 1024L;
  private int ttMaxNumRoles = Integer.MAX_VALUE;

  private ConfigureApplication(ApplicationArguments args) {
    for (String paramName : args.getOptionNames()) {
      switch (paramName) {
        // TODO change to "database.h2" for a future stable version
        case "database.db":
          databaseType = DatabaseType.H2;
          databaseUrl = args.getOptionValues(paramName).get(0);
          break;
        case "database.jdbc":
          databaseType = DatabaseType.JDBC;
          String[] jdbcParam = args.getOptionValues(paramName).get(0).split(";");
          databaseUrl = jdbcParam[1];
          databaseUser = jdbcParam[2];
          databasePassword = jdbcParam[3];
          break;
        case "database.ignite":
          databaseType = DatabaseType.IGNITE;
          databaseUrl = args.getOptionValues(paramName).get(0);
          break;
        case "database.keytables":
          keytablesType = DatabaseType.H2;
          keytablesUrl = args.getOptionValues(paramName).get(0);
          break;
        case "database.keytables.jdbc":
          keytablesType = DatabaseType.JDBC;
          String[] keytablesJdbcParam = args.getOptionValues(paramName).get(0).split(";");
          keytablesUrl = keytablesJdbcParam[1];
          keytablesUser = keytablesJdbcParam[2];
          keytablesPassword = keytablesJdbcParam[3];
          break;
        case "database.multithreading":
          if (args.getOptionValues(paramName).get(0).equalsIgnoreCase("false")) {
            multithreading = false;
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
        case "tt.maxbytesvalue":
          ttMaxBytesValue = Long.parseLong(args.getOptionValues(paramName).get(0));
          break;
        case "tt.maxnumroles":
          ttMaxNumRoles = Integer.parseInt(args.getOptionValues(paramName).get(0));
          break;
        default:
          break;
      }
    }
  }

  /**
   * Method run by the Application class to parse incoming command line arguments
   * @param args ApplicationArguments from spring to be parsed.
   */
  public static void parseArguments(ApplicationArguments args)
      throws OSHDBKeytablesNotFoundException, IOException {
    var config = new ConfigureApplication(args);
    switch (config.keytablesType) {
      case H2:
        DbConnData.keytablesDbSource = H2Support.createJdbcPoolFromPath(config.keytablesUrl);
        break;
      case JDBC:
        var keytablesHc = new HikariConfig();
        keytablesHc.setJdbcUrl(config.keytablesUrl);
        keytablesHc.setUsername(config.keytablesUser);
        keytablesHc.setPassword(config.keytablesPassword);
        keytablesHc.setMaximumPoolSize(config.numberOfDataExtractionThreads);
        DbConnData.keytablesDbSource = new HikariDataSource(keytablesHc);
        break;
      default:
        break;
    }
    switch (config.databaseType) {
      case H2:
        DbConnData.db = new OSHDBH2(config.databaseUrl);
        break;
      case JDBC:
        var hc = new HikariConfig();
        hc.setJdbcUrl(config.databaseUrl);
        hc.setUsername(config.databaseUser);
        hc.setPassword(config.databasePassword);
        DbConnData.dbSource =  new HikariDataSource(hc);
        DbConnData.db = new OSHDBJdbc(DbConnData.dbSource, config.dbPrefix,
            DbConnData.keytablesDbSource == null ? DbConnData.dbSource
                : DbConnData.keytablesDbSource);
        break;
      case IGNITE:
        if (DbConnData.keytablesDbSource == null) {
          throw new IllegalArgumentException("Keytables parameter missing");
        }
        DbConnData.db = new OSHDBIgnite(config.databaseUrl, config.dbPrefix,
            DbConnData.keytablesDbSource);
        break;
      default:
        throw new IllegalArgumentException(
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

    // initialize TagTranslator
    DbConnData.tagTranslator = new CachedTagTranslator(DbConnData.db.getTagTranslator(),
        config.ttMaxBytesValue, config.ttMaxNumRoles);

    // extract metadata
    RequestUtils.extractOSHDBMetadata();

    if (DbConnData.db instanceof OSHDBIgnite) {
      ((OSHDBIgnite) DbConnData.db).onClose(() -> {
        // TODO add connections to close
      });
    }
  }
}