package inventory.api.kafka;

import org.springframework.context.ApplicationEvent;

public class InventoryUpdatedEvent<T> extends ApplicationEvent {

  private String message;
  private long itemId;
  private int currentStockUnits;

  public InventoryUpdatedEvent(T source, String message) {
        super(source);
        this.message = message;
    }

  public InventoryUpdatedEvent(Object source, String message, long itemId, int currentStockUnits) {
    super(source);
    this.message = message;
    this.itemId = itemId;
    this.currentStockUnits = currentStockUnits;
  }

  public String getMessage() {
    return message;
  }

  public long getItemId() {
    return itemId;
  }

  public int getCurrentStockUnits() {
    return currentStockUnits;
  }

}
