package demo.inventory.api.messaging;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import demo.inventory.api.message.InvalidOrderNotice;
import demo.inventory.api.message.InventoryUpdatedNotice;
import demo.inventory.api.message.OrderCompletedNotice;
import demo.inventory.api.model.InventoryItemModel;
import demo.inventory.service.IInventoryService;

/**
 * This is a Kafka 'orders' notification listener. An order notification includes an inventory
 * item ID and a count of items to remove from inventory. In this example, removing from
 * inventory is implemented by decrementing the persisted count of items in inventory.
 */
@Service
@KafkaListener(topics = {"${events.api.orders.topic}"})
public class OrderCompletionListener {
    private Logger logger = LoggerFactory.getLogger(OrderCompletionListener.class);

    @Autowired
    protected ApplicationEventPublisher eventPublisher;


    private final IInventoryService inventoryService;

    public OrderCompletionListener(IInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaHandler
    @SendTo("${events.api.inventory.topic}")
    public Object handleOrderCompleted(OrderCompletedNotice orderNotice) {
        logger.info("Received : " + orderNotice);

        long itemId = orderNotice.getItemId();
        Optional<InventoryItemModel> optional = inventoryService.getInventoryItem(itemId);
        if (optional.isPresent()) {
          logger.info("Found inventory item: " + itemId);

          InventoryItemModel inventoryItem = optional.get();
          inventoryItem.setStock(inventoryItem.getStock() - orderNotice.getCount());
          inventoryService.updateInventoryItem(inventoryItem);
          int currentStockUnits = inventoryItem.getStock();
          
          logger.info("Updated inventory for item: " + itemId + ", new stock: " + currentStockUnits);

          // Internal notification
          getEventPublisher().publishEvent(new InventoryUpdatedEvent(this.getClass(), itemId, currentStockUnits));
          
          return new InventoryUpdatedNotice(itemId, currentStockUnits);
        }

        logger.warn("Received OrderCompletedNotice for item that does not exist! [item={}]", itemId);

        // Internal notification
        getEventPublisher().publishEvent(new InvalidInventoryItemEvent(this.getClass(), itemId));
        
        return new InvalidOrderNotice(itemId);
    }

    @KafkaHandler(isDefault = true)
    public void unknown(Object object) {
        logger.info("Received unknown : " + object);
    }

    private ApplicationEventPublisher getEventPublisher() {
      return eventPublisher;
    }
}
