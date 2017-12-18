package org.heigit.bigspatialdata.ohsome.springBootWebAPI.listener;

import org.heigit.bigspatialdata.ohsome.springBootWebAPI.eventHolder.EventHolderBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * This listener is activated on server startup and calls an eventholder bean to establish a
 * database connection.
 *
 */
@Component
public class ContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {

  private EventHolderBean eventHolderBean;

  @Autowired
  public void setEventHolderBean(EventHolderBean eventHolderBean) {
    this.eventHolderBean = eventHolderBean;
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
    System.out.println("Context Event Received");
    eventHolderBean.dbConn("C:/Users/kowatsch/Desktop/HeiGIT/oshdb/data/baden-wuerttemberg.oshdb",
        "C:/Users/kowatsch/Desktop/HeiGIT/oshdb/data/keytables", true);
  }
}
