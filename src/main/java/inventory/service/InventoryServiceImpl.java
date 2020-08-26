package inventory.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import inventory.api.model.InventoryItemModel;
import inventory.jpa.InventoryItem;
import inventory.jpa.InventoryRepo;

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
  public Iterable<InventoryItemModel> getInventory() {
    ModelMapper modelMapper = new ModelMapper();
    List<InventoryItemModel> acc = new ArrayList<>();
    itemsRepo.findAll().iterator()
        .forEachRemaining(entity -> acc.add(modelMapper.map(entity, InventoryItemModel.class)));
    return acc;
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
}