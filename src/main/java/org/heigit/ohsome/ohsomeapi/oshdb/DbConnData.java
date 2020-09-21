package org.heigit.ohsome.ohsomeapi.oshdb;

import com.zaxxer.hikari.HikariConfig;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBDatabase;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBJdbc;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;

/** Holds the database connection objects. */
public class DbConnData {

  private static OSHDBDatabase db = null;
  private static OSHDBJdbc keytables = null;
  private static TagTranslator tagTranslator = null;
  private static RemoteTagTranslator mapTagTranslator = null;
  private static HikariConfig keytablesDbPoolConfig = null;

  private DbConnData() {
    throw new IllegalStateException("Utility class");
  }

  public static OSHDBDatabase getDb() {
    return db;
  }

  public static void setDb(OSHDBDatabase db) {
    DbConnData.db = db;
  }

  public static OSHDBJdbc getKeytables() {
    return keytables;
  }

  public static void setKeytables(OSHDBJdbc keytables) {
    DbConnData.keytables = keytables;
  }

  public static TagTranslator getTagTranslator() {
    return tagTranslator;
  }

  public static void setTagTranslator1(TagTranslator tagTranslator) {
    DbConnData.tagTranslator = tagTranslator;
  }

  public static RemoteTagTranslator getMapTagTranslator() {
    return mapTagTranslator;
  }

  public static void setMapTagTranslator(RemoteTagTranslator mapTagTranslator) {
    DbConnData.mapTagTranslator = mapTagTranslator;
  }

  public static HikariConfig getKeytablesDbPoolConfig() {
    return keytablesDbPoolConfig;
  }

  public static void setKeytablesDbPoolConfig(HikariConfig keytablesDbPoolConfig) {
    DbConnData.keytablesDbPoolConfig = keytablesDbPoolConfig;
  }
}
