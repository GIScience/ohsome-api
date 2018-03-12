package org.heigit.bigspatialdata.ohsome.ohsomeApi;

import java.sql.SQLException;
import org.heigit.bigspatialdata.ohsome.ohsomeApi.inputProcessing.GeometryBuilder;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBDatabase;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBH2;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBIgnite;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Main class, which is used to run this Spring boot application. Establishes a connection to the
 * database on startup in the {@link #run(ApplicationArguments) run()} method using parameters
 * provided via the console.
 */
@SpringBootApplication
@ComponentScan({"org.heigit.bigspatialdata.ohsome.ohsomeApi"})
public class Application implements ApplicationRunner {

  private static OSHDBH2 h2Db = null;
  private static OSHDBIgnite igniteDb = null;
  private static OSHDBH2 keytables = null;
  private static String fromTstamp = null;
  private static String toTstamp = null;
  private static String attributionShort = null;
  private static String attributionUrl = null;
  private static Geometry dataPoly = null;
  private static TagTranslator tagTranslator = null;

  public static void main(String[] args) {

    if (args == null || args.length == 0)
      throw new RuntimeException(
          "You need to define at least the '--database.db' or the '--database.ignite' + '--database.keytables' parameter(s).");
    SpringApplication.run(Application.class, args);
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {

    boolean multithreading = true;
    boolean caching = false;

    // only used when tests are executed
    if (System.getProperty("database.db") != null)
      h2Db = new OSHDBH2(System.getProperty("database.db"));

    try {
      for (String paramName : args.getOptionNames()) {
        switch (paramName) {
          case "database.db":
            h2Db = new OSHDBH2(args.getOptionValues(paramName).get(0));
            extractMetadata(h2Db);
            break;
          case "database.ignite":
            igniteDb = new OSHDBIgnite(args.getOptionValues(paramName).get(0));
            extractMetadata(igniteDb);
            break;
          case "database.keytables":
            keytables = new OSHDBH2(args.getOptionValues(paramName).get(0));
            break;
          case "database.multithreading":
            if (args.getOptionValues(paramName).get(0).equals("false"))
              multithreading = false;
            break;
          case "database.caching":
            if (args.getOptionValues(paramName).get(0).equals("true"))
              caching = true;
            break;
          default:
            break;
        }
      }
      if ((h2Db == null && igniteDb == null) || (h2Db != null && igniteDb != null))
        throw new RuntimeException(
            "You have to define either the '--database.db' or the '--database.ignite' parameter.");
      if (h2Db != null) {
        h2Db.multithreading(multithreading);
        h2Db.inMemory(caching);
        tagTranslator = new TagTranslator(Application.getH2Db().getConnection());
      } else {
        keytables.multithreading(multithreading);
        tagTranslator = new TagTranslator(Application.getKeytables().getConnection());
      }
    } catch (ClassNotFoundException | SQLException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Extracts some metadata from the given db object and adds it to the corresponding objects.
   * 
   * @param db
   */
  private void extractMetadata(OSHDBDatabase db) {

    // the here defined hard-coded values are only temporary available
    // in future an exception will be thrown, if these metadata infos are not retrieveable

    if (db.metadata("extract") != null) {
      GeometryBuilder geomBuilder = new GeometryBuilder();
      dataPoly = geomBuilder.createPolygonFromMetadataGeoJson(db.metadata("extract"));
    }
    if (db.metadata("data.timerange_str") != null) {
      String[] timeranges = db.metadata("data.timerange_str").split(",");
      fromTstamp = timeranges[0];
      toTstamp = timeranges[1];
    } else {
      fromTstamp = "2007-11-01T00:00:00";
      toTstamp = "2018-03-01T00:00:00";
    }
    if (db.metadata("attribution.short") != null)
      attributionShort = db.metadata("attribution.short");
    else
      attributionShort = "© OpenStreetMap contributors";
    if (db.metadata("attribution.url") != null)
      attributionUrl = db.metadata("attribution.url");
    else
      attributionUrl = "http://ohsome.org";
  }

  public static OSHDBH2 getH2Db() {
    return h2Db;
  }

  public static OSHDBIgnite getIgniteDb() {
    return igniteDb;
  }

  public static OSHDBH2 getKeytables() {
    return keytables;
  }

  public static Geometry getDataPoly() {
    return dataPoly;
  }

  public static TagTranslator getTagTranslator() {
    return tagTranslator;
  }

  public static String getFromTstamp() {
    return fromTstamp;
  }

  public static String getToTstamp() {
    return toTstamp;
  }

  public static String getAttributionShort() {
    return attributionShort;
  }

  public static String getAttributionUrl() {
    return attributionUrl;
  }
}
