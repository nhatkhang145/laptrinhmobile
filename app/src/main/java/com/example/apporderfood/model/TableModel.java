package com.example.apporderfood.model;

import com.google.gson.annotations.SerializedName;

/** Model Ban an - is_occupied dieu khien mau hien thi do/xanh */
public class TableModel {

    @SerializedName("id")
    private Integer id;

    @SerializedName("tableName")
    private String tableName;

    @SerializedName("isOccupied")
    private Boolean isOccupied;  // true = do (co khach), false = xanh (trong)

    @SerializedName("area")
    private Area area;
    
    @SerializedName("activeUserId")
    private Integer activeUserId;

    public Integer getId()        { return id; }
    public String getTableName()  { return tableName; }
    public Boolean isOccupied()   { return isOccupied != null && isOccupied; }
    public Area getArea()         { return area; }
    public Integer getActiveUserId() { return activeUserId; }
}
