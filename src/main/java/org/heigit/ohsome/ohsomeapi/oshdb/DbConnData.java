package org.heigit.ohsome.ohsomeapi.oshdb;

import java.io.IOException;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBDatabase;
import org.heigit.bigspatialdata.oshdb.api.db.OSHDBJdbc;
import org.heigit.bigspatialdata.oshdb.util.tagtranslator.TagTranslator;
import org.heigit.ohsome.ohsomeapi.exception.ConfigurationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/** Holds the database connection objects. */
@Component
@RequestScope
public class DbConnData {

  private final OSHDBDatabase oshdb;
  private final DataSource keytablesPool;
  private final TagTranslator tagTranslator;
  private final RemoteTagTranslator mapTagTranslator;
  private final OSHDBJdbc keytables;
  private final ExtractMetadata extractMetadata;
  
  @Autowired
  public DbConnData(OSHDBDatabase oshdb, DataSource keytablesPool, TagTranslator tagTranslator,
      RemoteTagTranslator mapTagTranslator) throws SQLException, IOException {
    this.oshdb = oshdb;
    this.keytablesPool = keytablesPool;
    this.tagTranslator = tagTranslator;
    this.mapTagTranslator = mapTagTranslator;
    this.keytables = keytablesInstance(oshdb, keytablesPool);
    this.extractMetadata = new ExtractMetadata(keytables, tagTranslator);
  }

  private OSHDBJdbc keytablesInstance(OSHDBDatabase oshdb, DataSource keytablesPool)
      throws SQLException {
    if (keytablesPool != null) {
      return new OSHDBJdbc(keytablesPool.getConnection());
    }
    if (oshdb instanceof OSHDBJdbc) {
      return (OSHDBJdbc) oshdb;
    }
    throw new ConfigurationException();
  }

  public OSHDBDatabase getOshdb() {
    return oshdb;
  }

  public DataSource getKeytablesPool() {
    return keytablesPool;
  }

  public TagTranslator getTagTranslator() {
    return tagTranslator;
  }

  public RemoteTagTranslator getMapTagTranslator() {
    return mapTagTranslator;
  }

  public OSHDBJdbc getKeytables() {
    return keytables;
  }

  public ExtractMetadata getExtractMetadata() {
    return extractMetadata;
  }
  
}
