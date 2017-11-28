package org.heigit.bigspatialdata.ohsome.springBootWebAPI;

import org.heigit.bigspatialdata.ohsome.springBootWebAPI.eventHolder.EventHolderBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Main class, which is used to run this spring application.
 * @author kowatsch
 *
 */
@SpringBootApplication
public class Application {
	private static EventHolderBean bean;

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
        bean = ctx.getBean(EventHolderBean.class);
    }
    
    // 'straight-forward', maybe not so beautiful way to establish a db-conn on startup
    public static EventHolderBean getEventHolderBean() {
    	return bean;
    }
}