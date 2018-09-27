package org.heigit.bigspatialdata.ohsome.ohsomeapi.oshdb;

import org.heigit.bigspatialdata.oshdb.api.db.OSHDBDatabase;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBJdbc;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;

/** Holds the database connection objects. */
public class DbConnData {

  public static OSHDBDatabase db = null;
  public static OSHDBJdbc keytables = null;
  public static TagTranslator tagTranslator = null;
}
