package demo.inventory.api.kafka;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.Semaphore;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import demo.inventory.api.message.OrderCompletedNotice;
import demo.inventory.api.messaging.InvalidInventoryItemEvent;
import demo.inventory.api.messaging.InventoryUpdatedEvent;

@ActiveProfiles(profiles = "test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE, properties = "opentracing.spring.web.enabled=false")
//@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@EmbeddedKafka(topics={"${events.api.orders.topic}","${events.api.inventory.topic}"}, partitions=1)
@TestMethodOrder(OrderAnnotation.class)
public class OrderCompletionListenerTest {
  private static final Logger log = LoggerFactory.getLogger(OrderCompletionListenerTest.class);

  @Autowired
  private KafkaOperations<String, OrderCompletedNotice> kafkaOperations;

  @Autowired
  private EmbeddedKafkaBroker kafkaBroker;

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value(value = "${events.api.orders.topic}")
  private String ordersTopicName;

  @Value(value = "${events.api.inventory.topic}")
  private String inventoryTopicName;

  @Autowired
  AbstractApplicationContext context;
  
  static private Boolean isSubscribed = false;
  static private Semaphore sutCompleteSignal = new Semaphore(0); 
  
  /*
   * These listeners will be notified when the KafkaListener under test posts a ApplicationEvent.
   */
  @BeforeEach
  public void setupApplicationEventListeners() {  

    synchronized (isSubscribed) {

      if( isSubscribed ) 
        return;
        
      context.addApplicationListener(new ApplicationListener<InventoryUpdatedEvent>() {
        @Override
        public void onApplicationEvent(InventoryUpdatedEvent event) {
          log.info("Received InventoryUpdated ApplicationEvent");          
          signalSUTcomplete();
        }});
      
      context.addApplicationListener(new ApplicationListener<InvalidInventoryItemEvent>() {
        @Override
        public void onApplicationEvent(InvalidInventoryItemEvent event) {
          log.info("Received InvalidInventoryItem ApplicationEvent");          
          signalSUTcomplete();
        }});
      
      isSubscribed = true;
    }
  }
  
  @Test
  @Order(1)
  public void whenContextIsBootstrapped_thenOkReady() {

    assertTrue(kafkaOperations != null, "Missing KafkaOperations bean");
    assertTrue(kafkaBroker != null, "Missing KafkaBroker bean");

    log.info("[CONFIG] spring.kafka.bootstrap.servers = " + bootstrapServers);
    log.info("[CONFIG] events.api.orders.topic = " + ordersTopicName);
  }

  
  // Testcase: order placed for 10 units of some item in inventory
  @Test
  @Order(2)
  public void whenOrderCompletedSent_thenFindInventoryUpdatedNotice() {
  
    log.info("Posting an order notification");
  
    // Send OrderCompletedNotice
    OrderCompletedNotice message = new OrderCompletedNotice(3, 10);
    kafkaOperations.send(ordersTopicName, message).addCallback(
        this::onSendSuccess,
        ex -> fail("Unable to send OrderCompleted notification due to : " + ex.getMessage()));
    
    waitOnSUTcomplete();
    
    return;
  }

  
  // Testcase: order placed for 10 units of some item not in inventory
  @Test
  @Order(3)
  public void whenInvalidOrderCompletedSent_thenFindInvalidOrderNotice() {

    log.info("Posting an order notification");

    // Send invalid OrderCompletedNotice
    OrderCompletedNotice message = new OrderCompletedNotice(1234, 10);
    kafkaOperations.send(ordersTopicName, message).addCallback(
        this::onSendSuccess,
        ex -> fail("Unable to send OrderCompleted notification due to : " + ex.getMessage()));
    
    waitOnSUTcomplete();
    
    return;
  }

  
  private void onSendSuccess(SendResult<String, OrderCompletedNotice> result) {
    
    ProducerRecord<String, OrderCompletedNotice> producerRecord = result.getProducerRecord();
    // RecordMetadata recordMetadata = result.getRecordMetadata();
    
    log.info("Delivered OrderCompleted notification on topic [{}]: {}", 
        producerRecord.topic(),
        producerRecord.value().toString());
  }

  
  private void waitOnSUTcomplete() {    
    log.info("Begin wait on OrderCompletion Listener complete");
    //log.info("Available permits: "+sutCompleteSignal.availablePermits());
    try {
      sutCompleteSignal.acquire();
    }
    catch (InterruptedException e) {
      fail(e.toString());
    }
    log.info("End wait on OrderCompletion Listener complete");
  }
  
  private void signalSUTcomplete() {
    sutCompleteSignal.release();
  }

}
