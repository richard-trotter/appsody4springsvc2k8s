package demo.inventory.api.messaging;

import org.springframework.context.ApplicationEvent;

/**
 * Event that is fired when the Listener is successful in updating the Inventory.
 */
public final class InventoryUpdatedEvent extends ApplicationEvent {
  
  private static final long serialVersionUID = 1L;
  
  private long itemId;
  private long currentStockUnits;
  
  public InventoryUpdatedEvent(Object source, long itemId, long currentStockUnits) {
    super(source);
    this.itemId = itemId;
    this.currentStockUnits = currentStockUnits;
  }
  
  public long getItemId() {
    return itemId;
  }
  public long getCurrentStockUnits() {
    return currentStockUnits;
  }

}
