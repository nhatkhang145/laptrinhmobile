package com.example.apporderfood.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Category implements Serializable {
    @SerializedName("id")
    private Integer id;

    @SerializedName("catName")
    private String catName;

    @SerializedName("description")
    private String description;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("status")
    private Integer status; // 1 = Active, 0 = Hidden

    @SerializedName("itemCount")
    private Integer itemCount;

    public Category() {}

    public Category(Integer id, String catName, String description, String imageUrl, Integer status, Integer itemCount) {
        this.id = id;
        this.catName = catName;
        this.description = description;
        this.imageUrl = imageUrl;
        this.status = status;
        this.itemCount = itemCount;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCatName() { return catName; }
    public void setCatName(String catName) { this.catName = catName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getItemCount() { return itemCount; }
    public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }
}
