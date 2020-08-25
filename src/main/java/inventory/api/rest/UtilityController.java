package inventory.api.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.util.concurrent.SuccessCallback;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;

import inventory.api.kafka.messages.OrderCompletedNotice;

/**
 * REST Controller providing non-api endpoints for access to utility operations.
 */
@RestController("utilityController")
@ConditionalOnBean(KafkaOperations.class)
@RequestMapping(value = "/util", method = RequestMethod.GET)
public class UtilityController {

    private final static Logger logger = LoggerFactory.getLogger(UtilityController.class);

    private KafkaOperations<String, OrderCompletedNotice> kafkaOperations;

    @Value(value = "${events.api.orders.topic}")
    String topicName;

    public UtilityController(KafkaOperations<String, OrderCompletedNotice> kafkaOperations) {
      this.kafkaOperations = kafkaOperations;
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

        final OrderCompletedNotice message = new OrderCompletedNotice(itemId, count);
        kafkaOperations.send(topicName, message).addCallback(
                (SuccessCallback) result -> logger.info("Delivered : " + message),
                ex -> logger.error("Unable to send "+message+ " due to : " + ex.getMessage()));

        return;
    }

}