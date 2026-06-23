package com.example.apporderfood.model;

import com.google.gson.annotations.SerializedName;

public class Area {
    @SerializedName("id")    private Integer id;
    @SerializedName("areaName") private String areaName;

    public Integer getId()      { return id; }
    public String getAreaName() { return areaName; }
}
