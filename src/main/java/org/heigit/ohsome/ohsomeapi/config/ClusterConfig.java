package org.heigit.ohsome.ohsomeapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/*
 
  --cluster.servernodes.count=4
  --cluster.dataextraction.threadcount=2
 
*/

@Component
@ConfigurationProperties("cluster")
public class ClusterConfig {
  public static final int DEFAULT_NUMBER_OF_CLUSTER_NODES = 0;
  public static final int DEFAULT_NUMBER_OF_DATA_EXTRACTION_THREADS = 40;

  private ServerNodes serverNodes;
  private DataExtraction dataExtraction;

  public int getServerNodesCount() {
    if(serverNodes == null || serverNodes.count < 0) {
      return DEFAULT_NUMBER_OF_CLUSTER_NODES;
    }
    return serverNodes.count;
  }

  public int getDataExtractionThreadCount() {
    if(dataExtraction == null || dataExtraction.threadcount < 0) {
      return DEFAULT_NUMBER_OF_DATA_EXTRACTION_THREADS;
    }
    return dataExtraction.threadcount;
  }

  public void setServerNodes(ServerNodes serverNodes) {
    this.serverNodes = serverNodes;
  }

  public void setDataExtraction(DataExtraction dataExtraction) {
    this.dataExtraction = dataExtraction;
  }

  public static class ServerNodes {
    private int count = -1;

    public int getCount() {
      return count;
    }

    public void setCount(int count) {
      this.count = count;
    }
  }

  public static class DataExtraction {
    private int threadcount = -1;

    public int getThreadcount() {
      return threadcount;
    }

    public void setThreadcount(int threadcount) {
      this.threadcount = threadcount;
    }
  }  
}