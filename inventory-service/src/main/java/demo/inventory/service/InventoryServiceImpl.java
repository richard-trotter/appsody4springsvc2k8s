package demo.inventory.service;

import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import demo.inventory.api.model.InventoryItemModel;
import demo.inventory.persistence.InventoryItem;
import demo.inventory.persistence.InventoryRepo;


/**
 * Inventory service
 * 
 * Provide for separation of concerns between the controller and persistence
 * layers.
 */
@Service
public class InventoryServiceImpl implements IInventoryService {

  @Autowired
  private InventoryRepo itemsRepo;

  /**
   * @return all items in inventory
   */
  public Page<InventoryItemModel> getInventory(PageRequest pageRequest) {
    ModelMapper modelMapper = new ModelMapper();
    return itemsRepo.findAll(pageRequest)
        .map(entity -> modelMapper.map(entity, InventoryItemModel.class));
  }
  
  
  /**
   * @return an indicated item in inventory
   */
  public Optional<InventoryItemModel> getInventoryItem(long itemId) {
    
    Optional<InventoryItem> entity = itemsRepo.findById(itemId);
    if( !entity.isPresent() )
      return Optional.empty();
    
    ModelMapper modelMapper = new ModelMapper();
    InventoryItemModel model = modelMapper.map(entity.get(), InventoryItemModel.class); 
    return Optional.of(model);
  }
  

  /**
   * Update a persisted item
   * 
   * @param itemModel
   */
  public void updateInventoryItem(InventoryItemModel itemModel) {
    ModelMapper modelMapper = new ModelMapper();    
    InventoryItem entity = modelMapper.map(itemModel, InventoryItem.class); 
    itemsRepo.save(entity);
  }


  /**
   * Create a new persisted item.
   * 
   * @param itemModel
   * @return a copy of the item with its new generated id
   */
  @Override
  public InventoryItemModel createInventoryItem(InventoryItemModel itemModel) {
    ModelMapper modelMapper = new ModelMapper();    
    InventoryItem entity = modelMapper.map(itemModel, InventoryItem.class); 
    InventoryItem newPersistedEntity = itemsRepo.save(entity);
    InventoryItemModel itemModelWithId = modelMapper.map(newPersistedEntity, InventoryItemModel.class);
    return itemModelWithId;
  }


  /**
   * Delete a persisted item.
   * 
   * @param itemId
   */
  @Override
  public void deleteInventoryItem(long itemId) {
    itemsRepo.deleteById(itemId);
    return;
  }
}