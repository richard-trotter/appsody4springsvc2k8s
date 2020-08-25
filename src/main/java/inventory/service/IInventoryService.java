package inventory.service;

import java.util.Optional;

import inventory.api.model.InventoryItemModel;

public interface IInventoryService {

  /**
   * @return all items in inventory
   */
    Optional<InventoryItemModel> getInventoryItem(long id);

    /**
     * @return an indicated item in inventory
     */
    Iterable<InventoryItemModel> getInventory();

    /**
     * Update a persisted item
     * 
     * @param itemModel
     */
    public void updateInventoryItem(InventoryItemModel itemModel);
}