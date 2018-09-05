package org.heigit.bigspatialdata.ohsome.ohsomeApi;

import java.io.IOException;
import java.sql.SQLException;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.GeometryBuilder;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.oshdb.DbConnData;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.oshdb.ExtractMetadata;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBDatabase;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBH2;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBIgnite;
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
@ComponentScan({"org.heigit.bigspatialdata.ohsome.ohsomeApi"})
public class Application implements ApplicationRunner {

  public static final String apiVersion = "0.9";

  /** Main method to run this SpringBootApplication. */
  public static void main(String[] args) {

    if (args == null || args.length == 0) {
      throw new RuntimeException(
          "You need to define at least the '--database.db' or the '--database.ignite'"
              + " + '--database.keytables' parameter(s).");
    }
    SpringApplication.run(Application.class, args);
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {

    boolean multithreading = true;
    boolean caching = false;
    // only used when tests are executed directly in Eclipse
    if (System.getProperty("database.db") != null) {
      DbConnData.h2Db = new OSHDBH2(System.getProperty("database.db"));
      extractMetadata(DbConnData.h2Db);
    }
    try {
      for (String paramName : args.getOptionNames()) {
        switch (paramName) {
          case "database.db":
            DbConnData.h2Db = new OSHDBH2(args.getOptionValues(paramName).get(0));
            extractMetadata(DbConnData.h2Db);
            break;
          case "database.ignite":
            DbConnData.igniteDb = new OSHDBIgnite(args.getOptionValues(paramName).get(0));
            extractMetadata(DbConnData.igniteDb);
            break;
          case "database.keytables":
            DbConnData.keytables = new OSHDBH2(args.getOptionValues(paramName).get(0));
            break;
          case "database.multithreading":
            if (args.getOptionValues(paramName).get(0).equals("false")) {
              multithreading = false;
            }
            break;
          case "database.caching":
            if (args.getOptionValues(paramName).get(0).equals("true")) {
              caching = true;
            }
            break;
          default:
            break;
        }
      }
      if ((DbConnData.h2Db == null && DbConnData.igniteDb == null)
          || (DbConnData.h2Db != null && DbConnData.igniteDb != null)) {
        throw new RuntimeException(
            "You have to define either the '--database.db' or the '--database.ignite' parameter.");
      }
      if (DbConnData.h2Db != null) {
        DbConnData.h2Db.multithreading(multithreading);
        DbConnData.h2Db.inMemory(caching);
        DbConnData.tagTranslator = new TagTranslator(DbConnData.h2Db.getConnection());
      } else {
        DbConnData.keytables.multithreading(multithreading);
        DbConnData.tagTranslator = new TagTranslator(DbConnData.keytables.getConnection());
      }
    } catch (ClassNotFoundException | SQLException e) {
      throw new RuntimeException(e.getMessage());
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
      ExtractMetadata.dataPoly = geomBuilder.createGeometryFromMetadataGeoJson(dataPolyString);
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
