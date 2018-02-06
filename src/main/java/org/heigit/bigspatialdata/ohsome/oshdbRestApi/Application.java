package org.heigit.bigspatialdata.ohsome.oshdbRestApi;

import java.sql.SQLException;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDB_H2;
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
  /**
   * 0: oshdb 1: keytables
   */
  private static OSHDB_H2[] dbConnObjects;

  public static void main(String[] args) {
    if (args == null || args.length == 0)
      throw new RuntimeException(
          "You need to define at least the path to the database file (and keytables, if it is not included) as parameter via the command line.");
    SpringApplication.run(Application.class, args);
  }

  @SuppressWarnings("resource")
  @Override
  public void run(ApplicationArguments args) throws Exception {
    OSHDB_H2 oshdb;
    OSHDB_H2 oshdbKeytables;
    String dbPath = null;
    String keytablesPath = null;
    boolean multithreading = true;
    for (String paramName : args.getOptionNames()) {
      if (paramName.equals("database.db")) {
        dbPath = args.getOptionValues(paramName).get(0);
      } else if (paramName.equals("database.keytables")) {
        keytablesPath = args.getOptionValues(paramName).get(0);
      } else if (paramName.equals("database.multithreading")) {
        if (args.getOptionValues(paramName).get(0).equals("false"))
          multithreading = false;
      } else {
        throw new RuntimeException(
            "There are only three possible option-names: database.db, database.keytables and database.multithreading. "
                + "The first one is mandatory, the second one becomes mandatory if the keytables are not included in the db file "
                + "and the third one is optional.");
      }
    }
    try {
      dbConnObjects = new OSHDB_H2[2];
      oshdb = (new OSHDB_H2(dbPath).multithreading(multithreading));
      dbConnObjects[0] = oshdb;
      if (keytablesPath != null) {
        oshdbKeytables = new OSHDB_H2(keytablesPath);
        dbConnObjects[1] = oshdbKeytables;
      }
    } catch (ClassNotFoundException | SQLException e) {
      e.printStackTrace();
    }
  }

  public static OSHDB_H2[] getDbConnObjects() {
    return dbConnObjects;
  }
}
