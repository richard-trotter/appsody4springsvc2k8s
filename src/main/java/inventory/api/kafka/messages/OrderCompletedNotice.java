package inventory.api.kafka.messages;

/**
 * This class models a request to order a specified quantity of a specified
 * item from inventory.
 */
public class OrderCompletedNotice {

    long itemId;

    int count;

    public OrderCompletedNotice() {
    }

    public OrderCompletedNotice(long itemId, int count) {
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

    @Override
    public String toString() {
      return "OrderCompletedNotice [itemId=" + itemId + ", count=" + count + "]";
    }

}
