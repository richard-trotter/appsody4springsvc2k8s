package demo.inventory.api.model;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;

public class InventoryItemModel {

    private long id;

    @NotBlank
    private String name;

    private String description;

    private BigDecimal price;

    private String img_alt;

    private String img;

    private int stock;

    public InventoryItemModel() {
    }

    public InventoryItemModel(long id) {
        this.id = id;
    }

    public InventoryItemModel(String name, String description, BigDecimal price, String img_alt, String img, int stock) {
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal value) {
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

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((description == null) ? 0 : description.hashCode());
      result = prime * result + (int) (id ^ (id >>> 32));
      result = prime * result + ((img == null) ? 0 : img.hashCode());
      result = prime * result + ((img_alt == null) ? 0 : img_alt.hashCode());
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((price == null) ? 0 : price.hashCode());
      result = prime * result + stock;
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      InventoryItemModel other = (InventoryItemModel) obj;
      if (description == null) {
        if (other.description != null)
          return false;
      }
      else if (!description.equals(other.description))
        return false;
      if (id != other.id)
        return false;
      if (img == null) {
        if (other.img != null)
          return false;
      }
      else if (!img.equals(other.img))
        return false;
      if (img_alt == null) {
        if (other.img_alt != null)
          return false;
      }
      else if (!img_alt.equals(other.img_alt))
        return false;
      if (name == null) {
        if (other.name != null)
          return false;
      }
      else if (!name.equals(other.name))
        return false;
      if (price == null) {
        if (other.price != null)
          return false;
      }
      else if (!price.equals(other.price))
        return false;
      if (stock != other.stock)
        return false;
      return true;
    }
}