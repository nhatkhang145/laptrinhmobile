package com.example.apporderfood.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Unit implements Serializable {
    @SerializedName("id")
    private Integer id;

    @SerializedName("unitName")
    private String unitName;

    public Unit() {}

    public Unit(Integer id, String unitName) {
        this.id = id;
        this.unitName = unitName;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
}
