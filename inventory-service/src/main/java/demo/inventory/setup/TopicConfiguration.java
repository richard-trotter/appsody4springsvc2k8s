package demo.inventory.setup;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * The spring-kafka library will look for NewTopic beans and create subject 
 * Kafka topics if they do not already exist.
 */
@Configuration
@Profile({"dev"})
public class TopicConfiguration {

  @Value("${events.api.orders.topic}")
  String topicName;

  @Bean
  public NewTopic ordersTopic() {
    return new NewTopic(
        topicName  /* name */, 
        1  /* numPartitions */, 
        (short) 1  /* replicationFactor */);
  }
}
