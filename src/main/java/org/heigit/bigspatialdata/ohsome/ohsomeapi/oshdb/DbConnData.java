package org.heigit.bigspatialdata.ohsome.ohsomeapi.oshdb;

import org.heigit.bigspatialdata.oshdb.api.db.OSHDBH2;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBIgnite;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;

/** Holds the database connection objects. */
public class DbConnData {

  public static OSHDBH2 h2Db = null;
  public static OSHDBIgnite igniteDb = null;
  public static OSHDBH2 keytables = null;
  public static TagTranslator tagTranslator = null;
}
