package com.example.apporderfood.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Area implements Serializable {
    @SerializedName("id")
    private Integer id;

    @SerializedName("areaName")
    private String areaName;

    @SerializedName("isActive")
    private Boolean isActive;

    private Integer itemCount; // Not in DB, but used in UI to count tables

    public Area() {}

    public Area(Integer id, String areaName, Boolean isActive) {
        this.id = id;
        this.areaName = areaName;
        this.isActive = isActive;
    }

    public Area(Integer id, String areaName) {
        this.id = id;
        this.areaName = areaName;
        this.isActive = true;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }
}
