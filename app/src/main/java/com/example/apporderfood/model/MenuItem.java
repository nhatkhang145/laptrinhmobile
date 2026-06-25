package com.example.apporderfood.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class MenuItem {
    @SerializedName("id")       private Integer id;
    @SerializedName("itemName") private String itemName;
    @SerializedName("price")    private BigDecimal price;
    @SerializedName("category") private Category category;
    @SerializedName("unit")     private Unit unit;
    @SerializedName("isAvailable") private Boolean isAvailable;
    @SerializedName("imageUrl") private String imageUrl;

    public Integer getId()         { return id; }
    public String getItemName()    { return itemName; }
    public BigDecimal getPrice()   { return price; }
    public Category getCategory()  { return category; }
    public Unit getUnit()          { return unit; }
    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
