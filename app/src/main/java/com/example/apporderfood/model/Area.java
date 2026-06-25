package com.example.apporderfood.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Area implements Serializable {
    @SerializedName("id")
    private Integer id;

    @SerializedName("areaName")
    private String areaName;

    public Area() {}

    public Area(Integer id, String areaName) {
        this.id = id;
        this.areaName = areaName;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }
}
