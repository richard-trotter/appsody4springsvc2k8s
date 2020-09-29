package demo.inventory.api.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import demo.inventory.api.model.InventoryItemModel;
import demo.inventory.service.IInventoryService;

/**
 * REST Controller providing endpoints for access to the Inventory of items.
 */
@RestController("inventoryController")
@RequestMapping(value = "/inventory")
public class InventoryController {

    private final static Logger logger = LoggerFactory.getLogger(InventoryController.class);
    
    private static final String resourcePath = "/item";

    // Suitable for the configured test dataset size
    protected static final int DEFAULT_PAGE_SIZE = 6;
    
    @Value(value = "${events.api.orders.topic}")
    private String topicName;

    private IInventoryService inventoryService;


    // constructor
    public InventoryController(IInventoryService inventoryService) {
      this.inventoryService = inventoryService;
    }
    

    /**
     * @return a page of items in inventory
     */
    @GetMapping(path=resourcePath, params={"page","size"})
    @ResponseBody
    public Page<InventoryItemModel> getInventory(@RequestParam Integer page, @RequestParam Integer size, HttpServletResponse response) {
      
      Page<InventoryItemModel> itemsPage = inventoryService
          .getInventory(PageRequest.of(page, size));
      
      if( itemsPage.hasNext() ) { 

        Pageable nextPage = itemsPage.nextPageable();
        
        Link nextLink = linkTo(methodOn(InventoryController.class)
            .getInventory(nextPage.getPageNumber(), nextPage.getPageSize(), response))
              .withRel(IanaLinkRelations.NEXT_VALUE);
        
        response.addHeader(HttpHeaders.LINK, nextLink.toString());
      }

      return itemsPage; 
    }

    
    /**
     * @return all items in inventory
     */
    @GetMapping(resourcePath)
    @ResponseBody
    public Page<InventoryItemModel> getInventory() {

      return inventoryService
          .getInventory(PageRequest.of(0, DEFAULT_PAGE_SIZE));
    }

    
    /**
     * @return an indicated item in inventory
     */
    @GetMapping(resourcePath+"/{itemId}")
    @ResponseBody
    public InventoryItemModel getInventoryItem(@PathVariable(value = "itemId") long itemId) {
      
      Optional<InventoryItemModel> o = inventoryService.getInventoryItem(itemId);
      if( ! o.isPresent() )
        throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "No inventory item with id "+itemId);
      
      return o.get();
    }

    
    /**
     * Create a new inventory item
     */
    @PostMapping(resourcePath)
    @ResponseStatus(value = HttpStatus.CREATED)
    public void createInventoryItem(@Valid @RequestBody InventoryItemModel item, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

      InventoryItemModel itemWithId = inventoryService.createInventoryItem(item);
      
      Link nextLink = linkTo(methodOn(InventoryController.class)
          .getInventoryItem(itemWithId.getId()))
            .withRel(IanaLinkRelations.SELF_VALUE);
      
      httpResponse.setHeader(HttpHeaders.LINK, nextLink.toString());

      String locationValue = httpRequest.getRequestURI() + "/" + itemWithId.getId(); 
      httpResponse.setHeader(HttpHeaders.LOCATION, locationValue);

      return;
    }

    
    @DeleteMapping(resourcePath+"/{itemId}")
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteInventoryItem(@PathVariable(value = "itemId") long itemId) {
      
      inventoryService.deleteInventoryItem(itemId);
      
      logger.info("Deleted item with id: "+itemId);
    }
}