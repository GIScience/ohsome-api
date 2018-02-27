package org.heigit.bigspatialdata.ohsome.oshdbRestApi;

import java.sql.SQLException;
import java.util.ArrayList;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBDatabase;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBH2;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBIgnite;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main class, which is used to run this Spring boot application. Establishes a connection to the
 * database on startup in the {@link #run(ApplicationArguments) run()} method using parameters
 * provided via the console.
 */
@SpringBootApplication
public class Application implements ApplicationRunner {

  private static OSHDBH2 h2Db = null;
  private static OSHDBIgnite igniteDb = null;
  private static OSHDBH2 keytables = null;
  private static ArrayList<String> metadata = null;

  public static void main(String[] args) {
    if (args == null || args.length == 0)
      throw new RuntimeException(
          "You need to define at least the '--database.db' or the '--database.ignite' + '--database.keytables' parameter(s).");
    SpringApplication.run(Application.class, args);
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    boolean multithreading = true;
    try {
      for (String paramName : args.getOptionNames()) {
        if (paramName.equals("database.db")) {
          h2Db = new OSHDBH2(args.getOptionValues(paramName).get(0));
          metadata = extractMetadata(h2Db);
        } else if (paramName.equals("database.ignite")) {
          igniteDb = new OSHDBIgnite(args.getOptionValues(paramName).get(0));
          metadata = extractMetadata(igniteDb);
        } else if (paramName.equals("database.keytables")) {
          keytables = new OSHDBH2(args.getOptionValues(paramName).get(0));
        } else if (paramName.equals("database.multithreading")) {
          if (args.getOptionValues(paramName).get(0).equals("false"))
            multithreading = false;
        } else {
          // do nothing as some configuration parameters have a direct impact
        }
      }
      if ((h2Db == null && igniteDb == null) || (h2Db != null && igniteDb != null))
        throw new RuntimeException(
            "You have to define either the '--database.db' or the '--database.ignite' parameter.");

      if (h2Db != null)
        h2Db.multithreading(multithreading);
      else
        keytables.multithreading(multithreading);

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
    
    if (db.metadata("data.timerange_str") != null) {
      ArrayList<String> metadata = new ArrayList<String>();
      String[] timeranges = db.metadata("data.timerange_str").split(",");
      metadata.add(timeranges[0]);
      metadata.add(timeranges[1]);
      metadata.add(db.metadata("data.bbox_double"));
      return metadata;
    }

    return null;
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
}
