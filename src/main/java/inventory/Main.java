package inventory;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

// TODO: Add a Configuration class that can be referenced from testcases

@SpringBootApplication
public class Main {

  static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {

    logger.info("Inventory microservice is starting now...");
    logger.info("main("+String.join(",", args)+")");

    SpringApplication.run(Main.class, args);

  }

  @EventListener
  public void handleApplicationEvent(ApplicationReadyEvent evt) {

    String pfx = "[ApplicationReady] ";
    
    Environment env = evt.getApplicationContext().getEnvironment();
    
    logger.info("[CONFIG] active Spring profiles: "+Arrays.asList(env.getActiveProfiles()));
    
    String[] plist = {
        "spring.datasource.url",
        "spring.kafka.bootstrap-servers",
        "spring.cloud.kubernetes.enabled",
    };

    for( String pname : plist ) 
      try {
        logger.info("[CONFIG] "+pfx+pname+": "+env.getProperty(pname));
      }
      catch( IllegalArgumentException ex ) {
        logger.warn(ex.toString());
      }

    logger.info("Inventory microservice is ready for business!");
  }

}
