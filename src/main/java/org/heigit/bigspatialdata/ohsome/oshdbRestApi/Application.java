package org.heigit.bigspatialdata.ohsome.oshdbRestApi;

import java.sql.SQLException;
import java.util.ArrayList;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDB_H2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main class, which is used to run this Spring boot application.
 * Establishes a connection to the database on startup using parameters provided via the console.
 *
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
    boolean multithreading = true;
    ArrayList<String> paths = new ArrayList<String>();
    for (String paramName : args.getOptionNames()){
      if (paramName.equals("database.db")) {
        paths.add(args.getOptionValues(paramName).get(0));
      } else if (paramName.equals("database.keytables")) {
        paths.add(args.getOptionValues(paramName).get(0));
      } else if (paramName.equals("database.multithreading")) {
        if (args.getOptionValues(paramName).get(0).equals("false"))
          multithreading = false;
      } else {
        throw new RuntimeException(
            "There are only three possible option-names: database.db, database.keytables and database.multithreading.");
      }
    }
    try {
      dbConnObjects = new OSHDB_H2[2];
      oshdb = (new OSHDB_H2(paths.get(0)).multithreading(multithreading));
      dbConnObjects[0] = oshdb;
      if (paths.size() == 2) {
        oshdbKeytables = new OSHDB_H2(paths.get(1));
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
