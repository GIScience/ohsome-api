package org.heigit.ohsome.ohsomeapi.config;

import java.nio.file.Files;
import java.nio.file.Path;
import org.heigit.ohsome.ohsomeapi.exception.ConfigurationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/*

  --server.port=8080
  
  --database.prefix=global-68324
  --database.timeout=1000
  --database.ignite=ohsome-heigit.xml
  
  --database.keytables.datasourceClassName="org.postgresql.ds.PGSimpleDataSource"
  --database.keytables.databaseName="keytables-global-68324"
  --database.keytables.portNumber=5432
  --database.keytables.serverName=127.0.0.1
  or
  --database.keytables.jdbc=jdbc:postgresql://127.0.0.1:5432/keytables-global-68324
  
  --database.keytables.user=readonly
  --database.keytables.password=ohsome
  --database.keytables.poolsize=10
  
  --cluster.servernodes.count=4
  --cluster.dataextraction.threadcount=2
  
  #database.jdbc.multithreading = true
  #database.jdbc.timeout;
  
  #database.h2.url = "jdbc:h2:troilo.oshdb"
  #database.h2.file= "troilo2.oshdb"
  #database.h2.caching = true
  #database.h2.multithreading = false

 */

@Component
@ConfigurationProperties("database")
public class OSHDBConfig implements InitializingBean {
  public static final int DEFAULT_TIMEOUT_IN_MILLISECONDS = 100000;
  public static final int DEFAULT_NUMBER_OF_CLUSTER_NODES = 0;
  public static final int DEFAULT_NUMBER_OF_DATA_EXTRACTION_THREADS = 40;

  private String prefix = "";
  private long timeout = DEFAULT_TIMEOUT_IN_MILLISECONDS;

  private Path ignite;
  private JdbcCommon keytables;
  private H2 h2;
  private Jdbc jdbc;

  public String getPrefix() {
    return prefix;
  }
  
  public long getTimeout() {
    return timeout;
  }
  
  public double getTimeoutInSeconds() {
    return timeout / 1000.0;
  }
      
  public boolean hasIgnite() {
    return ignite != null;
  }
  
  public Path getIgnitePath() {
    return ignite;
  }
  
  public boolean hasKeytables() {
    return keytables != null;
  }
    
  public String getKeytablesDataSourceClassName() {
    return keytables.getDataSourceClassName();
  }
  
  public String getKeytablesServerName() {
    return keytables.getServerName();
  }
  
  public int getKeytablesPortNumber() {
    return keytables.getPortNumber();
  }
  
  public String getKeytablesDatabaseName() {
    return keytables.getDatabaseName();
  }
  
  public String getKeytablesJdbcUrl() {
    return keytables.getJdbcUrl();
  }
  
  public String getKeytablesUsername() {
    return keytables.getUser();
  }
  
  public String getKeytablesPassword() {
    return keytables.getPassword();
  }
  
  public int getKeytablesPoolSize() {
    return keytables.getPoolSize();
  }
  
  
  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }


  public void setIgnite(Path ignite) {
    this.ignite = ignite;
  }

  public void setKeytables(JdbcCommon keytables) {
    this.keytables = keytables;
  }
  

  public H2 getH2() {
    return h2;
  }

  public void setH2(H2 h2) {
    this.h2 = h2;
  }

  public boolean hasH2Db() {
    return h2 != null;
  }


  public Jdbc getJdbc() {
    return jdbc;
  }

  public void setJdbc(Jdbc jdbc) {
    this.jdbc = jdbc;
  }
  
  public boolean hasJdbcDb() {
    return jdbc != null;
  }

  
  


  public static abstract class DBCommon {
    private long timeout;

    public long getTimeout() {
      return timeout;
    }

    public void setTimeout(long timeout) {
      this.timeout = timeout;
    }
  }

  public static class Ignite {
    private Path config;

    public Path getConfig() {
      return config;
    }

    public void setConfig(Path config) {
      this.config = config;
    }
  }

  public static class JdbcCommon {
    private String dataSourceClassName;
    private String databaseName;
    private int portNumber;
    private String serverName;
   
    private String jdbc;

    private String user = "";
    private String password = "";

    private int poolSize = 0;

    public int getPoolSize() {
      return poolSize;
    }

    public void setPoolSize(int poolSize) {
      this.poolSize = poolSize;
    }

    public String getDataSourceClassName() {
      return dataSourceClassName;
    }

    public void setDataSourceClassName(String dataSourceClassName) {
      this.dataSourceClassName = dataSourceClassName;
    }

    public String getDatabaseName() {
      return databaseName;
    }

    public void setDatabaseName(String databaseName) {
      this.databaseName = databaseName;
    }

    public int getPortNumber() {
      return portNumber;
    }

    public void setPortNumber(int portNumber) {
      this.portNumber = portNumber;
    }

    public String getServerName() {
      return serverName;
    }

    public void setServerName(String serverName) {
      this.serverName = serverName;
    }

    public String getJdbcUrl() {
      return jdbc;
    }

    public void setJdbc(String jdbc) {
      this.jdbc = jdbc;
    }

    public String getUser() {
      return user;
    }

    public void setUser(String user) {
      this.user = user;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }
  }

  public static class Jdbc extends JdbcCommon {

    private boolean multithreading;

    public boolean isMultithreading() {
      return multithreading;
    }

    public void setMultithreading(boolean multithreading) {
      this.multithreading = multithreading;
    }
  }

  public static class H2 extends Jdbc {
    private boolean caching;
    private String file;

    public boolean isCaching() {
      return caching;
    }

    public void setCaching(boolean caching) {
      this.caching = caching;
    }

    public String getFile() {
      return file;
    }

    public void setFile(String file) {
      this.file = file;
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
      if(hasIgnite()) {
        if(!Files.exists(ignite)) {
          //TODO generate validation text
        }
        if(!hasKeytables()) {
          //TODO generate validation text
        }
      } else if(hasH2Db()){
        //TODO 
      } else if(hasJdbcDb()){
        
      } else {        
        throw new ConfigurationException(); 
      }
  }
}
