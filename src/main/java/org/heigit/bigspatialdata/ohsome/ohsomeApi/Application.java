package org.heigit.bigspatialdata.ohsome.ohsomeApi;

import java.sql.SQLException;
import java.util.ArrayList;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBDatabase;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBH2;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBIgnite;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

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
  private static ArrayList<String> metadata = null;
  private static Polygon dataPoly = null;
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
            metadata = extractMetadata(h2Db);
            break;
          case "database.ignite":
            igniteDb = new OSHDBIgnite(args.getOptionValues(paramName).get(0));
            metadata = extractMetadata(igniteDb);
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
      createDataPoly();
    } catch (ClassNotFoundException | SQLException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Extracts some metadata from the given db object.
   * 
   * @param db
   * @return
   */
  private ArrayList<String> extractMetadata(OSHDBDatabase db) {

    if (db.metadata("data.bpoly_double") == null && db.metadata("data.timerange_str") == null) {
      return null;
    } else {
      ArrayList<String> metadata = new ArrayList<String>();
      if (db.metadata("data.timerange_str") != null) {
        String[] timeranges = db.metadata("data.timerange_str").split(",");
        metadata.add(timeranges[0]);
        metadata.add(timeranges[1]);
      }
      if (db.metadata("data.bpoly_double") != null)
        metadata.add(db.metadata("data.bpoly_double"));
      return metadata;
    }
  }

  /**
   * Creates a polygon from the coordinates provided by the metadata containing the spatial extend
   * of the underlying data. Works at the moment only for non-complex polygons.
   */
  private void createDataPoly() {

    GeometryFactory geomFact = new GeometryFactory();
    Coordinate[] coords;
    if (metadata != null && metadata.get(2) != null) {
      String[] singleCoords = metadata.get(2).split(",");
      coords = new Coordinate[singleCoords.length / 2];
      int count = 0;
      for (int i = 0; i < singleCoords.length; i += 2) {
        coords[count] = new Coordinate(Double.parseDouble(singleCoords[i]),
            Double.parseDouble(singleCoords[i + 1]));
        count++;
      }
      dataPoly = geomFact.createPolygon(coords);
    }
    // hard-coded values for test purposes
    // else {
    // coords = new Coordinate[] {new Coordinate(80.76, 29.42), new Coordinate(88.48, 31.27),
    // new Coordinate(89.92, 25.6), new Coordinate(84.07, 25.65), new Coordinate(78.87, 25.68),
    // new Coordinate(80.76, 29.42)};
    // dataPoly = geomFact.createPolygon(coords);
    // }
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

  public static ArrayList<String> getMetadata() {
    return metadata;
  }

  public static Polygon getDataPoly() {
    return dataPoly;
  }

  public static TagTranslator getTagTranslator() {
    return tagTranslator;
  }
}
