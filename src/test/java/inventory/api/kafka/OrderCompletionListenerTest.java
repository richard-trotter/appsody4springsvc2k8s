package inventory.api.kafka;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import inventory.api.kafka.messages.InvalidOrderNotice;
import inventory.api.kafka.messages.InventoryUpdatedNotice;
import inventory.api.kafka.messages.OrderCompletedNotice;

@ActiveProfiles(profiles = "test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE, properties = "opentracing.spring.web.enabled=false")
@EmbeddedKafka(topics={"${events.api.orders.topic}"}, partitions=1)
@TestMethodOrder(OrderAnnotation.class)
public class OrderCompletionListenerTest {
  private static final Logger log = LoggerFactory.getLogger(OrderCompletionListenerTest.class);

  @Autowired
  private KafkaOperations<String, OrderCompletedNotice> kafkaOperations;

  @Autowired
  private EmbeddedKafkaBroker kafkaBroker;

  @Value("${spring.embedded.kafka.brokers}")
  private String brokerAddresses;

  @Value(value = "${events.api.orders.topic}")
  String topicName;

  private Properties consumerProperties = new Properties();

  private Properties getKafkaConsumerConfig() {
    if( consumerProperties.isEmpty() ) {
      consumerProperties.put("bootstrap.servers", brokerAddresses);
      consumerProperties.put("group.id", "inventory-service-test");
      consumerProperties.put("key.deserializer", "org.springframework.kafka.support.serializer.JsonDeserializer");
      consumerProperties.put("value.deserializer", "org.springframework.kafka.support.serializer.JsonDeserializer");
      consumerProperties.put(JsonDeserializer.TRUSTED_PACKAGES, "inventory.api.kafka.messages");
    }
    return consumerProperties;
  }
  
  @Test
  @Order(1)
  public final void whenContextIsBootstrapped_thenOkReady() {

    assertTrue(kafkaOperations != null, "Missing KafkaOperations bean");
    assertTrue(kafkaBroker != null, "Missing KafkaBroker bean");

    log.info("[CONFIG] spring.embedded.kafka.brokers = " + brokerAddresses);
    log.info("[CONFIG] events.api.orders.topic = " + topicName);
  }

  // Testcase: order placed for 10 units of some item in inventory
  @Test
  @Order(2)
  void whenOrderCompletedSent_thenFindInventoryUpdatedNotice() {
  
    log.info("Posting an order notification");
  
    // Send OrderCompletedNotice
    OrderCompletedNotice message = new OrderCompletedNotice(3, 10);
    kafkaOperations.send(topicName, message).addCallback(
        this::onSendSuccess,
        ex -> fail("Unable to send OrderCompleted notification due to : " + ex.getMessage()));
    
    // Verify listener posted invalid notice
    try (Consumer<String, InventoryUpdatedNotice> consumer = new KafkaConsumer<>(getKafkaConsumerConfig())) {
      
      consumer.subscribe(Collections.singletonList(topicName)); 
      
      ConsumerRecords<String, InventoryUpdatedNotice> records = consumer.poll(Duration.ofSeconds(1));
      log.info("Found {} consumer records on first poll", records.count());
      for (ConsumerRecord<String, InventoryUpdatedNotice> record : records)
      {
          log.info("topic = {}, key = {}, value = {}\n",
              record.topic(),
              record.key(), 
              record.value());
          return;
      }
      fail("Did not detect InventoryUpdatedNotice for inventory item");
    }
  
    return;
  }

  // Testcase: order placed for 10 units of some item not in inventory
  @Test
  @Order(3)
  void whenInvalidOrderCompletedSent_thenFindInvalidOrderNotice() {

    log.info("Posting an order notification");

    // Send invalid OrderCompletedNotice
    OrderCompletedNotice message = new OrderCompletedNotice(1234, 10);
    kafkaOperations.send(topicName, message).addCallback(
        this::onSendSuccess,
        ex -> fail("Unable to send OrderCompleted notification due to : " + ex.getMessage()));
    
    // Verify listener posted invalid notice
    try (Consumer<String, InvalidOrderNotice> consumer = new KafkaConsumer<>(getKafkaConsumerConfig())) {
      
      consumer.subscribe(Collections.singletonList(topicName)); 
      
      ConsumerRecords<String, InvalidOrderNotice> records = consumer.poll(Duration.ofSeconds(1));
      log.info("Found {} consumer records on first poll", records.count());
      for (ConsumerRecord<String, InvalidOrderNotice> record : records)
      {
          log.info("topic = {}, key = {}, value = {}\n",
              record.topic(),
              record.key(), 
              record.value());
          return;
      }
      fail("Did not detect InvalidOrderNotice for invalid inventory item id");
    }

    return;
  }

  private void onSendSuccess(SendResult<String, OrderCompletedNotice> result) {
    ProducerRecord<String, OrderCompletedNotice> producerRecord = result.getProducerRecord();
    // RecordMetadata recordMetadata = result.getRecordMetadata();
    log.info("Delivered OrderCompleted notification on topic [{}]: ", 
        producerRecord.topic(),
        producerRecord.value().toString());
  }

}
