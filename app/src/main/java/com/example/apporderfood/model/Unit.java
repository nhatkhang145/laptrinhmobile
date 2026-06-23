package com.example.apporderfood.model;

import com.google.gson.annotations.SerializedName;

public class Unit {
    @SerializedName("id")       private Integer id;
    @SerializedName("unitName") private String unitName;

    public Integer getId()      { return id; }
    public String getUnitName() { return unitName; }
}
