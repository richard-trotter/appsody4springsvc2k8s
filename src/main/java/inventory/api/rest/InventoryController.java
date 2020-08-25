package inventory.api.rest;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import inventory.api.model.InventoryItemModel;
import inventory.service.IInventoryService;

/**
 * REST Controller providing endpoints for access to the Inventory of items.
 */
// TODO: add standalone client for live api test
// TODO: verify conformance to REST status code coventions
@RestController("inventoryController")
@RequestMapping(value = "/inventory", method = RequestMethod.GET)
public class InventoryController {

    private final static Logger logger = LoggerFactory.getLogger(InventoryController.class);

    @Value(value = "${events.api.orders.topic}")
    private String topicName;

    private IInventoryService inventoryService;

    public InventoryController(IInventoryService inventoryService) {
      this.inventoryService = inventoryService;
    }
    
    /**
     * @return all items in inventory
     */
    @GetMapping("/item")
    @ResponseBody
    Iterable<InventoryItemModel> getInventory() {
        return inventoryService.getInventory();
    }

    /**
     * @return an indicated item in inventory
     */
    @GetMapping("/item/{itemId}")
    @ResponseBody
    Optional<InventoryItemModel> getInventoryItem(@PathVariable(value = "itemId") long itemId) {
        return inventoryService.getInventoryItem(itemId);
    }

}