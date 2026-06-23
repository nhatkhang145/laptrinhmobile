package com.example.apporderfood.model;

import com.google.gson.annotations.SerializedName;

public class Category {
    @SerializedName("id")      private Integer id;
    @SerializedName("catName") private String catName;

    public Integer getId()     { return id; }
    public String getCatName() { return catName; }
}
