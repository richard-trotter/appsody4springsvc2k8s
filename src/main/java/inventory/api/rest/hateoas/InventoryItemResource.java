package inventory.api.rest.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.RepresentationModel;

import inventory.api.model.InventoryItemModel;
import inventory.api.rest.InventoryController;

public class InventoryItemResource extends RepresentationModel<InventoryItemResource> {

  InventoryItemModel item;

  public InventoryItemResource(InventoryItemModel item) {
    
    this.item = item;
    
    this.add(
        linkTo(methodOn(InventoryController.class).getInventory())
        .withRel(IanaLinkRelations.COLLECTION_VALUE));

    this.add(
        linkTo(methodOn(InventoryController.class).getInventoryItem(item.getId()))
        .withRel(IanaLinkRelations.SELF_VALUE));

  }

  public InventoryItemModel getItem() {
    return item;
  }

  public void setItem(InventoryItemModel item) {
    this.item = item;
  }
}