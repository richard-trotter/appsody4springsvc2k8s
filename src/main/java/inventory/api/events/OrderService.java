package inventory.api.events;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import inventory.api.events.messages.InventoryOrderRequest;
import inventory.models.InventoryItem;
import inventory.models.InventoryRepo;

/**
 * This is a Kafka 'orders' notification listener. An order notification includes an inventory
 * item ID and a count of items to remove from inventory. In this example, removing from
 * inventory is implemented by decrementing the persisted count of items in inventory.
 * 
 * EventStreams service integration may be disabled for unit test.
 */
@Service
@KafkaListener(topics = {"${events.api.orders.topic}"}, id = "inventory-service")
@Profile("!unittest")
public class OrderService {
  private Logger logger = LoggerFactory.getLogger(OrderService.class);

  private InventoryRepo itemsRepo;

  @Value("${events.api.orders.topic}")
  String topicName;
          
  public OrderService(InventoryRepo itemsRepo) {
    this.itemsRepo = itemsRepo;
  }
  
  @KafkaHandler
  public void orderRequestedEvent(InventoryOrderRequest orderRequest) {
    logger.info("order requested: "+orderRequest);

    InventoryItem item = itemsRepo.findById(orderRequest.getItemId());
    if (item == null) {
      logger.warn("Received message for item that does not exist!" + orderRequest.getItemId());
      return;
    }

    item.setStock(item.getStock() - orderRequest.getCount());
    itemsRepo.save(item);
    logger.info("Updated inventory: " + orderRequest.getItemId() + " new stock: " + item.getStock());
  }

  @KafkaHandler(isDefault = true)
  public void unknown(Object object) {
    logger.info("Received unknown: " + object);
  }

  @PostConstruct
  public void logInit() {
    logger.info("Created KafkaListener for topic: \""+topicName+"\"");  
  }

}
