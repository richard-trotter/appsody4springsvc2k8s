package inventory.kafka;

import inventory.kafka.messages.InvalidOrder;
import inventory.kafka.messages.InventoryOrderRequest;
import inventory.kafka.messages.InventoryUpdated;
import inventory.jpa.InventoryItem;
import inventory.jpa.InventoryRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * This is a Kafka 'orders' notification listener. An order notification includes an inventory
 * item ID and a count of items to remove from inventory. In this example, removing from
 * inventory is implemented by decrementing the persisted count of items in inventory.
 * <p>
 * EventStreams service integration may be disabled for unit test.
 */
@Service
@KafkaListener(topics = {"${events.api.orders.topic}"}, id = "inventory-service")
public class OrderService {
    private Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final InventoryRepo inventoryRepo;

    public OrderService(InventoryRepo inventoryRepo) {
        this.inventoryRepo = inventoryRepo;
    }

    @KafkaHandler
    @SendTo("${events.api.orders.topic}")
    public Object orderRequestedEvent(InventoryOrderRequest orderRequest) {
        logger.info("order requested: " + orderRequest);

        long itemId = orderRequest.getItemId();
        Optional<InventoryItem> optional = inventoryRepo.findById(itemId);
        if (optional.isPresent()) {
            InventoryItem inventoryItem = optional.get();
            inventoryItem.setStock(inventoryItem.getStock() - orderRequest.getCount());
            inventoryRepo.save(inventoryItem);
            logger.info("Updated inventory: " + itemId + " new stock: " + inventoryItem.getStock());
            return new InventoryUpdated(itemId, orderRequest.getCount());
        }

        logger.warn("Received message for item that does not exist!" + itemId);
        return new InvalidOrder(itemId);
    }

    @KafkaHandler
    public void handleInventoryUpdated(InventoryUpdated inventoryUpdated) {
        logger.info("Invalid order received item id = [{}]" + inventoryUpdated.getId());
    }

    @KafkaHandler
    public void handleInvalidOrder(InvalidOrder invalidOrder) {
        logger.info("Invalid order received item id = [{}]" + invalidOrder.getItemId());
    }

    @KafkaHandler(isDefault = true)
    public void unknown(Object object) {
        logger.info("Received unknown: " + object);
    }
}
