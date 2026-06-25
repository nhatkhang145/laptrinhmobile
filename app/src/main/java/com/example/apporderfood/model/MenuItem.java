package com.example.apporderfood.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.math.BigDecimal;

public class MenuItem implements Serializable {
    @SerializedName("id")
    private Integer id;

    @SerializedName("itemName")
    private String itemName;

    @SerializedName("price")
    private BigDecimal price;

    @SerializedName("category")
    private Category category;

    @SerializedName("unit")
    private Unit unit;

    @SerializedName("isAvailable")
    private Boolean isAvailable;

    @SerializedName("imageUrl")
    private String imageUrl;

    public MenuItem() {}

    public MenuItem(Integer id, String itemName, BigDecimal price, Category category, Unit unit, Boolean isAvailable, String imageUrl) {
        this.id = id;
        this.itemName = itemName;
        this.price = price;
        this.category = category;
        this.unit = unit;
        this.isAvailable = isAvailable;
        this.imageUrl = imageUrl;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
