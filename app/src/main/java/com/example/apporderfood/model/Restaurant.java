package com.example.apporderfood.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Model Restaurant - Gson tu dong chuyen JSON -> Object nay
 * Ten field phai khop voi ten field JSON tra ve tu Spring Boot
 */
public class Restaurant implements Serializable {

    @SerializedName("id")
    private Integer id;

    @SerializedName("resName")
    private String resName;

    @SerializedName("resDomain")
    private String resDomain;

    @SerializedName("address")
    private String address;

    public Restaurant() {}

    public Restaurant(Integer id, String resName, String resDomain, String address) {
        this.id = id;
        this.resName = resName;
        this.resDomain = resDomain;
        this.address = address;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getResName() { return resName; }
    public void setResName(String resName) { this.resName = resName; }

    public String getResDomain() { return resDomain; }
    public void setResDomain(String resDomain) { this.resDomain = resDomain; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
