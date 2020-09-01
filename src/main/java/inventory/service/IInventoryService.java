package inventory.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import inventory.api.model.InventoryItemModel;

public interface IInventoryService {

  /**
   * @return a page of items in inventory
   */
  Page<InventoryItemModel> getInventory(PageRequest pageRequest);

  /**
   * @return an indicated item in inventory
   */
  Optional<InventoryItemModel> getInventoryItem(long id);

  /**
   * Update a persisted item
   * 
   * @param itemModel
   */
  public void updateInventoryItem(InventoryItemModel itemModel);

}