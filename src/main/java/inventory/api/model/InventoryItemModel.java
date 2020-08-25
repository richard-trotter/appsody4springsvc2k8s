package inventory.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

public class InventoryItemModel {

    private long id;

    private String name;

    private String description;

    private int price;

    private String img_alt;

    private String img;

    private int stock;

    public InventoryItemModel() {
    }

    public InventoryItemModel(long id) {
        this.id = id;
    }

    public InventoryItemModel(String name, String description, int price, String img_alt, String img, int stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.img = img;
        this.img_alt = img_alt;
        this.stock = stock;
    }

    public long getId() {
        return id;
    }

    public void setId(long value) {
        this.id = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int value) {
        this.price = value;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getImgAlt() {
        return img_alt;
    }

    public void setImgAlt(String img_alt) {
        this.img_alt = img_alt;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int value) {
        this.stock = value;
    }

    @Override
    public String toString() {
      return "InventoryItemModel [id=" + id 
          + ", name=" + name 
          + ", description=" + description 
          + ", price=" + price
          + ", stock=" + stock + "]";
    }
}