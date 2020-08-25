package inventory.api.kafka;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import inventory.api.kafka.messages.InvalidOrderNotice;
import inventory.api.kafka.messages.InventoryUpdatedNotice;
import inventory.api.kafka.messages.OrderCompletedNotice;
import inventory.api.model.InventoryItemModel;
import inventory.service.IInventoryService;

/**
 * This is a Kafka 'orders' notification listener. An order notification includes an inventory
 * item ID and a count of items to remove from inventory. In this example, removing from
 * inventory is implemented by decrementing the persisted count of items in inventory.
 */
// TODO: migrate to plain Kafka
@Service
@KafkaListener(topics = {"${events.api.orders.topic}"}, id = "inventory-service")
public class OrderCompletionListener {
    private Logger logger = LoggerFactory.getLogger(OrderCompletionListener.class);

    private final IInventoryService inventoryService;

    public OrderCompletionListener(IInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaHandler
    @SendTo("${events.api.orders.topic}")
    public Object handleOrderCompleted(OrderCompletedNotice orderNotice) {
        logger.info("Received : " + orderNotice);

        long itemId = orderNotice.getItemId();
        Optional<InventoryItemModel> optional = inventoryService.getInventoryItem(itemId);
        if (optional.isPresent()) {
            InventoryItemModel inventoryItem = optional.get();
            inventoryItem.setStock(inventoryItem.getStock() - orderNotice.getCount());
            inventoryService.updateInventoryItem(inventoryItem);
            int currentStockUnits = inventoryItem.getStock();
            String updateMessage = "Updated inventory for item: " + itemId + ", new stock: " + currentStockUnits;
            logger.info(updateMessage);
            return new InventoryUpdatedNotice(itemId, currentStockUnits);
        }

        logger.warn("Received OrderCompletedNotice for item that does not exist! [item={}]", itemId);
        return new InvalidOrderNotice(itemId);
    }

    @KafkaHandler
    public void handleInvalidOrder(InvalidOrderNotice invalidOrder) {
      // no action required by this OrderService
    }

    @KafkaHandler(isDefault = true)
    public void unknown(Object object) {
        logger.info("Received unknown : " + object);
    }
}
