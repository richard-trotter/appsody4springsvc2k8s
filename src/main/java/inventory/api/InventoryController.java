package inventory.api;

import inventory.api.events.messages.InventoryOrderRequest;
import inventory.models.InventoryItem;
import inventory.models.InventoryRepo;
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

    // May be disabled for unit test
    @Autowired(required = false)
    private KafkaOperations<String, InventoryOrderRequest> kafkaOperations;

    @Value(value = "${events.api.orders.topic}")
    String topicName;

    /**
     * @return all items in inventory
     */
    @GetMapping("/item")
    @ResponseBody
    Iterable<InventoryItem> getInventory() {
        return itemsRepo.findAll();
    }

    /**
     * @return an indicated item in inventory
     */
    @GetMapping("/item/{itemId}")
    @ResponseBody
    Optional<InventoryItem> getInventoryItem(@PathVariable(value = "itemId") long itemId) {
        return itemsRepo.findById(itemId);
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

        final InventoryOrderRequest message = new InventoryOrderRequest(itemId, count);
        kafkaOperations.send(topicName, message).addCallback(
                (SuccessCallback) result -> logger.info("Delivered InventoryOrderRequest [" + message + "]"),
                ex -> logger.error("Unable to send InventoryOrderRequest [" + message + "] due to : " + ex.getMessage()));

        return;
    }

}