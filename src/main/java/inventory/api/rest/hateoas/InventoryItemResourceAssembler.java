package inventory.api.rest.hateoas;

import org.springframework.hateoas.server.RepresentationModelAssembler;

import inventory.api.model.InventoryItemModel;

public class InventoryItemResourceAssembler implements RepresentationModelAssembler<InventoryItemModel, InventoryItemResource> {

  @Override
  public InventoryItemResource toModel(InventoryItemModel entity) {

    return new InventoryItemResource(entity);
  }
  

}
