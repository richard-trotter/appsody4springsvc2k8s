package inventory.api.rest;

import inventory.api.kafka.messages.OrderCompleted;
import inventory.jpa.InventoryItem;
import inventory.jpa.InventoryRepo;
import inventory.setup.ItemsBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.util.concurrent.SuccessCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.util.Optional;

/**
 * REST Controller providing endpoints for access to the Inventory of items.
 */
@RestController("inventoryController")
@RequestMapping(value = "/inventory", method = RequestMethod.GET)
public class InventoryController {

    private final static Logger logger = LoggerFactory.getLogger(InventoryController.class);

    @Autowired
    private InventoryRepo itemsRepo;

    @Autowired
    private ItemsBuilder itemsBuilder;
    
    private boolean isDatasetReady = false;
    
    // May be disabled for unit test
    @Autowired(required = false)
    private KafkaOperations<String, OrderCompleted> kafkaOperations;

    @Value(value = "${events.api.orders.topic}")
    String topicName;

    /**
     * @return all items in inventory
     */
    @GetMapping("/item")
    @ResponseBody
    Iterable<InventoryItem> getInventory() {
        return getItemsRepo().findAll();
    }

    /**
     * @return an indicated item in inventory
     */
    @GetMapping("/item/{itemId}")
    @ResponseBody
    Optional<InventoryItem> getInventoryItem(@PathVariable(value = "itemId") long itemId) {
        return getItemsRepo().findById(itemId);
    }

    /**
     * Simulate an order for inventory items.
     */
    @PostMapping("/order")
    @ResponseStatus(code = HttpStatus.OK)
    void placeOrder(@RequestParam(name = "itemId") long itemId, @RequestParam(name = "count") int count) {

        if (kafkaOperations == null) {
            logger.error("Missing KafkaTemplate for order notification producer");
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        logger.info("Posting an order notification");

        final OrderCompleted message = new OrderCompleted(itemId, count);
        kafkaOperations.send(topicName, message).addCallback(
                (SuccessCallback) result -> logger.info("Delivered InventoryOrderRequest [" + message + "]"),
                ex -> logger.error("Unable to send InventoryOrderRequest [" + message + "] due to : " + ex.getMessage()));

        return;
    }

    private synchronized InventoryRepo getItemsRepo() {
      
      if( ! isDatasetReady ) {
        try {
          itemsBuilder.createItems();
          isDatasetReady = true;
        }
        catch (IOException ex) {
          logger.error("Items data load error: "+ex.toString());
        }        
      }
      return itemsRepo;
    }

}