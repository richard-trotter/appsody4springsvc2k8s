package demo.inventory.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import demo.inventory.api.model.InventoryItemModel;

public interface IInventoryService {

  /**
   * @return all items in inventory
   */
    Optional<InventoryItemModel> getInventoryItem(long id);

    /**
     * @return a page of items in inventory
     */
    Page<InventoryItemModel> getInventory(PageRequest pageRequest);

    /**
     * Update a persisted item
     * 
     * @param itemModel
     */
    public void updateInventoryItem(InventoryItemModel itemModel);

    /**
     * Create a new persisted item
     * 
     * @param itemModel
     */
    public InventoryItemModel createInventoryItem(InventoryItemModel itemModel);
    
    /**
     * Delete a persisted item
     * 
     * @param itemModel
     */
    public void deleteInventoryItem(long id);
}