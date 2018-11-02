package org.heigit.bigspatialdata.ohsome.ohsomeapi;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.GeometryBuilder;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.inputprocessing.ProcessingData;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.oshdb.DbConnData;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.oshdb.ExtractMetadata;
import org.heigit.bigspatialdata.ohsome.ohsomeapi.oshdb.RemoteTagTranslator;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBDatabase;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBH2;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBIgnite;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBJdbc;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Main class, which is used to run this Spring boot application. Establishes a connection to the
 * database on startup in the {@link #run(ApplicationArguments) run()} method using parameters
 * provided via the console.
 */
@SpringBootApplication
@ComponentScan({"org.heigit.bigspatialdata.ohsome.ohsomeapi"})
public class Application implements ApplicationRunner {

  public static final String apiVersion = "0.9";

  /** Main method to run this SpringBootApplication. */
  public static void main(String[] args) {
    if (args == null || args.length == 0) {
      throw new RuntimeException(
          "For tests, define at least the '--database.db' or the '--database.ignite'"
              + " and '--database.keytables' parameter(s) inside the (nested) "
              + " '-DdbFilePathProperty=' parameter of 'mvn test'.");
    }
    SpringApplication.run(Application.class, args);
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    boolean multithreading = true;
    boolean caching = false;
    String dbPrefix = null;
    long timeout = 100000;
    // only used when tests are executed directly in Eclipse
    if (System.getProperty("database.db") != null) {
      DbConnData.db = new OSHDBH2(System.getProperty("database.db"));
    }
    try {
      for (String paramName : args.getOptionNames()) {
        switch (paramName) {
          case "database.db":
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
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });
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
            timeout = Long.valueOf(args.getOptionValues(paramName).get(0));
          default:
            break;
        }
      }
      if (DbConnData.db == null) {
        throw new RuntimeException(
            "You have to define one of the following three database parameters: '--database.db', "
                + "'--database.ignite', or '--database.jdbc'.");
      }
      if (DbConnData.db instanceof OSHDBJdbc) {
        DbConnData.db = ((OSHDBJdbc) DbConnData.db).multithreading(multithreading);
      }
      if (DbConnData.db instanceof OSHDBH2) {
        DbConnData.db = ((OSHDBH2) DbConnData.db).inMemory(caching);
      }
      if (DbConnData.keytables != null) {
        DbConnData.tagTranslator = new TagTranslator(DbConnData.keytables.getConnection());
        extractMetadata(DbConnData.keytables);
      } else {
        if (!(DbConnData.db instanceof OSHDBJdbc)) {
          throw new RuntimeException("Missing keytables.");
        }
        DbConnData.tagTranslator = new TagTranslator(((OSHDBJdbc) DbConnData.db).getConnection());
        extractMetadata(DbConnData.db);
      }
      if (DbConnData.mapTagTranslator == null) {
        DbConnData.mapTagTranslator = new RemoteTagTranslator(DbConnData.tagTranslator);
      }
      if (DbConnData.db instanceof OSHDBIgnite) {
        ((OSHDBIgnite) DbConnData.db).timeoutInMilliseconds(timeout);
        RemoteTagTranslator mtt = DbConnData.mapTagTranslator;
        ((OSHDBIgnite) DbConnData.db).onClose(() -> {
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
        DbConnData.db = DbConnData.db.prefix(dbPrefix);
      }
    } catch (ClassNotFoundException | SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Extracts some metadata from the given db object and adds it to the corresponding objects.
   * 
   * @param db <code>OSHDBDatabase</code> object to the OSHDB-file of either H2, or Ignite type.
   */
  private void extractMetadata(OSHDBDatabase db) throws JsonProcessingException, IOException {
    if (db.metadata("extract.region") != null) {
      String dataPolyString = db.metadata("extract.region");
      ObjectMapper mapper = new ObjectMapper();
      ExtractMetadata.dataPolyJson = mapper.readTree(dataPolyString);
      GeometryBuilder geomBuilder = new GeometryBuilder();
      geomBuilder.createGeometryFromMetadataGeoJson(dataPolyString);
      ExtractMetadata.dataPoly = ProcessingData.dataPolyGeom;
    }
    if (db.metadata("extract.timerange") != null) {
      String[] timeranges = db.metadata("extract.timerange").split(",");
      ExtractMetadata.fromTstamp = timeranges[0];
      ExtractMetadata.toTstamp = timeranges[1];
    } else {
      // the here defined hard-coded values are only temporary available
      // in future an exception will be thrown, if these metadata infos are not retrieveable
      ExtractMetadata.fromTstamp = "2007-11-01";
      ExtractMetadata.toTstamp = "2018-01-01T00:00:00";
    }
    if (db.metadata("attribution.short") != null) {
      ExtractMetadata.attributionShort = db.metadata("attribution.short");
    } else {
      ExtractMetadata.attributionShort = "© OpenStreetMap contributors";
    }
    if (db.metadata("attribution.url") != null) {
      ExtractMetadata.attributionUrl = db.metadata("attribution.url");
    } else {
      ExtractMetadata.attributionUrl = "https://ohsome.org/copyrights";
    }
  }
}
