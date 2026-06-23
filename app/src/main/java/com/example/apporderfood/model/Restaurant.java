package com.example.apporderfood.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model Restaurant - Gson tu dong chuyen JSON -> Object nay
 * Ten field phai khop voi ten field JSON tra ve tu Spring Boot
 */
public class Restaurant {

    @SerializedName("id")
    private Integer id;

    @SerializedName("resName")
    private String resName;

    @SerializedName("resDomain")
    private String resDomain;

    @SerializedName("address")
    private String address;

    // Getters
    public Integer getId()     { return id; }
    public String getResName() { return resName; }
    public String getResDomain() { return resDomain; }
    public String getAddress() { return address; }
}
