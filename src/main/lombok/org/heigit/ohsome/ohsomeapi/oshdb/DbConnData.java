package org.heigit.ohsome.ohsomeapi.oshdb;

import com.zaxxer.hikari.HikariConfig;
import org.heigit.ohsome.oshdb.api.db.OSHDBDatabase;
import org.heigit.ohsome.oshdb.api.db.OSHDBJdbc;
import org.heigit.ohsome.oshdb.util.tagtranslator.TagTranslator;

/** Holds the database connection objects. */
public class DbConnData {

  public static OSHDBDatabase db = null;
  public static OSHDBJdbc keytables = null;
  public static TagTranslator tagTranslator = null;
  public static RemoteTagTranslator mapTagTranslator = null;
  public static HikariConfig keytablesDbPoolConfig = null;

  private DbConnData() {
    throw new IllegalStateException("Utility class");
  }
}
