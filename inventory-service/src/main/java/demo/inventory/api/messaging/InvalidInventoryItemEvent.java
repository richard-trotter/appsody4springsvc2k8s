package demo.inventory.api.messaging;

import org.springframework.context.ApplicationEvent;

/**
 * Event that is fired when the Listener is successful in updating the Inventory.
 */
public final class InvalidInventoryItemEvent extends ApplicationEvent {
  
  private static final long serialVersionUID = 1L;
  
  private long itemId;
  
  public InvalidInventoryItemEvent(Object source, long itemId) {
    super(source);
    this.itemId = itemId;
  }
  
  public long getItemId() {
    return itemId;
  }

}
