package inventory.api.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import inventory.api.model.InventoryItemModel;
import inventory.api.rest.hateoas.InventoryItemResource;
import inventory.service.IInventoryService;

/**
 * REST Controller providing endpoints for access to the Inventory of items.
 */
// TODO: add standalone client for live api test
// TODO: verify conformance to REST status code conventions
@RestController("inventoryController")
@RequestMapping(value = "/inventory", method = RequestMethod.GET)
public class InventoryController {

    private final static Logger logger = LoggerFactory.getLogger(InventoryController.class);
    
    // Suitable for the configured test dataset size
    protected static final int DEFAULT_PAGE_SIZE = 6;

    @Value(value = "${events.api.orders.topic}")
    private String topicName;

    private IInventoryService inventoryService;

    public InventoryController(IInventoryService inventoryService) {
      this.inventoryService = inventoryService;
    }
    
    /**
     * @return a page of items in inventory
     */
    @GetMapping(path="/items", params={"page","size"})
    @ResponseBody
    public PagedModel<InventoryItemResource> getInventory(@RequestParam Integer page, @RequestParam Integer size) {
      
      Page<InventoryItemModel> itemsPage = inventoryService
          .getInventory(PageRequest.of(page, size));
      
      return toPagedModel(itemsPage);
    }

    /**
     * @return the first page of items in inventory
     */
    @GetMapping("/items")
    @ResponseBody
    public PagedModel<InventoryItemResource> getInventory() {

      Page<InventoryItemModel> itemsPage = inventoryService
          .getInventory(PageRequest.of(0, DEFAULT_PAGE_SIZE));

      return toPagedModel(itemsPage);
    }

    /**
     * @return an indicated item in inventory
     */
    @GetMapping("/items/{itemId}")
    @ResponseBody
    public InventoryItemResource getInventoryItem(@PathVariable(value = "itemId") long itemId) {

      InventoryItemResource response;
      
      Optional<InventoryItemModel> model = inventoryService.getInventoryItem(itemId);
      if( model.isPresent() ) {
        response = new InventoryItemResource(model.get());
        return response;
      }

      throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
    }
    
    
    private PagedModel<InventoryItemResource> toPagedModel(Page<InventoryItemModel> itemsPage) {
      
      Page<InventoryItemResource> resourcesPage = itemsPage
          .map(model -> new InventoryItemResource(model));
      
      PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(
          resourcesPage.getSize(),
          resourcesPage.getNumber(), 
          resourcesPage.getTotalElements(),
          resourcesPage.getTotalPages());

      // Note page numbers begin with '0'
      if( (1 + resourcesPage.getNumber()) < resourcesPage.getTotalPages() ) { 
        Integer nextPage = 1 + resourcesPage.getNumber();
        Link nextLink = linkTo(methodOn(InventoryController.class).getInventory(nextPage, resourcesPage.getSize()))
            .withRel(IanaLinkRelations.NEXT_VALUE);
        return PagedModel.of(resourcesPage.getContent(), metadata, nextLink);
      }
      else
        return PagedModel.of(resourcesPage.getContent(), metadata);

    }
}