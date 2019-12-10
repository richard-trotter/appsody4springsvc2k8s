package inventory.api.events.messages;

/**
 * This class models a request to order a specified quantity of a specified
 * item from inventory.
 */
public class InventoryOrderRequest {

    long itemId;

    int count;

    public InventoryOrderRequest() {
    }

    public InventoryOrderRequest(long itemId, int count) {
        this.itemId = itemId;
        this.count = count;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("InventoryOrderRequest {")
                .append(" itemId=").append(Long.toString(itemId))
                .append(", count=").append(Integer.toString(count)).append(" }");
        return buf.toString();
    }

}
