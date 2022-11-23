package org.heigit.ohsome.ohsomeapi.oshdb;

import javax.sql.DataSource;
import org.heigit.ohsome.oshdb.api.db.OSHDBDatabase;
import org.heigit.ohsome.oshdb.api.db.OSHDBJdbc;
import org.heigit.ohsome.oshdb.util.tagtranslator.TagTranslator;

/** Holds the database connection objects. */
public class DbConnData {

  public static OSHDBDatabase db = null;
  public static TagTranslator tagTranslator = null;
  public static DataSource dbSource = null;
  public static DataSource keytablesDbSource = null;

  private DbConnData() {
    throw new IllegalStateException("Utility class");
  }
}
